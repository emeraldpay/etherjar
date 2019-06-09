/*
 * Copyright (c) 2016-2018 Infinitape Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.infinitape.etherjar.rpc.transport;

import io.infinitape.etherjar.rpc.Batch;
import io.infinitape.etherjar.rpc.JacksonRpcConverter;
import io.infinitape.etherjar.rpc.RpcConverter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * A round-robin RPC Transport that uses multiple upstreams
 *
 * @author Igor Artamonov
 */
public class RoundRobinRpcTransport implements RpcTransport {

    private AtomicInteger seq = new AtomicInteger(0);
    private Lock validationLock = new ReentrantLock();


    private final List<URI> knownHosts;
    private final AtomicReference<List<RpcTransport>> active = new AtomicReference<>(Collections.emptyList());

    private final ExecutorService executorService;
    private RpcConverter rpcConverter = new JacksonRpcConverter();

    private ScheduledExecutorService scheduler;

    private UpstreamValidator upstreamValidator = new BasicUpstreamValidator();

    public RoundRobinRpcTransport(List<URI> knownHosts) {
        this(knownHosts, Executors.newCachedThreadPool());
    }

    public RoundRobinRpcTransport(List<URI> knownHosts, ExecutorService executorService) {
        if (knownHosts.isEmpty()) {
            throw new IllegalArgumentException("List of known upstreams should not be empty");
        }
        this.knownHosts = knownHosts;

        if (executorService == null) {
            throw new IllegalArgumentException("ExecutorService shouldn't be null");
        }
        this.executorService = executorService;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void setUpstreamValidator(UpstreamValidator upstreamValidator) {
        this.upstreamValidator = upstreamValidator;
    }

    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Start automatic validation of upstream endpoints
     *
     * @param period the period between successive executions
     * @param unit the time unit of the initialDelay and period parameters
     */
    public Future<Boolean> startAutoValidation(long period, TimeUnit unit) {
        if (scheduler == null) {
            scheduler = SchedulerInstance.getInstance();
        }

        Future<Boolean> firstValidation = scheduler.submit(this::revalidate);
        scheduler.scheduleAtFixedRate(this::revalidate, period, period, unit);
        return firstValidation;
    }

    /**
     * Revalidates all known endpoints
     */
    public boolean revalidate() {
        validationLock.lock();
        try {
            List<RpcTransport> transports = new ArrayList<>(knownHosts.size());
            List<Future<URI>> validations = new ArrayList<>(knownHosts.size());
            for (URI uri: knownHosts) {
                Future<URI> f = executorService.submit(
                        () -> upstreamValidator.validate(uri) ? uri : null
                );
                validations.add(f);
            }
            for (Future<URI> uriValidator: validations) {
                URI uri = null;
                try {
                    uri = uriValidator.get();
                } catch (Exception e) { }
                if (uri != null) {
                    transports.add(new DefaultRpcTransport(uri, rpcConverter, executorService));
                }
            }
            active.set(transports);
            return transports.size() > 0;
        } finally {
            validationLock.unlock();
        }
    }

    public boolean hasUpstreams() {
        List<RpcTransport> transports = active.get();
        return !transports.isEmpty();
    }

    public RpcTransport next() {
        List<RpcTransport> transports = active.get();
        if (transports.isEmpty()) {
            return null;
        }
        seq.updateAndGet((c) -> Integer.MAX_VALUE == c ? 0 : c);
        return transports.get(seq.getAndIncrement() % transports.size());
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
    }

    @Override
    public CompletableFuture<BatchStatus> execute(List<Batch.BatchItem<?, ?>> items) {
        RpcTransport next = next();
        if (next == null) {
            CompletableFuture<BatchStatus> error = new CompletableFuture<>();
            error.completeExceptionally(new IllegalStateException("No valid upstreams available"));
            return error;
        }
        return next.execute(items);
    }

    static class Builder {
        private ExecutorService executorService;
        private ScheduledExecutorService scheduler;
        private List<URI> hosts;
        private Long validateSeconds;
        private int minPeers = 3;

        public Builder hosts(List<String> knownHosts) throws URISyntaxException {
            List<URI> hosts = new ArrayList<>(knownHosts.size());
            for (String uri: knownHosts) {
                hosts.add(new URI(uri));
            }
            this.hosts = hosts;
            return this;
        }

        public Builder executor(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder scheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder revalidateEachSeconds(long validateSeconds) {
            if (validateSeconds <= 0) {
                throw new IllegalArgumentException("Validate period should be 1 second or more");
            }
            this.validateSeconds = validateSeconds;
            return this;
        }

        public Builder minPeers(int minPeers) {
            if (minPeers < 0) {
                throw new IllegalArgumentException("minPeers can't be less than 0. Provided: " + minPeers);
            }
            this.minPeers = minPeers;
            return this;
        }

        public RoundRobinRpcTransport build() {
            if (executorService == null) {
                executorService = Executors.newCachedThreadPool();
            }
            RoundRobinRpcTransport transport = new RoundRobinRpcTransport(hosts, executorService);
            BasicUpstreamValidator upstreamValidator = new BasicUpstreamValidator();
            upstreamValidator.setMinPeers(minPeers);
            transport.setUpstreamValidator(upstreamValidator);
            transport.setScheduler(scheduler);
            if (validateSeconds != null) {
                transport.startAutoValidation(validateSeconds, TimeUnit.SECONDS);
            }
            return transport;
        }

    }

    private static class SchedulerInstance {
        private static ScheduledExecutorService instance;

        public static synchronized ScheduledExecutorService getInstance() {
            if (instance == null) {
                instance = Executors.newScheduledThreadPool(1);
            }
            return instance;
        }
    }
}

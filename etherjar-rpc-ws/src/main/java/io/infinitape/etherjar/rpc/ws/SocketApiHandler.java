/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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
package io.infinitape.etherjar.rpc.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Main class that process events produced by WebSockets channel
 *
 * @author Igor Artamonov
 */
public class SocketApiHandler extends SimpleChannelInboundHandler<Object> {

    private AtomicInteger sequence = new AtomicInteger(1);

    private ChannelPromise handshakeFuture;

    private Lock subscribeLock = new ReentrantLock();
    private Map<Integer, Subscription> initializing = new HashMap<>();
    private boolean initialized = false;
    private Channel channel;
    private List<Subscription> subscriptions = new ArrayList<>();
    private JacksonWsConverter rpcConverter = new JacksonWsConverter();

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel ch = ctx.channel();

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content="
                + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        final WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            final TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            SubscriptionJson json = rpcConverter.readSubscription(textFrame.text());
            if (json.getSubscription() != null) {
                boolean consumed = false;
                String id = json.getSubscription();
                for (int i = 0; i < subscriptions.size() && !consumed; i++) {
                    Subscription s = subscriptions.get(i);
                    if (id.equals(s.getId())) {
                        consumed = true;
                        s.onReceive(json);
                    }
                }
                if (!consumed) {
                    System.err.println("Unknown subscription:" + id);
                }
            } else if (json.getId() != null) {
                subscribeLock.lock();
                try {
                    Subscription s = initializing.remove(json.getId());
                    if (s != null) {
                        s.setId(json.getStringResult());
                    } else {
                        System.err.println("Cannot find subscriber " + json.getId());
                    }
                } finally {
                    subscribeLock.unlock();
                }
            }
        } else if (frame instanceof CloseWebSocketFrame) {
            ch.close();
        }

    }

    public void stop() {
        subscribeLock.lock();
        try {
            for (Subscription s: subscriptions) {
                try {
                    s.stop(rpcConverter.getObjectMapper(), sequence.getAndIncrement());
                } catch (Exception e) { }
            }
        } finally {
            subscribeLock.unlock();
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE == evt) {
            subscribeLock.lock();
            try {
                this.channel = ctx.channel();
                for (Subscription s: subscriptions) {
                    internalStart(s);
                }
                initialized = true;
            } finally {
                subscribeLock.unlock();
            }
        }
    }

    /**
     * Starts to listen for the subscription
     *
     * @param subscription subscription
     */
    protected void internalStart(Subscription subscription) {
        Integer id = sequence.getAndIncrement();
        try {
            initializing.put(id, subscription);
            subscription.start(channel, rpcConverter.getObjectMapper(), id);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds new subscription to current connection
     *
     * @param subscription subscription
     */
    public void subscribe(Subscription subscription) {
        subscribeLock.lock();
        try {
            subscriptions.add(subscription);
            if (initialized) {
                internalStart(subscription);
            }
        } finally {
            subscribeLock.unlock();
        }
    }
}

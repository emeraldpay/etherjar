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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.infinitape.etherjar.rpc.JacksonRpcConverter;
import io.infinitape.etherjar.rpc.RpcConverter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Basic upstream validator that verifies that upstream host is available (answer RPC requests) and is not in
 * an initial sync mode
 *
 * @author Igor Artamonov
 */
public class BasicUpstreamValidator implements UpstreamValidator {

    private static final byte[] CHECK_JSON = ("[" +
        "{\"jsonrpc\":\"2.0\",\"method\":\"eth_syncing\",\"params\":[],\"id\":1}," +
        "{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":2}" +
        "]"
    ).getBytes();

    private HttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper;

    private int minPeers = 0;

    public BasicUpstreamValidator() {
        SimpleModule module = new SimpleModule("EtherJar/Validator");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        this.objectMapper = objectMapper;
    }

    public void setMinPeers(int minPeers) {
        if (minPeers < 0) {
            throw new IllegalArgumentException("minPeers can't be less than 0. Provided: " + minPeers);
        }
        this.minPeers = minPeers;
    }

    public boolean validateSyncing(Object result) {
        boolean providesSyncInfo = result instanceof Map;
        if (providesSyncInfo) {
            return false;
        }
        // FALSE, when not syncing
        if (result instanceof Boolean) {
            boolean syncing = (Boolean)result;
            return !syncing;
        }
        return "false".equals(result.toString().toLowerCase());
    }

    public boolean validatePeers(Object result) {
        if (result instanceof String && ((String)result).length() > 2 && ((String)result).startsWith("0x")) {
            int peers = Integer.parseInt(((String)result).substring(2), 16);
            return peers >= minPeers;
        }
        return false;
    }

    public int extractId(Map response) {
        Object value = response.get("id");
        if (value == null) {
            return -1;
        }
        if (Integer.class.isAssignableFrom(value.getClass())) {
            return (Integer)value;
        }
        return Integer.parseInt(value.toString());
    }

    @Override
    public boolean validate(URI uri) {

        boolean validSync = false;
        boolean validPeers = false;

        RequestConfig config = RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(3)).build();

        RequestBuilder requestBuilder = RequestBuilder.create("POST")
            .setUri(uri)
            .addHeader("Content-Type", "application/json")
            .setEntity(new ByteArrayEntity(CHECK_JSON))
            .setConfig(config);

        HttpResponse rcpResponse;
        try {
            rcpResponse = httpClient.execute(requestBuilder.build());
        } catch (IOException e) {
            return false;
        }
        if (rcpResponse.getStatusLine().getStatusCode() != 200) {
            return false;
        } else {
            HttpEntity entity = rcpResponse.getEntity();
            try {
                InputStream content = entity.getContent();
                List<Object> responses = objectMapper.readerFor(List.class).readValue(content);
                for (Object r: responses) {
                    if (!(r instanceof Map)) {
                        return false;
                    }
                    Map data = (Map)r;
                    if (data.get("error") != null || data.get("result") == null) {
                        return false;
                    }
                    int id = extractId(data);
                    if (id == 1) {
                        validSync = validateSyncing(data.get("result"));
                    } else if (id == 2) {
                        validPeers = validatePeers(data.get("result"));
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return validSync && validPeers;
    }
}

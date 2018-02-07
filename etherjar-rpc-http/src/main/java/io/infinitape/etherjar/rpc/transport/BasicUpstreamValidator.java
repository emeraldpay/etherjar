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

import io.infinitape.etherjar.rpc.JacksonRpcConverter;
import io.infinitape.etherjar.rpc.RpcConverter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * Basic upstream validator that verifies that upstream host is available (answer RPC requests) and is not in
 * an initial sync mode
 *
 * @author Igor Artamonov
 */
public class BasicUpstreamValidator implements UpstreamValidator {

    private static final byte[] CHECK_JSON = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_syncing\",\"params\":[],\"id\":1}".getBytes();

    private RpcConverter rpcConverter = new JacksonRpcConverter();
    private HttpClient httpClient = HttpClients.createDefault();;

    @Override
    public boolean validate(URI uri) {
        RequestBuilder requestBuilder = RequestBuilder.create("POST")
            .setUri(uri)
            .addHeader("Content-Type", "application/json")
            .setEntity(new ByteArrayEntity(CHECK_JSON));

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
                Object result = rpcConverter.fromJson(content, Object.class);
                if (result == null) {
                    return false;
                }

                boolean providesSyncInfo = result instanceof Map;
                boolean notSyncing = "false".equals(result.toString().toLowerCase());
                
                return !providesSyncInfo && notSyncing;
            } catch (IOException e) {
                return false;
            }
        }
    }
}

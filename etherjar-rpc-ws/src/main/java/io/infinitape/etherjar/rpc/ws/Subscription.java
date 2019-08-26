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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.infinitape.etherjar.domain.TransactionId;
import io.infinitape.etherjar.rpc.RpcResponseError;
import io.infinitape.etherjar.rpc.json.BlockJson;
import io.infinitape.etherjar.rpc.json.RequestJson;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.*;

/**
 * Ethereum Websocket subscription call
 *
 * @author Igor Artamonov
 */
public abstract class Subscription<T> {

    private List params;

    private List<SubscriptionListener<T>> listeners = new ArrayList<>();
    private Channel channel;
    private String id;

    protected Subscription(List params) {
        this.params = params;
    }

    public void addListener(SubscriptionListener<T> listener) {
        listeners.add(listener);
    }

    public void start(Channel channel, ObjectMapper objectMapper, Integer requestId) throws JsonProcessingException {
        this.channel = channel;
        RequestJson<Integer> json = new RequestJson<>("eth_subscribe", params, requestId);
        channel.write(new TextWebSocketFrame(objectMapper.writeValueAsString(json)));
        channel.flush();
    }

    public void stop(ObjectMapper objectMapper, Integer requestId) throws JsonProcessingException {
        RequestJson<Integer> json = new RequestJson<>("eth_unsubscribe", Collections.singletonList(id), requestId);
        channel.write(new TextWebSocketFrame(objectMapper.writeValueAsString(json)));
        channel.flush();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract void onReceive(SubscriptionJson json);

    public void onReceive(T data) {
        for (SubscriptionListener<T> listener: listeners) {
            try {
                listener.onData(data);
            } catch (Exception e) { }
        }
    }

    /**
     * Called on socket close
     *
     * @param error error if closed as a result of error
     */
    public void onClose(RpcResponseError error) {
        if (error != null) {
            System.err.println("Socket closed with " + error.getMessage());
        }
    }

    public static class Block extends Subscription<BlockJson<TransactionId>> {

        private static final List PARAMS = Arrays.asList(
            "newHeads"
        );

        protected Block() {
            super(PARAMS);
        }

        @Override
        public void onReceive(SubscriptionJson json) {
            BlockJson<TransactionId> blockJson = json.getBlockResult();
            onReceive(blockJson);
        }
    }

}

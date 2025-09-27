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
package io.emeraldpay.etherjar.rpc.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.emeraldpay.etherjar.rpc.RpcResponseError;
import io.emeraldpay.etherjar.rpc.json.BlockJson;
import io.emeraldpay.etherjar.rpc.RequestJson;
import io.emeraldpay.etherjar.rpc.json.TransactionRefJson;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Ethereum Websocket subscription call
 *
 * @author Igor Artamonov
 */
@NullMarked
public abstract class Subscription<T> {

    private final List params;

    private final List<SubscriptionListener<T>> listeners = new ArrayList<>();
    @Nullable
    private Channel channel;
    @Nullable
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
        if (channel != null) {
            channel.write(new TextWebSocketFrame(objectMapper.writeValueAsString(json)));
            channel.flush();
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
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
    public void onClose(@Nullable RpcResponseError error) {
        if (error != null) {
            System.err.println("Socket closed with " + error.getMessage());
        }
    }

    public static class Block extends Subscription<BlockJson<TransactionRefJson>> {

        private final ObjectMapper objectMapper;

        private static final List PARAMS = Arrays.asList(
            "newHeads"
        );

        protected Block(ObjectMapper objectMapper) {
            super(PARAMS);
            this.objectMapper = objectMapper;
        }

        @Override
        public void onReceive(SubscriptionJson json) {
            BlockJson<TransactionRefJson> blockJson = json.getBlockResult(objectMapper);
            onReceive(blockJson);
        }
    }

}

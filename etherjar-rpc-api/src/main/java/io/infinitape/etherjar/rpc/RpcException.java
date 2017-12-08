/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
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
package io.infinitape.etherjar.rpc;

import java.io.IOException;

/**
 * @author Igor Artamonov
 */
public class RpcException extends IOException {

    private int code;
    private String rpcMessage;
    private Object details;

    public RpcException(int code, String rpcMessage, Object details) {
        super("RPC Error " + code + ": " + rpcMessage);
        this.code = code;
        this.rpcMessage = rpcMessage;
        this.details = details;
    }

    public int getCode() {
        return code;
    }

    public String getRpcMessage() {
        return rpcMessage;
    }

    public Object getDetails() {
        return details;
    }
}

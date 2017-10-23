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

package io.infinitape.etherjar.rpc.json;

public class ResponseJson<X,T> {

    private String jsonrpc = "2.0";
    private T id;
    private X result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public T getId() {
        return id;
    }

    public void setId(T id) {
        if (!(Integer.class.isAssignableFrom(id.getClass()) || String.class.isAssignableFrom(id.getClass()))) {
            throw new IllegalArgumentException("ID must be String or Integer");
        }
        this.id = id;
    }

    public X getResult() {
        return result;
    }

    public void setResult(X result) {
        this.result = result;
    }
}

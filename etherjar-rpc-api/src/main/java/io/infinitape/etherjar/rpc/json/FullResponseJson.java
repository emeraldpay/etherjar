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

import io.infinitape.etherjar.rpc.RpcResponseError;

/**
 * Json structure to reference _both_ result and error
 *
 * @author Igor Artamonov
 */
public class FullResponseJson<X, ID> {

    private String jsonrpc = "2.0";
    private ID id;
    private X result;
    private RpcResponseError error;

    public ID getId() {
        return id;
    }

    public X getResult() {
        return result;
    }

    public RpcResponseError getError() {
        return error;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public boolean hasError() {
        return error != null;
    }

    public ResponseJson<X, ID> getResponseOnly() {
        ResponseJson<X, ID> responseJson = new ResponseJson<>();
        responseJson.setId(id);
        responseJson.setResult(result);
        return responseJson;
    }

}

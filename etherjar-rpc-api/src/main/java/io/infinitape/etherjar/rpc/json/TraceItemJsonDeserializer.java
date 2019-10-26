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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TraceItemJsonDeserializer extends EtherJsonDeserializer<TraceItemJson> {

    @Override
    public TraceItemJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        TraceItemJson trace = new TraceItemJson();

        JsonNode typeNode = node.get("type");
        if (typeNode != null) {
            String type = typeNode.asText();
            if (type != null) {
                trace.setType(TraceItemJson.TraceType.valueOf(type.toUpperCase()));
            }
        }

        JsonNode actionNode = node.get("action");
        if (actionNode != null && actionNode.isObject()) {
            TraceItemJson.Action action = new TraceItemJson.Action();
            trace.setAction(action);

            JsonNode callType = actionNode.get("callType");
            if (callType != null) {
                String name = callType.asText();
                if (name != null && name.length() > 0) {
                    action.setCallType(TraceItemJson.CallType.valueOf(name.toUpperCase()));
                }
            }
            action.setFrom(getAddress(actionNode, "from"));
            action.setGas(getLong(actionNode, "gas"));
            action.setInput(getData(actionNode, "input"));
            action.setTo(getAddress(actionNode, "to"));
            action.setValue(getWei(actionNode, "value"));
            action.setInit(getData(actionNode, "init"));
            action.setAddress(getAddress(actionNode, "address"));
            action.setBalance(getWei(actionNode, "balance"));
            action.setRefundAddress(getAddress(actionNode, "refundAddress"));

        }
        trace.setBlockHash(getBlockHash(node, "blockHash"));
        trace.setBlockNumber(getLong(node, "blockNumber"));

        JsonNode resultNode = node.get("result");
        if (resultNode != null && resultNode.isObject()) {
            TraceItemJson.Result result = new TraceItemJson.Result();
            trace.setResult(result);

            result.setGasUsed(getLong(resultNode, "gasUsed"));
            result.setOutput(getData(resultNode, "output"));

            result.setAddress(getAddress(resultNode, "address"));
            result.setCode(getData(resultNode, "code"));
        }
        JsonNode errorNode = node.get("error");
        if (errorNode != null) {
            trace.setError(errorNode.textValue());
        }
        trace.setSubtraces(getLong(node, "subtraces"));
        JsonNode traceAddrNode = node.get("traceAddress");
        if (traceAddrNode != null && traceAddrNode.isArray()) {
            List<Long> traceAddr = new ArrayList<>(traceAddrNode.size());
            for (JsonNode el: traceAddrNode) {
                traceAddr.add(getLong(el));
            }
            trace.setTraceAddress(traceAddr);
        }

        trace.setTransactionHash(getTxHash(node, "transactionHash"));
        trace.setTransactionPosition(getLong(node, "transactionPosition"));

        return trace;
    }
}

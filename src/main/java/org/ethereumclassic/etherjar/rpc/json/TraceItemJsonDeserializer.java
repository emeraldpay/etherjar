package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Artamonov
 */
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
            action.setGas(getQuantity(actionNode, "gas"));
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

            result.setGasUsed(getQuantity(resultNode, "gasUsed"));
            result.setOutput(getData(resultNode, "output"));

            result.setAddress(getAddress(resultNode, "address"));
            result.setCode(getData(resultNode, "code"));
            result.setGasUsed(getQuantity(resultNode, "gasUsed"));
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

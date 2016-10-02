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

        JsonNode actionNode = node.get("action");
        if (actionNode != null && actionNode.isObject()) {
            TraceItemJson.Action action = new TraceItemJson.Action();
            trace.setAction(action);
            JsonNode callNode = actionNode.get("call");
            if (callNode != null && callNode.isObject()) {
                TraceItemJson.ActionCall call = new TraceItemJson.ActionCall();
                action.setCall(call);
                JsonNode callType = callNode.get("callType");
                if (callType != null && callType.isObject()) {
                    //TODO fill with data
                    call.setCallType(new TraceItemJson.CallType());
                }
                call.setFrom(getAddress(callNode, "from"));
                call.setGas(getQuantity(callNode, "gas"));
                call.setInput(getData(callNode, "input"));
                call.setTo(getAddress(callNode, "to"));
                call.setValue(getWei(callNode, "value"));
            }
            JsonNode createNode = actionNode.get("create");
            if (createNode != null && createNode.isObject()) {
                TraceItemJson.ActionCreate create = new TraceItemJson.ActionCreate();
                action.setCreate(create);
                create.setFrom(getAddress(createNode, "from"));
                create.setGas(getQuantity(createNode, "gas"));
                create.setInit(getData(createNode, "init"));
                create.setValue(getWei(createNode, "value"));
            }
            JsonNode suicideNode = actionNode.get("suicide");
            if (suicideNode != null && suicideNode.isObject()) {
                TraceItemJson.ActionSuicide suicide = new TraceItemJson.ActionSuicide();
                action.setSuicide(suicide);
                suicide.setAddress(getAddress(suicideNode, "address"));
                suicide.setBalance(getWei(suicideNode, "balance"));
                suicide.setRefundAddress(getAddress(suicideNode, "refundAddress"));
            }
        }
        trace.setBlockHash(getBlockHash(node, "blockHash"));
        trace.setBlockNumber(getLong(node, "blockNumber"));

        JsonNode resultNode = node.get("result");
        if (resultNode != null && resultNode.isObject()) {
            TraceItemJson.Result result = new TraceItemJson.Result();
            trace.setResult(result);
            JsonNode callNode = resultNode.get("call");
            if (callNode != null && callNode.isObject()) {
                TraceItemJson.ResultCall call = new TraceItemJson.ResultCall();
                result.setCall(call);
                call.setGasUsed(getQuantity(callNode, "gasUsed"));
                call.setOutput(getData(callNode, "output"));
            }
            JsonNode createNode = resultNode.get("create");
            if (createNode != null && createNode.isObject()) {
                TraceItemJson.ResultCreate create = new TraceItemJson.ResultCreate();
                result.setCreate(create);
                create.setAddress(getAddress(createNode, "address"));
                create.setCode(getData(createNode, "code"));
                create.setGasUsed(getQuantity(createNode, "gasUsed"));
            }
            JsonNode failedCallNode = resultNode.get("failedCall");
            if (failedCallNode != null) {
                result.setFailedCall(Collections.emptyList());
            }
            JsonNode noneNode = resultNode.get("none");
            if (noneNode != null) {
                result.setNone(Collections.emptyList());
            }
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

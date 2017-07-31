package io.infinitape.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.infinitape.etherjar.core.HexData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionLogJsonDeserializer extends EtherJsonDeserializer<TransactionLogJson> {

    @Override
    public TransactionLogJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        return deserialize(node);
    }

    public TransactionLogJson deserialize(JsonNode node) {
        TransactionLogJson log = new TransactionLogJson();

        log.setAddress(getAddress(node, "address"));
        log.setBlockHash(getBlockHash(node, "blockHash"));
        log.setBlockNumber(getQuantity(node, "blockNumber").getValue().longValue());
        log.setData(getData(node, "data"));
        log.setLogIndex(getQuantity(node, "logIndex").getValue().longValue());
        List<HexData> topics = new ArrayList<>();
        for (JsonNode topic: node.get("topics")) {
            topics.add(HexData.from(topic.textValue()));
        }
        log.setTopics(topics);
        log.setTransactionHash(getTxHash(node, "transactionHash"));
        log.setTransactionIndex(getQuantity(node, "transactionIndex").getValue().longValue());

        return log;
    }
}

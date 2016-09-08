package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.ethereumclassic.etherjar.model.HexData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Artamonov
 */
public class TransactionLogJsonDeserializer extends EtherJsonDeserializer<TransactionLogJson> {

    @Override
    public TransactionLogJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        return deserialize(node);
    }

    public TransactionLogJson deserialize(JsonNode node) {
        TransactionLogJson log = new TransactionLogJson();

        log.setAddress(getAddress(node, "address"));
        log.setBlockHash(getData(node, "blockHash"));
        log.setBlockNumber(getQuantity(node, "blockNumber").getValue().intValue());
        log.setData(getData(node, "data"));
        log.setLogIndex(getQuantity(node, "logIndex").getValue().intValue());
        List<HexData> topics = new ArrayList<>();
        for (JsonNode topic: node.get("topics")) {
            topics.add(HexData.from(topic.textValue()));
        }
        log.setTopics(topics);
        log.setTransactionHash(getTxHash(node, "transactionHash"));
        log.setTransactionIndex(getQuantity(node, "transactionIndex").getValue().intValue());

        return log;
    }
}

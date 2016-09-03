package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.HexNumber;
import org.ethereumclassic.etherjar.model.HexValue;
import org.ethereumclassic.etherjar.model.TransactionId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Igor Artamonov
 */
public class BlockJsonDeserializer extends EtherJsonDeserializer<BlockJson<?>> {

    private TransactionJsonDeserializer transactionJsonDeserializer = new TransactionJsonDeserializer();

    @Override
    public BlockJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        BlockJson blockJson = new BlockJson();

        JsonNode node = jp.readValueAsTree();
        blockJson.setNumber(getHexNumber(node, "number").getValue().intValue());
        blockJson.setHash(getHexValue(node, "hash"));
        blockJson.setTimestamp(new Date(getHexNumber(node, "timestamp").getValue().longValue() * 1000L));

        List txes = new ArrayList();
        for (JsonNode tx: node.get("transactions")) {
            if (tx.isObject()) {
                txes.add(transactionJsonDeserializer.deserialize(tx));
            } else {
                txes.add(TransactionId.from(tx.textValue()));
            }
        }
        blockJson.setTransactions(txes);

        blockJson.setParentHash(getHexValue(node, "parentHash"));
        blockJson.setSha3Uncles(getHexValue(node, "sha3Uncles"));
        blockJson.setMiner(getAddress(node, "miner"));
        blockJson.setDifficulty(getHexNumber(node, "difficulty"));
        blockJson.setTotalDifficulty(getHexNumber(node, "totalDifficulty"));
        blockJson.setSize(node.get("size").longValue());
        blockJson.setGasLimit(getHexNumber(node, "gasLimit"));
        blockJson.setGasUsed(getHexNumber(node, "gasUsed"));
        blockJson.setExtraData(getHexValue(node, "extraData"));

        List<HexValue> uncles = new ArrayList<>();
        for (JsonNode tx: node.get("uncles")) {
            uncles.add(new HexValue(tx.textValue()));
        }
        blockJson.setUncles(uncles);

        return blockJson;
    }

}

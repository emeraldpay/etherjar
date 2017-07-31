package io.infinitape.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.infinitape.etherjar.core.HexData;
import io.infinitape.etherjar.core.TransactionId;
import io.infinitape.etherjar.core.BlockHash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlockJsonDeserializer extends EtherJsonDeserializer<BlockJson<?>> {

    private TransactionJsonDeserializer transactionJsonDeserializer = new TransactionJsonDeserializer();

    @Override @SuppressWarnings("unchecked")
    public BlockJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        BlockJson blockJson = new BlockJson();

        JsonNode node = jp.readValueAsTree();
        blockJson.setNumber(getQuantity(node, "number").getValue().longValue());
        blockJson.setHash(getBlockHash(node, "hash"));
        blockJson.setTimestamp(new Date(getQuantity(node, "timestamp").getValue().longValue() * 1000L));

        List txes = new ArrayList();
        for (JsonNode tx: node.get("transactions")) {
            if (tx.isObject()) {
                txes.add(transactionJsonDeserializer.deserialize(tx));
            } else {
                txes.add(TransactionId.from(tx.textValue()));
            }
        }
        blockJson.setTransactions(txes);

        blockJson.setParentHash(getBlockHash(node, "parentHash"));
        blockJson.setSha3Uncles(getData(node, "sha3Uncles"));
        blockJson.setMiner(getAddress(node, "miner"));
        blockJson.setDifficulty(getQuantity(node, "difficulty"));
        blockJson.setTotalDifficulty(getQuantity(node, "totalDifficulty"));
        blockJson.setSize(node.get("size").longValue());
        blockJson.setGasLimit(getQuantity(node, "gasLimit"));
        blockJson.setGasUsed(getQuantity(node, "gasUsed"));
        blockJson.setExtraData(getData(node, "extraData"));

        List<HexData> uncles = new ArrayList<>();
        for (JsonNode tx: node.get("uncles")) {
            uncles.add(BlockHash.from(tx.textValue()));
        }
        blockJson.setUncles(uncles);

        return blockJson;
    }

}

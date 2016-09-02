package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.HexNumber;
import org.ethereumclassic.etherjar.model.HexValue;
import org.ethereumclassic.etherjar.rpc.EtherConversionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Igor Artamonov
 */
public class BlockJsonDeserializer extends JsonDeserializer<BlockJson> {

    @Override
    public BlockJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        BlockJson blockJson = new BlockJson();

        JsonNode node = jp.readValueAsTree();
        blockJson.setNumber(getHexNumber(node, "number").getValue().intValue());
        blockJson.setHash(getHexValue(node, "hash"));
        blockJson.setTimestamp(new Date(getHexNumber(node, "timestamp").getValue().longValue() * 1000L));

        List txes = new ArrayList();
        for (JsonNode tx: node.get("transactions")) {
            if (tx.isArray()) {
                //TODO
            } else {
                txes.add(new HexValue(tx.textValue()));
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

    private HexValue getHexValue(JsonNode node, String name) {
        JsonNode subnode = node.get(name);
        if (subnode == null) {
            return null;
        }
        String value = subnode.textValue();
        if (value == null || value.length() == 0 || value.equals("0x")) {
            return null;
        }
        return new HexValue(value);
    }

    private HexNumber getHexNumber(JsonNode node, String name) {
        JsonNode subnode = node.get(name);
        if (subnode == null) {
            return null;
        }
        String value = subnode.textValue();
        if (value == null || value.length() == 0 || value.equals("0x")) {
            return null;
        }
        return HexNumber.parse(value);
    }

    private Address getAddress(JsonNode node, String name) {
        JsonNode subnode = node.get(name);
        if (subnode == null) {
            return null;
        }
        String value = subnode.textValue();
        if (value == null || value.length() == 0 || value.equals("0x")) {
            return null;
        }
        return Address.from(value);
    }
}

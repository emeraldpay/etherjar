package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.ethereumclassic.etherjar.model.HexNumber;

import java.io.IOException;

/**
 * @author Igor Artamonov
 */
public class TransactionJsonDeserializer extends EtherJsonDeserializer<TransactionJson> {

    @Override
    public TransactionJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        return deserialize(node);
    }

    public TransactionJson deserialize(JsonNode node) {
        TransactionJson tx = new TransactionJson();
        tx.setHash(getTxHash(node, "hash"));
        tx.setNonce(getHexNumber(node, "nonce").getValue().longValue());
        tx.setBlockHash(getHexValue(node, "blockHash"));
        HexNumber blockNumber = getHexNumber(node, "blockNumber");
        if (blockNumber != null)  {
            tx.setBlockNumber(blockNumber.getValue().intValue());
        }
        HexNumber txIndex = getHexNumber(node, "transactionIndex");
        if (txIndex != null) {
            tx.setTransactionIndex(txIndex.getValue().intValue());
        }
        tx.setFrom(getAddress(node, "from"));
        tx.setTo(getAddress(node, "to"));
        tx.setValue(getWei(node, "value"));
        tx.setGasPrice(getWei(node, "gasPrice"));
        tx.setGas(getHexNumber(node, "gas"));
        tx.setInput(getHexValue(node, "input"));

        return tx;
    }
}

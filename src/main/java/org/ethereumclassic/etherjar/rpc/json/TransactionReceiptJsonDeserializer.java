package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Artamonov
 */
public class TransactionReceiptJsonDeserializer extends EtherJsonDeserializer<TransactionReceiptJson> {

    private TransactionLogJsonDeserializer transactionLogJsonDeserializer = new TransactionLogJsonDeserializer();

    @Override
    public TransactionReceiptJson deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        TransactionReceiptJson receipt = new TransactionReceiptJson();

        receipt.setBlockHash(getBlockHash(node, "blockHash"));
        receipt.setBlockNumber(getQuantity(node, "blockNumber").getValue().longValue());
        receipt.setContractAddress(getAddress(node, "contractAddress"));
        receipt.setCumulativeGasUsed(getQuantity(node, "cumulativeGasUsed"));
        receipt.setGasUsed(getQuantity(node, "gasUsed"));
        receipt.setTransactionHash(getTxHash(node, "transactionHash"));
        receipt.setTransactionIndex(getQuantity(node, "transactionIndex").getValue().longValue());

        List<TransactionLogJson> logs = new ArrayList<>();
        if (node.hasNonNull("logs")) {
            for (JsonNode log: node.get("logs")) {
                logs.add(transactionLogJsonDeserializer.deserialize(log));
            }
        }
        receipt.setLogs(logs);

        return receipt;
    }
}

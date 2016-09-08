package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.ethereumclassic.etherjar.model.*;

/**
 * Utility class for Ethereum RPC JSON deserialization
 *
 * @author Igor Artamonov
 */
public abstract class EtherJsonDeserializer<T> extends JsonDeserializer<T> {

    protected String getHexString(JsonNode node, String name) {
        JsonNode subnode = node.get(name);
        if (subnode == null) {
            return null;
        }
        String value = subnode.textValue();
        if (value == null || value.length() == 0 || value.equals("0x")) {
            return null;
        }
        return value;
    }

    protected HexData getData(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return HexData.from(value);
    }

    protected HexQuantity getQuantity(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return HexQuantity.from(value);
    }

    protected Address getAddress(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return Address.from(value);
    }

    protected TransactionId getTxHash(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return TransactionId.from(value);
    }

    protected Wei getWei(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return new Wei(value);
    }

    protected BlockHash getBlockHash(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return BlockHash.from(value);
    }
}

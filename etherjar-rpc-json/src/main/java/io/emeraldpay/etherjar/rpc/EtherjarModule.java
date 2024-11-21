package io.emeraldpay.etherjar.rpc;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.emeraldpay.etherjar.domain.*;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;
import io.emeraldpay.etherjar.rpc.json.*;

/**
 * A configuration for Jackson JSON mapper to support Etherjar types
 */
public class EtherjarModule extends SimpleModule {

    public EtherjarModule() {
        super("etherjar");
        addSerializer(HexData.class, new HexDataSerializer());
        addSerializer(Wei.class, new WeiSerializer());
        addSerializer(HexQuantity.class, new HexQuantitySerializer());

        addDeserializer(HexData.class, new HexDataDeserializer());
        addDeserializer(Hex32.class, new Hex32Deserializer());
        addDeserializer(Wei.class, new WeiDeserializer());
        addDeserializer(HexQuantity.class, new HexQuantityDeserializer());
        addDeserializer(BlockHash.class, new BlockHashDeserializer());
        addDeserializer(TransactionId.class, new TransactionIdDeserializer());
        addDeserializer(Address.class, new AddressDeserializer());
        addDeserializer(MethodId.class, new MethodIdDeserializer());
        addDeserializer(Bloom.class, new BloomDeserializer());

        addKeySerializer(Address.class, new HexDataSerializer.AsKey());

        addKeyDeserializer(Address.class, new AddressDeserializer.FromKey());
        addKeyDeserializer(Hex32.class, new Hex32Deserializer.FromKey());
    }
}

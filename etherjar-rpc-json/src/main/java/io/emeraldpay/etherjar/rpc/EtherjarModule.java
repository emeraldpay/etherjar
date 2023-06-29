package io.emeraldpay.etherjar.rpc;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;
import io.emeraldpay.etherjar.rpc.json.HexDataSerializer;
import io.emeraldpay.etherjar.rpc.json.HexQuantitySerializer;
import io.emeraldpay.etherjar.rpc.json.WeiSerializer;

public class EtherjarModule extends SimpleModule {

    public EtherjarModule() {
        super("etherjar");
        addSerializer(HexData.class, new HexDataSerializer());
        addSerializer(Wei.class, new WeiSerializer());
        addSerializer(HexQuantity.class, new HexQuantitySerializer());
    }
}

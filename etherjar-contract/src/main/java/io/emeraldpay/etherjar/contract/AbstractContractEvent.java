package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.EventId;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rpc.json.TransactionLogJson;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContractEvent extends ContractEvent {

    public abstract EventId getEventId();

    public abstract List<Hex32> getArguments();

    public abstract HexData getData();

    @Override
    public void writeTo(TransactionLogJson log) {
        List<Hex32> topics = new ArrayList<>(1 + getArguments().size());
        topics.add(getEventId());
        topics.addAll(getArguments());
        log.setTopics(topics);
        log.setData(getData());
    }

}

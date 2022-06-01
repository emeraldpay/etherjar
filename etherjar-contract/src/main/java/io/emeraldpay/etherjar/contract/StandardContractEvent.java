package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.EventId;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.util.Collections;
import java.util.List;

public class StandardContractEvent extends AbstractContractEvent {

    private final EventId eventId;
    private final List<Hex32> arguments;
    private final HexData data;

    public StandardContractEvent(EventId eventId, List<Hex32> arguments, HexData data) {
        if (eventId == null) {
            throw new NullPointerException("EventId is not provided");
        }
        this.eventId = eventId;
        if (arguments == null) {
            this.arguments = Collections.emptyList();
        } else if (arguments.size() > 3) {
            throw new IllegalArgumentException("And event may contain 3 arguments at most. Provided: " + arguments.size());
        } else {
            this.arguments = arguments;
        }
        if (data == null) {
            this.data = HexData.empty();
        } else {
            this.data = data;
        }
    }

    public StandardContractEvent(EventId eventId, List<Hex32> arguments) {
        this(eventId, arguments, HexData.empty());
    }

    public StandardContractEvent(EventId eventId) {
        this(eventId, Collections.emptyList());
    }

    @Override
    public EventId getEventId() {
        return eventId;
    }

    @Override
    public List<Hex32> getArguments() {
        return arguments;
    }

    @Override
    public HexData getData() {
        return data;
    }

}

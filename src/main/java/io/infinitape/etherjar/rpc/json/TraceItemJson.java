package io.infinitape.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.infinitape.etherjar.model.*;
import io.infinitape.etherjar.model.*;

import java.util.List;

/**
 * @author Igor Artamonov
 */
@JsonDeserialize(using = TraceItemJsonDeserializer.class)
public class TraceItemJson {

    private TraceType type;
    private Action action;
    private BlockHash blockHash;
    private Long blockNumber;
    private Result result;
    private String error;
    private Long subtraces;
    private List<Long> traceAddress;
    private TransactionId transactionHash;
    private Long transactionPosition;

    public Action getAction() {
        return action;
    }

    public TraceType getType() {
        return type;
    }

    public void setType(TraceType type) {
        this.type = type;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public BlockHash getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(BlockHash blockHash) {
        this.blockHash = blockHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getSubtraces() {
        return subtraces;
    }

    public void setSubtraces(Long subtraces) {
        this.subtraces = subtraces;
    }

    public List<Long> getTraceAddress() {
        return traceAddress;
    }

    public void setTraceAddress(List<Long> traceAddress) {
        this.traceAddress = traceAddress;
    }

    public TransactionId getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(TransactionId transactionHash) {
        this.transactionHash = transactionHash;
    }

    public Long getTransactionPosition() {
        return transactionPosition;
    }

    public void setTransactionPosition(Long transactionPosition) {
        this.transactionPosition = transactionPosition;
    }

    public static class Action {

        private CallType callType;
        private Address from;
        private HexQuantity gas;
        private HexData init;
        private HexData input;
        private Address to;
        private Wei value;
        private Address address;
        private Wei balance;
        private Address refundAddress;

        public CallType getCallType() {
            return callType;
        }

        public void setCallType(CallType callType) {
            this.callType = callType;
        }

        public Address getFrom() {
            return from;
        }

        public void setFrom(Address from) {
            this.from = from;
        }

        public HexQuantity getGas() {
            return gas;
        }

        public void setGas(HexQuantity gas) {
            this.gas = gas;
        }

        public HexData getInit() {
            return init;
        }

        public void setInit(HexData init) {
            this.init = init;
        }

        public HexData getInput() {
            return input;
        }

        public void setInput(HexData input) {
            this.input = input;
        }

        public Address getTo() {
            return to;
        }

        public void setTo(Address to) {
            this.to = to;
        }

        public Wei getValue() {
            return value;
        }

        public void setValue(Wei value) {
            this.value = value;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Wei getBalance() {
            return balance;
        }

        public void setBalance(Wei balance) {
            this.balance = balance;
        }

        public Address getRefundAddress() {
            return refundAddress;
        }

        public void setRefundAddress(Address refundAddress) {
            this.refundAddress = refundAddress;
        }
    }

    public static enum CallType {
        NONE, CALL, CALLCODE, DELEGATECALL;
    }

    public static enum TraceType {
        CREATE, CALL, SUICIDE;
    }

    public static class Result {
        private HexQuantity gasUsed;
        private HexData output;
        private Address address;
        private HexData code;

        public HexQuantity getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(HexQuantity gasUsed) {
            this.gasUsed = gasUsed;
        }

        public HexData getOutput() {
            return output;
        }

        public void setOutput(HexData output) {
            this.output = output;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public HexData getCode() {
            return code;
        }

        public void setCode(HexData code) {
            this.code = code;
        }
    }

}

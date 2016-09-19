package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.ethereumclassic.etherjar.model.*;

import java.util.List;

/**
 * @author Igor Artamonov
 */
@JsonDeserialize(using = TraceItemJsonDeserializer.class)
public class TraceItemJson {

    private Action action;
    private BlockHash blockHash;
    private Long blockNumber;
    private Result result;
    private Long subtraces;
    private List<Long> traceAddress;
    private TransactionId transactionHash;
    private Long transactionPosition;

    public Action getAction() {
        return action;
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
        private ActionCall call;
        private ActionCreate create;

        public ActionCall getCall() {
            return call;
        }

        public void setCall(ActionCall call) {
            this.call = call;
        }

        public ActionCreate getCreate() {
            return create;
        }

        public void setCreate(ActionCreate create) {
            this.create = create;
        }
    }

    public static class ActionCreate {
        private Address from;
        private HexQuantity gas;
        private HexData init;
        private Wei value;

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

        public Wei getValue() {
            return value;
        }

        public void setValue(Wei value) {
            this.value = value;
        }
    }

    public static class ActionCall {
        private CallType callType;
        private Address from;
        private HexQuantity gas;
        private HexData input;
        private Address to;
        private Wei value;

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
    }

    public static class CallType {
        private List call;

        public List getCall() {
            return call;
        }

        public void setCall(List call) {
            this.call = call;
        }
    }

    public static class Result {
        private ResultCall call;
        private ResultCreate create;
        private List failedCall;

        public ResultCall getCall() {
            return call;
        }

        public void setCall(ResultCall call) {
            this.call = call;
        }

        public ResultCreate getCreate() {
            return create;
        }

        public void setCreate(ResultCreate create) {
            this.create = create;
        }

        public List getFailedCall() {
            return failedCall;
        }

        public void setFailedCall(List failedCall) {
            this.failedCall = failedCall;
        }
    }

    public static class ResultCall {
        private HexQuantity gasUsed;
        private HexData output;

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
    }

    public static class ResultCreate {
        private Address address;
        private HexData code;
        private HexQuantity gasUsed;

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

        public HexQuantity getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(HexQuantity gasUsed) {
            this.gasUsed = gasUsed;
        }
    }

}

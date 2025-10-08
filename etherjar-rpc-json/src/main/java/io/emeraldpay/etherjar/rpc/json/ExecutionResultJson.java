package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionResultJson {
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long gas;

    private boolean failed;

    private HexData returnValue;

    private List<StructLog> structLogs;

    public Long getGas() {
        return gas;
    }

    public void setGas(Long gas) {
        this.gas = gas;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public HexData getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(HexData returnValue) {
        this.returnValue = returnValue;
    }

    public List<StructLog> getStructLogs() {
        return structLogs;
    }

    public void setStructLogs(List<StructLog> structLogs) {
        this.structLogs = structLogs;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionResultJson that = (ExecutionResultJson) o;
        return failed == that.failed && Objects.equals(gas, that.gas)
            && Objects.equals(returnValue, that.returnValue)
            && Objects.equals(structLogs, that.structLogs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gas, failed, returnValue);
    }

    public static class StructLog {
        private Long pc;
        private String op;
        private Long gas;
        private Long gasCost;
        private Long depth;
        private List<HexData> stack;
        @JsonDeserialize(keyUsing = DirectHex32Deserializer.FromKey.class, contentUsing = DirectHex32Deserializer.class)
        private Map<Hex32, Hex32> storage;
        private HexData memory;
        private Long memSize;
        private Long refund;

        public Long getPc() {
            return pc;
        }

        public void setPc(Long pc) {
            this.pc = pc;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

        public Long getGas() {
            return gas;
        }

        public void setGas(Long gas) {
            this.gas = gas;
        }

        public Long getGasCost() {
            return gasCost;
        }

        public void setGasCost(Long gasCost) {
            this.gasCost = gasCost;
        }

        public Long getDepth() {
            return depth;
        }

        public void setDepth(Long depth) {
            this.depth = depth;
        }

        public List<HexData> getStack() {
            return stack;
        }

        public void setStack(List<HexData> stack) {
            this.stack = stack;
        }

        public Map<Hex32, Hex32> getStorage() {
            return storage;
        }

        public void setStorage(Map<Hex32, Hex32> storage) {
            this.storage = storage;
        }

        public HexData getMemory() {
            return memory;
        }

        public void setMemory(HexData memory) {
            this.memory = memory;
        }

        public Long getMemSize() {
            return memSize;
        }

        public void setMemSize(Long memSize) {
            this.memSize = memSize;
        }

        public Long getRefund() {
            return refund;
        }

        public void setRefund(Long refund) {
            this.refund = refund;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            StructLog structLog = (StructLog) o;
            return Objects.equals(pc, structLog.pc)
                && Objects.equals(op, structLog.op)
                && Objects.equals(gas, structLog.gas)
                && Objects.equals(gasCost, structLog.gasCost)
                && Objects.equals(depth, structLog.depth)
                && Objects.equals(stack, structLog.stack)
                && Objects.equals(storage, structLog.storage)
                && Objects.equals(memory, structLog.memory)
                && Objects.equals(memSize, structLog.memSize)
                && Objects.equals(refund, structLog.refund);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pc, op, gas, gasCost, depth, memSize, refund);
        }
    }
}

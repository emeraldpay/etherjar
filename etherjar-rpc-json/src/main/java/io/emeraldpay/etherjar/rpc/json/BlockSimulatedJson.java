package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockSimulatedJson extends BlockJson<TransactionRefJson>  {

    /**
     * Log events emitted during call. This includes ETH logs, if <code>traceTransfers</code> is enabled
     */
    private List<CallResultLog> calls;

    public List<CallResultLog> getCalls() {
        return calls;
    }

    public void setCalls(List<CallResultLog> calls) {
        this.calls = calls;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CallResultLog {

        /**
         * Transactions return data
         */
        private HexData returnData;

        /**
         * Log events emitted during call. This includes ETH logs, if traceTransfers is enabled
         */
        private List<TransactionLogJson> logs;

        /**
         * Gas used by the transaction
         */
        private HexQuantity gasUsed;

        /**
         * Status indicating that the transaction succeeded
         * 1 is success
         */
        @JsonDeserialize(using = HexLongDeserializer.class)
        @JsonSerialize(using = HexLongSerializer.class)
        private Long status;

        public HexData getReturnData() {
            return returnData;
        }

        public void setReturnData(HexData returnData) {
            this.returnData = returnData;
        }

        public List<TransactionLogJson> getLogs() {
            return logs;
        }

        public void setLogs(List<TransactionLogJson> logs) {
            this.logs = logs;
        }

        public HexQuantity getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(HexQuantity gasUsed) {
            this.gasUsed = gasUsed;
        }

        public Long getStatus() {
            return status;
        }

        public void setStatus(Long status) {
            this.status = status;
        }

        @JsonIgnore
        public boolean isSuccessful() {
            return status == 1;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CallResultLog that)) return false;
            return Objects.equals(returnData, that.returnData)
                && Objects.equals(logs, that.logs)
                && Objects.equals(gasUsed, that.gasUsed)
                && Objects.equals(status, that.status);
        }

        @Override
        public int hashCode() {
            return Objects.hash(returnData, logs, gasUsed, status);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockSimulatedJson that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(calls, that.calls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), calls);
    }
}


/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.infinitape.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.infinitape.etherjar.domain.Address;
import io.infinitape.etherjar.domain.BlockHash;
import io.infinitape.etherjar.domain.TransactionId;
import io.infinitape.etherjar.domain.Wei;
import io.infinitape.etherjar.hex.HexData;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@JsonDeserialize(using = TraceItemJsonDeserializer.class)
public class TraceItemJson implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TraceItemJson)) return false;

        TraceItemJson that = (TraceItemJson) o;

        if (type != that.type) return false;
        if (action != null ? !action.equals(that.action) : that.action != null) return false;
        if (blockHash != null ? !blockHash.equals(that.blockHash) : that.blockHash != null) return false;
        if (blockNumber != null ? !blockNumber.equals(that.blockNumber) : that.blockNumber != null) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        if (subtraces != null ? !subtraces.equals(that.subtraces) : that.subtraces != null) return false;
        if (traceAddress != null ? !traceAddress.equals(that.traceAddress) : that.traceAddress != null) return false;
        if (transactionHash != null ? !transactionHash.equals(that.transactionHash) : that.transactionHash != null)
            return false;
        return transactionPosition != null ? transactionPosition.equals(that.transactionPosition) : that.transactionPosition == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0);
        result = 31 * result + (transactionHash != null ? transactionHash.hashCode() : 0);
        return result;
    }

    public static class Action implements Serializable {

        private CallType callType;
        private Address from;
        private BigInteger gas;
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

        public BigInteger getGas() {
            return gas;
        }

        public void setGas(BigInteger gas) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Action)) return false;

            Action action = (Action) o;

            if (callType != action.callType) return false;
            if (from != null ? !from.equals(action.from) : action.from != null) return false;
            if (gas != null ? !gas.equals(action.gas) : action.gas != null) return false;
            if (init != null ? !init.equals(action.init) : action.init != null) return false;
            if (input != null ? !input.equals(action.input) : action.input != null) return false;
            if (to != null ? !to.equals(action.to) : action.to != null) return false;
            if (value != null ? !value.equals(action.value) : action.value != null) return false;
            if (address != null ? !address.equals(action.address) : action.address != null) return false;
            if (balance != null ? !balance.equals(action.balance) : action.balance != null) return false;
            return refundAddress != null ? refundAddress.equals(action.refundAddress) : action.refundAddress == null;
        }

        @Override
        public int hashCode() {
            int result = callType != null ? callType.hashCode() : 0;
            result = 31 * result + (from != null ? from.hashCode() : 0);
            result = 31 * result + (gas != null ? gas.hashCode() : 0);
            result = 31 * result + (init != null ? init.hashCode() : 0);
            result = 31 * result + (input != null ? input.hashCode() : 0);
            result = 31 * result + (to != null ? to.hashCode() : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (address != null ? address.hashCode() : 0);
            result = 31 * result + (balance != null ? balance.hashCode() : 0);
            result = 31 * result + (refundAddress != null ? refundAddress.hashCode() : 0);
            return result;
        }
    }

    public static enum CallType {
        NONE, CALL, CALLCODE, DELEGATECALL;
    }

    public static enum TraceType {
        CREATE, CALL, SUICIDE;
    }

    public static class Result implements Serializable {
        private BigInteger gasUsed;
        private HexData output;
        private Address address;
        private HexData code;

        public BigInteger getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(BigInteger gasUsed) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;

            Result result = (Result) o;

            if (gasUsed != null ? !gasUsed.equals(result.gasUsed) : result.gasUsed != null) return false;
            if (output != null ? !output.equals(result.output) : result.output != null) return false;
            if (address != null ? !address.equals(result.address) : result.address != null) return false;
            return code != null ? code.equals(result.code) : result.code == null;
        }

        @Override
        public int hashCode() {
            int result = gasUsed != null ? gasUsed.hashCode() : 0;
            result = 31 * result + (output != null ? output.hashCode() : 0);
            result = 31 * result + (address != null ? address.hashCode() : 0);
            result = 31 * result + (code != null ? code.hashCode() : 0);
            return result;
        }
    }

}

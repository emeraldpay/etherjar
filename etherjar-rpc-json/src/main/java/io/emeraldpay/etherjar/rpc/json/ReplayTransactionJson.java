/*
 * Copyright (c) 2022 EmeraldPay Inc, All Rights Reserved.
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
package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.emeraldpay.etherjar.domain.TransactionId;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.util.List;


@JsonDeserialize(using = ReplayTransactionJsonDeserializer.class)
public class ReplayTransactionJson {
    private HexData output;
    private TransactionId transactionHash;
    private StateDiffJson stateDiff;
    private List<TraceItemJson> trace;
    private VmTraceJson vmTrace;

    public HexData getOutput() {
        return output;
    }

    public void setOutput(HexData output) {
        this.output = output;
    }

    public TransactionId getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(TransactionId transactionHash) {
        this.transactionHash = transactionHash;
    }

    public StateDiffJson getStateDiff() {
        return stateDiff;
    }

    public void setStateDiff(StateDiffJson stateDiff) {
        this.stateDiff = stateDiff;
    }

    public List<TraceItemJson> getTrace() {
        return trace;
    }

    public void setTrace(List<TraceItemJson> trace) {
        this.trace = trace;
    }

    public VmTraceJson getVmTrace() {
        return vmTrace;
    }

    public void setVmTrace(VmTraceJson vmTrace) {
        this.vmTrace = vmTrace;
    }
}

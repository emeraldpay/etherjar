/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
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

package io.emeraldpay.etherjar.domain;

import io.emeraldpay.etherjar.hex.HexData;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Transaction signature with support of Replay Protection (EIP-155)
 */
@NullMarked
public class TransactionSignature {

    private @Nullable ChainId chainId;

    private @Nullable HexData r;
    private @Nullable HexData s;
    private @Nullable Integer v;

    private @Nullable Integer yParity;

    public TransactionSignature() {
    }

    public @Nullable ChainId getChainId() {
        return chainId;
    }

    public void setChainId(@Nullable ChainId chainId) {
        this.chainId = chainId;
    }

    public @Nullable HexData getR() {
        return r;
    }

    public void setR(@Nullable HexData r) {
        this.r = r;
    }

    public @Nullable HexData getS() {
        return s;
    }

    public void setS(@Nullable HexData s) {
        this.s = s;
    }

    public @Nullable Integer getV() {
        return v;
    }

    public void setV(@Nullable Integer v) {
        if (v == null || v < 0) {
            throw new IllegalArgumentException("Invalid V: " + v);
        }
        this.v = v;
    }

    public @Nullable ChainId getExtractedChainId() {
        if (!isProtected()) {
            return null;
        }
        return new ChainId((v - 35) / 2);
    }

    public @Nullable Integer getNormalizedV() {
        if (chainId == null) {
            return v;
        }
        if (v == null) {
            return null;
        }
        return v - chainId.getValue() * 2 - 35 + 27;
    }

    public boolean isProtected() {
        if (v == null) {
            throw new IllegalStateException("Not initialized");
        }
        if (v == 27 || v == 28) {
            return false;
        }
        return true;
    }

    public @Nullable Integer getYParity() {
        return yParity;
    }

    public void setYParity(@Nullable Integer yParity) {
        this.yParity = yParity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionSignature)) return false;
        TransactionSignature that = (TransactionSignature) o;
        return Objects.equals(chainId, that.chainId) && Objects.equals(r, that.r) && Objects.equals(s, that.s) && Objects.equals(v, that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chainId, s, v);
    }
}

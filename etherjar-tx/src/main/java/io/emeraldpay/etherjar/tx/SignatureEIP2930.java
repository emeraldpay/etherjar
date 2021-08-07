/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
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
package io.emeraldpay.etherjar.tx;

import java.math.BigInteger;

/**
 * Signature for a EIP-2930 type of transaction.
 *
 * @see <a href="https://eips.ethereum.org/EIPS/eip-2930">https://eips.ethereum.org/EIPS/eip-2930</a>
 */
public class SignatureEIP2930 extends Signature {

    private int yParity;
    private int chainId;

    public SignatureEIP2930() {
        setV(26);
    }

    public SignatureEIP2930(byte[] message, int yParity, int chainId, BigInteger r, BigInteger s) {
        super(message, 26, r, s);
        this.yParity = yParity;
        this.chainId = chainId;
    }

    public int getYParity() {
        return yParity;
    }

    public void setYParity(int yParity) {
        this.yParity = yParity;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public int getRecId() {
        return getYParity();
    }

    @Override
    public SignatureType getType() {
        return SignatureType.EIP2930;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureEIP2930 that = (SignatureEIP2930) o;
        if (!super.canEqual(that)) return false;
        return yParity == that.yParity && chainId == that.chainId;
    }
}

/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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
package io.infinitape.etherjar.tx;

import java.math.BigInteger;

/**
 * Signature with replay protection (EIP-155).
 *
 * v = CHAIN_ID * 2 + 35
 * v = CHAIN_ID * 2 + 36
 *
 * @link https://github.com/ethereum/eips/issues/155
 */
public class SignatureEip155 extends Signature {

    private int chainId;

    public SignatureEip155(int chainId) {
        this.chainId = chainId;
    }

    public SignatureEip155(int chainId, byte[] message, int v, BigInteger r, BigInteger s) {
        super(message, v, r, s);
        this.chainId = chainId;
    }

    @Override
    protected int getRecId() {
        if (getV() == 27 || getV() == 28) {
            return super.getRecId();
        }
        return getV() - chainId * 2 - 35;
    }

    public int getChainId() {
        return chainId;
    }

    public static int extractChainId(int v) {
        if (v == 27 || v == 26) {
            return 1;
        }
        int x = 35;
        if (((v - 36) & 1) == 0) {
            x = 36;
        }
        return (v - x) / 2;
    }
}

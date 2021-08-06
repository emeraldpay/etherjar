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

public enum TransactionType {

    /**
     * A standard transaction available in Ethereum from the beginning
     */
    STANDARD((byte)-1),

    /**
     * Transaction with Access List introduced by EIP-2930 and available since Berlin Fork of Ethereum Mainnet.
     */
    ACCESS_LIST((byte)1),

    /**
     * Transaction with Gas Max and Priority prices.
     * @see <a href="https://eips.ethereum.org/EIPS/eip-1559">EIP-1559</a>
     */
    GAS_PRIORITY((byte)2);

    private final byte flag;

    TransactionType(byte flag) {
        this.flag = flag;
    }

    /**
     * Get type of the encoded transaction, based of the first byte of a raw tx
     *
     * @see <a href="https://eips.ethereum.org/EIPS/eip-2718">EIP-2718</a>
     * @param prefix first byte of encoded raw transaction
     * @return transaction type
     */
    public static TransactionType fromPrefix(byte prefix) {
        // SPEC:
        // If it starts with a value in the range [0, 0x7f] then it is a new transaction type, if it starts
        // with a value in the range [0xc0, 0xfe] then it is a legacy transaction type. 0xff is not realistic
        // for an RLP encoded transaction, so it is reserved for future use as an extension sentinel value.
        //

        // convert signed byte to unsigned int to easier validation
        int u = ((int)prefix) & 0xff;
        if (u <= 0x7f) {
            //
            if (u == 2) {
                return TransactionType.GAS_PRIORITY;
            }
            if (u == 1) {
                return TransactionType.ACCESS_LIST;
            }
            throw new IllegalArgumentException("Unsupported type: 0x" + Integer.toHexString(u));
        }
        if (u >= 0xcf && u <= 0xfe) {
            return TransactionType.STANDARD;
        }
        throw new IllegalArgumentException("Unsupported type: 0x" + Integer.toHexString(u));
    }

    public byte getFlag() {
        return flag;
    }

    public boolean isFlagPrefixed() {
        return this == STANDARD;
    }
}

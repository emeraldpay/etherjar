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

import io.emeraldpay.etherjar.domain.Wei;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;

/**
 *
 * SPEC:
 * <pre>
 *  There is a base fee per gas in protocol [....].
 *  The base fee per gas is burned.
 *  Transactions specify the maximum fee per gas they are willing to give to miners to incentivize them to include their transaction (aka: priority fee).
 *  Transactions also specify the maximum fee per gas they are willing to pay total (aka: max fee), which covers both the priority fee and the block's network fee per gas (aka: base fee).
 *  The transaction will always pay the base fee per gas of the block it was included in, and they will pay the priority fee per gas set in the transaction, as long as the combined amount of the two fees doesn't exceed the transaction's maximum fee per gas.
 *  </pre>
 *
 * Summary:
 * <ul>
 *     <li><code>base_fee</code> - enforced by block, may be unknown to sender at moment of tx preparation</li>
 *     <li><code>priority_fee</code> - fee paid to miner, on top of <code>base_fee</code></li>
 *     <li><code>max_fee</code> - limit to pay in total (<code>base_fee + priority_fee</code>)</li>
 * </ul>
 *  base fee
 *
 * @see <a href="https://eips.ethereum.org/EIPS/eip-1559">EIP-1559</a>
 */
public class TransactionWithGasPriority extends TransactionWithAccess {

    private Wei priorityGasPrice;

    public TransactionWithGasPriority() {
    }

    public TransactionWithGasPriority(TransactionWithGasPriority other) {
        super(other);
        this.priorityGasPrice = other.priorityGasPrice;
    }

    /**
     * NOTE: same a default {@link #getGasPrice()}
     *
     * @return max_fee gap price
     */
    public Wei getMaxGasPrice() {
        return getGasPrice();
    }

    /**
     * Set max_fee price, same a default {@link #getGasPrice()}
     *
     * @param price max_fee price
     */
    public void setMaxGasPrice(Wei price) {
        setGasPrice(price);
    }

    public void setMaxGasPrice(BigInteger price) {
        setGasPrice(new Wei(price));
    }

    @Override
    public void setGasPrice(Wei gasPrice) {
        this.transactionId = null;
        super.setGasPrice(gasPrice);
        if (priorityGasPrice == null) {
            priorityGasPrice = gasPrice;
        }
    }

    /**
     *
     * @return priority_fee
     */
    public Wei getPriorityGasPrice() {
        return priorityGasPrice;
    }

    /**
     *
     * @param priorityGasPrice priority_fee
     */
    public void setPriorityGasPrice(Wei priorityGasPrice) {
        this.transactionId = null;
        this.priorityGasPrice = priorityGasPrice;
    }

    public void setPriorityGasPrice(BigInteger priorityGasPrice) {
        setPriorityGasPrice(new Wei(priorityGasPrice));
    }

    @Override
    public TransactionType getType() {
        return TransactionType.GAS_PRIORITY;
    }

    @Override
    public byte[] hash() {
        byte[] rlp = TransactionEncoder.DEFAULT.encode(this, false);
        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        return keccak.digest();
    }
}

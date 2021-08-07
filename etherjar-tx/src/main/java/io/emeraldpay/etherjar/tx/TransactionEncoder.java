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

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rlp.RlpWriter;

import java.io.ByteArrayOutputStream;

public class TransactionEncoder {

    public static final TransactionEncoder DEFAULT = new TransactionEncoder();

    public byte[] encode(Transaction tx, boolean includeSignature) {
        if (tx.getType() == TransactionType.GAS_PRIORITY) {
            return encode((TransactionWithGasPriority) tx, includeSignature);
        }
        if (tx.getType() == TransactionType.ACCESS_LIST) {
            return encode((TransactionWithAccess) tx, includeSignature);
        }
        if (tx.getType() == TransactionType.STANDARD) {
            if (includeSignature) {
                return encode(tx, true, null);
            } else {
                Signature signature = tx.getSignature();
                if (signature.getType() == SignatureType.EIP155) {
                    int chainId = ((SignatureEIP155)signature).getChainId();
                    return encode(tx, false, chainId);
                } else {
                    throw new IllegalStateException("Neither signature nor chainId specified");
                }
            }
        }
        throw new IllegalStateException("Unsupported transaction type: " + tx.getType());
    }

    public byte[] encode(Transaction tx, boolean includeSignature, Integer chainId) {
        RlpWriter wrt = new RlpWriter();
        wrt.startList()
            .write(tx.getNonce())
            .write(tx.getGasPrice().getAmount())
            .write(tx.getGas());
        if (tx.getTo() != null) {
            wrt.write(tx.getTo().getBytes());
        } else {
            wrt.write(new byte[0]);
        }
        if (tx.getValue() != null) {
            wrt.write(tx.getValue().getAmount());
        } else {
            wrt.write(new byte[0]);
        }

        HexData data = tx.getData();
        if (data != null) {
            wrt.write(data.getBytes());
        } else {
            wrt.write(new byte[0]);
        }

        if (includeSignature) {
            Signature signature = tx.getSignature();
            if (signature == null) {
                throw new NullPointerException("Signature is not set for transaction");
            }
            wrt.write(signature.getV())
                .write(signature.getR())
                .write(signature.getS());
        } else if (chainId != null) {
            // if EIP-155 include chain id and empty r,s
            wrt.write(chainId.byteValue())
                .write(0)
                .write(0);
        }
        wrt.closeList();
        return wrt.toByteArray();
    }

    protected void writeBody(RlpWriter wrt, TransactionWithAccess tx, boolean includeSignature) {
        if (tx.getTo() != null) {
            wrt.write(tx.getTo().getBytes());
        } else {
            wrt.write(new byte[0]);
        }
        if (tx.getValue() != null) {
            wrt.write(tx.getValue().getAmount());
        } else {
            wrt.write(new byte[0]);
        }

        HexData data = tx.getData();
        if (data != null) {
            wrt.write(data.getBytes());
        } else {
            wrt.write(new byte[0]);
        }

        wrt.startList();
        for (TransactionWithAccess.Access access: tx.getAccessList()) {
            wrt.startList();
            wrt.write(access.getAddress().getBytes());
            wrt.startList();
            for (Hex32 storageKey: access.getStorageKeys()) {
                wrt.write(storageKey.getBytes());
            }
            wrt.closeList();
            wrt.closeList();
        }
        wrt.closeList();

        if (includeSignature) {
            Signature signature = tx.getSignature();
            if (signature == null) {
                // just empty signature
                wrt.write(0)
                    .write(0)
                    .write(0);
            } else {
                if (signature.getType() == SignatureType.EIP2930) {
                    int yParity = ((SignatureEIP2930)signature).getYParity();
                    if (yParity == 0) {
                        wrt.write(0);
                    } else {
                        wrt.write(Integer.valueOf(yParity).byteValue());
                    }
                    wrt.write(signature.getR());
                    wrt.write(signature.getS());
                } else {
                    throw new ClassCastException("Required signature " + SignatureEIP2930.class.getName() + " but have " + signature.getClass().getName());
                }
            }
        }
    }

    public byte[] encode(TransactionWithAccess tx, boolean includeSignature) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(TransactionType.ACCESS_LIST.getFlag());
        RlpWriter wrt = new RlpWriter(buffer);
        wrt.startList()
            .write(Integer.valueOf(tx.getChainId()).byteValue())
            .write(tx.getNonce())
            .write(tx.getGasPrice().getAmount())
            .write(tx.getGas());
        writeBody(wrt, tx, includeSignature);
        wrt.closeList();
        return buffer.toByteArray();
    }

    public byte[] encode(TransactionWithGasPriority tx, boolean includeSignature) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(TransactionType.GAS_PRIORITY.getFlag());
        RlpWriter wrt = new RlpWriter(buffer);
        wrt.startList()
            .write(Integer.valueOf(tx.getChainId()).byteValue())
            .write(tx.getNonce())
            .write(tx.getPriorityGasPrice().getAmount())
            .write(tx.getMaxGasPrice().getAmount())
            .write(tx.getGas());
        writeBody(wrt, tx, includeSignature);
        wrt.closeList();
        return buffer.toByteArray();
    }
}

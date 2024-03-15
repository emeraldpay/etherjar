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

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rlp.RlpReader;
import io.emeraldpay.etherjar.rlp.RlpType;

import java.util.ArrayList;
import java.util.List;

public class TransactionDecoder {

    public static final TransactionDecoder DEFAULT = new TransactionDecoder();

    public Transaction decode(HexData raw) {
        return decode(raw.getBytes());
    }

    public Transaction decode(byte[] raw) {
        if (raw.length <= 1) {
            throw new IllegalArgumentException("Raw TX is too short: " + raw.length);
        }
        TransactionType type = TransactionType.fromPrefix(raw[0]);
        if (type == TransactionType.GAS_PRIORITY) {
            return decodeGasPriority(raw);
        }
        if (type == TransactionType.BLOB) {
            return decodeBlob(raw);
        }
        if (type == TransactionType.STANDARD) {
            return decodeStandard(raw);
        }
        if (type == TransactionType.ACCESS_LIST) {
            return decodeAccessList(raw);
        }
        throw new IllegalArgumentException("Unsupported transaction type: " + type);
    }

    /**
     * Decode transaction from an RLP encoded data
     *
     * @param raw RLP encoded transaction
     * @return transaction
     * @throws IllegalArgumentException if RLP is invalid or corrupted
     */
    public Transaction decodeStandard(byte[] raw) {
        RlpReader rdr = startReader(raw, 0);
        Transaction tx = new Transaction();
        readDefinitionsPart(rdr, tx);
        readBodyPart(rdr, tx);
        tryReadBaseSignature(rdr, tx);
        ensureFullyRead(rdr);
        return tx;
    }

    private void tryReadBaseSignature(RlpReader rdr, Transaction tx) {
        if (rdr.hasNext()) {
            Signature signature;
            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                int v = rdr.nextInt();
                if (v == 27 || v == 28) {
                    signature = new Signature();
                } else {
                    int chainId = Eip155.toChainId(v);
                    signature = new SignatureEIP155(chainId);
                }
                signature.setV(v);
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: V");
            }

            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                signature.setR(rdr.nextBigInt());
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: R");
            }

            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                signature.setS(rdr.nextBigInt());
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: S");
            }

            tx.setSignature(signature);
        }
    }

    public TransactionWithAccess decodeAccessList(byte[] raw) {
        // rlp([chainId, nonce, gasPrice, gasLimit, to, value, data, access_list, yParity, senderR, senderS])
        RlpReader rdr = startReader(raw, 1);
        TransactionWithAccess tx = new TransactionWithAccess();
        readChainId(rdr, tx);
        readDefinitionsPart(rdr, tx);
        readBodyPart(rdr, tx);
        readAccessList(rdr, tx);
        tryReadSignature(rdr, tx);
        ensureFullyRead(rdr);
        return tx;
    }

    public TransactionWithGasPriority decodeGasPriority(byte[] raw) {
        // rlp([chain_id, nonce, priorityGasPrice, maxGasPrice, gasLimit, to, value, data, access_list, yParity, senderR, senderS])
        RlpReader rdr = startReader(raw, 1);
        TransactionWithGasPriority tx = new TransactionWithGasPriority();

        readChainId(rdr, tx);
        readNonce(rdr, tx);
        readPriorityGasPrice(rdr, tx);
        readMaxGasPrice(rdr, tx);
        readGasLimit(rdr, tx);

        readBodyPart(rdr, tx);
        readAccessList(rdr, tx);
        tryReadSignature(rdr, tx);
        ensureFullyRead(rdr);
        return tx;
    }

    public TransactionWithBlob decodeBlob(byte[] raw) {
        // rlp([chain_id, nonce, max_priority_fee_per_gas, max_fee_per_gas, gas_limit, to, value, data, access_list, max_fee_per_blob_gas, blob_versioned_hashes, y_parity, r, s])
        RlpReader rdr = startReader(raw, 1);
        TransactionWithBlob tx = new TransactionWithBlob();
        readChainId(rdr, tx);
        readNonce(rdr, tx);
        readPriorityGasPrice(rdr, tx);
        readMaxGasPrice(rdr, tx);
        readGasLimit(rdr, tx);
        readBodyPart(rdr, tx);

        readAccessList(rdr, tx);
        readBlob(rdr, tx);
        tryReadSignature(rdr, tx);
        ensureFullyRead(rdr);
        return tx;
    }

    private RlpReader startReader(byte[] raw, int position) {
        RlpReader toprdr = new RlpReader(raw, position, raw.length - position);
        if (toprdr.getType() != RlpType.LIST) {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Not a list");
        }

        if (!toprdr.isConsumed()) {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Has additional data after tx definition");
        }
        return toprdr.nextList();
    }

    private void ensureFullyRead(RlpReader rdr) {
        if (!rdr.isConsumed()) {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Has more data than expected");
        }
    }

    private void readChainId(RlpReader rdr, TransactionWithAccess tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setChainId(rdr.nextInt());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: ChainID");
        }
    }

    private void tryReadSignature(RlpReader rdr, TransactionWithAccess tx) {
        if (rdr.hasNext()) {
            SignatureEIP2930 signature = new SignatureEIP2930();
            signature.setChainId(tx.getChainId());
            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                int yParity = rdr.nextInt();
                signature.setYParity(yParity);
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: yParity");
            }

            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                signature.setR(rdr.nextBigInt());
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: R");
            }

            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                signature.setS(rdr.nextBigInt());
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: S");
            }

            tx.setSignature(signature);
        }
    }

    private void readAccessList(RlpReader rdr, TransactionWithAccess tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.LIST) {
            RlpReader accessListRdr = rdr.nextList();
            List<TransactionWithAccess.Access> accessList = new ArrayList<>();

            while (accessListRdr.hasNext()) {
                RlpReader accessItemRdr = accessListRdr.nextList();
                Address address = Address.from(accessItemRdr.next());
                RlpReader storageListRdr = accessItemRdr.nextList();
                List<Hex32> storageList = new ArrayList<>();
                while (storageListRdr.hasNext()) {
                    storageList.add(Hex32.from(storageListRdr.next()));
                }
                accessList.add(new TransactionWithAccess.Access(address, storageList));
                if (!accessItemRdr.isConsumed()) {
                    throw new IllegalArgumentException("Transaction has invalid RLP encoding. Invalid value: Access List Item");
                }
            }
            if (!accessListRdr.isConsumed()) {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Invalid value: Access List");
            }
            tx.setAccessList(accessList);
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Not a list: Access List");
        }
    }

    protected void readBlob(RlpReader rdr, TransactionWithBlob tx) {
        // The field max_fee_per_blob_gas is a uint256
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setMaxFeePerBlobGas(new Wei(rdr.nextBigInt()));
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Max Fee Per Blob Gas");
        }
        // the field blob_versioned_hashes represents a list of hash outputs from kzg_to_versioned_hash.
        if (rdr.hasNext() && rdr.getType() == RlpType.LIST) {
            RlpReader blobVersionedHashesRdr = rdr.nextList();
            List<Hex32> blobVersionedHashes = new ArrayList<>();
            while (blobVersionedHashesRdr.hasNext()) {
                blobVersionedHashes.add(Hex32.from(blobVersionedHashesRdr.next()));
            }
            tx.setBlobVersionedHashes(blobVersionedHashes);
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Not a list: Blob Versioned Hashes");
        }
    }

    protected void readDefinitionsPart(RlpReader rdr, Transaction tx) {
        readNonce(rdr, tx);
        readGasPrice(rdr, tx);
        readGasLimit(rdr, tx);
    }

    protected void readBodyPart(RlpReader rdr, Transaction tx) {
        readTo(rdr, tx);
        readValue(rdr, tx);
        readData(rdr, tx);
    }

    private void readData(RlpReader rdr, Transaction tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setData(new HexData(rdr.next()));
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Data");
        }
    }

    private void readValue(RlpReader rdr, Transaction tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setValue(new Wei(rdr.nextBigInt()));
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Value");
        }
    }

    private void readTo(RlpReader rdr, Transaction tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            byte[] address = rdr.next();
            if (address != null && address.length > 0) {
                tx.setTo(Address.from(address));
            }
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: To");
        }
    }

    private void readGasLimit(RlpReader rdr, Transaction tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setGas(rdr.nextLong());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Gas");
        }
    }

    private void readGasPrice(RlpReader rdr, Transaction tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setGasPrice(rdr.nextBigInt());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Gas Price");
        }
    }

    private void readMaxGasPrice(RlpReader rdr, TransactionWithGasPriority tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setMaxGasPrice(rdr.nextBigInt());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Gas Price");
        }
    }

    private void readPriorityGasPrice(RlpReader rdr, TransactionWithGasPriority tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setPriorityGasPrice(rdr.nextBigInt());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Gas Price");
        }
    }

    private void readNonce(RlpReader rdr, Transaction tx) {
        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setNonce(rdr.nextLong());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Nonce");
        }
    }
}

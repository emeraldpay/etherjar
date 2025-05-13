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
import io.emeraldpay.etherjar.hex.Hex32;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.util.*;

/**
 * Transaction encoded with Address Access List (EIP-2930)
 *
 * @see <a href="https://eips.ethereum.org/EIPS/eip-2930">EIP-2930</a>
 */
public class TransactionWithAccess extends Transaction {

    private static final TransactionEncoder ENCODER = new TransactionEncoder();

    private int chainId;
    private List<Access> accessList = Collections.emptyList();

    public TransactionWithAccess() {
    }

    public TransactionWithAccess(TransactionWithAccess other) {
        super(other);
        this.chainId = other.chainId;
        if (other.accessList != null) {
            this.accessList = new ArrayList<>(other.accessList.size());
            for (Access access : other.accessList) {
                this.accessList.add(new Access(access));
            }
        }
    }

    public List<Access> getAccessList() {
        return accessList;
    }

    public void setAccessList(List<Access> accessList) {
        this.transactionId = null;
        this.accessList = accessList;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.transactionId = null;
        this.chainId = chainId;
    }

    @Override
    public byte[] hash() {
        byte[] rlp = ENCODER.encode(this, false);

        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        return keccak.digest();
    }

    @Override
    public byte[] hash(Integer chainId) {
        return hash();
    }

    @Override
    public TransactionType getType() {
        return TransactionType.ACCESS_LIST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionWithAccess that = (TransactionWithAccess) o;
        if (!super.canEqual(that)) return false;
        return  chainId == that.chainId && Objects.equals(accessList, that.accessList);
    }

    public static class Access {
        private Address address;
        private List<Hex32> storageKeys;

        public Access() {
            this(Address.empty(), Collections.emptyList());
        }

        public Access(Address address, List<Hex32> storageKeys) {
            if (address == null) {
                throw new NullPointerException("Address cannot be null");
            }
            this.address = address;
            if (storageKeys == null) {
                this.storageKeys = Collections.emptyList();
            } else {
                this.storageKeys = storageKeys;
            }
        }

        public Access(Address address, Hex32... storageKeys) {
            this(address, Arrays.asList(storageKeys));
        }

        public Access(Access other) {
            this.address = other.address;
            this.storageKeys = new ArrayList<>();
            this.storageKeys.addAll(other.storageKeys);
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public List<Hex32> getStorageKeys() {
            return storageKeys;
        }

        public void setStorageKeys(List<Hex32> storageKeys) {
            this.storageKeys = storageKeys;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Access)) return false;
            Access access = (Access) o;
            return address.equals(access.address) && storageKeys.equals(access.storageKeys);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, storageKeys);
        }
    }
}

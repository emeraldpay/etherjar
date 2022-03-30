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
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.util.HashMap;
import java.util.Map;

@JsonDeserialize(using = StateDiffJsonDeserializer.class)
public class StateDiffJson {

    private final Map<Address, AddressDiff> changes = new HashMap<>();

    public Map<Address, AddressDiff> getChanges() {
        return changes;
    }

    public AddressDiff getDiff(Address address) {
        return changes.get(address);
    }

    public void put(Address address, AddressDiff diff) {
        this.changes.put(address, diff);
    }

    static class AddressDiff {
        private Change<Wei> balance;
        private Change<HexData> code;
        private Change<Long> nonce;
        private final Map<Hex32, Change<Hex32>> storage = new HashMap<>();

        public Change<Wei> getBalance() {
            return balance;
        }

        public void setBalance(Change<Wei> balance) {
            this.balance = balance;
        }

        public Change<HexData> getCode() {
            return code;
        }

        public void setCode(Change<HexData> code) {
            this.code = code;
        }

        public Change<Long> getNonce() {
            return nonce;
        }

        public void setNonce(Change<Long> nonce) {
            this.nonce = nonce;
        }

        public Map<Hex32, Change<Hex32>> getStorage() {
            return storage;
        }

        public void setStorage(Map<Hex32, Change<Hex32>> storage) {
            this.storage.clear();
            this.storage.putAll(storage);
        }

        public void changeStorage(Hex32 ref, Change<Hex32> change) {
            this.storage.put(ref, change);
        }
    }

    static interface Change<T> {
        T getBefore();
        T getAfter();
        ChangeType getType();
        boolean existsBefore();
        boolean existsAfter();
        boolean hasChanged();
    }

    static enum ChangeType {
        NOTHING,
        REPLACE,
        CREATE,
        REMOVE
    }

    static class FullChange<T> implements Change<T> {
        private final T from;
        private final T to;

        public FullChange(T from, T to) {
            this.from = from;
            this.to = to;
        }

        public T getBefore() {
            return from;
        }

        public T getAfter() {
            return to;
        }

        public T getFrom() {
            return from;
        }

        public T getTo() {
            return to;
        }

        @Override
        public ChangeType getType() {
            return ChangeType.REPLACE;
        }

        @Override
        public boolean existsBefore() {
            return true;
        }

        @Override
        public boolean existsAfter() {
            return true;
        }

        @Override
        public boolean hasChanged() {
            return true;
        }
    }

    static class NoChange<T> implements Change<T> {

        public NoChange() {
        }

        @Override
        public T getBefore() {
            throw new IllegalStateException("BEFORE value is not available");
        }

        @Override
        public T getAfter() {
            throw new IllegalStateException("AFTER value is not available");
        }

        @Override
        public ChangeType getType() {
            return ChangeType.NOTHING;
        }

        @Override
        public boolean existsBefore() {
            return true;
        }

        @Override
        public boolean existsAfter() {
            return true;
        }

        @Override
        public boolean hasChanged() {
            return false;
        }
    }

    static class CreateChange<T> implements Change<T> {
        private final T value;

        CreateChange(T value) {
            this.value = value;
        }

        @Override
        public T getBefore() {
            return null;
        }

        @Override
        public T getAfter() {
            return value;
        }

        @Override
        public ChangeType getType() {
            return ChangeType.CREATE;
        }

        @Override
        public boolean existsBefore() {
            return false;
        }

        @Override
        public boolean existsAfter() {
            return true;
        }

        @Override
        public boolean hasChanged() {
            return true;
        }
    }

    static class RemoveChange<T> implements Change<T> {
        private final T value;

        RemoveChange(T value) {
            this.value = value;
        }

        @Override
        public T getBefore() {
            return value;
        }

        @Override
        public T getAfter() {
            return null;
        }

        @Override
        public ChangeType getType() {
            return ChangeType.REMOVE;
        }

        @Override
        public boolean existsBefore() {
            return true;
        }

        @Override
        public boolean existsAfter() {
            return false;
        }

        @Override
        public boolean hasChanged() {
            return true;
        }
    }
}

/*
 * Copyright (c) 2025 EmeraldPay Ltd, All Rights Reserved.
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
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EIP-712 typed data signer implementation for Ethereum structured data.
 *
 * <p>This class provides functionality to sign and verify messages according to the EIP-712 specification
 * for Ethereum typed structured data. The standard defines a method for hashing and signing
 * structured data using a domain separator to prevent replay attacks.</p>
 *
 * @see <a href="https://eips.ethereum.org/EIPS/eip-712">EIP-712: Typed structured data hashing and signing</a>
 */
public class EIP712MessageSigner {

    private final Signer signer;

    /**
     * Creates a new EIP-712 message signer.
     *
     * @param signer the underlying signer instance
     */
    public EIP712MessageSigner(Signer signer) {
        this.signer = signer;
    }

    /**
     * Represents the domain separator information for EIP-712 signing.
     */
    public static class EIP712Domain {
        private final String name;
        private final String version;
        private final Integer chainId;
        private final Address verifyingContract;
        private final byte[] salt;

        public EIP712Domain(String name, String version, Integer chainId, Address verifyingContract, byte[] salt) {
            this.name = name;
            this.version = version;
            this.chainId = chainId;
            this.verifyingContract = verifyingContract;
            this.salt = salt;
        }

        public String getName() { return name; }
        public String getVersion() { return version; }
        public Integer getChainId() { return chainId; }
        public Address getVerifyingContract() { return verifyingContract; }
        public byte[] getSalt() { return salt; }
    }

    /**
     * Represents a typed data structure for EIP-712 signing.
     */
    public static class TypedData {
        private final Map<String, List<TypedDataField>> types;
        private final String primaryType;
        private final EIP712Domain domain;
        private final Map<String, Object> message;

        public TypedData(Map<String, List<TypedDataField>> types, String primaryType, EIP712Domain domain, Map<String, Object> message) {
            this.types = types;
            this.primaryType = primaryType;
            this.domain = domain;
            this.message = message;
        }

        public Map<String, List<TypedDataField>> getTypes() { return types; }
        public String getPrimaryType() { return primaryType; }
        public EIP712Domain getDomain() { return domain; }
        public Map<String, Object> getMessage() { return message; }
    }

    /**
     * Represents a field in a typed data structure.
     */
    public static class TypedDataField {
        private final String name;
        private final String type;

        public TypedDataField(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() { return name; }
        public String getType() { return type; }
    }

    public static class TypedDataHashes {
        private final Hex32 domainSeparator;
        private final Hex32 messageHash;

        public TypedDataHashes(Hex32 domainSeparator, Hex32 messageHash) {
            this.domainSeparator = domainSeparator;
            this.messageHash = messageHash;
        }

        public TypedDataHashes(byte[] domainSeparator, byte[] messageHash) {
            this(Hex32.from(domainSeparator), Hex32.from(messageHash));
        }

        public Hex32 getDomainSeparator() {
            return domainSeparator;
        }

        public Hex32 getMessageHash() {
            return messageHash;
        }

        /**
         * Calculate the EIP-712 hash of typed data.
         *
         * <p>The hash is calculated as keccak256("\x19\x01" + domainSeparator + hashStruct(message))</p>

         * @return the Keccak-256 hash of the EIP-712 formatted typed data
         */
        protected Hex32 getTypedDataHash() {
            Keccak.Digest256 digest = new Keccak.Digest256();
            digest.update((byte)0x19);
            digest.update((byte)0x01);
            digest.update(domainSeparator.getBytes());
            digest.update(messageHash.getBytes());
            return Hex32.from(digest.digest());
        }
    }

    /**
     * Sign typed data with Private Key as by EIP-712.
     *
     * @param typedData the typed data to sign
     * @param pk signer private key
     * @return signature
     */
    public Signature signTypedData(TypedData typedData, PrivateKey pk) {
        return signTypedData(hashTypedData(typedData), pk);
    }

    /**
     * Sign typed data with Private Key as by EIP-712.
     *
     * @param typedDataHashes the typed data hashes to sign
     * @param pk signer private key
     * @return signature
     */
    public Signature signTypedData(TypedDataHashes typedDataHashes, PrivateKey pk) {
        Hex32 hash = typedDataHashes.getTypedDataHash();
        return signer.create(hash.getBytes(), pk, SignatureType.LEGACY);
    }

    /**
     * Sign and encode typed data with Private Key as by EIP-712.
     *
     * @param typedData the typed data to sign
     * @param pk signer private key
     * @return signature
     */
    public HexData signTypedDataEncoded(TypedData typedData, PrivateKey pk) {
        Signature signature = signTypedData(typedData, pk);
        return encodeSignature(signature);
    }

    public static HexData encodeSignature(Signature signature) {
        if (signature.getType() != SignatureType.LEGACY) {
            throw new IllegalArgumentException("Signature type must be LEGACY for EIP-712");
        }
        return Hex32.extendFrom(signature.getR())
            .concat(Hex32.extendFrom(signature.getS()))
            .concat(HexQuantity.from((long)signature.getV()).asData());
    }

    /**
     * Verify typed data signed as EIP-712.
     *
     * @param typedData original typed data
     * @param encodedSignature signature
     * @param signer address of the signer
     * @return true if signature is valid
     */
    public boolean verifyTypedDataSignature(TypedData typedData, HexData encodedSignature, Address signer) {
        return verifyTypedDataSignature(hashTypedData(typedData), encodedSignature, signer);
    }

    /**
     * Verify typed data signed as EIP-712.
     *
     * @param typedDataHashes hashes of original typed data
     * @param encodedSignature signature
     * @param signer address of the signer
     * @return true if signature is valid
     */
    public boolean verifyTypedDataSignature(TypedDataHashes typedDataHashes, HexData encodedSignature, Address signer) {
        if (encodedSignature.getSize() < Hex32.SIZE_BYTES + Hex32.SIZE_BYTES + 1) {
            throw new IllegalArgumentException("Signature is too short");
        }
        BigInteger r = new BigInteger(1, Hex32.from(encodedSignature.extract(Hex32.SIZE_BYTES)).getBytes());
        BigInteger s = new BigInteger(1, Hex32.from(encodedSignature.extract(Hex32.SIZE_BYTES, Hex32.SIZE_BYTES)).getBytes());
        BigInteger v = encodedSignature.extract(encodedSignature.getSize() - (Hex32.SIZE_BYTES + Hex32.SIZE_BYTES), Hex32.SIZE_BYTES + Hex32.SIZE_BYTES).asQuantity().getValue();
        if (v.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IllegalStateException("V is too large for int value: " + v);
        }
        Hex32 hash = typedDataHashes.getTypedDataHash();
        Signature signatureDetails = new Signature(hash.getBytes(), v.intValue(), r, s);
        return signatureDetails.recoverAddress().equals(signer);
    }

    /**
     * Prepares typed data for signing, by preparing hashes of its parts
     *
     * @param typedData the typed data to hash
     * @return Typed Data hashes
     */
    public TypedDataHashes hashTypedData(TypedData typedData) {
        byte[] domainSeparator = hashStruct("EIP712Domain", getDomainStructData(typedData.getDomain()), typedData.getTypes());
        byte[] messageHash = hashStruct(typedData.getPrimaryType(), typedData.getMessage(), typedData.getTypes());

        return new TypedDataHashes(domainSeparator, messageHash);
    }

    /**
     * Hash a struct according to EIP-712 specification.
     *
     * @param primaryType the primary type of the struct
     * @param data the struct data
     * @param types the type definitions
     * @return the hash of the struct
     */
    public byte[] hashStruct(String primaryType, Map<String, Object> data, Map<String, List<TypedDataField>> types) {
        byte[] typeHash = hashType(primaryType, types);
        byte[] encodedData = encodeData(primaryType, data, types);

        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(typeHash);
        digest.update(encodedData);
        return digest.digest();
    }

    /**
     * Hash a type according to EIP-712 specification.
     *
     * @param primaryType the primary type
     * @param types the type definitions
     * @return the hash of the type
     */
    public byte[] hashType(String primaryType, Map<String, List<TypedDataField>> types) {
        String typeString = encodeType(primaryType, types);
        return keccak256(typeString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encode a type according to EIP-712 specification.
     *
     * @param primaryType the primary type
     * @param types the type definitions
     * @return the encoded type string
     */
    public String encodeType(String primaryType, Map<String, List<TypedDataField>> types) {
        List<String> dependencies = findTypeDependencies(primaryType, types);
        dependencies.remove(primaryType);
        dependencies.add(0, primaryType);

        StringBuilder result = new StringBuilder();
        for (String type : dependencies) {
            result.append(type).append("(");
            List<TypedDataField> fields = types.get(type);
            for (int i = 0; i < fields.size(); i++) {
                TypedDataField field = fields.get(i);
                result.append(field.getType()).append(" ").append(field.getName());
                if (i < fields.size() - 1) {
                    result.append(",");
                }
            }
            result.append(")");
        }
        return result.toString();
    }

    /**
     * Find dependencies of a type recursively.
     *
     * @param primaryType the primary type
     * @param types the type definitions
     * @return the list of dependencies
     */
    protected List<String> findTypeDependencies(String primaryType, Map<String, List<TypedDataField>> types) {
        List<String> dependencies = new ArrayList<>();
        List<String> toProcess = new ArrayList<>();
        toProcess.add(primaryType);

        while (!toProcess.isEmpty()) {
            String currentType = toProcess.remove(0);
            if (dependencies.contains(currentType)) {
                continue;
            }
            dependencies.add(currentType);

            List<TypedDataField> fields = types.get(currentType);
            if (fields != null) {
                for (TypedDataField field : fields) {
                    String fieldType = field.getType();
                    // Remove array brackets if present
                    if (fieldType.endsWith("[]")) {
                        fieldType = fieldType.substring(0, fieldType.length() - 2);
                    }
                    if (types.containsKey(fieldType) && !dependencies.contains(fieldType)) {
                        toProcess.add(fieldType);
                    }
                }
            }
        }

        // Sort dependencies alphabetically, except for the primary type
        String primary = dependencies.remove(0);
        Collections.sort(dependencies);
        dependencies.add(0, primary);

        return dependencies;
    }

    /**
     * Encode data according to EIP-712 specification.
     *
     * @param primaryType the primary type
     * @param data the data to encode
     * @param types the type definitions
     * @return the encoded data
     */
    public byte[] encodeData(String primaryType, Map<String, Object> data, Map<String, List<TypedDataField>> types) {
        List<TypedDataField> fields = types.get(primaryType);
        byte[] result = new byte[fields.size() * 32];

        for (int i = 0; i < fields.size(); i++) {
            TypedDataField field = fields.get(i);
            Object value = data.get(field.getName());
            byte[] encodedValue = encodeValue(field.getType(), value, types);
            System.arraycopy(encodedValue, 0, result, i * 32, 32);
        }

        return result;
    }

    /**
     * Encode a single value according to EIP-712 specification.
     *
     * @param type the type of the value
     * @param value the value to encode
     * @param types the type definitions
     * @return the encoded value (32 bytes)
     */
    public byte[] encodeValue(String type, Object value, Map<String, List<TypedDataField>> types) {
        if (value == null) {
            return new byte[32];
        }

        if (type.equals("string") || type.equals("bytes")) {
            byte[] data = value instanceof String ? ((String) value).getBytes(StandardCharsets.UTF_8) : (byte[]) value;
            return keccak256(data);
        }

        if (value instanceof Hex32) {
            return ((Hex32) value).getBytes();
        }
        if (value instanceof HexData && ((HexData) value).getSize() <= Hex32.SIZE_BYTES) {
            return Hex32.extendFrom((HexData) value).getBytes();
        }

        if (type.startsWith("bytes") && type.length() > 5) {
            // Fixed-size bytes
            byte[] data = (byte[]) value;
            byte[] result = new byte[32];
            System.arraycopy(data, 0, result, 0, Math.min(data.length, 32));
            return result;
        } else if (type.equals("address")) {
            Address addr = (Address) value;
            byte[] result = new byte[32];
            System.arraycopy(addr.getBytes(), 0, result, 12, 20);
            return result;
        } else if (type.equals("bool")) {
            byte[] result = new byte[32];
            result[31] = (Boolean) value ? (byte) 1 : (byte) 0;
            return result;
        } else if (type.startsWith("uint") || type.startsWith("int")) {
            if (value instanceof HexQuantity) {
                return Hex32.extendFrom((HexQuantity) value).getBytes();
            } else if (value instanceof BigInteger) {
                return Hex32.extendFrom((BigInteger) value).getBytes();
            } else if (value instanceof Long) {
                return Hex32.extendFrom((Long) value).getBytes();
            } else if (value instanceof Number) {
                return Hex32.extendFrom(BigInteger.valueOf(((Number) value).longValue())).getBytes();
            } else {
                throw new IllegalArgumentException("Unsupported numeric type: " + value.getClass().getName());
            }
        } else if (type.endsWith("[]")) {
            // Array type
            Object[] array = (Object[]) value;
            byte[] result = new byte[32];
            byte[] arrayData = new byte[array.length * 32];

            String elementType = type.substring(0, type.length() - 2);
            for (int i = 0; i < array.length; i++) {
                byte[] elementEncoded = encodeValue(elementType, array[i], types);
                System.arraycopy(elementEncoded, 0, arrayData, i * 32, 32);
            }

            byte[] hash = keccak256(arrayData);
            System.arraycopy(hash, 0, result, 0, 32);
            return result;
        } else if (types.containsKey(type)) {
            // Struct type
            @SuppressWarnings("unchecked")
            Map<String, Object> structData = (Map<String, Object>) value;
            return hashStruct(type, structData, types);
        }

        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    /**
     * Get domain struct data from EIP712Domain.
     *
     * @param domain the domain
     * @return the domain struct data
     */
    protected Map<String, Object> getDomainStructData(EIP712Domain domain) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (domain.getName() != null) {
            result.put("name", domain.getName());
        }
        if (domain.getVersion() != null) {
            result.put("version", domain.getVersion());
        }
        if (domain.getChainId() != null) {
            result.put("chainId", domain.getChainId());
        }
        if (domain.getVerifyingContract() != null) {
            result.put("verifyingContract", domain.getVerifyingContract());
        }
        if (domain.getSalt() != null) {
            result.put("salt", domain.getSalt());
        }
        return result;
    }

    /**
     * Calculate Keccak-256 hash.
     *
     * @param data the data to hash
     * @return the hash
     */
    protected byte[] keccak256(byte[] data) {
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(data);
        return digest.digest();
    }

}

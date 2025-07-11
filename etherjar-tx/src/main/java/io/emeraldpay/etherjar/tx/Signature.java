/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
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
package io.emeraldpay.etherjar.tx;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Signature of a message (i.e. of a transaction)
 */
public class Signature {

    private byte[] message;

    private int v;
    private BigInteger r;
    private BigInteger s;

    private transient Address address;

    public Signature() {
    }

    /**
     * Creates existing signature
     *
     * @param message a signed message, usually a Keccak256 of some data
     * @param v v part of signature
     * @param r R part of signature
     * @param s S part of signature
     */
    public Signature(byte[] message, int v, BigInteger r, BigInteger s) {
        this.message = message;
        this.v = v;
        this.r = r;
        this.s = s;
    }

    public Signature(Signature other) {
        if (other.message != null) {
            this.message = Arrays.copyOf(other.message, other.message.length);
        }
        this.v = other.v;
        this.r = other.r;
        this.s = other.s;
    }

    /**
     * Extract signature from R, S, V encoded as 65 bytes.
     *
     * @param encoded R, S, V encoded as 65 bytes
     * @return signature without the message
     */
    public static Signature fromEncoded(byte[] encoded) {
        if (encoded == null || encoded.length != 65) {
            throw new IllegalArgumentException("Invalid signature length: " + (encoded == null ? 0 : encoded.length));
        }
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(encoded, 0, 32));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(encoded, 32, 64));
        int v = encoded[64] & 0xFF;
        return new Signature(null, v, r, s);
    }

    /**
     * Extract signature from R, S, V encoded as 65 bytes.
     *
     * @param encoded R, S, V encoded as 65 bytes
     * @return signature without the message
     */
    public static Signature fromEncoded(HexData encoded) {
        return Signature.fromEncoded(encoded.getBytes());
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.address = null;
        this.message = message;
    }

    /**
     * Creates a copy of this signature with a new message.
     *
     * @param message the message that was signed
     * @return a copy of this signature with the new message set
     */
    public Signature withMessage(byte[] message) {
        Signature copy = new Signature(this);
        copy.setMessage(message);
        return copy;
    }

    /**
     * Creates a copy of this signature with a new message.
     *
     * @param message the message that was signed
     * @return a copy of this signature with the new message set
     */
    public Signature withMessage(Hex32 message) {
        return this.withMessage(message.getBytes());
    }

    public SignatureType getType() {
        return SignatureType.LEGACY;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.address = null;
        this.v = v;
    }

    public BigInteger getR() {
        return r;
    }

    public void setR(BigInteger r) {
        this.address = null;
        this.r = r;
    }

    public BigInteger getS() {
        return s;
    }

    public void setS(BigInteger s) {
        this.address = null;
        this.s = s;
    }

    /**
     * Encodes signature as R, S, V in 65 bytes.
     *
     * @return encoded signature
     */
    public HexData encode() {
        if (r == null || s == null) {
            throw new IllegalStateException("Signature R/S are not set");
        }
        if (v < 0 || v > 255) {
            throw new IllegalStateException("Signature V is not set or invalid: " + v);
        }
        return HexData.combine(
            Hex32.extendFrom(r),
            Hex32.extendFrom(s),
            HexData.from((byte)v)
        );
    }

    /**
     * Recovers address that signed the message. Requires signature (v,R,S) and message to be set
     *
     * @return Address which signed the message, or null if address cannot be extracted
     */
    public Address recoverAddress() {
        if (address != null) {
            return address;
        }
        try {
            if (message == null || message.length == 0) {
                throw new IllegalStateException("Transaction/Message hash are not set");
            }
            byte[] pubkey = Signer.ecrecover(this);
            if (pubkey == null) {
                return null;
            }
            Keccak.Digest256 digest = new Keccak.Digest256();
            digest.update(pubkey);
            byte[] hash = digest.digest();

            byte[] buf = new byte[20];
            System.arraycopy(hash, 12, buf, 0, 20);
            Address address = Address.from(buf);
            this.address = address;
            return address;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getRecId() {
        return v - 27;
    }

    public boolean canEqual(Signature signature) {
        return v == signature.v
            && Arrays.equals(message, signature.message)
            && Objects.equals(r, signature.r)
            && Objects.equals(s, signature.s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return canEqual(signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, s);
    }

    public static Signature copyOf(Signature other) {
        if (other == null) {
            return null;
        }
        if (other instanceof SignatureEIP2930) {
            return new SignatureEIP2930((SignatureEIP2930)other);
        } else if (other instanceof SignatureEIP155) {
            return new SignatureEIP155((SignatureEIP155)other);
        } else {
            return new Signature(other);
        }
    }
}

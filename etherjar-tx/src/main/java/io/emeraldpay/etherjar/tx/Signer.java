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
import io.emeraldpay.etherjar.hex.HexData;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;
import java.util.Arrays;

public class Signer {

    public static final ECDomainParameters CURVE_PARAMS;
    public static final SecP256K1Curve CURVE;
    private static final BigInteger CURVE_ORDER;

    static {
        X9ECParameters params = CustomNamedCurves.getByName("secp256k1");
        CURVE_PARAMS = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        CURVE = (SecP256K1Curve) CURVE_PARAMS.getCurve();
        CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    }

    private final Integer chainId;

    public Signer(Integer chainId) {
        this.chainId = chainId;
    }

    /**
     * Creates a signer instance for the mainnet (chainId=1).
     */
    public Signer() {
        this(1);
    }

    /**
     * Creates a signer instance for the mainnet (chainId=1).
     */
    public static Signer newMainnet() {
        return new Signer(1);
    }

    /**
     * Get an EIP-191 message signer instance.
     *
     * @return EIP-191 message signer
     */
    public EIP191MessageSigner getEIP191MessageSigner() {
        return new EIP191MessageSigner(this);
    }

    /**
     * Get an EIP-712 message signer instance.
     *
     * @return EIP-712 message signer
     */
    public EIP712MessageSigner getEIP712MessageSigner() {
        return new EIP712MessageSigner(this);
    }

    /**
     * Sign a message with Private Key as by EIP-191
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     * @deprecated Use {@link #getEIP191MessageSigner()} instead
     */
    @Deprecated
    public Signature signMessage(String msg, PrivateKey pk) {
        return getEIP191MessageSigner().signMessage(msg, pk);
    }

    /**
     * Sign a message with Private Key as by EIP-191
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     * @deprecated Use {@link #getEIP191MessageSigner()} instead
     */
    @Deprecated
    public Signature signMessage(byte[] msg, PrivateKey pk) {
        return getEIP191MessageSigner().signMessage(msg, pk);
    }

    /**
     * Sign and encode a message with Private Key as by EIP-191
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     * @deprecated Use {@link #getEIP191MessageSigner()} instead
     */
    @Deprecated
    public HexData signMessageEncoded(String msg, PrivateKey pk) {
        return getEIP191MessageSigner().signMessageEncoded(msg, pk);
    }

    /**
     * Sign and encode a message with Private Key as by EIP-191
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     * @deprecated Use {@link #getEIP191MessageSigner()} instead
     */
    @Deprecated
    public HexData signMessageEncoded(byte[] msg, PrivateKey pk) {
        return getEIP191MessageSigner().signMessageEncoded(msg, pk);
    }

    /**
     * Verify message signed as EIP-191
     *
     * @param msg original message
     * @param encodedSignature signature
     * @param signer address of the signer
     * @return true if signature is valid
     * @deprecated Use {@link #getEIP191MessageSigner()} instead
     */
    @Deprecated
    public boolean verifyMessageSignature(String msg, HexData encodedSignature, Address signer) {
        return getEIP191MessageSigner().verifyMessageSignature(msg, encodedSignature, signer);
    }

    /**
     * Verify message signed as EIP-191
     *
     * @param msg original message
     * @param encodedSignature signature
     * @param signer address of the signer
     * @return true if signature is valid
     * @deprecated Use {@link #getEIP191MessageSigner()} instead
     */
    @Deprecated
    public boolean verifyMessageSignature(byte[] msg, HexData encodedSignature, Address signer) {
        return getEIP191MessageSigner().verifyMessageSignature(msg, encodedSignature, signer);
    }

    public Signature sign(Transaction tx, PrivateKey pk) {
        byte[] hash = hash(tx);
        SignatureType type = SignatureType.LEGACY;
        if (tx.getType() != TransactionType.STANDARD) {
            type = SignatureType.EIP2930;
        } else if (chainId != null) {
            type = SignatureType.EIP155;
        }
        return create(hash, pk, type);
    }

    public byte[] hash(Transaction tx) {
        return tx.hash(chainId);
    }

    @Deprecated
    public <T extends Signature> T create(byte[] hash, PrivateKey key, Class<T> type) {
        if (SignatureEIP155.class.equals(type)) {
            return create(hash, key, SignatureType.EIP155);
        }
        if (SignatureEIP2930.class.equals(type)) {
            return create(hash, key, SignatureType.EIP2930);
        }
        return create(hash, key, SignatureType.LEGACY);
    }

    @SuppressWarnings("unchecked")
    public <T extends Signature> T create(byte[] hash, PrivateKey key, SignatureType type) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        signer.init(true, key.getECKey());
        BigInteger[] signature = signer.generateSignature(hash);
        if (signature.length != 2) {
            throw new IllegalStateException("Invalid signature components: " + signature.length);
        }
        BigInteger r = signature[0];
        BigInteger s = signature[1];
        if (s.compareTo(CURVE_ORDER) > 0) {
            s = CURVE_PARAMS.getN().subtract(s);
        }
        byte[] publicKey = key.getPublicKey();

        int y = getY(hash, r, s, publicKey);
        if (SignatureType.EIP155.equals(type)) {
            return (T) new SignatureEIP155(chainId, hash, Eip155.toV(y, chainId), r, s);
        }
        if (SignatureType.EIP2930.equals(type)) {
            return (T) new SignatureEIP2930(hash, y, chainId, r, s);
        }
        return (T) new Signature(hash, 27 + y, r, s);
    }

    public int getY(byte[] hash, BigInteger r, BigInteger s, byte[] publicKey) {
        byte[] pub0 = ecrecover(0, hash, r, s);
        if (Arrays.equals(publicKey, pub0)) {
            return 0;
        }
        byte[] pub1 = ecrecover(1, hash, r, s);
        if (Arrays.equals(publicKey, pub1)) {
            return 1;
        }
        throw new IllegalStateException("Cannot find correct y");
    }

    /**
     *
     * @return public key derived from current v,R,S and message
     */
    public static byte[] ecrecover(Signature signature) {
        return ecrecover(signature.getRecId(), signature.getMessage(), signature.getR(), signature.getS());
    }

    // implementation is based on BitcoinJ ECKey code
    // see https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/ECKey.java
    protected static byte[] ecrecover(int recId, byte[] message, BigInteger r, BigInteger s) {
        BigInteger n = CURVE_PARAMS.getN();

        // Let x = r + jn
        BigInteger i = BigInteger.valueOf((long)recId / 2);
        BigInteger x = r.add(i.multiply(n));

        if (x.compareTo(CURVE.getQ()) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }

        // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
        // So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        if (!R.multiply(n).isInfinity()) {
            // If nR != point at infinity, then recId (i.e. v) is invalid
            return null;
        }

        //
        // Compute a candidate public key as:
        // Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        // Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation, ** is point multiplication and + is point addition (the EC group operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
        //
        BigInteger e = new BigInteger(1, message);
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = r.modInverse(n);
        BigInteger srInv = rInv.multiply(s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);

        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE_PARAMS.getG(), eInvrInv, R, srInv);

        // For Ethereum we don't use first byte of the key
        byte[] full = q.getEncoded(false);
        byte[] ethereum = new byte[full.length - 1];
        System.arraycopy(full, 1, ethereum, 0, ethereum.length);
        return ethereum;
    }

    /**
     * Decompress a compressed public key (x coordinate and low-bit of y-coordinate).
     *
     * @param xBN X-coordinate
     * @param yBit Sign of Y-coordinate
     * @return Uncompressed public key
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        ECFieldElement x = CURVE.fromBigInteger(xBN);
        ECFieldElement alpha = x.multiply(x.square().add(CURVE.getA())).add(CURVE.getB());
        ECFieldElement beta = alpha.sqrt();
        if (beta == null)
            throw new IllegalArgumentException("Invalid point compression");
        ECPoint ecPoint;
        BigInteger nBeta = beta.toBigInteger();
        if (nBeta.testBit(0) == yBit) {
            ecPoint = CURVE.createPoint(x.toBigInteger(), nBeta);
        } else {
            ECFieldElement y = CURVE.fromBigInteger(CURVE.getQ().subtract(nBeta));
            ecPoint = CURVE.createPoint(x.toBigInteger(), y.toBigInteger());
        }
        return ecPoint;
    }
}

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
package io.emeraldpay.etherjar.tx;

import io.emeraldpay.etherjar.domain.Address;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Signature of a message (i.e. of a transaction)
 */
public class Signature {

    public static final ECDomainParameters CURVE_PARAMS;
    public static final SecP256K1Curve CURVE;
    private static final BigInteger CURVE_ORDER;

    static {
        X9ECParameters params = CustomNamedCurves.getByName("secp256k1");
        CURVE_PARAMS = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        CURVE = (SecP256K1Curve) CURVE_PARAMS.getCurve();
        CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    }

    private byte[] message;

    private int v;
    private BigInteger r;
    private BigInteger s;

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

    public static Signature create(byte[] hash, PrivateKey key, Integer chainId) {
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

        if (chainId != null) {
            int y = -1;
            byte[] pub0 = ecrecover(0, hash, r, s);
            if (Arrays.equals(publicKey, pub0)) {
                y = 0;
            }
            if (y == -1) {
                byte[] pub1 = ecrecover(1, hash, r, s);
                if (Arrays.equals(publicKey, pub1)) {
                    y = 1;
                }
            }
            if (y == -1) {
                throw new IllegalStateException("Cannot find correct y");
            }

            return new SignatureEip155(chainId, hash, Eip155.toV(y, chainId), r, s);
        }
        return new Signature(hash, 27, r, s);
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public BigInteger getR() {
        return r;
    }

    public void setR(BigInteger r) {
        this.r = r;
    }

    public BigInteger getS() {
        return s;
    }

    public void setS(BigInteger s) {
        this.s = s;
    }

    /**
     * Recovers address that signed the message. Requires signature (v,R,S) and message to be set
     *
     * @return Address which signed the message, or null if address cannot be extracted
     */
    public Address recoverAddress() {
        try {
            byte[] pubkey = ecrecover();
            if (pubkey == null) {
                return null;
            }
            Keccak.Digest256 digest = new Keccak.Digest256();
            digest.update(pubkey);
            byte[] hash = digest.digest();

            byte[] buf = new byte[20];
            System.arraycopy(hash, 12, buf, 0, 20);
            return Address.from(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected int getRecId() {
        return v - 27;
    }

    /**
     *
     * @return public key derived from current v,R,S and message
     */
    public byte[] ecrecover() {
        return ecrecover(getRecId(), message, r, s);
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

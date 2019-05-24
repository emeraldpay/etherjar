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
package io.infinitape.etherjar.tx;

import io.infinitape.etherjar.domain.Address;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;

/**
 * Signature of a message (i.e. of a transaction)
 */
public class Signature {

    private static final ECDomainParameters ecParams;
    static {
        X9ECParameters params = CustomNamedCurves.getByName("secp256k1");
        ecParams = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    private byte[] message;

    private int v;
    private BigInteger r;
    private BigInteger s;

    public Signature() {
    }

    /**
     *
     * @param message a signed message, usually a Keccak256 of some data
     * @param v
     * @param r
     * @param s
     */
    public Signature(byte[] message, int v, BigInteger r, BigInteger s) {
        this.message = message;
        this.v = v;
        this.r = r;
        this.s = s;
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
    // implementation is based on BitcoinJ ECKey code
    // see https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/ECKey.java
    public byte[] ecrecover() {
        int recId = getRecId();
        SecP256K1Curve curve = (SecP256K1Curve)ecParams.getCurve();
        BigInteger n = ecParams.getN();

        // Let x = r + jn
        BigInteger i = BigInteger.valueOf((long)recId / 2);
        BigInteger x = r.add(i.multiply(n));

        if (x.compareTo(curve.getQ()) >= 0) {
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

        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(ecParams.getG(), eInvrInv, R, srInv);

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
        SecP256K1Curve curve = (SecP256K1Curve)ecParams.getCurve();
        ECFieldElement x = curve.fromBigInteger(xBN);
        ECFieldElement alpha = x.multiply(x.square().add(curve.getA())).add(curve.getB());
        ECFieldElement beta = alpha.sqrt();
        if (beta == null)
            throw new IllegalArgumentException("Invalid point compression");
        ECPoint ecPoint;
        BigInteger nBeta = beta.toBigInteger();
        if (nBeta.testBit(0) == yBit) {
            ecPoint = curve.createPoint(x.toBigInteger(), nBeta);
        } else {
            ECFieldElement y = curve.fromBigInteger(curve.getQ().subtract(nBeta));
            ecPoint = curve.createPoint(x.toBigInteger(), y.toBigInteger());
        }
        return ecPoint;
    }
}

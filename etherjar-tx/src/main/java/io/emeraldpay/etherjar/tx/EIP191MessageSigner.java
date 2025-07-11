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

/**
 * EIP-191 message signer implementation for Ethereum personal messages.
 *
 * <p>This class provides functionality to sign and verify messages according to the EIP-191 specification
 * for Ethereum Signed Messages. The standard prefixes messages with "\x19Ethereum Signed Message:\n"
 * followed by the message length before hashing with Keccak-256.</p>
 *
 * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191: Signed Data Standard</a>
 */
public class EIP191MessageSigner {

    private final Signer signer;

    /**
     * Creates a new EIP-191 message signer.
     *
     * @param signer the underlying signer instance
     */
    public EIP191MessageSigner(Signer signer) {
        this.signer = signer;
    }

    /**
     * Sign a message with Private Key as by EIP-191.
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     */
    public Signature signMessage(String msg, PrivateKey pk) {
        return signMessage(msg.getBytes(), pk);
    }

    /**
     * Sign a message with Private Key as by EIP-191.
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     */
    public Signature signMessage(byte[] msg, PrivateKey pk) {
        byte[] hash = getMessageHash(msg);
        return signer.create(hash, pk, SignatureType.LEGACY);
    }

    /**
     * Sign and encode a message with Private Key as by EIP-191.
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     */
    public HexData signMessageEncoded(String msg, PrivateKey pk) {
        return signMessageEncoded(msg.getBytes(), pk);
    }

    /**
     * Sign and encode a message with Private Key as by EIP-191.
     *
     * @param msg message to sign
     * @param pk signer private key
     * @return signature
     * @see <a href="https://eips.ethereum.org/EIPS/eip-191">EIP-191</a>
     */
    public HexData signMessageEncoded(byte[] msg, PrivateKey pk) {
        Signature signature = signMessage(msg, pk);
        return signature.encode();
    }

    /**
     * Verify message signed as EIP-191.
     *
     * @param msg original message
     * @param encodedSignature signature
     * @param signer address of the signer
     * @return true if signature is valid
     */
    public boolean verifyMessageSignature(String msg, HexData encodedSignature, Address signer) {
        return verifyMessageSignature(msg.getBytes(), encodedSignature, signer);
    }

    /**
     * Verify message signed as EIP-191.
     *
     * @param msg original message
     * @param encodedSignature signature
     * @param signer address of the signer
     * @return true if signature is valid
     */
    public boolean verifyMessageSignature(byte[] msg, HexData encodedSignature, Address signer) {
        Signature signature = Signature.fromEncoded(encodedSignature)
            .withMessage(getMessageHash(msg));
        return signature.recoverAddress().equals(signer);
    }

    /**
     * Calculate the EIP-191 hash of a message.
     *
     * <p>The hash is calculated as keccak256("\x19Ethereum Signed Message:\n" + len(message) + message)</p>
     *
     * @param msg the message to hash
     * @return the Keccak-256 hash of the EIP-191 formatted message
     */
    protected byte[] getMessageHash(byte[] msg) {
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update((byte)0x19);
        digest.update("Ethereum Signed Message:\n".getBytes());
        digest.update(Integer.toString(msg.length).getBytes());
        digest.update(msg);
        return digest.digest();
    }

}

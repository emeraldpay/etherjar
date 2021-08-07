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

public enum SignatureType {

    /**
     * Legacy signature that doesn't consider chainId, i.e., is not replay-protected between different forks
     */
    LEGACY,

    /**
     * Signature with replay protections, introduced in EIP-155
     * @see <a href="https://eips.ethereum.org/EIPS/eip-155">https://eips.ethereum.org/EIPS/eip-155</a>
     */
    EIP155,

    /**
     * Signature for new typed transactions, introduced in EIP-2930
     * @see <a href="https://eips.ethereum.org/EIPS/eip-2930">https://eips.ethereum.org/EIPS/eip-2930</a>
     */
    EIP2930
}

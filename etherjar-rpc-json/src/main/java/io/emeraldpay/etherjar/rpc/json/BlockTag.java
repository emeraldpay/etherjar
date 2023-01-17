/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
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

import java.io.Serializable;

/**
 * @see <a href="https://github.com/ethereum/execution-apis/blob/main/src/schemas/block.yaml#L92">https://github.com/ethereum/execution-apis/blob/main/src/schemas/block.yaml</a>
 */
public enum BlockTag implements Serializable {

    /**
     * The most recent block in the canonical chain observed by the client, this block may be re-orged out of the canonical chain even under healthy/normal conditions
     */
    LATEST("latest"),

    /**
     *  The lowest numbered block the client has available
     */
    EARLIEST("earliest"),

    /**
     * A sample next block built by the client on top of `latest` and containing the set of transactions usually taken from local mempool
     */
    PENDING("pending"),

    /**
     * The most recent block that is safe from re-orgs under honest majority and certain synchronicity assumptions
     */
    SAFE("safe"),

    /**
     * The most recent crypto-economically secure block, cannot be re-orged outside of manual intervention driven by community coordination
     */
    FINALIZED("finalized");

    private final String code;

    BlockTag(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

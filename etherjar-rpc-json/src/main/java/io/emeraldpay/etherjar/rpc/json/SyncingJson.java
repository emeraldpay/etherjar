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
package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

// Uses a custom serialization because it's either a boolean or an object
@JsonDeserialize(using = SyncingJsonDeserializer.class)
@JsonSerialize(using = SyncingJsonSerializer.class)
public abstract class SyncingJson {

    abstract public boolean isSyncing();

    abstract public Long getStartingBlock();

    abstract public Long getCurrentBlock();

    abstract public Long getHighestBlock();

    static class Status extends SyncingJson {
        private final boolean syncing;

        public Status(boolean syncing) {
            this.syncing = syncing;
        }

        @Override
        public boolean isSyncing() {
            return syncing;
        }

        @Override
        public Long getStartingBlock() {
            return 0L;
        }
        @Override
        public Long getCurrentBlock() {
            return null;
        }
        @Override
        public Long getHighestBlock() {
            return null;
        }
    }

    static class AtBlock extends SyncingJson {
        private Long startingBlock;
        private Long currentBlock;
        private Long highestBlock;
        private List<Stage> stages;

        @Override
        public boolean isSyncing() {
            return true;
        }

        public Long getStartingBlock() {
            return startingBlock;
        }

        public void setStartingBlock(Long startingBlock) {
            this.startingBlock = startingBlock;
        }

        public Long getCurrentBlock() {
            return currentBlock;
        }

        public void setCurrentBlock(Long currentBlock) {
            this.currentBlock = currentBlock;
        }

        public Long getHighestBlock() {
            return highestBlock;
        }

        public void setHighestBlock(Long highestBlock) {
            this.highestBlock = highestBlock;
        }

        public List<Stage> getStages() {
            return stages;
        }

        public void setStages(List<Stage> stages) {
            this.stages = stages;
        }
    }

    static class Stage {
        private String stageName;
        private Long block;

        public String getStageName() {
            return stageName;
        }

        public void setStageName(String stageName) {
            this.stageName = stageName;
        }

        public Long getBlock() {
            return block;
        }

        public void setBlock(Long block) {
            this.block = block;
        }
    }

}

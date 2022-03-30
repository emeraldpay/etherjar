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

import java.util.Arrays;
import java.util.stream.Collectors;

public class DeserializationDebug {

    static Logger LOGGER = new NoLogger();
//    static Logger LOGGER = new StdLogger();

    interface Logger {
        void info(String msg, Object... args);
        void error(String msg, Object... args);
        void error(String msg, Throwable t);
    }

    private static class NoLogger implements Logger {

        @Override
        public void info(String msg, Object... args) {
        }

        @Override
        public void error(String msg, Object... args) {

        }

        @Override
        public void error(String msg, Throwable t) {
        }
    }

    private static class StdLogger implements Logger {

        private String argToString(Object... args) {
            return Arrays.stream(args).map(o -> {
                if (o == null) {
                    return "NULL";
                }
                return o.toString();
            }).collect(Collectors.joining(", "));
        }

        @Override
        public void info(String msg, Object... args) {
            System.out.println(msg + " | " + argToString(args));
        }

        @Override
        public void error(String msg, Object... args) {
            System.err.println(msg + " | " + argToString(args));
        }

        @Override
        public void error(String msg, Throwable t) {
            System.err.println(msg);
            t.printStackTrace();
        }
    }
}

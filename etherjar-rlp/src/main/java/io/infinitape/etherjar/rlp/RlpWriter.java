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
package io.infinitape.etherjar.rlp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * RLP (Recursive Length Prefix) encoding writer
 *
 * @link https://github.com/ethereum/wiki/wiki/RLP
 */
public class RlpWriter {

    private ByteArrayOutputStream buffer;
    private LinkedList<ByteArrayOutputStream> levels;

    public RlpWriter() {
        levels = new LinkedList<>();
        buffer = new ByteArrayOutputStream();
    }

    /**
     * Start a new list, must be closed after all data is written.
     *
     * @see #closeList()
     * @return writer for the list
     */
    public RlpWriter startList() {
        levels.push(buffer);
        buffer = new ByteArrayOutputStream();
        return this;
    }

    /**
     * Close opened list
     *
     * @see #startList()
     * @return writer for the list
     */
    public RlpWriter closeList() {
        if (levels.isEmpty()) {
            throw new IllegalStateException("List not started");
        }
        byte[] list = buffer.toByteArray();
        buffer = levels.pop();
        write(list, RlpType.LIST);
        return this;
    }

    /**
     *
     * @return resulting data, encoded as RLP
     */
    public byte[] toByteArray() {
        if (!levels.isEmpty()) {
            throw new IllegalStateException("List is not closed");
        }
        return buffer.toByteArray();
    }

    /**
     *
     * @param value String to encode into RLP list
     * @return writer for the list
     */
    public RlpWriter write(String value) {
        return write(value == null ? new byte[0] : value.getBytes());
    }

    /**
     *
     * @param value number to encode into RLP list
     * @return writer for the list
     */
    public RlpWriter write(int value) {
        return write(toBytes(value));
    }

    /**
     *
     * @param value number to encode into RLP list
     * @return writer for the list
     */
    public RlpWriter write(long value) {
        return write(toBytes(value));
    }

    /**
     * Writes BigInteger into RLP list.
     *
     * Please note that the method stripes leading zeroes in bytes representation for compatibility with other Ethereum implementations,
     * i.e. it may be different from standard Java representation of BigInteger, for example when it has a leading zero
     * byte to reflect that the number is positive. A BigInteger written/read from RLP is threaten as a positive number
     * by default. To write strictly Java encoded BigInteger please use write as bytes.
     *
     * @param value number to encode into RLP list
     * @return writer for the list
     */
    public RlpWriter write(BigInteger value) {
        return write(shorten(value.toByteArray()));
    }

    /**
     *
     * @param value number to encode into RLP list
     * @return writer for the list
     */
    public RlpWriter write(byte[] value) {
        return this.write(value, RlpType.BYTES);
    }

    /**
     *
     * @param value bytes to encode into RLP list
     * @return writer for the list
     */
    protected RlpWriter write(byte[] value, RlpType type) {
        if (type != RlpType.LIST && buffer.size() > 0 && levels.size() == 0) {
            throw new IllegalStateException("Cannot encode another value into same RLP output. Use LIST to write multiple values.");
        }
        try {
            if (type == RlpType.BYTES) {
                if (value.length == 0) {
                    buffer.write(0x80);
                } else if (value.length == 1) {
                    if ((value[0] & 0xff) < 0x7f) {
                        // the data is a string if the range of the first byte(i.e. prefix) is [0x00, 0x7f], and the
                        // string is the first byte itself exactly;
                        buffer.write(value[0]);
                    } else {
                        // the data is a string if the range of the first byte is [0x80, 0xb7], and the string whose
                        // length is equal to the first byte minus 0x80 follows the first byte;
                        buffer.write(0x80 + 1);
                        buffer.write(value[0]);
                    }
                } else if (value.length <= (0xb7 - 0x80)) {
                    // the data is a string if the range of the first byte is [0x80, 0xb7], and the string whose
                    // length is equal to the first byte minus 0x80 follows the first byte;
                    buffer.write(0x80 + value.length);
                    buffer.write(value);
                } else {
                    // the data is a string if the range of the first byte is [0xb8, 0xbf], and the length of the
                    // string whose length in bytes is equal to the first byte minus 0xb7 follows the first byte,
                    // and the string follows the length of the string;
                    byte[] size = toBytes(value.length);
                    buffer.write(0xb7 + size.length);
                    buffer.write(size);
                    buffer.write(value);
                }
            } else if (type == RlpType.LIST) {
                if (value.length < 0xf7 - 0xc0) {
                    // the data is a list if the range of the first byte is [0xc0, 0xf7], and the concatenation of the
                    // RLP encodings of all items of the list which the total payload is equal to the first byte minus
                    // 0xc0 follows the first byte;
                    buffer.write(0xc0 + value.length);
                    buffer.write(value);
                } else {
                    // the data is a list if the range of the first byte is [0xf8, 0xff], and the total payload of the
                    // list whose length is equal to the first byte minus 0xf7 follows the first byte, and the concatenation
                    // of the RLP encodings of all items of the list follows the total payload of the list;
                    byte[] size = toBytes(value.length);
                    buffer.write(0xf7 + size.length);
                    buffer.write(size);
                    buffer.write(value);
                }
            } else {
                throw new IllegalArgumentException("Invalid RLP type: "+ type);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write to RLP buffer", e);
        }
        return this;
    }

    /**
     * Removes leading zeroes in the provided bytes array
     *
     * @param value a byte array
     * @return same byte array without leading zeroes
     */
    protected byte[] shorten(byte[] value) {
        int pos = 0;
        while (pos < value.length && value[pos] == 0) pos++;
        if (pos == 0) {
            return value;
        }
        byte[] sub = new byte[value.length - pos];
        System.arraycopy(value, pos, sub, 0, sub.length);
        return sub;
    }

    /**
     * Convert a number to bytes representation
     *
     * @param value a number
     * @return bytes, without leading zeroes
     */
    protected byte[] toBytes(long value) {
        if (value == 0) {
            return new byte[0];
        }
        return shorten(ByteBuffer.allocate(8).putLong(value).array());
    }
}

package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

public class UInt implements Type<BigInteger> {
    public static final int SIZE_BYTES = 32;
    private int bytes;

    public UInt(int bits) {
        if (bits > 256 && bits % 8 != 0)
            throw new IllegalArgumentException("Invalid uint bits count.");

        this.bytes = bits / 8;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public int getBytesFixedSize() {
        return this.bytes;
    }

    @Override
    public Hex32[] encode(BigInteger obj) {
        if (obj.signum() < 0 && obj.toByteArray().length > this.bytes)
            throw new IllegalArgumentException("Invalid uint value.");

        ByteBuffer alignedBuf = ByteBuffer.allocate(Hex32.SIZE_BYTES);
        alignedBuf.put(new byte[Hex32.SIZE_BYTES - obj.toByteArray().length]);
        alignedBuf.put(obj.toByteArray());

        return new Hex32[] {new Hex32(alignedBuf.array())};
    }

    @Override
    public BigInteger decode(Hex32[] data) {
        if (data.length > 1)
            throw new IllegalArgumentException("Invalid input for decoding.");

        return new BigInteger(data[0].toString().replaceFirst("0x", "+"), 16);
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}

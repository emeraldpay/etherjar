package org.ethereumclassic.etherjar.contract.type;


import org.bouncycastle.util.Arrays;
import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Objects;

//TODO: refactor Numeric to Mixin.
public class Numeric implements Type<BigInteger>{
    protected int bytes;
    private boolean signed;

    protected Numeric(int bits, boolean signed) {
        if (bits > 256 || bits <= 0 || bits % 8 != 0)
            throw new IllegalArgumentException("Invalid bits count.");

        this.bytes = bits / 8;
        this.signed = signed;
    }

    @Override
    public String getName() {
        return null;
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
        if (Objects.isNull(obj))
            throw new IllegalArgumentException("Invalid input.");

        ByteBuffer alignedBuf = ByteBuffer.allocate(Hex32.SIZE_BYTES);

        if (obj.compareTo(BigInteger.valueOf(-1)) == 0) {
            byte[] temp = new byte[this.bytes];
            Arrays.fill(temp, (byte) 0xff);
            alignedBuf.put(new byte[Hex32.SIZE_BYTES - this.bytes])
                      .put(temp);
        } else {
            alignedBuf.put(new byte[Hex32.SIZE_BYTES - obj.toByteArray().length]);
            alignedBuf.put(obj.toByteArray());
        }

        return new Hex32[] {new Hex32(alignedBuf.array())};
    }

    @Override
    public BigInteger decode(Hex32[] data) {
        if (data.length > 1 || Objects.isNull(data))
            throw new IllegalArgumentException("Invalid input for decoding.");

        return new BigInteger(data[0].getBytes());
    }

    @Override
    public <V> V visit(Visitor<V> visitor) {
        return null;
    }
}

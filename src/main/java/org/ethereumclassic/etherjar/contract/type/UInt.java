package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;

public class UInt implements Type<BigInteger> {

    public static final int SIZE_BYTES = 32;

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public int getBytesFixedSize() {
        return SIZE_BYTES;
    }

    @Override
    public Hex32[] encode(BigInteger obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigInteger decode(Hex32[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}

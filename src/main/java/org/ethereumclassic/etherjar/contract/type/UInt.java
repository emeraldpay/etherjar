package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;

public class UInt extends Numeric{
     public UInt(int bits) {
        super(bits, false);
    }

    @Override
    public String getName() { return String.format("uint%d", this.bytes*8); }

    @Override
    public Hex32[] encode(BigInteger obj) {
        if (obj.signum() < 0 && obj.toByteArray().length > this.bytes)
            throw new IllegalArgumentException("Invalid input size.");

        return super.encode(obj);
    }

    @Override
    public BigInteger decode(Hex32[] data) {
        return super.decode(data);
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}

package org.ethereumclassic.etherjar.contract.type;


import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;

public class Int extends Numeric {
    public Int(int bits) {
        super(bits, true);
    }

    @Override
    public String getName() { return String.format("int%d", this.bytes*8); }

    @Override
    public Hex32[] encode(BigInteger obj) {
        if (obj.toByteArray().length > this.bytes)
            throw new IllegalArgumentException("Invalid int value.");
        return super.encode(obj);
    }

    @Override
    public BigInteger decode(Hex32[] data) {
        return super.decode(data);
    }

    @Override
    public Object visit(Visitor visitor) {
        return null;
    }
}

package org.ethereumclassic.etherjar.contract.type;


import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;

public class Int implements Type<BigInteger> {
    @Override
    public Object visit(Visitor visitor) {
        return null;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public int getBytesFixedSize() {
        return 0;
    }

    @Override
    public Hex32[] encode(BigInteger obj) {
        return new Hex32[0];
    }


    @Override
    public BigInteger decode(Hex32[] data) {
        return null;
    }
}

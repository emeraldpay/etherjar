package org.ethereumclassic.etherjar.contract.type;


import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;

public class Bool extends UInt {
    private final boolean value;

    public Bool(boolean value) {
        super(8);
        this.value = value;
    }

    @Override
    public Hex32[] encode(BigInteger obj) {
        return super.encode(obj.compareTo(BigInteger.ZERO) == 1
                            ? BigInteger.ONE
                            : BigInteger.ZERO);
    }

    @Override
    public BigInteger decode(Hex32[] data) {
        return super.decode(data);
    }

}

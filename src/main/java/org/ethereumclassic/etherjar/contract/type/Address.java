package org.ethereumclassic.etherjar.contract.type;


public class Address extends Numeric {
    public Address() {
        super(160, false);
    }


    @Override
    public String getName() { return new String("address"); }
}

package io.infinitape.etherjar.model;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Conversion units.
 */
public enum EtherUnit {

    WEI("wei", BigInteger.ONE),
    KWEI("Kwei", BigInteger.TEN.pow(3)),
    MWEI("Mwei", BigInteger.TEN.pow(6)),
    GWEI("Gwei", BigInteger.TEN.pow(9)),
    SZABO("szabo", BigInteger.TEN.pow(12)),
    FINNEY("finney", BigInteger.TEN.pow(15)),
    ETHER("ether", BigInteger.TEN.pow(18)),
    KETHER("Kether", BigInteger.TEN.pow(21)),
    METHER("Mether", BigInteger.TEN.pow(24)),
    GETHER("Gether", BigInteger.TEN.pow(27));

    private String name;
    private BigInteger weiCount;
    private BigDecimal weiDivider;

    EtherUnit(String name, BigInteger weiCount) {
        this.name = name;
        this.weiCount = weiCount;
        this.weiDivider = new BigDecimal(weiCount);
    }

    public String getName() {
        return name;
    }

    public BigInteger getWeiCount() {
        return weiCount;
    }

    public BigDecimal getWeiDivider() {
        return weiDivider;
    }
}

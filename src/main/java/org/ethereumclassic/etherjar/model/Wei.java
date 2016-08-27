package org.ethereumclassic.etherjar.model;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Wei Value (amount)
 *
 * @author Igor Artamonov
 */
public class Wei {

    private final BigInteger value;

    public Wei(BigInteger value) {
        this.value = value;
    }

    public Wei(byte[] value) {
        this(new BigInteger(value));
    }

    public Wei(String value) {
        this.value = HexNumber.parse(value);
    }

    /**
     *
     * @param value amount in Ether
     * @return corresponding amount in wei
     */
    public static Wei fromEther(double value) {
        return new Wei(new BigDecimal(value).multiply(EtherUnit.ETHER.getWeiDivider()).toBigInteger());
    }

    /**
     *
     * @param etherUnit convert to unit
     * @return decimal value for specified unit (default decimal scale is 6)
     */
    public BigDecimal convertTo(EtherUnit etherUnit) {
        return this.convertTo(etherUnit, 6);
    }

    /**
     *
     * @param etherUnit convert to unit
     * @param scale decimal scale
     * @return decimal value for specified unit
     */
    public BigDecimal convertTo(EtherUnit etherUnit, int scale) {
        if (etherUnit == EtherUnit.WEI) {
            return new BigDecimal(this.value);
        }
        return new BigDecimal(this.value)
            .divide(etherUnit.getWeiDivider(), scale, BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * @see #convertTo
     * @return corresponding value in Ether
     */
    public BigDecimal toEther() {
        return convertTo(EtherUnit.ETHER);
    }

    /**
     *
     * @return bytes value
     */
    public byte[] getBytes() {
        return value.toByteArray();
    }

    /**
     *
     * @return source value of wei
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Format value as string using specified unit
     *
     * @param etherUnit target unit
     * @param scale decimal scale
     * @return formatted value
     */
    public String toString(EtherUnit etherUnit, int scale) {
        return String.valueOf(convertTo(etherUnit, scale)) + ' ' + etherUnit.getName();
    }

    public String toString() {
        return this.toString(EtherUnit.ETHER, 4);
    }
}

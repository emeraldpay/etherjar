package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Signed & unsigned fixed-point number.
 */
public abstract class DecimalType implements StaticType<BigDecimal> {

    static BigDecimal powerOfTwo(int bits) {
        return new BigDecimal(NumericType.powerOfTwo(bits));
    }

    private final int mBits;

    private final int nBits;

    private final boolean isSigned;

    private final BigDecimal fractionFactor;

    private final NumericType numericType;

    protected DecimalType(int mBits, int nBits, boolean isSigned) {
        if (mBits <= 0 || mBits % 8 != 0)
            throw new IllegalArgumentException("Decimal type invalid mBits count: " + mBits);

        if (nBits <= 0 || nBits % 8 != 0)
            throw new IllegalArgumentException("Decimal type invalid nBits count: " + nBits);

        if (mBits + nBits > 256)
            throw new IllegalArgumentException(
                    "Decimal type invalid total bits count: " + (nBits + mBits));

        this.mBits = mBits;
        this.nBits = nBits;
        this.isSigned = isSigned;

        fractionFactor = powerOfTwo(nBits);

        numericType = isSigned ?
                new IntType(mBits + nBits) : new UIntType(mBits + nBits);
    }

    /**
     * @return number of N bits
     */
    public int getNBits() {
        return nBits;
    }

    /**
     * @return number of M bits
     */
    public int getMBits() {
        return mBits;
    }

    /**
     * @return number of total bits
     */
    public int getBits() {
        return nBits + mBits;
    }

    /**
     * @return {@code true} if this {@link Type} is signed, otherwise {@code false}
     */
    public boolean isSigned() {
        return isSigned;
    }

    public Hex32 encode(double value) {
        return encodeStatic(BigDecimal.valueOf(value));
    }

    @Override
    public Hex32 encodeStatic(BigDecimal value) {
        BigInteger integer = value.multiply(fractionFactor)
                .setScale(0, RoundingMode.HALF_UP).toBigInteger();

        if (!numericType.isValueValid(integer))
            throw new IllegalArgumentException("Decimal value out of range: " + value);

        return numericType.encodeStatic(integer);
    }

    @Override
    public BigDecimal decodeStatic(Hex32 hex32) {
        BigInteger integer = numericType.decode(hex32);

        //noinspection BigDecimalMethodWithoutRoundingCalled
        return new BigDecimal(integer).divide(fractionFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), nBits, mBits, isSigned);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        DecimalType other = (DecimalType) obj;

        return nBits == other.nBits
                && mBits == other.mBits
                && isSigned == other.isSigned;
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}

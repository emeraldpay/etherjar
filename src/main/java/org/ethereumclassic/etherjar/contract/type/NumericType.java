package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Signed & unsigned integer type.
 */
public abstract class NumericType implements SimpleType<BigInteger> {

    private final static byte[] NEGATIVE_ARRAY_FOR_PADDING = new byte[32];

    static {
        Arrays.fill(NEGATIVE_ARRAY_FOR_PADDING, (byte) 0xFF);
    }

    static BigInteger powerOfTwo(int bits) {
        if (bits < 0)
            throw new IllegalArgumentException(
                    "Negative number of bits to calculate the power of two: " + bits);

        return BigInteger.ONE.shiftLeft(bits);
    }

    private final int bits;

    private final boolean isSigned;

    protected NumericType(int bits, boolean isSigned) {
        if (bits <= 0 || bits > 256 || bits % 8 != 0)
            throw new IllegalArgumentException("Invalid numeric type bits count: " + bits);

        this.bits = bits;
        this.isSigned = isSigned;
    }

    /**
     * @return number of bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * @return {@code true} if this {@link Type} is signed, otherwise {@code false}
     */
    public boolean isSigned() {
        return isSigned;
    }

    /**
     * Is a {@code value} is in a valid {@link Type} range.
     *
     * @param value a numeric value
     * @return {@code true} if {@code value} is valid, otherwise {@code false}
     */
    public boolean isValueValid(BigInteger value) {
        return value.compareTo(getMinValue()) >= 0 && value.compareTo(getMaxValue()) < 0;
    }

    /**
     * @return a minimal value (inclusive)
     */
    public abstract BigInteger getMinValue();

    /**
     * @return a maximum value (exclusive)
     */
    public abstract BigInteger getMaxValue();

    public Hex32 encode(long value) {
        return encodeSimple(BigInteger.valueOf(value));
    }

    @Override
    public Hex32 encodeSimple(BigInteger value) {
        if (!isValueValid(value))
            throw new IllegalArgumentException("Numeric value out of range: " + value);

        byte[] data = value.toByteArray();

        byte[] arr = new byte[Hex32.SIZE_BYTES];

        if (value.signum() == -1) {
            System.arraycopy(NEGATIVE_ARRAY_FOR_PADDING, 0, arr, 0, Hex32.SIZE_BYTES);
        }

        int bytes = bits >>> 3;

        if (data.length > bytes) {
            System.arraycopy(data, data.length - bytes, arr, Hex32.SIZE_BYTES - bytes, bytes);
        } else {
            System.arraycopy(data, 0, arr, Hex32.SIZE_BYTES - data.length, data.length);
        }

        return new Hex32(arr);
    }

    @Override
    public BigInteger decodeSimple(Hex32 hex32) {
        BigInteger value = new BigInteger(hex32.getBytes());

        if (!isSigned && value.signum() < 0) {
            value = getMaxValue().add(value);
        }

        if (!isValueValid(value))
            throw new IllegalArgumentException("Excess data to decode numeric value: " + hex32);

        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), bits, isSigned);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        NumericType other = (NumericType) obj;

        return bits == other.bits
                && isSigned == other.isSigned;
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}

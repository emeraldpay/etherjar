package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public abstract class NumericType implements Type<BigInteger> {

    private final static byte[] NEGATIVE_ARRAY_FOR_PADDING = new byte[32];

    static {
        Arrays.fill(NEGATIVE_ARRAY_FOR_PADDING, (byte) 0xFF);
    }

    private final int bytes;

    private final boolean isSigned;

    protected NumericType() {
        this(Hex32.SIZE_BYTES << 3);
    }

    protected NumericType(int bits) {
        this(bits, false);
    }

    protected NumericType(int bits, boolean isSigned) {
        if (bits <= 0 || bits > 256 || bits % 8 != 0)
            throw new IllegalArgumentException("Invalid bits count: " + bits);

        this.bytes = bits >>> 3;
        this.isSigned = isSigned;
    }

    /**
     * @return number of bytes
     */
    public int getBytes() {
        return bytes;
    }

    /**
     * @return number of bits
     */
    public int getBits() {
        return bytes << 3;
    }

    /**
     * @return {@code true} if this {@link Type} is isSigned, otherwise {@code false}
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

    @Override
    public <V> V visit(Visitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public int getEncodedSize() {
        return Hex32.SIZE_BYTES;
    }

    @Override
    public Hex32[] encode(BigInteger value) {
        if (!isValueValid(value))
            throw new IllegalArgumentException("Out of range: " + value);

        byte[] data = value.toByteArray();

        byte[] arr = new byte[Hex32.SIZE_BYTES];

        if (value.signum() == -1) {
            System.arraycopy(NEGATIVE_ARRAY_FOR_PADDING, 0, arr, 0, Hex32.SIZE_BYTES - bytes);
        }

        System.arraycopy(data, data.length - bytes, arr, Hex32.SIZE_BYTES - bytes, bytes);

        return new Hex32[] { new Hex32(arr) };
    }

    @Override
    public BigInteger decode(Hex32[] data) {
        if (data.length == 0 || data.length > 1)
            throw new IllegalArgumentException("Not single input data length: " + data.length);

        BigInteger value = new BigInteger(data[0].getBytes());

        if (!isSigned && value.signum() < 0) {
            value = getMaxValue().add(value);
        }

        if (!isValueValid(value))
            throw new IllegalArgumentException("Out of range: " + value);

        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), bytes, isSigned);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        NumericType other = (NumericType) obj;

        return bytes == other.bytes
                && isSigned == other.isSigned;
    }
}

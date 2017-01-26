package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.Hex32;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Address type, equivalent to 'uint160'.
 *
 * @see Address
 */
public class AddressType implements StaticType<Address> {

    public final static AddressType DEFAULT = new AddressType();

    final static int OFFSET_BYTES = Hex32.SIZE_BYTES - Address.SIZE_BYTES;

    final static byte[] PADDING_EMPTY_ARRAY = new byte[OFFSET_BYTES];

    /**
     * Try to parse a {@link AddressType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link AddressType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     *
     * @see #getCanonicalName()
     */
    public static Optional<AddressType> from(String str) {
        Objects.requireNonNull(str);

        if (!Objects.equals(str, "address"))
            return Optional.empty();

        return Optional.of(DEFAULT);
    }

    @Override
    public String getCanonicalName() { return "address"; }

    @Override
    public Hex32 encodeStatic(Address obj) {
        byte[] buf = new byte[Hex32.SIZE_BYTES];

        System.arraycopy(obj.getBytes(), 0, buf, OFFSET_BYTES, Address.SIZE_BYTES);

        return new Hex32(buf);
    }

    @Override
    public Address decodeStatic(Hex32 hex32) {
        byte[] buf = hex32.getBytes();

        if (!Arrays.equals(Arrays.copyOf(buf, OFFSET_BYTES), PADDING_EMPTY_ARRAY))
            throw new IllegalArgumentException("Excess data to decode address: " + hex32);

        return Address.from(Arrays.copyOfRange(buf, OFFSET_BYTES, Hex32.SIZE_BYTES));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        return Objects.equals(getClass(), obj.getClass());
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}

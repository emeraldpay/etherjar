package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.MethodId;

import java.util.*;

/**
 * An address, followed by a function selector, equivalent to 'bytes24'.
 *
 * @see Address
 * @see MethodId
 */
public class FunctionType implements StaticType<Map.Entry<Address, MethodId>> {

    public final static FunctionType DEFAULT = new FunctionType();

    final static int OFFSET_ADDRESS_BYTES = Hex32.SIZE_BYTES - Address.SIZE_BYTES - MethodId.SIZE_BYTES;
    final static int OFFSET_METHODID_BYTES = Hex32.SIZE_BYTES - MethodId.SIZE_BYTES;

    final static byte[] PADDING_EMPTY_ARRAY = new byte[OFFSET_ADDRESS_BYTES];

    /**
     * Try to parse a {@link FunctionType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link FunctionType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     *
     * @see #getCanonicalName()
     */
    public static Optional<FunctionType> from(String str) {
        Objects.requireNonNull(str);

        if (!Objects.equals(str, "function"))
            return Optional.empty();

        return Optional.of(DEFAULT);
    }

    @Override
    public String getCanonicalName() { return "function"; }

    @Override
    public Hex32 encodeStatic(Map.Entry<Address, MethodId> obj) {
        byte[] buf = new byte[Hex32.SIZE_BYTES];

        System.arraycopy(obj.getKey().getBytes(), 0, buf, OFFSET_ADDRESS_BYTES, Address.SIZE_BYTES);
        System.arraycopy(obj.getValue().getBytes(), 0, buf, OFFSET_METHODID_BYTES, MethodId.SIZE_BYTES);

        return new Hex32(buf);
    }

    @Override
    public Map.Entry<Address, MethodId> decodeStatic(Hex32 hex32) {
        byte[] buf = hex32.getBytes();

        if (!Arrays.equals(Arrays.copyOf(buf, OFFSET_ADDRESS_BYTES), PADDING_EMPTY_ARRAY))
            throw new IllegalArgumentException("Excess data to decode address: " + hex32);

        Address address = Address.from(
                Arrays.copyOfRange(buf, OFFSET_ADDRESS_BYTES, OFFSET_METHODID_BYTES));

        MethodId methodId = MethodId.from(
                Arrays.copyOfRange(buf, OFFSET_METHODID_BYTES, Hex32.SIZE_BYTES));

        return new AbstractMap.SimpleEntry<>(address, methodId);
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

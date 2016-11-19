package org.ethereumclassic.etherjar.contract.type;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.MethodId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The first four bytes of the call data for a function call specifies the function to be called. It is the
 * first (left, high-order in big-endian) four bytes of the Keccak (SHA-3) hash of the signature of the function. The
 * signature is defined as the canonical expression of the basic prototype, i.e. the function name with the
 * parenthesised list of parameter types. Parameter types are split by a single comma - no spaces are used.
 *
 * <p><b>Example:</b> <code>baz(uint32,bool)</code> with arguments <tt>(69, true)</tt> becomes
 * <tt>0xcdcd77c000000000000000000000000000000000000000000000000000000000000000450000000000000000000000000000000000000000000000000000000000000001</tt>
 *
 * @author Igor Artamonov
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public class MethodType implements Type<Object[]> {

    /**
     * Single-threaded builder.
     */
    public static class Builder {

        final static Pattern SIGNATURE_PATTERN =
            Pattern.compile("\\p{Alpha}+\\d*\\((\\w*|\\[|\\]|((?<!,),(?!\\))))*\\)");

        /**
         * Check contract method signature
         *
         * @param signature a method name
         * @return boolean
         */
        static boolean isSignatureValid(String signature) {
            return SIGNATURE_PATTERN.matcher(signature).matches();
        }

        private MethodId id;

        private List<Type> types = new ArrayList<>();

        /**
         * Start to build from full method signature like <tt>name(datatype1,datatype2)</tt>, or <tt>transfer(address,uint256)</tt>
         *
         * <p>Make sure you're using canonical name type, e.g <tt>uint256</tt> instead of simple <tt>uint</tt>
         *
         * @param signature full method signature ({@link #SIGNATURE_PATTERN})
         * @return builder
         */
        public Builder fromFullName(String signature) {
            if (signature == null)
                throw new IllegalArgumentException("Null contract method signature");
            if (!isSignatureValid(signature))
                throw new IllegalArgumentException("Invalid contract method signature: " + signature);
            Keccak.Digest256 keccak = new Keccak.Digest256();
            keccak.update(signature.getBytes());
            byte[] hash = keccak.digest();
            byte[] head = new byte[4];
            System.arraycopy(hash, 0, head, 0, 4);
            id = MethodId.from(head);
            return this;
        }

        public MethodType build() {
            return new MethodType(id, types);
        }
    }

    private final MethodId id;

    private final List<Type> parameterTypes;

    private MethodType(MethodId id, Type... parameterTypes) {
        this(id, Arrays.asList(parameterTypes));
    }

    private MethodType(MethodId id, Collection<Type> parameterTypes) {
        this.id = id;
        this.parameterTypes = new ArrayList<>(parameterTypes);
    }

    /**
     * @return function id
     */
    public MethodId getId() {
        return id;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public HexData encode(Object[] args) {
        Hex32[] arr = null;

        return encodeCall(arr);
    }

    @Override
    public Object[] decode(HexData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    /**
     * Encodes call data to send through RPC
     *
     * @param params encoded parameters of the call
     * @return encoded call
     */
    HexData encodeCall(Hex32... params) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            buf.write(id.getBytes());
            for (Hex32 param: params) {
                buf.write(param.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HexData(buf.toByteArray());
    }
}

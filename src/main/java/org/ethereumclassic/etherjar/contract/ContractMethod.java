package org.ethereumclassic.etherjar.contract;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.MethodId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class ContractMethod {

    private MethodId id;

    private ContractMethod(MethodId id) {
        this.id = id;
    }

    /**
     * @return function id
     */
    public MethodId getId() {
        return id;
    }

    /**
     * Encodes call data to send through RPC
     *
     * @param params parameters of the call
     * @return encoded call
     */
    public HexData encodeCall(Hex32... params) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            buf.write(id.getBytes());
            for (Hex32 param: params) {
                buf.write(param.getBytes());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new HexData(buf.toByteArray());
    }

    static class Builder {

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

        public Builder() {
        }

        /**
         * builds from full method signature like `name(datatype1,datatype2)`, or transfer(address,uint256)
         *
         * Make sure you're using canonical name type, e.g uint256 instead of simple uint
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

        public ContractMethod build() {
            return new ContractMethod(id);
        }
    }

}

package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.contract.type.Type;
import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.MethodId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * A smart contract method.
 *
 * @author Igor Artamonov
 * @see Contract
 */
public class ContractMethod {

    /**
     * Build from canonical method signature like <tt>name(datatype1,datatype2)</tt>, or <tt>transfer(address,uint256)</tt>.
     *
     * <p>Make sure you're using canonical name type, e.g <tt>uint256</tt> instead of simple <tt>uint</tt>
     *
     * <p>The signature is defined as the canonical expression of the basic prototype,
     * i.e. the function name with the parenthesised list of parameter types.
     * Parameter types are split by a single comma - no spaces are used.
     *
     * @param signature canonical signature
     * @return contract method instance
     * @see MethodId#fromSignature(String)
     */
    public static ContractMethod fromSignature(String signature) {
        if (signature == null)
            throw new IllegalArgumentException("Null contract method signature");

        return new ContractMethod(MethodId.fromSignature(signature));
    }

    private final MethodId id;

    private final List<Type> parameterTypes;

    public ContractMethod(MethodId id, Type... parameterTypes) {
        this.id = id;
        this.parameterTypes = Collections.unmodifiableList(Arrays.asList(parameterTypes));
    }

    /**
     * @return the method id
     */
    public MethodId getId() {
        return id;
    }

    /**
     * @return the method parameter types
     */
    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Encodes call data to send through RPC.
     *
     * <p><b>Example:</b> <code>baz(uint32,bool)</code> with arguments <tt>(69, true)</tt> becomes
     * <tt>0xcdcd77c000000000000000000000000000000000000000000000000000000000000000450000000000000000000000000000000000000000000000000000000000000001</tt>
     *
     * @param params parameters of the call
     * @return encoded call
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#function-selector-and-argument-encoding">Function Selector and Argument Encoding</a>
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#examples">Examples</a>
     */
    public HexData encodeCall(Object... params) {
        if (parameterTypes.size() != params.length)
            throw new IllegalArgumentException("Wrong number of input parameters: " + params.length);

        int headBytesSize = 0;
        int tailBytesSize = 0;

        for (Type type : parameterTypes) {
            headBytesSize += type.isDynamic() ?
                Hex32.SIZE_BYTES : type.getBytesFixedSize();
        }

        List<Hex32> head = new ArrayList<>(headBytesSize / Hex32.SIZE_BYTES);
        List<Hex32> tail = new ArrayList<>();

        for (int i = 0; i < parameterTypes.size(); i++) {
            Type type = parameterTypes.get(i);

            //noinspection unchecked
            Hex32[] data = type.encode(params[i]);

            if (!type.isDynamic()) {
                Collections.addAll(head, data);
            } else {
                Collections.addAll(tail, data);

                head.add(Hex32.from(MethodId.SIZE_BYTES + headBytesSize + tailBytesSize));
                tailBytesSize += data.length * Hex32.SIZE_BYTES;
            }
        }

        HexData[] data = new HexData[head.size() + tail.size() + 1];

        data[0] = id;
        System.arraycopy(head.toArray(new Hex32[head.size()]), 0, data, 1, head.size());
        System.arraycopy(tail.toArray(new Hex32[tail.size()]), 0, data, head.size() + 1, tail.size());

        return HexData.from(data);
    }

    /**
     * Encodes call data to send through RPC.
     *
     * @param params encoded parameters of the call
     * @return encoded call
     * @see #encodeCall(Object...)
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

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        ContractMethod other = (ContractMethod) obj;

        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return String.format("%s!%h@%h{id=%s,params=%s}",
            getClass().getSimpleName(), System.identityHashCode(this), hashCode(), id, parameterTypes);
    }
}

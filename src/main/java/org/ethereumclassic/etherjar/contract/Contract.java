package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.contract.type.MethodType;
import org.ethereumclassic.etherjar.model.MethodId;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A smart contract value object (ABI)
 *
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public class Contract {

    private final Collection<MethodType> methods;

    public Contract(MethodType... methods) {
        this(Arrays.asList(methods));
    }

    public Contract(Collection<MethodType> methods) {
        this.methods = Collections.unmodifiableCollection(methods);
    }

    /**
     * Find a method type by a method signature
     *
     * @param id a method signature id
     * @return a required method type or <tt>null</tt>
     */
    public MethodType getMethod(MethodId id) {
        if (id == null)
            throw new IllegalArgumentException("Null method signature id");

        for (MethodType method : methods) {
            if (method.getId().equals(id))
                return method;
        }

        return null;
    }

    /**
     * Get all method types
     *
     * @return a method types collection
     */
    public Collection<MethodType> getMethods() {
        return methods;
    }
}

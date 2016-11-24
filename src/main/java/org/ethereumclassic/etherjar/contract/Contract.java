package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.MethodId;

import java.util.*;

/**
 * A smart contract.
 *
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public class Contract {

    public static class Builder {

        private Address address;

        private ContractMethod constructor;

        private Collection<ContractMethod> methods;

        /**
         * @param address a contract address
         * @return the current builder object
         */
        public Builder withAdress(Address address) {
            this.address = Objects.requireNonNull(address);

            return this;
        }

        /**
         * @param constructor a contract constructor
         * @return the current builder object
         */
        public Builder withConstructor(ContractMethod constructor) {
            this.constructor = Objects.requireNonNull(constructor);

            return this;
        }

        /**
         * @param methods a contract methods
         * @return the current builder object
         */
        public Builder withMethods(ContractMethod... methods) {
            return withMethods(Arrays.asList(methods));
        }

        /**
         * @param methods a contract methods
         * @return the current builder object
         */
        public Builder withMethods(Collection<ContractMethod> methods) {
            this.methods = Objects.requireNonNull(methods);

            return this;
        }

        /**
         * Build a {@link Contract} object with predefined by builder conditions.
         *
         * @return a {@link Contract} object
         */
        public Contract build() {
            if (Objects.isNull(address) || Objects.isNull(methods) || methods.size() == 0)
                throw new IllegalStateException(
                        "Wrong contract builder state (null address, ot empty method list)");

            return Objects.isNull(constructor) ?
                    new Contract(address, methods) :
                    new Contract(address, constructor, methods);
        }
    }

    private final Address address;

    private final Optional<ContractMethod> constructor;

    private final Collection<ContractMethod> methods;

    public Contract(Address address, ContractMethod... methods) {
        this(address, null, methods);
    }

    public Contract(Address address, Collection<ContractMethod> methods) {
        this(address, null, methods);
    }

    public Contract(Address address, ContractMethod constructor, ContractMethod... methods) {
        this(address, constructor, Arrays.asList(methods));
    }

    public Contract(
            Address address, ContractMethod constructor, Collection<ContractMethod> methods) {
        this.address = Objects.requireNonNull(address);
        this.constructor = Optional.ofNullable(constructor);
        this.methods = Collections.unmodifiableCollection(new ArrayList<>(methods));
    }

    /**
     * Find a method type by a method signature.
     *
     * @param id a method signature id
     * @return a required method type or <tt>null</tt>
     */
    public ContractMethod getMethod(MethodId id) {
        if (id == null)
            throw new IllegalArgumentException("Null method signature id");

        for (ContractMethod method : methods) {
            if (method.getId().equals(id))
                return method;
        }

        return null;
    }

    /**
     * Get all method types.
     *
     * @return a method types collection
     */
    public Collection<ContractMethod> getMethods() {
        return methods;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), address);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        Contract other = (Contract) obj;

        return Objects.equals(address, other.address);
    }

    @Override
    public String toString() {
        return String.format("%s!%h@%h{address=%s,constructor=%b,methods=%s}",
                getClass().getSimpleName(), System.identityHashCode(this), hashCode(),
                address, constructor, methods);
    }
}

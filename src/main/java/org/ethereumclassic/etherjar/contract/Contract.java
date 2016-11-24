package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.MethodId;

import java.util.*;

/**
 * A smart contract (ABI).
 *
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public class Contract {

    public static class Builder {

        private Address address;

        private Collection<ContractMethod> methods;

        /**
         * @param address a contract address
         * @return the current builder object
         */
        public Builder at(Address address) {
            this.address = Objects.requireNonNull(address);

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
            return new Contract(address, methods);
        }
    }

    private final Address address;

    private final Collection<ContractMethod> methods;

    public Contract(Address address, ContractMethod... methods) {
        this(address, Arrays.asList(methods));
    }

    public Contract(Address address, Collection<ContractMethod> methods) {
        this.address = Objects.requireNonNull(address);
        this.methods = Collections.unmodifiableCollection(new ArrayList<>(methods));
    }

    /**
     * Get contract's address.
     *
     * @return an address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Get all contract's methods.
     *
     * @return a methods collection
     */
    public Collection<ContractMethod> getMethods() {
        return methods;
    }

    /**
     * Find a method type by a method signature id.
     *
     * @param id a method signature id
     * @return an {@code Optional} containing required method, or an empty
     * {@code Optional} if method with the given {@code id} doesn't exist
     */
    public Optional<ContractMethod> findMethod(MethodId id) {
        Objects.requireNonNull(id);

        return methods.stream().filter(it -> it.getId().equals(id)).findFirst();
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
        return String.format("%s!%h@%h{address=%s,methods=%s}",
                getClass().getSimpleName(), System.identityHashCode(this), hashCode(),
                address, methods);
    }
}

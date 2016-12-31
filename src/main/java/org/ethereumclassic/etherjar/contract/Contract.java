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

        private Address address = Address.EMPTY;

        private Collection<ContractMethod> methods = Collections.emptyList();

        /**
         * @param address a contract address
         * @return the current builder object
         */
        public Builder withAddress(Address address) {
            this.address = Objects.requireNonNull(address);

            return this;
        }

        /**
         * @param methods contract methods
         * @return the current builder object
         */
        public Builder withMethods(ContractMethod... methods) {
            return withMethods(Arrays.asList(methods));
        }

        /**
         * @param methods a contract methods collection
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

    private final List<ContractMethod> methods;

    public Contract(Address address, ContractMethod... methods) {
        this(address, Arrays.asList(methods));
    }

    public Contract(Address address, Collection<ContractMethod> methods) {
        this.address = Objects.requireNonNull(address);
        this.methods = Collections.unmodifiableList(new ArrayList<>(methods));
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
     * @return a list of methods
     */
    public List<ContractMethod> getMethods() {
        return methods;
    }

    /**
     * Find a methods type by a methods signature id.
     *
     * @param id a methods signature id
     * @return an {@code Optional} containing required methods, or an empty
     * {@code Optional} if methods with the given {@code id} doesn't exist
     */
    public Optional<ContractMethod> findMethod(MethodId id) {
        Objects.requireNonNull(id);

        return methods.stream()
                .filter(it -> it.getId().equals(id)).findFirst();
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
        return String.format("%s{address=%s,methods=%s}",
                getClass().getSimpleName(), address, methods);
    }
}

package io.emeraldpay.etherjar.erc20;

import io.emeraldpay.etherjar.contract.AbstractContractEvent;
import io.emeraldpay.etherjar.contract.ContractEvent;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.EventId;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rpc.json.TransactionLogJson;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * ERC20 events that may be produces by a standard ERC-20 contract. Which are Transfer and Approval.
 * </p>
 *
 * <h2>Processing Transaction Logs</h2>
 *
 * <p>
 * When processing a Transaction Log, for example represented as {@link TransactionLogJson}, you can use
 * the {@link ERC20Event#extractFrom(TransactionLogJson)} method which returns the Event with associated factory to extract event details.
 * Note that if the Log is not for a ERC-20 event then that method returns <code>null</code>.
 * </p>
 *
 * <p>
 * Example for extracting the ERC-20 transfers:
 * </p>
 *
 * <pre><code>
 * TransactionReceiptJson receipt;
 *
 * for (TransactionLogJson log: receipt.getLogs()) {
 *    ERC20Event event = ERC20Event.extractFrom(log);
 *    if (ERC20Event.TRANSFER.equals(event)) {
 *        TransferDetails details = ((ContractEvent.Factory&lt;TransferDetails&gt;)event.getFactory())
 *            .readFrom(log);
 *
 *        System.out.println(
 *                  " Transfer from " + details.getFrom()
 *                  + " to " + details.getTo()
 *                  + " of " + details.getAmount());
 *    }
 * }
 * </code></pre>
 *
 */
public enum ERC20Event {

    /**
     * MUST trigger when tokens are transferred, including zero value transfers.
     *
     * A token contract which creates new tokens SHOULD trigger a Transfer event with the _from address set to 0x0 when tokens are created.
     *
     * <code>event Transfer(address indexed _from, address indexed _to, uint256 _value)</code>
     */
    TRANSFER(
        "Transfer",
        EventId.fromSignature("Transfer", "address", "address", "uint256"),
        TransferDetails.FACTORY
    ),


    /**
     * MUST trigger on any successful call to approve(address _spender, uint256 _value).
     *
     * <code>event Approval(address indexed _owner, address indexed _spender, uint256 _value)</code>
     */
    APPROVAL(
        "Approval",
        EventId.fromSignature("Approval", "address", "address", "uint256"),
        ApprovalDetails.FACTORY
    );

    private final String name;
    private final EventId id;
    private final ContractEvent.Factory factory;

    ERC20Event(String name, EventId id, ContractEvent.Factory factory) {
        this.name = name;
        this.id = id;
        this.factory = factory;
    }

    public String getEventName() {
        return name;
    }

    public EventId getEventId() {
        return id;
    }

    /**
     * Depending on the event type (Transfer or Approval) it returns a factory that may extract event details as TransferDetails or ApprovalDetails.
     *
     * @param <E> actual type, i.e. TransferDetails or ApprovalDetails
     * @return a factory that can parse the details specific for current event type
     * @see TransferDetails
     * @see ApprovalDetails
     */
    @SuppressWarnings("unchecked")
    public <E extends Details> ContractEvent.Factory<E> getFactory() {
        return factory;
    }

    /**
     * Tries to find associated Event from the specified Transaction Log Topic
     *
     * @param topic Transaction Log Topic at position 0
     * @return Event or null if it's not ERC20 event
     * @see TransactionLogJson#getTopics()
     */
    public static ERC20Event extractFrom(Hex32 topic) {
        if (TRANSFER.id.equals(topic)) {
            return TRANSFER;
        }
        if (APPROVAL.id.equals(topic)) {
            return APPROVAL;
        }
        return null;
    }

    /**
     * Tries to find associated Event from the specified Transaction Log
     *
     * @param log Transaction Log
     * @return Event or null if it's not ERC20 log
     * @throws NullPointerException if log is null
     */
    public static ERC20Event extractFrom(TransactionLogJson log) {
        List<Hex32> topics = log.getTopics();
        // both Transfer and Approval has exactly 3 topics (id + address + address)
        if (topics.size() != 3) {
            return null;
        }
        // data contains actual number and cannot be emty for a valid ERC20
        if (log.getData() == null || log.getData().isEmpty()) {
            return null;
        }
        return extractFrom(topics.get(0));
    }

    public abstract static class Details extends AbstractContractEvent {
        abstract ERC20Event getEvent();
    }

    /**
     * Details for Transfer event
     *
     * @see ERC20Event#TRANSFER
     */
    public static class TransferDetails extends Details {

        private final Address from;
        private final Address to;
        private final BigInteger amount;

        public static final ContractEvent.Factory<TransferDetails> FACTORY = log -> {
            List<Hex32> topics = log.getTopics();
            Address from = Address.extract(topics.get(1));
            Address to = Address.extract(topics.get(2));
            BigInteger amount = log.getData().split32()[0].asUInt();
            return new TransferDetails(from, to, amount);
        };

        public TransferDetails(Address from, Address to, BigInteger amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }

        /**
         *
         * @return sender address.
         */
        public Address getFrom() {
            return from;
        }

        /**
         *
         * @return recepient address
         */
        public Address getTo() {
            return to;
        }

        /**
         *
         * @return amount transferred
         */
        public BigInteger getAmount() {
            return amount;
        }

        @Override
        public EventId getEventId() {
            return TRANSFER.id;
        }

        @Override
        public List<Hex32> getArguments() {
            return Arrays.asList(
                Hex32.extendFrom(from),
                Hex32.extendFrom(to)
            );
        }

        @Override
        public HexData getData() {
            return Hex32.extendFrom(amount);
        }

        @Override
        ERC20Event getEvent() {
            return TRANSFER;
        }
    }

    /**
     * Details for Approval event
     *
     * @see ERC20Event#APPROVAL
     */
    public static class ApprovalDetails extends Details {

        private final Address owner;
        private final Address spender;
        private final BigInteger amountLimit;

        public static final ContractEvent.Factory<ApprovalDetails> FACTORY = log -> {
            List<Hex32> topics = log.getTopics();
            Address from = Address.extract(topics.get(1));
            Address to = Address.extract(topics.get(2));
            BigInteger amount = log.getData().split32()[0].asUInt();
            return new ApprovalDetails(from, to, amount);
        };

        public ApprovalDetails(Address owner, Address spender, BigInteger amountLimit) {
            this.owner = owner;
            this.spender = spender;
            this.amountLimit = amountLimit;
        }

        /**
         *
         * @return address which approves spending
         */
        public Address getOwner() {
            return owner;
        }

        /**
         *
         * @return address which may spend up to approved amount
         */
        public Address getSpender() {
            return spender;
        }

        /**
         *
         * @return spending limit amount; note that the original address may not have the whole amount, it's just an upper bound
         */
        public BigInteger getAmountLimit() {
            return amountLimit;
        }

        @Override
        public EventId getEventId() {
            return APPROVAL.id;
        }

        @Override
        public List<Hex32> getArguments() {
            return Arrays.asList(
                Hex32.extendFrom(owner),
                Hex32.extendFrom(spender)
            );
        }

        @Override
        public HexData getData() {
            return Hex32.extendFrom(amountLimit);
        }

        @Override
        ERC20Event getEvent() {
            return APPROVAL;
        }
    }
}

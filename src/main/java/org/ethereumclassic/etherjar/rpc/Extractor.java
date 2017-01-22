package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.HexQuantity;
import org.ethereumclassic.etherjar.model.TransactionId;
import org.ethereumclassic.etherjar.model.Wei;
import org.ethereumclassic.etherjar.rpc.ConcurrencyUtils.Function;
import org.ethereumclassic.etherjar.rpc.ConcurrencyUtils.FutureMap;

import java.math.BigInteger;
import java.util.concurrent.*;

/**
 * @author Igor Artamonov
 */
public class Extractor {

    public Future<BigInteger> extractBigInteger(final Future<String> result) {
        return new FutureMap<>(result, new Function<String, BigInteger>() {
            @Override
            public BigInteger apply(String value) {
                return HexQuantity.from(value).getValue();
            }
        });
    }

    public Future<Long> extractLong(final Future<String> result) {
        return new FutureMap<>(result, new Function<String, Long>() {
            @Override
            public Long apply(String value) {
                BigInteger parsed = HexQuantity.from(value).getValue();
                return parsed != null ? parsed.longValue() : null;
            }
        });
    }

    public Future<Wei> extractWei(final Future<String> result) {
        return new FutureMap<>(result, new Function<String, Wei>() {
            @Override
            public Wei apply(String value) {
                return Wei.from(value);
            }
        });
    }

    public Future<HexData> extractData(final Future<String> result) {
        return new FutureMap<>(result, new Function<String, HexData>() {
            @Override
            public HexData apply(String value) {
                return HexData.from(value);
            }
        });
    }

    public Future<TransactionId> extractTransactionId(final Future<String> result) {
        return new FutureMap<>(result, new Function<String, TransactionId>() {
            @Override
            public TransactionId apply(String value) {
                return TransactionId.from(value);
            }
        });
    }

}

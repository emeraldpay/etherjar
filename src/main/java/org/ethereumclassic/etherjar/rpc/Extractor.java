package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.model.HexQuantity;
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

    public Future<Integer> extractInteger(final Future<String> result) {
        return new FutureMap<>(result, new Function<String, Integer>() {
            @Override
            public Integer apply(String value) {
                BigInteger parsed = HexQuantity.from(value).getValue();
                return parsed != null ? parsed.intValue() : null;
            }
        });
    }

    public Future<Wei> extractWei(final Future<String> result) {
        return new FutureMap<>(result, new Function<String, Wei>() {
            @Override
            public Wei apply(String value) {
                return new Wei(value);
            }
        });
    }


}

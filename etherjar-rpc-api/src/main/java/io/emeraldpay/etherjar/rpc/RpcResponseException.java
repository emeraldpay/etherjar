package io.emeraldpay.etherjar.rpc;

import java.io.IOException;

/**
 * @author Igor Artamonov
 */
public class RpcResponseException extends IOException {

    public RpcResponseException(String message) {
        super(message);
    }

    public RpcResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}

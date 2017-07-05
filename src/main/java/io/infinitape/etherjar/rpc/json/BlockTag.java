package io.infinitape.etherjar.rpc.json;

/**
 * @author Igor Artamonov
 */
public enum BlockTag {

    LATEST("latest"), EARLIEST("earliest"), PENDING("pending");

    private String code;

    BlockTag(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

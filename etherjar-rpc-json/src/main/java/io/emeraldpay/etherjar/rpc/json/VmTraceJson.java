package io.emeraldpay.etherjar.rpc.json;

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.util.ArrayList;
import java.util.List;

public class VmTraceJson {
    private HexData code;
    private List<Op> ops;

    public static class Op {
        private Long cost;
        private OpEx ex;
        private Long pc;
        private Long sub;
        private String op;
        private String idx;
    }

    public static class OpEx {
        private OpExMem mem;
        private final List<HexData> push = new ArrayList<>();
        private Object store; //TODO
        private Long used;
    }

    public static class OpExMem {
        //"mem": {
        //    "data": "0x0000000000000000000000000000000000000000000000000000000000000080",
        //    "off": 64
        //},
        private Hex32 data;
        private int off;
    }
}

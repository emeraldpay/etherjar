package io.infinitape.etherjar.rpc.json

import io.infinitape.etherjar.domain.BlockHash
import io.infinitape.etherjar.domain.TransactionId
import io.infinitape.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class BlockJsonSpec extends Specification {

    def "Makes an identical copy of an empty block"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.copy()
        then:
        orig == copy
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Makes an identical copy of a block with tx hash"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.copy()
        then:
        orig == copy
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Makes an identical copy of a block with full tx"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000-full.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.copy()
        then:
        orig == copy
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Erase tx details when exists"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000-full.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.withoutTransactionDetails()
        def origErased = orig.copy()
        origErased.transactions = []
        def copyErased = copy.copy()
        copyErased.transactions = []
        then:
        copy.hash == BlockHash.from("0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f")
        copy.transactions == [
            new TransactionRefJson(TransactionId.from("0x6f75b64d9364b71b43cde81a889f95df72e6be004b28477f9083ed0ee471a7f9")),
            new TransactionRefJson(TransactionId.from("0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611")),
            new TransactionRefJson(TransactionId.from("0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b")),
            new TransactionRefJson(TransactionId.from("0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9")),
        ]
        origErased == copyErased
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Returns same without tx if contains only references"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.withoutTransactionDetails()
        then:
        copy.hash == BlockHash.from("0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f")
        copy.transactions == [
            new TransactionRefJson(TransactionId.from("0x6f75b64d9364b71b43cde81a889f95df72e6be004b28477f9083ed0ee471a7f9")),
            new TransactionRefJson(TransactionId.from("0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611")),
            new TransactionRefJson(TransactionId.from("0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b")),
            new TransactionRefJson(TransactionId.from("0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9")),
        ]
        orig == copy
    }

    def "Returns same without tx if empty"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.withoutTransactionDetails()
        then:
        copy.hash == BlockHash.from("0x88e96d4537bea4d9c05d12549907b32561d3bf31f45aae734cdc119f13406cb6")
        copy.transactions == []
        orig == copy
    }
}

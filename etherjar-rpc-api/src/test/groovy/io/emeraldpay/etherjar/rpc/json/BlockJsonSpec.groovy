package io.emeraldpay.etherjar.rpc.json

import io.emeraldpay.etherjar.domain.BlockHash
import io.emeraldpay.etherjar.domain.TransactionId
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
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

    def "Reads logsBloom"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-11388816.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)
        then:
        act.logsBloom.toHex() == "0x562568502e1a684125a8e28fc6e8dd81a11593a24c818b6eb159afe20082613b4cbd49456602d909fe2e5957029b87304e308817af4b9205c53122a2e07fa6024804ce70021487b86a71a7cae008f0f81698046882e18e4019c4c34ed9405af9739001448735b59b451c2271d444983f8512c411c68827c2c0a6103741b114c812bb2337c16e4a0585594442e06124bbc130d9074dc8375840d771f214d10f02924127c24400a122248b83ac4f51241ba6c2b525a4820a18dc0e323a372c412c014a808e4006148138a6903010988acc828306821c0b40b900fb211e01f1ec6d7cbcb434af400d28216820613b9180b93785a161a96675d036708a0266a3815a"
    }

    def "Reads baseFee"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-13104052.json")
        JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)
        then:
        act.baseFeePerGas.getAmount().toString() == "94932363924"
    }
}

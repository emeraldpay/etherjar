package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.Hex32
import spock.lang.Specification

class StateDiffJsonDeserializerSpec extends Specification {

    ObjectMapper objectMapper = new ObjectMapper()

    def "Reads Change as Full Change"() {
        setup:
        String json = '{\n' +
            '  "*": {\n' +
            '    "from": "0x5be273c9585aa1ce8c",\n' +
            '    "to": "0x5be36bd43b0eddaf88"\n' +
            '  }\n' +
            '}'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readChange(jp, Wei, StateDiffJsonDeserializer.WEI_CONVERTER)

        then:
        act != null
        act.type == StateDiffJson.ChangeType.REPLACE
        act.before == Wei.from("0x5be273c9585aa1ce8c")
        act.after == Wei.from("0x5be36bd43b0eddaf88")
    }

    def "Reads Change as No Change"() {
        setup:
        String json = '"="'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readChange(jp, Wei, StateDiffJsonDeserializer.WEI_CONVERTER)

        then:
        act != null
        act.type == StateDiffJson.ChangeType.NOTHING
    }

    def "Reads Change as Remove"() {
        setup:
        String json = '{\n' +
            '  "-": "0x0"\n' +
            '}'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readChange(jp, Wei, StateDiffJsonDeserializer.WEI_CONVERTER)

        then:
        act != null
        act.type == StateDiffJson.ChangeType.REMOVE
        act.before == Wei.ZERO
    }

    def "Reads Change as Create"() {
        setup:
        String json = '{\n' +
            '  "+": "0x1"\n' +
            '}'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readChange(jp, Wei, StateDiffJsonDeserializer.WEI_CONVERTER)

        then:
        act != null
        act.type == StateDiffJson.ChangeType.CREATE
        act.after == new Wei(1)
    }

    def "Reads Address diff"() {
        setup:
        String json = '{\n' +
            '            "balance": {\n' +
            '                "*": {\n' +
            '                    "from": "0x5be273c9585aa1ce8c",\n' +
            '                    "to": "0x5be36bd43b0eddaf88"\n' +
            '                }\n' +
            '            },\n' +
            '            "code": "=",\n' +
            '            "nonce": "=",\n' +
            '            "storage": {\n' +
            '                "0x0000000000000000000000000000000000000000000000000000000000000032": {\n' +
            '                    "*": {\n' +
            '                        "from": "0x00000000000000000000000000000000000000000000000000000000000c4be4",\n' +
            '                        "to": "0x00000000000000000000000000000000000000000000000000000000000c4beb"\n' +
            '                    }\n' +
            '                }\n' +
            '            }\n' +
            '        }'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readAddressDiff(jp)

        then:
        act != null
        with(act.balance) {
            it.type == StateDiffJson.ChangeType.REPLACE
            it.before == Wei.from("0x5be273c9585aa1ce8c")
            it.after == Wei.from("0x5be36bd43b0eddaf88")
        }
        with(act.code) {
            it.type == StateDiffJson.ChangeType.NOTHING
        }
        with(act.nonce) {
            it.type == StateDiffJson.ChangeType.NOTHING
        }
    }

    def "Reads Storage all diffs with empty"() {
        setup:
        String json = '{}'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readStorageDiffs(jp)

        then:
        act != null
        act.size() == 0
    }

    def "Reads Storage all diffs with single item"() {
        setup:
        String json = '{\n' +
            '                "0x0000000000000000000000000000000000000000000000000000000000000032": {\n' +
            '                    "*": {\n' +
            '                        "from": "0x00000000000000000000000000000000000000000000000000000000000c4be4",\n' +
            '                        "to": "0x00000000000000000000000000000000000000000000000000000000000c4beb"\n' +
            '                    }\n' +
            '                }\n' +
            '            }'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readStorageDiffs(jp)

        then:
        act != null
        act.size() == 1
        act.containsKey(Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000032"))
        with(act.get(Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000032"))) {
            it.type == StateDiffJson.ChangeType.REPLACE
            it.before == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000c4be4")
            it.after == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000c4beb")
        }
    }

    def "Reads Storage all diffs with multiple items"() {
        setup:
        String json = '{\n' +
            '                "0x0000000000000000000000000000000000000000000000000000000000000008": {\n' +
            '                    "*": {\n' +
            '                        "from": "0x604686ec00000000001717828d2ca7e37f2f000000001a7a9ef8df8347ed5425",\n' +
            '                        "to": "0x604687000000000000173220f671cb397b2f000000001a5c534553cf62356a48"\n' +
            '                    }\n' +
            '                },\n' +
            '                "0x0000000000000000000000000000000000000000000000000000000000000009": {\n' +
            '                    "*": {\n' +
            '                        "from": "0x00000000000000000000000000000000002ec9c15530aa736922c065dff8deeb",\n' +
            '                        "to": "0x00000000000000000000000000000000002edb3263db119c420e888e76e3df97"\n' +
            '                    }\n' +
            '                },\n' +
            '                "0x000000000000000000000000000000000000000000000000000000000000000a": {\n' +
            '                    "*": {\n' +
            '                        "from": "0x0000000000000000000000000000006bc8fbbeb2d2877a4e706e75e033c5e4cf",\n' +
            '                        "to": "0x0000000000000000000000000000006bdfeac17d17dcad33bf3508bd8bd1a883"\n' +
            '                    }\n' +
            '                }\n' +
            '            }'
        StateDiffJsonDeserializer deserializer = new StateDiffJsonDeserializer()
        when:
        def jp = objectMapper.readTree(json).traverse()
        jp.nextToken()
        def act = deserializer.readStorageDiffs(jp)

        then:
        act != null
        act.size() == 3
        with(act.get(Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000008"))) {
            it.type == StateDiffJson.ChangeType.REPLACE
            it.before == Hex32.from("0x604686ec00000000001717828d2ca7e37f2f000000001a7a9ef8df8347ed5425")
            it.after == Hex32.from("0x604687000000000000173220f671cb397b2f000000001a5c534553cf62356a48")
        }
        with(act.get(Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000009"))) {
            it.type == StateDiffJson.ChangeType.REPLACE
            it.before == Hex32.from("0x00000000000000000000000000000000002ec9c15530aa736922c065dff8deeb")
            it.after == Hex32.from("0x00000000000000000000000000000000002edb3263db119c420e888e76e3df97")
        }
        with(act.get(Hex32.from("0x000000000000000000000000000000000000000000000000000000000000000a"))) {
            it.type == StateDiffJson.ChangeType.REPLACE
            it.before == Hex32.from("0x0000000000000000000000000000006bc8fbbeb2d2877a4e706e75e033c5e4cf")
            it.after == Hex32.from("0x0000000000000000000000000000006bdfeac17d17dcad33bf3508bd8bd1a883")
        }
    }

    def "Reads StateDiff"() {
        setup:
        String json = '{\n' +
            '        "0x0000000000007f150bd6f54c40a34d7c3d5e9f56": {\n' +
            '            "balance": {\n' +
            '                "*": {\n' +
            '                    "from": "0x5be273c9585aa1ce8c",\n' +
            '                    "to": "0x5be36bd43b0eddaf88"\n' +
            '                }\n' +
            '            },\n' +
            '            "code": "=",\n' +
            '            "nonce": "=",\n' +
            '            "storage": {\n' +
            '                "0x0000000000000000000000000000000000000000000000000000000000000032": {\n' +
            '                    "*": {\n' +
            '                        "from": "0x00000000000000000000000000000000000000000000000000000000000c4be4",\n' +
            '                        "to": "0x00000000000000000000000000000000000000000000000000000000000c4beb"\n' +
            '                    }\n' +
            '                }\n' +
            '            }\n' +
            '        },\n' +
            '        "0x1ea335202509e40a29c2cd1e40f15113565aedba": {\n' +
            '            "balance": {\n' +
            '                "-": "0x0"\n' +
            '            },\n' +
            '            "code": {\n' +
            '                "-": "0x6d7f150bd6f54c40a34d7c3d5e9f563318585733ff"\n' +
            '            },\n' +
            '            "nonce": {\n' +
            '                "-": "0x1"\n' +
            '            },\n' +
            '            "storage": {}\n' +
            '        }' +
            '     }'
        when:
        def act = objectMapper.readValue(json, StateDiffJson)
        then:
        act != null
        act.changes.size() == 2
    }

    def "Reads StateDiff 2"() {
        setup:
        String json = '{\n' +
            '        "0x86d21008a25866940bf044c5270af495f72e2511": {\n' +
            '            "balance": {\n' +
            '                "-": "0x0"\n' +
            '            },\n' +
            '            "code": {\n' +
            '                "-": "0x6d7f150bd6f54c40a34d7c3d5e9f563318585733ff"\n' +
            '            },\n' +
            '            "nonce": {\n' +
            '                "-": "0x1"\n' +
            '            },\n' +
            '            "storage": {}\n' +
            '        },\n' +
            '        "0xa26e80e7dea86279c6d778d702cc413e6cffa777": {\n' +
            '            "balance": {\n' +
            '                "*": {\n' +
            '                    "from": "0xc4a1e1a8deec3b30",\n' +
            '                    "to": "0xc4ccdae856225e10"\n' +
            '                }\n' +
            '            },\n' +
            '            "code": "=",\n' +
            '            "nonce": "=",\n' +
            '            "storage": {}\n' +
            '        },\n' +
            '        "0xb3b7874f13387d44a3398d298b075b7a3505d8d4": {\n' +
            '            "balance": {\n' +
            '                "*": {\n' +
            '                    "from": "0x3220f2c069b2c324290",\n' +
            '                    "to": "0x3220f9e44f3c5d0f068"\n' +
            '                }\n' +
            '            },\n' +
            '            "code": "=",\n' +
            '            "nonce": "=",\n' +
            '            "storage": {}\n' +
            '        }' +
            '     }'
        when:
        def act = objectMapper.readValue(json, StateDiffJson)
        then:
        act != null
        act.changes.size() == 3
    }
}

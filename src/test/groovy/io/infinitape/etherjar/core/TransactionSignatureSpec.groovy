package io.infinitape.etherjar.core

import spock.lang.Specification

class TransactionSignatureSpec extends Specification {

    TransactionSignature signature = new TransactionSignature()

    def "unprotected V"() {
        when:
        signature.v = 27
        signature.chainId = null
        then:
        signature.normalizedV == 27
        signature.extractedChainId == null
        !signature.protected
        when:
        signature.v = 28
        then:
        signature.normalizedV == 28
        signature.extractedChainId == null
        !signature.protected
    }

    def "protected V for classic"() {
        when:
        signature.v = 157
        signature.chainId = new ChainId(61)
        then:
        signature.extractedChainId.value == 61
        signature.normalizedV == 27
        signature.protected

        when:
        signature.v = 158
        signature.chainId = new ChainId(61)
        then:
        signature.extractedChainId.value == 61
        signature.normalizedV == 28
        signature.protected
    }

    def "protected V for classic testnet"() {
        when:
        signature.v = 159
        signature.chainId = new ChainId(62)
        then:
        signature.extractedChainId.value == 62
        signature.normalizedV == 27
        signature.protected

        when:
        signature.v = 160
        signature.chainId = new ChainId(62)
        then:
        signature.extractedChainId.value == 62
        signature.normalizedV == 28
        signature.protected
    }

    def "protected V for forked"() {
        when:
        signature.v = 38
        signature.chainId = new ChainId(1)
        then:
        signature.extractedChainId.value == 1
        signature.normalizedV == 28
        signature.protected

        when:
        signature.v = 37
        signature.chainId = new ChainId(1)
        then:
        signature.extractedChainId.value == 1
        signature.normalizedV == 27
        signature.protected
    }

    def "decline invalid v"() {
        when:
        signature.setV(1024)
        then:
        thrown(IllegalArgumentException)
        when:
        signature.setV(-1)
        then:
        thrown(IllegalArgumentException)
    }
}

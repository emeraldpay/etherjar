package io.emeraldpay.etherjar.tx

import spock.lang.Specification

class PrivateKeySpec extends Specification {

    def "Get address"() {
        expect:
        PrivateKey.create(pk).address.toHex() == address.toLowerCase()
        where:
        pk          | address
        "0x8313a298521f902c3b62121ac551d28ba3f844ccf1c1e0a98880981a8be91e52" | "0x75DD1046da96eB8e66eB095e912474929580aE55"
        "0xea65739882d3e4fe0bc4c0e4c49b9eb05f528a03580274b17dc53727390912a2" | "0x526E18bd5360837FEe312a05F3eD259F6141B7d7"
        "0x41c6b14b4e12b29da5549577cb4e0f799dc285ef22b18f2fd4c682b3e1ed0b13" | "0x9F4d18EF38724776b96780851222163f6249fB88"
        "0x0ac4414d1ff9f189bcd8e91f9be82627db51ec85a1c65f860932a4eab0dc39c2" | "0x202c977ce729A2659477381b949D372AD3269cD0"
        "0xff6a31e4a042d9dc9fbe983855459f2da5d088b72001fc752a5deaccc5c44871" | "0xf00bab1125b685a48A656508c9B3825F382a59fB"
        "0x1004451ebf56981dbac8c0de130b5d18a6833965ed9071ad305ac90a1671dc9d" | "0x9239C90C08FE68eca25aa8f29FFd73DEc1f0E31F"
        "0xb9cc30022af9d60ce6d19ac9a5c6a26dc5a001b7103baada11e76f0217c5b91e" | "0x001dA7A75DE7DfaBba42F9BabC7484c2a29262E7"
        "0x29e339d55949c854d2e0e416299b193fad0f325146a7ceecb99d668c585f4455" | "0x0052b4dCD2277D6e02ef0275597Cb584c7619bF3"
        "0x24be93d7f49cf1121f0caef133bf123d0b770f4861753adb5add11418ca44534" | "0xff777EF157F1235843fCB7c32c2EfFAd36020bb8"
    }
}

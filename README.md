# EtherJar

[![Bintray](https://img.shields.io/bintray/v/infinitape/etherjar/etherjar-domain.svg)](https://bintray.com/infinitape/etherjar)
[![Build Status](https://travis-ci.org/Infinitape/etherjar.svg?branch=master)](https://travis-ci.org/Infinitape/etherjar)
[![codecov](https://codecov.io/gh/Infinitape/etherjar/branch/master/graph/badge.svg)](https://codecov.io/gh/Infinitape/etherjar)
[![license](https://img.shields.io/github/license/infinitape/etherjar.svg?maxAge=2592000)](https://github.com/infinitape/etherjar/blob/master/LICENSE)

Framework agnostic modular Java 8+ integration library for [Ethereum blockchains](https://www.ethereum.org)

Latest Version: 0.6.0

## Architecture

* [ ] High-level [web3.js](https://github.com/ethereum/wiki/wiki/JavaScript-API)-like Java 8 API (_in progress_)
* [x] Low-level [JSON-RPC API](https://github.com/ethereum/wiki/wiki/JSON-RPC)
* [x] Transport data-layer 
  * [ ] IPC (_not implemented yet_)  
  * [x] HTTP
  * [ ] WebSockets (_in progress_)

## Modules

Structure of dependencies between modules:

* `etherjar-abi`
  * `etherjar-domain`
  * `etherjar-hex`
* `etherjar-domain`
* `etherjar-hex`  
* `etherjar-rlp`
* `etherjar-rpc-api`  
  * `etherjar-domain`
  * `etherjar-hex`  
* `etherjar-rpc-http`
  * `etherjar-rpc-api`
  * `etherjar-domain`
  * `etherjar-hex`
* `etherjar-rpc-ws`  
  * `etherjar-rpc-api`
* `etherjar-solidity`
  * `etherjar-abi`
  * `etherjar-domain`
* `etherjar-tx`
  * `etherjar-rlp`
  * `etherjar-domain`

where

* `etherjar-abi` - Smart contract [Application Binary Interface (ABI)](https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI)
* `etherjar-domain` - Core module contains pure domain logic (`Address`, `Block`, `Transaction`, `Wei` and so on)
* `etherjar-hex` - Hexadecimal encoding and encoding utils for `String`, `BigInteger`, byte arrays
* `etherjar-rlp` - Reading and writing RLP (Recursive Length Prefix) encoded data 
* `etherjar-rpc-api` - [JSON-RPC API](https://github.com/ethereum/wiki/wiki/JSON-RPC) generic implementation
* `etherjar-rpc-http` - HTTP transport implementation for JSON-RPC API data-layer
* `etherjar-rpc-ws` - WebSocket transport to subscribe to new blocks
* `etherjar-solidity` - Thin wrapper around [`solc` Solidity compiler](https://github.com/ethereum/solidity)
* `etherjar-tx` - Read, verify and manipulate Transactions   

## Usage

### Maven

```xml
<repositories>
    <repository>
      <id>etherjar</id>
      <url>https://dl.bintray.com/infinitape/etherjar</url>
    </repository>
</repositories>

<dependency>
  <groupId>io.infinitape</groupId>
  <artifactId>etherjar-rpc-http</artifactId>
  <version>0.6.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/infinitape/etherjar" 
    }
}

dependencies {
    compile 'io.infinitape:etherjar-rpc-http:0.6.0'
}
```

## Examples (version 0.7.0-SNAPSHOT)

How to call `web3_clientVersion` low-level JSON-RPC API method:

```java
public class GetClientVersion {
    public static void main(String[] args)
            throws URISyntaxException, IOException, ExecutionException, InterruptedException {

        try (RpcTransport trans = new DefaultRpcTransport(new URI("http://127.0.0.1:8545"))) {
            RpcClient client = new DefaultRpcClient(trans);
            Future<String> req = client.execute(Commands.web3().clientVersion());

            System.out.println(String.format("Client version: %s", req.get()));
        }
    }
}
```

How to call `eth_gasPrice` low-level JSON-RPC API method:

```java
public class GetGasPrice {

    public static void main(String[] args)
            throws URISyntaxException, IOException, ExecutionException, InterruptedException {

        try (RpcTransport trans = new DefaultRpcTransport(new URI("http://127.0.0.1:8545"))) {
            RpcClient client = new DefaultRpcClient(trans);
            Future<Wei> req = client.execute(Commands.eth().getGasPrice());

            System.out.println(String.format("Gas Price: %s Ether", req.get().toEthers(12)));
        }
    }
}
```

## Documentation

* [Reference Guide](./docs/index.md)

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/Infinitape/etherjar/issues).

## Licence

[Apache 2.0](LICENSE)

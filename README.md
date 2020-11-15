# EtherJar

[![Bintray](https://img.shields.io/bintray/v/emerald/etherjar/etherjar-domain.svg)](https://bintray.com/emerald/etherjar)
![Build Status](https://github.com/emeraldpay/etherjar/workflows/Tests/badge.svg)
[![codecov](https://codecov.io/gh/emeraldpay/etherjar/branch/master/graph/badge.svg)](https://codecov.io/gh/emeraldpay/etherjar)
[![license](https://img.shields.io/github/license/emeraldpay/etherjar.svg?maxAge=2592000)](https://github.com/emeraldpay/etherjar/blob/master/LICENSE)

Framework agnostic modular Java 11+ integration library for [Ethereum blockchains](https://www.ethereum.org)

- Latest Stable Version: 0.10.0
- Latest Snapshot Version: 0.11.0-SNAPSHOT

## Architecture

* [x] Low-level [JSON-RPC API](https://github.com/ethereum/wiki/wiki/JSON-RPC)
* [x] Transport data-layer 
  * [ ] IPC (_not implemented yet_)  
  * [x] HTTP
  * [x] WebSockets
  * [x] gRPC
* [ ] High-level Java 8 API (_in progress_)

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
* `etherjar-rpc-emerald`
  * `etherjar-rpc-api`
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
* `etherjar-contract`
  * `etherjar-abi`
  * `etherjar-domain`
  * `etherjar-rpc-api` 
* `etherjar-erc20`  
  * `etherjar-abi`
  * `etherjar-domain`
  * `etherjar-contract`

where

* `etherjar-abi` - Smart contract [Application Binary Interface (ABI)](https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI)
* `etherjar-contract` - Methods to organize contract call  
* `etherjar-domain` - Core module contains pure domain logic (`Address`, `Block`, `Transaction`, `Wei` and so on)
* `etherjar-erc20` - Classes to simplify use of ERC-20 tokens 
* `etherjar-hex` - Hexadecimal encoding and encoding utils for `String`, `BigInteger`, byte arrays
* `etherjar-rlp` - Reading and writing RLP (Recursive Length Prefix) encoded data 
* `etherjar-rpc-api` - [JSON-RPC API](https://github.com/ethereum/wiki/wiki/JSON-RPC) generic implementation
* `etherjar-rpc-emerald` - gRPC transport, see [Emerald Dshackle](https://github.com/emeraldpay/dshackle)
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
      <url>https://dl.bintray.com/emerald/etherjar</url>
    </repository>
</repositories>

<dependency>
  <groupId>io.emeraldpay.etherjar</groupId>
  <artifactId>etherjar-rpc-http</artifactId>
  <version>0.9.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/emerald/etherjar" 
    }
}

dependencies {
    compile 'io.emerald.etherjar:etherjar-rpc-http:0.9.0'
}
```

## Examples

How to call `web3_clientVersion` low-level JSON-RPC API method:

```java
public class GetClientVersion {
    public static void main(String[] args)
            throws URISyntaxException, IOException, ExecutionException, InterruptedException {        
        try (RpcTransport transport = HttpRpcTransport.newBuilder().connectTo("http://127.0.0.1:8545").build()) {
            RpcClient client = new DefaultRpcClient(transport);
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

        try (RpcTransport transport = HttpRpcTransport.newBuilder().connectTo("http://127.0.0.1:8545").build()) {
            RpcClient client = new DefaultRpcClient(transport);
            Future<Wei> req = client.execute(Commands.eth().getGasPrice());

            System.out.println(String.format("Gas Price: %s Ether", req.get().toEthers(12)));
        }
    }
}
```

## Documentation

* [Reference Guide](./docs/index.md)

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/emeraldpay/etherjar/issues).

## Licence

Copyright 2020 EmeraldPay, Inc

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and limitations under the License.


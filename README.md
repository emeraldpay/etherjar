# EtherJar

[![Build Status](https://travis-ci.org/Infinitape/etherjar.svg?branch=master)](https://travis-ci.org/Infinitape/etherjar)
[![codecov](https://codecov.io/gh/Infinitape/etherjar/branch/master/graph/badge.svg)](https://codecov.io/gh/Infinitape/etherjar)
[![license](https://img.shields.io/github/license/infinitape/etherjar.svg?maxAge=2592000)](https://github.com/infinitape/etherjar/blob/master/LICENSE)

Framework agnostic modular Java 8 integration library for [Ethereum blockchain](https://www.ethereum.org),
including [Ethereum Classic (ETC)](https://ethereumclassic.github.io/).

## Architecture

* [ ] High-level [web3.js](https://github.com/ethereum/web3.js) like Java 8 API (_in progress_)
* [x] Low-level [JSON-RPC API](https://github.com/ethereum/wiki/wiki/JSON-RPC)
* [x] Transport data-level 
  * [ ] IPC (_not implemented yet_)
  * [x] HTTP

## Modules

Structure of dependencies between modules:

* `etherjar-web3`
  * `etherjar-abi`
    * `etherjar-hex`
  * `etherjar-core`
  * `etherjar-hex`
  * `etherjar-rpc`
    * `etherjar-hex`
    * `etherjar-trans`
  * `etherjar-http`
    * `etherjar-trans`
* `etherjar-gen`
  * `etherjar-abi`
    * `etherjar-hex`
  * `etherjar-core`

where

* `etherjar-abi` - [Application Binary Interface (ABI)](https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI)
* `etherjar-core` - Core domain model, including transactions, smart contracts and event filters
* `etherjar-gen` - Smart contracts stubs generator
* `etherjar-hex` - Hexadecimal encoding and encoding utils for `String`, `BigInteger`, byte arrays
* `etherjar-http` - HTTP transport layer for JSON-RPC API
* `etherjar-rpc` - [JSON-RPC API](https://github.com/ethereum/wiki/wiki/JSON-RPC) generic implementation
* `etherjar-trans` -Abstract transport layer for JSON-RPC API
* `etherjar-web3` - [Web3.js](https://github.com/ethereum/web3.js) like Java 8 API on top of JSON-RPC API

## Usage

### Maven

```xml
<dependency>
  <groupId>io.infinitape</groupId>
  <artifactId>etherjar-web3</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
compile 'io.infinitape:etherjar-web3:0.1.0'
```

## Examples

How to call `web3_clientVersion` JSON-RPC API method:

```java
package example;

import DefaultRpcTransport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args)
            throws URISyntaxException, IOException, ExecutionException, InterruptedException {

        try (RpcTransport trans =
                     new DefaultRpcTransport(new URI("http://127.0.0.1:8545"))) {

            Future<String> req =
                    trans.execute("web3_clientVersion", Collections.EMPTY_LIST, String.class);

            System.out.println(String.format("Client version: %s", req.get()));
        }
    }
}
```

## Licence

Apache 2.0

/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.infinitape.etherjar.rpc.ws;

import io.infinitape.etherjar.domain.TransactionId;
import io.infinitape.etherjar.rpc.json.BlockJson;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Base64;

/**
 * Ethereum Websocket client
 *
 * @author Igor Artamonov
 */
public class WebsocketClient implements Closeable {

    private static final EventLoopGroup group = new NioEventLoopGroup();
    private SocketApiHandler socketApiHandler;
    private URI upstream;
    private URI origin;

    private String username;
    private String password;

    /**
     *
     * @param upstream URI to a websocket server, ex. ws://localhost:8546
     * @param origin origin header, ex. http://localhost
     */
    public WebsocketClient(URI upstream, URI origin) {
        this.upstream = upstream;
        this.origin = origin;
    }

    /**
     * Setup Basic Auth for RPC calls
     *
     * @param username username
     * @param password password
     */
    public void setBasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Connects to a Websocket
     *
     * @throws IOException when failed to connect
     */
    public void connect() throws IOException {
        if (socketApiHandler != null) {
            throw new IllegalStateException("Websocket is already established");
        }
        Bootstrap b = new Bootstrap();

        String protocol = upstream.getScheme();
        if (!"ws".equals(protocol) && !"wss".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        HttpHeaders customHeaders = prepareHeaders();

        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            upstream, WebSocketVersion.V13, null, false,
            customHeaders,
            1280000);
        WebSocketClientProtocolHandler handler = new WebSocketClientProtocolHandler(handshaker);
        socketApiHandler = new SocketApiHandler();

        final String host = upstream.getHost();
        final int port;
        if (upstream.getPort() == -1 && protocol.equals("wss")) {
            port = 443;
        } else {
            port = upstream.getPort();
        }

        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel sch) throws Exception {
                    ChannelPipeline pipeline = sch.pipeline();
                    SslContext sslContext = prepareSsl();
                    if (sslContext != null) {
                        pipeline.addLast("tls", sslContext.newHandler(sch.alloc(), host, port));
                    }
                    pipeline.addLast("http-codec", new HttpClientCodec());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                    pipeline.addLast("ws-handler", handler);
                    pipeline.addLast(socketApiHandler);
                }
            });

        ChannelFuture future = b.connect(host, port).awaitUninterruptibly();
        if (future.isDone()) {
            if (!future.isSuccess()) {
                throw new IOException("Failed to connect to " + upstream, future.cause());
            }
        } else {
            throw new IOException("Failed to connect to " + upstream.toString());
        }
    }

    public SslContext prepareSsl() throws GeneralSecurityException, SSLException, KeyStoreException {
        if (!upstream.getScheme().equals("wss")) {
            return null;
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        SslContext sslCtx = SslContextBuilder.forClient()
//            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .trustManager(trustManagerFactory)
            .build();
        return sslCtx;
    }

    public HttpHeaders prepareHeaders() {
        HttpHeaders customHeaders = new DefaultHttpHeaders();
        customHeaders.add(HttpHeaderNames.ORIGIN.toString(), origin.toASCIIString());
        customHeaders.add(HttpHeaderNames.CONTENT_LENGTH.toString(), 0);
        if (username != null && password != null) {
            String tmp = username + ":" + password;
            final String base64password = Base64.getEncoder().encodeToString(tmp.getBytes());
            String buffer = "Basic " + base64password;
            customHeaders.add(HttpHeaderNames.AUTHORIZATION.toString(), buffer);
        }
        return customHeaders;
    }

    /**
     * Subscribe to new blocks
     *
     * @param listener handler of new blocks
     */
    public void onNewBlock(SubscriptionListener<BlockJson<TransactionId>> listener) {
        Subscription<BlockJson<TransactionId>> sub = new Subscription.Block();
        sub.addListener(listener);
        this.socketApiHandler.subscribe(sub);
    }

    @Override
    public void close() throws IOException {
        socketApiHandler.stop();
    }
}

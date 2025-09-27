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
package io.emeraldpay.etherjar.rpc.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter;
import io.emeraldpay.etherjar.rpc.json.BlockJson;
import io.emeraldpay.etherjar.rpc.json.TransactionRefJson;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Base64;

/**
 * Ethereum Websocket client
 *
 * @author Igor Artamonov
 */
@NullMarked
public class WebsocketClient implements Closeable {

    private static final EventLoopGroup group = new NioEventLoopGroup();
    @Nullable
    private SocketApiHandler socketApiHandler;
    private final URI upstream;
    private final URI origin;

    @Nullable
    private String username;
    @Nullable
    private String password;

    private final ObjectMapper objectMapper;

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     *
     * @param upstream URI to a websocket server, ex. ws://localhost:8546
     * @param origin origin header, ex. http://localhost
     */
    private WebsocketClient(URI upstream, URI origin) {
        this(upstream, origin, JacksonRpcConverter.createJsonMapper());
    }

    public WebsocketClient(URI upstream, URI origin, ObjectMapper objectMapper) {
        this.upstream = upstream;
        this.origin = origin;
        this.objectMapper = objectMapper;
    }

    /**
     * Setup Basic Auth for RPC calls
     *
     * @param username username
     * @param password password
     */
    protected void setBasicAuth(String username, String password) {
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

    protected SslContext prepareSsl() throws GeneralSecurityException, SSLException, KeyStoreException {
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

    protected HttpHeaders prepareHeaders() {
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
    public void onNewBlock(SubscriptionListener<BlockJson<TransactionRefJson>> listener) {
        Subscription<BlockJson<TransactionRefJson>> sub = new Subscription.Block(objectMapper);
        sub.addListener(listener);
        if (this.socketApiHandler != null) {
            this.socketApiHandler.subscribe(sub);
        }
    }

    @Override
    public void close() throws IOException {
        if (socketApiHandler != null) {
            socketApiHandler.stop();
        }
    }

    /**
     * Build a configuration for the WebsocketClient
     */
    public static class Builder {

        @Nullable
        private URI address;
        @Nullable
        private URI origin;

        @Nullable
        private String username;
        @Nullable
        private String password;

        @Nullable
        private ObjectMapper objectMapper;

        /**
         *
         * @param address address of a Websocket endpoint, e.g. ws://127.0.0.1:8546
         * @return builder
         */
        public Builder connectTo(URI address) {
            this.address = address;
            return this;
        }

        /**
         * Optional original of the current client, if a Websocket server requires it for connection
         *
         * @param origin origin to of the current client, e.g. http://localhost
         * @return builder
         */
        public Builder origin(URI origin) {
            this.origin = origin;
            return this;
        }

        /**
         * Setup Basic Auth for RPC calls
         *
         * @param username username
         * @param password password
         * @return builder
         */
        public Builder basicAuth(String username, String password) {
            if (username == null || username.length() == 0) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            if (password == null || password.length() == 0) {
                throw new IllegalArgumentException("Password cannot be null or empty");
            }
            this.username = username;
            this.password = password;
            return this;
        }

        /**
         *
         * @return client
         */
        public WebsocketClient build() {
            if (address == null) {
                try {
                    address = new URI("ws://localhost:8546");
                } catch (URISyntaxException e) { }
            }
            if (origin == null) {
                try {
                    origin = new URI("http://localhost");
                } catch (URISyntaxException e) { }
            }
            if (objectMapper == null) {
                objectMapper = JacksonRpcConverter.createJsonMapper();
            }
            WebsocketClient client = new WebsocketClient(address, origin, objectMapper);
            if (username != null && password != null) {
                client.setBasicAuth(username, password);
            }
            return client;
        }
    }
}

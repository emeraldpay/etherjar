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

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

/**
 * Ethereum Websocket client
 *
 * @author Igor Artamonov
 */
public class WebsocketClient implements Closeable {

    private static final EventLoopGroup group = new NioEventLoopGroup();
    private SocketApiHandler socketApiHandler;

    /**
     * Connects to a Websocket
     *
     * @param upstream URI to a websocket server, ex. ws://localhost:8546
     * @param origin origin header, ex. http://localhost
     * @throws IOException when failed to connect
     */
    public void connect(URI upstream, URI origin) throws IOException {
        if (socketApiHandler != null) {
            throw new IllegalStateException("Websocket is already established");
        }
        Bootstrap b = new Bootstrap();

        String protocol = upstream.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        HttpHeaders customHeaders = new DefaultHttpHeaders();
        customHeaders.add(HttpHeaderNames.ORIGIN.toString(), origin.toASCIIString());
        customHeaders.add(HttpHeaderNames.CONTENT_LENGTH.toString(), 0);

        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            upstream, WebSocketVersion.V13, null, false,
            customHeaders,
            1280000);
        WebSocketClientProtocolHandler handler = new WebSocketClientProtocolHandler(handshaker);
        socketApiHandler = new SocketApiHandler();

        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel sch) throws Exception {
                    ChannelPipeline pipeline = sch.pipeline();
                    pipeline.addLast("http-codec", new HttpClientCodec());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                    pipeline.addLast("ws-handler", handler);
                    pipeline.addLast(socketApiHandler);
                }
            });

        ChannelFuture future = b.connect(upstream.getHost(), upstream.getPort()).awaitUninterruptibly();
        if (future.isDone()) {
            if (!future.isSuccess()) {
                throw new IOException("Failed to connect to " + upstream, future.cause());
            }
        } else {
            throw new IOException("Failed to connect to " + upstream.toString());
        }
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

package io.github.kimmking.gateway.outbound.netty4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.URI;

public class NettyHttpClient {

    // 单独作为client来访问服务端
    public void connect(String host, int port) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
//            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {

                    System.out.println("正在连接中...");

                    // 版本2 start
                    // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                    ch.pipeline().addLast(new HttpResponseDecoder());
                    // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                    ch.pipeline().addLast(new HttpRequestEncoder());
                    // end

                    ch.pipeline().addLast(new NettyHttpClientOutboundHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            if (f.isSuccess()) {

                System.out.println("成功连接服务器，host：{ " + host +" }，port：{ " + port + " }");
            }

            // 版本1
            // 最简单不用选择客户端交互的方式
            // 服务端直接返回给客户端 (ChannelHandlerContext) ctx.writeAndFlush(Unpooled.copiedBuffer(“返回内容”.getBytes()));
//            f.channel().writeAndFlush(Unpooled.copiedBuffer("我是客户端请求1$_".getBytes(CharsetUtil.UTF_8)));
//

            // 版本2
            // 服务端和客户端同时指定通信方式 http
            URI uri = new URI("http://127.0.0.1:8888");
            String msg = "我是客户端请求1$_";

            DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString(),
                    Unpooled.wrappedBuffer(msg.getBytes()));
            // 构建http请求
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION,
                    HttpHeaderNames.CONNECTION);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                    request.content().readableBytes());
            // 发送http请求
            f.channel().write(request);

            f.channel().flush();
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    // 整合进netty服务端里用netty client访问
    public void connect(ChannelHandlerContext ctx, String url) throws Exception {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
//            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_KEEPALIVE, true);

            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

                    ch.pipeline()
                            .addLast(new HttpResponseDecoder()) // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                            .addLast(new HttpRequestEncoder()) // 客户端发送的是httpRequest，所以要使用HttpRequestEncoder进行编码
                            .addLast(new HttpObjectAggregator(1024 * 10 * 1024))
                            .addLast(new NettyHttpClientOutboundHandler(ctx)); // 坑点 、、、、、、、
//                            .addLast(new LoggingHandler(LogLevel.INFO));
                }
            });

            URI uri = new URI(url);
            ChannelFuture f = b.connect(uri.getHost(), uri.getPort()).sync();
            if (f.isSuccess()) {

                System.out.println("成功连接服务器，host：{ " + uri.getHost() +" }，port：{ " + uri.getPort() + " }");
            }

            DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString(),
                    Unpooled.wrappedBuffer("请求头！@#".getBytes()));
            // 构建 HTTP
            request.headers().set(HttpHeaderNames.HOST, uri.getHost());
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

            f.channel().writeAndFlush(request);
            f.channel().closeFuture().sync();
        } finally {

            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        NettyHttpClient client = new NettyHttpClient();
        client.connect("127.0.0.1", 8888);
    }
}
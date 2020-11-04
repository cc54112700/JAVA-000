package io.github.kimmking.gateway.inbound;

import io.github.kimmking.gateway.outbound.netty4.NettyHttpClientOutboundHandler;
import io.github.kimmking.gateway.utils.ByteBufToBytes;
import io.github.kimmking.gateway.outbound.okhttp.OkhttpOutboundHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private final String proxyServer;

//    private ByteBufToBytes reader;

    // 整合 Okhttp
//    private OkhttpOutboundHandler handler;
    // 整合 HttpClient
//    private HttpClientOutboundHandler handler;

    // 整合 netty client
    private NettyHttpClientOutboundHandler handler;

//    private HttpOutboundHandler handler;

    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
//        handler = new HttpOutboundHandler(this.proxyServer);
//        handler = new HttpClientOutboundHandler(this.proxyServer);
//        handler = new OkhttpOutboundHandler(this.proxyServer);

        handler = new NettyHttpClientOutboundHandler(this.proxyServer);
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {

            // 整合 Okhttp HttpClient 都是浏览器直接访问服务端
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            handler.handle(fullRequest, ctx);

            // 客户端访问 两种方法
            // 1. 直接回写给客户端相应
//            handlerSimple(msg, ctx);

            // 2.客户端以http访问
//            if (msg instanceof HttpRequest) {
//                HttpRequest request = (HttpRequest) msg;
//                if (HttpUtil.isContentLengthSet(request)) {
//                    reader = new ByteBufToBytes(
//                            (int) HttpUtil.getContentLength(request));
//                }
//            }
//
//            if (msg instanceof HttpContent) {
//                HttpContent httpContent = (HttpContent) msg;
//                ByteBuf content = httpContent.content();
//                reader.reading(content);
//                content.release();
//                if (reader.isEnd()) {
//                    String resultStr = new String(reader.readFull());
//                    System.out.println("Client said:" + resultStr);
//                    FullHttpResponse response = new DefaultFullHttpResponse(
//                            HTTP_1_1, OK, Unpooled.wrappedBuffer("I am ok"
//                            .getBytes()));
//                    response.headers().set(CONTENT_TYPE, "text/plain");
//                    response.headers().set(CONTENT_LENGTH,
//                            response.content().readableBytes());
//                    response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//                    ctx.write(response);
//                    ctx.flush();
//                }
//            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            // io.netty.util.IllegalReferenceCountException: refCnt: 0, decrement: 1
//            ReferenceCountUtil.release(msg);
        }
    }

    private void handlerSimple(Object msg, ChannelHandlerContext ctx) {

        ByteBuf bb = (ByteBuf)msg;
        // 创建一个和buf同等长度的字节数组
        byte[] reqByte = new byte[bb.readableBytes()];
        // 将buf中的数据读取到数组中
        bb.readBytes(reqByte);
        String reqStr = new String(reqByte, CharsetUtil.UTF_8);
        System.out.println("server 接收到客户端的请求： " + reqStr);

        String respStr = new StringBuilder("来自服务器的响应").append(reqStr).append("$_").toString();

        // 返回给客户端响应                                                                                                                                                       和客户端链接中断即短连接，当信息返回给客户端后中断
        ctx.writeAndFlush(Unpooled.copiedBuffer(respStr.getBytes()));//.addListener(ChannelFutureListener.CLOSE);

    }

    private void handlerTest(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        FullHttpResponse response = null;
        try {
            String value = "hello,kimmking";
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(value.getBytes("UTF-8")));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", response.content().readableBytes());

        } catch (Exception e) {
            logger.error("处理测试接口出错", e);
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}

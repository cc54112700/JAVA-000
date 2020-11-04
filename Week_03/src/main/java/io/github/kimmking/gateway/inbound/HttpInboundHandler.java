package io.github.kimmking.gateway.inbound;

import io.github.kimmking.gateway.filter.NettyHttpRequestFilter;
import io.github.kimmking.gateway.outbound.netty4.NettyHttpClientOutboundHandler;
import io.github.kimmking.gateway.router.NettyHttpRouter;
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

import java.util.Arrays;
import java.util.List;

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


    private NettyHttpRequestFilter filter = null;

//    private HttpOutboundHandler handler;

    // 处理路由 暂时写在这里 后期优化到开始程序
    private static List<String> endpoints = Arrays.asList("http://localhost:8288","http://localhost:8288","http://localhost:8288");


    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
//        handler = new HttpOutboundHandler(this.proxyServer);
//        handler = new HttpClientOutboundHandler(this.proxyServer);
//        handler = new OkhttpOutboundHandler(this.proxyServer);


//        handler = new NettyHttpClientOutboundHandler(this.proxyServer);
        // 处理路由 暂时写在这里 后期优化到开始程序
        handler = new NettyHttpClientOutboundHandler(new NettyHttpRouter().route(endpoints));
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

            // 处理filter
            if (null == filter) {
                filter = new NettyHttpRequestFilter();
            }
            filter.filter(fullRequest, ctx);

            handler.handle(fullRequest, ctx);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            // io.netty.util.IllegalReferenceCountException: refCnt: 0, decrement: 1
            ReferenceCountUtil.release(msg);
        }
    }

    // 1. 直接回写给客户端相应
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

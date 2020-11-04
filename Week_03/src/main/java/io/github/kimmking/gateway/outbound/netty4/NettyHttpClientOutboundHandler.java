package io.github.kimmking.gateway.outbound.netty4;

import io.github.kimmking.gateway.utils.ByteBufToBytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {

    // 版本2的通用方法
    private ByteBufToBytes reader;

    private NettyHttpClient client;
    private String backendUrl;

    private ChannelHandlerContext selfCtx;

    public NettyHttpClientOutboundHandler(String backendUrl) {

        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
        client = new NettyHttpClient();
    }

    public NettyHttpClientOutboundHandler() {

    }

    public NettyHttpClientOutboundHandler(ChannelHandlerContext ctx) {

        this.selfCtx = ctx;
    }

    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) throws Exception {
        String url = backendUrl + fullRequest.uri();
        client.connect(ctx, url);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            System.out.println("CONTENT_TYPE:"
                    + response.headers().get(HttpHeaderNames.CONTENT_TYPE));
            if (HttpUtil.isContentLengthSet(response)) {
                reader = new ByteBufToBytes(
                        (int) HttpUtil.getContentLength(response));
            }
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();

            FullHttpResponse response = null;

            try {

                response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content));
                response.headers().set("Content-Type", "application/json");
                response.headers().setInt("Content-Length", response.content().readableBytes());
            } catch (Exception e) {
                e.printStackTrace();
                response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
                exceptionCaught(selfCtx, e);
            } finally {

                if (selfCtx.channel().isActive()) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                    selfCtx.writeAndFlush(response);
                } else {
                    selfCtx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }






    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client 读取数据出现异常");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端读取数据完毕");
    }


}
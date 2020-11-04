package io.github.kimmking.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class NettyHttpRequestFilter implements HttpRequestFilter {

    public NettyHttpRequestFilter() {

        // 单例模式以后再优化
    }

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {

        fullRequest.headers().add("nio-chu", "cc54112700");
    }
}

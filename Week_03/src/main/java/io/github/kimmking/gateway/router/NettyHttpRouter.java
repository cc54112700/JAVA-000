package io.github.kimmking.gateway.router;

import java.util.List;
import java.util.Random;

public class NettyHttpRouter implements HttpEndpointRouter {

    public NettyHttpRouter() {

        // 构造方法是否引入 fullhttprequest 后期优化扩展
    }

    @Override
    public String route(List<String> endpoints) {

        // 先写个随机算法。。。 以后再扩展
        return endpoints.get(new Random().nextInt(endpoints.size()));
    }
}

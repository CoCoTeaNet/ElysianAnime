package net.cocotea.elysiananime.properties;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Data
@Component
public class ProxyProp {

    @Inject("${elysiananime.proxy.enable}")
    private String enable;

    @Inject("${elysiananime.proxy.host}")
    private String host;

    @Inject("${elysiananime.proxy.port}")
    private Integer port;

    private Proxy proxy;

    public Proxy getProxy() {
        if (proxy == null) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }
        return proxy;
    }

}

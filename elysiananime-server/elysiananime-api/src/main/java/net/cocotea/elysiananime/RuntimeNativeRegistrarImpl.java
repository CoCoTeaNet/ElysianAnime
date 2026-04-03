package net.cocotea.elysiananime;

import org.noear.solon.annotation.Component;
import org.noear.solon.aot.RuntimeNativeMetadata;
import org.noear.solon.aot.RuntimeNativeRegistrar;
import org.noear.solon.core.AppContext;

@Component
public class RuntimeNativeRegistrarImpl implements RuntimeNativeRegistrar {
    @Override
    public void register(AppContext context, RuntimeNativeMetadata metadata) {
        metadata.registerResourceInclude("native-config/jni-config.json");
        metadata.registerResourceInclude("native-config/predefined-classes-config.json");
        metadata.registerResourceInclude("native-config/proxy-config.json");
        metadata.registerResourceInclude("native-config/reflect-config.json");
        metadata.registerResourceInclude("native-config/resource-config.json");
        metadata.registerResourceInclude("native-config/serialization-config.json");
    }
}

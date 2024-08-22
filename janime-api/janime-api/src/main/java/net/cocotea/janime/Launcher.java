package net.cocotea.janime;

import net.cocotea.janime.common.constant.GlobalConst;
import net.cocotea.janime.properties.DefaultProp;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.core.AppContext;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CoCoTea
 * @since v1.2.7
 */
@SolonMain
@EnableScheduling
@Import(scanPackages = {"net.cocotea.janime"})
public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        SolonApp app = Solon.start(Launcher.class, args);
        AppContext context = app.context();

        DefaultProp defaultProp = context.getBean("defaultProp");
        logger.warn("强密码：{}, 权限缓存状态：{}", defaultProp.getStrongPassword(), defaultProp.getPermissionCache());

        GlobalConst.START_TIME = System.currentTimeMillis();
    }
}

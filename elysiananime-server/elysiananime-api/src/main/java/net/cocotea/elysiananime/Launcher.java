package net.cocotea.elysiananime;

import net.cocotea.elysiananime.common.constant.GlobalConst;
import net.cocotea.elysiananime.properties.DefaultProp;
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
@Import(scanPackages = {"net.cocotea.elysiananime"})
public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        SolonApp app = Solon.start(Launcher.class, args);
        AppContext context = app.context();

        DefaultProp defaultProp = context.getBean("defaultProp");
        log.warn("强密码：{}, 权限缓存状态：{}", defaultProp.getStrongPassword(), defaultProp.getPermissionCache());

        GlobalConst.START_TIME = System.currentTimeMillis();

        log.info(""" 
                 ________  __                    _                        _                 _                     \s
                |_   __  |[  |                  (_)                      / \\               (_)                    \s
                  | |_ \\_| | |   _   __  .--.   __   ,--.   _ .--.      / _ \\     _ .--.   __   _ .--..--.  .---. \s
                  |  _| _  | |  [ \\ [  ]( (`\\] [  | `'_\\ : [ `.-. |    / ___ \\   [ `.-. | [  | [ `.-. .-. |/ /__\\\\\s
                 _| |__/ | | |   \\ '/ /  `'.'.  | | // | |, | | | |  _/ /   \\ \\_  | | | |  | |  | | | | | || \\__.,\s
                |________|[___][\\_:  /  [\\__) )[___]\\'-;__/[___||__]|____| |____|[___||__][___][___||__||__]'.__.'\s
                                \\__.'                                                                             \s
                                                         \s
                start success!""");
    }
}

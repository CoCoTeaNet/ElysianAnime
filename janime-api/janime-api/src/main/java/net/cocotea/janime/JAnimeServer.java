package net.cocotea.janime;

import com.sagframe.sagacity.sqltoy.plus.EnableSqlToyPlus;
import net.cocotea.janime.common.constant.GlobalConst;
import net.cocotea.janime.properties.DefaultProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableSqlToyPlus
public class JAnimeServer {

    private static final Logger logger = LoggerFactory.getLogger(JAnimeServer.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JAnimeServer.class, args);
        DefaultProp defaultProp = (DefaultProp) context.getBean("defaultProp");
        logger.info("强密码：{}, 权限缓存状态：{}", defaultProp.getStrongPassword(), defaultProp.getPermissionCache());
        GlobalConst.START_TIME = System.currentTimeMillis();
    }

    @RestController
    public static class IndexController {
        @RequestMapping("/")
        public String index() {
            return "Hello JAnime~";
        }
    }

}

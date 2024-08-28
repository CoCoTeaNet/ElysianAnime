package net.cocotea.elysiananime.config;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.redisx.RedisClient;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;

@Configuration
public class DbConfig {

    //typed=true，表示默认数据源。@Db 可不带名字注入
    @Bean(name = "db1", typed = true)
    public DataSource db1(@Inject("${myapp.db1}") HikariDataSource ds) {
        return ds;
    }

    // /
    // 上方是数据库配置
    // 下方是redis配置
    // /

    @Bean
    public RedisClient redisClient(@Inject("${myapp.rd1}") RedisClient client) {
        return client;
    }
}

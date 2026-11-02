package net.cocotea.elysiananime.test;

import net.cocotea.elysiananime.Launcher;
import org.junit.jupiter.api.BeforeAll;
import org.noear.solon.Solon;
import org.sagacity.sqltoy.dao.LightDao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 集成测试的公共上下文：启动 Solon 容器，让测试能拿到真实的 Bean（LightDao 等）。
 * <p>
 * 依赖本机 MySQL 与 Redis，连不上时整类测试跳过，不让 CI 因环境缺失而失败。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public abstract class SolonTestSupport {

    /**
     * 测试用端口，避免与本机运行的服务冲突
     */
    private static final String TEST_PORT = "18099";

    private static boolean started = false;

    private static boolean available = false;

    /**
     * 整个 JVM 只启动一次：二次 Solon.start 出来的上下文注入是坏的。
     * 收尾交给 shutdown hook，测试类之间不停容器。
     */
    @BeforeAll
    static void startContext() {
        if (started) {
            assumeTrue(available, "Solon 上下文启动失败，跳过集成测试");
            return;
        }
        started = true;
        try {
            Solon.start(Launcher.class, new String[]{"--server.port=" + TEST_PORT}, null);
            available = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // 不能用 Solon.stop()：它会 System.exit(1) 把 surefire 的 fork 进程打死，构建直接失败
                try {
                    Solon.stopBlock();
                } catch (Throwable ignored) {
                    // 关闭中
                }
            }, "solon-test-shutdown"));
        } catch (Throwable e) {
            available = false;
        }
        assumeTrue(available, "Solon 上下文启动失败，跳过集成测试");
    }

    /**
     * 数据库连接，连不上返回 null
     */
    protected static Connection connection() {
        try {
            String url = Solon.cfg().get("myapp.db1.jdbcUrl");
            String username = Solon.cfg().get("myapp.db1.username");
            String password = Solon.cfg().get("myapp.db1.password");
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 从 Bean 上取出注入的 LightDao。
     * <p>
     * sqltoy 的 Dao 是按注入点生成的代理，容器里没有对应的 Bean，只能从实例上反射取。
     */
    protected static LightDao lightDaoOf(Object bean) {
        Class<?> clazz = bean.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("lightDao");
                field.setAccessible(true);
                return (LightDao) field.get(bean);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("取不到 LightDao：" + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("取不到 LightDao：字段不存在");
    }

    /**
     * 反射写字段（测试里手工装配服务用）
     */
    protected static void setField(Object target, String name, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("写字段失败：" + name, e);
            }
        }
        throw new IllegalStateException("字段不存在：" + name);
    }
}

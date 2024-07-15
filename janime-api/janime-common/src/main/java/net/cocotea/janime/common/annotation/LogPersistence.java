package net.cocotea.janime.common.annotation;

import java.lang.annotation.*;

/**
 * 在Controller层使用该注解可以让操作日志持久化
 *
 * @author CoCoTea
 * @version 2.0.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface LogPersistence {

    /**
     * 日志类型：1登录 2操作
     */
    int logType() default 2;

    /**
     * 描述信息
     */
    String desc() default "";

}

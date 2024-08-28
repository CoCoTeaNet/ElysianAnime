package net.cocotea.elysiananime.util;

import cn.dev33.satoken.stp.StpUtil;

import java.math.BigInteger;

/**
 * 登录工具类
 *
 * @author CoCoTea
 * @version 2.0.0
 */
public class LoginUtils {

    /**
     * 获取登录ID
     *
     * @return 如果未登录返回null
     */
    public static BigInteger loginId() {
        BigInteger id = null;
        try {
            Object loginId = StpUtil.getLoginId();
            id = BigInteger.valueOf(Long.parseLong(loginId.toString()));
        } catch (Exception ignored) {
        }
        return id;
    }

    /**
     * 获取登录ID（抛出异常）
     *
     * @return 登录ID
     */
    public static BigInteger loginIdEx() {
        Object loginId = StpUtil.getLoginId();
        if (loginId == null) {
            return null;
        }
        return BigInteger.valueOf(Long.parseLong(loginId.toString()));
    }

    /**
     * 解析登录ID
     *
     * @param loginId 登录id
     * @return 登录ID
     */
    public static BigInteger parse(Object loginId) {
        if (loginId == null) {
            return null;
        }
        return BigInteger.valueOf(Long.parseLong(loginId.toString()));
    }

}

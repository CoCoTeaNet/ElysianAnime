package net.cocotea.janime.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.janime.common.constant.CharConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 规则工具类
 *
 * @author CoCoTea
 * @version 2.0.0
 */
public class RuleUtils {
    private static final Logger logger = LoggerFactory.getLogger(RuleUtils.class);

    /**
     * 获取重命名后的名称
     *
     * @param name 原始名称
     * @return 重命名名称
     */
    public static String rename(String name, int index, String suffix) {
        // 文件后缀
        List<String> list = ReUtil.findAll("[0-9]+", name, 0);
        String fileName;
        if (!list.isEmpty()) {
            fileName = list.get(index);
        } else {
            // 匹配不到默认为第一集
            fileName = String.valueOf(1);
        }
        return fileName.concat(CharConst.POINT).concat(suffix);
    }

    /**
     * 校验标识资源
     *
     * @return 是返回true
     */
    public static boolean isRemarkRes(String title, String mark) {
        List<String> list = ReUtil.findAll(mark, title, 0);
        return !list.isEmpty();
    }

    /**
     * 是排除的资源
     *
     * @return 是返回true
     */
    public static boolean isExcludeRes(String title, String mark) {
        if (StrUtil.isBlank(mark)) {
            logger.error("utils[isExcludeRes]mark is blank");
            return false;
        }
        String[] split = mark.split(CharConst.COMMA);
        List<String> list;
        for (String s : split) {
            list = ReUtil.findAll(s, title, 0);
            if (!list.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}

package net.cocotea.janime.common.util;

import java.util.List;

/**
 * 字符串拼接工具
 *
 * @author CoCoTea
 * @since v1
 */
public class StrcUtis {

    /**
     * 拼接字符串，比如 aaaa|bbb
     * @param list 列表
     * @param charStr 字符，例如|
     * @return aaaa|bbb
     */
    public static String addChar(List<String> list, String charStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(charStr);
            }
        }
        return sb.toString();
    }

}

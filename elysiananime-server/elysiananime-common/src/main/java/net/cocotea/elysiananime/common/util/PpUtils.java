package net.cocotea.elysiananime.common.util;

import net.cocotea.elysiananime.common.model.BusinessException;

/**
 * 参数解析工具
 *
 * @author CoCoTea
 * @since 2022-12-30 16:01:28
 */
public class PpUtils {

    /**
     * 解析时间范围
     *
     * @param times 时间范围格式：yyyy-MM-dd hh:mm:ss~yyyy-MM-dd hh:mm:ss
     * @param split 分隔符，例如 ~
     * @return 时间数组
     * @throws BusinessException 业务异常抛出
     */
    public String[] timeRange(String times, String split) throws BusinessException {
        String[] timeArr = times.split(split);
        if (timeArr.length != 2) {
            throw new BusinessException("util[PpUtils]解析参数异常");
        }
        return timeArr;
    }

    /**
     * 解析时间范围，默认分隔符为波浪号 ~
     *
     * @param times 时间范围格式：yyyy-MM-dd hh:mm:ss~yyyy-MM-dd hh:mm:ss
     * @return 时间数组
     * @throws BusinessException 业务异常抛出
     */
    public String[] timeRange(String times) throws BusinessException {
        return timeRange(times, "~");
    }

}

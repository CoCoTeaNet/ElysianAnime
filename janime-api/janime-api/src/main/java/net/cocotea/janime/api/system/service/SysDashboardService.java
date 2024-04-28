package net.cocotea.janime.api.system.service;

import net.cocotea.janime.api.system.model.vo.SystemInfoVO;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * 仪表板服务
 * @date 2022-1-31 18:12:12
 * @author jwss
 */
public interface SysDashboardService {
    /**
     * 获取系统统计数量
     * @return 数量集合
     */
    List<Map<String, Object>> getCount();

    /**
     * 获取系统信息
     * @return 系统信息
     * @throws  UnknownHostException 未知host异常
     */
    SystemInfoVO getSystemInfo();
}

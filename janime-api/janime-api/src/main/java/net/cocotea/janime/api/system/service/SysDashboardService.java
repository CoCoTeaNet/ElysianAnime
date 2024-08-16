package net.cocotea.janime.api.system.service;

import net.cocotea.janime.api.system.model.vo.SysOverviewVO;
import net.cocotea.janime.api.system.model.vo.SystemInfoVO;

import java.net.UnknownHostException;
import java.util.List;

/**
 * 仪表板服务
 * @date 2022-1-31 18:12:12
 * @author CoCoTea
 */
public interface SysDashboardService {
    /**
     * 获取系统统计数量
     * @return 数量集合
     */
    List<SysOverviewVO> getCount();

    /**
     * 获取系统信息
     * @return 系统信息
     * @throws  UnknownHostException 未知host异常
     */
    SystemInfoVO getSystemInfo();
}

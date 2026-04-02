package net.cocotea.elysiananime.api.system.service;

import net.cocotea.elysiananime.api.system.model.vo.SysOverviewVO;

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
}

package net.cocotea.janime.api.system.service;

import net.cocotea.janime.api.system.model.dto.SysLogAddDTO;
import net.cocotea.janime.api.system.model.dto.SysLogPageDTO;
import net.cocotea.janime.api.system.model.dto.SysLogUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysLogVO;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.service.BaseService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
public interface SysLogService extends BaseService<ApiPage<SysLogVO>, SysLogPageDTO, SysLogAddDTO, SysLogUpdateDTO> {
    /**
     * 通过日志类型保存
     *
     * @param logType {@link net.cocotea.janime.common.enums.LogTypeEnum}
     * @param request {@link HttpServletRequest}
     * @throws BusinessException 异常抛出
     */
    void saveByLogType(Integer logType, HttpServletRequest request) throws BusinessException;

    /**
     * 错误日志保存
     *
     * @param request {@link HttpServletRequest}
     */
    void saveErrorLog(HttpServletRequest request);
}

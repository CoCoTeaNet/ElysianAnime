package net.cocotea.elysiananime.api.system.service;

import net.cocotea.elysiananime.api.system.model.dto.SysLogAddDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysLogPageDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysLogUpdateDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysLogVO;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.service.BaseService;
import org.noear.solon.core.handle.Context;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
public interface SysLogService extends BaseService<ApiPage<SysLogVO>, SysLogPageDTO, SysLogAddDTO, SysLogUpdateDTO> {
    /**
     * 通过日志类型保存
     *
     * @param logType {@link net.cocotea.elysiananime.common.enums.LogTypeEnum}
     * @param request {@link HttpServletRequest}
     * @throws BusinessException 异常抛出
     */
    void saveByLogType(Integer logType, Context context) throws BusinessException;

    /**
     * 错误日志保存
     *
     * @param request {@link HttpServletRequest}
     */
    void saveErrorLog(Context context);
}

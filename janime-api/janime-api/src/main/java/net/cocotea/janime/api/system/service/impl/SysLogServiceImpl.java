package net.cocotea.janime.api.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.system.model.dto.SysLogAddDTO;
import net.cocotea.janime.api.system.model.dto.SysLogPageDTO;
import net.cocotea.janime.api.system.model.dto.SysLogUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysLog;
import net.cocotea.janime.api.system.model.vo.SysLogVO;
import net.cocotea.janime.api.system.service.SysLogService;
import net.cocotea.janime.common.enums.LogTypeEnum;
import net.cocotea.janime.common.enums.LogStatusEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.util.IpUtils;
import net.cocotea.janime.properties.DefaultProp;
import net.cocotea.janime.util.LoginUtils;
import org.sagacity.sqltoy.model.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
public class SysLogServiceImpl implements SysLogService {
    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private DefaultProp defaultProp;

    @Override
    public boolean add(SysLogAddDTO sysLogAddDTO) throws BusinessException {
        SysLog sysOperationLog = BeanUtil.toBean(sysLogAddDTO, SysLog.class);
        Object save = sqlToyHelperDao.save(sysOperationLog);
        return save != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) throws BusinessException {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(SysLogUpdateDTO param) throws BusinessException {
        return false;
    }

    @Override
    public boolean delete(BigInteger id) {
        return sqlToyHelperDao.deleteByIds(SysLog.class, id) > 0;
    }

    @Override
    public ApiPage<SysLogVO> listByPage(SysLogPageDTO pageDTO) throws BusinessException {
        String operator = pageDTO.getSysLog().getOperator();
        Map<String, Object> sysLogMap = BeanUtil.beanToMap(pageDTO.getSysLog());
        sysLogMap.put("operator", operator);
        Page<SysLogVO> page = sqlToyHelperDao.findPageBySql(pageDTO, "sys_log_JOIN_findList", sysLogMap, SysLogVO.class);
        return ApiPage.rest(page, SysLogVO.class);
    }

    @Async
    @Override
    public void saveByLogType(Integer logType, HttpServletRequest request) throws BusinessException {
        if (defaultProp.getSaveLog()) {
            SysLogAddDTO sysLogAddDTO = new SysLogAddDTO()
                    .setApiPath(request.getRequestURI())
                    .setIpAddress(IpUtils.getIp(request))
                    .setLogType(logType)
                    .setRequestWay(request.getMethod())
                    .setOperator(LoginUtils.loginId())
                    .setLogStatus(LogStatusEnum.SUCCESS.getCode());
            add(sysLogAddDTO);
        }
    }

    @Override
    public void saveErrorLog(HttpServletRequest request) {
        if (StpUtil.isLogin() && defaultProp.getSaveLog()) {
            SysLogAddDTO sysLogAddDTO = new SysLogAddDTO()
                    .setApiPath(request.getRequestURI())
                    .setIpAddress(IpUtils.getIp(request))
                    .setRequestWay(request.getMethod())
                    .setOperator(LoginUtils.loginId())
                    .setLogType(LogTypeEnum.OPERATION.getCode())
                    .setLogStatus(LogStatusEnum.ERROR.getCode());
            try {
                add(sysLogAddDTO);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

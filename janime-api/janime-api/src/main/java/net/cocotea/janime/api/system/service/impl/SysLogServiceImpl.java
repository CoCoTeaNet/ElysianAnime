package net.cocotea.janime.api.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import com.sagframe.sagacity.sqltoy.plus.multi.MultiWrapper;
import com.sagframe.sagacity.sqltoy.plus.multi.model.LambdaColumn;
import net.cocotea.janime.api.system.model.dto.SysLogAddDTO;
import net.cocotea.janime.api.system.model.dto.SysLogPageDTO;
import net.cocotea.janime.api.system.model.dto.SysLogUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysLog;
import net.cocotea.janime.api.system.model.po.SysUser;
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

@Service
public class SysLogServiceImpl implements SysLogService {
    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private DefaultProp defaultProp;

    @Override
    public boolean add(SysLogAddDTO param) throws BusinessException {
        SysLog sysOperationLog = Convert.convert(SysLog.class, param);
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
        MultiWrapper multiWrapper = Wrappers.lambdaMultiWrapper(SysLogVO.class)
                .select(
                        LambdaColumn.of(SysLog::getId), LambdaColumn.of(SysLog::getIpAddress),
                        LambdaColumn.of(SysLog::getRequestWay), LambdaColumn.of(SysLog::getLogStatus),
                        LambdaColumn.of(SysLog::getLogType), LambdaColumn.of(SysLog::getCreateTime),
                        LambdaColumn.of(SysUser::getUsername), LambdaColumn.of(SysUser::getNickname)
                )
                .from(SysLog.class)
                .leftJoin(SysUser.class).on().eq(SysLog::getOperator, SysUser::getId)
                .where()
                .eq(SysLog::getId, pageDTO.getSysLog().getId())
                .like(SysUser::getUsername, pageDTO.getSysLog().getOperator())
                .like(SysUser::getNickname, pageDTO.getSysLog().getOperator());
        Page<SysLogVO> page = sqlToyHelperDao.findPage(multiWrapper, new Page<>(pageDTO.getPageSize(), pageDTO.getPageNo()));
        return ApiPage.rest(page, SysLogVO.class);
    }

    @Async
    @Override
    public void saveByLogType(Integer logType, HttpServletRequest request) throws BusinessException {
        if (defaultProp.getSaveLog()) {
            SysLogAddDTO sysLogAddDTO = new SysLogAddDTO();
            sysLogAddDTO.setIpAddress(IpUtils.getIp(request));
            sysLogAddDTO.setLogType(logType);
            sysLogAddDTO.setRequestWay(request.getMethod());
            sysLogAddDTO.setOperator(LoginUtils.loginId());
            sysLogAddDTO.setLogStatus(LogStatusEnum.SUCCESS.getCode());
            add(sysLogAddDTO);
        }
    }

    @Override
    public void saveErrorLog(HttpServletRequest request) {
        if (StpUtil.isLogin() && defaultProp.getSaveLog()) {
            SysLogAddDTO sysLogAddDTO = new SysLogAddDTO();
            sysLogAddDTO.setIpAddress(IpUtils.getIp(request));
            sysLogAddDTO.setLogType(LogTypeEnum.OPERATION.getCode());
            sysLogAddDTO.setRequestWay(request.getMethod());
            sysLogAddDTO.setOperator(LoginUtils.loginId());
            sysLogAddDTO.setLogStatus(LogStatusEnum.ERROR.getCode());
            try {
                add(sysLogAddDTO);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

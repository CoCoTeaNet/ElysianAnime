package net.cocotea.janime.api.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import net.cocotea.janime.api.system.model.dto.SysLogAddDTO;
import net.cocotea.janime.api.system.model.dto.SysLogPageDTO;
import net.cocotea.janime.api.system.model.dto.SysLogUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysLog;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.api.system.model.vo.SysLogVO;
import net.cocotea.janime.api.system.service.SysLogService;
import net.cocotea.janime.common.enums.LogStatusEnum;
import net.cocotea.janime.common.enums.LogTypeEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.properties.DefaultProp;
import net.cocotea.janime.util.LoginUtils;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class SysLogServiceImpl implements SysLogService {

    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;

    @Db("db1")
    private LightDao lightDao;

    @Inject
    private DefaultProp defaultProp;

    @Override
    public boolean add(SysLogAddDTO sysLogAddDTO) throws BusinessException {
        SysLog sysOperationLog = BeanUtil.toBean(sysLogAddDTO, SysLog.class);
        Object save = sqlToyLazyDao.save(sysOperationLog);
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
        return sqlToyLazyDao.deleteByIds(SysLog.class, id) > 0;
    }

    @Override
    public ApiPage<SysLogVO> listByPage(SysLogPageDTO pageDTO) throws BusinessException {
        String operator = pageDTO.getSysLog().getOperator();

        Map<String, Object> sysLogMap = BeanUtil.beanToMap(pageDTO.getSysLog());
        sysLogMap.put("operator", operator);

        Page<SysLogVO> logVOPage = ApiPage.create(pageDTO);
        Page<SysLogVO> page = lightDao.findPage(logVOPage, "sys_log_JOIN_findList", sysLogMap, SysLogVO.class);

        return ApiPage.rest(page);
    }

    @Override
    public void saveByLogType(Integer logType, Context context) {
        ThreadUtil.execAsync(() -> {
            if (defaultProp.getSaveLog()) {
                SysLogAddDTO sysLogAddDTO = new SysLogAddDTO();
                sysLogAddDTO.setIpAddress(context.realIp());
                sysLogAddDTO.setLogType(logType);
                sysLogAddDTO.setRequestWay(context.method());
                sysLogAddDTO.setOperator(LoginUtils.loginId());
                sysLogAddDTO.setLogStatus(LogStatusEnum.SUCCESS.getCode());
                try {
                    add(sysLogAddDTO);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void saveErrorLog(Context context) {
        if (StpUtil.isLogin() && defaultProp.getSaveLog()) {
            SysLogAddDTO sysLogAddDTO = new SysLogAddDTO();
            sysLogAddDTO.setIpAddress(context.realIp());
            sysLogAddDTO.setLogType(LogTypeEnum.OPERATION.getCode());
            sysLogAddDTO.setRequestWay(context.method());
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

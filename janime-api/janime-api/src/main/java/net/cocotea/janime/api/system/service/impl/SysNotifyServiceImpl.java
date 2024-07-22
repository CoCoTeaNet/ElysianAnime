package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.janime.api.system.model.po.SysNotify;
import net.cocotea.janime.api.system.model.vo.SysNotifyVO;
import net.cocotea.janime.api.system.service.SysNotifyService;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.util.LoginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;

@Service
public class SysNotifyServiceImpl implements SysNotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SysNotifyServiceImpl.class);

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Override
    public void addNotify(SysNotifyAddDTO addDTO) throws BusinessException {
        boolean isGlobal = addDTO.getIsGlobal() == IsEnum.Y.getCode().intValue();
        LambdaQueryWrapper<SysNotify> lambdaWrapper = Wrappers.lambdaWrapper(SysNotify.class)
                .eq(SysNotify::getTitle, addDTO.getTitle())
                .eq(SysNotify::getIsDeleted, IsEnum.N.getCode());
        if (isGlobal) {
            lambdaWrapper.eq(SysNotify::getIsGlobal, IsEnum.Y.getCode());
        } else {
            if (addDTO.getReceiver() == null) {
                throw new BusinessException("非全局消息时，接收人必填");
            }
            lambdaWrapper
                    .eq(SysNotify::getIsGlobal, IsEnum.N.getCode())
                    .eq(SysNotify::getReceiver, addDTO.getReceiver());
        }
        SysNotify sysNotify = Convert.convert(SysNotify.class, addDTO);
        SysNotify sysNotifyExist = sqlToyHelperDao.findOne(lambdaWrapper);
        if (sysNotifyExist == null) {
            sqlToyHelperDao.save(sysNotify);
        } else {
            sysNotifyExist.setNotifyTime(DateUtil.date().toTimestamp());
            sqlToyHelperDao.update(sysNotifyExist);
        }
    }

    @Override
    public List<SysNotifyVO> listByType(String notifyType) {
        BigInteger loginId = LoginUtils.loginId();
        DateTime now = DateUtil.date();
        DateTime start = DateUtil.offsetDay(now, -30);
        LambdaQueryWrapper<SysNotify> lambdaWrapper = Wrappers.lambdaWrapper(SysNotify.class)
                .between(SysNotify::getNotifyTime, start, now)
                .eq(SysNotify::getReceiver, loginId)
                .eq(SysNotify::getIsDeleted, IsEnum.N.getCode())
                .orderByDesc(SysNotify::getNotifyTime)
                .orderByDesc(SysNotify::getId);
        List<SysNotify> list = sqlToyHelperDao.findList(lambdaWrapper);
        return Convert.toList(SysNotifyVO.class, list);
    }

    @Override
    public void read(BigInteger id) {
        sqlToyHelperDao.update(new SysNotify().setId(id).setIsDeleted(IsEnum.Y.getCode()));
    }

}

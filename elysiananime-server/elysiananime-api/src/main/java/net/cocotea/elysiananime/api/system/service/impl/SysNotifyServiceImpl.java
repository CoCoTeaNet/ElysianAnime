package net.cocotea.elysiananime.api.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import net.cocotea.elysiananime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.elysiananime.api.system.model.po.SysNotify;
import net.cocotea.elysiananime.api.system.model.vo.SysNotifyVO;
import net.cocotea.elysiananime.api.system.service.SysNotifyService;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.util.LoginUtils;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class SysNotifyServiceImpl implements SysNotifyService {

    private static final Logger log = LoggerFactory.getLogger(SysNotifyServiceImpl.class);

    @Db
    private LightDao lightDao;

    @Override
    public void addNotify(SysNotifyAddDTO addDTO) throws BusinessException {
        Map<String, Object> sysNotifyMapDTO = getNotifyMapDTO(addDTO);

        SysNotify sysNotify = BeanUtil.toBean(addDTO, SysNotify.class);
        SysNotify sysNotifyExist = lightDao.findOne("sys_notify_findList", sysNotifyMapDTO, SysNotify.class);

        if (sysNotifyExist == null) {
            lightDao.save(sysNotify);
        } else {
            sysNotifyExist.setNotifyTime(DateUtil.date().toTimestamp());
            lightDao.update(sysNotifyExist);
        }
    }

    private static Map<String, Object> getNotifyMapDTO(SysNotifyAddDTO addDTO) throws BusinessException {
        Map<String, Object> sysNotifyMapDTO = MapUtil.newHashMap();
        if (addDTO.getIsGlobal() == IsEnum.N.getCode().intValue()) {
            if (addDTO.getReceiver() == null) {
                throw new BusinessException("非全局消息时，接收人必填");
            }
            sysNotifyMapDTO.put("receiver", addDTO.getReceiver());
        }
        sysNotifyMapDTO.put("isGlobal", addDTO.getIsGlobal());
        sysNotifyMapDTO.put("title", addDTO.getTitle());
        return sysNotifyMapDTO;
    }

    @Override
    public List<SysNotifyVO> listByType(String notifyType) {
        BigInteger loginId = LoginUtils.loginId();
        DateTime now = DateUtil.date();
        DateTime start = DateUtil.offsetDay(now, -30);

        Map<String, Object> sysNotifyMapDTO = MapUtil.newHashMap(2);
        sysNotifyMapDTO.put("betweenNotifyTime", Arrays.asList(start, now));
        sysNotifyMapDTO.put("receiver", loginId);

        List<SysNotify> list = lightDao.find("sys_notify_findList", sysNotifyMapDTO, SysNotify.class);
        return Convert.toList(SysNotifyVO.class, list);
    }

    @Override
    public void read(BigInteger id) {
        lightDao.update(new SysNotify().setId(id).setIsDeleted(IsEnum.Y.getCode()));
    }

}

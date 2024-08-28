package net.cocotea.elysiananime.api.system.service;

import net.cocotea.elysiananime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysNotifyVO;
import net.cocotea.elysiananime.common.model.BusinessException;

import java.math.BigInteger;
import java.util.List;

public interface SysNotifyService {

    /**
     * 新增通知消息
     * @param addDTO {@link SysNotifyAddDTO}
     */
    void addNotify(SysNotifyAddDTO addDTO) throws BusinessException;

    /**
     * 通过通知类型获取通知列表（备注：默认时间为30天）
     *
     * @param notifyType 通知类型 {@link net.cocotea.elysiananime.common.constant.NotifyConst}
     * @return {@link SysNotifyVO}
     */
    List<SysNotifyVO> listByType(String notifyType);

    /**
     * 已读通知消息
     *
     * @param id 通知ID
     */
    void read(BigInteger id);

}

package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.sql.Timestamp;

/**
 * 新增通知传输参数
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysNotifyAddDTO {

    /**
     * 通知标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String memo;

    /**
     * 跳转链接
     */
    private String jumpUrl;

    /**
     * 通知类型 {@link net.cocotea.janime.common.constant.NotifyConst}
     */
    private String notifyType;

    /**
     * 接收人
     */
    private BigInteger receiver;

    /**
     * 是否全局
     */
    private Integer isGlobal;

    /**
     * 通知等级
     */
    private Integer level;

    /**
     * notify_time
     */
    private Timestamp notifyTime;

}

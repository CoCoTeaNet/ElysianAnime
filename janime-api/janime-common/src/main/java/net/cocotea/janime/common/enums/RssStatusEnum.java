package net.cocotea.janime.common.enums;

/**
 * 订阅状态: 0未订阅 1订阅中 2订阅完成
 *
 * @date 2023-6-24 00:15:05
 * @author CoCoTea
 * @since v1
 */
public enum RssStatusEnum {
    /**
     * 未订阅
     */
    UNSUBSCRIBED(0, "未订阅"),
    /**
     * 订阅中
     */
    SUBSCRIBING(1, "订阅中"),
    /**
     * 订阅完成
     */
    SUBSCRIPTION_COMPLETED(2, "订阅完成");

    final Integer code;
    final String desc;

    RssStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

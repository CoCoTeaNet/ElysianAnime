package net.cocotea.janime.common.enums;

/**
 * 发布状态;1正常 2审核中 3审核不通过 4冻结 5保存
 * @date 2022-8-12 00:44:20
 * @author CoCoTea
 */
public enum PublishStatusEnum {
    NORMAL(1, "正常"),
    REVIEW(2, "审核中"),
    REVIEW_FAILED(3, "审核不通过"),
    FROZEN(4, "冻结"),
    SAVE(5, "保存"),
    ;
    final Integer code;
    final String desc;

    PublishStatusEnum(Integer code, String desc) {
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

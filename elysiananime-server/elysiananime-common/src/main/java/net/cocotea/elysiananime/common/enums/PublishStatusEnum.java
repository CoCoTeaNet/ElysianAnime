package net.cocotea.elysiananime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发布状态;1正常 2审核中 3审核不通过 4冻结 5保存
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@Getter
@AllArgsConstructor
public enum PublishStatusEnum {
    NORMAL(1, "正常"),
    REVIEW(2, "审核中"),
    REVIEW_FAILED(3, "审核不通过"),
    FROZEN(4, "冻结"),
    SAVE(5, "保存"),
    ;
    final Integer code;
    final String desc;
}

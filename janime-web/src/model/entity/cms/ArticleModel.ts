/**
 * @author CoCoTea
 * 文章主表
 */
interface ArticleModel {
    // 主键id
    id?: string,
    // 标题
    title?: string,
    // 封面
    cover?: string,
    // 内容
    content?: string,
    // 简介
    summary?: string,
    // 发布状态;1正常 2审核中 3审核不通过 4冻结 5保存
    publishStatus?: number,
    // 标签：用逗号切割
    tags?: string,
    // 创建人
    createBy?: string,
    // 创建时间
    createTime?: string,
    // 更新人
    updateBy?: string,
    // 更新时间
    updateTime?: string,
    // 删除状态;0删除 1未删除
    deleteStatus?: number,
    // 乐观锁
    revision?: number,
}
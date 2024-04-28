/**
 * @author CoCoTea
 * 评论主表
 */
interface CommentModel {
    // 主键id
    id?: string,
    // 父级ID
    parentId?: string,
    // 回复用户ID
    replyUserId?: string,
    // 回复内容
    content?: string,
    // 回复类型;0文章 1用户
    replyType?: string,
    // 创建人
    createBy?: string,
    // 创建时间
    createTime?: string,
    // 更新人
    updateBy?: string,
    // 更新时间
    updateTime?: string,
    // 删除状态;0删除 1未删除
    deleteStatus?: string,
    // 乐观锁
    revision?: number,
    // 文章ID
    articleId?: string,
}
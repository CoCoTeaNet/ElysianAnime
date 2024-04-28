/**
* @author CoCoTea 572315466@qq.com
* @since 1.2.1 2022-12-28
* 系统文件表
*/
interface SysFileModel {

    // 主键ID
    id?: string,
    // 文件名称
    fileName?: string,
    // 文件后缀
    fileSuffix?: string,
    // 文件真实路径
    realPath?: string,
    // 浏览路径
    browsePath?: string,
    // 文件访问地址
    fullPath?: string,
    // 文件大小（单位：字节）
    fileSize?: number,
    // 创建人
    createBy?: string,
    // 创建时间
    createTime?: string,
    // 更新人
    updateBy?: string,
    // 更新时间
    updateTime?: string,
    // 是否分享
    isShare?: number
}
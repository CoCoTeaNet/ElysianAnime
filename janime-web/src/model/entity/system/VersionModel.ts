/**
 * @author CoCoTea
 * 系统版本更新记录表
 */
interface VersionModel {
    //
    id?: string,
    // 更新版本号
    updateNo?: string,
    // 版本更新描述
    updateDesc?: string,
    // 系统平台名称
    platformName?: string,
    // 下载地址
    downloadUrl?: string,
    // 创建人
    createBy?: string,
    // 创建时间
    createTime?: string,
    // 更新人
    updateBy?: string,
    // 更新时间
    updateTime?: string,
}
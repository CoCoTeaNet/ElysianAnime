/**
 * @author CoCoTea
 * 系统操作日志表
 */
interface OperationLogModel {
    // 主键id
    id?: string,
    // 日志编号
    logNumber?: number,
    // 请求IP地址
    ipAddress?: string,
    // 操作人员
    operator?: string,
    // 请求方式
    requestWay?: string,
    // 操作状态;0异常 1成功
    operationStatus?: number,
    // 系统模块
    systemModule?: string,
    // 操作类型;0登录日志 1操作日志
    operationType?: number,
    // 操作地点
    operationAddress?: string,
    // 日志类型
    logType?: number,
    // 操作时间
    operationTime?: string,
    // 排序
    sort?: number,
}
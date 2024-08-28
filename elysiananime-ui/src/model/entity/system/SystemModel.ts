/**
 * 系统信息模型
 */
interface SystemModel {

    os: string,

    serverName: string,

    serverIp: string,

    serverArchitecture: string,

    javaName: string

    javaVersion: string,

    javaPath: string,

    projectPath: string,

    runningTime: number

    cpuSystemUsed: number,

    cpuUserUsed: number

    cpuFree: number,

    cpuCount: number,

    memoryTotalSize: number,

    memoryAvailableSize: number,

    diskTotalSize: number,

    diskFreeSize: number,

    diskPath: string,

    diskSeparator: string
}
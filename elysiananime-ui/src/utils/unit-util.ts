const unitUtil = {
    /**
     * 计算内存单位（最低单位字节），返回字符串类型
     */
    memoryCalculate: memoryCalculate,
    /**
     * 计算时间单位（最低单位为秒）
     */
    timeCalculate: timeCalculate
}

function timeCalculate(num: number) {
    if (!num) {
        return '';
    }
    if (num > 86400) {
        return (num / 86400) + ' 天';
    } else if (num > 3600) {
        return (num / 3600) + ' 小时';
    } else if (num > 60) {
        return (num / 60) + ' 分钟';
    } else {
        return num + ' 秒';
    }
}

function memoryCalculate(num: number) {
    if (!num) {
        return '';
    }
    if (num > 1099511627776) {
        return (num / 1099511627776).toFixed(2) + " T";
    } else if (num > 1073741824) {
        return (num / 1073741824).toFixed(2) + " GB";
    } else if (num > 1048576) {
        return (num / 1048576).toFixed(2) + ' MB';
    } else {
        return (num / 1024).toFixed(2) + ' KB';
    }
}

export default unitUtil;
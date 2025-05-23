const formatUtil = {
    /**
     * 少于10补0，比如01
     */
    fillZero: fillZero,
    formatDate,
    strToDate    
}

function fillZero(num: number): string {
    return num < 10 ? '0' + num : '' + num;
}


function formatDate(date: Date | number | string, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
    if (typeof date === 'string' || typeof date === 'number') {
        date = new Date(date);
    }

    const year = date.getFullYear();
    const month = fillZero(date.getMonth() + 1);
    const day = fillZero(date.getDate());
    const hour = fillZero(date.getHours());
    const minute = fillZero(date.getMinutes());
    const second = fillZero(date.getSeconds());

    return format
        .replace('YYYY', year.toString())
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hour)
        .replace('mm', minute)
        .replace('ss', second);
}

function strToDate(dateStr: string): Date {
    const regex = /(\d{4})[年\/\-\.](\d{1,2})[月\/\-\.](\d{1,2})[日\/\-\.]/;
    const match = dateStr.match(regex);
    if (!match) {
        throw new Error('Invalid date string');
    }
    const year = parseInt(match[1], 10);
    const month = parseInt(match[2], 10) - 1; // 月份从 0 开始计数
    const day = parseInt(match[3], 10);
    return new Date(year, month, day);
}

export default formatUtil;
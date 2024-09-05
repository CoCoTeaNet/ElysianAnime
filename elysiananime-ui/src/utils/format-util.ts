const formatUtil = {
    /**
     * 少于10补0，比如01
     */
    fillZero: fillZero
}

function fillZero(num: number): string {
    return num < 10 ? '0' + num : '' + num;
}

export default formatUtil;
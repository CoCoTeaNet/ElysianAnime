import {ref} from "vue";

const acgUserOpusTypes = {
    getReadStatusCn: getReadStatusCn,
    getReadStatusList: getReadStatusList,
}

export function getReadStatusCn(s: number) {
    switch (s) {
        case 0:
            return '未看';
        case 1:
            return '已看';
        case 2:
            return '在看';
    }
    return s;
}

export function getReadStatusList() {
    return [
        {label: '未看', value: 0},
        {label: '已看', value: 1},
        {label: '在看', value: 2},
    ]
}


export default acgUserOpusTypes;
const commonTypes = {
    getIsCn: getIsCn,
    getIsList: getIsList
}

export function getIsCn(s: number) {
    switch (s) {
        case 0:
            return '否';
        case 1:
            return '是';
    }
    return s;
}
export function getIsList() {
    return [
        {label: '否', value: 0},
        {label: '是', value: 1},
    ]
}

export default commonTypes;
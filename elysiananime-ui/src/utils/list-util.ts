const treeMap = (list : any[], callback : Function): void => {
    list.map((item) => {
        if (item.children && item.children.length > 0) {
            treeMap(item.children, callback);
        }
        callback(item);
    });
}

/**
 * 通过集数查找资源URL
 */
const findResUrl = (list: any[], readingNum: number) => {
    let url = "";
    for (let i = 0; i < list.length; i++) {
        let item = list[i];
        if (item && item.name === readingNum) {
            url = item.url;
            break;
        }
    }
    return url;
}

const listUtil = {
    treeMap, findResUrl
}

export default listUtil;

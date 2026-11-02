const treeMap = (list : any[], callback : Function): void => {
    list.map((item) => {
        if (item.children && item.children.length > 0) {
            treeMap(item.children, callback);
        }
        callback(item);
    });
}

const listUtil = {
    treeMap
}

export default listUtil;

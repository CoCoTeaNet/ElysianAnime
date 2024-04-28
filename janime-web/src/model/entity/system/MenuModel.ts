/**
 * 菜单模型
 */
interface MenuModel {
    id?: string,
    parentId?: string,
    menuName?: string,
    menuType?: number,
    iconPath?: string,
    routerPath?: string,
    menuStatus?: number,
    isExternalLink?: number,
    isMenu?: number,
    sort?: number,
    permissionCode?: string,
    children?: MenuModel[]
}
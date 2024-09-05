/**
 * 用户模型
 */
interface UserModel {
    id?: string,
    username?: string,
    nickname?: string,
    email?: string,
    sex?: number,
    accountStatus?: number,
    avatar?: string,
    roleIds?: string[],
    roleList?: RoleModel[],
    roleName?: string,
    password?: string,
    token?: string,
    loginStatus?: boolean,
    menuList?: MenuModel[]
}
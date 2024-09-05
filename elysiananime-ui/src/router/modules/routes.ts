const AdminLayout = () => import("@/layout/AdminLayout.vue");
const Home = () => import("@/views/system/dashboard/Home.vue");
const UserView = () => import("@/views/system/manager/system/user/UserView.vue");
const MenuView = () => import("@/views/system/manager/system/menu/MenuView.vue");
const DictionaryView = () => import("@/views/system/manager/system/dictionary/DictionaryView.vue");
const PermissionView = () => import("@/views/system/manager/system/menu/PermissionView.vue");
const RoleView = () => import("@/views/system/manager/system/role/RoleView.vue");
const Dashboard = () => import("@/views/system/dashboard/Dashboard.vue");
const NotFound = () => import("@/views/error/NotFound.vue");
const UserCenterView = () => import("@/views/system/personal/UserCenterView.vue");
const OperationLogView = () => import("@/views/system/manager/system/log/SysLogView.vue");
const VersionView = () => import("@/views/system/manager/system/version/VersionView.vue");
const SysFileView = () => import("@/views/system/manager/system/file/SysFileView.vue");
const SysRecycleView = () => import("@/views/system/manager/system/file/SysRecycleView.vue");
const Login = () => import("@/views/system/login/Login.vue");
const AnimeOpusView = () => import("@/views/system/manager/anime/AnimeOpusView.vue");
const AnimeUserOpusView = () => import("@/views/system/manager/anime/AnimeUserOpusView.vue");
const AnimeLayout = () => import("@/layout/AnimeLayout.vue");
const AnimeHome = () => import("@/views/home/AnimeHome.vue");
const AnimeVideo = () => import("@/views/home/AnimeVideo.vue");
const AnimeShare = () => import("@/views/home/AnimeShare.vue");


export const routes = [
    {
        path: "/anime",
        name: "AnimeLayout",
        meta: {title: 'ElysianAnime'},
        component: AnimeLayout,
        redirect: {name: 'AnimeHome'},
        children: [
            {path: 'home', meta: {title: '追番管理系统~'}, name: 'AnimeHome', component: AnimeHome},
            {path: 'video/:id/:num/:time', meta: {title: '番剧资源'}, name: 'AnimeVideo', component: AnimeVideo},
            {path: 'shares', meta: {title: '番剧推荐'}, name: 'AnimeShare', component: AnimeShare},
        ]
    },
    {
        path: "/login",
        name: "Login",
        meta: {title: '登录ElysianAnime追番系统~~~'},
        component: Login
    },
    {
        path: '/admin',
        name: 'Admin',
        meta: {title: '后台管理'},
        component: AdminLayout,
        redirect: {name: 'Home'},
        children: [
            // 其它模块
            {path: 'home', meta: {title: '首页'}, name: 'Home', component: Home},
            {path: 'dashboard', meta: {title: '仪表盘'}, name: 'Dashboard', component: Dashboard},
            // 系统模块
            {path: 'sys-user-manager', meta: {title: '用户管理'}, name: 'UserView', component: UserView},
            {path: 'sys-menu-manager', meta: {title: '菜单管理'}, name: 'MenuView', component: MenuView},
            {path: 'sys-permission-manager', meta: {title: '权限管理'}, name: 'PermissionView', component: PermissionView},
            {path: 'sys-role-manager', meta: {title: '角色管理'}, name: 'RoleView', component: RoleView},
            {path: 'sys-dictionary-manager', meta: {title: '字典管理'}, name: 'DictionaryView', component: DictionaryView},
            {path: 'sys-log-manager', meta: {title: '日志管理'}, name: 'OperationLogView', component: OperationLogView},
            {path: 'sys-user-center', meta: {title: '用户中心'}, name: 'UserCenterView', component: UserCenterView},
            {path: 'sys-version-manager', meta: {title: '版本管理'}, name: 'VersionView', component: VersionView},
            {path: 'sys-file-manager', meta: {title: '文件管理'}, name: 'SysFileView', component: SysFileView},
            {path: 'sys-recycle-manager', meta: {title: '文件回收站'}, name: 'RecycleBinView', component: SysRecycleView},
            // 动漫模块
            {path: 'ani-opus-manager', meta: {title: '番剧管理'}, name: 'AnimeOpusView', component: AnimeOpusView},
            {path: 'ani-user-opus-manager', meta: {title: '我的追番'}, name: 'AnimeUserOpusView', component: AnimeUserOpusView},
        ]
    },
    {
        path: '/:pathMatch(.*)',
        name: 'error',
        component: NotFound,
        meta: {title: '404'},
    }
];

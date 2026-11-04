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
const Login = () => import("@/views/system/login/Login.vue");
const AnimeOpusView = () => import("@/views/system/manager/anime/AnimeOpusView.vue");
const AnimeUserOpusView = () => import("@/views/system/manager/anime/AnimeUserOpusView.vue");
const AnimeHome = () => import("@/views/anime/home/AnimeHome.vue");
const AnimeDetail = () => import("@/views/anime/detail/AnimeDetail.vue");
const AnimeShare = () => import("@/views/home/AnimeShare.vue");
const AnimeFeedList = () => import("@/views/home/AnimeFeedList.vue");

/**
 * 旧链接 num 按 episode_key 规则归一：03 → 3、SP01 → SP1；
 * 0 / 1 是旧调用方的「默认集」占位，不映射成集数
 */
const normalizeEpisodeKey = (raw: unknown): string => {
    const num = String(raw ?? '').trim();
    if (!num || num === '0' || num === '1') {
        return '';
    }
    const spMatch = num.match(/^SP\s*0*(\d+)$/i);
    if (spMatch) {
        return `SP${spMatch[1]}`;
    }
    const n = Number(num);
    if (!isNaN(n) && isFinite(n)) {
        return String(n);
    }
    return num;
};


export const routes = [
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
            // 番剧管理模块
            {path: 'ani-opus-manager', meta: {title: '番剧管理'}, name: 'AnimeOpusView', component: AnimeOpusView},
            {path: 'ani-user-opus-manager', meta: {title: '我的追番'}, name: 'AnimeUserOpusView', component: AnimeUserOpusView},
            // 番剧使用模块
            {path: 'ani-home', meta: {title: '追番首页'}, name: 'AnimeHome', component: AnimeHome},
            {
                path: 'ani-opus/:id',
                meta: {title: '番剧详情', keepAlive: false},
                name: 'AnimeDetail',
                component: AnimeDetail
            },
            {
                path: 'ani-video/:id/:num/:time',
                redirect: to => {
                    const query: Record<string, string> = {};
                    const ek = normalizeEpisodeKey(to.params.num);
                    const t = Number(to.params.time);
                    if (ek) {
                        query.ek = ek;
                    }
                    if (!isNaN(t) && t > 1) {
                        query.t = String(t);
                    }
                    return {path: `/admin/ani-opus/${to.params.id}`, query};
                }
            },
            {path: 'ani-shares', meta: {title: '番剧推荐'}, name: 'AnimeShare', component: AnimeShare},
            {path: 'ani-feed-list', meta: {title: '番剧投喂'}, name: 'AnimeFeedList', component: AnimeFeedList},
        ]
    },
    {
        path: '/:pathMatch(.*)',
        name: 'error',
        component: NotFound,
        meta: {title: '404'},
    }
];

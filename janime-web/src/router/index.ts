import {createRouter, createWebHashHistory, RouteLocationNormalized} from "vue-router";
import {routes} from "@/router/modules/routes";

export const router = createRouter({
    history: createWebHashHistory(),
    routes: routes
})

/**
 * 路由守卫
 * to: Route: 即将要进入的目标 路由对象
 * from: Route: 当前导航正要离开的路由
 * next: Function: 一定要调用该方法来 resolve 这个钩子。执行效果依赖 next 方法的调用参数
 */
router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized):boolean|object => {
    if ('/' == to.path) {
        return {name: 'AnimeHome'};
    }
    return true;
});

router.afterEach(function (to: any, from: any) {
    let title = '';
    to.matched.forEach((item: any, index: any) => {
        let field = (index === to.matched.length - 1 ? '' : ' - ');
        title += item.meta.title + field;
    });
    // 动态刷新title
    if (title) {
        document.title = title;
    }
})

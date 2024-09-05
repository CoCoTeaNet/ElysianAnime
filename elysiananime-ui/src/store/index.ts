import {InjectionKey} from 'vue';
import {createStore, Store, useStore as baseUseStore} from 'vuex';
// @ts-ignore
import md5 from 'js-md5';
import {router} from "@/router";

export interface State {
    // 用户信息
    userInfo: UserModel,
    // 是否折叠菜单
    isCollapseMenu: boolean,
    tabItems: TabItem[],
    // 是否胖屏显示
    isFullScreen: boolean
}

export const key: InjectionKey<Store<State>> = Symbol()

export const store = createStore<State>({
    state: {
        userInfo: {
            id: '',
            username: '',
            nickname: '',
            token: '',
            loginStatus: false,
            avatar: '',
            menuList: []
        },
        isCollapseMenu: false,
        tabItems: [],
        isFullScreen: getFullScreen()
    }
})

// 定义自己的 `useStore` 组合式函数
export function useStore() {
    return baseUseStore(key)
}

export function removeTabItem(id: string) {
    let arr: TabItem[] = [];
    store.state.tabItems.forEach((item: TabItem) => {
        if (item.id != id) arr.push(item);
    });
    store.state.tabItems = arr;
    // 激活最后一项
    if (arr.length > 0) {
        let activeTab = arr[arr.length - 1];
        activeTab.isActive = true;
        router.push({path: activeTab.url ? activeTab.url : ''}).then(r => console.log(r));
    } else {
        initTabItems();
    }
}

export function addTabItem(tab: TabItem) {
    tab.id = md5(tab.url);
    if (store.state.tabItems.length === 0) {
        store.state.tabItems.push(tab);
    } else {
        let existFlag = false;
        store.state.tabItems.forEach((item: TabItem) => {
            if (item.id == tab.id) {
                existFlag = true;
            }
        });
        if (!existFlag) {
            store.state.tabItems.forEach((item: TabItem) => item.isActive = false);
            store.state.tabItems.push(tab);
        } else {
            store.state.tabItems.forEach((item: TabItem) => {
                item.isActive = item.id === tab.id;
            });
        }
    }
}

export function initTabItems() {
    store.state.tabItems = [];
    let url: string = '/admin/home';
    store.state.tabItems.push({name: '首页', id: md5(url), isActive: true, url: url});
    router.push({path: url}).then(r => console.log(r));
}

/**
 * 设置用户信息
 * @param v 用户模型
 */
export function setUserInfo(v: UserModel) {
    store.state.userInfo = v;
    localStorage.setItem("userInfo", JSON.stringify(v));
}

/**
 * 更新用户信息
 * 不包括菜单
 * @param v 用户模型
 */
export function updateUserInfo(v: UserModel) {
    v.menuList = store.state.userInfo.menuList;
    v.loginStatus = store.state.userInfo.loginStatus;
    v.token = store.state.userInfo.token;
    setUserInfo(v);
}

/**
 * 设置是否折叠菜单
 */
export function setCollapseMenu() {
    store.state.isCollapseMenu = !store.state.isCollapseMenu;
}

/**
 * 设置胖屏状态
 */
export function setFullScreen() {
    let flag: boolean = !store.state.isFullScreen;
    store.state.isFullScreen = flag;
    localStorage.setItem("isFullScreen", String(flag));
}

function getFullScreen(): boolean {
    let value = localStorage.getItem("isFullScreen");
    let flag: boolean = true;
    if (value) {
        flag = ('false' !== value);
    }
    console.log(`value=${value},flag=${flag}`);
    return flag;
}
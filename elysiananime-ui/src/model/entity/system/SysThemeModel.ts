/**
 * @author CoCoTea 572315466@qq.com
 * @since 1.2.4 2023-2-25 22:36:27
 * 系统主题表
 */
interface SysThemeModel {

    // 主键ID
    id?: string,
    // 布局模式：0默认
    layoutMode?: number,
    // 主题颜色
    primaryColor?: string,
    // 颜色2
    color2?: string,
    // 颜色3
    color3?: string,
    // 颜色4
    color4?: string,
    // 是否暗黑模式：0否 1是
    isDark?: boolean,

}
import {createApp} from 'vue'
import 'element-plus/dist/index.css'
import ElementPlus from 'element-plus'
import App from './App.vue'
import {router} from './router'
import { store, key } from './store'
import * as ElIcons from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// 暗黑模式
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/dark/css-vars.css'

const app = createApp(App)

// 统一注册Icon图标
for (const name in ElIcons){
    app.component(name,(ElIcons as any)[name])
}
app.use(router)
app.use(store, key)
app.use(ElementPlus, {locale: zhCn})
app.mount('#app')

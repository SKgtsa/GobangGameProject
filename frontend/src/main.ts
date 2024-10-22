import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
/* import store from '/store' */
import ElementPlus from 'element-plus'
import 'element-plus/theme-chalk/display.css'
import 'element-plus/dist/index.css'
import './assets/main.css'
import './common/common.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import Vue3TouchEvents from "vue3-touch-events";
const app = createApp(App)



app.use(router)

app.use(ElementPlus)

app.use(Vue3TouchEvents)

app.mount('#app')

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}


/**
 * 全局变量
 * 采用全局变量实现组件间参数的传递
 */

import {ref} from "vue";
export const windowWidth = ref(0)
export const windowHeight = ref(0)
export const mobile = ref(false)

// export const baseURL = ref('http://localhost:5174');
export const baseURL = ref('http://gobangback.clankalliance.cn');

export const getBaseURL = () => {
    return baseURL.value;
}
//展示app.vue中的router
export const showGlobalRouter = ref(true);
//数据对象 真实的值
export let data = ref([])

export const showPage = ref(false);

export const transition = () => {
    showPage.value = false;
    setTimeout(() => {
        showPage.value = true;
    },400)
}


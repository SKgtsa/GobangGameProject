/**
 * 全局变量
 * 采用全局变量实现组件间参数的传递
 */

import {ref} from "vue";
export const windowWidth = ref(0)
export const windowHeight = ref(0)
export const mobile = ref(false)

//数据对象 展示的值
export let dataOnDisplay = ref([])

//数据对象 真实的值
export let data = ref([])

//每一个数据对应的宽度和高度
export let targetWidth = ref('0')
//每一个数据方块距离顶部的竖直距离
export let targetPositionLeft = ref([])
//每一个数据方块距离左侧的水平距离
export let targetPositionTop = ref([])

//0 未选择 1 冒泡排序 2 箱子排序 3 堆排序 4 归并排序
export const sortMethod = ref('0')

//视觉中心 用于根据当前对象位置来移动视图
export const focusX = ref(0);
export const focusY = ref(0);

//为修复堆排序bug使用的数值数组
export const valueData = ref<number[]>([])

export const showPage = ref(false);

export const transition = () => {
    showPage.value = false;
    setTimeout(() => {
        showPage.value = true;
    },400)
}
//动画帧数 每秒刷新次数
export const fps = ref(60);
//程序周期 每个操作的时间 单位为秒
export const cycle = ref(1);

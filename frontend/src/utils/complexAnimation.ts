import {
    cycle,
    data,
    dataOnDisplay, focusX, focusY, fps,
    targetWidth, valueData,
    windowHeight
} from "@/global/global";

/**
 * 高级动画工具
 * 通过将目标以对象的形式储存来实现更复杂的动画
 * 针对箱子排序 归并排序设计
 */


export interface Target {
    x: number,
    y: number,
    value: number,
    order: number
}

export const transform = (oldData: number[]): Target[] => {
    let dataTransformed: Target[] = [];
    for(let i = 0;i < oldData.length;i ++){
        dataTransformed[i] = {
            x: Number(targetWidth.value) * (i + 0.5),
            y: windowHeight.value * 0.2,
            value: oldData[i],
            order: i,
        }
    }
    return dataTransformed;
}

/**
 * 归并排序使用的 面向更宽区域居中的生成方法
 * @param oldData
 */
export const transformForMerge = (oldData: string[]): Target[] => {
    let dataTransformed: Target[] = [];
    let center = oldData.length / 2 - 1;
    let width = Number(targetWidth.value) * (oldData.length * 2 + 1);
    for(let i = 0;i < oldData.length;i ++){
        dataTransformed[i] = {
            x: width / 2 + (i - center) * Number(targetWidth.value),
            y: Number(targetWidth.value)* 1.5,
            value: Number(oldData[i]),
            order: i,
        }
    }
    console.log(dataTransformed)
    return dataTransformed;
}

/**
 * 堆排序所使用的 建立树的生成方法
 * @param oldData
 */
export const transformForHeap = (oldData: string[]): Target[] => {
    let dataTransformed: Target[] = [];
    let height = 1;
    let temp = 2;
    //该行中元素到其孩子的横向距离
    let lineBase = 0.5;
    let lastLineLength = 1;
    for(;temp - 1 < oldData.length;temp *= 2){
        height ++;
        lineBase *= 2;
        lastLineLength *= 2;
    }
    dataTransformed[0] = {
        x: Number(targetWidth.value) * (2 * lastLineLength + 1) / 2,
        y: Number(targetWidth.value),
        value: Number(oldData[0]),
        order: 0,
    }
    valueData.value[0] = Number(oldData[0]);
    let left = true;
    //当前层剩余多少个
    let levelNumRemain = 2;
    //当前层总数
    let levelNum = 2;
    for(let i = 1;i < oldData.length;i ++){
        if(levelNumRemain === 0){
            levelNum *= 2;
            levelNumRemain = levelNum;
            lineBase /= 2;
        }
        dataTransformed[i] = {
            x: dataTransformed[Math.floor((i - 1) / 2)].x + (left? -1:1) * lineBase * Number(targetWidth.value),
            y: dataTransformed[Math.floor((i - 1) / 2)].y + 2 * Number(targetWidth.value),
            value: Number(oldData[i]),
            order: i,
        };
        valueData.value[i] = Number(oldData[i]);
        left = !left;
        levelNumRemain --;
    }
    return dataTransformed;
}

/**
 * 移动函数
 * 将target移动到(x,y)
 * 若focus为true,则视野跟随target
 * @param order
 * @param x
 * @param y
 * @param focus 可选参数 视野是否跟随
 */
export const move = (order: number, x: number, y: number, focus : boolean = true) => {
    const paceX = (x - dataOnDisplay.value[order].x) / cycle.value /fps.value;
    const paceY = (y - dataOnDisplay.value[order].y) / cycle.value /fps.value;
    let tempRefreshTime = 0;
    let tempClock = setInterval(() => {
        dataOnDisplay.value[order].x += paceX;
        dataOnDisplay.value[order].y += paceY;
        if(focus){
            focusX.value = dataOnDisplay.value[order].x;
            focusY.value = dataOnDisplay.value[order].y;
        }
        tempRefreshTime ++;
        if(tempRefreshTime >= fps.value * cycle.value){
            tempRefreshTime = 0;
            window.clearInterval(tempClock);
            console.log(dataOnDisplay.value[order])
        }
    },cycle.value * 1000 / fps.value)
    return 1;
}

/**
 * 批量移动函数
 * 将target数组中所有目标以centerOrder为中心移动到(x,y)
 * @param start
 * @param end
 * @param deltaX
 * @param deltaY
 * @param centerOrder target[centerOrder]为中心
 * @param focus 可选参数 视野是否跟随
 */
export const batchMove = (start: number, end: number, deltaX: number, deltaY: number, centerOrder: number, focus: boolean = true) => {
    console.log('把从' + start + '到' + end + '的目标向右移动' + deltaX + ' 向下移动' + deltaY);
    let tempRefreshTime = 0;
    const paceX = deltaX / cycle.value /fps.value;
    const paceY = deltaY / cycle.value /fps.value;
    let tempClock = setInterval(() => {
        for(let i = start;i <= end;i ++){
            dataOnDisplay.value[i].x += paceX;
            dataOnDisplay.value[i].y += paceY;
            if(focus){
                focusX.value = dataOnDisplay.value[i].x;
                focusY.value = dataOnDisplay.value[i].y;
            }
        }
        tempRefreshTime ++;
        if(tempRefreshTime === fps.value * cycle.value){
            tempRefreshTime = 0;
            window.clearInterval(tempClock);
        }
    },cycle.value * 1000 / fps.value)
    return 1;
}

export const generateBox = () => {
    let maxNum = 0;
    for(let i = 0;i < data.value.length;i ++){
        if(Number(data.value[i].value) > maxNum)
            maxNum = Number(data.value[i].value);
    }
    let boxList : Target[] = [];
    for(let i = 0;i <= maxNum; i ++){
        boxList[i] = {
            x:  Number(targetWidth.value) * (i + 0.5),
            y:  windowHeight.value * 0.1,
            value: 0,
            order: i
        }
    }
    return boxList;
}




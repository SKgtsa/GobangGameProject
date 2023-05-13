import {
    cycle,
    data,
    dataOnDisplay,
    fps,
    targetPositionLeft,
    targetPositionTop,
    targetWidth,
    windowHeight
} from "@/global/global";

/**
 * 一维动画工具
 * 预先编写好一系列简单移动，按照统一的周期移动。
 * 针对线性表编写
 * 调用函数的时刻为动画开始帧
 */

/**
 * 复位操作 瞬时刷新
 * 调用本函数会对数据的显示进行复位
 * 使用data中的数据来更新dataOnDisplay
 */
export const rePosition = () => {
    targetWidth.value = '' + windowHeight.value * 0.1;
    let newPositionTop = [dataOnDisplay.value.length];
    let newPositionLeft = [dataOnDisplay.value.length];
    for(let i = 0;i < dataOnDisplay.value.length;i ++){
        newPositionTop[i] =  windowHeight.value * 0.2;
        newPositionLeft[i] = Number(targetWidth.value) * (i + 0.5)
    }
    targetPositionTop.value = newPositionTop;
    targetPositionLeft.value = newPositionLeft;
}
/**
 * 上移操作 执行一个周期
 * 将从start到end的这段数组上移
 * @param start
 * @param end
 */
const up = (target: number) => {
    console.log('up' + target)
    let tempRefreshTime = 0;
    //逐帧刷新位置
    let tempClock = setInterval(() => {
        targetPositionTop.value[target] -= Number(targetWidth.value) / cycle.value / fps.value;
        tempRefreshTime ++;
        if(tempRefreshTime === fps.value * cycle.value){
            tempRefreshTime = 0;
            window.clearInterval(tempClock);
        }
    },cycle.value * 1000 / fps.value)
}
/**
 * 下移操作
 * 将从start到end的这段数组下移
 * @param start
 * @param end
 */
const down = (target: number) => {
    console.log('down' + target)
    let tempRefreshTime = 0;
    //逐帧刷新位置
    let tempClock = setInterval(() => {
        targetPositionTop.value[target] += Number(targetWidth.value) / cycle.value / fps.value;
        tempRefreshTime ++;
        if(tempRefreshTime === fps.value * cycle.value){
            tempRefreshTime = 0;
            window.clearInterval(tempClock);
        }
    },cycle.value * 1000 / fps.value)
}
/**
 * 平移操作
 * 将start的数据移动到position
 * 只移动位置，更新data
 * @param start
 * @param end
 * @param position
 */
const move = (start: number, position: number) => {
    console.log('move from ' + start + ' to ' + position)
    if(start === position)
        return;
    let tempRefreshTime = 0;
    //逐帧刷新位置
    let tempClock = setInterval(() => {
        console.log('tempRefreshTime=' + tempRefreshTime + ' targetRefreshTime: ' + fps.value * cycle.value)
        targetPositionLeft.value[start] += (position - start) * Number(targetWidth.value) / fps.value / cycle.value;

        tempRefreshTime ++;
        if(tempRefreshTime === fps.value * cycle.value){
            tempRefreshTime = 0;
            window.clearInterval(tempClock);
        }
    },cycle.value * 1000 / fps.value)
}
/**
 * 交换操作 三个周期
 * 将从startA到endA的这段数组与startB到endB这段数组进行位置的调换
 * startA==endA时则a里只有一个数
 * @param startA
 * @param endA
 * @param startB
 * @param endB
 */
export const swap = (a: number, b: number) => {
    console.log('swap ' + a + ' and ' + b)
    setTimeout(() => {
        console.log(dataOnDisplay.value)
        setTimeout(() => {
            down(a);
            down(b);
            setTimeout(() => {
                //交换
                let temp = data.value[a];
                data.value[a] = data.value[b];
                data.value[b] = temp;
                rePosition()
            }, cycle.value * 1000)
        },cycle.value * 1000)
        console.log(dataOnDisplay.value)

        move(a,b);
        move(b,a);

    }, cycle.value * 1000)
    up(a);
    up(b);
}

<template>
  <div class="buttonArea">
    <el-button plain class="playButton" @click="conductSorting" ><el-image class="buttonImage" :src="missionOnGoing? 'https://courseback.clankalliance.cn/static/inbuild/ds/pause.png':'https://courseback.clankalliance.cn/static/inbuild/ds/play.png'"/></el-button>
<!--    <el-button @click="sortOut">输出结果</el-button>-->
  </div>
  <el-scrollbar ref="scrollBarRef" style="width: 95%;height: 62vh">
    <div class="sortingArea" :style="{
      'width': `${Number(targetWidth) * (2 * heapWidth + 1)}px`,
      'height': `${Number(targetWidth) * (heapHeight * 2 + 1)}px`
    }">
      <canvas ref="canvas" class="baseCanvas"  :width="Number(targetWidth) * (2 * heapWidth + 1)" :height="Number(targetWidth) * (heapHeight * 2 + 1)" />
      <div class="numBlockWrap"  v-for="(item,index) in dataOnDisplay" :style="{
      'width': `${Number(targetWidth)}px`,
      'height': `${Number(targetWidth)}px`,
      'left': `${item.x}px`,
      'top': `${item.y}px`
    }">
        <div class="numBlock" :style="{
          'background-color':  'azure',
          'color': '#000'
        }">
          <a class="num">{{ item.value }}</a>
        </div>
      </div>
      <div class="numBlockWrap"  v-for="(item,index) in resultArray" :style="{
      'width': `${Number(targetWidth)}px`,
      'height': `${Number(targetWidth)}px`,
      'left': `${item.x}px`,
      'top': `${item.y}px`
    }">
        <div class="numBlock" :style="{
          'background-color': `${true? 'cornflowerblue': 'azure'}`,
          'color': `${true? '#FFF': '#000'}`
        }">
          <a class="num">{{ item.value }}</a>
        </div>
      </div>
    </div>
  </el-scrollbar>
  <a class="statusText">{{statusText}}</a>
</template>

<script lang="ts" setup>
import {
  cycle,
  dataOnDisplay, focusX, focusY, fps,
  targetWidth, transition, valueData, windowHeight, windowWidth,
} from "@/global/global";
import {onBeforeUnmount, onMounted, ref, watch} from "vue";
import {ElMessage, ElMessageBox, ElScrollbar} from "element-plus";
import type {Target} from "@/utils/complexAnimation";
import { move} from "@/utils/complexAnimation";

const scrollBarRef = ref<InstanceType<typeof ElScrollbar>>()

let watchScrollBar;

//为true: 建立大根堆 为false: 建立小根堆
let increasing = ref(true);

const missionOnGoing = ref(false);

const heapHeight = ref(0);

const heapWidth = ref(0.5);

const canvas = ref();

const resultArray = ref<Target[]>([])

const statusText = ref('已建立树，等待调整');

console.log(dataOnDisplay.value)


const drawLine = () => {
  const ctx = canvas.value.getContext('2d');
  for(let i = dataOnDisplay.value.length - 1;i > 0;i --){
    ctx.moveTo(dataOnDisplay.value[i].x + 0.5 * Number(targetWidth.value), dataOnDisplay.value[i].y + 0.5 * Number(targetWidth.value));
    ctx.lineTo(dataOnDisplay.value[Math.floor((i - 1) / 2)].x + 0.5 * Number(targetWidth.value), dataOnDisplay.value[Math.floor((i - 1) / 2)].y + 0.5 * Number(targetWidth.value))
    ctx.stroke();
  }
}

let initialized = false;

onMounted(() => {
  if(!initialized) {
    initialized = true;
    drawLine();
    ElMessageBox.confirm(
        '采用什么顺序输出结果？',
        {
          confirmButtonText: '升序排序',
          cancelButtonText: '降序排序'
        }
    ).then(() => {
      //升序排序
      increasing.value = true;
      heapInit();
    }).catch(() => {
      //降序排序
      increasing.value = false;
      heapInit();
    })
  }
})

const redraw = (index: number) => {
  refreshCanvas();
  drawLine();
  return 0.1;
}

const refreshCanvas = () => {
  canvas.value.width = canvas.value.width;
}

/**
 * 对dataOnDisplay中数据按照大根堆或小根堆来调整
 */
const heapInit = () => {
  for(let root = Math.floor(valueData.value.length / 2) - 1; root >= 0;root --){
    justify(root);
  }
}

/**
 * 以root为根的树的调整
 * @param root
 */
const justify = (root: number) => {
  let fatherIndex = root;
  let childIndex = (root + 1) * 2;
  if(childIndex === valueData.value.length) {
    if((increasing.value && valueData.value[fatherIndex] > valueData.value[childIndex - 1]) ||
        (!increasing.value && valueData.value[fatherIndex] < valueData.value[childIndex - 1])
    ){
      swapAction(0, 1);
    }
  }
  while(childIndex < valueData.value.length){
    if(increasing.value){
      if(valueData.value[childIndex - 1] < valueData.value[childIndex] || valueData.value[childIndex] === undefined){
        childIndex --;
      }
      if(valueData.value[fatherIndex] <= valueData.value[childIndex] || valueData.value[childIndex] === undefined)
        break;
    }else{
      if(valueData.value[childIndex - 1] > valueData.value[childIndex] || valueData.value[childIndex] === undefined){
        childIndex --;
      }
      if(valueData.value[fatherIndex] >= valueData.value[childIndex] || valueData.value[childIndex] === undefined)
        break;
    }
    swapAction(fatherIndex, childIndex);
    fatherIndex = childIndex;
    childIndex = (childIndex + 1) * 2;
  }
}

interface Mission {
  indexA: number,
  indexB: number,
  x: number,
  y: number,
  type: string,
}

const swapMission = (indexA: number, indexB: number): Mission => {
  return {
    indexA: indexA,
    indexB: indexB,
    x: null,
    y: null,
    type: 'swap',
  }
}

const moveMission = (index: number,x: number,y: number): Mission => {
  return {
    indexA: index,
    indexB: null,
    x: x,
    y: y,
    type: 'move',
  }
}

const redrawMission = (index: number): Mission => {
  return {
    indexA: index,
    indexB: null,
    x: null,
    y: null,
    type: 'redraw',
  }
}

const transportMission = (): Mission => {
  return {
    indexA: null,
    indexB: null,
    x: null,
    y: null,
    type: 'transport',
  }
}

for(let i = 1; i <= valueData.value.length;i *= 2){
  heapHeight.value ++;
  heapWidth.value *= 2;
}

//任务队列 先入先出
let missionQueue: Mission[] = [];

const executeMission = () => {
  if(!missionOnGoing.value){
    watchScrollBar();
    return;
  }
  const mission: Mission = missionQueue.shift();
  if(mission !== undefined){
    console.log('正常进入任务执行环节')
    switch (mission.type){
      case 'swap':
        statusText.value = '交换值为' + dataOnDisplay.value[mission.indexA].value + '和值为' + dataOnDisplay.value[mission.indexB].value + '的节点，使其满足最' + (increasing.value? '小':'大') + '堆';
        setTimeout(executeMission,swap(mission.indexA, mission.indexB) * cycle.value * 1000);
        break;
      case 'transport':
        setTimeout(executeMission,transport() * cycle.value * 1000);
        break;
      case 'move':
        statusText.value = '从堆中取出原本的根节点';
        setTimeout(executeMission,move(mission.indexA, mission.x, mission.y) * cycle.value * 1000);
        break;
      case 'redraw':
        setTimeout(executeMission,redraw(mission.indexA) * cycle.value * 1000);
        break;
    }
  }else{
    initialized = false;
    missionOnGoing.value = false;
    missionQueue.length = 0;
    watchScrollBar();
    ElMessage({
      message: '排序完成',
      type: 'success'
    })
    missionOnGoing.value = false;
    if(resultArray.value.length === 0)
      sortOut();
  }
}

const conductSorting = () => {
  if(missionOnGoing.value){
    missionOnGoing.value =false;
    watchScrollBar();
  }else{
    missionOnGoing.value = true;
    watchScrollBar = watch([focusX,focusY],(newValue,oldValue) => {
      scrollBarRef.value!.scrollTo(newValue[0] - windowWidth.value * 0.3,newValue[1] - windowHeight.value * 0.3);
    },{immediate: true})
    executeMission();
  }
}

const swap = (indexA: number, indexB: number): number => {
  let targetA: Target = dataOnDisplay.value[indexA];
  let targetB: Target = dataOnDisplay.value[indexB];
  setTimeout(() => {
    const xA = targetA.x;
    const yA = targetA.y;
    const vA = targetA.value;
    targetA.value = targetB.value;
    targetA.x = targetB.x;
    targetA.y = targetB.y;
    targetB.value = vA;
    targetB.x = xA;
    targetB.y = yA;
    dataOnDisplay.value[indexA] = targetA;
    dataOnDisplay.value[indexB] = targetB;
    console.log(dataOnDisplay.value)
  }, 1.1 * cycle.value * 1000)
  console.log('动画 交换' + indexA + '和' + indexB)
  console.log('数值 交换' + dataOnDisplay.value[indexA].value + '和' + dataOnDisplay.value[indexB].value)
  targetA = dataOnDisplay.value[indexA];
  targetB = dataOnDisplay.value[indexB];
  console.log(targetA)
  console.log(targetB)
  console.log('====================')
  move(indexB, targetA.x, targetA.y, false);
  move(indexA, targetB.x, targetB.y, false);
  focusX.value = (targetA.x + targetB.x) / 2;
  focusY.value = (targetA.y + targetB.y) / 2;
  return 1.2;
}

/**
 * 对data中两对象交换 并增加两对象位置交换的任务
 * @param indexA
 * @param indexB
 */
const swapAction = (indexA: number, indexB: number) => {
  missionQueue.push(swapMission(indexA, indexB))
  let vA = valueData.value[indexA];
  valueData.value[indexA] = valueData.value[indexB];
  valueData.value[indexB] = vA;
}

const pop = (x: number, y: number, hasFather: boolean) => {
  swapAction(0, valueData.value.length - 1);
  missionQueue.push(moveMission(valueData.value.length - 1, x, y));
  valueData.value.length --;
  missionQueue.push(transportMission());
  if(hasFather)
    missionQueue.push(redrawMission(valueData.value.length - 1));
  justify(0);

}

/**
 * 将dataOnDisplay[i]剪切到resultArray中
 * 出堆的时候需要
 */
const transport = () => {
  resultArray.value.push(dataOnDisplay.value[dataOnDisplay.value.length - 1])
  dataOnDisplay.value.length --;
  return 0.1;
}

const sortOut = () => {
  const length = dataOnDisplay.value.length;
  Math.pow(2,heapHeight.value - 1);
  const baseX: number = dataOnDisplay.value[Math.pow(2,heapHeight.value - 1) - 1].x;
  const baseY: number = dataOnDisplay.value[Math.pow(2,heapHeight.value - 1) - 1].y + Number(targetWidth.value);
  for(let i = 0;i < length;i ++){
    pop(baseX + Number(targetWidth.value) * i, baseY, i !== length - 1);
  }
}
onBeforeUnmount(() => {
  initialized = false;
  missionOnGoing.value = false;
  missionQueue.length = 0;
  watchScrollBar();
  transition();
})
</script>

<style scoped>
.sortingArea{
  position: relative;
}
.baseCanvas{
  position: absolute;
  left: 0;
  top: 0;
}
.numBlockWrap{
  position: absolute;

}
.numBlock{
  text-align: center;
  border-style: solid;
  border-width: 2px;
  border-radius: 1vh;
  display: flex;
  position: absolute;
  width: 80%;
  height: 90%;
}
.num{
  margin: auto;
  font-size: 3vh;
}
</style>

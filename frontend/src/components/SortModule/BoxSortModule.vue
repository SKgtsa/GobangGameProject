<template>
  <div class="buttonArea">
    <el-button v-if="!unboxing" plain class="playButton" @click="conductSorting" ><el-image class="buttonImage" :src="sortingStatus === 'noSorting' || sortingStatus === 'pausing'? 'https://courseback.clankalliance.cn/static/inbuild/ds/play.png':'https://courseback.clankalliance.cn/static/inbuild/ds/pause.png'"/></el-button>
  </div>
  <el-scrollbar ref="scrollBarRef" style="width: 90%;height: 62vh">
    <div class="sortingArea" :style="{
      'width': `${Number(targetWidth) * (data.length > boxList.length? data.length: boxList.length)}px`,
      'height': `${Number(targetWidth) * 2.9}px`
    }">
      <div class="numBlockWrap"  v-for="(item,index) in dataOnDisplay" :style="{
      'width': `${targetWidth}px`,
      'height': `${targetWidth}px`,
      'left': `${item.x}px`,
      'top': `${item.y}px`
    }" v-show="boxingOver <= index">
        <div class="numBlock" :style="{
          'background-color': `${iterator === index? 'cornflowerblue': 'azure'}`,
          'color': `${iterator === index? '#FFF': '#000'}`
        }">
          <a class="num">{{ item.value }}</a>
        </div>
      </div>
    </div>
    <div class="boxArea" :style="{
      'width': `${Number(targetWidth) * (data.length > boxList.length? data.length: boxList.length)}px`,
      'height': `${Number(targetWidth) * 3}px`
    }">
      <div class="numBlockWrap"  v-for="(item,index) in boxList" :style="{
        'width': `${targetWidth}px`,
        'height': `${targetWidth}px`,
        'left': `${item.x}px`,
        'top': `${item.y}px`
      }">
        <div class="numBlock" :style="{
          'background-color': `${true? 'cornflowerblue': 'azure'}`,
          'color': `${true? '#FFF': '#000'}`
        }">
          <a class="num">{{ item.value }} x {{item.order}}</a>
        </div>
      </div>
    </div>
  </el-scrollbar>
  <a class="statusText">{{statusText}}</a>
</template>

<script lang="ts" setup>
import {
  cycle,
  data,
  dataOnDisplay, focusX, focusY,
  targetWidth, transition, windowHeight, windowWidth,
} from "@/global/global";
import {onBeforeUnmount, ref, watch} from "vue";
import {ElMessage, ElMessageBox, ElScrollbar} from "element-plus";
import type {Target} from "@/utils/complexAnimation";
import {generateBox, move} from "@/utils/complexAnimation";

let clock = null;

let iterator = ref(-1);

let boxingOver = ref(-1);

let boxList = ref<Target[]>([]);

const scrollBarRef = ref<InstanceType<typeof ElScrollbar>>()

let watchScrollBar;

const statusText = ref('等待开始');

const unboxing = ref(false)

interface Mission {
  target: Target,
  type: string,
  x: number,
  y: number
}

let sortingStatus = ref('noSorting');
const conductSorting = () => {
  switch (sortingStatus.value){
    case 'noSorting':
      startBoxing();
      sortingStatus.value = 'boxing';
      break;
    case 'boxing':
      pause();
      sortingStatus.value = 'pausing';
      break;
    case 'pausing':
      if(boxingOver.value >= dataOnDisplay.value.length - 1){
        continueUnboxing();
        sortingStatus.value = 'unboxing';
      }else{
        continueBoxing();
        sortingStatus.value = 'boxing';
      }
      break;
    case 'unboxing':
      //正在从箱子里取数 组成新序列
      pauseUnboxing();
      sortingStatus.value = 'pausing';
      break;
  }
}

const updateIterator = () => {
  console.log('data.value.length= ' + data.value.length)
  console.log('iterator.value: ' + iterator.value);
  if(iterator.value === data.value.length - 1){
    ElMessage({
      message: '入箱完成',
      type: 'success'
    })
    dataOnDisplay.value.length = 0;
    boxingOver.value = 0;
    terminateBoxing();
  }else{
    boxingOver.value ++;
  }
  iterator.value ++;
}

const continueBoxing = () => {
  watchScrollBar = watch([focusX,focusY],(newValue,oldValue) => {
    scrollBarRef.value!.scrollTo(newValue[0] - windowWidth.value * 0.3,newValue[1] - windowHeight.value * 0.3);
  },{immediate: true})
  clock = setInterval(() => {
    statusText.value = '将序号为 ' + iterator.value + ', 值为 ' + dataOnDisplay.value[iterator.value].value + ' 的元素放入值对应的箱子';
    move(iterator.value, boxList.value[Number(dataOnDisplay.value[iterator.value].value)].x, windowHeight.value * 0.39);
    setTimeout(() => {
      boxList.value[Number(dataOnDisplay.value[iterator.value].value)].value ++;
    },1 * cycle.value * 1000);
    setTimeout(updateIterator ,1.4 * cycle.value * 1000)
  },1.5 * cycle.value * 1000)
}

const startBoxing = () => {
  iterator.value = 0;
  boxList.value = generateBox();
  continueBoxing();
}

const pause = () => {
  watchScrollBar();
  statusText.value = '暂停入箱'
  window.clearInterval(clock);
}

let missionQueue: Mission[] = [];

const terminateBoxing = () => {
  pause();
  unboxing.value = true;
  ElMessageBox.confirm(
      '采用什么顺序输出结果？',
      {
        confirmButtonText: '升序排序',
        cancelButtonText: '降序排序'
      }
  ).then(() => {
    //升序排序
    createMissionListAscending();
  }).catch(() => {
    //降序排序
    handleMissionListDescending();
  })

}

const createMissionListAscending = () => {
  let num = 0;
  for(let i = 0;i < boxList.value.length;i ++){
    num = handleBox(i, num);
  }
  console.log(missionQueue)
  setTimeout(continueUnboxing, cycle.value * 1000);
}

const handleMissionListDescending = () => {
  let num = 0;
  for(let i = boxList.value.length - 1;i >= 0;i --){
    num = handleBox(i, num);
  }
  console.log(missionQueue)
  setTimeout(continueUnboxing, cycle.value * 1000);
}

const handleBox = (i: number, num: number) => {
  for(let j = 1;j <= boxList.value[i].value; j ++){
    let newTarget: Target = {
      x: boxList.value[i].x,
      y: windowHeight.value * 0.39,
      value: boxList.value[i].order,
      order: num
    }
    //新建对象
    dataOnDisplay.value.push(newTarget)
    //任务队列末尾增加对象移动任务
    missionQueue.push({
      target: newTarget,
      type: 'move',
      x: (num + 0.5) * Number(targetWidth.value),
      y: windowHeight.value * 0.2
    })
    //任务队列末尾增加箱子计数自减任务
    missionQueue.push({
      target: boxList.value[i],
      type: 'degree',
      x: 0,
      y: 0
    })
    num ++;
  }
  return num;
}


const degree = (target: Target) => {
  boxList.value[target.order].value --;
  return 0;
}

const pauseUnboxingStatus = ref(false);

const continueUnboxing = () => {
  pauseUnboxingStatus.value = false;
  watchScrollBar = watch([focusX,focusY],(newValue,oldValue) => {
    scrollBarRef.value!.scrollTo(newValue[0] - windowWidth.value * 0.3,newValue[1] - windowHeight.value * 0.3);
  },{immediate: true})
  executeMission();
}

const executeMission = () => {
  if(pauseUnboxingStatus.value)
    return;
  const mission: Mission = missionQueue.shift();
  if(mission !== undefined){
    switch (mission.type){
      case 'move':
        statusText.value = '将' + mission.target.value + '号箱中的元素取出';
        setTimeout(executeMission,move(mission.target.order,mission.x,mission.y) * cycle.value * 1000);
        break;
      case 'degree':
        setTimeout(executeMission,degree(mission.target) * cycle.value * 1000);
        break;
    }
  }else{
    ElMessage({
      message: '排序完成',
      type: 'success'
    })
  }
}

const pauseUnboxing = () => {
  pauseUnboxingStatus.value = true;
  watchScrollBar()
  statusText.value = '暂停出箱';
}

onBeforeUnmount(() => {
  pauseUnboxing()
  unboxing.value = false;
  pause();
  missionQueue.length = 0;
  transition();
})
</script>

<style scoped>
.sortingArea{
  position: relative;
}
.boxArea{
  position: relative;
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
}
.num{
  margin: auto;
  font-size: 3vh;
}
</style>

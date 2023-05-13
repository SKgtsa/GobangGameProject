<template>
  <div class="buttonArea">
    <el-button plain class="playButton" @click="conductSorting" ><el-image class="buttonImage" :src="missionOnGoing? 'https://courseback.clankalliance.cn/static/inbuild/ds/pause.png':'https://courseback.clankalliance.cn/static/inbuild/ds/play.png'"/></el-button>
  </div>
  <el-scrollbar ref="scrollBarRef" style="width: 90%;height: 62vh">
    <div class="sortingArea" :style="{
      'width': `${Number(targetWidth) * (data.length * 2 + 1)}px`,
      'height': `${Number(targetWidth) * mergeHeight}px`
    }">
      <div class="numBlockWrap"  v-for="(item,index) in dataOnDisplay" :style="{
      'width': `${Number(targetWidth)}px`,
      'height': `${Number(targetWidth)}px`,
      'left': `${item.x}px`,
      'top': `${item.y}px`
    }">
        <div class="numBlock" :style="{
          'background-color': `${targetActivate[index]? 'cornflowerblue': 'azure'}`,
          'color': `${targetActivate[index]? '#FFF': '#000'}`
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
  data,
  dataOnDisplay, focusX, focusY, fps,
  targetWidth, transition, windowHeight, windowWidth,
} from "@/global/global";
import {onBeforeUnmount, onMounted, ref, watch} from "vue";
import {ElMessage, ElMessageBox, ElScrollbar} from "element-plus";
import type {Target} from "@/utils/complexAnimation";
import {batchMove, move} from "@/utils/complexAnimation";

const scrollBarRef = ref<InstanceType<typeof ElScrollbar>>()

let watchScrollBar;

let increasing = ref(true);

const missionOnGoing = ref(false);

const mergeHeight = ref(0);

const statusText = ref('等待开始');

let initialized = false;

onMounted(() => {
  if(!initialized){
    initialized = true;
    scrollBarRef.value!.scrollTo(Number(targetWidth.value) * (data.value.length * 2 + 1) / 2,Number(targetWidth.value) * mergeHeight.value / 2)
    console.log('onMount')
    ElMessageBox.confirm(
        '采用什么顺序输出结果？',
        {
          confirmButtonText: '升序排序',
          cancelButtonText: '降序排序'
        }
    ).then(() => {
      //升序排序
      increasing.value = true;
    }).catch(() => {
      //降序排序
      increasing.value = false;
    })
  }
})

interface Mission {
  start: number,
  end: number,
  centerOrder: number,
  x: number,
  y: number,
  type: string,
  isLeft: boolean
}

const targetActivate = ref([]);
for(let i = 0;i < dataOnDisplay.value.length;i ++){
  targetActivate.value[i] = false;
}

for(let i = 1; i <= data.value.length;i *= 2)
  mergeHeight.value ++;

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
      case 'batchMove':
        statusText.value = '分而治之'
        setTimeout(executeMission,batchMove(mission.start,mission.end,mission.x,mission.y,mission.centerOrder) * cycle.value * 1000)
        break;
      case 'move':
        statusText.value = dataOnDisplay.value[mission.start].value + '的值更' + (increasing.value? '小':'大') + ' 向上移动排入'
        setTimeout(executeMission,move(mission.start,mission.x,mission.y) * cycle.value * 1000)
        break;
      case 'activate':
        setTimeout(executeMission,changeColor(mission.start) * cycle.value * 1000)
        break;
    }
  }else{
    ElMessage({
      message: '排序完成',
      type: 'success'
    })
    statusText.value = '排序完成';
  }
}

const changeColor = (targetOrder: number) => {
  targetActivate.value[targetOrder] = !targetActivate.value[targetOrder];
  return 0.2;
}

const conductSorting = () => {
  if(missionOnGoing.value){
    missionOnGoing.value =false;
    watchScrollBar();
    statusText.value = '暂停排序';
  }else{
    missionOnGoing.value = true;
    watchScrollBar = watch([focusX,focusY],(newValue,oldValue) => {
      scrollBarRef.value!.scrollTo(newValue[0] - windowWidth.value * 0.3,newValue[1] - windowHeight.value * 0.3);
    },{immediate: true})
    data.value = merge({
      start: 0,
      end: dataOnDisplay.value.length - 1,
    }, dataOnDisplay.value[0].x,dataOnDisplay.value[0].y)
    statusText.value = '开始排序';
    executeMission();
  }
}

interface ArrayIndexTarget{
  start: number,
  end: number
}

const batchMoveDownMission = (start: number, end: number, centerOrder, left: boolean): Mission => {
  return {
    start: start,
    end: end,
    centerOrder: centerOrder,
    x: Number(targetWidth.value) * 0.5 * (left? -1:1),
    y: Number(targetWidth.value),
    type: 'batchMove',
    isLeft: null,
  }
}

const moveUpMission = (targetOrder: number, x: number, y: number): Mission => {
  return {
    start: targetOrder,
    end: null,
    centerOrder: null,
    x: x,
    y: y,
    type: 'move',
    isLeft: null,
  }
}

const activateMission = (targetOrder: number): Mission => {
  return {
    start: targetOrder,
    end: null,
    centerOrder: null,
    x: null,
    y: null,
    type: 'activate',
    isLeft: null,
  }
}

const merge = (array: ArrayIndexTarget, baseX: number, baseY: number): Target[] => {
  let targets: Target[] = new Array();
  if(array.start === array.end){
    return targets;
  }
  let arrayOldLeft: ArrayIndexTarget = {
    start: array.start,
    end: array.start + Math.floor((array.end - array.start) /2)
  }
  let arrayOldRight: ArrayIndexTarget = {
    start: arrayOldLeft.end + 1,
    end: array.end
  }
  missionQueue.push(batchMoveDownMission(arrayOldLeft.start,arrayOldLeft.end,arrayOldLeft.end,true));
  let targetsL: Target[] = merge(arrayOldLeft, baseX - 0.5 * Number(targetWidth.value),baseY + Number(targetWidth.value));
  missionQueue.push(batchMoveDownMission(arrayOldRight.start,arrayOldRight.end,arrayOldRight.start,false));
  let targetsR: Target[] = merge(arrayOldRight, baseX + (0.5 + arrayOldRight.start - arrayOldLeft.start) * Number(targetWidth.value),baseY + Number(targetWidth.value));
  for(let i = 0;i <= array.end - array.start;i ++){
    let target:Target;
    if((targetsL.length === 0) ||
        (targetsR[0] && increasing.value && targetsL[0].value > targetsR[0].value) || (targetsR[0] && !increasing.value && targetsL[0].value < targetsR[0].value)
    ){
      target = targetsR.shift();
    }else{
      target = targetsL.shift();
    }
    targets[i] = target;
    missionQueue.push(activateMission(target.order));
    missionQueue.push(moveUpMission(target.order,baseX + Number(targetWidth.value)*i ,baseY));
    missionQueue.push(activateMission(target.order));
  }
  return targets;
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

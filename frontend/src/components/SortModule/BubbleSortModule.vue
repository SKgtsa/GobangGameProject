<template>
  <div class="buttonArea">
    <el-button plain class="playButton" @click="conductSorting" ><el-image class="buttonImage" :src="sortingStatus === 'sorting'? 'https://courseback.clankalliance.cn/static/inbuild/ds/pause.png':'https://courseback.clankalliance.cn/static/inbuild/ds/play.png'"/></el-button>
    <el-switch
        v-model="increasing"
        active-text="升序排序"
        inactive-text="降序排序"
    />
    <el-button class="playButton" @click="terminate" v-if="sortingStatus !== 'noSorting'"><el-image class="buttonImage" src="https://courseback.clankalliance.cn/static/inbuild/ds/terminate.png"/></el-button>
  </div>
  <el-scrollbar ref="scrollBar" style="width: 90%;height: 62vh">
    <div class="sortingArea" :style="{
      'width': `${Number(targetWidth) * dataOnDisplay.length}px`,
      'height': `${Number(targetWidth) * 3}px`
    }">
      <div class="numBlockWrap"  v-for="(item,index) in dataOnDisplay" :style="{
      'width': `${targetWidth}px`,
      'height': `${targetWidth}px`,
      'left': `${targetPositionLeft[index]}px`,
      'top': `${targetPositionTop[index]}px`
    }">
        <div class="numBlock" :style="{
          'background-color': `${inIterator === index || inIterator + 1 === index? 'cornflowerblue': 'azure'}`,
          'color': `${inIterator === index || inIterator + 1 === index? '#FFF': '#000'}`
        }">
          <a class="num">{{ item }}</a>
        </div>
      </div>
    </div>
  </el-scrollbar>
  <a class="statusText">{{status}}</a>
</template>

<script lang="ts" setup>
import {
  cycle,
  data,
  dataOnDisplay,
  targetPositionLeft,
  targetPositionTop,
  targetWidth, transition,
} from "@/global/global";
import {swap} from "@/utils/simpleAnimation";
import {onBeforeUnmount, ref} from "vue";
import {ElMessage, ElScrollbar} from "element-plus";
let scrollBar = ref<InstanceType<typeof ElScrollbar>>()
let oldData = [];

let clock = null;

const status = ref('等待开始');

let outIterator = ref(0);
let inIterator = ref(-2);

let increasing = ref(true);

let sortingStatus = ref('noSorting');
const conductSorting = () => {
  switch (sortingStatus.value){
    case 'noSorting':
      startSorting();
      sortingStatus.value = 'sorting';
      break;
    case 'sorting':
      pause();
      sortingStatus.value = 'pausing';
      break;
    case 'pausing':
      continueSorting();
      sortingStatus.value = 'sorting';
      break;
  }
}

const orderedNum = ref(0);

const updateIterator = () => {
  if(inIterator.value === data.value.length - 2){
    inIterator.value = 0;
    console.log('orderedNum: ' +  orderedNum.value)
    if(orderedNum.value === data.value.length - 1){
      ElMessage({
        message: '排序完成',
        type: 'success'
      })
      status.value = '排序完成'
      //终止
      terminate();
    }else{
      outIterator.value ++;
      orderedNum.value = 0;
    }
  }else{
    inIterator.value ++;
  }
}

const continueSorting = () => {
  clock = setInterval(() => {
    scrollBar.value!.scrollTo(targetPositionLeft.value[inIterator.value],targetPositionLeft.value[inIterator.value])
    if((increasing.value && Number(data.value[inIterator.value]) > Number(data.value[inIterator.value + 1])) ||
        (!increasing.value && Number(data.value[inIterator.value]) < Number(data.value[inIterator.value + 1]))
    ){
      orderedNum.value = 0;
      swap(inIterator.value,inIterator.value + 1);
      status.value = '顺序错误，进行交换'
    }else{
      orderedNum.value ++;
      status.value = '顺序正确，无需交换'
    }
    setTimeout(updateIterator ,3.1 * cycle.value * 1000)
  },3.2 * cycle.value * 1000)
}

const startSorting = () => {
  oldData = data.value;
  inIterator.value = 0;
  continueSorting();
  console.log(data.value)
}

const pause = () => {
  window.clearInterval(clock);
  status.value = '排序暂停'
}

const terminate = () => {
  pause()
  inIterator.value = -2;
  outIterator.value = 0;
}

onBeforeUnmount(() =>{
  window.clearInterval(clock);
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
}
.num{
  margin: auto;
  font-size: 3vh;
}
</style>

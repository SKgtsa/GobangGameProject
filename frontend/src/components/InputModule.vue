<template>
  <div class="inputContainer">
    <a class="title">输入</a>
    <a class="description">{{rawData.length === 0? '请在此处以\',\'为间隔输入数组': (sortMethod === '0'? '请选择排序方式':'请点击下方按钮开始排序') }}</a>
    <div class="inputPart">
      <el-input class="inputLine" :autosize="{ minRows: 1, maxRows: 5 }" type="textarea" v-model="rawData" placeholder="请在此输入数据"/>
      <el-button class="randomButton" @click="generateRandom">随机</el-button>
    </div>
    <div class="buttonWrapper">
      <el-button class="startButton" @click="startSorting" v-if="sortMethod !== '0' && rawData.length !== 0">开始排序</el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {data, dataOnDisplay, sortMethod, transition} from "@/global/global";
import {ref} from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";

const rawData = ref('')

const generateRandom = () => {
  const length = Math.floor(Math.random() * 20);
  rawData.value = '';
  for(let i = 1;i <= length;i ++){
    if(i != 1)
      rawData.value += ',';
    rawData.value += Math.floor(Math.random() * 30);
  }
}

const rawDataValid = ():boolean => {
  const newData = rawData.value.split(',');
  if(newData.length === 0)
    return false;
  for(let i = 0;i < newData.length;i ++){
    try {
      let num:number = Number(newData[i]);
    }catch (e){
      return false;
    }
  }
  return true;
}

const startSorting = () => {
  if(!rawDataValid()){
    ElMessage({
      message: '输入不规范',
      type: 'error'
    })
    return;
  }
  transition();
  switch (sortMethod.value){
    case '1':
      dataOnDisplay.value = rawData.value.split(',');
      data.value = dataOnDisplay.value;
      rePosition()
      router.push('/sort/bubbleSort')
      break;
    case '2':
      dataOnDisplay.value = transform(rawData.value.split(',') as any);
      data.value = dataOnDisplay.value;
      router.push('/sort/boxSort')
      break;
    case '3':
      dataOnDisplay.value = transformForHeap(rawData.value.split(',') as any);
      data.value = dataOnDisplay.value;
      router.push('/sort/heapSort')
      break;
    case '4':
      dataOnDisplay.value = transformForMerge(rawData.value.split(',') as any);
      data.value = dataOnDisplay.value;
      console.log(dataOnDisplay)
      router.push('/sort/mergeSort')
      break;
  }
}

</script>

<style scoped>
.inputContainer{
  display: flex;
  flex-direction: column;
}
.title{
  font-size: 8vh;
  color: azure;
}
.description{
  font-size: 2vh;
}
.inputLine{
  font-size: 2vh;
}
.description{
  font-size: 2vh;
  line-height: 4vh;
  font-weight: normal;
  color: #313131;
}
.buttonWrapper{
  width: 100%;
  display: flex;
  flex-direction: row-reverse;
  padding-top: 2vh;
}
.startButton{
  height: 6vh;
  font-size: 3vh;
  background-color: rgba(0,0,0,0);
  border-width: 0.6vh;
  border-color: azure;
  border-radius: 1.5vh;
  color: azure;
}
.inputPart{
  display: flex;
  flex-direction: row;
}
.randomButton{
  height: 4vh;
}
</style>

<script setup lang="ts">

import {mobile, windowHeight, windowWidth} from "@/global/global";
import {rePosition} from "@/utils/simpleAnimation";

const getWindowResize = function () {
  const originalHeight = windowHeight.value;
  windowWidth.value = window.innerWidth;
  windowHeight.value = window.innerHeight;
  if(
      navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
      || window.innerWidth / window.innerHeight <= 1
  ){
    mobile.value = true;
  }else{
    mobile.value = false;
  }
  if(originalHeight != windowHeight.value){
    rePosition();
  }
}


getWindowResize()
window.addEventListener('resize',getWindowResize)


</script>

<template>
          <router-view />
</template>

<style scoped>
html,body,el-container,el-main,el-footer,el-aside,router-view{
  /*设置内部填充为0，几个布局元素之间没有间距*/
  padding: 0px;
  /*外部间距也是如此设置*/
  margin: 0;
  /*统一设置高度为100%*/
  height: 100%;
  width: 100vw !important;
}
</style>

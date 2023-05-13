<template>
  <transition :name="mobile? 'el-zoom-in-top':'el-zoom-in-left'">
    <el-menu
        :default-active="sortMethod"
        @select = "handleSelect"
        :ellipsis="false"
        class="navBar"
        :mode="mobile? 'horizontal':'vertical'"
        v-if="showPage"
        :style="{
          'width': `${mobile? '100vw':'30vw'}`
        }"
    >
      <div>
        <div class="dot"/>
        <el-menu-item class="name" index="0" style="font-size: 14vh;height: 18vh">排序</el-menu-item>
      </div>
      <div class="flex-grow"/>
      <div>
        <el-menu-item index="1" class="item">冒泡排序</el-menu-item>
        <el-menu-item index="2" class="item">箱子排序</el-menu-item>
        <el-menu-item index="3" class="item">堆排序</el-menu-item>
        <el-menu-item index="4" class="item">归并排序</el-menu-item>
      </div>
    </el-menu>
  </transition>
</template>

<script lang="ts" setup>

import {mobile, showPage, sortMethod} from "@/global/global";
import {onMounted} from "vue";

const handleSelect = (key: string) => {
  console.log('将sortMethod设置为' + key)
  sortMethod.value = key;
}

onMounted(() => {
  showPage.value = true;
})

</script>

<style scoped>
.name{
  z-index: 10;
}
.dot{
  width: 12vh;
  height: 12vh;
  border-radius: 6vh;
  z-index: 1;
  background-color: rgba(255,200,90,0.8);
  position: absolute;
  top: 4vh;
  left: 6vh;
}
.flex-grow{
  flex-grow: 1;
}
.navBar{
  border-style: none;
  z-index: 100;
  background-color: rgba(0,0,0,0);
}
.item{
  font-size: 3vh;
  font-weight: normal;
  height: 4vh;
}
</style>

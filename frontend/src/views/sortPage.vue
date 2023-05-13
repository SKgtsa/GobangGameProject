<template>
  <div class="mainContainer">
    <el-drawer z-index="1" :with-header="false" :modal-append-to-body="false" class="drawer" before-close="forbiddenClose" v-model="showPage" :size="'100%'" :modal="false" :show-close="false">
      <div class="cardWrap" v-if="showPage">
        <div class="card">
          <router-view/>
          <el-button @click="back">更换数据</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
  import router from "@/router";
  import {showPage, transition} from "@/global/global";

  const back = () => {
    transition();
    router.push('/')
  }
</script>

<style scoped>
.mainContainer{
  display: flex;
  flex-direction: row;
  width: 100vw;
}
.cardWrap{
  padding-top: 10vh;
}
.card{
  width: 90vw;
  height: 75vh;
}
/deep/.el-drawer.rtl {
  background: rgba(0,0,0,0);
  box-shadow: rgba(0,0,0,0) 0 0 0;
  margin: 0 -50px;
}
</style>

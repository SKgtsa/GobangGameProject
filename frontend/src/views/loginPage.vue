<template>
  <div class="mainContainer" >
    <div class="loginWindow" :style="{
      'width': `${mobile? 90:40}vw`
    }" >
      <button class="switchButton" @click="passwordLogin = !passwordLogin" >{{passwordLogin? "验证码登录":"密码登录"}}</button>
      <div class="inputArea">
        <el-input class="input" v-model="phone" placeholder="手机"/>
        <el-input show-password class="input" v-if="passwordLogin" v-model="password" placeholder="密码"/>
        <div class="verifyArea"  v-if="!passwordLogin">
          <el-input class="input" v-model="verifyCode" placeholder="验证码"/>
          <button class="verifyButton" @click="sendCode" >{{codeSend? (codeSecRemain + '秒'):'发送验证码'}}</button>
        </div>
      </div>
      <button @click="showRegister = !showRegister" class="registerButton" >
        注册
      </button>
      <button @click="submitLogin" class="loginButton" >登录</button>
    </div>
  </div>

  <el-drawer
      v-model="showRegister"
      title="用户注册"
      :direction="'ttb'"
      size="60%"
  >
    <div class="registerForm" >
      <el-input class="input" v-model="registerInfo.nickName" placeholder="昵称"/>
      <el-switch
          v-model="registerInfo.gender"
          size="large"
          active-text="女"
          inactive-text="男"
      />
      <el-input class="input" v-model="registerInfo.phone" placeholder="手机"/>
      <el-input class="input" show-password v-model="registerInfo.password" placeholder="密码"/>
      <div class="verifyArea">
        <el-input class="input" v-model="registerInfo.code" placeholder="验证码"/>
        <button class="verifyButton" @click="sendCodeRegister" >{{codeSend? (codeSecRemain + '秒'):'发送验证码'}}</button>
      </div>
    </div>
    <el-button class="submitButton" @click="submitRegister" >提交</el-button>
  </el-drawer>

</template>

<script lang="ts" setup>
import {mobile} from "@/global/global";
import {reactive, ref} from "vue";
import service from "@/request";
import {ElMessage} from "element-plus";
import router from "@/router";
import {isNumber} from "@/utils/commonUtils";

const passwordLogin = ref(false);

const phone = ref('');
const password = ref('');
const verifyCode = ref('');

const codeSend = ref(false);
const codeSecRemain = ref(10);

let timer = null;

const showRegister = ref(false);

const registerInfo = reactive({
  nickName: '',
  password: '',
  phone: '',
  gender: false,
  code: ''
})

const submitRegister = () => {
  if(
      registerInfo.phone == ''
      || registerInfo.nickName == ''
      || registerInfo.password == ''
      || registerInfo.code == ''
  ){
    ElMessage({
      message: '请填写完整',
      type: 'error'
    })
    return;
  }else if(!isNumber(registerInfo.phone)){
    ElMessage({
      message: '请填写正常手机号',
      type: 'error'
    })
    return;
  }
  service.post("/api/user/register", registerInfo).then((res) => {
    if(res.data.success){
      ElMessage({
        message: '注册成功',
        type: 'success'
      })
    }else{
      ElMessage({
        message: res.data.message,
        type: 'error'
      })
    }
  })
}

const submitLogin = () => {
  if(phone.value == ''){
    ElMessage({
      message: '请填写完整',
      type: 'error'
    })
    return;
  }else if(!isNumber(phone.value)){
    ElMessage({
      message: '请填写正常手机号',
      type: 'error'
    })
    return;
  }
  switch (passwordLogin.value){
    case true:
      if(password.value == ''){
        ElMessage({
          message: '请填写完整',
          type: 'error'
        })
        return;
      }
      service.post("/api/user/login",{phone: phone.value, password: password.value}).then((res) => {
        if(res.data.success){
          ElMessage({
            message: '登录成功',
            type: 'success'
          })
          console.log(res)
          localStorage.setItem('token',res.data.token);
          console.log()
          router.push('/game');
        }else{
          ElMessage({
            message: res.data.message,
            type: 'error'
          })
        }
      })
      break;
    case false:
      if(verifyCode.value == ''){
        ElMessage({
          message: '请填写完整',
          type: 'error'
        })
        return;
      }
      service.post("/api/user/loginCode",{phone:phone.value, code: verifyCode.value}).then((res) => {
        console.log(res);
        if(res.data.success){
          ElMessage({
            message: '登录成功',
            type: 'success'
          })
          localStorage.setItem('token',res.data.token);
          router.push('/game');
        }else {
          ElMessage({
            message: res.data.message,
            type: 'error'
          })
        }
      })
      break;
  }
}

const updateTime = () => {
  codeSecRemain.value --;
  if(codeSecRemain.value === 0){
    codeSecRemain.value = 60;
    codeSend.value = false;
    clearInterval(timer);
  }
  localStorage.setItem('codeSend',codeSend.value + '');
  localStorage.setItem('codeSec',codeSecRemain.value + '');
}

const codeSendInitial = () => {
  let temp = localStorage.getItem("codeSend");
  let tempSec = localStorage.getItem("codeSec")
  if(temp === 'true')
    codeSend.value = true;
  if(tempSec != undefined)
    codeSecRemain.value = Number(tempSec);
  if(codeSend.value)
    timer = setInterval(updateTime, 1000);

}
codeSendInitial();

const sendCode = () => {
  if(codeSend.value)
    return;
  if(phone.value == ''){
    ElMessage({
      message: '请填写完整',
      type: 'error'
    })
    return;
  }else if(!isNumber(phone.value)){
    ElMessage({
      message: '请填写正常手机号',
      type: 'error'
    })
    return;
  }
  codeSend.value = true;
  service.post(
      "/api/user/loginPhone",
      {
        phone: phone.value
      }).then((res) => {
        if(res.data.success){
          timer.value = setInterval(updateTime, 1000)
          ElMessage({
            message: '发送成功',
            type: 'success'
          })
        }else{
          ElMessage({
            message: '用户不存在',
            type: 'error'
          })
        }
  })
}

const sendCodeRegister = () => {
  if(codeSend.value)
    return;
  if(phone.value == ''){
    ElMessage({
      message: '请填写完整',
      type: 'error'
    })
    return;
  }else if(!isNumber(phone.value)){
    ElMessage({
      message: '请填写正常手机号',
      type: 'error'
    })
    return;
  }
  codeSend.value = true;
  service.post(
      "/api/user/registerPhone",
      {
        phone: registerInfo.phone
      }).then((res) => {
    if(res.data.success){
      timer.value = setInterval(updateTime, 1000)
      ElMessage({
        message: '发送成功',
        type: 'success'
      })
    }else{
      ElMessage({
        message: '用户不存在',
        type: 'error'
      })
    }
  })
}

</script>

<style scoped>
.mainContainer{
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-image: url("../assets/images/background.gif");
  background-repeat: repeat;
  background-size: 25vh;
}
.loginWindow{
  height: auto;
  background-color: azure;
  display: flex;
  margin: auto;
  flex-direction: column;
}
.switchButton{
  text-align: left;
  font-size: 5vh;
  height: auto;
  background-color: dodgerblue;
  color: azure;
}
.switchButton:hover{
  background-color: #599af8;
}
.inputArea{
  height: 15vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}
.input{
  margin: auto;
  height: 5vh;
  font-size: 3vh;
}
.loginButton{
  font-size: 5vh;
  background-color: dodgerblue;
  color: azure;
  text-align: center;

}
.loginButton:hover{
  background-color: #599af8;
}
.verifyArea{
  display: flex;
  flex-direction: row;
}
.verifyButton{

}
.registerButton{
  height: 6vh;
  text-align: center;
  color: darkslategrey;
  background-color: rgba(0,0,0,0);
  border-style: none;
}
.registerButton:hover{
  color: #599af8;
}
.registerForm{
  width: 100%;
  height: 30vh;
  display: flex;
  flex-direction: column;
}
.submitButton{
  margin: auto;
}
</style>

<template>
  <div class="mainContainer" >
    <div class="userArea" :style="{
      'width': `${mobile? 0.5 * windowWidth:0.5 * windowHeight}px`,
      'height': `${mobile? 0.5 * windowWidth:0.5 * windowHeight}px`,
      'left': `${mobile? 'auto': '0px'}`,
      'top': `${mobile? 'auto': '0px'}`,
      'right': `${mobile? '0px': 'auto'}`,
      'bottom': `${mobile? '0px': 'auto'}`
    }">
      <div class="userAreaIn">
        <div class="userTop">
          <div class="avatarArea">
            <div class="avatar">
              <el-image class="avatarImage" :src="baseURL + userinfo.avatarURL" />
            </div>
          </div>
          <div class="record">
            <div :style="{
          'font-size': `${mobile? 0.04 * windowWidth:0.04 * windowHeight}px`
        }" class="recordText" >胜:{{userinfo.winNum}}场</div>
            <div :style="{
          'font-size': `${mobile? 0.04 * windowWidth:0.04 * windowHeight}px`
        }" class="recordText" >败:{{userinfo.loseNum}}场</div>
          </div>
        </div>
        <div class="userBottom" :style="{
          'font-size': `${mobile? 0.08 * windowWidth:0.08 * windowHeight}px`
        }">
          <a>{{userinfo.nickName}}</a>
        </div>
      </div>
    </div>
    <div @click="onClick3D" class="three" id="my-three" :style="{
      // 'z-index': `${processStatus === 0? 50:150}`
      'z-index': '150'
    }" ></div>
    <div class="onlineArea"  :style="{
      'width': `${mobile? windowWidth:(windowWidth - 0.5 * windowHeight)}px`,
      'height': `${mobile? (windowHeight - 0.5 * windowWidth):windowHeight}px`
    }">
      <el-button class="quickGame" @click="handleStartMatch" >
        <div class="buttonInside" >
          <a class="buttonBigText" >快速游戏</a>
          <a class="buttonSmallText" >与随机玩家对战</a>
        </div>
      </el-button>
      <el-button class="inviteGame" @click="inputCode" >
        <div class="buttonInside" >
          <a class="buttonBigText" >邀请游戏</a>
          <a class="buttonSmallText" >使用邀请码进入房间</a>
        </div>
      </el-button>
      <el-button class="createPrivate" @click="handleCreatePrivate" >
        <div class="buttonInside" >
          <a class="buttonBigText" >创建房间</a>
          <a class="buttonSmallText" >创建私人房间</a>
        </div>
      </el-button>
    </div>
  </div>
  <div class="mask" v-if="waiting" >
    <div class="maskIn">
      <div class="maskWindow" :style="{
        'width': `${mobile? 90: 30}vw`
      }">
        <div class="waitingText">正在等待玩家</div>
        <div class="waitingText">房间号:{{roomCode}}</div>
        <el-image class="loadIcon" :src="baseURL + '/static/inbuild/loading.gif'"/>
        <div class="quit" @click="handleQuit" >退出</div>
      </div>
    </div>
  </div>
  <transition name="el-zoom-in-top">
    <div class="rivalGameArea" v-show="processStatus === 2">
      <transition name="el-zoom-in-top">
        <div class="roundNote" :style="{
          'width': `${mobile? 40:20}vw`,
          'top': '4vh',
          'left': '55vw'
        }" v-show="!myTurn" style="background-color: dodgerblue" ><div class="roundText">对方回合</div></div>
      </transition>
      <div class="gameTextBarTop">
        <a class="gameText" style="top: 8vh;left: 55vw" >{{rivalinfo.nickName}}</a>
      </div>
      <div class="gameAvatar" style="right: 55vw; top: 3vh;border-color: dodgerblue">
        <el-image class="gameAvatarImage" :src="baseURL + rivalinfo.avatarURL" />
      </div>
    </div>
  </transition>
  <transition name="el-zoom-in-bottom">
    <div class="userGameArea" v-show="processStatus === 2">
      <transition name="el-zoom-in-bottom">
        <div class="roundNote" :style="{
          'width': `${mobile? 40:20}vw`,
          'bottom': '4vh',
          'right': '55vw'
        }" v-show="myTurn" style="background-color: #d14845" ><div class="roundText">我方回合</div></div>
      </transition>
      <div class="gameTextBarBottom">
        <a class="gameText" style="bottom: 8vh;right: 55vw">{{userinfo.nickName}}</a>
      </div>
      <div class="gameAvatar" style="left: 55vw; bottom: 3vh; border-color: #d14845">
        <el-image class="gameAvatarImage" :src="baseURL + userinfo.avatarURL" />
      </div>
    </div>
  </transition>
<!--  <el-dialog v-model="showInputCode" title="接收邀请">-->
<!--    <el-input v-model="roomCode" placeholder="请输入房间号" />-->
<!--  </el-dialog>-->
</template>

<script lang="ts" setup type="module">
import {getBaseURL, mobile, windowHeight, windowWidth} from "@/global/global";
import * as THREE from 'three'

import {reactive, ref} from "vue";
import {_debounce} from "@/utils/throTtle";
import router from "@/router";
import {ElMessage, ElMessageBox} from "element-plus";
const boardWidth = ref(350);
let renderer = null;
let camera = null;
let scene = null;
let white = false;//为true,玩家下白棋
const processStatus = ref(0);//0开始 1过渡 2游戏

const forbiddenClose = (done) => {}

const transTime = ref(5000);

const startPosition = reactive({x: 0, y: 0, z: 0});

const targetPosition = reactive({x: 0, y: 0, z: 0})

const startTime = ref(0);

const roomCode = ref('');

const waiting = ref(false);

const baseURL = getBaseURL();

const myTurn = ref(false);

const startSize = reactive({
  w: 0,
  h: 0
})

const userinfo = reactive({
  nickName: '',
  winNum: 0,
  loseNum: 0,
  id: '',
  avatarURL: ''
})

const rivalinfo = reactive({
  nickName: '',
  winNum: 0,
  loseNum: 0,
  id: '',
  avatarURL: ''
})

//摄像机旋转平面y轴与坐标系y轴夹角 度
const rotateFlatAngle = 30;
//摄像机当前旋转到的角度 度
let rotateAngle = 0;
//旋转半径
const r = 400;
//游戏时镜头位置
const position = 450;
const PI = Math.PI;
let animation;
//摄像机移动速度
const movePace = 1;
//区域扩大速度
let enlargePaceW = 5;
let enlargePaceH = 5;
const rendererSize = reactive({
  width: 10,
  height: 10
})
//棋盘 null无棋 false黑棋 true白棋
const logicalBoard = [];

const clearBoard = () => {
  for(let i = 0;i < 15;i ++){
    logicalBoard[i] = [];
    for(let j = 0;j < 15;j ++){
      logicalBoard[i][j] = null;
    }
  }
}

clearBoard();

const clearScene = () => {
  cancelAnimationFrame(animation);
  console.log('clear')
  scene.traverse((child) => {
    if (child.material) {
      child.material.dispose();
    }
    if (child.geometry) {
      child.geometry.dispose();
    }
    child = null;
  });
  renderer.setSize(0,0);
  renderer.dispose();
  renderer.clear();
  scene.clear();
  scene = null;
  camera = null;
  renderer.domElement = null;
  renderer = null;
}

const rotateUpdate = () => {
  const angle = PI * rotateAngle / 180;
  const angleFlat = PI * rotateFlatAngle / 180;
  camera.position.set(r * Math.cos(angle),r * Math.sin(angle) * Math.cos(angleFlat),r * Math.sin(angle) * Math.sin(angleFlat));
  camera.lookAt(0,0,0);
  renderer.render(scene,camera)
  document.getElementById('my-three')?.appendChild(renderer.domElement)
  rotateAngle += 0.3;
  animation = requestAnimationFrame(rotateUpdate);
}

const drawLine = (color) => {
  for(let i = 0;i < 15;i ++){
    let row = [];
    let column = [];
    row.push(new THREE.Vector3(boardWidth.value / 2, (i - 7) * boardWidth.value / 14, boardWidth.value / 19));
    row.push(new THREE.Vector3(-boardWidth.value / 2, (i - 7) * boardWidth.value / 14, boardWidth.value / 19));
    column.push(new THREE.Vector3((i - 7) * boardWidth.value / 14, boardWidth.value / 2,boardWidth.value / 19));
    column.push(new THREE.Vector3((i - 7) * boardWidth.value / 14, -boardWidth.value / 2,boardWidth.value / 19));
    const geoRow = new THREE.BufferGeometry().setFromPoints(row);
    const geoCol = new THREE.BufferGeometry().setFromPoints(column);
    const material = new THREE.LineBasicMaterial( { color: color } );
    const lineR = new THREE.Line( geoRow, material );
    scene.add( lineR );
    const lineC = new THREE.Line( geoCol, material );
    scene.add( lineC );
  }
}

const initialRedraw = () => {
  const length = (mobile.value? windowWidth.value:windowHeight.value) * 0.5;
  scene = new THREE.Scene();
  camera = new THREE.PerspectiveCamera(45, 1, 1, 1000);
  renderer = new THREE.WebGLRenderer()
  renderer.setSize(length,length)
  rendererSize.width = length;
  rendererSize.height = length;
  drawLine(0x00ff00);
  rotateUpdate();
}

const transactionRedraw = () => {
  const width = window.innerWidth, height = window.innerHeight;
  scene = new THREE.Scene();
  camera = new THREE.PerspectiveCamera(45, width/height, 1, 1000);
  camera.position.set(0,-1 * position,position);
  camera.lookAt(0,0,0);
  renderer = new THREE.WebGLRenderer()
  renderer.setSize(width,height)
  drawLine(0x00ff00);
}

const inGameRedraw = () => {
  console.log('ingame')
  scene = new THREE.Scene();
  const geometry = new THREE.BoxGeometry(boardWidth.value * 1.2, boardWidth.value * 1.2, boardWidth.value / 10);
  const material = new THREE.MeshLambertMaterial({
    color: 0xDDDD99,
    transparent: false
  });
  const mesh = new THREE.Mesh(geometry, material);
  scene.add(mesh);

  const backGeo = new THREE.BoxGeometry(boardWidth.value * 10, boardWidth.value * 10, 2);
  const backMat = new THREE.MeshLambertMaterial({
    color: 0xffffff,
    transparent: false
  });
  const background = new THREE.Mesh(backGeo, backMat);
  background.position.set(0,0,)
  scene.add(background);

  let spotLight = new THREE.SpotLight(0xfffaee)
  spotLight.position.set(0,0,position / 3);
  spotLight.castShadow = true;
  spotLight.intensity = 0.8
  scene.add(spotLight);
  const width = window.innerWidth, height = window.innerHeight;
  camera = new THREE.PerspectiveCamera(45, width/height, 1, 1000);
  camera.position.set(0,-1 * position,position);
  camera.lookAt(0,0,0);
  renderer = new THREE.WebGLRenderer()
  renderer.setSize(width,height)
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type=THREE.PCFSoftShadowMap;
  drawLine(0x000000);
  for(let i = 0;i < 15;i ++){
    for(let j = 0;j < 15;j ++){
      if(logicalBoard[i][j] != null){
        addPiece(j,i,logicalBoard[i][j]);
      }
    }
  }
}

const addPiece = (x,y,white,needMark=false) => {
  const  pieceGeometry = new THREE.SphereGeometry(5, 4, 4);
  const  pieceMaterial = new THREE.MeshLambertMaterial({
    color: white? 0xffffff:0x000000
  });
  const pieceMesh = new THREE.Mesh(pieceGeometry, pieceMaterial);
  pieceMesh.position.set(x * boardWidth.value / 14 - boardWidth.value / 2,y * boardWidth.value / 14 - boardWidth.value / 2,boardWidth.value / 20);
  scene.add(pieceMesh);
  renderer.render(scene,camera);
}

const redrawPosition = () => {
  const length = Math.min(windowWidth.value, windowHeight.value) / 4;
  boardWidth.value = length;
  if(scene !== null)
    clearScene();
  switch (processStatus.value){
    case 0:
      initialRedraw();
      break;
    case 1:
      transactionRedraw();
      renderer.render(scene,camera)
      document.getElementById('my-three')?.appendChild(renderer.domElement)
      break;
    case 2:
      inGameRedraw();
      renderer.render(scene,camera)
      document.getElementById('my-three')?.appendChild(renderer.domElement)
      break;
  }
}

const startTrans = () => {
  cancelAnimationFrame(animation);
  startSize.w = rendererSize.width;
  startSize.h = rendererSize.height;
  startTime.value = new Date().getTime()
  if(processStatus.value === 0) {
    processStatus.value = 1;
    startPosition.x = camera.position.x;
    startPosition.y = camera.position.y;
    startPosition.z = camera.position.z;
    targetPosition.x=0;
    targetPosition.y=-1*position;
    targetPosition.z=position;
  }else{
    processStatus.value = 3;
    const angle = PI * rotateAngle / 180;
    const angleFlat = PI * rotateFlatAngle / 180;
    startPosition.x = camera.position.x;
    startPosition.y = camera.position.y;
    startPosition.z = camera.position.z;
    targetPosition.x=r * Math.cos(angle);
    targetPosition.y=r * Math.sin(angle) * Math.cos(angleFlat);
    targetPosition.z=r * Math.sin(angle) * Math.sin(angleFlat);
  }
  transUpdate();
}

const transUpdate = () => {
  console.log('transUpdate')
  let x,y,z, xT=targetPosition.x,yT=targetPosition.y,zT=targetPosition.z, xS=startPosition.x, yS=startPosition.y,zS=startPosition.z;
  let finishRate = (new Date().getTime() - startTime.value) / transTime.value;
  console.log(finishRate);
  console.log(startTime.value);
  if(finishRate >= 1){
    finishRate = 1;
  }
  x = xS + finishRate * (xT - xS);
  y = yS + finishRate * (yT - yS);
  z = zS + finishRate * (zT - zS);
  let w,h;
  if(processStatus.value === 1){
    w = startSize.w + (windowWidth.value - startSize.w) * finishRate;
    h = startSize.h + (windowHeight.value - startSize.h) * finishRate;
  }else{
    const length = (mobile.value? windowWidth.value:windowHeight.value) * 0.5;
    w = startSize.w + (length - startSize.w) * finishRate;
    h = startSize.h + (length - startSize.h) * finishRate;
  }
  camera = new THREE.PerspectiveCamera(45, w/h, 1, 1000);
  camera.position.set(x,y,z);
  camera.lookAt(0,0,0);
  renderer.setSize(w,h);
  rendererSize.width = w;
  rendererSize.height = h;
  renderer.render(scene,camera)
  document.getElementById('my-three')?.appendChild(renderer.domElement)
  if(finishRate === 1){
    console.log('transOver')
    cancelAnimationFrame(animation);
    if(processStatus.value === 1) {
      processStatus.value = 2;
    }else {
      processStatus.value = 0;
      clearBoard();
    }
    setTimeout(redrawPosition,800)
    return;
  }
  animation = requestAnimationFrame(transUpdate);
}

const onClick3D = (e) => {
  if(myTurn.value){
    let vector = new THREE.Vector3();//三维坐标对象
    vector.set(
        ( e.clientX / window.innerWidth ) * 2 - 1,
        - ( e.clientY / window.innerHeight ) * 2 + 1,
        0.5 );
    vector.unproject( camera );
    let rayCaster = new THREE.Raycaster(camera.position, vector.sub(camera.position).normalize());
    let intersects = rayCaster.intersectObjects(scene.children);
    if (intersects.length > 0) {
      let selected = intersects[0];//取第一个物体
      const xP = Math.round((selected.point.x + boardWidth.value / 2) / (boardWidth.value / 14));
      const yP = Math.round((selected.point.y + boardWidth.value / 2) / (boardWidth.value / 14));

      if(xP >= 0 && xP < 15 && yP >= 0 && yP < 15 && logicalBoard[yP][xP] == null){
        myTurn.value = false;
        logicalBoard[yP][xP] = white;
        console.log(logicalBoard[yP][xP])
        ws.send("place?" + xP + "?" + yP);
        addPiece(xP,yP,white);
      }
    }
  }
}

const redrawPositionDebounced = _debounce(redrawPosition, 300)

redrawPositionDebounced()

window.addEventListener('resize',redrawPositionDebounced)

/**
 * websocket
 */

const ws = new WebSocket(
    `ws://gobangback.clankalliance.cn/websocket/${localStorage.getItem('token')}`
)

ws.onmessage = (e) => {
  console.log('========ws收到========')
  console.log(e)
  console.log('=====================')
  const request = e.data.split('?');
  switch (request[0]){
    case 'connected':
      //connected?{nickName}?{winNum}?{loseNum}?{id}?{gender}?{avatarURL}?{token}
      console.log('connected');
      handleConnected(request);
      break;
    case 'loginFail':
      //loginFail
      handleLoginFail();
      break;
    case 'place':
      //place?{x}?{y}?{white: boolean}
      handlePlace(request);
      break;
    case 'placeError':
      //error
      handleLoginFail();
      break;
    case 'occupied':
      //occupied?{white:boolean}?{x}?{y}
      handleOccupied(request);
      break;
    case 'win':
      //win
      handleWin();
      break;
    case 'lose':
      //lose
      handleLose();
      break;
    case 'start':
      //start?{first: boolean}?{nickName}?{avatarURL}?{winNum}?{LoseNum}
      handleStart(request);
      break;
    case 'createRoomSuccess':
      //createRoomSuccess?{roomCode}
      handleCreateRoomSuccess(request);
      break;
    case 'roomCodeInvalid':
      //roomCodeInvalid
      handleRoomInvalid();
      break;
    case 'rivalOffline':
      //rivalOffline
      handleRivalDisconnected();
      break;
  }
}

const handleRivalDisconnected = () => {
  startTrans();
  ElMessage({
    message: '对方退出了游戏',
    type: 'error'
  })
}

const handleConnected = (method) => {
  userinfo.nickName= method[1];
  userinfo.id = method[4];
  userinfo.gender = method[5] === 'true';
  userinfo.winNum = Number(method[2]);
  userinfo.loseNum = Number(method[3]);
  userinfo.avatarURL = method[6];
  localStorage.setItem('token', method[7]);
  console.log(userinfo);
}

const handleLoginFail = () => {
  ElMessage({
    message: '登录失效',
    type:'error'
  })
  router.push('/');
}

const handlePlace = (method) => {
  const x = Number(method[1]),y = Number(method[2]),isWhite = (method[3] === 'true');
  console.log('收到棋子，颜色为白：' + isWhite)
  logicalBoard[y][x] = isWhite;
  addPiece(x,y,isWhite);
  myTurn.value = true;
}

const handleOccupied = (method) => {
  const isWhite = method[1] === 'true', x = Number(method[2]),y = Number(method[3]);
  ElMessage({
    message: '发生内部错误',
    type: 'error'
  })
  addPiece(x,y,isWhite);
}

const handleWin = () => {
  startTrans();
  ElMessage({
    message: '获胜',
    type: 'success'
  })
}

const handleLose = () => {
  startTrans();
  ElMessage({
    message: '失败',
    type: 'success'
  })
}

const handleStart = (method) => {
  white = !(method[1] === 'true');
  console.log(white)
  myTurn.value = !white;
  waiting.value = false;
  rivalinfo.nickName= method[2];
  rivalinfo.winNum = Number(method[4]);
  rivalinfo.loseNum = Number(method[5]);
  rivalinfo.avatarURL = method[3];
  startTrans();
}

const handleCreateRoomSuccess = (method) => {
  waiting.value = true;
  roomCode.value = method[1];
}

const handleRoomInvalid = () => {
  ElMessage({
    message: '房间已满或不存在',
    type: 'error'
  })
}

const handleStartMatch = () => {
  ws.send("match");
}

const handleCreatePrivate = () => {
  ws.send("private");
}

const handleQuit = () => {
  ws.send("quit");
  waiting.value = false;
}

const handleCodeEnter = () => {
  ws.send("invite?" + roomCode.value);
}

const inputCode = () => {
  ElMessageBox.prompt('请输入房间号','接收邀请',{
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  }).then(({value}) => {
    roomCode.value = value;
    handleCodeEnter();
  })
}

</script>

<style scoped>
.mainContainer{
  width: 100vw;
  height: 100vh;
  background-color: black;
}
.three{
  position: absolute;
  left: 0;
  bottom: 0;
}
.userArea{
  background-color: #313131;
  position: absolute;
  z-index: 100;
  color: #FFF;
}
.onlineArea{
  background-image: url("../assets/images/background.gif");
  background-repeat: repeat;
  background-size: 25vh;
  position: absolute;
  z-index: 100;
  right: 0;
  top: 0;
  display: flex;
  flex-direction: column;
}
.quickGame{
  width: 80%;
  height: 20%;
  margin: auto;
  border-radius: 2vh;
  background: -webkit-linear-gradient(to bottom right, rgba(124,255,60,1),rgba(124,255,60,0.3));
  background: -o-linear-gradient(to bottom right, rgba(124,255,60,1),rgba(124,255,60,0.3));
  background: -moz-linear-gradient(to bottom right, rgba(124,255,60,1),rgba(124,255,60,0.3));
  background: linear-gradient(to bottom right, rgba(124,255,60,1),rgba(124,255,60,0.3));
  /*background-color: rgba(124,255,60,0.79);*/
  color: azure;
  border-width: 8px;
  border-color: rgba(255,255,255,0.8);
}
.inviteGame{
  width: 80%;
  height: 20%;
  margin: auto;
  border-radius: 2vh;
  background: -webkit-linear-gradient(to bottom right, rgba(255,43,30,1),rgba(255,43,30,0.3));
  background: -o-linear-gradient(to bottom right, rgba(255,43,30,1),rgba(255,43,30,0.3));
  background: -moz-linear-gradient(to bottom right, rgba(255,43,30,1),rgba(255,43,30,0.3));
  background: linear-gradient(to bottom right, rgba(255,43,30,1),rgba(255,43,30,0.3));
  color: azure;
  border-width: 8px;
  border-color: rgba(255,255,255,0.8);
}
.createPrivate{
  width: 80%;
  height: 20%;
  margin: auto;
  border-radius: 2vh;
  background: -webkit-linear-gradient(to bottom right, rgba(255,216,38,1),rgba(255,216,38,0.3));
  background: -o-linear-gradient(to bottom right, rgba(255,216,38,1),rgba(255,216,38,0.3));
  background: -moz-linear-gradient(to bottom right, rgba(255,216,38,1),rgba(255,216,38,0.3));
  background: linear-gradient(to bottom right, rgba(255,216,38,1),rgba(255,216,38,0.3));
  color: azure;
  border-width: 8px;
  border-color: rgba(255,255,255,0.8);
}
.buttonInside{
  display: flex;
  flex-direction: column;
}
.buttonBigText{
  display: flex;
  font-size: 6vh;
  font-weight: bold;
}
.buttonSmallText{
  display: flex;
  font-size: 2vh;
  font-weight: lighter;
}
.userAreaIn{
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}
.userTop{
  width: 100%;
  height: 60%;
  display: flex;
  flex-direction: row;
}
.userBottom{
  width: 100%;
  height: 40%;
}
.avatarArea{
  width: 50%;
  height: 100%;
  display: flex;
}
.avatar{
  background-color: #FFFFFF;
  width: 80%;
  height: 66%;
  margin: auto;
}
.avatarImage{
  width: 100%;
}
.record{
  width: 50%;
  display: flex;
  flex-direction: column;
}
.recordText{
  margin: auto;
}
.mask{
  z-index: 1000;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0,0,0,0.5);
  position: absolute;
  left: 0;
  top: 0;
}
.maskIn{
  width: 100%;
  height: 80%;
  display: flex;
  flex-direction: column;
}
.maskWindow{
  background-color: #599af8;
  margin: auto;
  display: flex;
  flex-direction: column;
  height: 30vh;
}
.loadIcon{
  width: 20vh;
  height: 20vh;
  margin: auto;
}
.waitingText{
  font-size: 4vh;
  margin: auto;
  color: #FFFFFF;
}
.quit{
  font-size: 3vh;
  margin: auto;
  color: #FFFFFF;
}
.quit:hover{
  color: #d14845;
}
.rivalGameArea{
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 15vh;
  z-index: 200;
}
.userGameArea{
  position: absolute;
  bottom: 0;
  right: 0;
  width: 100vw;
  height: 15vh;
  z-index: 200;
}
.roundNote{
  height: 10vh;
  font-size: 5vh;
  color: #FFFFFF;
  border-radius: 1vh;
  display: flex;
  flex-direction: column;
  position: absolute;
  z-index: 210;
}
.roundText{
  margin: auto;
}
.gameTextBarTop{
  width: 100vw;
  height: 10vh;
  position: absolute;
  top: 0;
  left: 0;
  background: -webkit-linear-gradient(to bottom, rgba(60,60,150,1),rgba(0,0,80,0));
  background: -o-linear-gradient(to bottom, rgba(60,60,150,1),rgba(0,0,80,0));
  background: -moz-linear-gradient(to bottom, rgba(60,60,150,1),rgba(0,0,80,0));
  background: linear-gradient(to bottom, rgba(60,60,150,1),rgba(0,0,80,0));
}
.gameTextBarBottom{
  width: 100vw;
  height: 10vh;
  position: absolute;
  right: 0;
  bottom: 0;
  background: -webkit-linear-gradient(to top, rgba(150,60,60,1),rgba(80,0,0,0));
  background: -o-linear-gradient(to top, rgba(150,60,60,1),rgba(80,0,0,0));
  background: -moz-linear-gradient(to top, rgba(150,60,60,1),rgba(80,0,0,0));
  background: linear-gradient(to top, rgba(150,60,60,1),rgba(80,0,0,0));
}
.gameAvatar{
  width: 15vh;
  height: 15vh;
  position: absolute;
  border-width: 10px;
  border-style: solid;
}
.gameAvatarImage{
  width: 100%;
}
.gameText{
  color: #FFFFFF;
  font-size: 5vh;
  position: absolute;
}
</style>

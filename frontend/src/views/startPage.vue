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

    </div>
    <div @click="onClick3D" class="three" id="my-three" :style="{
      'z-index': `${processStatus === 0? 50:150}`
    }" ></div>
    <div class="onlineArea"  :style="{
      'width': `${mobile? windowWidth:(windowWidth - 0.5 * windowHeight)}px`,
      'height': `${mobile? (windowHeight - 0.5 * windowWidth):windowHeight}px`
    }">
      <el-button class="quickGame" >
        <div class="buttonInside" >
          <a class="buttonBigText" >快速游戏</a>
          <a class="buttonSmallText" >与随机玩家对战</a>
        </div>
      </el-button>
      <el-button class="inviteGame" >
        <div class="buttonInside" >
          <a class="buttonBigText" >邀请游戏</a>
          <a class="buttonSmallText" >使用邀请码进入房间</a>
        </div>
      </el-button>
    </div>
  </div>
</template>

<script lang="ts" setup type="module">
import {mobile, windowHeight, windowWidth} from "@/global/global";
import * as THREE from 'three'

import {reactive, ref} from "vue";
import {_debounce} from "@/utils/throTtle";
const boardWidth = ref(350);
let renderer = null;
let camera = null;
let scene = null;
let white = false;//为true,玩家下白棋
const processStatus = ref(0);//0开始 1过渡 2游戏

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

for(let i = 0;i < 15;i ++){
  logicalBoard[i] = [];
  for(let j = 0;j < 15;j ++){
    logicalBoard[i][j] = null;
  }
}

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
  renderer.forceContextLoss();
  renderer.dispose();
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
  console.log('transation')
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
  console.log('transation')
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
  // const ambient = new THREE.AmbientLight(0xffffff, 0.3);
  // const light = new THREE.PointLight(0xffffff, 1);
  // scene.add(ambient);
  // light.position.set(0,0,boardWidth.value * 5);
  // scene.add(light);
  const width = window.innerWidth, height = window.innerHeight;
  camera = new THREE.PerspectiveCamera(45, width/height, 1, 1000);
  camera.position.set(0,-1 * position,position);
  camera.lookAt(0,0,0);
  renderer = new THREE.WebGLRenderer()
  renderer.setSize(width,height)
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type=THREE.PCFSoftShadowMap;
  // const controls = new OrbitControls(camera, renderer.domElement);
  // controls.addEventListener('change', () => {
  //   renderer.render(scene, camera)
  // })
  drawLine(0x000000);
  for(let i = 0;i < 15;i ++){
    for(let j = 0;j < 15;j ++){
      if(logicalBoard[i][j] != null){
        addPiece(j,i,logicalBoard[i][j]);
      }
    }
  }
}

const addPiece = (x,y,white) => {
  const  pieceGeometry = new THREE.SphereGeometry(5, 4, 4);
  const  pieceMaterial = new THREE.MeshLambertMaterial({
    color: white? 0xffffff:0x000000
  });
  const pieceMesh = new THREE.Mesh(pieceGeometry, pieceMaterial);
  pieceMesh.position.set(x * boardWidth.value / 14 - boardWidth.value / 2,y * boardWidth.value / 14 - boardWidth.value / 2,boardWidth.value / 20);
  scene.add(pieceMesh);
  renderer.render(scene,camera);
}

const redrawPosition = (length) => {
  boardWidth.value = length;
  console.log('status: ' + processStatus.value)
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
  processStatus.value = 1;
  cancelAnimationFrame(animation);
  enlargePaceW = (windowWidth.value - rendererSize.width) / 300;
  enlargePaceH = (windowHeight.value - rendererSize.height) / 300;
  transUpdate();
}

const transUpdate = () => {
  let p = camera.position;
  let x,y,z, xT=0,yT=-1*position,zT=position;
  x = Math.abs(p.x - xT) > movePace? p.x + (p.x - xT > 0? -1: 1) * movePace:xT;
  y = Math.abs(p.y - yT) > movePace? p.y + (p.y - yT > 0? -1: 1) * movePace:yT;
  z = Math.abs(p.z - zT) > movePace? p.z + (p.z - zT > 0? -1: 1) * movePace:zT;
  if(x===p.x && y===p.y && z===p.z){
    console.log('transOver')
    cancelAnimationFrame(animation);
    processStatus.value = 2;
    setTimeout(handleWindowResize,800)
    return;
  }
  let w = (windowWidth.value - rendererSize.width) > enlargePaceW?rendererSize.width + enlargePaceW:windowWidth.value;
  let h = (windowHeight.value - rendererSize.height) > enlargePaceH?rendererSize.height + enlargePaceH:windowHeight.value;
  camera = new THREE.PerspectiveCamera(45, w/h, 1, 1000);
  camera.position.set(x,y,z);
  camera.lookAt(0,0,0);
  renderer.setSize(w,h);
  rendererSize.width = w;
  rendererSize.height = h;
  renderer.render(scene,camera)
  document.getElementById('my-three')?.appendChild(renderer.domElement)
  animation = requestAnimationFrame(transUpdate);
}

// onMounted(()=>{
//   document.getElementById('my-three')?.appendChild(renderer.domElement)
// })
const onClick3D = (e) => {
  let vector = new THREE.Vector3();//三维坐标对象
  vector.set(
      ( e.clientX / window.innerWidth ) * 2 - 1,
      - ( e.clientY / window.innerHeight ) * 2 + 1,
      0.5 );
  vector.unproject( camera );
  let raycaster = new THREE.Raycaster(camera.position, vector.sub(camera.position).normalize());
  let intersects = raycaster.intersectObjects(scene.children);
  if (intersects.length > 0) {
    let selected = intersects[0];//取第一个物体
    const xP = Math.round((selected.point.x + boardWidth.value / 2) / (boardWidth.value / 14));
    const yP = Math.round((selected.point.y + boardWidth.value / 2) / (boardWidth.value / 14));
    console.log('x:' + xP + ', y:' + yP);
    if(xP >= 0 && xP < 15 && yP >= 0 && yP < 15 && logicalBoard[yP][xP] == null){
      logicalBoard[yP][xP] = white;
      addPiece(xP,yP,white);
    }
  }
}

// redrawPosition();

const handleWindowResize = () => {
  windowWidth.value = window.innerWidth;
  windowHeight.value = window.innerHeight;
  if(
      navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
      || window.innerWidth / window.innerHeight <= 1
  ){
    console.log('mobile=true')
    mobile.value = true;
  }else{
    console.log('mobile=false')
    mobile.value = false;
  }
  redrawPosition(Math.min(windowWidth.value, windowHeight.value) / 4);
}

const handleWindowResizeDebounced = _debounce(handleWindowResize, 300)


handleWindowResizeDebounced()

window.addEventListener('resize',handleWindowResizeDebounced)
// setTimeout(() => {
//   startTrans();
//   // console.log('set1')
//   // processStatus.value = 1;
//   // redrawPosition(Math.min(windowWidth.value, windowHeight.value) / 4);
//   // setTimeout(() => {
//   //   console.log('set2')
//   //   processStatus.value = 2;
//   //   redrawPosition(Math.min(windowWidth.value, windowHeight.value) / 4);
//   // },1000)
// },1000)

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
}
.onlineArea{
  background-color: #313131;
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
</style>

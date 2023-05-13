<template>
  <div class="mainContainer" >
    <div @click="onClick3D" class="three" id="my-three"></div>
  </div>
</template>

<script lang="ts" setup type="module">
import {mobile, windowHeight, windowWidth} from "@/global/global";
import * as THREE from 'three'
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js"; //控制器

import {onMounted, ref} from "vue";
const boardWidth = ref(350);
let renderer = null;
let camera = null;
let scene = null;


const clearScene = () => {
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

const redrawPosition = (length) => {
  console.log(length)
  console.log('redraw')
  boardWidth.value = length;
  if(scene !== null)
    clearScene();
  scene = new THREE.Scene();
  const geometry = new THREE.BoxGeometry(boardWidth.value * 1.2, boardWidth.value * 1.2, boardWidth.value / 10);
  const material = new THREE.MeshLambertMaterial({
    color: 0xDDDD99,
    transparent: false
  });
  const mesh = new THREE.Mesh(geometry, material);
  scene.add(mesh);
  const ambient = new THREE.AmbientLight(0xffffff, 0.4);
  const light = new THREE.PointLight(0xffffff, 1);

  scene.add(ambient);
  light.position.set(200,300,400);
  scene.add(light);
  const width = window.innerWidth, height = window.innerHeight;
  camera = new THREE.PerspectiveCamera(45, width/height, 1, 1000);
  camera.position.set(0,-400,400);
  camera.lookAt(0,0,0);
  renderer = new THREE.WebGLRenderer()
  renderer.setSize(width,height)
  console.log('render')
  renderer.render(scene,camera)
  document.getElementById('my-three')?.appendChild(renderer.domElement)

  for(let i = 1;i <= 15;i ++){

  }
}

onMounted(()=>{
  document.getElementById('my-three')?.appendChild(renderer.domElement)
})
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
    console.log("x坐标:"+selected.point.x);
    console.log("y坐标:"+selected.point.y);
    console.log("z坐标:"+selected.point.z);
    const  geometry2 = new THREE.SphereGeometry(5, 4, 4);
    const  material2 = new THREE.MeshLambertMaterial({
        color: 0xffff00
    });
    const mesh2 = new THREE.Mesh(geometry2, material2); //网格模型对象Mesh
    // mesh3.translateX(120); //球体网格模型沿Y轴正方向平移120
    mesh2.position.set(selected.point.x,selected.point.y,selected.point.z);//设置mesh3模型对象的xyz坐标为120,0,0
    scene.add(mesh2);
    renderer.render(scene,camera)//执行渲染操作、指定场景、相机作为参数
  }
}

// redrawPosition();


const getWindowResize = function () {
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
  redrawPosition(Math.min(windowWidth.value, windowHeight.value) / 4);
}


getWindowResize()

window.addEventListener('resize',getWindowResize)

</script>

<style scoped>
.mainContainer{
  background-color: #313131;
}
.three{
  /*background-color: dodgerblue;*/
  position: absolute;
  left: 0;
  bottom: 0;
}
</style>

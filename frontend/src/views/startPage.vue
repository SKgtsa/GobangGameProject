<template>
  <div class="mainContainer" >
    <div class="three" id="my-three"></div>
  </div>
</template>

<script lang="ts" setup type="module">
import {mobile} from "@/global/global";
import * as THREE from 'three'
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js"; //控制器
import gsap from "gsap";
import { CSSRulePlugin } from "gsap/CSSRulePlugin";
import {onMounted, ref} from "vue";
gsap.registerPlugin(CSSRulePlugin);
const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(
    45,
    window.innerWidth / window.innerHeight,
    1,
    5000
);
const renderer= new THREE.WebGLRenderer({
  antialias: true,
  alpha: true
})
const controls = new OrbitControls(camera, renderer.domElement);
const geometry = new THREE.BoxGeometry(400,400,50);
const material = new THREE.MeshLambertMaterial({
  color: 0x00ff00,
  transparent: true,
  opacity: 0.5
})
const mesh = new THREE.Mesh(geometry, material);

const ambient = new THREE.AmbientLight(0xff0000, 0.4);
const light = new THREE.PointLight(0xff0000, 1);
light.position.set(200,300,400);
const width = window.innerWidth, height = window.innerHeight;
camera.position.set(300,300,300);
camera.lookAt(0,0,0);
renderer.setSize(width, height);

scene.add(mesh);
scene.add(ambient);
scene.add(light);
renderer.setSize(window.innerWidth, window.innerHeight);
renderer.render(scene, camera)

controls.addEventListener('change', () => {
  renderer.render(scene, camera)
})
onMounted(() => {
  document.getElementById('my-three')?.appendChild(renderer.domElement)
})
</script>

<style scoped>
.mainContainer{
  background-color: #313131;
}
.three{
  background-color: dodgerblue;
  position: absolute;
  left: 0;
  bottom: 0;
}
</style>

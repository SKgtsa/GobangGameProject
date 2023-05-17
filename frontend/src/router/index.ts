import { createRouter, createWebHistory } from 'vue-router'
import {reload} from "@/utils/reloadRouter";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'loginPage',
      component: () => import('../views/loginPage.vue'),
    },{
      path: '/game',
      name: 'gamePage',
      component: () => import('../views/gamePage.vue'),
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  reload();
  next();
})
export default router

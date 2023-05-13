import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'startPage',
      component: () => import('../views/startPage.vue'),
    },{
      path: '/sort/',
      name: 'sortPage',
      component: () => import('../views/sortPage.vue'),
      meta: {
        keepAlive: true // 需要缓存
      },
      children: [
        {
          path: '/sort/bubbleSort',
          name: 'bubbleSort',
          component: () => import('../components/SortModule/BubbleSortModule.vue')
        },{
          path: '/sort/boxSort',
          name: 'boxSort',
          component: () => import('../components/SortModule/BoxSortModule.vue')
        },{
          path: '/sort/heapSort',
          name: 'heapSort',
          component: () => import('../components/SortModule/HeapSortModule.vue')
        },{
          path: '/sort/mergeSort',
          name: 'mergeSort',
          component: () => import('../components/SortModule/MergeSortModule.vue')
        }
      ]
    }
  ]
})


export default router

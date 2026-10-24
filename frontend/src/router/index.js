import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layout/AppLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'projects',
        name: 'projects',
        component: () => import('../views/Projects.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'modules',
        name: 'modules',
        component: () => import('../views/Modules.vue'),
        meta: { title: '模块管理' }
      },
      {
        path: 'cases',
        name: 'cases',
        component: () => import('../views/TestCases.vue'),
        meta: { title: '测试用例' }
      },
      {
        path: 'plans',
        name: 'plans',
        component: () => import('../views/TestPlans.vue'),
        meta: { title: '测试计划' }
      },
      {
        path: 'plans/:id',
        name: 'plan-detail',
        component: () => import('../views/PlanDetail.vue'),
        meta: { title: '计划执行' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('tcm_token')
  if (!to.meta.public && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && token) {
    return { name: 'dashboard' }
  }
  document.title = to.meta.title ? `${to.meta.title} · 测试用例管理系统` : '测试用例管理系统'
  return true
})

export default router

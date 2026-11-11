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
        path: 'cases/new',
        name: 'case-create',
        component: () => import('../views/CaseCreate.vue'),
        meta: { title: '新建用例' }
      },
      {
        path: 'cases/:id',
        name: 'case-detail',
        component: () => import('../views/CaseDetail.vue'),
        meta: { title: '用例详情' }
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
      },
      {
        path: 'ai/generate',
        name: 'ai-generate',
        component: () => import('../views/AiGenerate.vue'),
        meta: { title: 'AI 生成用例' }
      },
      {
        path: 'ai/settings',
        name: 'ai-settings',
        component: () => import('../views/AiSettings.vue'),
        meta: { title: 'AI 模型配置', requiresAdmin: true }
      },
      {
        path: 'ai/monitor',
        name: 'ai-monitor',
        component: () => import('../views/AiMonitor.vue'),
        meta: { title: 'AI 调用监控' }
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
  if (to.meta.requiresAdmin) {
    const user = JSON.parse(localStorage.getItem('tcm_user') || 'null')
    if (user?.role !== 'ADMIN') {
      return { name: 'dashboard' }
    }
  }
  document.title = to.meta.title ? `${to.meta.title} · 测试用例管理系统` : '测试用例管理系统'
  return true
})

export default router

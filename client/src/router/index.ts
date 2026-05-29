import { createRouter, createWebHistory } from 'vue-router'
import { authGuard } from './guards'
import {
  assetRoutes,
  governanceRoutes,
  ingestionRoutes,
  platformRoutes,
  researchRoutes,
  wave2Routes,
} from './modules/routes'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/ForbiddenView.vue'),
      meta: { title: '无权限' },
    },
    {
      path: '/404',
      name: 'NotFound',
      component: () => import('@/views/error/NotFoundView.vue'),
      meta: { title: '页面不存在' },
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '工作台' },
        },
        ...platformRoutes,
        ...ingestionRoutes,
        ...governanceRoutes,
        ...assetRoutes,
        ...researchRoutes,
        ...wave2Routes,
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/404' },
  ],
})

router.beforeEach(authGuard)

router.afterEach((to) => {
  const title = (to.meta.title as string) || 'Nanda'
  document.title = `${title} · 共病专病科研平台`
})

export default router

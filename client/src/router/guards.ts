import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { getAccessToken } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const PUBLIC_PATHS = ['/login', '/403', '/404']

export async function authGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext,
) {
  const token = getAccessToken()
  const isPublic = PUBLIC_PATHS.includes(to.path)

  if (!token && !isPublic) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (token && to.path === '/login') {
    next('/')
    return
  }

  if (token && !isPublic) {
    const auth = useAuthStore()
    if (!auth.userId) {
      try {
        await auth.fetchMe()
      } catch {
        auth.reset()
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
    }

    const permission = to.meta.permission as string | undefined
    if (permission && !auth.hasPermission(permission)) {
      next('/403')
      return
    }
  }

  next()
}

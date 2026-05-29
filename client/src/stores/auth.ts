import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { clearAuth, getAccessToken, setOrgId, setTokens } from '@/api/http'
import type { LoginRequest } from '@/types/modules/platform'

export const useAuthStore = defineStore('auth', () => {
  const userId = ref<number | null>(null)
  const username = ref('')
  const displayName = ref('')
  const orgId = ref<number | null>(null)
  const orgIds = ref<number[]>([])
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!getAccessToken())
  const currentOrgId = computed(() => orgId.value)

  function hasPermission(code: string) {
    return permissions.value.includes(code)
  }

  function hasAnyPermission(codes: string[]) {
    return codes.some((c) => permissions.value.includes(c))
  }

  async function login(payload: LoginRequest) {
    const res = await authApi.login(payload)
    setTokens(res.accessToken, res.refreshToken)
    userId.value = res.user.id
    username.value = res.user.username
    displayName.value = res.user.displayName
    orgId.value = res.user.orgId
    permissions.value = res.permissions ?? []
    if (res.user.orgId) {
      setOrgId(res.user.orgId)
    }
    await fetchMe()
  }

  async function fetchMe() {
    const me = await authApi.me()
    userId.value = me.userId
    username.value = me.username
    orgId.value = me.orgId
    orgIds.value = me.orgIds ?? []
    permissions.value = me.permissions ?? []
    if (me.orgId) {
      setOrgId(me.orgId)
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      reset()
    }
  }

  function switchOrg(id: number) {
    if (!orgIds.value.includes(id) && orgId.value !== id) return
    orgId.value = id
    setOrgId(id)
  }

  function reset() {
    clearAuth()
    userId.value = null
    username.value = ''
    displayName.value = ''
    orgId.value = null
    orgIds.value = []
    permissions.value = []
  }

  return {
    userId,
    username,
    displayName,
    orgId,
    orgIds,
    permissions,
    isLoggedIn,
    currentOrgId,
    hasPermission,
    hasAnyPermission,
    login,
    fetchMe,
    logout,
    switchOrg,
    reset,
  }
})

import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { PageResult, Result } from '@/types/api'
import {
  FORBIDDEN_CODE,
  SUCCESS_CODE,
  UNAUTHORIZED_CODE,
} from '@/types/api'

const TOKEN_KEY = 'nanda_access_token'
const REFRESH_KEY = 'nanda_refresh_token'
const ORG_KEY = 'nanda_org_id'

let refreshPromise: Promise<string | null> | null = null

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_KEY)
}

export function getOrgId(): number | null {
  const raw = localStorage.getItem(ORG_KEY)
  return raw ? Number(raw) : null
}

export function setTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem(TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_KEY, refreshToken)
}

export function setOrgId(orgId: number) {
  localStorage.setItem(ORG_KEY, String(orgId))
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(ORG_KEY)
}

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return null

  try {
    const { data } = await axios.post<Result<{ accessToken: string; refreshToken: string }>>(
      `${import.meta.env.VITE_API_BASE}/auth/refresh`,
      { refreshToken },
    )
    if (data.code === SUCCESS_CODE && data.data) {
      setTokens(data.data.accessToken, data.data.refreshToken)
      return data.data.accessToken
    }
  } catch {
    // fall through
  }
  return null
}

export const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 30000,
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  const orgId = getOrgId()
  if (orgId) {
    config.headers['X-Org-Id'] = String(orgId)
  }
  return config
})

http.interceptors.response.use(
  async (response) => {
    const payload = response.data as Result<unknown>
    if (payload && typeof payload.code === 'number') {
      if (payload.code === SUCCESS_CODE) {
        return response
      }
      if (payload.code === FORBIDDEN_CODE) {
        ElMessage.error(payload.message || '无操作权限')
        router.push('/403')
        return Promise.reject(payload)
      }
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(payload)
    }
    return response
  },
  async (error) => {
    const status = error.response?.status
    const original = error.config

    if (status === 401 && original && !original._retry) {
      original._retry = true
      if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => {
          refreshPromise = null
        })
      }
      const newToken = await refreshPromise
      if (newToken) {
        original.headers.Authorization = `Bearer ${newToken}`
        return http(original)
      }
      clearAuth()
      router.push('/login')
      return Promise.reject(error)
    }

    const payload = error.response?.data as Result<unknown> | undefined
    if (payload?.code === UNAUTHORIZED_CODE) {
      clearAuth()
      router.push('/login')
    } else {
      ElMessage.error(payload?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

type ResultPayload<T> = Pick<Result<T>, 'code' | 'data'> & Partial<Result<T>>

export async function unwrap<T>(promise: Promise<AxiosResponse<ResultPayload<T>>>): Promise<T> {
  const { data } = await promise
  return data.data
}

export async function unwrapPage<T>(promise: Promise<AxiosResponse<ResultPayload<PageResult<T>>>>) {
  return unwrap(promise)
}

declare module 'axios' {
  export interface InternalAxiosRequestConfig {
    _retry?: boolean
  }
}

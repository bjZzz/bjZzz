import { http, unwrap } from './http'
import type { LoginRequest, LoginResponse, MeResponse, RefreshTokenResponse } from '@/types/modules/platform'

export const authApi = {
  login(data: LoginRequest) {
    return unwrap(http.post<{ code: number; data: LoginResponse }>('/auth/login', data))
  },
  logout() {
    return unwrap(http.post('/auth/logout'))
  },
  refresh(refreshToken?: string) {
    return unwrap(http.post<{ code: number; data: RefreshTokenResponse }>('/auth/refresh', { refreshToken }))
  },
  me() {
    return unwrap(http.get<{ code: number; data: MeResponse }>('/auth/me'))
  },
}

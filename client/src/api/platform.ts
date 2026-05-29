import { http, unwrap, unwrapPage } from './http'
import type { PageQuery } from '@/types/api'
import type {
  AuditLogVO,
  OrgCreateRequest,
  OrgTreeNode,
  OrgUpdateRequest,
  OrgVO,
  PermissionTreeNode,
  RoleVO,
  UserCreateRequest,
  UserUpdateRequest,
  UserVO,
} from '@/types/modules/platform'

export const platformApi = {
  orgTree() {
    return unwrap(http.get<{ code: number; data: OrgTreeNode[] }>('/orgs/tree'))
  },
  orgGet(id: number) {
    return unwrap(http.get<{ code: number; data: OrgVO }>(`/orgs/${id}`))
  },
  orgCreate(data: OrgCreateRequest) {
    return unwrap(http.post<{ code: number; data: OrgVO }>('/orgs', data))
  },
  orgUpdate(id: number, data: OrgUpdateRequest) {
    return unwrap(http.put<{ code: number; data: OrgVO }>(`/orgs/${id}`, data))
  },
  orgDelete(id: number) {
    return unwrap(http.delete(`/orgs/${id}`))
  },
  users(params: PageQuery & { username?: string; status?: string }) {
    return unwrapPage<UserVO>(http.get('/users', { params }))
  },
  userGet(id: number) {
    return unwrap(http.get<{ code: number; data: UserVO }>(`/users/${id}`))
  },
  userCreate(data: UserCreateRequest) {
    return unwrap(http.post<{ code: number; data: UserVO }>('/users', data))
  },
  userUpdate(id: number, data: UserUpdateRequest) {
    return unwrap(http.put<{ code: number; data: UserVO }>(`/users/${id}`, data))
  },
  userAssignRoles(id: number, roleIds: number[]) {
    return unwrap(http.put(`/users/${id}/roles`, { roleIds }))
  },
  userBindOrgs(id: number, orgIds: number[]) {
    return unwrap(http.put(`/users/${id}/orgs`, { orgIds }))
  },
  userUpdateStatus(id: number, status: string) {
    return unwrap(http.put(`/users/${id}/status`, { status }))
  },
  roles() {
    return unwrap(http.get<{ code: number; data: RoleVO[] }>('/roles'))
  },
  permissionsTree() {
    return unwrap(http.get<{ code: number; data: PermissionTreeNode[] }>('/permissions/tree'))
  },
  auditLogs(params: PageQuery & { username?: string; action?: string }) {
    return unwrapPage<AuditLogVO>(http.get('/audit/logs', { params }))
  },
}

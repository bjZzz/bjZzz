export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: {
    id: number
    username: string
    displayName: string
    orgId: number
  }
  permissions: string[]
}

export interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface MeResponse {
  userId: number
  username: string
  orgId: number
  orgIds: number[]
  permissions: string[]
}

export interface OrgTreeNode {
  id: number
  orgCode: string
  orgName: string
  orgType?: string
  parentId?: number
  levelType?: string
  status?: string
  children?: OrgTreeNode[]
}

export interface OrgVO {
  id: number
  orgCode: string
  orgName: string
  orgType?: string
  parentId?: number
  levelType?: string
  status?: string
  createdAt?: string
}

export interface OrgCreateRequest {
  orgCode: string
  orgName: string
  orgType?: string
  parentId?: number
  levelType?: string
}

export interface OrgUpdateRequest {
  orgName?: string
  orgType?: string
  status?: string
}

export interface UserVO {
  id: number
  username: string
  displayName: string
  primaryOrgId?: number
  status?: string
  orgId?: number
  roleIds?: number[]
  orgIds?: number[]
  createdAt?: string
}

export interface UserCreateRequest {
  username: string
  password: string
  displayName: string
  primaryOrgId?: number
  roleIds?: number[]
  orgIds?: number[]
}

export interface UserUpdateRequest {
  displayName?: string
  primaryOrgId?: number
}

export interface RoleVO {
  id: number
  roleCode: string
  roleName: string
  description?: string
}

export interface PermissionTreeNode {
  id: number
  permCode: string
  permName: string
  module?: string
  children?: PermissionTreeNode[]
}

export interface AuditLogVO {
  id: number
  userId?: number
  username?: string
  action?: string
  resourceType?: string
  resourceId?: string
  detail?: string
  ipAddress?: string
  createdAt?: string
}

export interface Result<T> {
  code: number
  message: string
  data: T
  requestId?: string
  errors?: Array<{ field: string; reason: string }>
}

export interface PageResult<T> {
  items: T[]
  page: number
  size: number
  total: number
}

export interface PageQuery {
  page?: number
  size?: number
  sort?: string
}

export const SUCCESS_CODE = 0
export const FORBIDDEN_CODE = 40301
export const UNAUTHORIZED_CODE = 40101

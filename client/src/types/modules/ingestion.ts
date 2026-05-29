export interface DataSourceVO {
  id: number
  sourceCode: string
  sourceName: string
  protocol: string
  configJson?: string
  orgId?: number
  status?: string
  createdAt?: string
}

export interface DataSourceCreateRequest {
  sourceCode: string
  sourceName: string
  protocol: string
  configJson?: string
}

export interface SyncJobVO {
  id: number
  sourceId: number
  jobName: string
  cronExpression?: string
  syncMode?: string
  status?: string
  lastRunAt?: string
  createdAt?: string
}

export interface SyncJobCreateRequest {
  sourceId: number
  jobName: string
  cronExpression?: string
  syncMode?: string
}

export interface StagingBatchVO {
  id: number
  sourceId?: number
  jobId?: number
  orgId?: number
  receivedAt?: string
  recordCount?: number
  successCount?: number
  failCount?: number
  status?: string
  errorMessage?: string
  createdAt?: string
}

export interface ConnectionTestVO {
  success: boolean
  message?: string
}

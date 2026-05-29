import { http, unwrap, unwrapPage } from './http'
import type { PageQuery } from '@/types/api'
import type {
  ConnectionTestVO,
  DataSourceCreateRequest,
  DataSourceVO,
  StagingBatchVO,
  SyncJobCreateRequest,
  SyncJobVO,
} from '@/types/modules/ingestion'

export const ingestionApi = {
  datasources(params?: PageQuery) {
    return unwrapPage<DataSourceVO>(http.get('/ingestion/datasources', { params }))
  },
  datasourceGet(id: number) {
    return unwrap(http.get<{ code: number; data: DataSourceVO }>(`/ingestion/datasources/${id}`))
  },
  datasourceCreate(data: DataSourceCreateRequest) {
    return unwrap(http.post<{ code: number; data: DataSourceVO }>('/ingestion/datasources', data))
  },
  datasourceTestConnection(id: number) {
    return unwrap(http.post<{ code: number; data: ConnectionTestVO }>(`/ingestion/datasources/${id}/test-connection`))
  },
  syncJobs(params?: PageQuery) {
    return unwrapPage<SyncJobVO>(http.get('/ingestion/sync-jobs', { params }))
  },
  syncJobCreate(data: SyncJobCreateRequest) {
    return unwrap(http.post<{ code: number; data: SyncJobVO }>('/ingestion/sync-jobs', data))
  },
  syncJobStart(id: number) {
    return unwrap(http.post(`/ingestion/sync-jobs/${id}/start`))
  },
  stagingBatches(params?: PageQuery) {
    return unwrapPage<StagingBatchVO>(http.get('/ingestion/staging/batches', { params }))
  },
  stagingBatchGet(id: number) {
    return unwrap(http.get<{ code: number; data: StagingBatchVO }>(`/ingestion/staging/batches/${id}`))
  },
  stagingBatchRetry(id: number) {
    return unwrap(http.post(`/ingestion/staging/batches/${id}/retry`))
  },
}

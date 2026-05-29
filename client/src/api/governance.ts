import { http, unwrap, unwrapPage } from './http'
import type { PageQuery } from '@/types/api'
import type {
  CleaningRuleCreateRequest,
  CleaningRuleVO,
  CrfFormCreateRequest,
  CrfFormVO,
  CrfResponseSubmitRequest,
  CrfResponseVO,
  DictDiagnosisCreateRequest,
  DictDiagnosisVO,
  LineageEdgeVO,
  MetadataCatalogVO,
  PublishRuleCreateRequest,
  PublishRuleVO,
} from '@/types/modules/governance'

export const governanceApi = {
  diagnosisDict(params?: PageQuery) {
    return unwrapPage<DictDiagnosisVO>(http.get('/governance/dictionaries/diagnosis', { params }))
  },
  diagnosisDictCreate(data: DictDiagnosisCreateRequest) {
    return unwrap(http.post<{ code: number; data: DictDiagnosisVO }>('/governance/dictionaries/diagnosis', data))
  },
  cleaningRules(params?: PageQuery) {
    return unwrapPage<CleaningRuleVO>(http.get('/governance/cleaning-rules', { params }))
  },
  cleaningRuleCreate(data: CleaningRuleCreateRequest) {
    return unwrap(http.post<{ code: number; data: CleaningRuleVO }>('/governance/cleaning-rules', data))
  },
  publishRules(params?: PageQuery) {
    return unwrapPage<PublishRuleVO>(http.get('/governance/publish/rules', { params }))
  },
  publishRuleCreate(data: PublishRuleCreateRequest) {
    return unwrap(http.post<{ code: number; data: PublishRuleVO }>('/governance/publish/rules', data))
  },
  publishExecute(batchId: number) {
    return unwrap(http.post(`/governance/publish/tasks/${batchId}/execute`))
  },
  crfForms(params?: PageQuery) {
    return unwrapPage<CrfFormVO>(http.get('/governance/crf/forms', { params }))
  },
  crfFormCreate(data: CrfFormCreateRequest) {
    return unwrap(http.post<{ code: number; data: CrfFormVO }>('/governance/crf/forms', data))
  },
  crfFormPublish(id: number) {
    return unwrap(http.post(`/governance/crf/forms/${id}/publish`))
  },
  crfResponses(params?: PageQuery) {
    return unwrapPage<CrfResponseVO>(http.get('/governance/crf/forms/responses', { params }))
  },
  crfResponseSubmit(data: CrfResponseSubmitRequest) {
    return unwrap(http.post<{ code: number; data: CrfResponseVO }>('/governance/crf/forms/responses', data))
  },
  metadataCatalog(params?: PageQuery) {
    return unwrapPage<MetadataCatalogVO>(http.get('/governance/metadata/catalog', { params }))
  },
  metadataLineage(params?: { tableName?: string }) {
    return unwrap(http.get<{ code: number; data: LineageEdgeVO[] }>('/governance/metadata/lineage', { params }))
  },
}

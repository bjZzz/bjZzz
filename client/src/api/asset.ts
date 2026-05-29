import { http, unwrap, unwrapPage } from './http'
import type { PageQuery } from '@/types/api'
import type {
  ComorbidityRuleCreateRequest,
  ComorbidityRuleVO,
  ComorbidityViewVO,
  EmpiMatchCandidateVO,
  EmpiPatientVO,
  QcDashboardVO,
  QcReviewRequest,
  QcReviewTaskVO,
  SpecialtyPatientVO,
  SpecialtyType,
  TimelineEventVO,
} from '@/types/modules/asset'

export const assetApi = {
  empiPatient(empiId: number) {
    return unwrap(http.get<{ code: number; data: EmpiPatientVO }>(`/empi/patients/${empiId}`))
  },
  empiTimeline(empiId: number) {
    return unwrap(http.get<{ code: number; data: TimelineEventVO[] }>(`/empi/patients/${empiId}/timeline`))
  },
  empiMatchCandidates(params?: PageQuery) {
    return unwrapPage<EmpiMatchCandidateVO>(http.get('/empi/match-candidates', { params }))
  },
  empiConfirmCandidate(id: number) {
    return unwrap(http.post(`/empi/match-candidates/${id}/confirm`))
  },
  empiRejectCandidate(id: number) {
    return unwrap(http.post(`/empi/match-candidates/${id}/reject`))
  },
  specialtyPatients(type: SpecialtyType, params?: PageQuery) {
    return unwrapPage<SpecialtyPatientVO>(http.get(`/specialty/${type}/patients`, { params }))
  },
  specialtyPatient(type: SpecialtyType, recordId: number) {
    return unwrap(http.get<{ code: number; data: SpecialtyPatientVO }>(`/specialty/${type}/patients/${recordId}`))
  },
  comorbidityRules(params?: PageQuery) {
    return unwrapPage<ComorbidityRuleVO>(http.get('/comorbidity/rules', { params }))
  },
  comorbidityRuleCreate(data: ComorbidityRuleCreateRequest) {
    return unwrap(http.post<{ code: number; data: ComorbidityRuleVO }>('/comorbidity/rules', data))
  },
  comorbidityRuleRefresh(ruleId: number) {
    return unwrap(http.post(`/comorbidity/rules/${ruleId}/refresh`))
  },
  comorbidityViews(params?: PageQuery) {
    return unwrapPage<ComorbidityViewVO>(http.get('/comorbidity/views', { params }))
  },
  qcDashboard() {
    return unwrap(http.get<{ code: number; data: QcDashboardVO }>('/quality/dashboard'))
  },
  qcReviewTasks(params?: PageQuery) {
    return unwrapPage<QcReviewTaskVO>(http.get('/quality/review-tasks', { params }))
  },
  qcReview(taskId: number, data: QcReviewRequest) {
    return unwrap(http.post(`/quality/review-tasks/${taskId}/review`, data))
  },
}

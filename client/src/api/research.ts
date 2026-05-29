import { http, unwrap, unwrapPage } from './http'
import type { PageQuery } from '@/types/api'
import type {
  CohortCreateRequest,
  CohortMemberEnrollRequest,
  CohortMemberVO,
  CohortVO,
  FollowUpCompleteRequest,
  FollowUpPlanCreateRequest,
  FollowUpPlanVO,
  FollowUpTaskVO,
  ProjectCreateRequest,
  ProjectMemberAddRequest,
  ProjectMemberVO,
  ProjectProgressVO,
  ProjectTransitionRequest,
  ProjectVO,
} from '@/types/modules/research'

export const researchApi = {
  projects(params?: PageQuery) {
    return unwrapPage<ProjectVO>(http.get('/projects', { params }))
  },
  projectGet(id: number) {
    return unwrap(http.get<{ code: number; data: ProjectVO }>(`/projects/${id}`))
  },
  projectCreate(data: ProjectCreateRequest) {
    return unwrap(http.post<{ code: number; data: ProjectVO }>('/projects', data))
  },
  projectUpdateStatus(id: number, data: ProjectTransitionRequest) {
    return unwrap(http.put<{ code: number; data: ProjectVO }>(`/projects/${id}/status`, data))
  },
  projectProgress(id: number) {
    return unwrap(http.get<{ code: number; data: ProjectProgressVO }>(`/projects/${id}/progress`))
  },
  projectMembers(id: number) {
    return unwrap(http.get<{ code: number; data: ProjectMemberVO[] }>(`/projects/${id}/members`))
  },
  projectAddMember(id: number, data: ProjectMemberAddRequest) {
    return unwrap(http.post(`/projects/${id}/members`, data))
  },
  projectRemoveMember(id: number, memberId: number) {
    return unwrap(http.delete(`/projects/${id}/members/${memberId}`))
  },
  cohorts(params?: PageQuery & { projectId?: number }) {
    return unwrapPage<CohortVO>(http.get('/cohorts', { params }))
  },
  cohortGet(id: number) {
    return unwrap(http.get<{ code: number; data: CohortVO }>(`/cohorts/${id}`))
  },
  cohortCreate(data: CohortCreateRequest) {
    return unwrap(http.post<{ code: number; data: CohortVO }>('/cohorts', data))
  },
  cohortUpdateRules(id: number, ruleJson: string) {
    return unwrap(http.put(`/cohorts/${id}/rules`, { ruleJson }))
  },
  cohortScreen(id: number) {
    return unwrap(http.post<{ code: number; data: { screened: number; enrolled: number } }>(`/cohorts/${id}/screen`))
  },
  cohortMembers(id: number, params?: PageQuery) {
    return unwrapPage<CohortMemberVO>(http.get(`/cohorts/${id}/members`, { params }))
  },
  cohortAddMember(id: number, data: CohortMemberEnrollRequest) {
    return unwrap(http.post(`/cohorts/${id}/members`, data))
  },
  cohortRemoveMember(id: number, memberId: number) {
    return unwrap(http.delete(`/cohorts/${id}/members/${memberId}`))
  },
  cohortRandomize(id: number) {
    return unwrap(http.post(`/cohorts/${id}/randomize`))
  },
  followUpPlans(params?: PageQuery & { projectId?: number }) {
    return unwrapPage<FollowUpPlanVO>(http.get('/follow-ups/plans', { params }))
  },
  followUpPlanCreate(data: FollowUpPlanCreateRequest) {
    return unwrap(http.post<{ code: number; data: FollowUpPlanVO }>('/follow-ups/plans', data))
  },
  followUpTasks(params?: PageQuery & { status?: string }) {
    return unwrapPage<FollowUpTaskVO>(http.get('/follow-ups/tasks', { params }))
  },
  followUpTaskStart(taskId: number) {
    return unwrap(http.post(`/follow-ups/tasks/${taskId}/start`))
  },
  followUpTaskComplete(taskId: number, data: FollowUpCompleteRequest) {
    return unwrap(http.put(`/follow-ups/tasks/${taskId}/complete`, data))
  },
}

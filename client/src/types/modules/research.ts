export interface ProjectVO {
  id: number
  projectCode?: string
  projectName: string
  status?: string
  designJson?: string
  templateCode?: string
  piUserId?: number
  startDate?: string
  endDate?: string
  createdAt?: string
}

export interface ProjectCreateRequest {
  projectName: string
  designJson?: string
  templateCode?: string
  startDate?: string
  endDate?: string
}

export interface ProjectTransitionRequest {
  targetStatus: string
}

export interface ProjectMemberVO {
  id: number
  projectId: number
  userId: number
  roleInProject?: string
}

export interface ProjectMemberAddRequest {
  userId: number
  roleInProject?: string
}

export interface ProjectProgressVO {
  projectId: number
  cohortCount?: number
  memberCount?: number
  pendingTasks?: number
  overdueTasks?: number
}

export interface CohortVO {
  id: number
  projectId?: number
  cohortName: string
  cohortType?: string
  ruleJson?: string
  memberCount?: number
  createdAt?: string
}

export interface CohortCreateRequest {
  projectId: number
  cohortName: string
  cohortType?: string
  ruleJson?: string
}

export interface CohortMemberVO {
  id: number
  cohortId: number
  empiId?: number
  groupLabel?: string
  enrollDate?: string
  status?: string
}

export interface CohortMemberEnrollRequest {
  empiId: number
  groupLabel?: string
}

export interface FollowUpPlanVO {
  id: number
  projectId?: number
  planName: string
  stages?: FollowUpStageVO[]
}

export interface FollowUpStageVO {
  id?: number
  stageName: string
  offsetDays?: number
  windowDays?: number
  sortOrder?: number
}

export interface FollowUpPlanCreateRequest {
  projectId: number
  planName: string
  stages?: FollowUpStageVO[]
}

export interface FollowUpTaskVO {
  id: number
  stageId?: number
  cohortMemberId?: number
  dueDate?: string
  status?: string
  channel?: string
}

export interface FollowUpCompleteRequest {
  resultJson?: string
  channel?: string
}

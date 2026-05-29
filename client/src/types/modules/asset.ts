export type SpecialtyType = 'metabolic' | 'cardio_cerebrovascular' | 'respiratory'

export interface EmpiPatientVO {
  id: number
  displayName: string
  gender?: string
  birthDate?: string
  mergeStatus?: string
  matchConfidence?: number
}

export interface EmpiMatchCandidateVO {
  id: number
  candidateEmpiId?: number
  candidateName?: string
  matchScore?: number
  matchFeatures?: string
  reviewStatus?: string
  createdAt?: string
}

export interface TimelineEventVO {
  eventType?: string
  title?: string
  detail?: string
  eventTime?: string
  sourceId?: number
}

export interface SpecialtyPatientVO {
  id: number
  empiId?: number
  specialtyType?: string
  displayName?: string
  status?: string
  coreFields?: string
  extendedFields?: string
  firstDiagnosisDate?: string
  createdAt?: string
}

export interface ComorbidityRuleVO {
  id: number
  ruleName: string
  expressionJson?: string
  status?: string
}

export interface ComorbidityRuleCreateRequest {
  ruleName: string
  expressionJson?: string
  timeWindowJson?: string
}

export interface ComorbidityViewVO {
  id: number
  ruleId?: number
  ruleName?: string
  empiId?: number
  displayName?: string
  comorbidityLabels?: string
  refreshedAt?: string
}

export interface QcDashboardVO {
  metrics?: QcMetricVO[]
  openReviewTasks?: number
  pendingCandidates?: number
}

export interface QcMetricVO {
  metricType?: string
  metricValue?: number
  threshold?: number
  alert?: boolean
}

export interface QcReviewTaskVO {
  id: number
  batchId?: number
  recordId?: number
  reviewStatus?: string
  assignedTo?: number
  createdAt?: string
}

export interface QcReviewRequest {
  approved: boolean
  comment?: string
}

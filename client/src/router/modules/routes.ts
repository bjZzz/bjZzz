import type { RouteRecordRaw } from 'vue-router'

export const platformRoutes: RouteRecordRaw[] = [
  {
    path: 'platform/orgs',
    name: 'PlatformOrgs',
    component: () => import('@/views/platform/OrgListView.vue'),
    meta: { title: '机构管理', permission: 'platform:org:read' },
  },
  {
    path: 'platform/users',
    name: 'PlatformUsers',
    component: () => import('@/views/platform/UserListView.vue'),
    meta: { title: '用户管理', permission: 'platform:user:read' },
  },
  {
    path: 'platform/roles',
    name: 'PlatformRoles',
    component: () => import('@/views/platform/RoleListView.vue'),
    meta: { title: '角色权限', permission: 'platform:user:read' },
  },
  {
    path: 'platform/audit',
    name: 'PlatformAudit',
    component: () => import('@/views/platform/AuditLogView.vue'),
    meta: { title: '审计日志', permission: 'platform:audit:read' },
  },
]

export const ingestionRoutes: RouteRecordRaw[] = [
  {
    path: 'ingestion/datasources',
    name: 'IngestionDatasources',
    component: () => import('@/views/ingestion/DataSourceListView.vue'),
    meta: { title: '数据源', permission: 'ingestion:datasource:read' },
  },
  {
    path: 'ingestion/sync-jobs',
    name: 'IngestionSyncJobs',
    component: () => import('@/views/ingestion/SyncJobListView.vue'),
    meta: { title: '同步任务', permission: 'ingestion:datasource:read' },
  },
  {
    path: 'ingestion/batches',
    name: 'IngestionBatches',
    component: () => import('@/views/ingestion/StagingBatchListView.vue'),
    meta: { title: '批次监控', permission: 'ingestion:staging:read' },
  },
]

export const governanceRoutes: RouteRecordRaw[] = [
  {
    path: 'governance/dictionaries',
    name: 'GovernanceDict',
    component: () => import('@/views/governance/DictDiagnosisView.vue'),
    meta: { title: '诊断字典', permission: 'governance:dict:read' },
  },
  {
    path: 'governance/cleaning',
    name: 'GovernanceCleaning',
    component: () => import('@/views/governance/CleaningRuleView.vue'),
    meta: { title: '清洗规则', permission: 'governance:dict:read' },
  },
  {
    path: 'governance/publish',
    name: 'GovernancePublish',
    component: () => import('@/views/governance/PublishRuleView.vue'),
    meta: { title: '发布规则', permission: 'governance:publish:execute' },
  },
  {
    path: 'governance/crf',
    name: 'GovernanceCrf',
    component: () => import('@/views/governance/CrfFormView.vue'),
    meta: { title: 'CRF 表单', permission: 'governance:crf:design' },
  },
  {
    path: 'governance/crf-entry',
    name: 'GovernanceCrfEntry',
    component: () => import('@/views/governance/CrfEntryView.vue'),
    meta: { title: 'CRF 录入', permission: 'governance:crf:entry' },
  },
  {
    path: 'governance/metadata',
    name: 'GovernanceMetadata',
    component: () => import('@/views/governance/MetadataView.vue'),
    meta: { title: '元数据', permission: 'governance:metadata:read' },
  },
]

export const assetRoutes: RouteRecordRaw[] = [
  {
    path: 'asset/empi',
    name: 'AssetEmpi',
    component: () => import('@/views/asset/EmpiView.vue'),
    meta: { title: 'EMPI', permission: 'asset:empi:read' },
  },
  {
    path: 'asset/specialty',
    name: 'AssetSpecialty',
    component: () => import('@/views/asset/SpecialtyPatientView.vue'),
    meta: { title: '专病患者', permission: 'asset:specialty:read' },
  },
  {
    path: 'asset/comorbidity',
    name: 'AssetComorbidity',
    component: () => import('@/views/asset/ComorbidityView.vue'),
    meta: { title: '共病库', permission: 'asset:comorbidity:read' },
  },
  {
    path: 'asset/qc',
    name: 'AssetQc',
    component: () => import('@/views/asset/QcView.vue'),
    meta: { title: '质控', permission: 'asset:qc:read' },
  },
]

export const researchRoutes: RouteRecordRaw[] = [
  {
    path: 'research/projects',
    name: 'ResearchProjects',
    component: () => import('@/views/research/ProjectListView.vue'),
    meta: { title: '科研项目', permission: 'research:project:read' },
  },
  {
    path: 'research/cohorts',
    name: 'ResearchCohorts',
    component: () => import('@/views/research/CohortListView.vue'),
    meta: { title: '队列管理', permission: 'research:cohort:manage' },
  },
  {
    path: 'research/follow-ups',
    name: 'ResearchFollowUps',
    component: () => import('@/views/research/FollowUpView.vue'),
    meta: { title: '随访管理', permission: 'research:followup:manage' },
  },
]

export const wave2Routes: RouteRecordRaw[] = [
  {
    path: 'analytics/search',
    name: 'AnalyticsSearch',
    component: () => import('@/views/wave2/PlaceholderView.vue'),
    meta: { title: '高级检索', permission: 'analytics:search:execute', wave: 2 },
  },
  {
    path: 'analytics/exports',
    name: 'AnalyticsExports',
    component: () => import('@/views/wave2/PlaceholderView.vue'),
    meta: { title: '导出审批', permission: 'analytics:export:create', wave: 2 },
  },
  {
    path: 'analytics/sandbox',
    name: 'AnalyticsSandbox',
    component: () => import('@/views/wave2/PlaceholderView.vue'),
    meta: { title: '沙箱 IDE', permission: 'analytics:sandbox:execute', wave: 2 },
  },
  {
    path: 'integration/upload',
    name: 'IntegrationUpload',
    component: () => import('@/views/wave2/PlaceholderView.vue'),
    meta: { title: '分中心上传', permission: 'integration:upload:write', wave: 2 },
  },
  {
    path: 'asset/cockpit',
    name: 'AssetCockpit',
    component: () => import('@/views/wave2/PlaceholderView.vue'),
    meta: { title: '运营驾驶舱', permission: 'asset:specialty:read', wave: 2 },
  },
]

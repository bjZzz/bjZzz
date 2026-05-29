<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Box,
  Collection,
  DataAnalysis,
  Document,
  Expand,
  Fold,
  Grid,
  HomeFilled,
  Link,
  Monitor,
  Setting,
  SwitchButton,
  User,
} from '@element-plus/icons-vue'
import OrgTreeSelect from '@/components/OrgTreeSelect.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const auth = useAuthStore()

const selectedOrg = ref<number | null>(auth.orgId)

watch(
  () => auth.orgId,
  (v) => {
    selectedOrg.value = v
  },
)

function onOrgChange(orgId: number | null) {
  if (orgId) {
    auth.switchOrg(orgId)
    router.go(0)
  }
}

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}

interface MenuItem {
  title: string
  path?: string
  icon?: unknown
  permission?: string
  wave?: number
  children?: MenuItem[]
}

const menuGroups: MenuItem[] = [
  { title: '工作台', path: '/dashboard', icon: HomeFilled },
  {
    title: '平台管理',
    icon: Setting,
    children: [
      { title: '机构管理', path: '/platform/orgs', permission: 'platform:org:read' },
      { title: '用户管理', path: '/platform/users', permission: 'platform:user:read' },
      { title: '角色权限', path: '/platform/roles', permission: 'platform:user:read' },
      { title: '审计日志', path: '/platform/audit', permission: 'platform:audit:read' },
    ],
  },
  {
    title: '数据采集',
    icon: Collection,
    children: [
      { title: '数据源', path: '/ingestion/datasources', permission: 'ingestion:datasource:read' },
      { title: '同步任务', path: '/ingestion/sync-jobs', permission: 'ingestion:datasource:read' },
      { title: '批次监控', path: '/ingestion/batches', permission: 'ingestion:staging:read' },
    ],
  },
  {
    title: '数据治理',
    icon: Document,
    children: [
      { title: '诊断字典', path: '/governance/dictionaries', permission: 'governance:dict:read' },
      { title: '清洗规则', path: '/governance/cleaning', permission: 'governance:dict:read' },
      { title: '发布规则', path: '/governance/publish', permission: 'governance:publish:execute' },
      { title: 'CRF 表单', path: '/governance/crf', permission: 'governance:crf:design' },
      { title: 'CRF 录入', path: '/governance/crf-entry', permission: 'governance:crf:entry' },
      { title: '元数据', path: '/governance/metadata', permission: 'governance:metadata:read' },
    ],
  },
  {
    title: '数据资产',
    icon: Box,
    children: [
      { title: 'EMPI', path: '/asset/empi', permission: 'asset:empi:read' },
      { title: '专病患者', path: '/asset/specialty', permission: 'asset:specialty:read' },
      { title: '共病库', path: '/asset/comorbidity', permission: 'asset:comorbidity:read' },
      { title: '质控', path: '/asset/qc', permission: 'asset:qc:read' },
      { title: '运营驾驶舱', path: '/asset/cockpit', permission: 'asset:specialty:read', wave: 2 },
    ],
  },
  {
    title: '科研协作',
    icon: Grid,
    children: [
      { title: '科研项目', path: '/research/projects', permission: 'research:project:read' },
      { title: '队列管理', path: '/research/cohorts', permission: 'research:cohort:manage' },
      { title: '随访管理', path: '/research/follow-ups', permission: 'research:followup:manage' },
    ],
  },
  {
    title: '分析应用',
    icon: DataAnalysis,
    children: [
      { title: '高级检索', path: '/analytics/search', permission: 'analytics:search:execute', wave: 2 },
      { title: '导出审批', path: '/analytics/exports', permission: 'analytics:export:create', wave: 2 },
      { title: '沙箱 IDE', path: '/analytics/sandbox', permission: 'analytics:sandbox:execute', wave: 2 },
    ],
  },
  {
    title: '外部集成',
    icon: Link,
    children: [
      { title: '分中心上传', path: '/integration/upload', permission: 'integration:upload:write', wave: 2 },
    ],
  },
]

function filterMenu(items: MenuItem[]): MenuItem[] {
  return items
    .map((item) => {
      if (item.children) {
        const children = filterMenu(item.children)
        return children.length ? { ...item, children } : null
      }
      if (item.permission && !auth.hasPermission(item.permission)) return null
      return item
    })
    .filter(Boolean) as MenuItem[]
}

const visibleMenus = computed(() => filterMenu(menuGroups))

const breadcrumbs = computed(() => {
  const matched = route.matched.filter((r) => r.meta?.title)
  return matched.map((r) => ({ title: r.meta.title as string, path: r.path }))
})
</script>

<template>
  <el-container class="h-full">
    <el-aside :width="app.sidebarCollapsed ? '64px' : '220px'" class="border-r border-slate-200 bg-white transition-all">
      <div class="flex h-14 items-center justify-center border-b border-slate-200 px-3">
        <Monitor v-if="app.sidebarCollapsed" class="h-6 w-6 text-blue-600" />
        <span v-else class="text-sm font-bold text-blue-700">共病专病科研平台</span>
      </div>
      <el-menu :default-active="route.path" :collapse="app.sidebarCollapsed" router class="border-none">
        <template v-for="group in visibleMenus" :key="group.title">
          <el-menu-item v-if="group.path" :index="group.path">
            <el-icon v-if="group.icon"><component :is="group.icon" /></el-icon>
            <span>{{ group.title }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="group.title">
            <template #title>
              <el-icon v-if="group.icon"><component :is="group.icon" /></el-icon>
              <span>{{ group.title }}</span>
            </template>
            <el-menu-item v-for="child in group.children" :key="child.path" :index="child.path!">
              <span>{{ child.title }}</span>
              <el-tag v-if="child.wave === 2" size="small" type="info" class="ml-2">W2</el-tag>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="flex h-14 items-center justify-between border-b border-slate-200 bg-white px-4">
        <div class="flex items-center gap-3">
          <el-button text @click="app.toggleSidebar">
            <el-icon><Fold v-if="!app.sidebarCollapsed" /><Expand v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="bc in breadcrumbs" :key="bc.path">{{ bc.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="flex items-center gap-4">
          <OrgTreeSelect v-model="selectedOrg" @update:model-value="onOrgChange" />
          <span class="flex items-center gap-1 text-sm text-slate-600">
            <el-icon><User /></el-icon>
            {{ auth.displayName || auth.username }}
          </span>
          <el-button text type="danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </el-header>
      <el-main class="bg-slate-50 p-4">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

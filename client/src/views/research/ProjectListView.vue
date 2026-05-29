<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { researchApi } from '@/api/research'
import type { ProjectCreateRequest, ProjectMemberVO, ProjectProgressVO, ProjectVO } from '@/types/modules/research'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const detailVisible = ref(false)
const current = ref<ProjectVO | null>(null)
const progress = ref<ProjectProgressVO | null>(null)
const members = ref<ProjectMemberVO[]>([])
const form = reactive<ProjectCreateRequest>({ projectName: '', templateCode: 'DEFAULT' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await researchApi.projects({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await researchApi.projectCreate(form)
  ElMessage.success('项目已创建')
  dialogVisible.value = false
  load()
}

async function showDetail(row: ProjectVO) {
  current.value = row
  ;[progress.value, members.value] = await Promise.all([
    researchApi.projectProgress(row.id),
    researchApi.projectMembers(row.id),
  ])
  detailVisible.value = true
}

async function changeStatus(status: string) {
  if (!current.value) return
  await researchApi.projectUpdateStatus(current.value.id, { targetStatus: status })
  ElMessage.success('状态已更新')
  current.value = await researchApi.projectGet(current.value.id)
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="科研项目">
      <template #actions>
        <PermButton permission="research:project:write">
          <el-button type="primary" @click="dialogVisible = true">新建项目</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as ProjectVO[]" stripe>
      <el-table-column prop="projectCode" label="编码" />
      <el-table-column prop="projectName" label="名称" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column prop="startDate" label="开始" width="120" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }"><el-button link type="primary" @click="showDetail(row)">详情</el-button></template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建项目" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.projectName" /></el-form-item>
        <el-form-item label="模板"><el-input v-model="form.templateCode" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
    <el-drawer v-model="detailVisible" title="项目详情" size="520px">
      <div v-if="current" class="space-y-4">
        <h3 class="font-semibold">{{ current.projectName }}</h3>
        <StatusTag :status="current.status" />
        <div v-if="progress" class="grid grid-cols-2 gap-2 text-sm">
          <div>队列数: {{ progress.cohortCount }}</div>
          <div>成员数: {{ progress.memberCount }}</div>
          <div>待办随访: {{ progress.pendingTasks }}</div>
          <div>逾期: {{ progress.overdueTasks }}</div>
        </div>
        <PermButton permission="research:project:write">
          <el-button size="small" @click="changeStatus('ACTIVE')">激活</el-button>
          <el-button size="small" @click="changeStatus('COMPLETED')">完成</el-button>
        </PermButton>
        <h4 class="font-medium">成员</h4>
        <el-table :data="members" size="small">
          <el-table-column prop="userId" label="用户ID" />
          <el-table-column prop="roleInProject" label="角色" />
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

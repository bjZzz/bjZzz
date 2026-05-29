<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { researchApi } from '@/api/research'
import type { FollowUpPlanCreateRequest, FollowUpPlanVO, FollowUpTaskVO } from '@/types/modules/research'

const activeTab = ref('tasks')
const { page, size, total, loading, items, applyPageResult } = usePagination()
const plans = ref<FollowUpPlanVO[]>([])
const planDialog = ref(false)
const planForm = reactive<FollowUpPlanCreateRequest>({
  projectId: 1,
  planName: '',
  stages: [{ stageName: '基线', offsetDays: 0, windowDays: 7, sortOrder: 1 }],
})

async function loadTasks() {
  loading.value = true
  try {
    applyPageResult(await researchApi.followUpTasks({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function loadPlans() {
  plans.value = (await researchApi.followUpPlans({ page: 1, size: 50 })).items
}

async function createPlan() {
  await researchApi.followUpPlanCreate(planForm)
  ElMessage.success('随访计划已创建')
  planDialog.value = false
  loadPlans()
}

async function startTask(row: FollowUpTaskVO) {
  await researchApi.followUpTaskStart(row.id)
  ElMessage.success('任务已开始')
  loadTasks()
}

async function completeTask(row: FollowUpTaskVO) {
  await researchApi.followUpTaskComplete(row.id, { resultJson: '{}', channel: 'PHONE' })
  ElMessage.success('任务已完成')
  loadTasks()
}

async function onTabChange(name: string | number) {
  if (name === 'tasks') await loadTasks()
  else await loadPlans()
}

onMounted(loadTasks)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="随访管理">
      <template #actions>
        <el-button type="primary" @click="planDialog = true">新建计划</el-button>
      </template>
    </PageHeader>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="随访任务" name="tasks">
        <el-table v-loading="loading" :data="items as FollowUpTaskVO[]" stripe>
          <el-table-column prop="id" label="任务ID" width="90" />
          <el-table-column prop="dueDate" label="到期日" />
          <el-table-column prop="channel" label="渠道" />
          <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button link type="primary" @click="startTask(row)">开始</el-button>
              <el-button link @click="completeTask(row)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="mt-4 flex justify-end">
          <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="loadTasks" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="随访计划" name="plans">
        <el-table :data="plans" stripe>
          <el-table-column prop="planName" label="计划名称" />
          <el-table-column prop="projectId" label="项目ID" width="90" />
          <el-table-column label="阶段数">
            <template #default="{ row }">{{ row.stages?.length ?? 0 }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="planDialog" title="新建随访计划" width="520px">
      <el-form :model="planForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="planForm.projectId" :min="1" class="w-full" /></el-form-item>
        <el-form-item label="计划名"><el-input v-model="planForm.planName" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="planDialog = false">取消</el-button><el-button type="primary" @click="createPlan">保存</el-button></template>
    </el-dialog>
  </div>
</template>

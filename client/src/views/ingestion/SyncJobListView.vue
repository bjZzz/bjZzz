<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { ingestionApi } from '@/api/ingestion'
import type { SyncJobCreateRequest, SyncJobVO } from '@/types/modules/ingestion'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const form = reactive<SyncJobCreateRequest>({ sourceId: 0, jobName: '', cronExpression: '0 0 2 * * ?', syncMode: 'T1' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await ingestionApi.syncJobs({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await ingestionApi.syncJobCreate(form)
  ElMessage.success('同步任务已创建')
  dialogVisible.value = false
  load()
}

async function startJob(row: SyncJobVO) {
  await ingestionApi.syncJobStart(row.id)
  ElMessage.success('任务已启动')
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="同步任务" description="配置采集调度与手动执行">
      <template #actions>
        <PermButton permission="ingestion:datasource:write">
          <el-button type="primary" @click="dialogVisible = true">新建任务</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as SyncJobVO[]" stripe>
      <el-table-column prop="jobName" label="任务名称" />
      <el-table-column prop="sourceId" label="数据源ID" width="100" />
      <el-table-column prop="syncMode" label="模式" width="80" />
      <el-table-column prop="cronExpression" label="Cron" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <PermButton permission="ingestion:sync:execute">
            <el-button link type="primary" @click="startJob(row)">立即执行</el-button>
          </PermButton>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建同步任务" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="数据源ID"><el-input-number v-model="form.sourceId" :min="1" class="w-full" /></el-form-item>
        <el-form-item label="任务名称"><el-input v-model="form.jobName" /></el-form-item>
        <el-form-item label="Cron"><el-input v-model="form.cronExpression" /></el-form-item>
        <el-form-item label="模式">
          <el-select v-model="form.syncMode" class="w-full">
            <el-option label="T+1" value="T1" />
            <el-option label="T+7" value="T7" />
            <el-option label="实时" value="REALTIME" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

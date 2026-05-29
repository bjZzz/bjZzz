<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { assetApi } from '@/api/asset'
import type { QcDashboardVO, QcReviewTaskVO } from '@/types/modules/asset'

const dashboard = ref<QcDashboardVO | null>(null)
const { page, size, total, loading, items, applyPageResult } = usePagination()

async function loadDashboard() {
  dashboard.value = await assetApi.qcDashboard()
}

async function loadTasks() {
  loading.value = true
  try {
    applyPageResult(await assetApi.qcReviewTasks({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function review(row: QcReviewTaskVO, approved: boolean) {
  await assetApi.qcReview(row.id, { approved, comment: approved ? '通过' : '驳回' })
  ElMessage.success('审核已提交')
  loadTasks()
  loadDashboard()
}

onMounted(() => {
  loadDashboard()
  loadTasks()
})
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-xl bg-white p-4 shadow-sm">
      <PageHeader title="质控看板" />
      <div v-if="dashboard" class="grid gap-4 md:grid-cols-4">
        <div class="rounded-lg border p-4">
          <div class="text-sm text-slate-500">待审任务</div>
          <div class="text-2xl font-bold">{{ dashboard.openReviewTasks ?? 0 }}</div>
        </div>
        <div class="rounded-lg border p-4">
          <div class="text-sm text-slate-500">待确认候选</div>
          <div class="text-2xl font-bold">{{ dashboard.pendingCandidates ?? 0 }}</div>
        </div>
        <div v-for="m in dashboard.metrics?.slice(0, 2)" :key="m.metricType" class="rounded-lg border p-4" :class="{ 'border-red-300 bg-red-50': m.alert }">
          <div class="text-sm text-slate-500">{{ m.metricType }}</div>
          <div class="text-2xl font-bold">{{ m.metricValue }} <span class="text-sm font-normal text-slate-400">/ {{ m.threshold }}</span></div>
        </div>
      </div>
    </div>
    <div class="rounded-xl bg-white p-4 shadow-sm">
      <PageHeader title="质控审核任务" />
      <el-table v-loading="loading" :data="items as QcReviewTaskVO[]" stripe>
        <el-table-column prop="id" label="任务ID" width="90" />
        <el-table-column prop="batchId" label="批次" width="90" />
        <el-table-column prop="recordId" label="记录" width="90" />
        <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.reviewStatus" /></template></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <PermButton permission="asset:qc:write">
              <el-button link type="primary" @click="review(row, true)">通过</el-button>
              <el-button link type="danger" @click="review(row, false)">驳回</el-button>
            </PermButton>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="loadTasks" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { ingestionApi } from '@/api/ingestion'
import type { StagingBatchVO } from '@/types/modules/ingestion'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const detailVisible = ref(false)
const current = ref<StagingBatchVO | null>(null)

async function load() {
  loading.value = true
  try {
    applyPageResult(await ingestionApi.stagingBatches({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function showDetail(row: StagingBatchVO) {
  current.value = await ingestionApi.stagingBatchGet(row.id)
  detailVisible.value = true
}

async function retry(row: StagingBatchVO) {
  await ingestionApi.stagingBatchRetry(row.id)
  ElMessage.success('重试已提交')
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="批次监控" description="Staging 批次状态与重试" />
    <el-table v-loading="loading" :data="items as StagingBatchVO[]" stripe>
      <el-table-column prop="id" label="批次ID" width="90" />
      <el-table-column prop="sourceId" label="数据源" width="90" />
      <el-table-column prop="recordCount" label="记录数" width="90" />
      <el-table-column prop="successCount" label="成功" width="80" />
      <el-table-column prop="failCount" label="失败" width="80" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column prop="receivedAt" label="接收时间" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          <PermButton permission="ingestion:sync:execute">
            <el-button link @click="retry(row)">重试</el-button>
          </PermButton>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-drawer v-model="detailVisible" title="批次详情" size="480px">
      <dl v-if="current" class="space-y-3 text-sm">
        <div><dt class="text-slate-500">状态</dt><dd><StatusTag :status="current.status" /></dd></div>
        <div><dt class="text-slate-500">错误信息</dt><dd>{{ current.errorMessage || '-' }}</dd></div>
        <div><dt class="text-slate-500">创建时间</dt><dd>{{ current.createdAt || '-' }}</dd></div>
      </dl>
    </el-drawer>
  </div>
</template>

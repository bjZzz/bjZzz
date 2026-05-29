<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { usePagination } from '@/composables/usePagination'
import { platformApi } from '@/api/platform'
import type { AuditLogVO } from '@/types/modules/platform'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const query = reactive({ username: '', action: '' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await platformApi.auditLogs({ page: page.value, size: size.value, ...query }))
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="审计日志" />
    <div class="mb-4 flex gap-2">
      <el-input v-model="query.username" placeholder="用户名" clearable class="w-40" />
      <el-input v-model="query.action" placeholder="操作" clearable class="w-40" />
      <el-button @click="load">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="items as AuditLogVO[]" stripe>
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="action" label="操作" width="120" />
      <el-table-column prop="resourceType" label="资源类型" width="120" />
      <el-table-column prop="resourceId" label="资源ID" width="120" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP" width="140" />
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
  </div>
</template>

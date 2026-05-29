<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { ingestionApi } from '@/api/ingestion'
import type { DataSourceCreateRequest, DataSourceVO } from '@/types/modules/ingestion'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const form = reactive<DataSourceCreateRequest>({ sourceCode: '', sourceName: '', protocol: 'JDBC', configJson: '{}' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await ingestionApi.datasources({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await ingestionApi.datasourceCreate(form)
  ElMessage.success('数据源已创建')
  dialogVisible.value = false
  load()
}

async function testConnection(row: DataSourceVO) {
  const res = await ingestionApi.datasourceTestConnection(row.id)
  ElMessage[res.success ? 'success' : 'error'](res.message || (res.success ? '连接成功' : '连接失败'))
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="数据源管理" description="配置多源数据采集连接">
      <template #actions>
        <PermButton permission="ingestion:datasource:write">
          <el-button type="primary" @click="dialogVisible = true">新建数据源</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as DataSourceVO[]" stripe>
      <el-table-column prop="sourceCode" label="编码" />
      <el-table-column prop="sourceName" label="名称" />
      <el-table-column prop="protocol" label="协议" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <PermButton permission="ingestion:datasource:write">
            <el-button link type="primary" @click="testConnection(row)">测试连接</el-button>
          </PermButton>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建数据源" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编码"><el-input v-model="form.sourceCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.sourceName" /></el-form-item>
        <el-form-item label="协议">
          <el-select v-model="form.protocol" class="w-full">
            <el-option label="JDBC" value="JDBC" />
            <el-option label="FILE" value="FILE" />
            <el-option label="HL7" value="HL7" />
            <el-option label="FHIR" value="FHIR" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置"><el-input v-model="form.configJson" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

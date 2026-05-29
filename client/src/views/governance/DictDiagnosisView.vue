<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import { usePagination } from '@/composables/usePagination'
import { governanceApi } from '@/api/governance'
import type { DictDiagnosisCreateRequest, DictDiagnosisVO } from '@/types/modules/governance'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const form = reactive<DictDiagnosisCreateRequest>({ code: '', nameZh: '', nameEn: '' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await governanceApi.diagnosisDict({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await governanceApi.diagnosisDictCreate(form)
  ElMessage.success('字典项已添加')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="诊断字典">
      <template #actions>
        <PermButton permission="governance:dict:write">
          <el-button type="primary" @click="dialogVisible = true">新增</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as DictDiagnosisVO[]" stripe>
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="nameZh" label="中文名" />
      <el-table-column prop="nameEn" label="英文名" />
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新增诊断字典" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="中文名"><el-input v-model="form.nameZh" /></el-form-item>
        <el-form-item label="英文名"><el-input v-model="form.nameEn" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

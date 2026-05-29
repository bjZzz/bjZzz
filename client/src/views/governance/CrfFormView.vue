<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { governanceApi } from '@/api/governance'
import type { CrfFormCreateRequest, CrfFormVO } from '@/types/modules/governance'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const form = reactive<CrfFormCreateRequest>({
  formCode: '',
  formName: '',
  specialtyType: 'metabolic',
  schemaJson: '{"fields":[]}',
})

async function load() {
  loading.value = true
  try {
    applyPageResult(await governanceApi.crfForms({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await governanceApi.crfFormCreate(form)
  ElMessage.success('CRF 表单已创建')
  dialogVisible.value = false
  load()
}

async function publish(row: CrfFormVO) {
  await governanceApi.crfFormPublish(row.id)
  ElMessage.success('表单已发布')
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="CRF 表单" description="基础版：JSON Schema 编辑与发布">
      <template #actions>
        <PermButton permission="governance:crf:design">
          <el-button type="primary" @click="dialogVisible = true">新建表单</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as CrfFormVO[]" stripe>
      <el-table-column prop="formCode" label="编码" />
      <el-table-column prop="formName" label="名称" />
      <el-table-column prop="specialtyType" label="专病类型" />
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <PermButton permission="governance:crf:design">
            <el-button v-if="row.status !== 'PUBLISHED'" link type="primary" @click="publish(row)">发布</el-button>
          </PermButton>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建 CRF 表单" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码"><el-input v-model="form.formCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.formName" /></el-form-item>
        <el-form-item label="专病类型"><el-input v-model="form.specialtyType" /></el-form-item>
        <el-form-item label="Schema"><el-input v-model="form.schemaJson" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

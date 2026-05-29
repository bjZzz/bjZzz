<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { governanceApi } from '@/api/governance'
import type { CrfFormVO, CrfResponseVO } from '@/types/modules/governance'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const forms = ref<CrfFormVO[]>([])
const submitVisible = ref(false)
const submitForm = reactive({ formId: 0, empiId: undefined as number | undefined, responseJson: '{}' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await governanceApi.crfResponses({ page: page.value, size: size.value }))
    forms.value = (await governanceApi.crfForms({ page: 1, size: 100 })).items
  } finally {
    loading.value = false
  }
}

async function submit() {
  await governanceApi.crfResponseSubmit(submitForm)
  ElMessage.success('录入已提交')
  submitVisible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="CRF 录入">
      <template #actions>
        <el-button type="primary" @click="submitVisible = true">新建录入</el-button>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as CrfResponseVO[]" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="formId" label="表单ID" width="90" />
      <el-table-column prop="empiId" label="EMPI" width="90" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" />
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="submitVisible" title="CRF 录入" width="520px">
      <el-form :model="submitForm" label-width="90px">
        <el-form-item label="表单">
          <el-select v-model="submitForm.formId" class="w-full">
            <el-option v-for="f in forms" :key="f.id" :label="f.formName" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="EMPI ID"><el-input-number v-model="submitForm.empiId" :min="1" class="w-full" /></el-form-item>
        <el-form-item label="响应 JSON"><el-input v-model="submitForm.responseJson" type="textarea" :rows="5" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="submitVisible = false">取消</el-button><el-button type="primary" @click="submit">提交</el-button></template>
    </el-dialog>
  </div>
</template>

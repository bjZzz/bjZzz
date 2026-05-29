<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { governanceApi } from '@/api/governance'
import type { CleaningRuleCreateRequest, CleaningRuleVO } from '@/types/modules/governance'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const form = reactive<CleaningRuleCreateRequest>({ ruleName: '', ruleType: 'VALIDATION', expressionJson: '{}' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await governanceApi.cleaningRules({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await governanceApi.cleaningRuleCreate(form)
  ElMessage.success('清洗规则已创建')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="清洗规则">
      <template #actions>
        <PermButton permission="governance:dict:write">
          <el-button type="primary" @click="dialogVisible = true">新建规则</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as CleaningRuleVO[]" stripe>
      <el-table-column prop="ruleName" label="规则名称" />
      <el-table-column prop="ruleType" label="类型" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建清洗规则" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.ruleType" /></el-form-item>
        <el-form-item label="表达式"><el-input v-model="form.expressionJson" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

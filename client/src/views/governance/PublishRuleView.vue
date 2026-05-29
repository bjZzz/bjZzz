<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { governanceApi } from '@/api/governance'
import type { PublishRuleCreateRequest, PublishRuleVO } from '@/types/modules/governance'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const form = reactive<PublishRuleCreateRequest>({ ruleName: '', targetTable: '', mappingJson: '{}' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await governanceApi.publishRules({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await governanceApi.publishRuleCreate(form)
  ElMessage.success('发布规则已创建')
  dialogVisible.value = false
  load()
}

async function execute(_row: PublishRuleVO) {
  const { value } = await ElMessageBox.prompt('输入批次 ID 执行发布', '执行发布', { inputPattern: /^\d+$/, inputErrorMessage: '请输入数字' })
  await governanceApi.publishExecute(Number(value))
  ElMessage.success('发布任务已提交')
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="发布规则">
      <template #actions>
        <PermButton permission="governance:publish:execute">
          <el-button type="primary" @click="dialogVisible = true">新建规则</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as PublishRuleVO[]" stripe>
      <el-table-column prop="ruleName" label="规则名称" />
      <el-table-column prop="targetTable" label="目标表" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <PermButton permission="governance:publish:execute">
            <el-button link type="primary" @click="execute(row)">执行</el-button>
          </PermButton>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建发布规则" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="目标表"><el-input v-model="form.targetTable" /></el-form-item>
        <el-form-item label="映射 JSON"><el-input v-model="form.mappingJson" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

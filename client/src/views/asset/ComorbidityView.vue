<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { assetApi } from '@/api/asset'
import type { ComorbidityRuleCreateRequest, ComorbidityRuleVO, ComorbidityViewVO } from '@/types/modules/asset'

const activeTab = ref('rules')
const { page, size, total, loading, items, applyPageResult } = usePagination()
const views = ref<ComorbidityViewVO[]>([])
const viewsLoading = ref(false)
const dialogVisible = ref(false)
const form = reactive<ComorbidityRuleCreateRequest>({ ruleName: '', expressionJson: '{}', timeWindowJson: '{}' })

async function loadRules() {
  loading.value = true
  try {
    applyPageResult(await assetApi.comorbidityRules({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function loadViews() {
  viewsLoading.value = true
  try {
    views.value = (await assetApi.comorbidityViews({ page: 1, size: 50 })).items
  } finally {
    viewsLoading.value = false
  }
}

async function submit() {
  await assetApi.comorbidityRuleCreate(form)
  ElMessage.success('规则已创建')
  dialogVisible.value = false
  loadRules()
}

async function refresh(row: ComorbidityRuleVO) {
  await assetApi.comorbidityRuleRefresh(row.id)
  ElMessage.success('视图刷新已提交')
}

async function onTabChange(name: string | number) {
  if (name === 'rules') await loadRules()
  else await loadViews()
}

onMounted(loadRules)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="共病库">
      <template #actions>
        <PermButton permission="asset:comorbidity:write">
          <el-button type="primary" @click="dialogVisible = true">新建规则</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="共病规则" name="rules">
        <el-table v-loading="loading" :data="items as ComorbidityRuleVO[]" stripe>
          <el-table-column prop="ruleName" label="规则名称" />
          <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <PermButton permission="asset:comorbidity:write">
                <el-button link type="primary" @click="refresh(row)">刷新</el-button>
              </PermButton>
            </template>
          </el-table-column>
        </el-table>
        <div class="mt-4 flex justify-end">
          <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="loadRules" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="共病视图" name="views">
        <el-table v-loading="viewsLoading" :data="views" stripe>
          <el-table-column prop="displayName" label="患者" />
          <el-table-column prop="ruleName" label="规则" />
          <el-table-column prop="comorbidityLabels" label="共病标签" />
          <el-table-column prop="refreshedAt" label="刷新时间" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="dialogVisible" title="新建共病规则" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="表达式"><el-input v-model="form.expressionJson" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="时间窗"><el-input v-model="form.timeWindowJson" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

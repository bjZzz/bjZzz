<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import { usePagination } from '@/composables/usePagination'
import { researchApi } from '@/api/research'
import type { CohortCreateRequest, CohortMemberVO, CohortVO } from '@/types/modules/research'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const dialogVisible = ref(false)
const membersVisible = ref(false)
const currentCohort = ref<CohortVO | null>(null)
const members = ref<CohortMemberVO[]>([])
const form = reactive<CohortCreateRequest>({ projectId: 1, cohortName: '', cohortType: 'INCLUSION', ruleJson: '{}' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await researchApi.cohorts({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await researchApi.cohortCreate(form)
  ElMessage.success('队列已创建')
  dialogVisible.value = false
  load()
}

async function screen(row: CohortVO) {
  const res = await researchApi.cohortScreen(row.id)
  ElMessage.success(`筛选完成：${res.screened} 人，入组 ${res.enrolled} 人`)
  load()
}

async function showMembers(row: CohortVO) {
  currentCohort.value = row
  members.value = (await researchApi.cohortMembers(row.id, { page: 1, size: 100 })).items
  membersVisible.value = true
}

async function randomize(row: CohortVO) {
  await researchApi.cohortRandomize(row.id)
  ElMessage.success('随机分组已完成')
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="队列管理">
      <template #actions>
        <PermButton permission="research:cohort:manage">
          <el-button type="primary" @click="dialogVisible = true">新建队列</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-table v-loading="loading" :data="items as CohortVO[]" stripe>
      <el-table-column prop="cohortName" label="队列名称" />
      <el-table-column prop="projectId" label="项目ID" width="90" />
      <el-table-column prop="cohortType" label="类型" />
      <el-table-column prop="memberCount" label="成员数" width="90" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="showMembers(row)">成员</el-button>
          <el-button link @click="screen(row)">筛选</el-button>
          <el-button link @click="randomize(row)">随机化</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建队列" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="form.projectId" :min="1" class="w-full" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.cohortName" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.cohortType" /></el-form-item>
        <el-form-item label="规则"><el-input v-model="form.ruleJson" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
    <el-drawer v-model="membersVisible" title="队列成员" size="480px">
      <el-table :data="members" size="small">
        <el-table-column prop="empiId" label="EMPI" />
        <el-table-column prop="groupLabel" label="分组" />
        <el-table-column prop="status" label="状态" />
      </el-table>
    </el-drawer>
  </div>
</template>

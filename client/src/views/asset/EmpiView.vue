<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { assetApi } from '@/api/asset'
import type { EmpiMatchCandidateVO, EmpiPatientVO, TimelineEventVO } from '@/types/modules/asset'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const searchId = ref<number>()
const patient = ref<EmpiPatientVO | null>(null)
const timeline = ref<TimelineEventVO[]>([])
const detailVisible = ref(false)

async function loadCandidates() {
  loading.value = true
  try {
    applyPageResult(await assetApi.empiMatchCandidates({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function searchPatient() {
  if (!searchId.value) return
  patient.value = await assetApi.empiPatient(searchId.value)
  timeline.value = await assetApi.empiTimeline(searchId.value)
  detailVisible.value = true
}

async function confirm(row: EmpiMatchCandidateVO) {
  await assetApi.empiConfirmCandidate(row.id)
  ElMessage.success('已确认合并')
  loadCandidates()
}

async function reject(row: EmpiMatchCandidateVO) {
  await assetApi.empiRejectCandidate(row.id)
  ElMessage.success('已拒绝')
  loadCandidates()
}

loadCandidates()
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-xl bg-white p-4 shadow-sm">
      <PageHeader title="EMPI 患者检索" />
      <div class="flex gap-2">
        <el-input-number v-model="searchId" :min="1" placeholder="EMPI ID" />
        <el-button type="primary" @click="searchPatient">查询</el-button>
      </div>
    </div>
    <div class="rounded-xl bg-white p-4 shadow-sm">
      <PageHeader title="匹配候选" description="人工确认 EMPI 合并" />
      <el-table v-loading="loading" :data="items as EmpiMatchCandidateVO[]" stripe>
        <el-table-column prop="candidateName" label="候选姓名" />
        <el-table-column prop="matchScore" label="匹配分" width="100" />
        <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.reviewStatus" /></template></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <PermButton permission="asset:empi:match">
              <el-button link type="primary" @click="confirm(row)">确认</el-button>
              <el-button link type="danger" @click="reject(row)">拒绝</el-button>
            </PermButton>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="loadCandidates" />
      </div>
    </div>
    <el-drawer v-model="detailVisible" title="患者详情" size="480px">
      <div v-if="patient" class="space-y-4">
        <div><strong>{{ patient.displayName }}</strong> · {{ patient.gender }} · {{ patient.birthDate }}</div>
        <h4 class="font-medium">时间线</h4>
        <el-timeline>
          <el-timeline-item v-for="(ev, i) in timeline" :key="i" :timestamp="ev.eventTime">{{ ev.title }} — {{ ev.detail }}</el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

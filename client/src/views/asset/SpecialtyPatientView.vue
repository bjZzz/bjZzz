<script setup lang="ts">
import { ref, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import JsonFieldViewer from '@/components/JsonFieldViewer.vue'
import SpecialtyTypeTabs from '@/components/SpecialtyTypeTabs.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { assetApi } from '@/api/asset'
import type { SpecialtyPatientVO, SpecialtyType } from '@/types/modules/asset'

const specialtyType = ref<SpecialtyType>('metabolic')
const { page, size, total, loading, items, applyPageResult } = usePagination()
const detailVisible = ref(false)
const current = ref<SpecialtyPatientVO | null>(null)

async function load() {
  loading.value = true
  try {
    applyPageResult(await assetApi.specialtyPatients(specialtyType.value, { page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function showDetail(row: SpecialtyPatientVO) {
  current.value = await assetApi.specialtyPatient(specialtyType.value, row.id)
  detailVisible.value = true
}

watch(specialtyType, () => {
  page.value = 1
  load()
})

load()
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="专病患者" description="三类专病库患者列表" />
    <SpecialtyTypeTabs v-model="specialtyType" />
    <el-table v-loading="loading" :data="items as SpecialtyPatientVO[]" stripe>
      <el-table-column prop="displayName" label="姓名" />
      <el-table-column prop="empiId" label="EMPI" width="90" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column prop="firstDiagnosisDate" label="首诊日期" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }"><el-button link type="primary" @click="showDetail(row)">详情</el-button></template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-drawer v-model="detailVisible" title="专病详情" size="520px">
      <div v-if="current" class="space-y-4">
        <dl class="grid grid-cols-2 gap-2 text-sm">
          <div><dt class="text-slate-500">姓名</dt><dd>{{ current.displayName }}</dd></div>
          <div><dt class="text-slate-500">EMPI</dt><dd>{{ current.empiId }}</dd></div>
        </dl>
        <div><h4 class="mb-2 font-medium">核心字段</h4><JsonFieldViewer :data="current.coreFields" /></div>
        <div><h4 class="mb-2 font-medium">扩展字段</h4><JsonFieldViewer :data="current.extendedFields" /></div>
      </div>
    </el-drawer>
  </div>
</template>

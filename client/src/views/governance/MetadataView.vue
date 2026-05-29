<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { usePagination } from '@/composables/usePagination'
import { governanceApi } from '@/api/governance'
import type { LineageEdgeVO, MetadataCatalogVO } from '@/types/modules/governance'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const lineage = ref<LineageEdgeVO[]>([])
const activeTab = ref('catalog')

async function loadCatalog() {
  loading.value = true
  try {
    applyPageResult(await governanceApi.metadataCatalog({ page: page.value, size: size.value }))
  } finally {
    loading.value = false
  }
}

async function loadLineage() {
  lineage.value = await governanceApi.metadataLineage()
}

async function onTabChange(name: string | number) {
  if (name === 'catalog') await loadCatalog()
  else await loadLineage()
}

onMounted(loadCatalog)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="元数据" description="目录与血缘（只读）" />
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="元数据目录" name="catalog">
        <el-table v-loading="loading" :data="items as MetadataCatalogVO[]" stripe>
          <el-table-column prop="tableName" label="表名" />
          <el-table-column prop="columnName" label="字段" />
          <el-table-column prop="dataType" label="类型" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="sourceSystem" label="来源" />
        </el-table>
        <div class="mt-4 flex justify-end">
          <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="loadCatalog" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="血缘关系" name="lineage">
        <el-table :data="lineage" stripe>
          <el-table-column prop="sourceTable" label="源表" />
          <el-table-column prop="targetTable" label="目标表" />
          <el-table-column prop="transformType" label="转换类型" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

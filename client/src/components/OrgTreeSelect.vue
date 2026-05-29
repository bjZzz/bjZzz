<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { platformApi } from '@/api/platform'
import type { OrgTreeNode } from '@/types/modules/platform'

const props = defineProps<{ modelValue?: number | null }>()
const emit = defineEmits<{ 'update:modelValue': [number | null] }>()
const tree = ref<OrgTreeNode[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    tree.value = await platformApi.orgTree()
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => props.modelValue, () => { if (!tree.value.length) load() })

function onChange(v: number | null) {
  emit('update:modelValue', v)
}
</script>

<template>
  <el-tree-select
    :model-value="modelValue ?? undefined"
    :data="tree"
    :props="{ label: 'orgName', children: 'children', value: 'id' } as Record<string, string>"
    :loading="loading"
    check-strictly
    clearable
    filterable
    placeholder="选择机构"
    class="w-56"
    @update:model-value="onChange"
  />
</template>

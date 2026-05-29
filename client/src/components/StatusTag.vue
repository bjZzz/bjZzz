<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status?: string }>()

const type = computed(() => {
  const s = (props.status ?? '').toUpperCase()
  if (['ACTIVE', 'SUCCESS', 'COMPLETED', 'PUBLISHED', 'APPROVED'].includes(s)) return 'success'
  if (['PENDING', 'RUNNING', 'DRAFT', 'OPEN'].includes(s)) return 'warning'
  if (['FAILED', 'REJECTED', 'DISABLED', 'INACTIVE'].includes(s)) return 'danger'
  return 'info'
})
</script>

<template>
  <el-tag :type="type" size="small">{{ status || '-' }}</el-tag>
</template>

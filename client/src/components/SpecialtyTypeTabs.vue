<script setup lang="ts">
import { computed } from 'vue'
import type { SpecialtyType } from '@/types/modules/asset'

const props = defineProps<{
  modelValue: SpecialtyType
}>()

const emit = defineEmits<{ 'update:modelValue': [SpecialtyType] }>()

const tabs = [
  { label: '代谢专病', value: 'metabolic' as SpecialtyType },
  { label: '心脑血管', value: 'cardio_cerebrovascular' as SpecialtyType },
  { label: '呼吸专病', value: 'respiratory' as SpecialtyType },
]

const active = computed({
  get: () => props.modelValue,
  set: (v: SpecialtyType) => emit('update:modelValue', v),
})
</script>

<template>
  <el-tabs v-model="active" class="mb-4">
    <el-tab-pane v-for="tab in tabs" :key="tab.value" :label="tab.label" :name="tab.value" />
  </el-tabs>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  permission: string | string[]
}>()

const auth = useAuthStore()
const visible = computed(() =>
  Array.isArray(props.permission)
    ? auth.hasAnyPermission(props.permission)
    : auth.hasPermission(props.permission),
)
</script>

<template>
  <slot v-if="visible" />
</template>

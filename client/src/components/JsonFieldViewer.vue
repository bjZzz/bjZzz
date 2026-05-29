<script setup lang="ts">
defineProps<{ data?: string | Record<string, unknown> | null }>()
</script>

<template>
  <pre class="overflow-auto rounded-lg bg-slate-900 p-4 text-xs text-slate-100">{{
    typeof data === 'string' ? data : JSON.stringify(data ?? {}, null, 2)
  }}</pre>
</template>

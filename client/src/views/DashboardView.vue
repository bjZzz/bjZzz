<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="rounded-xl bg-white p-6 shadow-sm">
    <h2 class="text-xl font-semibold text-slate-800">工作台</h2>
    <p class="mt-2 text-slate-500">欢迎，{{ auth.displayName || auth.username }}</p>
    <div class="mt-6 grid gap-4 md:grid-cols-3">
      <div class="rounded-lg border border-slate-200 p-4">
        <div class="text-sm text-slate-500">当前机构</div>
        <div class="mt-1 text-lg font-medium">{{ auth.orgId ?? '-' }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 p-4">
        <div class="text-sm text-slate-500">权限数量</div>
        <div class="mt-1 text-lg font-medium">{{ auth.permissions.length }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 p-4">
        <div class="text-sm text-slate-500">Wave 1 MVP</div>
        <div class="mt-1 text-lg font-medium">平台 · 采集 · 治理 · 资产 · 科研</div>
      </div>
    </div>
  </div>
</template>

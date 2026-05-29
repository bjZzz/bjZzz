<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const title = route.meta.title as string
</script>

<template>
  <div class="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center">
    <el-tag type="info" class="mb-4">Wave 2 规划中</el-tag>
    <h2 class="text-xl font-semibold text-slate-800">{{ title }}</h2>
    <p class="mt-2 text-slate-500">该模块将在 Wave 2 阶段实现，当前为路由占位页。</p>
  </div>
</template>

<script setup lang="ts"></script>

<template>
  <div class="flex flex-col items-center justify-center py-20">
    <h1 class="text-6xl font-bold text-slate-300">404</h1>
    <p class="mt-4 text-slate-600">页面不存在</p>
    <el-button type="primary" class="mt-6" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

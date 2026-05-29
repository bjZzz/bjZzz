<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { platformApi } from '@/api/platform'
import type { PermissionTreeNode, RoleVO } from '@/types/modules/platform'

const roles = ref<RoleVO[]>([])
const permissions = ref<PermissionTreeNode[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    ;[roles.value, permissions.value] = await Promise.all([platformApi.roles(), platformApi.permissionsTree()])
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="角色权限" description="查看角色与权限树（只读）" />
    <div class="grid gap-4 md:grid-cols-2">
      <div>
        <h3 class="mb-2 font-medium">角色列表</h3>
        <el-table v-loading="loading" :data="roles" stripe size="small">
          <el-table-column prop="roleCode" label="编码" />
          <el-table-column prop="roleName" label="名称" />
          <el-table-column prop="description" label="描述" />
        </el-table>
      </div>
      <div>
        <h3 class="mb-2 font-medium">权限树</h3>
        <el-tree :data="permissions" node-key="id" default-expand-all :props="{ label: 'permName', children: 'children' }">
          <template #default="{ data }">
            <span>{{ data.permName }} <code class="ml-2 text-xs text-slate-400">{{ data.permCode }}</code></span>
          </template>
        </el-tree>
      </div>
    </div>
  </div>
</template>

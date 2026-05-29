<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePagination } from '@/composables/usePagination'
import { platformApi } from '@/api/platform'
import type { UserCreateRequest, UserVO } from '@/types/modules/platform'

const { page, size, total, loading, items, applyPageResult } = usePagination()
const query = reactive({ username: '', status: '' })
const dialogVisible = ref(false)
const roleDialog = ref(false)
const currentUser = ref<UserVO | null>(null)
const roles = ref<{ id: number; roleName: string }[]>([])
const selectedRoles = ref<number[]>([])
const form = reactive<UserCreateRequest>({ username: '', password: '', displayName: '' })

async function load() {
  loading.value = true
  try {
    applyPageResult(await platformApi.users({ page: page.value, size: size.value, ...query }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await platformApi.userCreate(form)
  ElMessage.success('用户已创建')
  dialogVisible.value = false
  load()
}

async function openRoles(row: UserVO) {
  currentUser.value = row
  roles.value = await platformApi.roles()
  selectedRoles.value = row.roleIds ?? []
  roleDialog.value = true
}

async function saveRoles() {
  if (!currentUser.value) return
  await platformApi.userAssignRoles(currentUser.value.id, selectedRoles.value)
  ElMessage.success('角色已更新')
  roleDialog.value = false
  load()
}

async function toggleStatus(row: UserVO) {
  const next = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  await platformApi.userUpdateStatus(row.id, next)
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="用户管理">
      <template #actions>
        <PermButton permission="platform:user:write">
          <el-button type="primary" @click="dialogVisible = true">新建用户</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <div class="mb-4 flex gap-2">
      <el-input v-model="query.username" placeholder="用户名" clearable class="w-48" @keyup.enter="load" />
      <el-button @click="load">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="items as UserVO[]" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="displayName" label="姓名" />
      <el-table-column label="状态"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <PermButton permission="platform:user:write">
            <el-button link type="primary" @click="openRoles(row)">分配角色</el-button>
            <el-button link @click="toggleStatus(row)">{{ row.status === 'ACTIVE' ? '禁用' : '启用' }}</el-button>
          </PermButton>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="load" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建用户" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.displayName" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="roleDialog" title="分配角色" width="480px">
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox v-for="r in roles" :key="r.id" :value="r.id">{{ r.roleName }}</el-checkbox>
      </el-checkbox-group>
      <template #footer><el-button @click="roleDialog = false">取消</el-button><el-button type="primary" @click="saveRoles">保存</el-button></template>
    </el-dialog>
  </div>
</template>

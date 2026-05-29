<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PermButton from '@/components/PermButton.vue'
import { platformApi } from '@/api/platform'
import type { OrgCreateRequest, OrgTreeNode } from '@/types/modules/platform'

const tree = ref<OrgTreeNode[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<OrgCreateRequest>({ orgCode: '', orgName: '', orgType: 'HOSPITAL', parentId: undefined })

async function load() {
  loading.value = true
  try {
    tree.value = await platformApi.orgTree()
  } finally {
    loading.value = false
  }
}

function openCreate(parentId?: number) {
  editingId.value = null
  Object.assign(form, { orgCode: '', orgName: '', orgType: 'HOSPITAL', parentId })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await platformApi.orgUpdate(editingId.value, { orgName: form.orgName, orgType: form.orgType })
    ElMessage.success('更新成功')
  } else {
    await platformApi.orgCreate(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  load()
}

async function remove(node: OrgTreeNode) {
  await ElMessageBox.confirm(`确定删除机构「${node.orgName}」？`, '确认')
  await platformApi.orgDelete(node.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div class="rounded-xl bg-white p-4 shadow-sm">
    <PageHeader title="机构管理" description="维护多级机构树">
      <template #actions>
        <PermButton permission="platform:org:write">
          <el-button type="primary" @click="openCreate()">新建机构</el-button>
        </PermButton>
      </template>
    </PageHeader>
    <el-tree v-loading="loading" :data="tree" node-key="id" default-expand-all :props="{ label: 'orgName', children: 'children' }">
      <template #default="{ data }">
        <div class="flex w-full items-center justify-between pr-4">
          <span>{{ data.orgName }} <el-tag size="small" class="ml-2">{{ data.orgCode }}</el-tag></span>
          <span class="flex gap-2">
            <PermButton permission="platform:org:write">
              <el-button link type="primary" @click.stop="openCreate(data.id)">添加子机构</el-button>
              <el-button link type="danger" @click.stop="remove(data)">删除</el-button>
            </PermButton>
          </span>
        </div>
      </template>
    </el-tree>
    <el-dialog v-model="dialogVisible" title="机构" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编码"><el-input v-model="form.orgCode" :disabled="!!editingId" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.orgName" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.orgType" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

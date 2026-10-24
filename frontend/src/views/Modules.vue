<template>
  <div class="page-card">
    <div class="page-header">
      <h3 class="page-title">模块管理</h3>
      <div class="toolbar" style="margin: 0">
        <el-select v-model="projectId" placeholder="选择项目" style="width: 220px" @change="loadTree">
          <el-option v-for="item in projects" :key="item.id" :label="`${item.name}（${item.code}）`" :value="item.id" />
        </el-select>
        <el-button type="primary" :icon="Plus" :disabled="!projectId" @click="openCreate(null)">新建根模块</el-button>
      </div>
    </div>

    <el-empty v-if="!projectId" description="请先选择项目" />
    <el-tree
      v-else
      v-loading="loading"
      :data="tree"
      node-key="id"
      default-expand-all
      :expand-on-click-node="false"
      empty-text="暂无模块，点击右上角新建"
      class="module-tree"
    >
      <template #default="{ data }">
        <div class="tree-node">
          <span class="node-label">
            <el-icon><Folder /></el-icon>
            {{ data.name }}
            <span v-if="data.sortOrder" class="muted">（排序 {{ data.sortOrder }}）</span>
          </span>
          <span class="node-actions">
            <el-button link type="primary" size="small" @click.stop="openCreate(data)">添加子模块</el-button>
            <el-button link type="primary" size="small" @click.stop="openEdit(data)">重命名</el-button>
            <el-button link type="danger" size="small" @click.stop="remove(data)">删除</el-button>
          </span>
        </div>
      </template>
    </el-tree>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="模块名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item v-if="parentName" label="上级模块">
          <span>{{ parentName }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Plus } from '@element-plus/icons-vue'
import { moduleApi, projectApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const projects = ref([])
const projectId = ref(null)
const tree = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const parentNode = ref(null)
const editingParentId = ref(null)
const formRef = ref()
const form = reactive({ name: '', sortOrder: 0 })

const rules = {
  name: [{ required: true, message: '请输入模块名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => {
  if (editingId.value) {
    return '重命名模块'
  }
  return parentNode.value ? '新建子模块' : '新建根模块'
})

const parentName = computed(() => parentNode.value?.name || '')

async function loadProjects() {
  projects.value = await projectApi.all()
  const cached = Number(localStorage.getItem('tcm_project_id'))
  const matched = projects.value.find((item) => item.id === cached)
  projectId.value = matched ? matched.id : projects.value[0]?.id || null
  if (projectId.value) {
    localStorage.setItem('tcm_project_id', String(projectId.value))
    await loadTree()
  }
}

async function loadTree() {
  if (!projectId.value) {
    tree.value = []
    return
  }
  localStorage.setItem('tcm_project_id', String(projectId.value))
  loading.value = true
  try {
    tree.value = await moduleApi.tree(projectId.value)
  } finally {
    loading.value = false
  }
}

function openCreate(node) {
  editingId.value = null
  parentNode.value = node
  editingParentId.value = null
  form.name = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

function openEdit(node) {
  editingId.value = node.id
  parentNode.value = null
  editingParentId.value = node.parentId ?? null
  form.name = node.name
  form.sortOrder = node.sortOrder || 0
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    saving.value = true
    try {
      if (editingId.value) {
        await moduleApi.update(editingId.value, {
          name: form.name,
          sortOrder: form.sortOrder,
          projectId: projectId.value,
          parentId: editingParentId.value
        })
        ElMessage.success('模块已更新')
      } else {
        await moduleApi.create({
          projectId: projectId.value,
          parentId: parentNode.value ? parentNode.value.id : null,
          name: form.name,
          sortOrder: form.sortOrder
        })
        ElMessage.success('模块已创建')
      }
      dialogVisible.value = false
      loadTree()
    } finally {
      saving.value = false
    }
  })
}

async function remove(node) {
  await ElMessageBox.confirm(`确认删除模块「${node.name}」？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await moduleApi.remove(node.id)
  ElMessage.success('模块已删除')
  loadTree()
}

onMounted(loadProjects)
</script>

<style scoped>
.module-tree {
  background: transparent;
  padding: 4px 0;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.node-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.node-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}
</style>

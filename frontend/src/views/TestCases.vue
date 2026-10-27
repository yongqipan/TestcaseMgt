<template>
  <div class="page-card">
    <div class="page-header">
      <h3 class="page-title">测试用例</h3>
      <el-button type="primary" :icon="Plus" :disabled="!projectId" @click="openCreate">新建用例</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="projectId" placeholder="选择项目" style="width: 200px" @change="onProjectChange">
        <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-tree-select
        v-model="filters.moduleId"
        :data="moduleTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        check-strictly
        clearable
        placeholder="全部模块"
        style="width: 200px"
        @change="reload"
      />
      <el-input
        v-model="filters.keyword"
        placeholder="搜索标题或编号"
        clearable
        style="width: 220px"
        :prefix-icon="Search"
        @keyup.enter="reload"
        @clear="reload"
      />
      <el-select v-model="filters.priority" placeholder="优先级" clearable style="width: 120px" @change="reload">
        <el-option v-for="item in priorities" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 130px" @change="reload">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="就绪" value="ACTIVE" />
        <el-option label="已废弃" value="DEPRECATED" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe class="clickable-table" @row-click="openDetail">
      <el-table-column prop="caseNo" label="编号" width="140" />
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="模块" width="130">
        <template #default="{ row }">{{ moduleName(row.moduleId) }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="priorityTag(row.priority)" effect="light">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ typeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" effect="plain">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <CaseFormDialog
      v-model="dialogVisible"
      :project-id="projectId"
      :module-tree="moduleTree"
      :case-data="editingCase"
      @saved="load"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import CaseFormDialog from '../components/CaseFormDialog.vue'
import { caseApi, moduleApi, projectApi } from '../api'

const router = useRouter()

const priorities = ['P0', 'P1', 'P2', 'P3']

const loading = ref(false)
const projects = ref([])
const projectId = ref(null)
const moduleTree = ref([])
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const filters = reactive({ keyword: '', moduleId: null, priority: '', status: '' })

const dialogVisible = ref(false)
const editingCase = ref(null)

const moduleMap = computed(() => {
  const map = new Map()
  const walk = (nodes) => {
    for (const node of nodes) {
      map.set(node.id, node.name)
      if (node.children?.length) {
        walk(node.children)
      }
    }
  }
  walk(moduleTree.value)
  return map
})

function moduleName(id) {
  if (!id) {
    return '-'
  }
  return moduleMap.value.get(id) || '-'
}

function priorityTag(priority) {
  return { P0: 'danger', P1: 'warning', P2: 'primary', P3: 'info' }[priority] || 'info'
}

function typeLabel(type) {
  return { FUNCTIONAL: '功能测试', API: '接口测试', UI: '界面测试', PERFORMANCE: '性能测试' }[type] || type
}

function statusLabel(status) {
  return { DRAFT: '草稿', ACTIVE: '就绪', DEPRECATED: '已废弃' }[status] || status
}

function statusTag(status) {
  return { DRAFT: 'info', ACTIVE: 'success', DEPRECATED: 'danger' }[status] || 'info'
}

async function loadProjects() {
  projects.value = await projectApi.all()
  const cached = Number(localStorage.getItem('tcm_project_id'))
  const matched = projects.value.find((item) => item.id === cached)
  projectId.value = matched ? matched.id : projects.value[0]?.id || null
  if (projectId.value) {
    localStorage.setItem('tcm_project_id', String(projectId.value))
    await loadModules()
    await load()
  }
}

async function loadModules() {
  if (!projectId.value) {
    moduleTree.value = []
    return
  }
  moduleTree.value = await moduleApi.tree(projectId.value)
}

async function load() {
  if (!projectId.value) {
    rows.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const data = await caseApi.page({
      projectId: projectId.value,
      moduleId: filters.moduleId || undefined,
      keyword: filters.keyword,
      priority: filters.priority,
      status: filters.status,
      page: page.value,
      size: size.value
    })
    rows.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function onProjectChange() {
  localStorage.setItem('tcm_project_id', String(projectId.value))
  filters.moduleId = null
  page.value = 1
  await loadModules()
  await load()
}

function reload() {
  page.value = 1
  load()
}

function openCreate() {
  editingCase.value = null
  dialogVisible.value = true
}

function openDetail(row) {
  router.push(`/cases/${row.id}`)
}

onMounted(loadProjects)
</script>

<style scoped>
.clickable-table :deep(.el-table__row) {
  cursor: pointer;
}
</style>

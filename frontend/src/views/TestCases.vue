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

    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="expand">
            <p v-if="row.precondition"><strong>前置条件：</strong>{{ row.precondition }}</p>
            <div v-if="row.steps && row.steps.length">
              <strong>测试步骤：</strong>
              <ol>
                <li v-for="(step, index) in row.steps" :key="index">
                  {{ step.action }}
                  <span class="muted">→ 预期：{{ step.expected || '-' }}</span>
                </li>
              </ol>
            </div>
            <p v-if="!row.precondition && (!row.steps || !row.steps.length)" class="muted">暂无用例详情</p>
          </div>
        </template>
      </el-table-column>
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
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用例' : '新建用例'" width="820px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="16">
            <el-form-item label="用例标题" prop="title">
              <el-input v-model="form.title" placeholder="简要描述验证目标" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所属模块">
              <el-tree-select
                v-model="form.moduleId"
                :data="moduleTree"
                :props="{ label: 'name', children: 'children' }"
                node-key="id"
                check-strictly
                clearable
                placeholder="可选"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="优先级">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option v-for="item in priorities" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用例类型">
              <el-select v-model="form.type" style="width: 100%">
                <el-option label="功能测试" value="FUNCTIONAL" />
                <el-option label="接口测试" value="API" />
                <el-option label="界面测试" value="UI" />
                <el-option label="性能测试" value="PERFORMANCE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="就绪" value="ACTIVE" />
                <el-option label="已废弃" value="DEPRECATED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="前置条件">
          <el-input v-model="form.precondition" type="textarea" :rows="2" placeholder="执行该用例前需满足的条件" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，例如：登录,冒烟" />
        </el-form-item>

        <el-form-item label="测试步骤">
          <div class="steps-editor" style="width: 100%">
            <div v-for="(step, index) in form.steps" :key="index" class="step-row">
              <div class="step-index">{{ index + 1 }}</div>
              <el-input v-model="step.action" type="textarea" :rows="2" placeholder="操作步骤" />
              <el-input v-model="step.expected" type="textarea" :rows="2" placeholder="预期结果" />
              <el-button text type="danger" :icon="Delete" @click="removeStep(index)" />
            </div>
            <el-button text type="primary" :icon="Plus" @click="addStep">添加步骤</el-button>
          </div>
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
import { Delete, Plus, Search } from '@element-plus/icons-vue'
import { caseApi, moduleApi, projectApi } from '../api'

const priorities = ['P0', 'P1', 'P2', 'P3']

const loading = ref(false)
const saving = ref(false)
const projects = ref([])
const projectId = ref(null)
const moduleTree = ref([])
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const filters = reactive({ keyword: '', moduleId: null, priority: '', status: '' })

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const form = reactive({
  title: '',
  moduleId: null,
  priority: 'P1',
  type: 'FUNCTIONAL',
  status: 'ACTIVE',
  precondition: '',
  tags: '',
  steps: []
})

const rules = {
  title: [{ required: true, message: '请输入用例标题', trigger: 'blur' }]
}

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

function resetForm() {
  form.title = ''
  form.moduleId = null
  form.priority = 'P1'
  form.type = 'FUNCTIONAL'
  form.status = 'ACTIVE'
  form.precondition = ''
  form.tags = ''
  form.steps = []
}

function openCreate() {
  editingId.value = null
  resetForm()
  addStep()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.title = row.title
  form.moduleId = row.moduleId ?? null
  form.priority = row.priority || 'P1'
  form.type = row.type || 'FUNCTIONAL'
  form.status = row.status || 'ACTIVE'
  form.precondition = row.precondition || ''
  form.tags = row.tags || ''
  form.steps = (row.steps || []).map((step) => ({ action: step.action, expected: step.expected }))
  if (form.steps.length === 0) {
    addStep()
  }
  dialogVisible.value = true
}

function addStep() {
  form.steps.push({ action: '', expected: '' })
}

function removeStep(index) {
  form.steps.splice(index, 1)
}

async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    if (!projectId.value) {
      ElMessage.warning('请先选择项目')
      return
    }
    saving.value = true
    try {
      const payload = {
        projectId: projectId.value,
        moduleId: form.moduleId,
        title: form.title,
        priority: form.priority,
        type: form.type,
        status: form.status,
        precondition: form.precondition,
        tags: form.tags,
        steps: form.steps.filter((step) => step.action && step.action.trim())
      }
      if (editingId.value) {
        await caseApi.update(editingId.value, payload)
        ElMessage.success('用例已更新')
      } else {
        await caseApi.create(payload)
        ElMessage.success('用例已创建')
      }
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用例「${row.title}」？相关执行记录将一并删除。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await caseApi.remove(row.id)
  ElMessage.success('用例已删除')
  if (rows.value.length === 1 && page.value > 1) {
    page.value -= 1
  }
  load()
}

onMounted(loadProjects)
</script>

<style scoped>
.expand {
  padding: 4px 24px 12px;
  font-size: 13px;
  color: #4a5568;
}

.expand ol {
  margin: 8px 0 0;
  padding-left: 20px;
  line-height: 1.9;
}
</style>

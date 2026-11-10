<template>
  <div class="page-card">
    <div class="page-header">
      <div class="title-group">
        <h3 class="page-title">AI 生成用例</h3>
        <el-tag type="primary" effect="light" size="small">AI</el-tag>
      </div>
      <el-button type="primary" :icon="MagicStick" :disabled="!projectId" @click="openCreate">新建生成任务</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="projectId" placeholder="选择项目" style="width: 200px" @change="onProjectChange">
        <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px" @change="reload">
        <el-option label="排队中" value="PENDING" />
        <el-option label="生成中" value="RUNNING" />
        <el-option label="已完成" value="SUCCEEDED" />
        <el-option label="失败" value="FAILED" />
      </el-select>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe class="clickable-table" @row-click="openDetail">
      <el-table-column prop="id" label="#" width="70" />
      <el-table-column prop="requirement" label="需求摘要" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" effect="light">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生成 / 采纳" width="120">
        <template #default="{ row }">{{ row.totalCount ?? 0 }} / {{ row.acceptedCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="createdBy" label="创建人" width="110">
        <template #default="{ row }">{{ row.createdBy || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
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

    <el-dialog v-model="createVisible" title="新建 AI 生成任务" width="640px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="所属项目">
          <el-input :model-value="projectName" disabled />
        </el-form-item>
        <el-form-item label="所属模块">
          <el-tree-select
            v-model="createForm.moduleId"
            :data="moduleTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            check-strictly
            clearable
            placeholder="可选，归属模块"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="需求描述" prop="requirement">
          <el-input
            v-model="createForm.requirement"
            type="textarea"
            :rows="6"
            placeholder="粘贴需求、用户故事或接口说明，AI 将据此生成功能测试用例"
          />
        </el-form-item>
        <el-form-item label="参考文件">
          <el-input v-model="createForm.fileRefs" placeholder="可选，填写文档名或链接，便于追溯" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">开始生成</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" :title="detailTitle" size="640px" @closed="stopPolling">
      <div v-loading="detailLoading">
        <template v-if="detail.generation">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="状态">
              <el-tag :type="statusTag(detail.generation.status)" effect="light">
                {{ statusLabel(detail.generation.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="生成/采纳">
              {{ detail.generation.totalCount ?? 0 }} / {{ detail.generation.acceptedCount ?? 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="创建人">{{ detail.generation.createdBy || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.generation.createdAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="需求描述" :span="2">
              <div class="pre-wrap">{{ detail.generation.requirement }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.generation.errorMsg" label="错误信息" :span="2">
              <span class="error-text">{{ detail.generation.errorMsg }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="section-header">
            <h4>生成结果</h4>
            <div v-if="detail.generation.status === 'SUCCEEDED'" class="section-actions">
              <el-button size="small" @click="toggleAll(true)">全选未采纳</el-button>
              <el-button
                type="primary"
                size="small"
                :loading="accepting"
                :disabled="!selectedIds.length"
                @click="acceptSelected"
              >
                采纳为用例（{{ selectedIds.length }}）
              </el-button>
            </div>
          </div>

          <el-empty v-if="!detail.items.length" description="暂无生成结果" />
          <div v-else class="item-list">
            <div v-for="item in detail.items" :key="item.id" class="gen-item">
              <el-checkbox
                v-model="item.__checked"
                :disabled="Boolean(item.acceptedCaseId)"
                @change="syncSelected"
              />
              <div class="gen-body">
                <div class="gen-title">
                  <span>{{ item.title }}</span>
                  <el-tag :type="priorityTag(item.priority)" size="small" effect="light">{{ item.priority }}</el-tag>
                  <el-tag size="small" effect="plain">{{ typeLabel(item.type) }}</el-tag>
                  <el-tag v-if="item.confidence != null" size="small" type="success" effect="plain">
                    置信度 {{ item.confidence }}
                  </el-tag>
                  <el-tag v-if="item.acceptedCaseId" size="small" type="info" effect="light">已采纳 #{{ item.acceptedCaseId }}</el-tag>
                </div>
                <div v-if="item.precondition" class="gen-pre">前置：{{ item.precondition }}</div>
                <ol class="gen-steps">
                  <li v-for="(step, index) in item.steps || []" :key="index">
                    {{ step.action }}
                    <span class="muted">→ {{ step.expected || '-' }}</span>
                  </li>
                </ol>
              </div>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Refresh } from '@element-plus/icons-vue'
import { aiGenerationApi, moduleApi, projectApi } from '../api'

const loading = ref(false)
const projects = ref([])
const projectId = ref(null)
const moduleTree = ref([])
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = reactive({ status: '' })

const createVisible = ref(false)
const creating = ref(false)
const createFormRef = ref()
const createForm = reactive({ moduleId: null, requirement: '', fileRefs: '' })
const createRules = {
  requirement: [{ required: true, message: '请输入需求描述', trigger: 'blur' }]
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({ generation: null, items: [] })
const selectedIds = ref([])
const accepting = ref(false)
let pollTimer = null

const projectName = computed(() => {
  const matched = projects.value.find((item) => item.id === projectId.value)
  return matched ? matched.name : '-'
})

const detailTitle = computed(() => {
  const g = detail.value.generation
  return g ? `生成任务 #${g.id}` : '生成详情'
})

function statusLabel(status) {
  return { PENDING: '排队中', RUNNING: '生成中', SUCCEEDED: '已完成', FAILED: '失败' }[status] || status || '-'
}

function statusTag(status) {
  return { PENDING: 'info', RUNNING: 'warning', SUCCEEDED: 'success', FAILED: 'danger' }[status] || 'info'
}

function priorityTag(priority) {
  return { P0: 'danger', P1: 'warning', P2: 'primary', P3: 'info' }[priority] || 'info'
}

function typeLabel(type) {
  return { FUNCTIONAL: '功能测试', API: '接口测试', UI: '界面测试', PERFORMANCE: '性能测试', SECURITY: '安全测试' }[type] || type || '-'
}

async function loadProjects() {
  projects.value = await projectApi.all()
  const cached = Number(localStorage.getItem('tcm_project_id'))
  const matched = projects.value.find((item) => item.id === cached)
  projectId.value = matched ? matched.id : projects.value[0]?.id || null
  if (projectId.value) {
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
    const data = await aiGenerationApi.page({
      projectId: projectId.value,
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
  page.value = 1
  await loadModules()
  await load()
}

function reload() {
  page.value = 1
  load()
}

function openCreate() {
  createForm.moduleId = null
  createForm.requirement = ''
  createForm.fileRefs = ''
  createVisible.value = true
}

async function submitCreate() {
  await createFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    creating.value = true
    try {
      const generation = await aiGenerationApi.create({
        projectId: projectId.value,
        moduleId: createForm.moduleId || null,
        requirement: createForm.requirement,
        fileRefs: createForm.fileRefs
      })
      ElMessage.success('已提交生成任务')
      createVisible.value = false
      page.value = 1
      await load()
      openDetail(generation)
    } finally {
      creating.value = false
    }
  })
}

async function openDetail(row) {
  detailVisible.value = true
  detail.value = { generation: row, items: [] }
  await fetchDetail(row.id)
  startPollingIfNeeded(row.id)
}

async function fetchDetail(id) {
  detailLoading.value = true
  try {
    const data = await aiGenerationApi.detail(id)
    const items = (data.items || []).map((item) => ({ ...item, __checked: false }))
    detail.value = { generation: data.generation, items }
    syncSelected()
  } finally {
    detailLoading.value = false
  }
}

function startPollingIfNeeded(id) {
  stopPolling()
  const status = detail.value.generation?.status
  if (status === 'PENDING' || status === 'RUNNING') {
    pollTimer = setInterval(async () => {
      await fetchDetail(id)
      const next = detail.value.generation?.status
      if (next === 'SUCCEEDED' || next === 'FAILED') {
        stopPolling()
        load()
      }
    }, 2500)
  }
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function toggleAll(checked) {
  detail.value.items.forEach((item) => {
    if (!item.acceptedCaseId) {
      item.__checked = checked
    }
  })
  syncSelected()
}

function syncSelected() {
  selectedIds.value = detail.value.items
    .filter((item) => item.__checked && !item.acceptedCaseId)
    .map((item) => item.id)
}

async function acceptSelected() {
  if (!selectedIds.value.length) {
    return
  }
  accepting.value = true
  try {
    const created = await aiGenerationApi.accept(detail.value.generation.id, selectedIds.value)
    ElMessage.success(`已采纳 ${created.length} 条用例`)
    await fetchDetail(detail.value.generation.id)
    await load()
  } finally {
    accepting.value = false
  }
}

onMounted(loadProjects)
onBeforeUnmount(stopPolling)
</script>

<style scoped>
.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clickable-table :deep(.el-table__row) {
  cursor: pointer;
}

.pre-wrap {
  white-space: pre-wrap;
  line-height: 1.7;
}

.error-text {
  color: #e34d59;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 20px 0 12px;
}

.section-header h4 {
  margin: 0;
  font-size: 15px;
}

.section-actions {
  display: flex;
  gap: 8px;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.gen-item {
  display: flex;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid #eef1f6;
  border-radius: 8px;
  background: #fafbfd;
}

.gen-body {
  flex: 1;
  min-width: 0;
}

.gen-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-weight: 600;
}

.gen-pre {
  margin-top: 6px;
  color: #8a94a6;
  font-size: 12px;
}

.gen-steps {
  margin: 6px 0 0;
  padding-left: 18px;
  line-height: 1.8;
  font-size: 13px;
  color: #4a5568;
}
</style>

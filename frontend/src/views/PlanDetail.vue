<template>
  <div v-loading="loading">
    <div class="page-card">
      <div class="page-header">
        <div class="title-group">
          <el-button text :icon="ArrowLeft" @click="$router.push('/plans')">返回</el-button>
          <h3 class="page-title">{{ detail.plan?.name || '计划详情' }}</h3>
          <el-tag v-if="detail.plan" :type="statusTag(detail.plan.status)" effect="light">
            {{ statusLabel(detail.plan.status) }}
          </el-tag>
        </div>
        <div class="toolbar" style="margin: 0">
          <el-button :icon="Edit" @click="openEdit">编辑</el-button>
          <el-tooltip :disabled="canDelete" content="计划已开始，不能删除" placement="top">
            <span>
              <el-button type="danger" :icon="Delete" :disabled="!canDelete" @click="removePlan">删除计划</el-button>
            </span>
          </el-tooltip>
          <el-button type="primary" :icon="Plus" @click="openAddCases">添加用例</el-button>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>
      <p v-if="detail.plan?.description" class="description">{{ detail.plan.description }}</p>
      <p class="muted">
        所属项目：{{ detail.plan?.projectId || '-' }} ｜ 起止日期：{{ detail.plan?.startDate || '-' }} ~
        {{ detail.plan?.endDate || '-' }} ｜ 创建人：{{ detail.plan?.createdBy || '-' }}
      </p>

      <div class="stat-grid detail-stats">
        <div class="mini-stat">
          <div class="label">用例总数</div>
          <div class="value">{{ detail.stats?.total ?? 0 }}</div>
        </div>
        <div class="mini-stat">
          <div class="label">已执行</div>
          <div class="value">{{ detail.stats?.executed ?? 0 }}</div>
        </div>
        <div class="mini-stat">
          <div class="label">通过</div>
          <div class="value pass">{{ detail.stats?.pass ?? 0 }}</div>
        </div>
        <div class="mini-stat">
          <div class="label">失败</div>
          <div class="value fail">{{ detail.stats?.fail ?? 0 }}</div>
        </div>
        <div class="mini-stat">
          <div class="label">未执行</div>
          <div class="value">{{ detail.stats?.untested ?? 0 }}</div>
        </div>
        <div class="mini-stat">
          <div class="label">通过率</div>
          <div class="value">{{ detail.stats?.passRate ?? 0 }}%</div>
        </div>
      </div>

      <el-progress
        class="progress"
        :percentage="executionPercent"
        :stroke-width="12"
        status="success"
        :format="() => `执行进度 ${detail.stats?.executed ?? 0}/${detail.stats?.total ?? 0}`"
      />
    </div>

    <div class="page-card section">
      <div class="page-header">
        <h3 class="page-title">用例执行清单</h3>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px">
          <el-option label="未执行" value="UNTESTED" />
          <el-option label="通过" value="PASS" />
          <el-option label="失败" value="FAIL" />
          <el-option label="阻塞" value="BLOCKED" />
          <el-option label="跳过" value="SKIPPED" />
        </el-select>
      </div>

      <el-table
        :data="filteredCases"
        stripe
        :empty-text="detail.stats?.total ? '暂无数据' : '尚未选择用例，点击右上角「添加用例」'"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand">
              <p v-if="row.precondition"><strong>前置条件：</strong>{{ row.precondition }}</p>
              <ol v-if="row.steps && row.steps.length">
                <li v-for="(step, index) in row.steps" :key="index">
                  {{ step.action }}
                  <span class="muted">→ 预期：{{ step.expected || '-' }}</span>
                </li>
              </ol>
              <p v-else class="muted">暂无用例步骤</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="caseNo" label="编号" width="140" />
        <el-table-column prop="title" label="用例标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTag(row.priority)" effect="light">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行结果" width="110">
          <template #default="{ row }">
            <el-tag :type="runTag(row.runStatus)" effect="light">{{ runLabel(row.runStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executor" label="执行人" width="100">
          <template #default="{ row }">{{ row.executor || '-' }}</template>
        </el-table-column>
        <el-table-column prop="executedAt" label="执行时间" width="180">
          <template #default="{ row }">{{ row.executedAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRun(row)">记录结果</el-button>
            <el-tooltip :disabled="!isCaseExecuted(row)" content="该用例已执行，不能移除" placement="top">
              <span>
                <el-button link type="danger" :disabled="isCaseExecuted(row)" @click="removeCase(row)">移除</el-button>
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="addDialogVisible" title="添加测试用例" width="920px" top="5vh">
      <div class="toolbar add-toolbar">
        <el-input
          v-model="addFilters.keyword"
          placeholder="搜索标题或编号"
          clearable
          style="width: 220px"
          :prefix-icon="Search"
          @keyup.enter="searchAvailable"
          @clear="searchAvailable"
        />
        <el-tree-select
          v-model="addFilters.moduleId"
          :data="moduleTree"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          check-strictly
          clearable
          placeholder="全部模块"
          style="width: 180px"
          @change="searchAvailable"
        />
        <el-select v-model="addFilters.priority" placeholder="优先级" clearable style="width: 120px" @change="searchAvailable">
          <el-option v-for="item in priorities" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button type="primary" @click="searchAvailable">查询</el-button>
      </div>

      <el-table
        :key="availableTableKey"
        ref="availableTableRef"
        v-loading="availableLoading"
        :data="availableRows"
        row-key="id"
        stripe
        height="380"
        empty-text="当前项目暂无可添加的用例"
        @selection-change="onAvailableSelectionChange"
      >
        <el-table-column type="selection" width="48" reserve-selection />
        <el-table-column prop="caseNo" label="编号" width="140" />
        <el-table-column prop="title" label="用例标题" min-width="260" show-overflow-tooltip />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTag(row.priority)" effect="light">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ caseStatusLabel(row.status) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="availablePage"
          v-model:page-size="availableSize"
          :total="availableTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadAvailable"
          @size-change="onAvailableSizeChange"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <span class="muted">已选 {{ availableSelection.length }} 条</span>
          <div>
            <el-button @click="addDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="adding" :disabled="!availableSelection.length" @click="submitAddCases">
              添加
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑计划" width="560px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option label="未开始" value="PLANNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="起止日期">
          <el-date-picker
            v-model="editForm.range"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="本轮测试的范围与目标" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="runDialogVisible" title="记录执行结果" width="520px">
      <p class="run-title">{{ currentCase?.caseNo }} · {{ currentCase?.title }}</p>
      <el-form label-width="80px">
        <el-form-item label="执行结果">
          <el-radio-group v-model="runForm.status">
            <el-radio-button label="PASS">通过</el-radio-button>
            <el-radio-button label="FAIL">失败</el-radio-button>
            <el-radio-button label="BLOCKED">阻塞</el-radio-button>
            <el-radio-button label="SKIPPED">跳过</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="runForm.remark" type="textarea" :rows="3" placeholder="失败原因、阻塞原因或补充说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="runDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRun">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { moduleApi, planApi } from '../api'

const priorities = ['P0', 'P1', 'P2', 'P3']

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const detail = ref({})
const filterStatus = ref('')
const runDialogVisible = ref(false)
const currentCase = ref(null)
const runForm = reactive({ status: 'PASS', remark: '' })

const editDialogVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref()
const editForm = reactive({ name: '', status: 'PLANNED', range: [], description: '' })
const editRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }]
}

const moduleTree = ref([])
const addDialogVisible = ref(false)
const availableLoading = ref(false)
const adding = ref(false)
const availableTableRef = ref()
const availableTableKey = ref(0)
const availableRows = ref([])
const availableTotal = ref(0)
const availablePage = ref(1)
const availableSize = ref(10)
const availableSelection = ref([])
const addFilters = reactive({ keyword: '', moduleId: null, priority: '' })

const planId = computed(() => route.params.id)

const canDelete = computed(() => {
  const plan = detail.value.plan || {}
  return (plan.status || 'PLANNED') === 'PLANNED' && !(detail.value.stats?.executed > 0)
})

function isCaseExecuted(row) {
  return Boolean(row.runStatus && row.runStatus !== 'UNTESTED')
}

const filteredCases = computed(() => {
  const cases = detail.value.cases || []
  if (!filterStatus.value) {
    return cases
  }
  return cases.filter((item) => item.runStatus === filterStatus.value)
})

const executionPercent = computed(() => {
  const stats = detail.value.stats || {}
  if (!stats.total) {
    return 0
  }
  return Math.round(((stats.executed || 0) / stats.total) * 100)
})

function statusLabel(status) {
  return { PLANNED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', ARCHIVED: '已归档' }[status] || status
}

function statusTag(status) {
  return { PLANNED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', ARCHIVED: 'info' }[status] || 'info'
}

function priorityTag(priority) {
  return { P0: 'danger', P1: 'warning', P2: 'primary', P3: 'info' }[priority] || 'info'
}

function runLabel(status) {
  return { UNTESTED: '未执行', PASS: '通过', FAIL: '失败', BLOCKED: '阻塞', SKIPPED: '跳过' }[status] || status
}

function runTag(status) {
  return { UNTESTED: 'info', PASS: 'success', FAIL: 'danger', BLOCKED: 'warning', SKIPPED: 'info' }[status] || 'info'
}

function caseStatusLabel(status) {
  return { DRAFT: '草稿', ACTIVE: '就绪', DEPRECATED: '已废弃' }[status] || status || '-'
}

async function load() {
  loading.value = true
  try {
    detail.value = await planApi.detail(planId.value)
  } finally {
    loading.value = false
  }
}

function openEdit() {
  const plan = detail.value.plan || {}
  editForm.name = plan.name || ''
  editForm.status = plan.status || 'PLANNED'
  editForm.range = plan.startDate && plan.endDate ? [plan.startDate, plan.endDate] : []
  editForm.description = plan.description || ''
  editDialogVisible.value = true
}

async function submitEdit() {
  await editFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    editSaving.value = true
    try {
      await planApi.update(planId.value, {
        projectId: detail.value.plan?.projectId,
        name: editForm.name,
        status: editForm.status,
        description: editForm.description,
        startDate: editForm.range?.[0] || null,
        endDate: editForm.range?.[1] || null
      })
      ElMessage.success('计划已更新')
      editDialogVisible.value = false
      load()
    } finally {
      editSaving.value = false
    }
  })
}

async function removePlan() {
  await ElMessageBox.confirm(`确认删除计划「${detail.value.plan?.name}」？删除后不可恢复。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await planApi.remove(planId.value)
  ElMessage.success('计划已删除')
  router.push('/plans')
}

async function loadModules() {
  if (!detail.value.plan?.projectId) {
    moduleTree.value = []
    return
  }
  moduleTree.value = await moduleApi.tree(detail.value.plan.projectId)
}

async function openAddCases() {
  addFilters.keyword = ''
  addFilters.moduleId = null
  addFilters.priority = ''
  availablePage.value = 1
  availableSelection.value = []
  availableRows.value = []
  availableTotal.value = 0
  availableTableKey.value += 1
  addDialogVisible.value = true
  await loadModules()
  await loadAvailable()
}

async function loadAvailable() {
  if (!detail.value.plan?.projectId) {
    return
  }
  availableLoading.value = true
  try {
    const data = await planApi.availableCases(planId.value, {
      keyword: addFilters.keyword,
      moduleId: addFilters.moduleId || undefined,
      priority: addFilters.priority,
      page: availablePage.value,
      size: availableSize.value
    })
    availableRows.value = data.list
    availableTotal.value = data.total
  } finally {
    availableLoading.value = false
  }
}

function searchAvailable() {
  availablePage.value = 1
  loadAvailable()
}

function onAvailableSizeChange() {
  availablePage.value = 1
  loadAvailable()
}

function onAvailableSelectionChange(rows) {
  availableSelection.value = rows
}

async function submitAddCases() {
  if (!availableSelection.value.length) {
    return
  }
  adding.value = true
  try {
    const result = await planApi.addCases(planId.value, availableSelection.value.map((item) => item.id))
    ElMessage.success(`已添加 ${result.added ?? availableSelection.value.length} 条用例`)
    addDialogVisible.value = false
    load()
  } finally {
    adding.value = false
  }
}

async function removeCase(row) {
  await ElMessageBox.confirm(`确认从计划中移除用例「${row.title}」？`, '移除用例', {
    type: 'warning',
    confirmButtonText: '移除',
    cancelButtonText: '取消'
  })
  await planApi.removeCase(planId.value, row.id)
  ElMessage.success('用例已移除')
  load()
}

function openRun(row) {
  currentCase.value = row
  runForm.status = row.runStatus && row.runStatus !== 'UNTESTED' ? row.runStatus : 'PASS'
  runForm.remark = row.remark || ''
  runDialogVisible.value = true
}

async function submitRun() {
  saving.value = true
  try {
    await planApi.submitRun(planId.value, {
      caseId: currentCase.value.id,
      status: runForm.status,
      remark: runForm.remark
    })
    ElMessage.success('执行结果已记录')
    runDialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await load()
  if (route.query.addCases === '1') {
    router.replace({ path: route.path, query: {} })
    openAddCases()
  }
})
</script>

<style scoped>
.title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.description {
  margin: 0 0 6px;
  color: #4a5568;
  font-size: 14px;
}

.detail-stats {
  margin: 18px 0 14px;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.mini-stat {
  background: #f7f9fc;
  border-radius: 8px;
  padding: 12px 14px;
}

.mini-stat .label {
  color: #8a94a6;
  font-size: 12px;
}

.mini-stat .value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 4px;
}

.mini-stat .value.pass {
  color: #38b26b;
}

.mini-stat .value.fail {
  color: #e34d59;
}

.progress {
  margin-top: 8px;
}

.section {
  margin-top: 16px;
}

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

.run-title {
  margin: 0 0 16px;
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>

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

      <el-table :data="filteredCases" stripe>
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
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRun(row)">记录结果</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { planApi } from '../api'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const detail = ref({})
const filterStatus = ref('')
const runDialogVisible = ref(false)
const currentCase = ref(null)
const runForm = reactive({ status: 'PASS', remark: '' })

const planId = computed(() => route.params.id)

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

async function load() {
  loading.value = true
  try {
    detail.value = await planApi.detail(planId.value)
  } finally {
    loading.value = false
  }
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

onMounted(load)
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
</style>

<template>
  <div v-loading="loading">
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">今日调用</div>
        <div class="value">{{ stats.calls }}</div>
      </div>
      <div class="stat-card">
        <div class="label">今日 Token</div>
        <div class="value">{{ stats.totalTokens }}</div>
      </div>
      <div class="stat-card">
        <div class="label">平均耗时</div>
        <div class="value">{{ stats.avgLatencyMs }}<span class="unit">ms</span></div>
      </div>
      <div class="stat-card">
        <div class="label">本月配额使用</div>
        <div class="value">{{ quotaUsagePercent }}<span class="unit">%</span></div>
        <div class="hint">
          {{ quota?.tokenUsed ?? 0 }} / {{ quota?.tokenQuota ? quota.tokenQuota : '不限' }}
        </div>
      </div>
    </div>

    <div class="page-card" style="margin-top: 16px">
      <div class="page-header">
        <h3 class="page-title">调用日志</h3>
        <div class="toolbar" style="margin: 0">
          <el-select v-model="filters.scene" placeholder="场景" clearable style="width: 150px" @change="reload">
            <el-option v-for="item in scenes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="reload">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>

      <el-table :data="rows" stripe>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="场景" width="130">
          <template #default="{ row }">{{ sceneLabel(row.scene) }}</template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型" width="130">
          <template #default="{ row }">{{ row.modelName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" effect="light">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalTokens" label="Token" width="100" />
        <el-table-column prop="latencyMs" label="耗时(ms)" width="100" />
        <el-table-column label="错误" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.errorMsg" class="error-text">{{ row.errorCode }} · {{ row.errorMsg }}</span>
            <span v-else class="muted">-</span>
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
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { aiAdminApi, aiMonitorApi } from '../api'

const scenes = [
  { value: 'GENERATE_CASE', label: '用例生成' },
  { value: 'REVIEW_CASE', label: '用例评审' },
  { value: 'GENERATE_REPORT', label: '测试报告' },
  { value: 'CONNECTION_TEST', label: '连接测试' }
]

const loading = ref(false)
const stats = ref({ calls: 0, totalTokens: 0, avgLatencyMs: 0 })
const quota = ref(null)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = reactive({ scene: '', status: '' })

const quotaUsagePercent = computed(() => {
  const q = quota.value
  if (!q || !q.tokenQuota) {
    return 0
  }
  return Math.min(100, Math.round(((q.tokenUsed || 0) / q.tokenQuota) * 100))
})

function sceneLabel(scene) {
  const matched = scenes.find((item) => item.value === scene)
  return matched ? matched.label : scene || '-'
}

async function load() {
  loading.value = true
  try {
    const [statsData, quotaData, logs] = await Promise.all([
      aiMonitorApi.stats(),
      aiAdminApi.quota(),
      aiMonitorApi.logs({
        scene: filters.scene,
        status: filters.status,
        page: page.value,
        size: size.value
      })
    ])
    stats.value = statsData
    quota.value = quotaData
    rows.value = logs.list
    total.value = logs.total
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

onMounted(load)
</script>

<style scoped>
.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.unit {
  font-size: 14px;
  margin-left: 4px;
  color: #8a94a6;
}

.error-text {
  color: #e34d59;
}
</style>

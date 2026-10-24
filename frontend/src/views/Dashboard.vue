<template>
  <div v-loading="loading">
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">项目总数</div>
        <div class="value">{{ stats.projectCount ?? 0 }}</div>
        <div class="hint">进行中的测试资产</div>
      </div>
      <div class="stat-card">
        <div class="label">测试用例</div>
        <div class="value">{{ stats.caseCount ?? 0 }}</div>
        <div class="hint">已沉淀的用例数量</div>
      </div>
      <div class="stat-card">
        <div class="label">测试计划</div>
        <div class="value">{{ stats.planCount ?? 0 }}</div>
        <div class="hint">历史与进行中的计划</div>
      </div>
      <div class="stat-card">
        <div class="label">整体通过率</div>
        <div class="value">{{ stats.passRate ?? 0 }}%</div>
        <div class="hint">共 {{ stats.runCount ?? 0 }} 条执行记录</div>
      </div>
    </div>

    <el-row :gutter="16" class="section">
      <el-col :xs="24" :md="12">
        <div class="page-card">
          <div class="page-header">
            <h3 class="page-title">用例优先级分布</h3>
          </div>
          <div v-if="priorityRows.length === 0" class="empty">暂无数据</div>
          <div v-for="item in priorityRows" :key="item.name" class="dist-row">
            <span class="dist-name">{{ item.name }}</span>
            <el-progress :percentage="percent(item.value, maxPriority)" :show-text="false" :stroke-width="10" />
            <span class="dist-value">{{ item.value }}</span>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :md="12">
        <div class="page-card">
          <div class="page-header">
            <h3 class="page-title">用例状态分布</h3>
          </div>
          <div v-if="statusRows.length === 0" class="empty">暂无数据</div>
          <div v-for="item in statusRows" :key="item.name" class="dist-row">
            <span class="dist-name">{{ statusLabel(item.name) }}</span>
            <el-progress
              :percentage="percent(item.value, maxStatus)"
              :show-text="false"
              :stroke-width="10"
              color="#38b26b"
            />
            <span class="dist-value">{{ item.value }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="page-card section">
      <div class="page-header">
        <h3 class="page-title">最近测试计划</h3>
        <el-button text type="primary" @click="$router.push('/plans')">查看全部</el-button>
      </div>
      <el-table :data="stats.recentPlans || []" stripe>
        <el-table-column prop="name" label="计划名称" min-width="200" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="planTag(row.status)" effect="light">{{ planLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="起止日期" width="220">
          <template #default="{ row }">
            {{ row.startDate || '-' }} ~ {{ row.endDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/plans/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { dashboardApi } from '../api'

const loading = ref(false)
const stats = ref({})

const priorityRows = computed(() => stats.value.priorityDistribution || [])
const statusRows = computed(() => stats.value.statusDistribution || [])
const maxPriority = computed(() => Math.max(1, ...priorityRows.value.map((i) => i.value)))
const maxStatus = computed(() => Math.max(1, ...statusRows.value.map((i) => i.value)))

function percent(value, max) {
  return Math.round((value / max) * 100)
}

function statusLabel(status) {
  return { DRAFT: '草稿', ACTIVE: '就绪', DEPRECATED: '已废弃' }[status] || status
}

function planLabel(status) {
  return { PLANNED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', ARCHIVED: '已归档' }[status] || status
}

function planTag(status) {
  return { PLANNED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', ARCHIVED: 'info' }[status] || 'info'
}

async function load() {
  loading.value = true
  try {
    stats.value = await dashboardApi.stats()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.section {
  margin-top: 16px;
}

.dist-row {
  display: grid;
  grid-template-columns: 70px 1fr 40px;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.dist-name {
  font-size: 13px;
  color: #5b6577;
  white-space: nowrap;
}

.dist-value {
  text-align: right;
  font-size: 13px;
  font-weight: 600;
}

.empty {
  color: #8a94a6;
  font-size: 13px;
  padding: 20px 0;
}
</style>

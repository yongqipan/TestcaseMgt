<template>
  <div v-loading="loading">
    <div class="page-card">
      <div class="page-header">
        <div class="title-group">
          <el-button text :icon="ArrowLeft" @click="$router.push('/cases')">返回</el-button>
          <h3 class="page-title">{{ detail.title || '用例详情' }}</h3>
          <el-tag v-if="detail.priority" :type="priorityTag(detail.priority)" effect="light">
            {{ detail.priority }}
          </el-tag>
          <el-tag :type="statusTag(detail.status)" effect="plain">{{ statusLabel(detail.status) }}</el-tag>
        </div>
        <div class="toolbar" style="margin: 0">
          <el-button type="primary" :icon="Edit" @click="startEdit">编辑</el-button>
          <el-button :icon="Star" :loading="reviewing" @click="runAiReview">AI 评审</el-button>
          <el-button type="danger" plain :icon="Delete" @click="remove">删除</el-button>
        </div>
      </div>

      <div v-if="editing">
        <CaseForm
          ref="formRef"
          :project-id="detail.projectId"
          :module-tree="moduleTree"
          :case-data="detail"
          @save="save"
        />
        <div class="edit-actions">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
        </div>
      </div>

      <template v-else>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="用例编号">{{ detail.caseNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属项目">{{ projectName }}</el-descriptions-item>
          <el-descriptions-item label="所属模块">{{ moduleName }}</el-descriptions-item>
          <el-descriptions-item label="用例类型">{{ typeLabel(detail.type) }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.creator || '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detail.updatedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源">
            <el-tag :type="detail.source === 'AI' ? 'success' : 'info'" effect="light" size="small">
              {{ detail.source === 'AI' ? 'AI 生成' : '手工创建' }}
            </el-tag>
            <el-tag
              v-if="detail.aiConfidence != null"
              size="small"
              type="primary"
              effect="plain"
              style="margin-left: 6px"
            >
              置信度 {{ detail.aiConfidence }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="AI 评审分">
            <span v-if="detail.aiReviewScore != null" :class="scoreClass(detail.aiReviewScore)">
              {{ detail.aiReviewScore }}
            </span>
            <span v-else class="muted">未评审</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="section">
          <h4>前置条件</h4>
          <p class="content">{{ detail.precondition || '无' }}</p>
        </div>

        <div class="section">
          <h4>测试步骤</h4>
          <el-table v-if="steps.length" :data="steps" border>
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="action" label="操作步骤" min-width="280" />
            <el-table-column prop="expected" label="预期结果" min-width="280" />
          </el-table>
          <p v-else class="muted">暂无用例步骤</p>
        </div>

        <div v-if="tags.length" class="section">
          <h4>标签</h4>
          <el-tag v-for="tag in tags" :key="tag" class="tag" type="info" effect="plain">{{ tag }}</el-tag>
        </div>
      </template>
    </div>

    <el-dialog v-model="reviewVisible" title="AI 用例评审" width="640px">
      <div v-if="reviewResult" class="review-panel">
        <div class="review-score">
          <span class="score-value" :class="scoreClass(reviewResult.totalScore)">{{ reviewResult.totalScore }}</span>
          <span class="muted">/ 100</span>
        </div>
        <div v-if="dimensionEntries.length" class="dimension-list">
          <div v-for="entry in dimensionEntries" :key="entry.key" class="dimension-row">
            <span class="dimension-name">{{ dimensionLabel(entry.key) }}</span>
            <el-progress :percentage="Number(entry.value) || 0" :stroke-width="10" style="flex: 1" />
          </div>
        </div>
        <div class="section">
          <h4>改进建议</h4>
          <ul v-if="reviewSuggestions.length" class="suggestion-list">
            <li v-for="(item, index) in reviewSuggestions" :key="index">{{ item }}</li>
          </ul>
          <p v-else class="muted">暂无建议</p>
        </div>
      </div>
      <div v-else-if="reviewError" class="error-text">{{ reviewError }}</div>
      <el-empty v-else description="暂无评审结果" />
      <template #footer>
        <el-button @click="reviewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Delete, Edit, Star } from '@element-plus/icons-vue'
import CaseForm from '../components/CaseForm.vue'
import { aiInsightApi, caseApi, moduleApi, projectApi } from '../api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const detail = ref({})
const moduleTree = ref([])
const projects = ref([])
const formRef = ref()

const reviewing = ref(false)
const reviewVisible = ref(false)
const reviewResult = ref(null)
const reviewError = ref('')

const dimensionEntries = computed(() => {
  const raw = reviewResult.value?.dimensions
  if (!raw) {
    return []
  }
  let parsed = raw
  if (typeof raw === 'string') {
    try {
      parsed = JSON.parse(raw)
    } catch (e) {
      return []
    }
  }
  if (!parsed || typeof parsed !== 'object') {
    return []
  }
  return Object.entries(parsed)
    .filter(([, value]) => typeof value === 'number')
    .map(([key, value]) => ({ key, value }))
})

const reviewSuggestions = computed(() => {
  const raw = reviewResult.value?.suggestions
  if (!raw) {
    return []
  }
  let parsed = raw
  if (typeof raw === 'string') {
    try {
      parsed = JSON.parse(raw)
    } catch (e) {
      return [raw]
    }
  }
  if (Array.isArray(parsed)) {
    return parsed.map((item) => (typeof item === 'string' ? item : JSON.stringify(item)))
  }
  return [String(parsed)]
})

const steps = computed(() => detail.value.steps || [])

const tags = computed(() => {
  const raw = detail.value.tags || ''
  return raw
    .split(/[,，]/)
    .map((tag) => tag.trim())
    .filter(Boolean)
})

const projectName = computed(() => {
  const matched = projects.value.find((item) => item.id === detail.value.projectId)
  return matched ? matched.name : detail.value.projectId || '-'
})

const moduleName = computed(() => {
  if (!detail.value.moduleId) {
    return '-'
  }
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
  return map.get(detail.value.moduleId) || '-'
})

function priorityTag(priority) {
  return { P0: 'danger', P1: 'warning', P2: 'primary', P3: 'info' }[priority] || 'info'
}

function statusLabel(status) {
  return { DRAFT: '草稿', ACTIVE: '就绪', DEPRECATED: '已废弃' }[status] || status || '-'
}

function statusTag(status) {
  return { DRAFT: 'info', ACTIVE: 'success', DEPRECATED: 'danger' }[status] || 'info'
}

function typeLabel(type) {
  return { FUNCTIONAL: '功能测试', API: '接口测试', UI: '界面测试', PERFORMANCE: '性能测试', SECURITY: '安全测试' }[type] || type || '-'
}

function scoreClass(score) {
  if (score == null) {
    return ''
  }
  if (score >= 80) {
    return 'score-good'
  }
  if (score >= 60) {
    return 'score-mid'
  }
  return 'score-bad'
}

function dimensionLabel(key) {
  return {
    completeness: '完整性',
    executability: '可执行性',
    coverage: '覆盖率',
    clarity: '清晰度'
  }[key] || key
}

async function runAiReview() {
  reviewing.value = true
  reviewResult.value = null
  reviewError.value = ''
  reviewVisible.value = true
  try {
    const result = await aiInsightApi.review({ caseId: detail.value.id })
    reviewResult.value = result
    detail.value.aiReviewScore = result.totalScore
  } catch (e) {
    reviewError.value = e?.message || '评审失败，请检查 AI 模型配置'
  } finally {
    reviewing.value = false
  }
}

async function load() {
  loading.value = true
  try {
    detail.value = await caseApi.detail(route.params.id)
    if (detail.value.projectId) {
      const [tree, projectList] = await Promise.all([
        moduleApi.tree(detail.value.projectId),
        projectApi.all()
      ])
      moduleTree.value = tree
      projects.value = projectList
    }
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  load()
}

async function save(payload) {
  saving.value = true
  try {
    await caseApi.update(detail.value.id, payload)
    ElMessage.success('用例已更新')
    editing.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function submit() {
  formRef.value.submit()
}

async function remove() {
  await ElMessageBox.confirm(
    `确认删除用例「${detail.value.title}」？相关执行记录将一并删除。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
  )
  await caseApi.remove(detail.value.id)
  ElMessage.success('用例已删除')
  router.replace('/cases')
}

onMounted(load)
</script>

<style scoped>
.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.section {
  margin-top: 22px;
}

.section h4 {
  margin: 0 0 10px;
  font-size: 15px;
}

.content {
  margin: 0;
  color: #4a5568;
  line-height: 1.8;
  white-space: pre-wrap;
}

.tag {
  margin-right: 8px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.score-good {
  color: #38b26b;
  font-weight: 700;
}

.score-mid {
  color: #e6a23c;
  font-weight: 700;
}

.score-bad {
  color: #e34d59;
  font-weight: 700;
}

.review-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-score {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.review-score .score-value {
  font-size: 40px;
}

.dimension-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dimension-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dimension-name {
  width: 72px;
  color: #4a5568;
  font-size: 13px;
}

.suggestion-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.9;
  color: #4a5568;
}

.error-text {
  color: #e34d59;
}
</style>

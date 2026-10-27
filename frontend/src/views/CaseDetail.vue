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
          <el-button type="primary" :icon="Edit" @click="editVisible = true">编辑</el-button>
          <el-button type="danger" plain :icon="Delete" @click="remove">删除</el-button>
        </div>
      </div>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="用例编号">{{ detail.caseNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属项目">{{ projectName }}</el-descriptions-item>
        <el-descriptions-item label="所属模块">{{ moduleName }}</el-descriptions-item>
        <el-descriptions-item label="用例类型">{{ typeLabel(detail.type) }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detail.creator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updatedAt || '-' }}</el-descriptions-item>
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
    </div>

    <CaseFormDialog
      v-model="editVisible"
      :project-id="detail.projectId"
      :module-tree="moduleTree"
      :case-data="detail"
      @saved="load"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Delete, Edit } from '@element-plus/icons-vue'
import CaseFormDialog from '../components/CaseFormDialog.vue'
import { caseApi, moduleApi, projectApi } from '../api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref({})
const moduleTree = ref([])
const projects = ref([])
const editVisible = ref(false)

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
  return { FUNCTIONAL: '功能测试', API: '接口测试', UI: '界面测试', PERFORMANCE: '性能测试' }[type] || type || '-'
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
</style>

<template>
  <div class="page-card">
    <div class="page-header">
      <h3 class="page-title">测试计划</h3>
      <el-button type="primary" :icon="Plus" :disabled="!projects.length" @click="openCreate">新建计划</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="projectId" placeholder="全部项目" clearable style="width: 200px" @change="reload">
        <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索计划名称"
        clearable
        style="width: 240px"
        :prefix-icon="Search"
        @keyup.enter="reload"
        @clear="reload"
      />
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe class="clickable-table" @row-click="openDetail">
      <el-table-column prop="name" label="计划名称" min-width="200" />
      <el-table-column label="所属项目" width="140">
        <template #default="{ row }">{{ projectName(row.projectId) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" effect="light">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="起止日期" width="210">
        <template #default="{ row }">{{ row.startDate || '-' }} ~ {{ row.endDate || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdBy" label="创建人" width="110" />
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

    <el-dialog v-model="dialogVisible" title="新建计划" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="item in projects" :key="item.id" :label="`${item.name}（${item.code}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：V1.2 版本回归测试" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="未开始" value="PLANNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="起止日期">
          <el-date-picker
            v-model="form.range"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="本轮测试的范围与目标" />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { planApi, projectApi } from '../api'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const projects = ref([])
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const projectId = ref(null)

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  projectId: null,
  name: '',
  status: 'PLANNED',
  range: [],
  description: ''
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }]
}

const projectMap = computed(() => {
  const map = new Map()
  projects.value.forEach((item) => map.set(item.id, item.name))
  return map
})

function projectName(id) {
  return projectMap.value.get(id) || '-'
}

function statusLabel(status) {
  return { PLANNED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', ARCHIVED: '已归档' }[status] || status
}

function statusTag(status) {
  return { PLANNED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', ARCHIVED: 'info' }[status] || 'info'
}

async function loadProjects() {
  projects.value = await projectApi.all()
}

async function load() {
  loading.value = true
  try {
    const data = await planApi.page({
      projectId: projectId.value || undefined,
      keyword: keyword.value,
      page: page.value,
      size: size.value
    })
    rows.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function openCreate() {
  form.projectId = projectId.value || projects.value[0]?.id || null
  form.name = ''
  form.status = 'PLANNED'
  form.range = []
  form.description = ''
  dialogVisible.value = true
}

function openDetail(row) {
  router.push(`/plans/${row.id}`)
}

async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    saving.value = true
    try {
      const payload = {
        projectId: form.projectId,
        name: form.name,
        status: form.status,
        description: form.description,
        startDate: form.range?.[0] || null,
        endDate: form.range?.[1] || null
      }
      const created = await planApi.create(payload)
      ElMessage.success('计划已创建，请选择要执行的用例')
      dialogVisible.value = false
      load()
      router.push({ path: `/plans/${created.id}`, query: { addCases: '1' } })
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  await loadProjects()
  await load()
})
</script>

<style scoped>
.clickable-table :deep(.el-table__row) {
  cursor: pointer;
}
</style>

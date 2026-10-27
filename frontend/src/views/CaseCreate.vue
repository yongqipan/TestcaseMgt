<template>
  <div class="page-card">
    <div class="page-header">
      <div class="title-group">
        <el-button text :icon="ArrowLeft" @click="$router.push('/cases')">返回</el-button>
        <h3 class="page-title">新建用例</h3>
      </div>
      <div class="toolbar" style="margin: 0">
        <el-button @click="$router.push('/cases')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </div>
    </div>

    <CaseForm
      ref="formRef"
      v-model:project-id="projectId"
      :projects="projects"
      :module-tree="moduleTree"
      show-project
      @project-change="onProjectChange"
      @save="save"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import CaseForm from '../components/CaseForm.vue'
import { caseApi, moduleApi, projectApi } from '../api'

const router = useRouter()
const saving = ref(false)
const formRef = ref()
const projects = ref([])
const projectId = ref(null)
const moduleTree = ref([])

async function onProjectChange(value) {
  if (value) {
    localStorage.setItem('tcm_project_id', String(value))
  }
  moduleTree.value = value ? await moduleApi.tree(value) : []
}

async function load() {
  projects.value = await projectApi.all()
  const cached = Number(localStorage.getItem('tcm_project_id'))
  const matched = projects.value.find((item) => item.id === cached)
  projectId.value = matched ? matched.id : projects.value[0]?.id || null
  await onProjectChange(projectId.value)
}

async function save(payload) {
  saving.value = true
  try {
    await caseApi.create(payload)
    ElMessage.success('用例已创建')
    router.push('/cases')
  } finally {
    saving.value = false
  }
}

function submit() {
  formRef.value.submit()
}

onMounted(load)
</script>

<style scoped>
.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>

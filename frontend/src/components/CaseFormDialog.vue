<template>
  <el-dialog
    v-model="visible"
    :title="editing ? '编辑用例' : '新建用例'"
    width="820px"
    top="6vh"
    destroy-on-close
  >
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
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import { caseApi } from '../api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  projectId: { type: [Number, String], default: null },
  moduleTree: { type: Array, default: () => [] },
  caseData: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const priorities = ['P0', 'P1', 'P2', 'P3']
const saving = ref(false)
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

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const editing = computed(() => Boolean(props.caseData?.id))

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      fillForm()
    }
  }
)

function fillForm() {
  const data = props.caseData
  form.title = data?.title || ''
  form.moduleId = data?.moduleId ?? null
  form.priority = data?.priority || 'P1'
  form.type = data?.type || 'FUNCTIONAL'
  form.status = data?.status || 'ACTIVE'
  form.precondition = data?.precondition || ''
  form.tags = data?.tags || ''
  form.steps = (data?.steps || []).map((step) => ({ action: step.action, expected: step.expected }))
  if (!form.steps.length) {
    addStep()
  }
}

function addStep() {
  form.steps.push({ action: '', expected: '' })
}

function removeStep(index) {
  form.steps.splice(index, 1)
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  if (!props.projectId) {
    ElMessage.warning('请先选择项目')
    return
  }
  saving.value = true
  try {
    const payload = {
      projectId: props.projectId,
      moduleId: form.moduleId,
      title: form.title,
      priority: form.priority,
      type: form.type,
      status: form.status,
      precondition: form.precondition,
      tags: form.tags,
      steps: form.steps.filter((step) => step.action && step.action.trim())
    }
    if (editing.value) {
      await caseApi.update(props.caseData.id, payload)
      ElMessage.success('用例已更新')
    } else {
      await caseApi.create(payload)
      ElMessage.success('用例已创建')
    }
    visible.value = false
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>

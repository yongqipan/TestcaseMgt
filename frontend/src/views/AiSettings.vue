<template>
  <div v-loading="loading">
    <div class="page-card">
      <div class="page-header">
        <div class="title-group">
          <h3 class="page-title">AI 模型配置</h3>
          <el-tag type="primary" effect="light" size="small">AI</el-tag>
        </div>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="模型提供商" name="providers">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="openProvider()">新增提供商</el-button>
          </div>
          <el-table :data="providers" stripe>
            <el-table-column prop="name" label="名称" min-width="140" />
            <el-table-column prop="baseUrl" label="Base URL" min-width="240" show-overflow-tooltip />
            <el-table-column label="API Key" width="160">
              <template #default="{ row }">
                <span :class="row.hasApiKey ? '' : 'muted'">{{ row.apiKey || '未配置' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" effect="light">
                  {{ row.enabled ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openProvider(row)">编辑</el-button>
                <el-button link type="danger" @click="removeProvider(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="模型" name="models">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" :disabled="!providers.length" @click="openModel()">新增模型</el-button>
            <span v-if="!providers.length" class="muted">请先添加模型提供商</span>
          </div>
          <el-table :data="models" stripe>
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column prop="modelCode" label="模型编码" min-width="160" show-overflow-tooltip />
            <el-table-column prop="providerName" label="提供商" width="120" />
            <el-table-column label="场景" width="130">
              <template #default="{ row }">{{ sceneLabel(row.scene) }}</template>
            </el-table-column>
            <el-table-column label="默认" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault" type="success" effect="light">默认</el-tag>
                <span v-else class="muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" effect="light">
                  {{ row.enabled ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="210" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :loading="testingId === row.id" @click="testModel(row)">测试</el-button>
                <el-button link type="primary" @click="openModel(row)">编辑</el-button>
                <el-button link type="danger" @click="removeModel(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Prompt 模板" name="prompts">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="openPrompt()">新增模板</el-button>
          </div>
          <el-table :data="prompts" stripe>
            <el-table-column label="场景" width="140">
              <template #default="{ row }">{{ sceneLabel(row.scene) }}</template>
            </el-table-column>
            <el-table-column prop="name" label="名称" min-width="180" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" effect="light">
                  {{ row.enabled ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedBy" label="更新人" width="110">
              <template #default="{ row }">{{ row.updatedBy || '-' }}</template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPrompt(row)">编辑</el-button>
                <el-button link type="danger" @click="removePrompt(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="配额与监控" name="quota">
          <el-form label-width="120px" style="max-width: 520px">
            <el-form-item label="本月 Token 配额">
              <el-input-number v-model="quotaForm.tokenQuota" :min="0" :step="10000" style="width: 220px" />
              <span class="muted" style="margin-left: 10px">0 表示不限制</span>
            </el-form-item>
            <el-form-item label="告警阈值(%)">
              <el-input-number v-model="quotaForm.alertThreshold" :min="1" :max="100" style="width: 160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingQuota" @click="saveQuota">保存配额</el-button>
            </el-form-item>
          </el-form>
          <el-descriptions v-if="quota" :column="3" border style="margin-top: 10px">
            <el-descriptions-item label="统计月份">{{ quota.quotaMonth }}</el-descriptions-item>
            <el-descriptions-item label="已用 Token">{{ quota.tokenUsed }}</el-descriptions-item>
            <el-descriptions-item label="累计成本">{{ quota.costUsed }}</el-descriptions-item>
          </el-descriptions>
          <el-descriptions :column="3" border style="margin-top: 12px">
            <el-descriptions-item label="今日调用">{{ todayStats.calls }}</el-descriptions-item>
            <el-descriptions-item label="今日 Token">{{ todayStats.totalTokens }}</el-descriptions-item>
            <el-descriptions-item label="平均耗时">{{ todayStats.avgLatencyMs }} ms</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="providerDialog" :title="providerForm.id ? '编辑提供商' : '新增提供商'" width="560px">
      <el-form ref="providerFormRef" :model="providerForm" :rules="providerRules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="providerForm.name" placeholder="如 OpenAI / DeepSeek" />
        </el-form-item>
        <el-form-item label="Base URL" prop="baseUrl">
          <el-input v-model="providerForm.baseUrl" placeholder="https://api.openai.com/v1" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input
            v-model="providerForm.apiKey"
            type="password"
            show-password
            :placeholder="providerForm.id ? '留空表示不修改' : '请输入 API Key'"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="providerForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="providerDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitProvider">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="modelDialog" :title="modelForm.id ? '编辑模型' : '新增模型'" width="560px">
      <el-form ref="modelFormRef" :model="modelForm" :rules="modelRules" label-width="100px">
        <el-form-item label="提供商" prop="providerId">
          <el-select v-model="modelForm.providerId" style="width: 100%">
            <el-option v-for="item in providers" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="modelForm.name" placeholder="如 GPT-4o" />
        </el-form-item>
        <el-form-item label="模型编码" prop="modelCode">
          <el-input v-model="modelForm.modelCode" placeholder="如 gpt-4o-mini" />
        </el-form-item>
        <el-form-item label="适用场景">
          <el-select v-model="modelForm.scene" clearable placeholder="不限" style="width: 100%">
            <el-option v-for="item in scenes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="输入单价/千token">
          <el-input-number v-model="modelForm.inputPrice" :min="0" :precision="4" style="width: 200px" />
        </el-form-item>
        <el-form-item label="输出单价/千token">
          <el-input-number v-model="modelForm.outputPrice" :min="0" :precision="4" style="width: 200px" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="modelForm.isDefault" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="modelForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitModel">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="promptDialog" :title="promptForm.id ? '编辑 Prompt 模板' : '新增 Prompt 模板'" width="760px">
      <el-form ref="promptFormRef" :model="promptForm" :rules="promptRules" label-width="90px">
        <el-form-item label="场景" prop="scene">
          <el-select v-model="promptForm.scene" :disabled="Boolean(promptForm.id)" style="width: 100%">
            <el-option v-for="item in scenes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="promptForm.name" placeholder="如 用例生成 Prompt v2" />
        </el-form-item>
        <el-form-item label="变量">
          <el-input v-model="promptForm.variables" placeholder="逗号分隔，如 projectName,moduleName,requirement" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="promptForm.content" type="textarea" :rows="12" placeholder="使用 {{变量名}} 占位符" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="promptForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promptDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPrompt">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { aiAdminApi, aiMonitorApi } from '../api'

const scenes = [
  { value: 'GENERATE_CASE', label: '用例生成' },
  { value: 'REVIEW_CASE', label: '用例评审' },
  { value: 'GENERATE_REPORT', label: '测试报告' }
]

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('providers')
const providers = ref([])
const models = ref([])
const prompts = ref([])
const quota = ref(null)
const todayStats = ref({ calls: 0, totalTokens: 0, avgLatencyMs: 0 })
const testingId = ref(null)

const providerDialog = ref(false)
const providerFormRef = ref()
const providerForm = reactive({ id: null, name: '', baseUrl: '', apiKey: '', enabled: true })
const providerRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入 Base URL', trigger: 'blur' }]
}

const modelDialog = ref(false)
const modelFormRef = ref()
const modelForm = reactive({
  id: null, providerId: null, name: '', modelCode: '', scene: '',
  inputPrice: 0, outputPrice: 0, isDefault: false, enabled: true
})
const modelRules = {
  providerId: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  modelCode: [{ required: true, message: '请输入模型编码', trigger: 'blur' }]
}

const promptDialog = ref(false)
const promptFormRef = ref()
const promptForm = reactive({ id: null, scene: 'GENERATE_CASE', name: '', variables: '', content: '', enabled: true })
const promptRules = {
  scene: [{ required: true, message: '请选择场景', trigger: 'change' }],
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

const quotaForm = reactive({ tokenQuota: 0, alertThreshold: 80 })
const savingQuota = ref(false)

function sceneLabel(scene) {
  const matched = scenes.find((item) => item.value === scene)
  return matched ? matched.label : scene || '不限'
}

async function loadAll() {
  loading.value = true
  try {
    const [providerList, modelList, promptList, quotaData, stats] = await Promise.all([
      aiAdminApi.providers(),
      aiAdminApi.models(),
      aiAdminApi.prompts(),
      aiAdminApi.quota(),
      aiMonitorApi.stats()
    ])
    providers.value = providerList
    models.value = modelList
    prompts.value = promptList
    quota.value = quotaData
    todayStats.value = stats
    quotaForm.tokenQuota = quotaData.tokenQuota || 0
    quotaForm.alertThreshold = quotaData.alertThreshold || 80
  } finally {
    loading.value = false
  }
}

function openProvider(row) {
  Object.assign(providerForm, {
    id: row?.id || null,
    name: row?.name || '',
    baseUrl: row?.baseUrl || '',
    apiKey: '',
    enabled: row ? row.enabled : true
  })
  providerDialog.value = true
}

async function submitProvider() {
  await providerFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    saving.value = true
    try {
      const payload = {
        name: providerForm.name,
        baseUrl: providerForm.baseUrl,
        apiKey: providerForm.apiKey || undefined,
        enabled: providerForm.enabled
      }
      if (providerForm.id) {
        await aiAdminApi.updateProvider(providerForm.id, payload)
      } else {
        await aiAdminApi.createProvider(payload)
      }
      ElMessage.success('提供商已保存')
      providerDialog.value = false
      await loadAll()
    } finally {
      saving.value = false
    }
  })
}

async function removeProvider(row) {
  await ElMessageBox.confirm(`确认删除提供商「${row.name}」？其下模型可能无法使用。`, '删除确认', {
    type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
  })
  await aiAdminApi.removeProvider(row.id)
  ElMessage.success('已删除')
  await loadAll()
}

function openModel(row) {
  Object.assign(modelForm, {
    id: row?.id || null,
    providerId: row?.providerId || providers.value[0]?.id || null,
    name: row?.name || '',
    modelCode: row?.modelCode || '',
    scene: row?.scene || '',
    inputPrice: row?.inputPrice ?? 0,
    outputPrice: row?.outputPrice ?? 0,
    isDefault: row ? row.isDefault : false,
    enabled: row ? row.enabled : true
  })
  modelDialog.value = true
}

async function submitModel() {
  await modelFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    saving.value = true
    try {
      const payload = {
        providerId: modelForm.providerId,
        name: modelForm.name,
        modelCode: modelForm.modelCode,
        scene: modelForm.scene || null,
        inputPrice: modelForm.inputPrice,
        outputPrice: modelForm.outputPrice,
        isDefault: modelForm.isDefault,
        enabled: modelForm.enabled
      }
      if (modelForm.id) {
        await aiAdminApi.updateModel(modelForm.id, payload)
      } else {
        await aiAdminApi.createModel(payload)
      }
      ElMessage.success('模型已保存')
      modelDialog.value = false
      await loadAll()
    } finally {
      saving.value = false
    }
  })
}

async function removeModel(row) {
  await ElMessageBox.confirm(`确认删除模型「${row.name}」？`, '删除确认', {
    type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
  })
  await aiAdminApi.removeModel(row.id)
  ElMessage.success('已删除')
  await loadAll()
}

async function testModel(row) {
  testingId.value = row.id
  try {
    const result = await aiAdminApi.testModel(row.id)
    if (result.success) {
      ElMessage.success(`连接成功（${result.latencyMs}ms）：${result.reply}`)
    } else {
      ElMessage.error(`连接失败：${result.error}`)
    }
  } finally {
    testingId.value = null
  }
}

function openPrompt(row) {
  Object.assign(promptForm, {
    id: row?.id || null,
    scene: row?.scene || 'GENERATE_CASE',
    name: row?.name || '',
    variables: row?.variables || '',
    content: row?.content || '',
    enabled: row ? row.enabled : true
  })
  promptDialog.value = true
}

async function submitPrompt() {
  await promptFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    saving.value = true
    try {
      const payload = {
        scene: promptForm.scene,
        name: promptForm.name,
        variables: promptForm.variables,
        content: promptForm.content,
        enabled: promptForm.enabled
      }
      if (promptForm.id) {
        await aiAdminApi.updatePrompt(promptForm.id, payload)
      } else {
        await aiAdminApi.createPrompt(payload)
      }
      ElMessage.success('模板已保存')
      promptDialog.value = false
      await loadAll()
    } finally {
      saving.value = false
    }
  })
}

async function removePrompt(row) {
  await ElMessageBox.confirm(`确认删除模板「${row.name}」？`, '删除确认', {
    type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
  })
  await aiAdminApi.removePrompt(row.id)
  ElMessage.success('已删除')
  await loadAll()
}

async function saveQuota() {
  savingQuota.value = true
  try {
    quota.value = await aiAdminApi.updateQuota({
      tokenQuota: quotaForm.tokenQuota,
      alertThreshold: quotaForm.alertThreshold
    })
    ElMessage.success('配额已保存')
  } finally {
    savingQuota.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>

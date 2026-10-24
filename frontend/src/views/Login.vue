<template>
  <div class="login-page">
    <div class="brand">
      <div class="brand-badge">TCM</div>
      <h1>测试用例管理系统</h1>
      <p>集中管理测试资产，跟踪每一次执行结果</p>
      <ul>
        <li>项目 / 模块 / 用例三级组织</li>
        <li>测试计划与执行结果留痕</li>
        <li>通过率与质量分布实时统计</li>
      </ul>
    </div>

    <el-card class="login-card" shadow="always">
      <h2>账号登录</h2>
      <p class="muted">默认账号 admin / admin123</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    loading.value = true
    try {
      await auth.login({ ...form })
      ElMessage.success('登录成功')
      router.replace(route.query.redirect || '/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 80px;
  padding: 40px;
  background: radial-gradient(circle at 15% 20%, #2f6bff 0%, #162a5c 45%, #0b1220 100%);
  color: #fff;
  flex-wrap: wrap;
}

.brand {
  max-width: 420px;
}

.brand-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.15);
  font-weight: 700;
  letter-spacing: 2px;
}

.brand h1 {
  font-size: 34px;
  margin: 18px 0 10px;
}

.brand p {
  color: rgba(255, 255, 255, 0.75);
  margin: 0 0 20px;
}

.brand ul {
  padding-left: 18px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 2;
  font-size: 14px;
}

.login-card {
  width: 360px;
  border-radius: 14px;
}

.login-card h2 {
  margin: 0 0 4px;
}

.login-card .muted {
  margin: 0 0 18px;
}

.submit {
  width: 100%;
  margin-top: 6px;
}
</style>

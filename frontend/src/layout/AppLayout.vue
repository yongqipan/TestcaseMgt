<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="22"><DataAnalysis /></el-icon>
        <span>TCM 测试管理</span>
      </div>
      <el-menu :default-active="activeMenu" router class="menu" background-color="transparent">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/modules">
          <el-icon><Grid /></el-icon>
          <span>模块管理</span>
        </el-menu-item>
        <el-menu-item index="/cases">
          <el-icon><Document /></el-icon>
          <span>测试用例</span>
        </el-menu-item>
        <el-menu-item index="/plans">
          <el-icon><Calendar /></el-icon>
          <span>测试计划</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <el-dropdown @command="handleCommand">
          <span class="user">
            <el-avatar :size="30" class="avatar">{{ initials }}</el-avatar>
            <span class="name">{{ auth.displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>{{ auth.user?.username }}</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/plans')) {
    return '/plans'
  }
  if (route.path.startsWith('/cases')) {
    return '/cases'
  }
  return route.path
})

const pageTitle = computed(() => route.meta.title || '测试用例管理系统')

const initials = computed(() => {
  const name = auth.displayName
  return name ? name.slice(0, 1).toUpperCase() : 'T'
})

async function handleCommand(command) {
  if (command === 'logout') {
    await auth.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #101a33;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.menu {
  border-right: none;
  padding: 8px;
}

.menu :deep(.el-menu-item) {
  color: #b8c2d8;
  border-radius: 8px;
  margin-bottom: 4px;
}

.menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: rgba(47, 107, 255, 0.35);
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 2px rgba(31, 42, 68, 0.06);
  padding: 0 24px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #1f2a44;
}

.avatar {
  background: var(--tcm-primary);
}

.name {
  font-size: 14px;
}

.main {
  padding: 20px 24px;
  background: #f4f6fb;
  overflow-y: auto;
}
</style>

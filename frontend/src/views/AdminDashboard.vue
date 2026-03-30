<template>
  <div class="admin-dashboard">
    <el-container class="admin-container">
      <el-aside :width="isMobile ? '100%' : '240px'" class="admin-sidebar">
        <div v-if="!isMobile" class="sidebar-header">
          <h2>管理后台</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          :mode="isMobile ? 'horizontal' : 'vertical'"
          class="admin-menu"
          router
        >
          <el-menu-item index="/admin/dashboard">
            <span>数据大盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/projects">
            <span>项目审计</span>
          </el-menu-item>
          <el-menu-item index="/admin/notices">
            <span>系统通知</span>
          </el-menu-item>
          <el-menu-item index="/admin/funding">
            <span>资金管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)

const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.admin-dashboard {
  /* subtract header height to prevent scrollbar */
  height: calc(100vh - 72px);
  width: 100%;
  display: flex;
}
.admin-container {
  height: 100%;
  width: 100%;
}
.admin-sidebar {
  background-color: var(--bg-surface);
  transition: width 0.3s;
  border-right: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 24px 20px 12px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 8px;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 16px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 700;
}
.admin-menu {
  border-right: none;
  background-color: transparent;
}
.admin-main {
  padding: 24px;
  background-color: var(--bg-page);
  overflow-y: auto;
}

/* Customizing the menu items for a more modern look */
:deep(.el-menu-item) {
  margin: 8px 12px;
  border-radius: var(--radius-md);
  height: 48px;
  line-height: 48px;
  color: var(--text-secondary);
}

:deep(.el-menu-item.is-active) {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 600;
}

:deep(.el-menu-item:hover:not(.is-active)) {
  background-color: var(--border-light);
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .admin-container {
    flex-direction: column;
  }
  .admin-sidebar {
    height: auto;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }
  .admin-menu {
    display: flex;
    overflow-x: auto;
    white-space: nowrap;
    padding: 8px;
  }
  :deep(.el-menu-item) {
    margin: 0 4px;
  }
  .admin-menu::-webkit-scrollbar {
    display: none;
  }
  .admin-main {
    padding: 16px;
  }
}
</style>

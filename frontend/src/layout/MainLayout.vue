<template>
  <div class="layout-container">
    <header class="app-header" :class="{ 'scrolled': isScrolled }">
      <div class="header-content">
        <div class="logo" @click="router.push('/home')" tabindex="0" @keydown.enter="router.push('/home')">
          <img src="/favicon.svg" alt="FundTogether Logo" class="logo-icon" />
          FundTogether
        </div>
        <nav class="nav-links" aria-label="Main Navigation">
          <span v-if="userStore.token" class="user-greeting">
            你好，{{ userStore.userInfo?.nickname || userStore.userInfo?.account }}
          </span>
          <el-button 
            :type="route.path.startsWith('/projects') ? 'primary' : 'text'" 
            class="nav-btn" 
            @click="router.push('/projects')"
          >项目探索</el-button>
          
          <template v-if="!userStore.token">
            <el-button text @click="router.push('/login')">登录</el-button>
          </template>
          <template v-else>
            <el-button 
              v-if="userStore.userInfo?.role === 3" 
              :type="route.path.startsWith('/admin') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/admin')"
            >管理后台</el-button>
            <el-button 
              v-if="userStore.userInfo?.role === 2" 
              :type="route.path.startsWith('/sponsor/projects') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/sponsor/projects')"
            >我的项目</el-button>
            <el-button 
              :type="route.path.startsWith('/user/account') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/account')"
            >我的账户</el-button>
            <el-button 
              :type="route.path.startsWith('/user/orders') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/orders')"
            >我的支持</el-button>
            <el-button 
              :type="route.path.startsWith('/user/messages') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/messages')"
            >消息</el-button>
            <el-button 
              :type="route.path.startsWith('/user/profile') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/profile')"
            >个人主页</el-button>
            <el-button text type="danger" @click="logout">退出登录</el-button>
          </template>
        </nav>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../store/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { onMounted, onUnmounted, watch, ref } from 'vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const isScrolled = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

let ws: WebSocket | null = null

const initWebSocket = () => {
  if (ws) {
    ws.close()
  }
  const userId = userStore.userInfo?.id
  if (!userId) return

  ws = new WebSocket(`ws://localhost:8080/ws/${userId}`)

  ws.onopen = () => {
    console.log('WebSocket connected')
  }

  ws.onmessage = (event) => {
    try {
      const message = event.data
      ElNotification({
        title: '新通知',
        message: message,
        type: 'info',
        duration: 5000
      })
    } catch (e) {
      console.error('Failed to handle websocket message', e)
    }
  }

  ws.onclose = () => {
    console.log('WebSocket disconnected')
  }

  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  if (userStore.token) {
    initWebSocket()
  }
})

watch(() => userStore.token, (newVal) => {
  if (newVal) {
    initWebSocket()
  } else {
    if (ws) {
      ws.close()
      ws = null
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (ws) {
    ws.close()
  }
})

const logout = () => {
  userStore.logout()
  ElMessage.success('已成功退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-page);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.app-header.scrolled {
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 4px 20px -10px rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  height: 72px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.logo {
  font-family: var(--font-heading);
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
  transition: opacity 0.2s;
  outline: none;
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: inline-block;
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.logo:hover .logo-icon, .logo:focus .logo-icon {
  transform: scale(1.1) rotate(-5deg);
}

.logo:hover, .logo:focus {
  opacity: 0.9;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-greeting {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-right: 16px;
  padding-right: 16px;
  border-right: 1px solid var(--border-color);
}

.nav-btn {
  border-radius: var(--radius-pill);
  padding: 8px 16px;
  margin-right: 4px;
  transition: all 0.3s ease;
}

.nav-btn.el-button--primary {
  padding: 8px 20px;
}

.app-main {
  flex: 1;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

/* 响应式适配 */
@media (max-width: 900px) {
  .header-content {
    padding: 0 20px;
  }
  .nav-links .el-button {
    padding: 8px 12px;
  }
}

@media (max-width: 768px) {
  .app-header {
    height: auto;
  }
  .header-content {
    flex-direction: column;
    padding: 16px 20px;
    gap: 16px;
    height: auto;
  }
  .nav-links {
    flex-wrap: wrap;
    justify-content: center;
  }
  .user-greeting {
    display: none;
  }
}
</style>

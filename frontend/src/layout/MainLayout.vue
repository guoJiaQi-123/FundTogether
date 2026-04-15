<template>
  <div class="layout-container">
    <header class="app-header" :class="{ 'scrolled': isScrolled }">
      <div class="header-content">
        <div class="logo" @click="router.push('/home')" tabindex="0" @keydown.enter="router.push('/home')">
          <img src="/favicon.svg" alt="FundTogether Logo" class="logo-icon" />
          FundTogether
        </div>
        <nav class="nav-links" aria-label="Main Navigation">
          <el-dropdown @command="handleSetLanguage" style="margin-right: 12px; cursor: pointer;">
            <span class="el-dropdown-link" style="display: flex; align-items: center;">
              {{ t('common.language') }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh" :disabled="locale === 'zh'">中文</el-dropdown-item>
                <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <a href="https://github.com/guoJiaQi-123/FundTogether" target="_blank" class="github-link" title="GitHub">
            <svg viewBox="0 0 1024 1024" width="22" height="22" style="margin-right: 12px; margin-top: 5px;"><path d="M511.6 76.3C264.3 76.2 64 276.4 64 523.5 64 718.9 189.3 885 363.8 946c23.5 5.9 19.9-10.8 19.9-22.2v-77.5c-135.7 15.9-141.2-73.9-150.3-88.9C215 726 171.5 718 184.5 703c30.9-15.9 62.4 4 98.9 57.9 26.4 39.1 77.9 32.5 104 26 5.7-23.5 17.9-44.5 34.7-60.8-140.6-25.2-199.2-111-199.2-213 0-49.5 16.3-95 48.3-131.7-20.4-60.5 1.9-112.3 4.9-120 58.1-5.2 118.5 41.6 123.2 45.3 33-8.9 70.7-13.6 112.9-13.6 42.4 0 80.2 4.9 113.5 13.9 11.3-8.6 67.3-48.8 121.3-43.9 2.9 7.7 24.7 58.3 5.5 118 32.4 36.8 48.9 82.7 48.9 132.3 0 102.2-59 188.1-200 212.9 23.5 23.2 38.1 55.4 38.1 91v112.5c0.8 9 0 27.9 22.4 22.4C835 885 960 719 960 523.6 960 276.4 759.7 76.3 511.6 76.3z" fill="var(--text-primary)"></path></svg>
          </a>

          <span v-if="userStore.token" class="user-greeting">
            {{ locale === 'zh' ? '你好，' : 'Hello, ' }}{{ userStore.userInfo?.nickname || userStore.userInfo?.account }}
          </span>
          <el-button 
            :type="route.path.startsWith('/projects') ? 'primary' : 'text'" 
            class="nav-btn" 
            @click="router.push('/projects')"
          >{{ t('common.projects') }}</el-button>
          
          <template v-if="!userStore.token">
            <el-button text @click="router.push('/login')">{{ t('common.login') }}</el-button>
          </template>

          <template v-else>
            <el-button 
              v-if="isAdmin(userStore.userInfo?.role)" 
              :type="route.path.startsWith('/admin') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/admin')"
            >{{ t('common.admin') }}</el-button>
            <el-button 
              v-if="isSponsor(userStore.userInfo?.role)" 
              :type="route.path.startsWith('/sponsor/projects') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/sponsor/projects')"
            >{{ locale === 'zh' ? '我的项目' : 'My Projects' }}</el-button>
            <el-button 
              v-if="isSponsor(userStore.userInfo?.role)" 
              :type="route.path.startsWith('/user/profile') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/profile')"
            >{{ locale === 'zh' ? '账户' : 'Account' }}</el-button>
            <el-button 
              :type="route.path.startsWith('/user/orders') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/orders')"
            >{{ locale === 'zh' ? '我的支持' : 'My Support' }}</el-button>
            <el-button 
              :type="route.path.startsWith('/user/favorites') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/favorites')"
            >{{ locale === 'zh' ? '收藏' : 'Favorites' }}</el-button>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-button 
                :type="route.path.startsWith('/user/messages') ? 'primary' : 'text'" 
                class="nav-btn" 
                @click="router.push('/user/messages')"
              >{{ locale === 'zh' ? '消息' : 'Messages' }}</el-button>
            </el-badge>
            <el-button 
              :type="route.path.startsWith('/user/profile') ? 'primary' : 'text'" 
              class="nav-btn" 
              @click="router.push('/user/profile')"
            >{{ t('common.profile') }}</el-button>
            <el-button text type="danger" @click="logout">{{ t('common.logout') }}</el-button>
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
import { useUserStore, isAdmin, isSponsor } from '../store/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { onMounted, onUnmounted, watch, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'
import { getUnreadCount } from '../api/user'

const { t, locale } = useI18n()

const handleSetLanguage = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
}

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const isScrolled = ref(false)
const unreadCount = ref(0)

const fetchUnreadCount = async () => {
  if (!userStore.token) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (error) { console.error(error) }
}

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

  const token = localStorage.getItem('token')
  ws = new WebSocket(`ws://localhost:8080/ws/${userId}?token=${token}`)

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
    fetchUnreadCount()
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
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: all var(--transition-fast);
}

.app-header.scrolled {
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 calc(var(--spacing-unit) * 5);
  height: 64px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.logo {
  font-family: var(--font-heading);
  font-size: var(--text-xl);
  font-weight: 800;
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  letter-spacing: -0.02em;
  transition: opacity var(--transition-fast);
  outline: none;
}

.logo-icon {
  width: 28px;
  height: 28px;
  transition: transform var(--transition-fast);
}

.logo:hover .logo-icon {
  transform: scale(1.05);
}

.logo:hover {
  opacity: 0.85;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
}

.user-greeting {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--text-secondary);
  margin-right: var(--spacing-2);
  padding-right: var(--spacing-2);
  border-right: 1px solid var(--border-color);
}

.nav-btn {
  border-radius: var(--radius-pill);
  padding: 6px 14px;
  margin-right: 2px;
  transition: all var(--transition-fast);
  font-size: var(--text-xs);
}

.nav-btn.el-button--primary {
  padding: 6px 16px;
}

.app-main {
  flex: 1;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

@media (max-width: 900px) {
  .header-content {
    padding: 0 var(--spacing-3);
  }
  .nav-links .el-button {
    padding: 6px 10px;
  }
}

@media (max-width: 768px) {
  .app-header {
    height: auto;
  }
  .header-content {
    flex-direction: column;
    padding: var(--spacing-2) var(--spacing-3);
    gap: var(--spacing-2);
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

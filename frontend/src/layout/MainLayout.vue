<template>
  <div class="layout-container">
    <header class="app-header" :class="{ 'scrolled': isScrolled }">
      <div class="header-content">
        <div class="header-left">
          <div class="logo" @click="router.push('/home')" tabindex="0" @keydown.enter="router.push('/home')">
            <img src="/favicon.svg" alt="FundTogether Logo" class="logo-icon" />
            <span class="logo-text">FundTogether</span>
          </div>
        </div>

        <nav class="header-center" aria-label="Main Navigation">
          <el-button
            :type="route.path.startsWith('/projects') ? 'primary' : 'text'"
            class="nav-btn"
            @click="router.push('/projects')"
          >{{ t('common.projects') }}</el-button>

          <template v-if="userStore.token">
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
              :type="route.path.startsWith('/user/orders') ? 'primary' : 'text'"
              class="nav-btn"
              @click="router.push('/user/orders')"
            >{{ locale === 'zh' ? '我的支持' : 'My Support' }}</el-button>
            <el-button
              :type="route.path.startsWith('/user/favorites') ? 'primary' : 'text'"
              class="nav-btn"
              @click="router.push('/user/favorites')"
            >{{ locale === 'zh' ? '收藏' : 'Favorites' }}</el-button>
          </template>
        </nav>

        <div class="header-right">
          <el-dropdown @command="handleSetLanguage" class="tool-item">
            <button class="tool-btn" :title="t('common.language')" :aria-label="t('common.language')">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M2 12h20"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
              <span class="tool-btn-label">{{ locale === 'zh' ? '中' : 'EN' }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh" :disabled="locale === 'zh'">中文</el-dropdown-item>
                <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <a
            href="https://github.com/guoJiaQi-123/FundTogether"
            target="_blank"
            class="tool-btn github-link"
            title="GitHub"
            aria-label="GitHub"
          >
            <svg viewBox="0 0 1024 1024" width="20" height="20"><path d="M511.6 76.3C264.3 76.2 64 276.4 64 523.5 64 718.9 189.3 885 363.8 946c23.5 5.9 19.9-10.8 19.9-22.2v-77.5c-135.7 15.9-141.2-73.9-150.3-88.9C215 726 171.5 718 184.5 703c30.9-15.9 62.4 4 98.9 57.9 26.4 39.1 77.9 32.5 104 26 5.7-23.5 17.9-44.5 34.7-60.8-140.6-25.2-199.2-111-199.2-213 0-49.5 16.3-95 48.3-131.7-20.4-60.5 1.9-112.3 4.9-120 58.1-5.2 118.5 41.6 123.2 45.3 33-8.9 70.7-13.6 112.9-13.6 42.4 0 80.2 4.9 113.5 13.9 11.3-8.6 67.3-48.8 121.3-43.9 2.9 7.7 24.7 58.3 5.5 118 32.4 36.8 48.9 82.7 48.9 132.3 0 102.2-59 188.1-200 212.9 23.5 23.2 38.1 55.4 38.1 91v112.5c0.8 9 0 27.9 22.4 22.4C835 885 960 719 960 523.6 960 276.4 759.7 76.3 511.6 76.3z" fill="currentColor"></path></svg>
          </a>

          <template v-if="userStore.token">
            <el-popover
              placement="bottom-end"
              :width="380"
              trigger="click"
              popper-class="notif-popover"
              @show="handlePopoverShow"
            >
              <template #reference>
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
                  <button class="tool-btn" :aria-label="locale === 'zh' ? '消息通知' : 'Notifications'">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                      <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                    </svg>
                  </button>
                </el-badge>
              </template>

              <div class="notif-panel">
                <div class="notif-header">
                  <span class="notif-title">{{ locale === 'zh' ? '消息通知' : 'Notifications' }}</span>
                  <el-button
                    v-if="notifMessages.some(m => !m.isRead)"
                    text
                    type="primary"
                    size="small"
                    @click="markAllNotifRead"
                  >{{ locale === 'zh' ? '全部标为已读' : 'Mark all read' }}</el-button>
                </div>

                <div v-loading="notifLoading" class="notif-body">
                  <div v-if="!notifLoading && notifMessages.length === 0" class="notif-empty">
                    <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="var(--gray-300)" stroke-width="1.5">
                      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                      <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                    </svg>
                    <p>{{ locale === 'zh' ? '暂无消息' : 'No messages' }}</p>
                  </div>

                  <div v-else class="notif-list">
                    <div
                      v-for="msg in notifMessages"
                      :key="msg.id"
                      class="notif-item"
                      :class="{ 'notif-unread': !msg.isRead }"
                      @click="handleNotifClick(msg)"
                    >
                      <span class="notif-dot" v-if="!msg.isRead"></span>
                      <div class="notif-content-wrap">
                        <div class="notif-item-header">
                          <span class="notif-type-badge" :class="'notif-badge-type' + msg.type">{{ getNotifTypeName(msg.type) }}</span>
                          <span class="notif-time">{{ msg.createdAt }}</span>
                        </div>
                        <div class="notif-item-title">{{ msg.title }}</div>
                        <div class="notif-item-desc">{{ msg.content }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-popover>

            <el-dropdown class="tool-item user-menu" trigger="click">
              <button class="user-chip" :aria-label="locale === 'zh' ? '用户菜单' : 'User menu'">
                <el-avatar :size="28" :src="userStore.userInfo?.avatar || defaultAvatar" class="user-avatar" />
                <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.account }}</span>
                <el-icon class="user-chip-arrow"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/user/profile')">
                    <el-icon style="margin-right: 8px;"><User /></el-icon>
                    {{ t('common.profile') }}
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="logout">
                    <el-icon style="margin-right: 8px; color: var(--color-danger);"><SwitchButton /></el-icon>
                    <span style="color: var(--color-danger);">{{ t('common.logout') }}</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <template v-else>
            <el-button type="primary" class="login-btn" @click="router.push('/login')">{{ t('common.login') }}</el-button>
          </template>
        </div>
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
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { getUnreadCount, getMyMessages, markMessageRead } from '../api/user'
import defaultAvatar from '../assets/default-avatar.svg'

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

const notifMessages = ref<any[]>([])
const notifLoading = ref(false)

const getNotifTypeName = (type: number) => {
  const zh: Record<number, string> = { 1: '系统公告', 2: '项目动态', 3: '评论回复', 4: '订单通知' }
  const en: Record<number, string> = { 1: 'System', 2: 'Project', 3: 'Reply', 4: 'Order' }
  const map = locale.value === 'zh' ? zh : en
  return map[type] || (locale.value === 'zh' ? '其他' : 'Other')
}

const fetchUnreadCount = async () => {
  if (!userStore.token) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (error) { console.error(error) }
}

const fetchNotifMessages = async () => {
  if (!userStore.token) return
  notifLoading.value = true
  try {
    const res = await getMyMessages({ current: 1, size: 10 })
    notifMessages.value = res.data?.records || []
  } catch (error) {
    console.error(error)
  } finally {
    notifLoading.value = false
  }
}

const handlePopoverShow = () => {
  fetchNotifMessages()
}

const markAllNotifRead = async () => {
  try {
    const unread = notifMessages.value.filter(m => !m.isRead)
    for (const msg of unread) {
      await markMessageRead(msg.id)
    }
    await Promise.all([fetchNotifMessages(), fetchUnreadCount()])
  } catch (error) {
    console.error(error)
  }
}

const handleNotifClick = async (msg: any) => {
  if (!msg.isRead) {
    try {
      await markMessageRead(msg.id)
      msg.isRead = true
      fetchUnreadCount()
    } catch (error) { console.error(error) }
  }
  if (msg.relatedId && msg.type === 2) {
    router.push(`/projects/${msg.relatedId}`)
  }
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
        title: locale.value === 'zh' ? '新通知' : 'New notification',
        message: message,
        type: 'info',
        duration: 5000
      })
      fetchUnreadCount()
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
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: var(--spacing-4);
  padding: 0 calc(var(--spacing-unit) * 5);
  height: 64px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
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

.logo:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 4px;
  border-radius: 4px;
}

.nav-btn {
  border-radius: var(--radius-pill);
  padding: 8px 16px;
  transition: all var(--transition-fast);
  font-size: var(--text-sm);
  font-weight: 600;
  height: 36px;
  border: none;
}

.nav-btn.el-button--text:hover {
  background: var(--gray-100);
  color: var(--color-primary);
}

.nav-btn.el-button--primary {
  padding: 8px 18px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

/* 右侧工具按钮统一样式 */
.tool-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  padding: 0;
  text-decoration: none;
  gap: 4px;
}

.tool-btn:hover {
  background: var(--gray-100);
  color: var(--color-primary);
}

.tool-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* 带文字的工具按钮（语言切换） */
.tool-btn-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 让带文字的 tool-btn 变成胶囊形 */
.tool-item .tool-btn:has(.tool-btn-label) {
  width: auto;
  padding: 0 10px;
  border-radius: var(--radius-pill);
  gap: 4px;
}

.github-link {
  color: var(--text-primary);
}

.tool-item {
  display: inline-flex;
  outline: none;
}

/* 用户信息胶囊按钮 */
.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 0 10px 0 4px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--border-color);
  background: var(--bg-surface);
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: var(--text-sm);
  font-weight: 600;
  max-width: 180px;
}

.user-chip:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.user-chip:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.user-avatar {
  flex-shrink: 0;
  --el-avatar-bg-color: transparent;
}

.user-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100px;
}

.user-chip-arrow {
  font-size: 12px;
  color: var(--text-tertiary);
  transition: transform var(--transition-fast);
}

.user-menu:hover .user-chip-arrow {
  transform: rotate(180deg);
}

.login-btn {
  border-radius: var(--radius-pill);
  padding: 8px 20px;
  height: 36px;
  font-weight: 600;
  margin-left: 4px;
}

.app-main {
  flex: 1;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

/* 中等屏幕：收起 Logo 文字，保留图标 */
@media (max-width: 1100px) {
  .header-center {
    gap: 2px;
  }
  .nav-btn {
    padding: 8px 12px;
  }
}

@media (max-width: 960px) {
  .header-content {
    padding: 0 var(--spacing-3);
    gap: var(--spacing-2);
  }
  .logo-text {
    display: none;
  }
  .user-name {
    display: none;
  }
  .user-chip {
    padding: 0 6px;
  }
}

@media (max-width: 768px) {
  .app-header {
    height: auto;
  }
  .header-content {
    grid-template-columns: auto 1fr;
    grid-template-areas:
      "left right"
      "center center";
    row-gap: var(--spacing-2);
    padding: var(--spacing-2) var(--spacing-3);
    height: auto;
  }
  .header-left { grid-area: left; }
  .header-right { grid-area: right; justify-content: flex-end; }
  .header-center {
    grid-area: center;
    flex-wrap: wrap;
    justify-content: center;
    width: 100%;
    padding-top: var(--spacing-2);
    border-top: 1px solid var(--border-light);
  }
  .logo-text {
    display: inline;
  }
}

.notif-badge {
  margin: 0 2px;
}
.notif-badge :deep(.el-badge__content) {
  border: 2px solid var(--bg-surface);
}
</style>

<style>
.notif-popover.el-popover {
  padding: 0 !important;
  border-radius: var(--radius-lg);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}
.notif-panel {
  display: flex;
  flex-direction: column;
  max-height: 460px;
}
.notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-surface);
}
.notif-title {
  font-family: var(--font-heading);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.notif-body {
  flex: 1;
  overflow-y: auto;
  min-height: 140px;
}
.notif-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  color: var(--text-tertiary);
  gap: 8px;
}
.notif-empty p {
  margin: 0;
  font-size: var(--text-sm);
}
.notif-list {
  display: flex;
  flex-direction: column;
}
.notif-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--border-light);
}
.notif-item:last-child {
  border-bottom: none;
}
.notif-item:hover {
  background: var(--color-primary-light);
}
.notif-unread {
  background: hsl(225, 68%, 98%);
}
.notif-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  margin-top: 8px;
  flex-shrink: 0;
}
.notif-content-wrap {
  flex: 1;
  min-width: 0;
  margin-left: 2px;
}
.notif-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.notif-type-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 1px 8px;
  border-radius: 999px;
}
.notif-badge-type1 {
  background: hsl(38, 92%, 95%);
  color: var(--color-warning);
}
.notif-badge-type2 {
  background: hsl(160, 55%, 94%);
  color: var(--color-success);
}
.notif-badge-type3 {
  background: hsl(225, 68%, 95%);
  color: var(--color-primary);
}
.notif-badge-type4 {
  background: hsl(280, 35%, 94%);
  color: var(--secondary-2, var(--color-primary));
}
.notif-time {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-left: auto;
}
.notif-item-title {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.notif-item-desc {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

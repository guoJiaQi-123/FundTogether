<template>
  <div class="admin-dashboard" :class="{ 'is-collapsed': collapsed }">
    <el-container class="admin-container">
      <el-aside
        :width="asideWidth"
        class="admin-sidebar"
        :class="{ collapsed: collapsed }"
      >
        <div class="sidebar-header">
          <div class="brand">
            <img src="/favicon.svg" alt="FundTogether Logo" class="brand-logo" />
            <span v-if="!collapsed" class="brand-name">FundTogether</span>
          </div>
          <el-tooltip
            v-if="!isMobile"
            :content="collapsed ? '展开菜单' : '收起菜单'"
            placement="right"
          >
            <button class="collapse-btn" @click="toggleCollapse">
              <el-icon :size="16">
                <Fold v-if="!collapsed" />
                <Expand v-else />
              </el-icon>
            </button>
          </el-tooltip>
        </div>

        <el-scrollbar class="sidebar-scroll">
          <div class="menu-wrapper">
            <template v-for="group in menuGroups" :key="group.title">
              <div v-if="!collapsed" class="menu-group-title">
                {{ group.title }}
              </div>
              <div v-else class="menu-group-divider"></div>
              <el-menu
                :default-active="activeMenu"
                :collapse="collapsed"
                :collapse-transition="false"
                class="admin-menu"
                router
              >
                <el-menu-item
                  v-for="item in group.items"
                  :key="item.path"
                  :index="item.path"
                >
                  <el-icon>
                    <component :is="item.icon" />
                  </el-icon>
                  <template #title>
                    <span class="menu-item-label">{{ item.label }}</span>
                    <el-badge
                      v-if="getBadge(item.badgeKey) > 0"
                      :value="getBadge(item.badgeKey)"
                      :max="99"
                      class="menu-badge"
                    />
                  </template>
                </el-menu-item>
              </el-menu>
            </template>
          </div>
        </el-scrollbar>

        <div class="sidebar-footer">
          <div class="admin-card" :class="{ 'collapsed-card': collapsed }">
            <el-avatar
              :size="collapsed ? 32 : 40"
              :src="userStore.userInfo?.avatar || defaultAvatar"
              class="admin-avatar"
            />
            <div v-if="!collapsed" class="admin-info">
              <div class="admin-name">
                {{ userStore.userInfo?.nickname || userStore.userInfo?.account || '管理员' }}
              </div>
              <div class="admin-role-tag">
                <el-icon :size="10"><Star /></el-icon>
                <span>系统管理员</span>
              </div>
            </div>
            <el-tooltip v-if="collapsed" content="退出登录" placement="right">
              <button class="logout-btn icon-only" @click="handleLogout">
                <el-icon :size="14"><SwitchButton /></el-icon>
              </button>
            </el-tooltip>
            <button v-else class="logout-btn" @click="handleLogout">
              <el-icon :size="14"><SwitchButton /></el-icon>
            </button>
          </div>
        </div>
      </el-aside>

      <el-container class="admin-right">
        <el-header class="admin-header" height="56px">
          <div class="header-inner">
            <div class="header-left">
              <button
                v-if="isMobile"
                class="collapse-btn mobile"
                @click="toggleCollapse"
              >
                <el-icon :size="18">
                  <Fold v-if="!collapsed" />
                  <Expand v-else />
                </el-icon>
              </button>
              <el-breadcrumb :separator-icon="ArrowRight" class="admin-breadcrumb">
                <el-breadcrumb-item>管理后台</el-breadcrumb-item>
                <el-breadcrumb-item v-if="currentGroupTitle">
                  {{ currentGroupTitle }}
                </el-breadcrumb-item>
                <el-breadcrumb-item>
                  <span class="current-page">{{ currentPageTitle }}</span>
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="header-right">
              <el-tooltip content="返回前台" placement="bottom">
                <button class="header-icon-btn" @click="goHome">
                  <el-icon :size="16"><HomeFilled /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="刷新数据" placement="bottom">
                <button class="header-icon-btn" @click="refreshBadges">
                  <el-icon :size="16"><Refresh /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </div>
        </el-header>
        <el-main class="admin-main">
          <div class="admin-main-inner">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  DataAnalysis,
  User,
  Files,
  Bell,
  Money,
  Medal,
  Wallet,
  Warning,
  Star,
  TrendCharts,
  Fold,
  Expand,
  SwitchButton,
  Refresh,
  HomeFilled,
  ArrowRight
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getOverviewStats } from '../api/admin'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 40 40\'><rect width=\'40\' height=\'40\' fill=\'%234F46E5\'/><text x=\'50%25\' y=\'55%25\' font-size=\'16\' text-anchor=\'middle\' fill=\'white\' font-family=\'sans-serif\' font-weight=\'700\'>A</text></svg>'

interface MenuItem {
  path: string
  label: string
  icon: any
  badgeKey?: 'pendingProjects' | 'pendingWithdrawals' | 'pendingReports'
}

interface MenuGroup {
  title: string
  items: MenuItem[]
}

const menuGroups: MenuGroup[] = [
  {
    title: '数据概览',
    items: [
      { path: '/admin/dashboard', label: '数据大盘', icon: markRaw(DataAnalysis) }
    ]
  },
  {
    title: '内容管理',
    items: [
      { path: '/admin/users', label: '用户管理', icon: markRaw(User) },
      {
        path: '/admin/projects',
        label: '项目审核',
        icon: markRaw(Files),
        badgeKey: 'pendingProjects'
      },
      { path: '/admin/recommends', label: '推荐管理', icon: markRaw(Star) },
      { path: '/admin/notices', label: '系统公告', icon: markRaw(Bell) }
    ]
  },
  {
    title: '资金管理',
    items: [
      { path: '/admin/funding', label: '平台财务', icon: markRaw(Money) },
      {
        path: '/admin/withdrawals',
        label: '提现审批',
        icon: markRaw(Wallet),
        badgeKey: 'pendingWithdrawals'
      },
      { path: '/admin/user-levels', label: '用户等级', icon: markRaw(Medal) }
    ]
  },
  {
    title: '风控运营',
    items: [
      {
        path: '/admin/reports',
        label: '举报管理',
        icon: markRaw(Warning),
        badgeKey: 'pendingReports'
      },
      { path: '/admin/heat-rules', label: '热度管理', icon: markRaw(TrendCharts) }
    ]
  }
]

const activeMenu = computed(() => route.path)

const currentMenuItem = computed<MenuItem | null>(() => {
  for (const group of menuGroups) {
    const hit = group.items.find((it) => it.path === route.path)
    if (hit) return hit
  }
  return null
})

const currentGroupTitle = computed(() => {
  for (const group of menuGroups) {
    if (group.items.some((it) => it.path === route.path)) return group.title
  }
  return ''
})

const currentPageTitle = computed(
  () => currentMenuItem.value?.label || '管理后台'
)

/* ============ 响应式 & 折叠 ============ */
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value <= 768)
const collapsed = ref(windowWidth.value <= 1100)
const asideWidth = computed(() => {
  if (isMobile.value && !collapsed.value) return '240px'
  if (isMobile.value && collapsed.value) return '0'
  return collapsed.value ? '72px' : '240px'
})

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const handleResize = () => {
  windowWidth.value = window.innerWidth
  if (window.innerWidth <= 1100 && !collapsed.value) {
    collapsed.value = true
  }
}

/* ============ Badges ============ */
const badges = ref({
  pendingProjects: 0,
  pendingWithdrawals: 0,
  pendingReports: 0
})

const getBadge = (key?: MenuItem['badgeKey']) => {
  if (!key) return 0
  return badges.value[key] || 0
}

const fetchBadges = async () => {
  try {
    const [overviewRes, withdrawalRes, reportRes] = await Promise.allSettled([
      getOverviewStats(),
      request.get('/withdrawal/admin/list', {
        params: { status: 0, pageNum: 1, pageSize: 1 }
      }),
      request.get('/report/admin/list', {
        params: { status: 0, pageNum: 1, pageSize: 1 }
      })
    ])

    if (overviewRes.status === 'fulfilled') {
      const d: any = (overviewRes.value as any)?.data ?? overviewRes.value
      badges.value.pendingProjects = Number(d?.pendingProjects || 0)
    }
    if (withdrawalRes.status === 'fulfilled') {
      const d: any =
        (withdrawalRes.value as any)?.data ?? withdrawalRes.value
      badges.value.pendingWithdrawals = Number(d?.total ?? d?.totalCount ?? 0)
    }
    if (reportRes.status === 'fulfilled') {
      const d: any = (reportRes.value as any)?.data ?? reportRes.value
      badges.value.pendingReports = Number(d?.total ?? d?.totalCount ?? 0)
    }
  } catch (e) {
    /* 静默失败 */
  }
}

const refreshBadges = async () => {
  await fetchBadges()
  ElMessage.success('数据已刷新')
}

/* ============ 操作 ============ */
const goHome = () => {
  router.push('/home')
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    type: 'warning',
    confirmButtonText: '退出',
    cancelButtonText: '取消'
  })
    .then(() => {
      userStore.logout()
      router.push('/login')
    })
    .catch(() => {})
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchBadges()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.admin-dashboard {
  height: calc(100vh - 72px);
  width: 100%;
  display: flex;
  background-color: var(--bg-page);
}

.admin-container {
  height: 100%;
  width: 100%;
}

/* ============ Sidebar ============ */
.admin-sidebar {
  background-color: var(--bg-surface);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  transition: width 0.24s ease;
  overflow: hidden;
}

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
}

.brand-logo {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  object-fit: contain;
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.collapse-btn {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  background-color: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition-fast),
    color var(--transition-fast);
}

.collapse-btn:hover {
  background-color: var(--border-light);
  color: var(--text-primary);
}

.collapse-btn.mobile {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.sidebar-scroll {
  flex: 1;
  overflow: hidden;
}

.menu-wrapper {
  padding: 16px 0 24px;
}

.menu-group-title {
  padding: 16px 24px 8px;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.menu-group-divider {
  height: 1px;
  background-color: var(--border-light);
  margin: 8px 16px;
}

.admin-menu {
  border-right: none;
  background-color: transparent;
}

:deep(.el-menu-item) {
  margin: 2px 12px;
  border-radius: var(--radius-sm);
  height: 40px;
  line-height: 40px;
  color: var(--text-secondary);
  padding-left: 12px !important;
  padding-right: 12px !important;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-menu-item .el-icon) {
  font-size: 17px;
  margin-right: 10px;
  color: var(--text-tertiary);
  transition: color var(--transition-fast);
}

:deep(.el-menu-item.is-active) {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 600;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: var(--color-primary);
}

:deep(.el-menu-item:hover:not(.is-active)) {
  background-color: var(--border-light);
  color: var(--text-primary);
}

:deep(.el-menu-item:hover:not(.is-active) .el-icon) {
  color: var(--text-primary);
}

.menu-item-label {
  flex: 1;
}

.menu-badge :deep(.el-badge__content) {
  background-color: var(--color-danger);
  color: #fff;
  border: none;
  font-size: 11px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
  transform: translateY(-1px);
}

/* collapsed menu tweaks */
:deep(.el-menu--collapse) {
  width: 48px;
  border-right: none;
}

:deep(.el-menu--collapse .el-menu-item) {
  margin: 2px 12px;
  padding: 0 !important;
  justify-content: center;
}

:deep(.el-menu--collapse .el-menu-item .el-icon) {
  margin-right: 0;
}

/* ============ Sidebar Footer / Admin Card ============ */
.sidebar-footer {
  border-top: 1px solid var(--border-light);
  padding: 12px;
  flex-shrink: 0;
}

.admin-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius-md);
  background-color: var(--bg-page);
  border: 1px solid var(--border-light);
  transition: background-color var(--transition-fast);
}

.admin-card.collapsed-card {
  flex-direction: column;
  padding: 8px 4px;
  gap: 6px;
}

.admin-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.admin-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.admin-role-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 2px;
  font-size: 11px;
  font-weight: 500;
  color: var(--color-primary);
  background-color: var(--color-primary-light);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
}

.logout-btn {
  width: 28px;
  height: 28px;
  border: 1px solid var(--border-light);
  background-color: var(--bg-surface);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.logout-btn:hover {
  color: var(--color-danger);
  border-color: var(--color-danger);
  background-color: var(--bg-surface);
}

.logout-btn.icon-only {
  width: 28px;
  height: 28px;
}

/* ============ Right / Header ============ */
.admin-right {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background-color: var(--bg-page);
}

.admin-header {
  background-color: var(--bg-surface);
  border-bottom: 1px solid var(--border-light);
  padding: 0;
  flex-shrink: 0;
}

.header-inner {
  height: 100%;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.admin-breadcrumb :deep(.el-breadcrumb__inner) {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.admin-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 600;
}

.admin-breadcrumb :deep(.el-breadcrumb__separator) {
  color: var(--text-tertiary);
}

.current-page {
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-icon-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background-color: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.header-icon-btn:hover {
  background-color: var(--border-light);
  color: var(--text-primary);
}

/* ============ Main ============ */
.admin-main {
  padding: 0;
  background-color: var(--bg-page);
  overflow-y: auto;
}

.admin-main-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  width: 100%;
  box-sizing: border-box;
}

/* ============ Responsive ============ */
@media (max-width: 1100px) {
  .admin-main-inner {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    top: 72px;
    bottom: 0;
    left: 0;
    z-index: 100;
    box-shadow: var(--shadow-lg);
  }
  .admin-sidebar.collapsed {
    transform: translateX(-100%);
    width: 0 !important;
  }
  .header-inner {
    padding: 0 16px;
  }
  .admin-main-inner {
    padding: 12px;
  }
}
</style>

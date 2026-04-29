<template>
  <div class="admin-container">
    <main class="main-content">
      <!-- 页头 -->
      <header class="page-header">
        <div class="header-text">
          <h2 class="page-title">用户管理</h2>
          <p class="page-desc">管理平台所有用户的账号、角色与状态</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn" @click="exportCsv">
            <el-icon class="el-icon--left"><Download /></el-icon>
            导出 CSV
          </el-button>
        </div>
      </header>

      <!-- 统计卡片 -->
      <section class="stat-grid">
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-icon stat-icon--primary">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">总用户</div>
            <div class="stat-value">{{ stats.totalUsers }}</div>
          </div>
        </div>
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-icon stat-icon--success">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">正常用户</div>
            <div class="stat-value">{{ stats.activeUsers }}</div>
          </div>
        </div>
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-icon stat-icon--danger">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">禁用用户</div>
            <div class="stat-value">{{ stats.disabledUsers }}</div>
          </div>
        </div>
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-icon stat-icon--warning">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">近 30 天新增</div>
            <div class="stat-value">{{ stats.newUsers }}</div>
          </div>
        </div>
      </section>

      <!-- 筛选工具条 -->
      <section class="filter-bar">
        <div class="filter-row">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索账号 / 昵称 / 邮箱 / 手机"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filters.role" placeholder="全部角色" clearable class="filter-select">
            <el-option label="全部角色" :value="''" />
            <el-option label="普通用户" :value="1" />
            <el-option label="项目发起人" :value="2" />
            <el-option label="管理员" :value="4" />
          </el-select>
          <el-select v-model="filters.status" placeholder="全部状态" clearable class="filter-select">
            <el-option label="全部状态" :value="''" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <div class="filter-actions">
            <el-button class="action-btn" @click="resetFilters">
              <el-icon class="el-icon--left"><RefreshLeft /></el-icon>
              重置
            </el-button>
            <el-button type="primary" class="action-btn" @click="handleSearch">
              <el-icon class="el-icon--left"><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </section>

      <!-- 用户表格 -->
      <section class="table-section">
        <el-table
          :data="users"
          v-loading="loading"
          stripe
          class="responsive-table"
          :row-class-name="() => 'table-row'"
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="用户" min-width="220">
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar
                  v-if="row.avatar"
                  :src="row.avatar"
                  :size="40"
                  class="user-avatar"
                />
                <div v-else class="user-avatar user-avatar--fallback">
                  {{ getInitial(row.nickname || row.account) }}
                </div>
                <div class="user-info">
                  <div class="user-nickname">{{ row.nickname || '未设置昵称' }}</div>
                  <div class="user-account">@{{ row.account }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="联系方式" min-width="200">
            <template #default="{ row }">
              <div class="contact-cell">
                <div class="contact-line" :title="row.email">
                  <el-icon><Message /></el-icon>
                  <span>{{ row.email || '-' }}</span>
                </div>
                <div class="contact-line contact-line--sub">
                  <el-icon><Iphone /></el-icon>
                  <span>{{ row.phone || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="角色" min-width="180">
            <template #default="{ row }">
              <div class="role-tags">
                <el-tag
                  v-if="hasRole(row.role, UserRole.USER)"
                  size="small"
                  effect="plain"
                  class="role-tag"
                >用户</el-tag>
                <el-tag
                  v-if="hasRole(row.role, UserRole.SPONSOR)"
                  type="warning"
                  size="small"
                  effect="plain"
                  class="role-tag"
                >发起人</el-tag>
                <el-tag
                  v-if="hasRole(row.role, UserRole.ADMIN)"
                  type="danger"
                  size="small"
                  effect="plain"
                  class="role-tag"
                >管理员</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status === 1"
                inline-prompt
                active-text="正常"
                inactive-text="禁用"
                @change="(val: boolean | string | number) => toggleUserStatus(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="余额" width="120" align="right">
            <template #default="{ row }">
              <span class="balance-text">￥{{ formatNumber(row.balance) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="170">
            <template #default="{ row }">
              <span class="date-text">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最后登录" width="170">
            <template #default="{ row }">
              <span class="date-text">{{ formatDate(row.updatedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right" align="center">
            <template #default="{ row }">
              <div class="op-group">
                <el-tooltip content="修改角色" placement="top">
                  <el-button circle size="small" class="op-btn" @click="openRoleDialog(row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip :content="row.status === 1 ? '禁用' : '启用'" placement="top">
                  <el-button
                    circle
                    size="small"
                    class="op-btn"
                    :class="row.status === 1 ? 'op-btn--danger' : 'op-btn--success'"
                    @click="quickToggleStatus(row)"
                  >
                    <el-icon>
                      <Lock v-if="row.status === 1" />
                      <Unlock v-else />
                    </el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="重置密码" placement="top">
                  <el-button circle size="small" class="op-btn" @click="resetPassword(row)">
                    <el-icon><Key /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无用户数据" />
          </template>
        </el-table>

        <div class="pagination">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            :page-sizes="[10, 20, 50]"
            v-model:page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="fetchUsers"
            @size-change="handleSizeChange"
          />
        </div>
      </section>
    </main>

    <!-- 修改角色 Dialog -->
    <el-dialog
      v-model="roleDialogVisible"
      title="修改用户角色"
      width="560px"
      class="role-dialog"
    >
      <div class="role-dialog-user">
        <el-avatar
          v-if="currentUser.avatar"
          :src="currentUser.avatar"
          :size="44"
          class="user-avatar"
        />
        <div v-else class="user-avatar user-avatar--fallback role-dialog-avatar">
          {{ getInitial(currentUser.nickname || currentUser.account) }}
        </div>
        <div>
          <div class="role-dialog-name">{{ currentUser.nickname || currentUser.account }}</div>
          <div class="role-dialog-account">@{{ currentUser.account }}</div>
        </div>
      </div>

      <el-checkbox-group v-model="roleForm.roles" class="role-card-group">
        <label
          v-for="item in roleOptions"
          :key="item.value"
          class="role-card"
          :class="{ 'role-card--checked': roleForm.roles.includes(item.value) }"
        >
          <div class="role-card-main">
            <el-checkbox :value="item.value" class="role-card-check" />
            <div class="role-card-info">
              <div class="role-card-title">
                <el-icon class="role-card-icon"><component :is="item.icon" /></el-icon>
                {{ item.title }}
              </div>
              <div class="role-card-desc">{{ item.desc }}</div>
            </div>
          </div>
        </label>
      </el-checkbox-group>

      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoleChange" :loading="submitting">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Lock,
  Unlock,
  Key,
  Search,
  RefreshLeft,
  Download,
  UserFilled,
  CircleCheck,
  CircleClose,
  TrendCharts,
  Message,
  Iphone,
  User,
  Medal,
  Star
} from '@element-plus/icons-vue'
import { hasRole, UserRole } from '../../store/user'

interface UserItem {
  id: number
  account: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  role: number
  status: number
  balance?: number
  createdAt?: string
  updatedAt?: string
}

const users = ref<UserItem[]>([])
const loading = ref(false)
const statsLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const stats = reactive({
  totalUsers: 0,
  activeUsers: 0,
  disabledUsers: 0,
  newUsers: 0
})

const filters = reactive({
  keyword: '',
  role: '' as number | '',
  status: '' as number | ''
})

const roleDialogVisible = ref(false)
const submitting = ref(false)
const currentUser = ref<UserItem>({} as UserItem)
const roleForm = reactive({
  userId: 0,
  roles: [] as number[]
})

const roleOptions = [
  {
    value: 1,
    title: '普通用户',
    desc: '可以浏览项目、参与众筹、管理个人账户',
    icon: User
  },
  {
    value: 2,
    title: '项目发起人',
    desc: '可以发起众筹项目并管理项目信息',
    icon: Star
  },
  {
    value: 4,
    title: '管理员',
    desc: '拥有后台管理权限，可审核项目与管理用户',
    icon: Medal
  }
]

const maskFromRoles = (roles: number[]): number => {
  return roles.reduce((acc, r) => acc | r, 0)
}

const rolesFromMask = (mask: number): number[] => {
  const result: number[] = []
  if (mask & UserRole.USER) result.push(UserRole.USER)
  if (mask & UserRole.SPONSOR) result.push(UserRole.SPONSOR)
  if (mask & UserRole.ADMIN) result.push(UserRole.ADMIN)
  if (result.length === 0) result.push(UserRole.USER)
  return result
}

const getInitial = (name?: string): string => {
  if (!name) return 'U'
  const ch = name.trim().charAt(0)
  return ch ? ch.toUpperCase() : 'U'
}

const formatDate = (value?: string): string => {
  if (!value) return '-'
  const d = new Date(value)
  if (isNaN(d.getTime())) return value
  return d.toLocaleString('zh-CN', { hour12: false })
}

const formatNumber = (val?: number): string => {
  if (val === undefined || val === null) return '0.00'
  return Number(val).toFixed(2)
}

const fetchStats = async () => {
  statsLoading.value = true
  try {
    const [overviewRes, dailyRes] = await Promise.all([
      request.get('/admin/stats/overview').catch(() => null),
      request.get('/admin/stats/daily-users', { params: { days: 30 } }).catch(() => null)
    ])

    const overview: any = overviewRes?.data || {}
    stats.totalUsers = overview.totalUsers ?? 0
    stats.activeUsers = overview.activeUsers ?? 0
    stats.disabledUsers = overview.disabledUsers ?? Math.max(0, stats.totalUsers - stats.activeUsers)

    const daily: any[] = (dailyRes?.data as any[]) || []
    stats.newUsers = daily.reduce((sum, it) => sum + (it.count || it.value || 0), 0)
  } catch (e) {
    console.error(e)
  } finally {
    statsLoading.value = false
  }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.role !== '' && filters.role !== null) params.role = filters.role
    if (filters.status !== '' && filters.status !== null) params.status = filters.status

    const res: any = await request.get('/admin/users', { params })
    users.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

const resetFilters = () => {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  currentPage.value = 1
  fetchUsers()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchUsers()
}

const openRoleDialog = (row: UserItem) => {
  currentUser.value = row
  roleForm.userId = row.id
  roleForm.roles = rolesFromMask(row.role)
  roleDialogVisible.value = true
}

const submitRoleChange = async () => {
  submitting.value = true
  try {
    const roleMask = maskFromRoles(roleForm.roles)
    await request.post('/admin/user/role', { userId: roleForm.userId, role: roleMask })
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const toggleUserStatus = (row: UserItem, val: boolean | string | number) => {
  const targetStatus = val ? 1 : 0
  if (row.status === targetStatus) return
  const action = targetStatus === 0 ? '禁用' : '启用'
  if (targetStatus === 0) {
    ElMessageBox.prompt(`请输入禁用原因：`, `确认${action}用户`, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
      inputPlaceholder: '例如：违规发布内容'
    }).then(async ({ value }) => {
      await doStatusChange(row, targetStatus, value)
    }).catch(() => {
      // 用户取消，恢复状态
    })
  } else {
    ElMessageBox.confirm(`确认${action}该用户吗？`, '提示', { type: 'success' }).then(async () => {
      await doStatusChange(row, targetStatus)
    }).catch(() => {})
  }
}

const quickToggleStatus = (row: UserItem) => {
  const targetStatus = row.status === 1 ? 0 : 1
  toggleUserStatus(row, targetStatus === 1)
}

const doStatusChange = async (row: UserItem, status: number, reason?: string) => {
  try {
    await request.put(`/admin/users/${row.id}/status`, { status, reason })
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    fetchUsers()
  } catch (error: any) {
    ElMessage.warning('操作失败，请稍后重试')
    fetchUsers()
  }
}

const resetPassword = (row: UserItem) => {
  ElMessageBox.confirm(
    `确认为用户「${row.nickname || row.account}」重置密码吗？`,
    '重置密码',
    { type: 'warning' }
  ).then(async () => {
    try {
      await request.post(`/admin/users/${row.id}/reset-password`)
      ElMessage.success('密码已重置，新密码已发送至用户邮箱')
    } catch {
      ElMessage.info('重置密码接口暂未实现')
    }
  }).catch(() => {})
}

const exportCsv = () => {
  const headers = ['ID', '账号', '昵称', '邮箱', '手机', '角色', '状态', '余额', '注册时间']
  const roleText = (mask: number) => {
    const arr: string[] = []
    if (mask & UserRole.USER) arr.push('用户')
    if (mask & UserRole.SPONSOR) arr.push('发起人')
    if (mask & UserRole.ADMIN) arr.push('管理员')
    return arr.join('/')
  }
  const rows = users.value.map(u => [
    u.id,
    u.account,
    u.nickname || '',
    u.email || '',
    u.phone || '',
    roleText(u.role),
    u.status === 1 ? '正常' : '禁用',
    formatNumber(u.balance),
    formatDate(u.createdAt)
  ])
  const csv = [headers, ...rows]
    .map(r => r.map(v => `"${String(v).replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `users-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('CSV 已导出')
}

onMounted(() => {
  fetchUsers()
  fetchStats()
})
</script>

<style scoped>
.admin-container {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页头 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: -0.02em;
}

.page-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.action-btn {
  height: 36px;
  border-radius: var(--radius-md);
  font-weight: 500;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon--primary { background: var(--color-primary); }
.stat-icon--success { background: var(--color-success); }
.stat-icon--danger { background: var(--color-danger); }
.stat-icon--warning { background: var(--color-warning); }

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1.1;
}

/* 筛选工具条 */
.filter-bar {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filter-input {
  flex: 2 1 280px;
  min-width: 240px;
}

.filter-select {
  flex: 1 1 160px;
  min-width: 140px;
  max-width: 200px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

/* 表格 */
.table-section {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 8px 8px 16px;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.responsive-table {
  border-radius: var(--radius-md);
  overflow: auto;
}

:deep(.el-table) {
  --el-table-border-color: var(--border-light);
  --el-table-header-bg-color: hsl(220, 14%, 98%);
  --el-table-row-hover-bg-color: var(--color-primary-light);
  font-size: 13px;
}

:deep(.el-table th.el-table__cell) {
  background: hsl(220, 14%, 98%);
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-table .table-row td.el-table__cell) {
  padding: 10px 0;
}

/* 用户单元格 */
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
  --el-avatar-bg-color: transparent;
  border: 2px solid var(--color-primary-light);
  box-shadow: 0 0 0 1px var(--color-primary-light);
}

.user-avatar--fallback {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
}

.user-info {
  min-width: 0;
}

.user-nickname {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 13px;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-account {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* 联系方式 */
.contact-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-line--sub {
  color: var(--text-secondary);
}

.contact-line .el-icon {
  flex-shrink: 0;
}

/* 角色标签 */
.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-tag {
  border-radius: var(--radius-sm);
}

.balance-text {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: var(--text-primary);
}

.date-text {
  font-size: 12px;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

/* 操作按钮 */
.op-group {
  display: inline-flex;
  gap: 6px;
}

.op-btn {
  transition: transform var(--transition-fast);
}

.op-btn:hover {
  transform: translateY(-1px);
}

.op-btn--danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}

.op-btn--success {
  color: var(--color-success);
  border-color: var(--color-success);
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 0 8px;
}

/* 角色 Dialog */
.role-dialog-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: hsl(220, 14%, 98%);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}

.role-dialog-avatar {
  width: 44px;
  height: 44px;
  --el-avatar-bg-color: transparent;
}

.role-dialog-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.role-dialog-account {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.role-card-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.role-card {
  display: block;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
  cursor: pointer;
  background: var(--bg-surface);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.role-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.role-card--checked {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.role-card-main {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.role-card-check {
  margin-top: 2px;
}

.role-card-info {
  flex: 1;
}

.role-card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.role-card-icon {
  color: var(--color-primary);
}

.role-card-desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .filter-actions { margin-left: 0; }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .page-title { font-size: 20px; }
  .stat-grid { grid-template-columns: 1fr; }
  .filter-row { flex-direction: column; align-items: stretch; }
  .filter-input, .filter-select { max-width: none; }
  .filter-actions { width: 100%; justify-content: flex-end; }
}
</style>

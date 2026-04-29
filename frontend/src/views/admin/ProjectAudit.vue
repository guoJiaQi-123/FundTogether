<template>
  <div class="project-audit-container">
    <!-- 页头 -->
    <header class="page-header">
      <div class="page-header__text">
        <h1 class="page-header__title">项目审计管理</h1>
        <p class="page-header__subtitle">审核、监管与管理平台上全部众筹项目的生命周期</p>
      </div>
      <div class="page-header__actions">
        <el-button :icon="Refresh" @click="refreshAll" plain>刷新</el-button>
      </div>
    </header>

    <!-- 顶部统计卡片 -->
    <section class="stat-grid">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--danger">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">待审核项目</div>
          <div class="stat-card__value">
            <el-badge :value="overview.pendingProjects" :hidden="!overview.pendingProjects" :max="99" class="stat-badge">
              <span>{{ formatNumber(overview.pendingProjects) }}</span>
            </el-badge>
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--primary">
          <el-icon><Promotion /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">筹款中项目</div>
          <div class="stat-card__value">{{ formatNumber(overview.ongoingProjects) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">已成功项目</div>
          <div class="stat-card__value">{{ formatNumber(overview.successfulProjects) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--accent">
          <el-icon><Coin /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">平台总筹款金额</div>
          <div class="stat-card__value">￥{{ formatNumber(overview.totalAmount) }}</div>
        </div>
      </div>
    </section>

    <!-- 主卡片 -->
    <el-card class="audit-card" shadow="never">
      <el-tabs v-model="activeTab" class="audit-tabs" @tab-click="handleTabClick">
        <!-- 待审核项目 Tab -->
        <el-tab-pane name="pending">
          <template #label>
            <span class="tab-label">
              待审核项目
              <el-badge v-if="overview.pendingProjects" :value="overview.pendingProjects" :max="99" class="tab-badge" />
            </span>
          </template>

          <!-- 搜索工具条 -->
          <div class="toolbar">
            <el-input
              v-model="pendingFilters.keyword"
              placeholder="搜索项目关键词"
              :prefix-icon="Search"
              clearable
              class="toolbar__input"
              @keyup.enter="fetchPendingProjects"
            />
            <el-input
              v-model="pendingFilters.sponsor"
              placeholder="发起人"
              :prefix-icon="User"
              clearable
              class="toolbar__input"
              @keyup.enter="fetchPendingProjects"
            />
            <el-date-picker
              v-model="pendingFilters.range"
              type="daterange"
              range-separator="~"
              start-placeholder="提交起始"
              end-placeholder="提交结束"
              value-format="YYYY-MM-DD"
              class="toolbar__range"
            />
            <el-button type="primary" :icon="Search" @click="fetchPendingProjects">查询</el-button>
            <el-button :icon="RefreshRight" @click="resetPendingFilters">重置</el-button>
          </div>

          <el-skeleton v-if="pendingLoading && pendingProjects.length === 0" :rows="6" animated />
          <el-table
            v-else
            :data="filteredPendingProjects"
            style="width: 100%"
            v-loading="pendingLoading"
            stripe
            size="default"
            class="data-table"
          >
            <el-table-column prop="id" label="ID" width="72" align="center" />
            <el-table-column label="封面" width="88" align="center">
              <template #default="{ row }">
                <el-image
                  :src="row.coverImage || placeholderImage"
                  fit="cover"
                  class="cover-thumb"
                  :preview-src-list="row.coverImage ? [row.coverImage] : []"
                  hide-on-click-modal
                />
              </template>
            </el-table-column>
            <el-table-column label="项目" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="project-cell">
                  <span class="project-cell__title">{{ row.title }}</span>
                  <el-tag v-if="row.categoryName" size="small" effect="plain" type="info">
                    {{ row.categoryName }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="发起人" width="180">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="28" :src="getAvatarSrc(row.sponsorAvatar)" />
                  <span class="user-cell__name">{{ row.sponsorName || `用户 ${row.sponsorId}` }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="目标金额" width="130" align="right">
              <template #default="{ row }">
                <span class="amount-text">￥{{ formatNumber(row.targetAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="时长" width="100" align="center">
              <template #default="{ row }">
                <span class="muted">{{ calcDuration(row.startTime, row.endTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="170">
              <template #default="{ row }">
                <span class="muted">{{ row.createdAt || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <div class="op-actions">
                  <el-button size="small" :icon="View" @click="previewProject(row.id)">预览</el-button>
                  <el-button size="small" type="success" :icon="Check" @click="handleApprove(row.id)">通过</el-button>
                  <el-button size="small" type="danger" :icon="Close" @click="openRejectDialog(row.id)">驳回</el-button>
                </div>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无待审核项目" />
            </template>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pendingCurrentPage"
              v-model:page-size="pendingPageSize"
              :total="pendingTotal"
              @current-change="fetchPendingProjects"
              layout="total, prev, pager, next, jumper"
              background
            />
          </div>
        </el-tab-pane>

        <!-- 全量项目 Tab -->
        <el-tab-pane label="全量项目" name="all">
          <!-- 状态筛选 chip -->
          <div class="chip-group">
            <el-check-tag
              v-for="item in statusChips"
              :key="item.value"
              :checked="allFilters.status === item.value"
              class="chip"
              @change="applyStatusFilter(item.value)"
            >
              {{ item.label }}
            </el-check-tag>
          </div>

          <!-- 搜索工具条 -->
          <div class="toolbar">
            <el-input
              v-model="allFilters.keyword"
              placeholder="搜索项目关键词"
              :prefix-icon="Search"
              clearable
              class="toolbar__input"
              @keyup.enter="fetchAllProjects"
            />
            <el-input
              v-model="allFilters.sponsor"
              placeholder="发起人"
              :prefix-icon="User"
              clearable
              class="toolbar__input"
              @keyup.enter="fetchAllProjects"
            />
            <el-date-picker
              v-model="allFilters.range"
              type="daterange"
              range-separator="~"
              start-placeholder="起始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              class="toolbar__range"
            />
            <el-button type="primary" :icon="Search" @click="fetchAllProjects">查询</el-button>
            <el-button :icon="RefreshRight" @click="resetAllFilters">重置</el-button>
          </div>

          <el-skeleton v-if="allLoading && allProjects.length === 0" :rows="6" animated />
          <el-table
            v-else
            :data="filteredAllProjects"
            style="width: 100%"
            v-loading="allLoading"
            stripe
            class="data-table"
          >
            <el-table-column label="封面" width="88" align="center">
              <template #default="{ row }">
                <el-image
                  :src="row.coverImage || placeholderImage"
                  fit="cover"
                  class="cover-thumb"
                  :preview-src-list="row.coverImage ? [row.coverImage] : []"
                  hide-on-click-modal
                />
              </template>
            </el-table-column>
            <el-table-column label="项目" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="project-cell">
                  <el-link
                    type="primary"
                    :underline="false"
                    class="project-cell__title project-cell__title--link"
                    @click="router.push(`/projects/${row.id}`)"
                  >
                    {{ row.title }}
                  </el-link>
                  <el-tag v-if="row.categoryName" size="small" effect="plain" type="info">
                    {{ row.categoryName }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="发起人" width="170">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="28" :src="getAvatarSrc(row.sponsorAvatar)" />
                  <span class="user-cell__name">{{ row.sponsorName || `用户 ${row.sponsorId}` }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-bind="getStatusTagProps(row)">{{ getStatusLabel(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="已筹 / 目标" min-width="200">
              <template #default="{ row }">
                <div class="progress-cell">
                  <div class="progress-cell__text">
                    <span class="amount-text">￥{{ formatNumber(row.currentAmount) }}</span>
                    <span class="muted"> / ￥{{ formatNumber(row.targetAmount) }}</span>
                  </div>
                  <el-progress
                    :percentage="getProgressPercent(row)"
                    :color="getProgressColor(row)"
                    :stroke-width="6"
                    :show-text="false"
                  />
                  <span class="progress-cell__pct">{{ getProgressPercent(row) }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="支持者" width="100" align="center">
              <template #default="{ row }">
                <span>{{ formatNumber(row.supporterCount || 0) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="热度" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" effect="plain" type="warning">{{ formatNumber(row.heat || 0) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="筹款时间" width="180">
              <template #default="{ row }">
                <span class="muted">
                  {{ row.startTime ? row.startTime.substring(0, 10) : '-' }} ~
                  {{ row.endTime ? row.endTime.substring(0, 10) : '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{ row }">
                <div class="op-actions">
                  <el-button size="small" :icon="UserFilled" @click="openSupportersDialog(row.id)">支持者</el-button>
                  <el-tooltip content="编辑项目简介/详情/视频链接" placement="top">
                    <el-button size="small" type="warning" :icon="Edit" @click="openEditContentDialog(row)">编辑</el-button>
                  </el-tooltip>
                  <el-button
                    v-if="row.status === 1"
                    size="small"
                    type="danger"
                    :icon="Delete"
                    @click="openTakedownDialog(row.id)"
                  >
                    下架
                  </el-button>
                </div>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无项目" />
            </template>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="allCurrentPage"
              v-model:page-size="allPageSize"
              :total="allTotal"
              @current-change="fetchAllProjects"
              layout="total, prev, pager, next, jumper"
              background
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showRejectDialog" title="填写驳回原因" width="520px" align-center>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入驳回原因，向发起人说明本次驳回的理由..."
        maxlength="300"
        show-word-limit
      />
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 下架弹窗 -->
    <el-dialog v-model="showTakedownDialog" title="填写下架原因" width="520px" align-center>
      <el-input
        v-model="takedownReason"
        type="textarea"
        :rows="4"
        placeholder="请输入下架原因..."
        maxlength="300"
        show-word-limit
      />
      <template #footer>
        <el-button @click="showTakedownDialog = false">取消</el-button>
        <el-button type="danger" @click="handleTakedown">确认下架</el-button>
      </template>
    </el-dialog>

    <!-- 支持者列表弹窗 -->
    <el-dialog v-model="supportersDialogVisible" title="项目支持者列表" width="880px" align-center>
      <div class="supporter-summary">
        <div class="supporter-summary__item">
          <div class="supporter-summary__label">总支持人数</div>
          <div class="supporter-summary__value">{{ formatNumber(supporterStats.count) }}</div>
        </div>
        <div class="supporter-summary__item">
          <div class="supporter-summary__label">总支持金额</div>
          <div class="supporter-summary__value supporter-summary__value--accent">
            ￥{{ formatNumber(supporterStats.total) }}
          </div>
        </div>
        <div class="supporter-summary__item">
          <div class="supporter-summary__label">平均支持金额</div>
          <div class="supporter-summary__value">￥{{ formatNumber(supporterStats.avg) }}</div>
        </div>
        <div class="supporter-summary__spacer" />
        <el-button type="primary" :icon="Download" @click="exportSupporters" :loading="exporting">
          导出 Excel
        </el-button>
      </div>
      <el-table :data="supporters" style="width: 100%" v-loading="loadingSupporters" class="data-table" stripe>
        <el-table-column label="用户" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="30" :src="getAvatarSrc(row?.avatar)" />
              <el-link type="primary" :underline="false" @click="openUserPreview(row.userId)">
                {{ row.nickname || `用户 ${row.userId}` }}
              </el-link>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="支持金额" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-text amount-text--accent">￥{{ formatNumber(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="留言" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ muted: !row.message }">{{ row.message || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支持时间" width="180" />
        <el-table-column label="发货状态" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getShippingTagType(row.shippingStatus)">
              {{ getShippingLabel(row.shippingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无支持者" />
        </template>
      </el-table>
    </el-dialog>

    <!-- 管理员编辑项目内容 -->
    <el-dialog v-model="editContentDialogVisible" title="编辑项目内容" width="820px" align-center>
      <el-form ref="editContentFormRef" :model="editContentForm" :rules="editContentRules" label-width="100px">
        <el-form-item label="项目标题">
          <el-input v-model="editContentForm.title" disabled />
        </el-form-item>
        <el-form-item label="项目简介" prop="summary">
          <el-input v-model="editContentForm.summary" type="textarea" :rows="2" placeholder="请输入一句话简介" />
        </el-form-item>
        <el-form-item label="视频链接">
          <el-input v-model="editContentForm.videoUrl" placeholder="请输入视频链接URL (可选)" />
        </el-form-item>
        <el-form-item label="项目详情" prop="content">
          <el-input v-model="editContentForm.content" type="textarea" :rows="8" placeholder="支持HTML格式" />
        </el-form-item>
        <el-form-item label="筹款时间">
          <div class="form-date-range">
            <el-date-picker
              v-model="editContentForm.startTime"
              type="datetime"
              placeholder="开始时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="flex:1;"
            />
            <el-date-picker
              v-model="editContentForm.endTime"
              type="datetime"
              placeholder="截止时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="flex:1;"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editContentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditContent" :loading="savingContent">保存</el-button>
      </template>
    </el-dialog>

    <UserProfilePreviewDialog v-model="userPreviewVisible" :user-id="userPreviewUserId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  UserFilled,
  Search,
  Refresh,
  RefreshRight,
  View,
  Check,
  Close,
  Edit,
  Delete,
  Warning,
  Promotion,
  CircleCheck,
  Coin,
  Download
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getPendingProjects,
  approveProject,
  rejectProject,
  takedownProject,
  updateProjectContentByAdmin,
  getOverviewStats
} from '../../api/admin'
import request from '../../utils/request'
import defaultAvatar from '../../assets/default-avatar.svg'
import UserProfilePreviewDialog from '../../components/UserProfilePreviewDialog.vue'

const router = useRouter()
const activeTab = ref('pending')

const placeholderImage =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 48 48"><rect width="48" height="48" fill="%23F2F3F5"/><path d="M16 30l6-8 4 5 3-4 7 9H16z" fill="%23C9CDD4"/><circle cx="18" cy="18" r="3" fill="%23C9CDD4"/></svg>'

const formatNumber = (value: unknown): string => {
  const num = Number(value ?? 0)
  if (!Number.isFinite(num)) return '0'
  return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

// ===== 顶部概览 =====
const overview = ref({
  pendingProjects: 0,
  ongoingProjects: 0,
  successfulProjects: 0,
  totalAmount: 0
})

const fetchOverview = async () => {
  try {
    const res: any = await getOverviewStats()
    const d = res?.data || {}
    overview.value = {
      pendingProjects: Number(d.pendingProjects ?? d.pending ?? 0),
      ongoingProjects: Number(d.ongoingProjects ?? d.ongoing ?? 0),
      successfulProjects: Number(d.successfulProjects ?? d.successful ?? 0),
      totalAmount: Number(d.totalAmount ?? d.totalFunding ?? 0)
    }
  } catch {
    // 静默
  }
}

// ===== 待审核项目 =====
const pendingLoading = ref(false)
const pendingProjects = ref<any[]>([])
const pendingCurrentPage = ref(1)
const pendingPageSize = ref(10)
const pendingTotal = ref(0)

const pendingFilters = reactive({
  keyword: '',
  sponsor: '',
  range: [] as string[] | null
})

const resetPendingFilters = () => {
  pendingFilters.keyword = ''
  pendingFilters.sponsor = ''
  pendingFilters.range = []
  fetchPendingProjects()
}

const showRejectDialog = ref(false)
const rejectReason = ref('')
const currentRejectId = ref<number | null>(null)

// ===== 全量项目 =====
const allLoading = ref(false)
const allProjects = ref<any[]>([])
const allCurrentPage = ref(1)
const allPageSize = ref(10)
const allTotal = ref(0)

const statusChips = [
  { label: '全部', value: -1 },
  { label: '待审核', value: 0 },
  { label: '筹款中', value: 1 },
  { label: '已达标', value: 100 }, // 伪状态：status=1 且已达标
  { label: '已驳回', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已下架', value: 4 },
  { label: '筹款成功', value: 5 },
  { label: '筹款失败', value: 6 }
]

const allFilters = reactive({
  keyword: '',
  sponsor: '',
  range: [] as string[] | null,
  status: -1 as number
})

const applyStatusFilter = (value: number) => {
  allFilters.status = value
}

const resetAllFilters = () => {
  allFilters.keyword = ''
  allFilters.sponsor = ''
  allFilters.range = []
  allFilters.status = -1
  fetchAllProjects()
}

const showTakedownDialog = ref(false)
const takedownReason = ref('')
const currentTakedownId = ref<number | null>(null)

// ===== 支持者弹窗 =====
const supportersDialogVisible = ref(false)
const supporters = ref<any[]>([])
const loadingSupporters = ref(false)
const exporting = ref(false)

const supporterStats = computed(() => {
  const list = supporters.value
  const count = list.length
  const total = list.reduce((acc, s) => acc + Number(s.amount || 0), 0)
  const avg = count > 0 ? total / count : 0
  return { count, total, avg }
})

const getAvatarSrc = (avatar?: string) => avatar || defaultAvatar

const userPreviewVisible = ref(false)
const userPreviewUserId = ref<number | null>(null)
const openUserPreview = (userId: number) => {
  userPreviewUserId.value = userId
  userPreviewVisible.value = true
}

const escapeCsvCell = (value: unknown) => {
  const normalized = String(value ?? '').replace(/"/g, '""')
  return `"${normalized}"`
}

const exportSupporters = () => {
  if (supporters.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  exporting.value = true
  const headers = ['用户名称', '支持金额', '留言', '支持时间', '发货状态']
  const rows = supporters.value.map((s) => [
    s.nickname || `用户 ${s.userId}`,
    s.amount,
    s.message || '',
    s.payTime || '',
    getShippingLabel(s.shippingStatus)
  ])

  const csvContent = [
    headers.map(escapeCsvCell).join(','),
    ...rows.map((row) => row.map(escapeCsvCell).join(','))
  ].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `项目支持者列表_${Date.now()}.csv`
  link.click()
  exporting.value = false
}

const openSupportersDialog = async (projectId: number) => {
  supportersDialogVisible.value = true
  loadingSupporters.value = true
  try {
    const res: any = await request.get(`/admin/projects/${projectId}/supporters`)
    supporters.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('获取支持者列表失败')
  } finally {
    loadingSupporters.value = false
  }
}

const getShippingLabel = (status: any): string => {
  switch (Number(status)) {
    case 1:
      return '已发货'
    case 2:
      return '已签收'
    case 3:
      return '无需发货'
    default:
      return '未发货'
  }
}

const getShippingTagType = (status: any): 'success' | 'primary' | 'info' | 'warning' => {
  switch (Number(status)) {
    case 1:
      return 'primary'
    case 2:
      return 'success'
    case 3:
      return 'info'
    default:
      return 'warning'
  }
}

// ===== 编辑项目内容 =====
const editContentDialogVisible = ref(false)
const savingContent = ref(false)
const editContentFormRef = ref<FormInstance>()
const editContentForm = ref<any>({
  id: undefined,
  title: '',
  summary: '',
  videoUrl: '',
  content: '',
  startTime: '',
  endTime: ''
})

const editContentRules = reactive<FormRules>({
  summary: [{ required: true, message: '请输入项目简介', trigger: 'blur' }],
  content: [{ required: true, message: '请输入项目详情', trigger: 'blur' }]
})

const openEditContentDialog = (row: any) => {
  editContentForm.value = {
    id: row.id,
    title: row.title || '',
    summary: row.summary || '',
    videoUrl: row.videoUrl || '',
    content: row.content || '',
    startTime: row.startTime || '',
    endTime: row.endTime || ''
  }
  editContentDialogVisible.value = true
  if (editContentFormRef.value) editContentFormRef.value.clearValidate()
}

const submitEditContent = async () => {
  if (!editContentFormRef.value) return
  await editContentFormRef.value.validate(async (valid) => {
    if (!valid) return
    savingContent.value = true
    try {
      await updateProjectContentByAdmin({
        id: editContentForm.value.id,
        summary: editContentForm.value.summary,
        videoUrl: editContentForm.value.videoUrl,
        content: editContentForm.value.content,
        startTime: editContentForm.value.startTime,
        endTime: editContentForm.value.endTime
      })
      ElMessage.success('项目内容已更新')
      editContentDialogVisible.value = false
      fetchAllProjects()
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
    } finally {
      savingContent.value = false
    }
  })
}

// ===== 数据获取 =====
const fetchPendingProjects = async () => {
  pendingLoading.value = true
  try {
    const res: any = await getPendingProjects({
      current: pendingCurrentPage.value,
      size: pendingPageSize.value
    })
    pendingProjects.value = res.data.records
    pendingTotal.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    pendingLoading.value = false
  }
}

const fetchAllProjects = async () => {
  allLoading.value = true
  try {
    const res: any = await request.get('/admin/projects/all', {
      params: { current: allCurrentPage.value, size: allPageSize.value }
    })
    allProjects.value = res.data.records
    allTotal.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    allLoading.value = false
  }
}

// 前端过滤（后端不支持时的兜底）
const inRange = (dateStr: string, range: string[] | null | undefined) => {
  if (!range || range.length !== 2) return true
  if (!dateStr) return false
  const t = new Date(dateStr).getTime()
  const start = new Date(range[0]).getTime()
  const end = new Date(range[1]).getTime() + 24 * 3600 * 1000 - 1
  return t >= start && t <= end
}

const filteredPendingProjects = computed(() => {
  return pendingProjects.value.filter((row) => {
    if (
      pendingFilters.keyword &&
      !String(row.title || '').toLowerCase().includes(pendingFilters.keyword.toLowerCase())
    ) {
      return false
    }
    if (
      pendingFilters.sponsor &&
      !String(row.sponsorName || '').toLowerCase().includes(pendingFilters.sponsor.toLowerCase())
    ) {
      return false
    }
    if (!inRange(row.createdAt, pendingFilters.range)) return false
    return true
  })
})

const filteredAllProjects = computed(() => {
  return allProjects.value.filter((row) => {
    if (
      allFilters.keyword &&
      !String(row.title || '').toLowerCase().includes(allFilters.keyword.toLowerCase())
    ) {
      return false
    }
    if (
      allFilters.sponsor &&
      !String(row.sponsorName || '').toLowerCase().includes(allFilters.sponsor.toLowerCase())
    ) {
      return false
    }
    if (!inRange(row.createdAt, allFilters.range)) return false
    if (allFilters.status !== -1) {
      if (allFilters.status === 100) {
        // 已达标
        if (!(Number(row.status) === 1 && Number(row.currentAmount) >= Number(row.targetAmount))) {
          return false
        }
      } else if (Number(row.status) !== allFilters.status) {
        return false
      }
    }
    return true
  })
})

onMounted(() => {
  fetchOverview()
  fetchPendingProjects()
})

const refreshAll = () => {
  fetchOverview()
  if (activeTab.value === 'pending') fetchPendingProjects()
  else fetchAllProjects()
}

const handleTabClick = (tab: any) => {
  if (tab.paneName === 'pending' && pendingProjects.value.length === 0) {
    fetchPendingProjects()
  } else if (tab.paneName === 'all' && allProjects.value.length === 0) {
    fetchAllProjects()
  }
}

// ===== 操作 =====
const previewProject = (id: number) => {
  router.push(`/projects/${id}`)
}

const handleApprove = (id: number) => {
  ElMessageBox.confirm('确认通过该项目的审核并上线吗？', '提示', {
    type: 'warning'
  })
    .then(async () => {
      try {
        await approveProject(id)
        ElMessage.success('审核通过，项目已上线')
        fetchPendingProjects()
        fetchOverview()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    })
    .catch(() => {})
}

const openRejectDialog = (id: number) => {
  currentRejectId.value = id
  rejectReason.value = ''
  showRejectDialog.value = true
}

const handleReject = async () => {
  if (!rejectReason.value) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  if (!currentRejectId.value) return

  try {
    await rejectProject(currentRejectId.value, rejectReason.value)
    ElMessage.success('项目已驳回')
    showRejectDialog.value = false
    fetchPendingProjects()
    fetchOverview()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const openTakedownDialog = (id: number) => {
  currentTakedownId.value = id
  takedownReason.value = ''
  showTakedownDialog.value = true
}

const handleTakedown = async () => {
  if (!takedownReason.value) {
    ElMessage.warning('请输入下架原因')
    return
  }
  if (!currentTakedownId.value) return

  try {
    await takedownProject(currentTakedownId.value, takedownReason.value)
    ElMessage.success('项目已下架')
    showTakedownDialog.value = false
    fetchAllProjects()
    fetchOverview()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// ===== 展示辅助 =====
const getProgressPercent = (row: any): number => {
  const target = Number(row.targetAmount || 0)
  const current = Number(row.currentAmount || 0)
  if (target <= 0) return 0
  return Math.min(999, Math.round((current / target) * 100))
}

const getProgressColor = (row: any): string => {
  const pct = getProgressPercent(row)
  if (pct >= 100) return 'hsl(160, 55%, 42%)'
  if (pct >= 50) return 'hsl(212, 85%, 55%)'
  return 'hsl(38, 92%, 50%)'
}

const getStatusLabel = (row: any): string => {
  const s = Number(row.status)
  if (s === 0) return '待审核'
  if (s === 1) {
    return Number(row.currentAmount) >= Number(row.targetAmount) ? '已达标' : '筹款中'
  }
  if (s === 2) return '已驳回'
  if (s === 3) return '已取消'
  if (s === 4) return '已下架'
  if (s === 5) return '筹款成功'
  if (s === 6) return '筹款失败'
  return '未知'
}

const getStatusTagProps = (
  row: any
): { type: 'primary' | 'success' | 'warning' | 'info' | 'danger'; effect: 'light' | 'plain' | 'dark' } => {
  const s = Number(row.status)
  if (s === 0) return { type: 'warning', effect: 'light' }
  if (s === 1) {
    return Number(row.currentAmount) >= Number(row.targetAmount)
      ? { type: 'success', effect: 'light' }
      : { type: 'primary', effect: 'light' }
  }
  if (s === 2) return { type: 'danger', effect: 'light' }
  if (s === 3 || s === 4) return { type: 'info', effect: 'light' }
  if (s === 5) return { type: 'success', effect: 'dark' }
  if (s === 6) return { type: 'danger', effect: 'light' }
  return { type: 'info', effect: 'light' }
}

const calcDuration = (start: string | undefined, end: string | undefined): string => {
  if (!start || !end) return '-'
  const ms = new Date(end).getTime() - new Date(start).getTime()
  if (!Number.isFinite(ms) || ms <= 0) return '-'
  const days = Math.ceil(ms / (24 * 3600 * 1000))
  return `${days} 天`
}
</script>

<style scoped>
.project-audit-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 页头 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}
.page-header__title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: 0.2px;
}
.page-header__subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-card__icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}
.stat-card__icon--primary {
  background: hsl(212, 85%, 95%);
  color: hsl(212, 85%, 45%);
}
.stat-card__icon--success {
  background: hsl(160, 55%, 94%);
  color: hsl(160, 55%, 36%);
}
.stat-card__icon--accent {
  background: hsl(271, 76%, 95%);
  color: hsl(271, 76%, 53%);
}
.stat-card__icon--danger {
  background: hsl(0, 72%, 95%);
  color: hsl(0, 72%, 51%);
}
.stat-card__label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.stat-card__value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
}
.stat-badge :deep(.el-badge__content) {
  background-color: hsl(0, 72%, 51%);
}

/* 主卡片 */
.audit-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.audit-card :deep(.el-card__body) {
  padding: 8px 24px 24px;
}
.audit-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.tab-badge :deep(.el-badge__content) {
  background-color: hsl(0, 72%, 51%);
}

/* 状态 chip */
.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: hsl(220, 14%, 98%);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}
.chip {
  cursor: pointer;
  user-select: none;
}

/* 搜索工具条 */
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.toolbar__input {
  width: 220px;
}
.toolbar__range {
  width: 320px;
}

/* 数据表 */
.data-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}
.data-table :deep(.el-table__header) th {
  background: hsl(220, 14%, 97%);
  color: var(--text-secondary);
  font-weight: 600;
}

/* 单元格 */
.cover-thumb {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}
.project-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.project-cell__title {
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
}
.project-cell__title--link {
  justify-content: flex-start;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-cell__name {
  font-size: 13px;
  color: var(--text-primary);
}
.amount-text {
  color: var(--text-primary);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.amount-text--accent {
  color: hsl(0, 72%, 51%);
}
.muted {
  color: var(--text-tertiary);
  font-size: 12px;
}
.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.progress-cell__text {
  font-size: 13px;
}
.progress-cell__pct {
  font-size: 12px;
  color: var(--text-secondary);
  align-self: flex-end;
  font-variant-numeric: tabular-nums;
}
.op-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

/* 分页 */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 支持者摘要 */
.supporter-summary {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 20px;
  margin-bottom: 16px;
  background: hsl(220, 14%, 98%);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}
.supporter-summary__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.supporter-summary__label {
  font-size: 12px;
  color: var(--text-secondary);
}
.supporter-summary__value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}
.supporter-summary__value--accent {
  color: hsl(0, 72%, 51%);
}
.supporter-summary__spacer {
  flex: 1;
}

.form-date-range {
  display: flex;
  gap: 12px;
  width: 100%;
}

/* 响应式 */
@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .toolbar__input,
  .toolbar__range {
    width: 100%;
  }
}
@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .page-header {
    align-items: flex-start;
  }
  .audit-card :deep(.el-card__body) {
    padding: 8px 12px 16px;
  }
  .pagination-container {
    justify-content: center;
  }
  .supporter-summary {
    flex-wrap: wrap;
  }
}
</style>

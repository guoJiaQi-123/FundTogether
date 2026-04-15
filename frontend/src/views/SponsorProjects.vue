<template>
  <div class="sponsor-container">
    <main class="main-content">
      <div class="page-header">
        <h2>我的项目</h2>
        <el-button type="primary" @click="openCreateDialog">创建项目</el-button>
      </div>
      
      <div class="overview-section">
        <div class="overview-copy">
          <h3>项目运营概览</h3>
          <p>优先处理待审核项目，及时跟进筹款中项目的发货、动态与支持者反馈。</p>
        </div>

        <div v-if="loading && projects.length === 0" class="overview-skeleton">
          <el-skeleton v-for="item in 4" :key="item" animated>
            <template #template>
              <el-skeleton-item variant="rect" class="overview-skeleton-item" />
            </template>
          </el-skeleton>
        </div>

        <div v-else class="overview-grid">
          <div
            v-for="card in summaryCards"
            :key="card.label"
            class="overview-card"
            :class="card.tone"
          >
            <span class="overview-label">{{ card.label }}</span>
            <strong class="overview-value">{{ card.value }}</strong>
            <span class="overview-tip">{{ card.tip }}</span>
          </div>
        </div>
      </div>

      <el-card>
        <el-table :data="projects" style="width: 100%" v-loading="loading">
          <template #empty>
            <el-empty description="暂无项目，创建后可在这里持续跟进审核、支持者和发货进度。">
              <el-button type="primary" @click="openCreateDialog">立即创建项目</el-button>
            </el-empty>
          </template>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="封面" width="120">
            <template #default="{ row }">
              <img :src="row.coverImage" style="width: 80px; height: 60px; object-fit: cover;" />
            </template>
          </el-table-column>
          <el-table-column label="标题" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link type="primary" :underline="false" @click="router.push(`/projects/${row.id}`)">
                <el-icon class="mr-1"><Link /></el-icon>
                {{ row.title }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="targetAmount" label="目标金额" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row)">
                {{ getStatusName(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button 
                v-if="row.status === 0 || row.status === 1" 
                type="primary" 
                size="small" 
                @click="openEditDialog(row)"
              >
                {{ row.status === 1 ? '编辑内容' : '编辑' }}
              </el-button>
              <el-button 
                v-if="row.status === 0" 
                type="danger" 
                size="small" 
                @click="handleCancel(row.id)"
              >
                取消
              </el-button>
              <el-button 
                v-if="row.status === 5" 
                type="success" 
                size="small" 
                @click="openDeliveryDialog(row)"
              >
                发货管理
              </el-button>
              <template v-if="row.status !== 0 && row.status !== 3">
                <el-button 
                  type="success" 
                  size="small" 
                  @click="openSupportersDialog(row.id)"
                >
                  支持者
                </el-button>
                <el-dropdown trigger="click" @command="handleCommand" style="margin-left: 10px;">
                  <el-button type="info" size="small">
                    更多 <el-icon class="el-icon--right"><arrow-down /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="() => openCommentsDialog(row.id)">留言管理</el-dropdown-item>
                      <el-dropdown-item :command="() => openDashboard(row)">数据看板</el-dropdown-item>
                      <el-dropdown-item :command="() => openUpdatesDialog(row.id)">发布动态</el-dropdown-item>
                      <el-dropdown-item :command="() => openPayoutsDialog(row.id)">资金拨付</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="fetchProjects"
          />
        </div>
      </el-card>
    </main>

    <!-- 支持者弹窗 -->
    <el-dialog v-model="supportersDialogVisible" title="支持者名单" width="800px">
      <div style="margin-bottom: 15px; text-align: right;">
        <el-button type="success" @click="exportSupporters" :loading="exporting">导出为 Excel</el-button>
      </div>
      <el-table :data="supporters" style="width: 100%" v-loading="loadingSupporters">
        <template #empty>
          <el-empty description="当前项目还没有支持记录，继续完善项目内容以提升转化。" />
        </template>
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="amount" label="支持金额" width="100" />
        <el-table-column prop="message" label="留言" show-overflow-tooltip />
        <el-table-column prop="payTime" label="支付时间" width="180" />
      </el-table>
      <div class="pagination" style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="supportersTotal"
          :page-size="supportersPageSize"
          v-model:current-page="supportersCurrentPage"
          @current-change="fetchSupporters"
        />
      </div>
    </el-dialog>

    <el-dialog v-model="projectDialogVisible" :title="isActiveEdit ? '修改项目内容' : (isEdit ? '修改项目' : '创建项目')" width="800px">
      <el-form ref="projectFormRef" :model="projectForm" :rules="rules" label-width="100px">
        <el-form-item label="项目标题" prop="title">
          <el-input v-model="projectForm.title" placeholder="请输入项目标题" :disabled="isActiveEdit" />
        </el-form-item>
        <el-form-item label="项目简介" prop="summary">
          <el-input v-model="projectForm.summary" type="textarea" rows="2" placeholder="请输入一句话简介" />
        </el-form-item>
        <el-form-item label="封面图片" prop="coverImage">
          <el-input v-model="projectForm.coverImage" placeholder="请输入封面图片URL" :disabled="isActiveEdit" />
        </el-form-item>
        <el-form-item label="视频链接" prop="videoUrl">
          <el-input v-model="projectForm.videoUrl" placeholder="请输入视频链接URL (可选)" />
        </el-form-item>
        <el-form-item label="目标金额" prop="targetAmount">
          <el-input-number v-model="projectForm.targetAmount" :min="100" :precision="2" :step="1000" :disabled="isActiveEdit" />
        </el-form-item>
        <el-form-item label="截止时间" prop="endTime">
          <el-date-picker
            v-model="projectForm.endTime"
            type="datetime"
            placeholder="选择截止时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled="isActiveEdit"
          />
        </el-form-item>
        <el-form-item label="项目详情" prop="content">
          <el-input v-model="projectForm.content" type="textarea" rows="6" placeholder="支持HTML格式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="projectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProject" :loading="submitting">
          {{ isActiveEdit ? '提交修改' : (isEdit ? '重新提交审核' : '提交审核') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Comments Dialog -->
    <el-dialog v-model="commentsDialogVisible" title="留言管理" width="800px">
      <el-table :data="comments" v-loading="commentsLoading">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">{{ new Date(row.createTime).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="replyComment(row)">回复</el-button>
            <el-button type="danger" size="small" @click="deleteComment(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- Dashboard Dialog -->
    <el-dialog v-model="dashboardDialogVisible" title="数据看板" width="800px">
      <div class="dashboard-stats" v-if="dashboardData">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>筹款总额</template>
              <div class="stat-value">￥{{ dashboardData.currentAmount }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>筹款进度</template>
              <div class="stat-value">{{ Math.min(100, Math.round((dashboardData.currentAmount / dashboardData.targetAmount) * 100)) }}%</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>剩余天数</template>
              <div class="stat-value">{{ calculateRemainingDays(dashboardData.endTime) }} 天</div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>累计访问量</template>
              <div class="stat-value">{{ dashboardData.viewCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>分享次数</template>
              <div class="stat-value">{{ dashboardData.shareCount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- Updates Dialog -->
    <el-dialog v-model="updatesDialogVisible" title="发布动态" width="600px">
      <el-form :model="updateForm" label-width="80px">
        <el-form-item label="动态标题">
          <el-input v-model="updateForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="动态内容">
          <el-input v-model="updateForm.content" type="textarea" rows="4" placeholder="向支持者更新项目进度..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updatesDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdate" :loading="submittingUpdate">发布</el-button>
      </template>
    </el-dialog>

    <!-- Payouts Dialog -->
    <el-dialog v-model="payoutsDialogVisible" title="资金拨付详情" width="800px">
      <el-table :data="payouts" style="width: 100%" v-loading="payoutsLoading">
        <el-table-column label="拨付阶段" width="120">
          <template #default="scope">
            第 {{ scope.row.stage }} 期
          </template>
        </el-table-column>
        <el-table-column prop="conditionDesc" label="拨付条件" />
        <el-table-column label="拨付金额" width="120">
          <template #default="scope">
            ¥ {{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已拨付' : '待拨付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="拨付时间" width="180">
          <template #default="scope">
            {{ scope.row.payoutTime ? new Date(scope.row.payoutTime).toLocaleString() : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <!-- 发货管理弹窗 -->
    <el-dialog v-model="showDeliveryDialog" :title="`发货管理 - ${currentProject?.title}`" width="800px">
      <el-table :data="supporters" style="width: 100%" v-loading="loadingSupporters">
        <template #empty>
          <el-empty description="暂无可发货的支持订单，后续支持成功后会自动出现在这里。" />
        </template>
        <el-table-column prop="nickname" label="支持者" width="120" />
        <el-table-column prop="amount" label="支持金额" width="100" />
        <el-table-column prop="message" label="留言" show-overflow-tooltip />
        <el-table-column prop="payTime" label="支付时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.payTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="deliveryStatus" label="发货状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.deliveryStatus === 1 ? 'success' : 'warning'">
              {{ row.deliveryStatus === 1 ? '已发货' : '待发货' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.deliveryStatus === 0" type="primary" size="small" @click="handleDelivery(row)">
              发货
            </el-button>
            <span v-else style="font-size: 12px; color: #67c23a;">单号: {{ row.expressNo }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="supportersTotal"
          :page-size="supportersPageSize"
          v-model:current-page="supportersCurrentPage"
          @current-change="fetchSupporters"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Link } from '@element-plus/icons-vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const handleCommand = (cmd: Function) => {
  if (typeof cmd === 'function') {
    cmd()
  }
}
import type { FormInstance, FormRules } from 'element-plus'

const projects = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

const summaryCards = computed(() => {
  const pendingCount = projects.value.filter(item => item.status === 0).length
  const fundraisingCount = projects.value.filter(item => item.status === 1).length
  const successCount = projects.value.filter(item => item.status === 5).length

  return [
    {
      label: '全部项目',
      value: total.value,
      tip: '累计创建项目总数',
      tone: 'primary'
    },
    {
      label: '当前页筹款中',
      value: fundraisingCount,
      tip: '可继续优化内容与动态',
      tone: 'success'
    },
    {
      label: '当前页待审核',
      value: pendingCount,
      tip: '建议尽快检查资料完整性',
      tone: 'warning'
    },
    {
      label: '当前页筹款成功',
      value: successCount,
      tip: '优先处理发货与回访',
      tone: 'info'
    }
  ]
})

const showDeliveryDialog = ref(false)
const currentProject = ref<any>(null)

const projectDialogVisible = ref(false)
const isEdit = ref(false)
const isActiveEdit = ref(false)
const submitting = ref(false)
const projectFormRef = ref<FormInstance>()

const supportersDialogVisible = ref(false)
const supporters = ref<any[]>([])
const loadingSupporters = ref(false)

const exporting = ref(false)
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
  const headers = ['用户ID', '支持金额', '留言', '支持时间']
  const rows = supporters.value.map(s => [
    s.userId, 
    s.amount, 
    s.message || '', 
    formatDateTime(s.createTime || s.payTime)
  ])
  const csvContent = [
    headers.map(escapeCsvCell).join(','),
    ...rows.map(row => row.map(escapeCsvCell).join(','))
  ].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `supporters_${Date.now()}.csv`
  link.click()
  exporting.value = false
}

// Comments
const commentsDialogVisible = ref(false)
const comments = ref<any[]>([])
const commentsLoading = ref(false)
const currentCommentsProjectId = ref<number | null>(null)

const openCommentsDialog = async (projectId: number) => {
  currentCommentsProjectId.value = projectId
  commentsDialogVisible.value = true
  commentsLoading.value = true
  try {
    const res = await request.get(`/comment/project/${projectId}`, { params: { size: 100 } })
    comments.value = res.data.records
  } catch (error) {
    console.error(error)
  } finally {
    commentsLoading.value = false
  }
}

const deleteComment = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该留言吗？', '提示', { type: 'warning' })
    await request.delete(`/comment/${id}`)
    ElMessage.success('删除成功')
    openCommentsDialog(currentCommentsProjectId.value!)
  } catch (error) {
    console.error(error)
  }
}

const replyComment = (row: any) => {
  ElMessageBox.prompt('请输入回复内容', '回复留言', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async ({ value }) => {
    if (!value) return
    try {
      await request.post('/comment/create', {
        projectId: currentCommentsProjectId.value,
        parentId: row.id,
        content: value
      })
      ElMessage.success('回复成功')
      openCommentsDialog(currentCommentsProjectId.value!)
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

// Dashboard
const dashboardDialogVisible = ref(false)
const dashboardData = ref<any>(null)

const openDashboard = (row: any) => {
  dashboardData.value = row
  dashboardDialogVisible.value = true
}

const calculateRemainingDays = (endTime: string) => {
  const diff = new Date(endTime).getTime() - Date.now()
  return diff > 0 ? Math.ceil(diff / (1000 * 3600 * 24)) : 0
}

// Updates
const updatesDialogVisible = ref(false)
const submittingUpdate = ref(false)
const currentUpdateProjectId = ref<number | null>(null)
const updateForm = reactive({ title: '', content: '' })

const openUpdatesDialog = (projectId: number) => {
  currentUpdateProjectId.value = projectId
  updateForm.title = ''
  updateForm.content = ''
  updatesDialogVisible.value = true
}

// Payouts
const payoutsDialogVisible = ref(false)
const payouts = ref<any[]>([])
const payoutsLoading = ref(false)

const openPayoutsDialog = async (projectId: number) => {
  payoutsDialogVisible.value = true
  payoutsLoading.value = true
  try {
    const res = await request.get('/funding/payouts', {
      params: { projectId, current: 1, size: 100 }
    })
    payouts.value = res.data.records
  } catch (error) {
    ElMessage.error('获取拨付详情失败')
  } finally {
    payoutsLoading.value = false
  }
}

const submitUpdate = async () => {
  if (!updateForm.title || !updateForm.content) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  submittingUpdate.value = true
  try {
    await request.post('/sponsor/update/publish', {
      projectId: currentUpdateProjectId.value,
      title: updateForm.title,
      content: updateForm.content
    })
    ElMessage.success('动态发布成功')
    updatesDialogVisible.value = false
  } catch (error) {
    console.error(error)
  } finally {
    submittingUpdate.value = false
  }
}
const supportersCurrentPage = ref(1)
const supportersPageSize = ref(10)
const supportersTotal = ref(0)
const currentSupportersProjectId = ref<number | null>(null)

const defaultForm = {
  id: undefined,
  title: '',
  summary: '',
  coverImage: '',
  videoUrl: '',
  targetAmount: 10000,
  endTime: '',
  content: ''
}
const projectForm = ref({ ...defaultForm })

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入简介', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请输入封面图片', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }],
  endTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入项目详情', trigger: 'blur' }]
})

const getStatusName = (row: any) => {
  if (row.status === 1 && row.currentAmount >= row.targetAmount) {
    return '已达标'
  }
  const map: Record<number, string> = {
    0: '待审核', 1: '筹款中', 2: '已驳回', 3: '已取消', 4: '已下架', 5: '筹款成功', 6: '筹款失败'
  }
  return map[row.status] || '未知'
}

const getStatusTagType = (row: any) => {
  if (row.status === 1 && row.currentAmount >= row.targetAmount) {
    return 'success'
  }
  const map: Record<number, string> = {
    0: 'warning', 1: 'primary', 2: 'danger', 3: 'info', 4: 'info', 5: 'success', 6: 'danger'
  }
  return map[row.status] || ''
}

const fetchProjects = async () => {
  loading.value = true
  try {
    const res = await request.get('/sponsor/project/my-list', {
      params: { current: currentPage.value, size: pageSize.value }
    })
    projects.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  isActiveEdit.value = false
  projectForm.value = { ...defaultForm }
  projectDialogVisible.value = true
  if (projectFormRef.value) projectFormRef.value.clearValidate()
}

const handleCancel = async (id: number) => {
  ElMessageBox.confirm('确定要取消该项目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/sponsor/project/cancel/${id}`)
      ElMessage.success('取消成功')
      fetchProjects()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const openSupportersDialog = (id: number) => {
  currentSupportersProjectId.value = id
  supportersCurrentPage.value = 1
  supportersDialogVisible.value = true
  fetchSupporters()
}

const openDeliveryDialog = (row: any) => {
  currentProject.value = row
  currentSupportersProjectId.value = row.id
  showDeliveryDialog.value = true
  supportersCurrentPage.value = 1
  fetchSupporters()
}

const fetchSupporters = async () => {
  if (!currentSupportersProjectId.value) return
  loadingSupporters.value = true
  try {
    const res: any = await request.get(`/sponsor/project/${currentSupportersProjectId.value}/supporters`, {
      params: { current: supportersCurrentPage.value, size: supportersPageSize.value }
    })
    supporters.value = res.data.records
    supportersTotal.value = res.data.total
  } catch (error) {
    console.error(error)
    ElMessage.error('获取支持者列表失败')
  } finally {
    loadingSupporters.value = false
  }
}

const handleDelivery = (row: any) => {
  ElMessageBox.prompt('请输入发货的物流单号', '发货确认', {
    confirmButtonText: '确认发货',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '物流单号不能为空'
  }).then(async ({ value }) => {
    try {
      await request.post(`/order/delivery/${row.id}`, { expressNo: value })
      ElMessage.success('发货成功')
      fetchSupporters()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.msg || '发货失败')
    }
  }).catch(() => {})
}

const openEditDialog = (row: any) => {
  isEdit.value = true
  isActiveEdit.value = row.status === 1
  projectForm.value = { ...row }
  projectDialogVisible.value = true
  if (projectFormRef.value) projectFormRef.value.clearValidate()
}

const submitProject = async () => {
  if (!projectFormRef.value) return
  await projectFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isActiveEdit.value) {
          await request.put('/sponsor/project/update-active', projectForm.value)
          ElMessage.success('项目内容修改成功')
        } else if (isEdit.value) {
          await request.put('/sponsor/project/update', projectForm.value)
          ElMessage.success('修改成功')
        } else {
          await request.post('/sponsor/project/create', projectForm.value)
          ElMessage.success('创建成功，等待审核')
        }
        projectDialogVisible.value = false
        fetchProjects()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.sponsor-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  max-width: 1200px;
  margin: 30px auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.overview-section {
  margin-bottom: 20px;
}

.overview-copy {
  margin-bottom: 14px;
}

.overview-copy h3 {
  margin: 0 0 6px;
  font-size: 20px;
  color: #303133;
}

.overview-copy p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.overview-grid,
.overview-skeleton {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.overview-card {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid #ebeef5;
  box-shadow: 0 10px 30px -20px rgba(15, 23, 42, 0.3);
}

.overview-card::after {
  content: '';
  position: absolute;
  top: -18px;
  right: -18px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
}

.overview-card.primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

.overview-card.success {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: #fff;
}

.overview-card.warning {
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  color: #fff;
}

.overview-card.info {
  background: linear-gradient(135deg, #7c7df0 0%, #a2a4ff 100%);
  color: #fff;
}

.overview-label,
.overview-tip,
.overview-value {
  position: relative;
  z-index: 1;
  display: block;
}

.overview-label {
  font-size: 14px;
  opacity: 0.92;
}

.overview-value {
  margin: 10px 0 6px;
  font-size: 32px;
  line-height: 1;
}

.overview-tip {
  font-size: 13px;
  opacity: 0.88;
}

.overview-skeleton-item {
  width: 100%;
  height: 118px;
  border-radius: 18px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 992px) {
  .overview-grid,
  .overview-skeleton {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .main-content {
    margin: 20px 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .overview-grid,
  .overview-skeleton {
    grid-template-columns: 1fr;
  }
}
</style>

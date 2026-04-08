<template>
  <div class="project-audit-container">
    <el-card class="audit-card">
      <template #header>
        <div class="card-header">
          <span>项目审计管理</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 待审核项目 Tab -->
        <el-tab-pane label="待审核项目" name="pending">
          <el-table :data="pendingProjects" border style="width: 100%" v-loading="pendingLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="项目名称" />
            <el-table-column prop="targetAmount" label="目标金额" width="120" />
            <el-table-column prop="createdAt" label="提交时间" width="180" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
                <el-button type="danger" size="small" @click="openRejectDialog(row.id)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pendingCurrentPage"
              v-model:page-size="pendingPageSize"
              :total="pendingTotal"
              @current-change="fetchPendingProjects"
              layout="total, prev, pager, next"
            />
          </div>
        </el-tab-pane>

        <!-- 全量项目 Tab -->
        <el-tab-pane label="全量项目" name="all">
          <el-table :data="allProjects" border style="width: 100%" v-loading="allLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="项目名称" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <el-link type="primary" :underline="false" @click="router.push(`/projects/${row.id}`)">
                  <el-icon class="mr-1"><Link /></el-icon>
                  {{ row.title }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column label="发起人" width="120">
              <template #default="{ row }">
                <el-tag size="small" type="info" effect="plain">
                  <el-icon><User /></el-icon> {{ row.sponsorName || `用户 ${row.sponsorId}` }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="row.status === 1 && row.currentAmount >= row.targetAmount" type="success">已达标</el-tag>
                <el-tag v-else-if="row.status === 1" type="primary">筹款中</el-tag>
                <el-tag v-else-if="row.status === 2" type="danger">已驳回</el-tag>
                <el-tag v-else-if="row.status === 3" type="info">已取消</el-tag>
                <el-tag v-else-if="row.status === 4" type="info">已下架</el-tag>
                <el-tag v-else-if="row.status === 5" type="success">筹款成功</el-tag>
                <el-tag v-else-if="row.status === 6" type="danger">筹款失败</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="目标 / 已筹" width="160">
              <template #default="{ row }">
                <div>目标：￥{{ row.targetAmount }}</div>
                <div style="color: #f56c6c;">已筹：￥{{ row.currentAmount }}</div>
              </template>
            </el-table-column>
            <el-table-column label="筹款时间" width="220">
              <template #default="{ row }">
                <div style="font-size: 12px; color: #606266;">
                  起: {{ row.startTime ? row.startTime.substring(0, 10) : '-' }}<br/>
                  止: {{ row.endTime ? row.endTime.substring(0, 10) : '-' }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="openSupportersDialog(row.id)">支持者</el-button>
                <el-button v-if="row.status === 1" type="danger" size="small" @click="openTakedownDialog(row.id)">下架</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="allCurrentPage"
              v-model:page-size="allPageSize"
              :total="allTotal"
              @current-change="fetchAllProjects"
              layout="total, prev, pager, next"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showRejectDialog" title="填写驳回原因" width="500px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因..." />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRejectDialog = false">取消</el-button>
          <el-button type="danger" @click="handleReject">确认驳回</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 下架弹窗 -->
    <el-dialog v-model="showTakedownDialog" title="填写下架原因" width="500px">
      <el-input v-model="takedownReason" type="textarea" :rows="4" placeholder="请输入下架原因..." />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTakedownDialog = false">取消</el-button>
          <el-button type="danger" @click="handleTakedown">确认下架</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 支持者列表弹窗 -->
    <el-dialog v-model="supportersDialogVisible" title="项目支持者列表" width="800px">
      <div class="dialog-header-actions" style="margin-bottom: 15px; text-align: right;">
        <el-button type="primary" @click="exportSupporters" :loading="exporting">
          导出 Excel
        </el-button>
      </div>
      <el-table :data="supporters" border style="width: 100%" v-loading="loadingSupporters">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="支持金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">￥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="留言" show-overflow-tooltip />
        <el-table-column prop="payTime" label="支持时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Link, User } from '@element-plus/icons-vue'
import { getPendingProjects, approveProject, rejectProject, takedownProject } from '../../api/admin'
import request from '../../utils/request'

const router = useRouter()
const activeTab = ref('pending')

// 待审核项目状态
const pendingLoading = ref(false)
const pendingProjects = ref([])
const pendingCurrentPage = ref(1)
const pendingPageSize = ref(10)
const pendingTotal = ref(0)

const showRejectDialog = ref(false)
const rejectReason = ref('')
const currentRejectId = ref<number | null>(null)

// 全量项目状态
const allLoading = ref(false)
const allProjects = ref([])
const allCurrentPage = ref(1)
const allPageSize = ref(10)
const allTotal = ref(0)

const showTakedownDialog = ref(false)
const takedownReason = ref('')
const currentTakedownId = ref<number | null>(null)

const supportersDialogVisible = ref(false)
const supporters = ref<any[]>([])
const loadingSupporters = ref(false)
const exporting = ref(false)

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
    s.payTime || ''
  ])
  
  const csvContent = "data:text/csv;charset=utf-8,\uFEFF" 
    + headers.join(",") + "\n" 
    + rows.map(e => e.join(",")).join("\n")
    
  const encodedUri = encodeURI(csvContent)
  const link = document.createElement("a")
  link.setAttribute("href", encodedUri)
  link.setAttribute("download", `项目支持者列表.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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

const fetchPendingProjects = async () => {
  pendingLoading.value = true
  try {
    const res: any = await getPendingProjects({ current: pendingCurrentPage.value, size: pendingPageSize.value })
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

onMounted(() => {
  fetchPendingProjects()
})

const handleTabClick = (tab: any) => {
  if (tab.paneName === 'pending' && pendingProjects.value.length === 0) {
    fetchPendingProjects()
  } else if (tab.paneName === 'all' && allProjects.value.length === 0) {
    fetchAllProjects()
  }
}

// 待审核操作
const handleApprove = (id: number) => {
  ElMessageBox.confirm('确认通过该项目的审核并上线吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await approveProject(id)
      ElMessage.success('审核通过，项目已上线')
      fetchPendingProjects()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
  }).catch(() => {})
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
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 全量项目操作
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
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}
</script>

<style scoped>
.project-audit-container {
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.audit-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
}

.card-header {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-primary);
}

.table-wrapper {
  overflow-x: auto;
}
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .project-audit-container {
    padding: 0;
  }
  .pagination-container {
    justify-content: center;
  }
}
</style>

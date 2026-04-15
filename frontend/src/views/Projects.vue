<template>
  <div class="projects-container">
    <main class="main-content">
      <div class="content-layout">
        <!-- Main Project List -->
        <div class="project-list-section">
          <div class="section-header">
            <h2>探索项目</h2>
          </div>
          
          <!-- Filters -->
          <div class="filter-card">
            <el-form :inline="true" class="filter-form">
              <el-form-item label="关键字">
                <el-input v-model="keyword" placeholder="搜索项目标题或简介" clearable @clear="fetchProjects" @keyup.enter="fetchProjects" style="width: 200px;">
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="项目分类">
                <el-select v-model="categoryId" placeholder="全部分类" clearable @change="fetchProjects" style="width: 150px;">
                  <el-option label="科技创新" :value="1" />
                  <el-option label="公益助农" :value="2" />
                  <el-option label="文化艺术" :value="3" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="目标金额">
                <el-select v-model="amountRange" placeholder="不限" clearable @change="handleAmountChange" style="width: 150px;">
                  <el-option label="1万以下" value="0-10000" />
                  <el-option label="1万-5万" value="10000-50000" />
                  <el-option label="5万-10万" value="50000-100000" />
                  <el-option label="10万以上" value="100000-" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="筹款进度">
                <el-select v-model="progressRange" placeholder="不限" clearable @change="handleProgressChange" style="width: 150px;">
                  <el-option label="50%以下" value="0-50" />
                  <el-option label="50%-100%" value="50-100" />
                  <el-option label="已达标(100%+)" value="100-" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="排序方式">
                <el-select v-model="sortType" placeholder="排序方式" @change="fetchProjects" style="width: 150px">
                  <el-option label="默认排序(热度)" :value="1" />
                  <el-option label="最新上线" :value="2" />
                  <el-option label="金额最高" :value="3" />
                </el-select>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="fetchProjects">筛选</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="project-grid" v-loading="loading">
            <el-card 
              v-for="item in projects" 
              :key="item.id" 
              class="project-card"
              @click="goToDetail(item.id)"
              shadow="hover"
            >
              <div class="image-wrapper">
                <img :src="item.coverImage" class="project-image" />
                <div class="status-badge" v-if="item.status === 5 || (item.status === 1 && item.currentAmount >= item.targetAmount)">
                  筹款成功
                </div>
              </div>
              <div class="project-info">
                <h3 class="project-title">{{ item.title }}</h3>
                <p class="project-desc">{{ item.summary }}</p>
                
                <div class="sponsor-info" @click.stop="openUserProfile(item.sponsorId)">
                  <el-avatar :size="24" :src="item.sponsorAvatar || defaultAvatar" class="sponsor-avatar" />
                  <span class="sponsor-name">{{ item.sponsorName || '未知用户' }}</span>
                  <span class="sponsor-tag">发起人</span>
                </div>
                
                <div class="progress-container">
                  <el-progress 
                    :percentage="Math.min(100, Math.round((item.currentAmount / item.targetAmount) * 100))" 
                    :stroke-width="8"
                    :color="customColors"
                  />
                </div>
                
                <div class="project-stats">
                  <div class="stat-item">
                    <span class="stat-value">￥{{ item.currentAmount }}</span>
                    <span class="stat-label">已筹金额</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ item.supporterCount }}</span>
                    <span class="stat-label">支持人数</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ calculateRemainingDays(item.endTime) }}</span>
                    <span class="stat-label">剩余天数</span>
                  </div>
                </div>
              </div>
            </el-card>
          </div>

          <div class="pagination" v-if="total > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="fetchProjects"
            />
          </div>
          <el-empty v-else-if="!loading" description="暂无在线项目" />
        </div>
      </div>
    </main>

    <UserProfilePreviewDialog
      v-model="showUserProfile"
      :user-id="selectedUserId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import request from '../utils/request'
import UserProfilePreviewDialog from '../components/UserProfilePreviewDialog.vue'
import defaultAvatar from '../assets/default-avatar.svg'

const router = useRouter()

const showUserProfile = ref(false)
const selectedUserId = ref<number | null>(null)

const openUserProfile = (userId: number) => {
  if (!userId) return
  selectedUserId.value = userId
  showUserProfile.value = true
}

const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 },
]

const calculateRemainingDays = (endTime: string) => {
  if (!endTime) return 0
  const end = new Date(endTime).getTime()
  const now = new Date().getTime()
  const diff = end - now
  return diff > 0 ? Math.ceil(diff / (1000 * 60 * 60 * 24)) : 0
}

const projects = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const sortType = ref(1)
const keyword = ref('')
const categoryId = ref<number | null>(null)
const amountRange = ref('')
const progressRange = ref('')

const minAmount = ref<number | null>(null)
const maxAmount = ref<number | null>(null)
const minProgress = ref<number | null>(null)
const maxProgress = ref<number | null>(null)

const handleAmountChange = (val: string) => {
  if (!val) {
    minAmount.value = null
    maxAmount.value = null
  } else {
    const parts = val.split('-')
    minAmount.value = parts[0] ? Number(parts[0]) : null
    maxAmount.value = parts[1] ? Number(parts[1]) : null
  }
  fetchProjects()
}

const handleProgressChange = (val: string) => {
  if (!val) {
    minProgress.value = null
    maxProgress.value = null
  } else {
    const parts = val.split('-')
    minProgress.value = parts[0] ? Number(parts[0]) : null
    maxProgress.value = parts[1] ? Number(parts[1]) : null
  }
  fetchProjects()
}

const fetchProjects = async () => {
  loading.value = true
  try {
    const res = await request.get('/public/project/list', {
      params: {
        current: currentPage.value,
        size: pageSize.value,
        sortType: sortType.value,
        keyword: keyword.value || undefined,
        categoryId: categoryId.value || undefined,
        minAmount: minAmount.value !== null ? minAmount.value : undefined,
        maxAmount: maxAmount.value !== null ? maxAmount.value : undefined,
        minProgress: minProgress.value !== null ? minProgress.value : undefined,
        maxProgress: maxProgress.value !== null ? maxProgress.value : undefined
      }
    })
    projects.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/projects/${id}`)
}

onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.projects-container {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: calc(var(--spacing-unit) * 8);
}
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: calc(var(--spacing-unit) * 5) var(--spacing-4);
}
.content-layout {
  display: flex;
  gap: calc(var(--spacing-unit) * 5);
}
.project-list-section {
  flex: 1;
  min-width: 0;
}
.section-header {
  margin-bottom: var(--spacing-4);
}
.section-header h2 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: var(--text-2xl);
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}
.filter-card {
  background: var(--bg-surface);
  padding: var(--spacing-3);
  border-radius: var(--radius-lg);
  margin-bottom: calc(var(--spacing-unit) * 5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}
.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-2);
}
.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-4);
  margin-bottom: calc(var(--spacing-unit) * 5);
}
.project-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: all var(--transition-fast);
}
.project-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  border-bottom: 1px solid var(--border-light);
  transition: transform var(--transition-fast);
}
.image-wrapper {
  position: relative;
  overflow: hidden;
}
.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: var(--color-success);
  color: white;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: bold;
  box-shadow: var(--shadow-sm);
  z-index: 10;
}
.project-info {
  padding: var(--spacing-3);
  display: flex;
  flex-direction: column;
  flex: 1;
}
.project-title {
  font-family: var(--font-heading);
  font-size: var(--text-base);
  margin: 0 0 var(--spacing-2) 0;
  color: var(--text-primary);
  line-height: 1.3;
}
.project-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0 0 var(--spacing-2) 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}
.sponsor-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  margin-bottom: var(--spacing-2);
  cursor: pointer;
  width: fit-content;
}
.sponsor-avatar {
  --el-avatar-bg-color: transparent;
  flex-shrink: 0;
}
.sponsor-info:hover .sponsor-name {
  color: var(--color-primary);
}
.sponsor-name {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}
.sponsor-tag {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 1px 8px;
  border-radius: var(--radius-pill);
  line-height: 18px;
}
.progress-container {
  margin-bottom: var(--spacing-3);
}
.project-stats {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-top: 1px solid var(--border-light);
  padding-top: var(--spacing-2);
}
.stat-item {
  display: flex;
  flex-direction: column;
}
.stat-item:last-child {
  align-items: flex-end;
}
.stat-value {
  font-family: var(--font-heading);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--text-primary);
}
.stat-label {
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--text-tertiary);
  margin-top: 4px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: calc(var(--spacing-unit) * 8);
}
@media (max-width: 768px) {
  .main-content {
    padding: var(--spacing-3) var(--spacing-2);
  }
  .section-header h2 {
    font-size: var(--text-xl);
  }
  .filter-card {
    padding: var(--spacing-3);
  }
  .filter-form {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 15px;
  }
  .filter-form :deep(.el-form-item__content),
  .filter-form :deep(.el-input),
  .filter-form :deep(.el-select) {
    width: 100% !important;
  }
  .project-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-3);
  }
  .project-image {
    height: 200px;
  }
}
</style>

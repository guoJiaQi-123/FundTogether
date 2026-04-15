<template>
  <div class="home-container">
    <main class="main-content">
      <div class="hero-section">
        <h1 class="hero-title">{{ t('home.heroTitle') }}</h1>
        <p class="hero-subtitle">{{ t('home.heroSubtitle') }}</p>
      </div>

      <div class="content-layout">
        <!-- Main Project List -->
        <div class="project-list-section">
          
          <!-- Recommended Section -->
          <div class="section-header" v-if="recommendedProjects.length > 0">
            <h2>{{ t('home.recommended') }}</h2>
          </div>
          <div class="project-grid" v-loading="recommendLoading" v-if="recommendedProjects.length > 0">
            <el-card 
              v-for="item in recommendedProjects" 
              :key="'rec-' + item.id" 
              class="project-card recommend-card"
              @click="goToDetail(item.id)"
              shadow="hover"
            >
              <div class="recommend-badge">{{ t('home.recommendBadge') }}</div>
              <img :src="item.coverImage" class="project-image" />
              <div class="project-info">
                <h3 class="project-title">{{ item.title }}</h3>
                <p class="project-desc">{{ item.summary }}</p>
                
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
                    <span class="stat-label">{{ t('home.raised') }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ item.supporterCount }}</span>
                    <span class="stat-label">{{ t('home.supporters') }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ calculateRemainingDays(item.endTime) }}</span>
                    <span class="stat-label">{{ t('home.daysLeft') }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </div>

          <div class="section-header" style="margin-top: 20px;">
            <h2>{{ t('home.allProjects') }}</h2>
            <div class="filter-bar">
              <el-input v-model="keyword" :placeholder="t('home.searchPlaceholder')" clearable @clear="fetchProjects" @keyup.enter="fetchProjects" style="width: 200px; margin-right: 10px;">
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select v-model="categoryId" :placeholder="t('home.allCategories')" clearable @change="fetchProjects" style="width: 120px; margin-right: 10px;">
                <el-option :label="t('home.categoryTech')" :value="1" />
                <el-option :label="t('home.categoryCharity')" :value="2" />
                <el-option :label="t('home.categoryArt')" :value="3" />
              </el-select>
              <el-select v-model="amountRange" :placeholder="t('home.targetAmount')" clearable @change="handleAmountChange" style="width: 120px; margin-right: 10px;">
                <el-option :label="t('home.under10k')" value="0-10000" />
                <el-option :label="t('home.between10k50k')" value="10000-50000" />
                <el-option :label="t('home.between50k100k')" value="50000-100000" />
                <el-option :label="t('home.above100k')" value="100000-" />
              </el-select>
              <el-select v-model="progressRange" :placeholder="t('home.progress')" clearable @change="handleProgressChange" style="width: 120px; margin-right: 10px;">
                <el-option :label="t('home.under50')" value="0-50" />
                <el-option :label="t('home.between50and100')" value="50-100" />
                <el-option :label="t('home.reached100')" value="100-" />
              </el-select>
              <el-select v-model="sortType" :placeholder="t('home.sortType')" @change="fetchProjects" style="width: 150px">
                <el-option :label="t('home.sortHot')" :value="1" />
                <el-option :label="t('home.sortNew')" :value="2" />
                <el-option :label="t('home.sortAmount')" :value="3" />
              </el-select>
            </div>
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
                  {{ t('home.successStatus') }}
                </div>
              </div>
              <div class="project-info">
                <h3 class="project-title">{{ item.title }}</h3>
                <p class="project-desc">{{ item.summary }}</p>
                
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
                    <span class="stat-label">{{ t('home.raised') }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ item.supporterCount }}</span>
                    <span class="stat-label">{{ t('home.supporters') }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ calculateRemainingDays(item.endTime) }}</span>
                    <span class="stat-label">{{ t('home.daysLeft') }}</span>
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
          <el-empty v-else-if="!loading" :description="t('common.noData')" />
        </div>

        <!-- Sidebar / Leaderboard -->
        <aside class="sidebar">
          <el-card class="leaderboard-card" shadow="never">
            <template #header>
              <div class="leaderboard-header">
                <h3><el-icon><Trophy /></el-icon> {{ t('home.leaderboardTitle') }}</h3>
                <el-select v-model="leaderboardSort" size="small" style="width: 100px" @change="fetchLeaderboard">
                  <el-option :label="t('home.byHeat')" :value="1" />
                  <el-option :label="t('home.byAmount')" :value="3" />
                </el-select>
              </div>
            </template>
            <div class="leaderboard-list" v-loading="leaderboardLoading">
              <div 
                v-for="(item, index) in leaderboardProjects" 
                :key="item.id"
                class="leaderboard-item"
                @click="goToDetail(item.id)"
              >
                <div class="rank-badge" :class="`rank-${index + 1}`">{{ index + 1 }}</div>
                <div class="image-wrapper-small">
                  <img :src="item.coverImage" class="item-image" />
                  <div class="status-badge-small" v-if="item.status === 5 || (item.status === 1 && item.currentAmount >= item.targetAmount)">
                    {{ t('home.successStatus') }}
                  </div>
                </div>
                <div class="lb-info">
                  <div class="lb-title">{{ item.title }}</div>
                  <div class="lb-stats">
                    <span>{{ t('home.raised') }}: ￥{{ item.currentAmount }}</span>
                    <span style="margin-left: 10px;">{{ t('home.heat') }}: {{ item.heat || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Trophy } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import request from '../utils/request'

const { t } = useI18n()

const router = useRouter()

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

// Recommended
const recommendedProjects = ref<any[]>([])
const recommendLoading = ref(false)

const fetchRecommended = async () => {
  recommendLoading.value = true
  try {
    const res = await request.get('/projects/recommend')
    recommendedProjects.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    recommendLoading.value = false
  }
}

// Leaderboard
const leaderboardProjects = ref<any[]>([])
const leaderboardLoading = ref(false)
const leaderboardSort = ref(1)

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

const fetchLeaderboard = async () => {
  leaderboardLoading.value = true
  try {
    const res = await request.get('/public/project/list', {
      params: {
        current: 1,
        size: 5,
        sortType: leaderboardSort.value
      }
    })
    leaderboardProjects.value = res.data.records
  } catch (error) {
    console.error(error)
  } finally {
    leaderboardLoading.value = false
  }
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
  fetchRecommended()
  fetchProjects()
  fetchLeaderboard()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: calc(var(--spacing-unit) * 8);
}

.hero-section {
  position: relative;
  text-align: center;
  padding: calc(var(--spacing-unit) * 12) var(--spacing-4);
  background: var(--primary);
  color: white;
  border-radius: var(--radius-xl);
  margin: var(--spacing-4);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.hero-title {
  font-family: var(--font-heading);
  font-size: var(--text-3xl);
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 var(--spacing-3) 0;
  position: relative;
  z-index: 1;
  color: #FFFFFF;
}

.hero-subtitle {
  font-size: var(--text-base);
  color: rgba(255, 255, 255, 0.85);
  margin: 0 auto;
  max-width: 600px;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--spacing-4);
}

.content-layout {
  display: flex;
  gap: calc(var(--spacing-unit) * 5);
  margin-top: calc(var(--spacing-unit) * 5);
}

.project-list-section {
  flex: 1;
  min-width: 0;
}

.sidebar {
  width: 340px;
  flex-shrink: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
}

.section-header h2 {
  margin: 0;
  font-size: var(--text-xl);
  letter-spacing: -0.02em;
  color: var(--text-primary);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-2);
  background: var(--bg-surface);
  padding: var(--spacing-2);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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

.recommend-card {
  border: 2px solid var(--color-accent) !important;
  position: relative;
}

.recommend-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background-color: var(--color-accent);
  color: white;
  padding: 4px 14px;
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: 700;
  z-index: 10;
  box-shadow: var(--shadow-md);
}

.project-image {
  width: 100%;
  height: 220px;
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
  margin: 0 0 var(--spacing-3) 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
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

.leaderboard-card {
  position: sticky;
  top: 84px;
}

.leaderboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.leaderboard-header h3 {
  margin: 0;
  font-size: var(--text-base);
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-2) 0;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.leaderboard-item:hover {
  transform: translateX(4px);
}

.leaderboard-item:last-child {
  border-bottom: none;
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background-color: var(--bg-page);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xs);
  font-weight: 800;
  margin-right: var(--spacing-2);
  flex-shrink: 0;
}

.rank-1 {
  background-color: var(--color-warning);
  color: white;
  box-shadow: var(--shadow-md);
}

.rank-2 {
  background-color: var(--gray-300);
  color: white;
}

.rank-3 {
  background-color: hsl(25, 60%, 45%);
  color: white;
}

.image-wrapper-small {
  position: relative;
  width: 60px;
  height: 60px;
  margin-right: 15px;
  flex-shrink: 0;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.status-badge-small {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: var(--color-success);
  color: white;
  padding: 2px 4px;
  border-radius: 2px;
  font-size: 10px;
  font-weight: bold;
  z-index: 10;
}

.lb-info {
  flex: 1;
  min-width: 0;
}

.lb-title {
  font-family: var(--font-heading);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.lb-stats {
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--text-secondary);
}

@media (max-width: 1024px) {
  .content-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .hero-section {
    margin: var(--spacing-2);
    padding: calc(var(--spacing-unit) * 8) var(--spacing-3);
  }
  .hero-title {
    font-size: var(--text-2xl);
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: var(--text-xl);
  }
  .hero-subtitle {
    font-size: var(--text-sm);
  }
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-3);
  }
  .filter-bar {
    width: 100%;
    flex-direction: column;
  }
  .filter-bar .el-input,
  .filter-bar .el-select {
    width: 100% !important;
  }
  .project-grid {
    grid-template-columns: 1fr;
  }
  .main-content {
    padding: 0 var(--spacing-2);
  }
}
</style>

<template>
  <div class="favorites-page">
    <div class="page-header">
      <div class="header-content">
        <h2>我的收藏</h2>
        <span class="count-badge" v-if="total > 0">{{ total }} 个项目</span>
      </div>
      <p class="header-desc" v-if="projects.length > 0">你关注的项目动态，一目了然</p>
    </div>
    <div v-loading="loading" class="loading-wrapper">
      <div v-if="projects.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="var(--gray-300)" stroke-width="1.5">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
        </div>
        <h3>还没有收藏</h3>
        <p>浏览项目，点击心形图标收藏感兴趣的项目</p>
        <el-button type="primary" @click="router.push('/projects')">发现项目</el-button>
      </div>
      <div class="masonry-grid" v-else>
        <div v-for="(project, index) in projects" :key="project.id" class="fav-card" :style="{ animationDelay: index * 60 + 'ms' }" @click="router.push(`/projects/${project.id}`)">
          <div class="card-image-wrapper">
            <img :src="project.coverImage" class="card-cover" v-if="project.coverImage" />
            <div class="card-cover-overlay">
              <el-progress :percentage="Math.min(100, Math.round((project.currentAmount / project.targetAmount) * 100))" :stroke-width="4" :show-text="false" color="white" class="overlay-progress" />
              <span class="overlay-percent">{{ Math.min(100, Math.round((project.currentAmount / project.targetAmount) * 100)) }}%</span>
            </div>
            <button class="unfav-btn" @click.stop="handleRemove(project.id)" title="取消收藏">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
              </svg>
            </button>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ project.title }}</h3>
            <p class="card-summary">{{ project.summary }}</p>
            <div class="card-footer">
              <span class="card-amount">¥{{ project.currentAmount }}</span>
              <span class="card-target">/ ¥{{ project.targetAmount }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="pagination" v-if="total > 10">
        <el-pagination v-model:current-page="current" :page-size="10" layout="prev, pager, next" :total="total" @current-change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyFavorites, removeFavorite } from '../api/user'

const router = useRouter()
const projects = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyFavorites({ current: current.value, size: 10 })
    projects.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRemove = async (projectId: number) => {
  try {
    await removeFavorite(projectId)
    ElMessage.success('已取消收藏')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.favorites-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-5) var(--spacing-4);
}
.page-header {
  margin-bottom: var(--spacing-5);
}
.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}
.page-header h2 {
  font-family: var(--font-heading);
  font-size: var(--text-2xl);
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.02em;
}
.count-badge {
  font-size: var(--text-xs);
  font-weight: 700;
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 4px 12px;
  border-radius: var(--radius-pill);
}
.header-desc {
  margin: var(--spacing-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}
.loading-wrapper {
  min-height: 300px;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: calc(var(--spacing-unit) * 10) var(--spacing-4);
  text-align: center;
}
.empty-icon {
  margin-bottom: var(--spacing-3);
  opacity: 0.6;
}
.empty-state h3 {
  font-family: var(--font-heading);
  font-size: var(--text-lg);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-1);
}
.empty-state p {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  margin: 0 0 var(--spacing-4);
}
.masonry-grid {
  columns: 3;
  column-gap: var(--spacing-3);
}
.fav-card {
  break-inside: avoid;
  margin-bottom: var(--spacing-3);
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all var(--transition-fast);
  animation: cardIn 0.4s ease-out both;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
.fav-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
  border-color: var(--color-primary);
}
.card-image-wrapper {
  position: relative;
  overflow: hidden;
}
.card-cover {
  width: 100%;
  display: block;
  transition: transform 0.4s ease-out;
}
.fav-card:hover .card-cover {
  transform: scale(1.05);
}
.card-cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-3);
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  opacity: 0;
  transition: opacity var(--transition-fast);
}
.fav-card:hover .card-cover-overlay {
  opacity: 1;
}
.overlay-progress {
  flex: 1;
}
.overlay-percent {
  font-size: var(--text-xs);
  font-weight: 700;
  color: white;
  white-space: nowrap;
}
.unfav-btn {
  position: absolute;
  top: var(--spacing-2);
  right: var(--spacing-2);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-danger);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.8);
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}
.fav-card:hover .unfav-btn {
  opacity: 1;
  transform: scale(1);
}
.unfav-btn:hover {
  background: var(--color-danger);
  color: white;
  transform: scale(1.1) !important;
}
.card-body {
  padding: var(--spacing-3);
}
.card-title {
  font-family: var(--font-heading);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-1);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-summary {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  line-height: 1.5;
  margin: 0 0 var(--spacing-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-footer {
  display: flex;
  align-items: baseline;
  gap: 2px;
}
.card-amount {
  font-family: var(--font-heading);
  font-size: var(--text-base);
  font-weight: 800;
  color: var(--color-primary);
}
.card-target {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.pagination {
  margin-top: var(--spacing-5);
  display: flex;
  justify-content: center;
}
@media (max-width: 1024px) {
  .masonry-grid {
    columns: 2;
  }
}
@media (max-width: 640px) {
  .masonry-grid {
    columns: 1;
  }
  .favorites-page {
    padding: var(--spacing-3) var(--spacing-2);
  }
  .page-header h2 {
    font-size: var(--text-xl);
  }
}
</style>

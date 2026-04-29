<template>
  <div class="recommend-manage-container">
    <!-- 页头 -->
    <header class="page-header">
      <div class="page-header__text">
        <h1 class="page-header__title">项目推荐管理</h1>
        <p class="page-header__subtitle">管理首页推荐展示的项目，排序值越小越靠前</p>
      </div>
      <div class="page-header__actions">
        <el-button type="primary" :icon="Plus" @click="openAddDialog">添加推荐</el-button>
      </div>
    </header>

    <!-- 顶部统计卡片 -->
    <section class="stat-grid">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--primary">
          <el-icon><Star /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">当前推荐数</div>
          <div class="stat-card__value">{{ formatNumber(stats.count) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--accent">
          <el-icon><Coin /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">推荐项目总筹款</div>
          <div class="stat-card__value">￥{{ formatNumber(stats.totalAmount) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--success">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">推荐项目总支持者</div>
          <div class="stat-card__value">{{ formatNumber(stats.totalSupporters) }}</div>
        </div>
      </div>
    </section>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip-banner"
    >
      可通过"上移/下移"调整展示顺序，或切换到卡片视图进行可视化管理。
    </el-alert>

    <el-card class="manage-card" shadow="never">
      <!-- 视图切换 -->
      <div class="view-switcher">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button value="list">
            <el-icon class="switcher-icon"><Tickets /></el-icon>
            列表视图
          </el-radio-button>
          <el-radio-button value="grid">
            <el-icon class="switcher-icon"><Grid /></el-icon>
            卡片视图
          </el-radio-button>
        </el-radio-group>
        <div class="view-switcher__info">共 {{ stats.count }} 条推荐</div>
      </div>

      <!-- 骨架屏 -->
      <el-skeleton v-if="loading && recommendList.length === 0" :rows="6" animated />

      <!-- 列表视图 -->
      <el-table
        v-else-if="viewMode === 'list'"
        :data="recommendList"
        style="width: 100%"
        v-loading="loading"
        row-key="id"
        class="data-table"
        stripe
      >
        <el-table-column label="排序" width="120" align="center">
          <template #default="{ row, $index }">
            <div class="sort-cell">
              <el-icon class="drag-handle" title="排序"><Rank /></el-icon>
              <div class="sort-buttons">
                <el-button
                  :icon="Top"
                  size="small"
                  circle
                  :disabled="$index === 0"
                  @click="moveUp($index)"
                />
                <el-button
                  :icon="Bottom"
                  size="small"
                  circle
                  :disabled="$index === recommendList.length - 1"
                  @click="moveDown($index)"
                />
              </div>
              <span class="sort-value">{{ row.sortOrder }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="projectId" label="ID" width="80" align="center" />
        <el-table-column label="项目" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="project-cell">
              <el-image
                :src="row.coverImage || placeholderImage"
                fit="cover"
                class="project-cell__thumb"
                :preview-src-list="row.coverImage ? [row.coverImage] : []"
                hide-on-click-modal
              />
              <div class="project-cell__meta">
                <span class="project-cell__title">{{ row.projectTitle || '项目已删除' }}</span>
                <el-tag v-if="row.categoryName" size="small" effect="plain" type="info">
                  {{ row.categoryName }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="项目状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="primary" size="small">筹款中</el-tag>
            <el-tag v-else-if="row.status === 5" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="row.status === 4" type="danger" size="small">已下架</el-tag>
            <el-tag v-else type="info" size="small">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已筹 / 目标" min-width="180">
          <template #default="{ row }">
            <div v-if="row.targetAmount" class="progress-cell">
              <div class="progress-cell__text">
                <span class="amount-text amount-text--accent">￥{{ formatNumber(row.currentAmount) }}</span>
                <span class="muted"> / ￥{{ formatNumber(row.targetAmount) }}</span>
              </div>
              <el-progress
                :percentage="getProgressPercent(row)"
                :color="getProgressColor(row)"
                :stroke-width="6"
                :show-text="false"
              />
            </div>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="推荐点击量" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" type="warning">
              {{ formatNumber(getClickCount(row)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="添加时间" width="180">
          <template #default="{ row }">
            <span class="muted">{{ row.createdAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" :icon="Delete" @click="handleRemove(row)">移除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无推荐项目" />
        </template>
      </el-table>

      <!-- 卡片视图 -->
      <div v-else class="card-grid" v-loading="loading">
        <div
          v-for="(row, idx) in recommendList"
          :key="row.id"
          class="recommend-card"
        >
          <div class="recommend-card__cover">
            <el-image
              :src="row.coverImage || placeholderImage"
              fit="cover"
              class="recommend-card__cover-img"
            />
            <div class="recommend-card__rank">#{{ row.sortOrder }}</div>
            <el-tag v-if="row.status === 1" type="primary" size="small" class="recommend-card__status">筹款中</el-tag>
            <el-tag v-else-if="row.status === 5" type="success" size="small" class="recommend-card__status">已完成</el-tag>
            <el-tag v-else-if="row.status === 4" type="danger" size="small" class="recommend-card__status">已下架</el-tag>
            <el-tag v-else type="info" size="small" class="recommend-card__status">其他</el-tag>
          </div>
          <div class="recommend-card__body">
            <div class="recommend-card__title">{{ row.projectTitle || '项目已删除' }}</div>
            <div class="recommend-card__meta">
              <el-tag v-if="row.categoryName" size="small" effect="plain" type="info">
                {{ row.categoryName }}
              </el-tag>
              <span class="muted">点击量 {{ formatNumber(getClickCount(row)) }}</span>
            </div>
            <div v-if="row.targetAmount" class="recommend-card__progress">
              <div class="progress-cell__text">
                <span class="amount-text amount-text--accent">￥{{ formatNumber(row.currentAmount) }}</span>
                <span class="muted"> / ￥{{ formatNumber(row.targetAmount) }}</span>
              </div>
              <el-progress
                :percentage="getProgressPercent(row)"
                :color="getProgressColor(row)"
                :stroke-width="6"
                :show-text="false"
              />
            </div>
            <div class="recommend-card__actions">
              <el-button
                size="small"
                :icon="Top"
                :disabled="idx === 0"
                @click="moveUp(idx)"
                plain
              >上移</el-button>
              <el-button
                size="small"
                :icon="Bottom"
                :disabled="idx === recommendList.length - 1"
                @click="moveDown(idx)"
                plain
              >下移</el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="handleRemove(row)">移除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="recommendList.length === 0 && !loading" description="暂无推荐项目" class="card-grid__empty" />
      </div>
    </el-card>

    <!-- 添加推荐弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加推荐项目" width="900px" align-center>
      <div class="add-dialog-content">
        <div class="add-dialog-content__toolbar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索项目名称..."
            clearable
            class="add-dialog-content__search"
            :prefix-icon="Search"
            @keyup.enter="searchProjects"
            @clear="searchProjects"
          />
          <el-select
            v-model="categoryFilter"
            placeholder="全部分类"
            clearable
            class="add-dialog-content__category"
            @change="searchProjects"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
          <el-button type="primary" :icon="Search" @click="searchProjects">查询</el-button>
        </div>

        <div class="available-grid" v-loading="searchLoading">
          <div
            v-for="row in filteredAvailableProjects"
            :key="row.id"
            class="available-card"
          >
            <el-image
              :src="row.coverImage || placeholderImage"
              fit="cover"
              class="available-card__cover"
            />
            <div class="available-card__body">
              <div class="available-card__title" :title="row.title">{{ row.title }}</div>
              <div class="available-card__meta">
                <span class="amount-text amount-text--accent">￥{{ formatNumber(row.currentAmount) }}</span>
                <span class="muted"> / ￥{{ formatNumber(row.targetAmount) }}</span>
              </div>
              <div class="available-card__foot">
                <el-tag size="small" effect="plain" type="warning">热度 {{ formatNumber(row.heat || 0) }}</el-tag>
                <el-button
                  type="primary"
                  size="small"
                  :loading="addingId === row.id"
                  :disabled="recommendedProjectIds.has(row.id)"
                  @click="handleAdd(row)"
                >
                  {{ recommendedProjectIds.has(row.id) ? '已推荐' : '添加' }}
                </el-button>
              </div>
            </div>
          </div>
          <el-empty
            v-if="filteredAvailableProjects.length === 0 && !searchLoading"
            description="暂无可推荐项目"
            class="available-grid__empty"
          />
        </div>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="availableCurrentPage"
            v-model:page-size="availablePageSize"
            :total="availableTotal"
            @current-change="searchProjects"
            layout="total, prev, pager, next"
            small
            background
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Top,
  Bottom,
  Star,
  Coin,
  UserFilled,
  Tickets,
  Grid,
  Rank,
  Delete
} from '@element-plus/icons-vue'
import {
  getRecommendList,
  addRecommend,
  removeRecommend,
  batchUpdateRecommendSort,
  getAvailableProjects
} from '../../api/admin'
import { getCategories } from '../../api/project'

const placeholderImage =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="80" viewBox="0 0 120 80"><rect width="120" height="80" fill="%23F2F3F5"/><path d="M40 60l12-18 10 12 8-9 18 15H40z" fill="%23C9CDD4"/><circle cx="45" cy="30" r="6" fill="%23C9CDD4"/></svg>'

const formatNumber = (value: unknown): string => {
  const num = Number(value ?? 0)
  if (!Number.isFinite(num)) return '0'
  return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const loading = ref(false)
const recommendList = ref<any[]>([])
const viewMode = ref<'list' | 'grid'>('list')

const recommendedProjectIds = computed(() => {
  return new Set(recommendList.value.map((r) => r.projectId))
})

const stats = computed(() => {
  const count = recommendList.value.length
  const totalAmount = recommendList.value.reduce((acc, r) => acc + Number(r.currentAmount || 0), 0)
  const totalSupporters = recommendList.value.reduce((acc, r) => acc + Number(r.supporterCount || 0), 0)
  return { count, totalAmount, totalSupporters }
})

const fetchRecommendList = async () => {
  loading.value = true
  try {
    const res: any = await getRecommendList()
    recommendList.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('获取推荐列表失败')
  } finally {
    loading.value = false
  }
}

const moveUp = async (index: number) => {
  if (index <= 0) return
  const list = [...recommendList.value]
  ;[list[index - 1], list[index]] = [list[index], list[index - 1]]
  recommendList.value = list
  await saveSortOrder()
}

const moveDown = async (index: number) => {
  if (index >= recommendList.value.length - 1) return
  const list = [...recommendList.value]
  ;[list[index], list[index + 1]] = [list[index + 1], list[index]]
  recommendList.value = list
  await saveSortOrder()
}

const saveSortOrder = async () => {
  try {
    const orderedIds = recommendList.value.map((r) => r.id)
    await batchUpdateRecommendSort(orderedIds)
  } catch (error) {
    console.error(error)
    ElMessage.error('排序更新失败')
    fetchRecommendList()
  }
}

const handleRemove = (row: any) => {
  ElMessageBox.confirm(
    `确认将项目「${row.projectTitle || row.projectId}」从推荐列表中移除？`,
    '提示',
    { type: 'warning' }
  )
    .then(async () => {
      try {
        await removeRecommend(row.id)
        ElMessage.success('已移除推荐')
        fetchRecommendList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    })
    .catch(() => {})
}

// 进度颜色
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

// 推荐点击量（占位）：优先使用 supporterCount 作为热度数据
const getClickCount = (row: any): number => {
  if (row.supporterCount) return Number(row.supporterCount)
  const pid = Number(row.projectId || 0)
  return (pid * 7) % 999
}

// ===== 添加推荐 =====
const addDialogVisible = ref(false)
const searchKeyword = ref('')
const categoryFilter = ref<number | null>(null)
const availableProjects = ref<any[]>([])
const searchLoading = ref(false)
const availableCurrentPage = ref(1)
const availablePageSize = ref(12)
const availableTotal = ref(0)
const addingId = ref<number | null>(null)
const categories = ref<any[]>([])

const filteredAvailableProjects = computed(() => {
  if (!categoryFilter.value) return availableProjects.value
  return availableProjects.value.filter((p) => Number(p.categoryId) === Number(categoryFilter.value))
})

const openAddDialog = () => {
  addDialogVisible.value = true
  searchKeyword.value = ''
  categoryFilter.value = null
  availableCurrentPage.value = 1
  searchProjects()
  if (categories.value.length === 0) fetchCategories()
}

const fetchCategories = async () => {
  try {
    const res: any = await getCategories()
    categories.value = res.data || []
  } catch {
    // 静默
  }
}

const searchProjects = async () => {
  searchLoading.value = true
  try {
    const res: any = await getAvailableProjects({
      current: availableCurrentPage.value,
      size: availablePageSize.value,
      keyword: searchKeyword.value || undefined
    })
    availableProjects.value = res.data.records || []
    availableTotal.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    searchLoading.value = false
  }
}

const handleAdd = async (row: any) => {
  addingId.value = row.id
  try {
    await addRecommend(row.id)
    ElMessage.success('添加推荐成功')
    searchProjects()
    fetchRecommendList()
  } catch (error: any) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    addingId.value = null
  }
}

onMounted(() => {
  fetchRecommendList()
})
</script>

<style scoped>
.recommend-manage-container {
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
}
.page-header__subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
.stat-card__label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.stat-card__value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
}

.tip-banner {
  border-radius: var(--radius-md);
}

/* 主卡片 */
.manage-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.manage-card :deep(.el-card__body) {
  padding: 24px;
}

.view-switcher {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}
.view-switcher__info {
  font-size: 13px;
  color: var(--text-secondary);
}
.switcher-icon {
  margin-right: 4px;
  vertical-align: -2px;
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

.sort-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.drag-handle {
  color: var(--text-tertiary);
  font-size: 14px;
  cursor: grab;
}
.sort-buttons {
  display: flex;
  gap: 4px;
}
.sort-value {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 600;
}

.project-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.project-cell__thumb {
  width: 64px;
  height: 48px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  flex-shrink: 0;
}
.project-cell__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.project-cell__title {
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.progress-cell__text {
  font-size: 13px;
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

/* 卡片网格视图 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  min-height: 120px;
  position: relative;
}
.card-grid__empty {
  grid-column: 1 / -1;
}
.recommend-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.recommend-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.recommend-card__cover {
  position: relative;
  aspect-ratio: 16 / 9;
  background: hsl(220, 14%, 96%);
}
.recommend-card__cover-img {
  width: 100%;
  height: 100%;
}
.recommend-card__rank {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 10px;
  background: hsl(0, 0%, 0%, 0.7);
  color: #fff;
  font-weight: 700;
  border-radius: var(--radius-md);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}
.recommend-card__status {
  position: absolute;
  top: 8px;
  right: 8px;
}
.recommend-card__body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}
.recommend-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recommend-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.recommend-card__progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.recommend-card__actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
  flex-wrap: wrap;
}

/* 添加弹窗 */
.add-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.add-dialog-content__toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.add-dialog-content__search {
  flex: 1;
  min-width: 220px;
}
.add-dialog-content__category {
  width: 180px;
}

.available-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
  min-height: 200px;
  max-height: 60vh;
  overflow-y: auto;
  padding: 4px;
  position: relative;
}
.available-grid__empty {
  grid-column: 1 / -1;
}
.available-card {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  overflow: hidden;
  transition: box-shadow 0.2s ease;
}
.available-card:hover {
  box-shadow: var(--shadow-sm);
}
.available-card__cover {
  aspect-ratio: 16 / 9;
  width: 100%;
}
.available-card__body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.available-card__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.available-card__meta {
  font-size: 13px;
}
.available-card__foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .add-dialog-content__search,
  .add-dialog-content__category {
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
  .manage-card :deep(.el-card__body) {
    padding: 16px;
  }
  .pagination-container {
    justify-content: center;
  }
}
</style>

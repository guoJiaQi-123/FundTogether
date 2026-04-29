<template>
  <div class="admin-container">
    <main class="main-content">
      <!-- 页头 -->
      <header class="page-header">
        <div class="header-text">
          <h2 class="page-title">系统通知管理</h2>
          <p class="page-desc">统一管理平台发布的系统公告与通知消息</p>
        </div>
        <el-button type="primary" class="action-btn" @click="openDialog()">
          <el-icon class="el-icon--left"><Plus /></el-icon>
          发布新通知
        </el-button>
      </header>

      <!-- 统计卡片 -->
      <section class="stat-grid">
        <div class="stat-card">
          <div class="stat-icon stat-icon--primary">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">通知总数</div>
            <div class="stat-value">{{ allNotices.length }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--success">
            <el-icon><Promotion /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">已发布</div>
            <div class="stat-value">{{ publishedCount }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--warning">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">草稿</div>
            <div class="stat-value">{{ draftCount }}</div>
          </div>
        </div>
      </section>

      <!-- 筛选区 -->
      <section class="filter-bar">
        <div class="filter-label">状态筛选</div>
        <el-radio-group v-model="statusFilter" size="default" class="filter-tabs">
          <el-radio-button :value="'all'">全部 ({{ allNotices.length }})</el-radio-button>
          <el-radio-button :value="'published'">已发布 ({{ publishedCount }})</el-radio-button>
          <el-radio-button :value="'draft'">草稿 ({{ draftCount }})</el-radio-button>
        </el-radio-group>
      </section>

      <!-- 通知时间线 -->
      <section class="timeline-section" v-loading="loading">
        <div v-if="filteredNotices.length === 0 && !loading" class="empty-wrap">
          <el-empty description="暂无通知数据" />
        </div>

        <article
          v-for="item in filteredNotices"
          :key="item.id"
          class="notice-item"
          :class="item.status === 1 ? 'notice-item--published' : 'notice-item--draft'"
        >
          <div class="notice-rail">
            <span
              class="notice-dot"
              :class="item.status === 1 ? 'notice-dot--published' : 'notice-dot--draft'"
            ></span>
          </div>

          <div class="notice-card">
            <div class="notice-header">
              <div class="notice-title-wrap">
                <el-tag
                  v-if="item.status === 1"
                  type="success"
                  size="small"
                  effect="plain"
                  class="notice-tag"
                >
                  <el-icon><CircleCheck /></el-icon>
                  <span>已发布</span>
                </el-tag>
                <el-tag
                  v-else
                  type="info"
                  size="small"
                  effect="plain"
                  class="notice-tag"
                >
                  <el-icon><EditPen /></el-icon>
                  <span>草稿</span>
                </el-tag>
                <el-tag
                  v-if="isUrgent(item)"
                  type="danger"
                  size="small"
                  effect="dark"
                  class="notice-tag"
                >紧急</el-tag>
                <h3 class="notice-title">{{ item.title }}</h3>
              </div>
              <div class="notice-meta">
                <span class="meta-pair">
                  <el-icon><Clock /></el-icon>
                  {{ formatDate(item.createdAt) }}
                </span>
                <span class="meta-pair">
                  <el-icon><User /></el-icon>
                  目标：全体用户
                </span>
              </div>
            </div>

            <p class="notice-content">{{ item.content }}</p>

            <div class="notice-footer">
              <el-button size="small" class="notice-op" @click="previewNotice(item)">
                <el-icon class="el-icon--left"><View /></el-icon>
                预览
              </el-button>
              <el-button size="small" class="notice-op" @click="openDialog(item)">
                <el-icon class="el-icon--left"><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="item.status === 0"
                size="small"
                type="success"
                class="notice-op"
                @click="publishNotice(item.id)"
              >
                <el-icon class="el-icon--left"><Promotion /></el-icon>
                发布
              </el-button>
              <el-button
                size="small"
                class="notice-op notice-op--danger"
                @click="deleteNotice(item.id)"
              >
                <el-icon class="el-icon--left"><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </article>
      </section>
    </main>

    <!-- 编辑 / 新建 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑通知' : '发布新通知'"
      width="640px"
      class="notice-dialog"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="通知标题" required>
          <el-input
            v-model="form.title"
            placeholder="请输入通知标题"
            maxlength="80"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="通知内容" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入通知内容，支持换行"
            maxlength="1000"
            show-word-limit
            resize="vertical"
          />
          <div class="form-hint">当前字数：{{ form.content.length }} / 1000</div>
        </el-form-item>
        <div class="form-options">
          <el-form-item label="标记为紧急公告">
            <el-switch v-model="form.urgent" />
          </el-form-item>
          <el-form-item label="立即发布">
            <el-switch v-model="form.publish" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNotice" :loading="submitting">
          {{ form.publish ? '保存并发布' : '保存为草稿' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览 Dialog -->
    <el-dialog v-model="previewVisible" title="通知预览" width="560px">
      <div class="preview-content">
        <div class="preview-header">
          <el-tag
            v-if="previewData?.status === 1"
            type="success"
            size="small"
            effect="plain"
          >已发布</el-tag>
          <el-tag v-else type="info" size="small" effect="plain">草稿</el-tag>
          <el-tag v-if="isUrgent(previewData)" type="danger" size="small" effect="dark">紧急</el-tag>
          <span class="preview-time">{{ formatDate(previewData?.createdAt) }}</span>
        </div>
        <h2 class="preview-title">{{ previewData?.title }}</h2>
        <div class="preview-body">{{ previewData?.content }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import {
  Plus,
  Edit,
  Delete,
  View,
  Bell,
  Promotion,
  EditPen,
  CircleCheck,
  Clock,
  User
} from '@element-plus/icons-vue'

interface NoticeItem {
  id: number
  title: string
  content: string
  status: number
  type?: string | number
  createdAt?: string
  updatedAt?: string
}

const allNotices = ref<NoticeItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const previewVisible = ref(false)
const previewData = ref<NoticeItem | null>(null)

const statusFilter = ref<'all' | 'published' | 'draft'>('all')

// 本地维护的紧急状态集合（后端若暂无 type 字段时前端兜底）
const urgentLocalIds = ref<Set<number>>(new Set())

const form = ref({
  id: undefined as number | undefined,
  title: '',
  content: '',
  publish: true,
  urgent: false
})

const publishedCount = computed(() => allNotices.value.filter(n => n.status === 1).length)
const draftCount = computed(() => allNotices.value.filter(n => n.status === 0).length)

const filteredNotices = computed(() => {
  if (statusFilter.value === 'published') {
    return allNotices.value.filter(n => n.status === 1)
  }
  if (statusFilter.value === 'draft') {
    return allNotices.value.filter(n => n.status === 0)
  }
  return allNotices.value
})

const isUrgent = (item: NoticeItem | null | undefined): boolean => {
  if (!item) return false
  if (item.type === 'urgent' || item.type === 1) return true
  return urgentLocalIds.value.has(item.id)
}

const formatDate = (value?: string): string => {
  if (!value) return '-'
  const d = new Date(value)
  if (isNaN(d.getTime())) return value
  return d.toLocaleString('zh-CN', { hour12: false })
}

const fetchNotices = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/notices')
    allNotices.value = res.data?.records || res.data || []
  } catch (e) {
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (row?: NoticeItem) => {
  if (row) {
    form.value = {
      id: row.id,
      title: row.title,
      content: row.content,
      publish: row.status === 1,
      urgent: isUrgent(row)
    }
  } else {
    form.value = { id: undefined, title: '', content: '', publish: true, urgent: false }
  }
  dialogVisible.value = true
}

const submitNotice = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    const payload: Record<string, any> = {
      title: form.value.title,
      content: form.value.content,
      status: form.value.publish ? 1 : 0,
      type: form.value.urgent ? 'urgent' : 'normal'
    }
    if (form.value.id) {
      try {
        await request.put(`/admin/notices/${form.value.id}`, payload)
      } catch {
        // 如后端未提供编辑接口，降级 POST
        await request.post('/admin/notices', payload)
      }
      if (form.value.urgent) urgentLocalIds.value.add(form.value.id)
      else urgentLocalIds.value.delete(form.value.id)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/notices', payload)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    fetchNotices()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const publishNotice = async (id: number) => {
  try {
    await request.put(`/admin/notices/${id}/publish`)
    ElMessage.success('发布成功')
    fetchNotices()
  } catch {
    ElMessage.error('发布失败')
  }
}

const deleteNotice = (id: number) => {
  ElMessageBox.confirm('确认删除该通知吗？此操作不可恢复。', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await request.delete(`/admin/notices/${id}`)
      urgentLocalIds.value.delete(id)
      ElMessage.success('删除成功')
      fetchNotices()
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const previewNotice = (item: NoticeItem) => {
  previewData.value = item
  previewVisible.value = true
}

onMounted(() => {
  fetchNotices()
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
  grid-template-columns: repeat(3, 1fr);
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

/* 筛选条 */
.filter-bar {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.filter-tabs :deep(.el-radio-button__inner) {
  border-radius: var(--radius-sm);
}

/* 时间线 */
.timeline-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 120px;
}

.empty-wrap {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 48px 0;
}

.notice-item {
  display: grid;
  grid-template-columns: 32px 1fr;
  gap: 16px;
  position: relative;
}

.notice-rail {
  position: relative;
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

.notice-rail::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: -16px;
  left: 50%;
  width: 2px;
  background: var(--border-light);
  transform: translateX(-50%);
}

.notice-item:last-child .notice-rail::before {
  bottom: 0;
}

.notice-dot {
  position: relative;
  z-index: 1;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--bg-surface);
  border: 3px solid var(--color-info);
  box-shadow: 0 0 0 3px var(--bg-surface);
}

.notice-dot--published {
  border-color: var(--color-success);
}

.notice-dot--draft {
  border-color: var(--color-info);
}

.notice-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), border-color var(--transition-fast);
}

.notice-card:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.notice-item--draft .notice-card {
  background: hsl(220, 14%, 98%);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.notice-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
  flex: 1;
}

.notice-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  line-height: 1.4;
}

.notice-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border-radius: var(--radius-sm);
}

.notice-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.meta-pair {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.notice-content {
  margin: 8px 0 12px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap;
  word-break: break-word;
}

.notice-footer {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 4px;
  border-top: 1px dashed var(--border-light);
  margin-top: 4px;
  padding-top: 12px;
}

.notice-op {
  border-radius: var(--radius-md);
  transition: transform var(--transition-fast);
}

.notice-op:hover {
  transform: translateY(-1px);
}

.notice-op--danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}

/* Dialog */
.form-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  text-align: right;
}

/* 预览 */
.preview-content {
  padding: 8px 0;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.preview-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: auto;
}

.preview-title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  line-height: 1.4;
}

.preview-body {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 16px;
  background: hsl(220, 14%, 98%);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--color-primary);
  max-height: 400px;
  overflow-y: auto;
}

/* 响应式 */
@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .page-title { font-size: 20px; }
  .stat-grid { grid-template-columns: 1fr; }
  .notice-item { grid-template-columns: 20px 1fr; gap: 12px; }
  .notice-header { flex-direction: column; }
  .form-options { grid-template-columns: 1fr; }
}
</style>

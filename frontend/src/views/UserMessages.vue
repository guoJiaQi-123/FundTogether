<template>
  <div class="messages-page">
    <div class="page-header">
      <div class="header-row">
        <h2>消息通知</h2>
        <el-button v-if="messages.some(m => !m.isRead)" text type="primary" size="small" @click="markAllRead">全部标为已读</el-button>
      </div>
      <p class="header-desc">你的项目动态与系统通知</p>
    </div>

    <div v-loading="loading" class="loading-wrapper">
      <div v-if="messages.length === 0 && !loading" class="empty-state">
        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="var(--gray-300)" stroke-width="1.5">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <p>暂无消息</p>
      </div>
      <div class="message-feed" v-else>
        <div v-for="msg in messages" :key="msg.id" class="msg-card" :class="{ 'msg-unread': !msg.isRead }" @click="handleMsgClick(msg)">
          <div class="msg-dot" v-if="!msg.isRead"></div>
          <div class="msg-icon" :class="'msg-icon--type' + msg.type">
            <svg v-if="msg.type === 1" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></svg>
            <svg v-else-if="msg.type === 2" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
            <svg v-else-if="msg.type === 3" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"/><path d="M1 10h22"/></svg>
          </div>
          <div class="msg-body">
            <div class="msg-header">
              <span class="msg-type-badge" :class="'badge-type' + msg.type">{{ getTypeName(msg.type) }}</span>
              <span class="msg-time">{{ msg.createdAt }}</span>
            </div>
            <div class="msg-title">{{ msg.title }}</div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
          <div class="msg-action" v-if="!msg.isRead || (msg.relatedId && msg.type === 2)">
            <el-button v-if="!msg.isRead" text size="small" @click.stop="markAsRead(msg.id)">已读</el-button>
            <el-button v-if="msg.relatedId && msg.type === 2" text size="small" type="primary" @click.stop="router.push(`/projects/${msg.relatedId}`)">查看</el-button>
          </div>
        </div>
      </div>
      <div class="pagination" v-if="total > 10">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize" layout="prev, pager, next" :total="total" @current-change="fetchMessages" small />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const messages = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getTypeName = (type: number) => {
  const map: any = { 1: '系统公告', 2: '项目动态', 3: '评论回复', 4: '订单通知' }
  return map[type] || '其他'
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const res = await request.get('/message/my-list', {
      params: { current: currentPage.value, size: pageSize.value }
    })
    messages.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const markAsRead = async (id: number) => {
  try {
    await request.put(`/message/${id}/read`)
    fetchMessages()
  } catch (error) {
    console.error(error)
  }
}

const markAllRead = async () => {
  try {
    for (const msg of messages.value.filter(m => !m.isRead)) {
      await request.put(`/message/${msg.id}/read`)
    }
    fetchMessages()
  } catch (error) { console.error(error) }
}

const handleMsgClick = async (msg: any) => {
  if (!msg.isRead) {
    markAsRead(msg.id)
  }
  if (msg.relatedId && msg.type === 2) {
    router.push(`/projects/${msg.relatedId}`)
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.messages-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-5) var(--spacing-4);
}
.page-header {
  margin-bottom: var(--spacing-4);
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-header h2 {
  font-family: var(--font-heading);
  font-size: var(--text-2xl);
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.02em;
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
  padding: calc(var(--spacing-unit) * 10) var(--spacing-4);
  color: var(--text-tertiary);
}
.empty-state p {
  margin-top: var(--spacing-2);
}
.message-feed {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}
.msg-card {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-3);
  padding: var(--spacing-3) var(--spacing-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
}
.msg-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}
.msg-unread {
  background: var(--color-primary-light);
  border-color: hsl(225, 68%, 88%);
}
.msg-dot {
  position: absolute;
  top: 16px;
  left: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
}
.msg-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.msg-icon--type1 {
  background: hsl(38, 92%, 95%);
  color: var(--color-warning);
}
.msg-icon--type2 {
  background: hsl(160, 55%, 94%);
  color: var(--color-success);
}
.msg-icon--type3 {
  background: hsl(225, 68%, 95%);
  color: var(--color-primary);
}
.msg-icon--type4 {
  background: hsl(280, 35%, 94%);
  color: var(--secondary-2);
}
.msg-body {
  flex: 1;
  min-width: 0;
}
.msg-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  margin-bottom: 4px;
}
.msg-type-badge {
  font-size: 12px;
  font-weight: 700;
  padding: 1px 8px;
  border-radius: var(--radius-pill);
}
.badge-type1 {
  background: hsl(38, 92%, 95%);
  color: var(--color-warning);
}
.badge-type2 {
  background: hsl(160, 55%, 94%);
  color: var(--color-success);
}
.badge-type3 {
  background: hsl(225, 68%, 95%);
  color: var(--color-primary);
}
.badge-type4 {
  background: hsl(280, 35%, 94%);
  color: var(--secondary-2);
}
.msg-time {
  font-size: 12px;
  color: var(--text-tertiary);
}
.msg-title {
  font-weight: 700;
  font-size: var(--text-sm);
  color: var(--text-primary);
  margin-bottom: 4px;
}
.msg-content {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.msg-action {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.pagination {
  margin-top: var(--spacing-4);
  display: flex;
  justify-content: center;
}
@media (max-width: 768px) {
  .messages-page {
    padding: var(--spacing-3) var(--spacing-2);
    max-width: 100%;
  }
  .page-header h2 {
    font-size: var(--text-xl);
  }
  .msg-card {
    padding: var(--spacing-2) var(--spacing-3);
  }
}
</style>

<template>
  <div class="orders-page">
    <div class="page-header">
      <h2>我的支持</h2>
      <p class="header-desc">追踪你的每一笔支持与资金流向</p>
    </div>

    <div class="stats-row">
      <div class="stat-card stat-card--primary">
        <div class="stat-card-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
        </div>
        <div class="stat-card-info">
          <span class="stat-card-value">¥{{ stats.totalAmount || 0 }}</span>
          <span class="stat-card-label">累计支持金额</span>
        </div>
      </div>
      <div class="stat-card stat-card--accent">
        <div class="stat-card-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18"/><path d="M9 21V9"/></svg>
        </div>
        <div class="stat-card-info">
          <span class="stat-card-value">{{ stats.projectCount || 0 }}</span>
          <span class="stat-card-label">支持项目数</span>
        </div>
      </div>
    </div>

    <el-card class="content-card">
      <el-tabs v-model="activeTab" class="modern-tabs">
        <el-tab-pane name="orders">
          <template #label>
            <span class="tab-label">支持订单</span>
          </template>
          <div v-loading="loading">
            <div v-if="orders.length === 0 && !loading" class="empty-state">
              <p>暂无支持记录</p>
            </div>
            <div class="order-list" v-else>
              <div v-for="order in orders" :key="order.id" class="order-card" @click="router.push(`/projects/${order.projectId}`)">
                <div class="order-main">
                  <div class="order-top">
                    <span class="order-project">{{ order.projectName || `项目 ${order.projectId}` }}</span>
                    <el-tag size="small" :type="order.status === 1 ? 'success' : (order.status === 0 ? 'warning' : (order.status === 3 ? 'danger' : 'info'))">
                      {{ order.status === 1 ? '已支付' : (order.status === 0 ? '待支付' : (order.status === 3 ? '已退款' : '已取消')) }}
                    </el-tag>
                  </div>
                  <div class="order-meta">
                    <span class="order-amount">¥{{ order.amount }}</span>
                    <span class="order-channel">{{ order.payChannel === '3' ? '余额支付' : (order.payChannel === '2' ? '支付宝' : '微信支付') }}</span>
                    <span class="order-time">{{ new Date(order.createdAt).toLocaleDateString() }}</span>
                  </div>
                  <div class="order-extra" v-if="order.status === 1">
                    <el-tag v-if="order.deliveryStatus === 1" size="small" type="success" effect="plain">已发货 {{ order.expressNo }}</el-tag>
                    <el-tag v-else size="small" type="warning" effect="plain">待发货</el-tag>
                  </div>
                </div>
                <div class="order-arrow">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="var(--text-tertiary)" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
                </div>
              </div>
            </div>
          </div>
          <div class="pagination" v-if="total > 10">
            <el-pagination v-model:current-page="currentPage" :page-size="pageSize" layout="prev, pager, next" :total="total" @current-change="fetchOrders" small />
          </div>
        </el-tab-pane>

        <el-tab-pane name="ledgers">
          <template #label>
            <span class="tab-label">资金流水</span>
          </template>
          <div v-loading="loadingLedgers">
            <div v-if="ledgers.length === 0 && !loadingLedgers" class="empty-state">
              <p>暂无流水记录</p>
            </div>
            <div class="ledger-timeline" v-else>
              <div v-for="ledger in ledgers" :key="ledger.id" class="ledger-item">
                <div class="ledger-dot" :class="{ 'dot-in': ledger.type === 2 || ledger.type === 3, 'dot-out': ledger.type === 1 }"></div>
                <div class="ledger-content">
                  <div class="ledger-header">
                    <span class="ledger-type">
                      <el-tag size="small" :type="ledger.type === 1 ? 'danger' : 'success'" effect="plain">
                        {{ ledger.type === 1 ? '支出' : '收入' }}
                      </el-tag>
                      <span class="ledger-project" v-if="ledger.projectName" @click.stop="router.push(`/projects/${ledger.projectId}`)">{{ ledger.projectName }}</span>
                    </span>
                    <span class="ledger-amount" :class="{ 'amount-in': ledger.type === 2 || ledger.type === 3, 'amount-out': ledger.type === 1 }">
                      {{ ledger.type === 2 || ledger.type === 3 ? '+' : '-' }}¥{{ ledger.amount }}
                    </span>
                  </div>
                  <div class="ledger-remark">
                    <el-icon class="trace-icon"><Tickets /></el-icon>
                    {{ ledger.remark }}
                  </div>
                  <div class="ledger-time">{{ new Date(ledger.createdAt).toLocaleString() }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="pagination" v-if="ledgerTotal > 10">
            <el-pagination v-model:current-page="ledgerPage" :page-size="ledgerSize" layout="prev, pager, next" :total="ledgerTotal" @current-change="fetchLedgers" small />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { Tickets } from '@element-plus/icons-vue'

const router = useRouter()

const activeTab = ref('orders')

const orders = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = ref<any>({})

// Ledgers state
const ledgers = ref<any[]>([])
const loadingLedgers = ref(false)
const ledgerPage = ref(1)
const ledgerSize = ref(10)
const ledgerTotal = ref(0)

const fetchLedgers = async () => {
  loadingLedgers.value = true
  try {
    const res = await request.get('/funding/ledgers', {
      params: { current: ledgerPage.value, size: ledgerSize.value }
    })
    ledgers.value = res.data.records
    ledgerTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('获取资金流水失败')
  } finally {
    loadingLedgers.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await request.get('/order/my-stats')
    stats.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/order/my-list', {
      params: { current: currentPage.value, size: pageSize.value }
    })
    orders.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchOrders()
  fetchLedgers()
})
</script>

<style scoped>
.orders-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-5) var(--spacing-4);
}
.page-header {
  margin-bottom: var(--spacing-4);
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
.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
}
.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-3) var(--spacing-4);
  border-radius: var(--radius-lg);
  color: white;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}
.stat-card--primary {
  background: var(--primary);
}
.stat-card--accent {
  background: var(--secondary-3);
}
.stat-card-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-card-value {
  font-family: var(--font-heading);
  font-size: var(--text-xl);
  font-weight: 800;
  display: block;
  line-height: 1.2;
}
.stat-card-label {
  font-size: var(--text-xs);
  opacity: 0.85;
  display: block;
  margin-top: 2px;
}
.content-card {
  border-radius: var(--radius-xl);
  border: none;
  box-shadow: var(--shadow-md);
}
.content-card :deep(.el-card__body) {
  padding: var(--spacing-4);
}
.modern-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--border-light);
}
.tab-label {
  font-weight: 600;
  font-size: var(--text-sm);
}
.empty-state {
  text-align: center;
  padding: var(--spacing-5) var(--spacing-4);
  color: var(--text-tertiary);
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}
.order-card {
  display: flex;
  align-items: center;
  padding: var(--spacing-3) var(--spacing-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.order-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
  transform: translateX(4px);
}
.order-main {
  flex: 1;
  min-width: 0;
}
.order-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-1);
}
.order-project {
  font-weight: 700;
  font-size: var(--text-sm);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}
.order-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  margin-bottom: var(--spacing-1);
}
.order-amount {
  font-family: var(--font-heading);
  font-weight: 800;
  font-size: var(--text-base);
  color: var(--color-danger);
}
.order-channel {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  background: var(--gray-100);
  padding: 2px 8px;
  border-radius: var(--radius-pill);
}
.order-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.order-extra {
  margin-top: var(--spacing-1);
}
.order-arrow {
  flex-shrink: 0;
  margin-left: var(--spacing-2);
  transition: transform var(--transition-fast);
}
.order-card:hover .order-arrow {
  transform: translateX(4px);
}
.ledger-timeline {
  position: relative;
  padding-left: var(--spacing-4);
}
.ledger-timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: var(--border-color);
}
.ledger-item {
  position: relative;
  padding: var(--spacing-2) 0 var(--spacing-3);
  display: flex;
  gap: var(--spacing-3);
}
.ledger-dot {
  position: absolute;
  left: calc(-1 * var(--spacing-4) + 1px);
  top: 14px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 3px solid var(--bg-surface);
  z-index: 1;
}
.dot-in {
  background: var(--color-success);
}
.dot-out {
  background: var(--color-danger);
}
.ledger-content {
  flex: 1;
  min-width: 0;
  background: var(--gray-50);
  border-radius: var(--radius-md);
  padding: var(--spacing-2) var(--spacing-3);
}
.ledger-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-1);
}
.ledger-type {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
}
.ledger-project {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-primary);
  cursor: pointer;
}
.ledger-project:hover {
  text-decoration: underline;
}
.ledger-amount {
  font-family: var(--font-heading);
  font-size: var(--text-base);
  font-weight: 800;
}
.amount-in {
  color: var(--color-success);
}
.amount-out {
  color: var(--color-danger);
}
.ledger-remark {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-1);
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: var(--spacing-1);
}
.trace-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--color-primary);
}
.ledger-time {
  font-size: 12px;
  color: var(--text-tertiary);
}
.pagination {
  margin-top: var(--spacing-4);
  display: flex;
  justify-content: center;
}
@media (max-width: 768px) {
  .orders-page {
    padding: var(--spacing-3) var(--spacing-2);
  }
  .stats-row {
    grid-template-columns: 1fr;
  }
  .page-header h2 {
    font-size: var(--text-xl);
  }
}
</style>

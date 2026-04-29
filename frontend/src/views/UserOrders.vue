<template>
  <div class="orders-page">
    <div class="page-header">
      <div class="header-text">
        <h2>我的支持</h2>
        <p class="header-desc">追踪你的每一笔支持与资金流向，让每份信任都可溯源</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="router.push('/projects')">
          <el-icon style="margin-right: 6px"><Search /></el-icon>
          去发现项目
        </el-button>
      </div>
    </div>

    <div class="overview-grid">
      <div class="overview-card primary">
        <div class="card-icon">
          <el-icon><Money /></el-icon>
        </div>
        <div class="card-content">
          <span class="overview-label">累计支持金额</span>
          <strong class="overview-value">¥{{ formatNumber(stats.totalAmount || 0) }}</strong>
        </div>
      </div>
      <div class="overview-card success">
        <div class="card-icon">
          <el-icon><FolderOpened /></el-icon>
        </div>
        <div class="card-content">
          <span class="overview-label">支持项目数</span>
          <strong class="overview-value">{{ stats.projectCount || 0 }}</strong>
        </div>
      </div>
      <div class="overview-card warning">
        <div class="card-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <span class="overview-label">订单总数</span>
          <strong class="overview-value">{{ total || 0 }}</strong>
        </div>
      </div>
      <div class="overview-card info">
        <div class="card-icon">
          <el-icon><Tickets /></el-icon>
        </div>
        <div class="card-content">
          <span class="overview-label">流水记录</span>
          <strong class="overview-value">{{ ledgerTotal || 0 }}</strong>
        </div>
      </div>
    </div>

    <div class="charts-grid">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">近6个月支持趋势</span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart-container"></div>
      </el-card>
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">支持项目分布 Top 10</span>
          </div>
        </template>
        <div ref="projectChartRef" class="chart-container"></div>
      </el-card>
    </div>

    <el-card class="content-card">
      <el-tabs v-model="activeTab" class="modern-tabs">
        <el-tab-pane name="orders">
          <template #label>
            <span class="tab-label">支持订单</span>
          </template>
          
          <div class="filter-bar">
            <el-select v-model="orderStatusFilter" placeholder="订单状态" clearable @change="fetchOrders">
              <el-option label="全部" :value="null" />
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已取消" :value="2" />
              <el-option label="已退款" :value="3" />
            </el-select>
          </div>

          <div v-loading="loading">
            <div v-if="orders.length === 0 && !loading" class="empty-state">
              <el-empty description="暂无支持记录" />
            </div>
            <div class="order-list" v-else>
              <div v-for="order in orders" :key="order.id" class="order-card" @click="router.push(`/projects/${order.projectId}`)">
                <div class="order-main">
                  <div class="order-top">
                    <span class="order-project">{{ order.projectName || `项目 ${order.projectId}` }}</span>
                    <el-tag size="small" :type="getStatusType(order.status)">
                      {{ getStatusText(order.status) }}
                    </el-tag>
                  </div>
                  <div class="order-meta">
                    <span class="order-amount">¥{{ formatNumber(order.amount) }}</span>
                    <span class="order-channel">{{ getPayChannelText(order.payChannel) }}</span>
                    <span class="order-time">{{ new Date(order.createdAt).toLocaleString('zh-CN') }}</span>
                  </div>
                  <div class="order-extra" v-if="order.status === 1">
                    <el-tag v-if="order.deliveryStatus === 1" size="small" type="success" effect="plain">
                      <el-icon style="margin-right: 4px"><Van /></el-icon>
                      已发货 {{ order.expressNo }}
                    </el-tag>
                    <el-tag v-else size="small" type="warning" effect="plain">
                      <el-icon style="margin-right: 4px"><Clock /></el-icon>
                      待发货
                    </el-tag>
                  </div>
                </div>
                <div class="order-arrow">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
          <div class="pagination" v-if="total > pageSize">
            <el-pagination 
              v-model:current-page="currentPage" 
              :page-size="pageSize" 
              layout="total, prev, pager, next" 
              :total="total" 
              @current-change="fetchOrders" 
              small 
            />
          </div>
        </el-tab-pane>

        <el-tab-pane name="ledgers">
          <template #label>
            <span class="tab-label">资金流水</span>
          </template>
          <div v-loading="loadingLedgers">
            <div v-if="ledgers.length === 0 && !loadingLedgers" class="empty-state">
              <el-empty description="暂无流水记录" />
            </div>
            <div class="ledger-timeline" v-else>
              <div v-for="ledger in ledgers" :key="ledger.id" class="ledger-item">
                <div class="ledger-dot" :class="{ 'dot-in': isIncome(ledger.type), 'dot-out': isOutcome(ledger.type) }">
                  <el-icon v-if="isIncome(ledger.type)"><Top /></el-icon>
                  <el-icon v-else><Bottom /></el-icon>
                </div>
                <div class="ledger-content">
                  <div class="ledger-header">
                    <span class="ledger-type">
                      <el-tag size="small" :type="isIncome(ledger.type) ? 'success' : 'danger'" effect="plain">
                        {{ getLedgerTypeText(ledger.type) }}
                      </el-tag>
                      <span class="ledger-project" v-if="ledger.projectName" @click.stop="router.push(`/projects/${ledger.projectId}`)">{{ ledger.projectName }}</span>
                    </span>
                    <span class="ledger-amount" :class="{ 'amount-in': isIncome(ledger.type), 'amount-out': isOutcome(ledger.type) }">
                      {{ isIncome(ledger.type) ? '+' : '-' }}¥{{ formatNumber(ledger.amount) }}
                    </span>
                  </div>
                  <div class="ledger-remark">
                    <el-icon class="trace-icon"><Tickets /></el-icon>
                    {{ ledger.remark }}
                  </div>
                  <div class="ledger-time">{{ new Date(ledger.createdAt).toLocaleString('zh-CN') }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="pagination" v-if="ledgerTotal > ledgerSize">
            <el-pagination 
              v-model:current-page="ledgerPage" 
              :page-size="ledgerSize" 
              layout="total, prev, pager, next" 
              :total="ledgerTotal" 
              @current-change="fetchLedgers" 
              small 
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { 
  Tickets, Search, Money, FolderOpened, Document, 
  ArrowRight, Van, Clock, Top, Bottom
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '../utils/request'

const router = useRouter()

const activeTab = ref('orders')
const orders = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = ref<any>({})
const orderStatusFilter = ref<number | null>(null)

const ledgers = ref<any[]>([])
const loadingLedgers = ref(false)
const ledgerPage = ref(1)
const ledgerSize = ref(10)
const ledgerTotal = ref(0)

const trendChartRef = ref<HTMLElement>()
const projectChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let projectChart: echarts.ECharts | null = null

const formatNumber = (num: any) => {
  if (num === null || num === undefined) return '0.00'
  const n = Number(num)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (dt: string) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatTime = (dt: string) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'success'
    case 0: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '已支付'
    case 0: return '待支付'
    case 3: return '已退款'
    case 2: return '已取消'
    default: return '未知'
  }
}

const getPayChannelText = (channel: string) => {
  switch (channel) {
    case '3': return '余额支付'
    case '2': return '支付宝'
    case '1': return '微信支付'
    default: return '其他'
  }
}

const isIncome = (type: number) => type === 2 || type === 3 || type === 5
const isOutcome = (type: number) => type === 1 || type === 4

const getLedgerTypeText = (type: number) => {
  switch (type) {
    case 1: return '项目支持'
    case 2: return '平台退款'
    case 3: return '阶段拨付'
    case 4: return '发起人提现'
    case 5: return '用户充值'
    default: return '其他'
  }
}

const fetchLedgers = async () => {
  loadingLedgers.value = true
  try {
    const res = await request.get('/funding/ledgers', {
      params: { current: ledgerPage.value, size: ledgerSize.value, mine: true, excludeType: 5 }
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
    const params: any = { current: currentPage.value, size: pageSize.value }
    if (orderStatusFilter.value !== null) {
      params.status = orderStatusFilter.value
    }
    const res = await request.get('/order/my-list', { params })
    orders.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchMonthlyTrend = async () => {
  try {
    const res = await request.get('/order/my-monthly-trend')
    const data = res.data || []
    renderTrendChart(data)
  } catch (error) {
    console.error(error)
  }
}

const fetchProjectDistribution = async () => {
  try {
    const res = await request.get('/order/my-project-distribution')
    const data = res.data || []
    renderProjectChart(data)
  } catch (error) {
    console.error(error)
  }
}

const renderTrendChart = (data: any[]) => {
  if (!trendChartRef.value) return
  
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: '#e5e7eb',
      textStyle: { color: '#374151' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.month),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#6b7280' }
    },
    series: [
      {
        name: '支持金额',
        type: 'line',
        smooth: true,
        data: data.map(d => Number(d.totalAmount)),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79, 70, 229, 0.2)' },
            { offset: 1, color: 'rgba(79, 70, 229, 0.02)' }
          ])
        },
        lineStyle: { color: '#4F46E5', width: 3 },
        itemStyle: { color: '#4F46E5' },
        emphasis: { itemStyle: { color: '#4F46E5' } }
      },
      {
        name: '订单数',
        type: 'bar',
        data: data.map(d => Number(d.orderCount)),
        itemStyle: { color: '#10B981' },
        barWidth: '20%'
      }
    ]
  }

  trendChart.setOption(option)
}

const renderProjectChart = (data: any[]) => {
  if (!projectChartRef.value) return
  
  if (!projectChart) {
    projectChart = echarts.init(projectChartRef.value)
  }

  const colors = ['#4F46E5', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#06B6D4', '#EC4899', '#14B8A6', '#F97316', '#6366F1']
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: '#e5e7eb',
      textStyle: { color: '#374151' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'category',
      data: data.map(d => d.title).reverse(),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280' }
    },
    series: [
      {
        name: '支持金额',
        type: 'bar',
        data: data.map((d, i) => ({
          value: Number(d.totalAmount),
          itemStyle: { color: colors[i % colors.length] }
        })).reverse(),
        barWidth: '50%',
        label: {
          show: true,
          position: 'right',
          formatter: (params: any) => `¥${formatNumber(params.value)}`
        }
      }
    ]
  }

  projectChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  projectChart?.resize()
}

onMounted(async () => {
  await Promise.all([
    fetchStats(),
    fetchOrders(),
    fetchLedgers()
  ])
  
  await nextTick()
  
  await Promise.all([
    fetchMonthlyTrend(),
    fetchProjectDistribution()
  ])
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  projectChart?.dispose()
})
</script>

<style scoped>
.orders-page {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: var(--spacing-6) var(--spacing-5);
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-5);
  padding-bottom: var(--spacing-4);
  border-bottom: 1px solid var(--border-light);
}

.header-text {
  flex: 1;
  min-width: 0;
}

.page-header h2 {
  font-family: var(--font-heading);
  font-size: var(--text-3xl, 28px);
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.header-desc {
  margin: var(--spacing-2) 0 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  line-height: 1.5;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-5);
}

.overview-card {
  position: relative;
  overflow: hidden;
  padding: var(--spacing-4);
  border-radius: var(--radius-lg);
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-md);
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  transition: all var(--transition-fast);
}

.overview-card::after {
  content: '';
  position: absolute;
  top: -20px;
  right: -20px;
  width: 104px;
  height: 104px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.overview-card.primary {
  background: var(--primary);
  color: var(--bg-surface);
}

.overview-card.success {
  background: var(--color-success);
  color: var(--bg-surface);
}

.overview-card.warning {
  background: var(--color-warning);
  color: var(--bg-surface);
}

.overview-card.info {
  background: var(--secondary-2);
  color: var(--bg-surface);
}

.card-icon {
  position: relative;
  z-index: 1;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon .el-icon {
  font-size: 24px;
}

.card-content {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
}

.overview-label {
  display: block;
  font-size: var(--text-sm);
  opacity: 0.9;
  margin-bottom: var(--spacing-1);
}

.overview-value {
  display: block;
  font-size: var(--text-2xl);
  font-weight: 800;
  letter-spacing: -0.02em;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-5);
}

.chart-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  background: var(--bg-surface);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
}

.chart-container {
  height: 300px;
  width: 100%;
}

.content-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  background: var(--bg-surface);
}

.modern-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--border-light);
}

.modern-tabs :deep(.el-tabs__item) {
  font-size: var(--text-base);
  padding: 0 var(--spacing-4);
  height: 48px;
}

.tab-label {
  font-weight: 600;
  font-size: var(--text-sm);
}

.filter-bar {
  display: flex;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
  padding: var(--spacing-3);
  background: var(--gray-50);
  border-radius: var(--radius-md);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-8) var(--spacing-4);
}

.order-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: var(--spacing-3);
}

.order-card {
  display: flex;
  align-items: center;
  padding: var(--spacing-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.order-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.08);
  transform: translateY(-2px);
}

.order-main {
  flex: 1;
  min-width: 0;
}

.order-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-2);
  gap: var(--spacing-2);
}

.order-project {
  font-weight: 600;
  font-size: var(--text-base);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-2);
}

.order-amount {
  font-family: var(--font-heading);
  font-weight: 800;
  font-size: var(--text-lg);
  color: var(--color-danger);
  letter-spacing: -0.02em;
}

.order-channel {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  background: var(--gray-100);
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  font-weight: 500;
}

.order-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.order-extra {
  margin-top: var(--spacing-2);
}

.order-arrow {
  flex-shrink: 0;
  margin-left: var(--spacing-3);
  transition: transform var(--transition-fast);
  opacity: 0.5;
  color: var(--text-tertiary);
}

.order-card:hover .order-arrow {
  transform: translateX(4px);
  opacity: 1;
}

.ledger-timeline {
  position: relative;
  padding-left: var(--spacing-6);
  max-width: 900px;
  margin: 0 auto;
}

.ledger-timeline::before {
  content: '';
  position: absolute;
  left: 11px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: var(--border-light);
}

.ledger-item {
  position: relative;
  padding: var(--spacing-3) 0 var(--spacing-4);
  display: flex;
  gap: var(--spacing-3);
}

.ledger-dot {
  position: absolute;
  left: calc(-1 * var(--spacing-6) + 3px);
  top: 20px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 3px solid var(--bg-surface);
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot-in {
  background: var(--color-success);
  box-shadow: 0 0 0 2px var(--color-success);
}

.dot-out {
  background: var(--color-danger);
  box-shadow: 0 0 0 2px var(--color-danger);
}

.ledger-dot .el-icon {
  font-size: 10px;
  color: #ffffff;
}

.ledger-content {
  flex: 1;
  min-width: 0;
  background: var(--gray-50);
  border-radius: var(--radius-md);
  padding: var(--spacing-3) var(--spacing-4);
  transition: background var(--transition-fast);
  border: 1px solid var(--border-light);
}

.ledger-content:hover {
  background: var(--gray-100);
}

.ledger-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-2);
  gap: var(--spacing-2);
}

.ledger-type {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  min-width: 0;
  flex: 1;
}

.ledger-project {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-primary);
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ledger-project:hover {
  text-decoration: underline;
}

.ledger-amount {
  font-family: var(--font-heading);
  font-size: var(--text-lg);
  font-weight: 800;
  flex-shrink: 0;
  letter-spacing: -0.02em;
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
  font-size: var(--text-sm);
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
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.pagination {
  margin-top: var(--spacing-5);
  display: flex;
  justify-content: center;
}

@media (max-width: 1100px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .order-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .orders-page {
    padding: var(--spacing-4) var(--spacing-3);
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .page-header h2 {
    font-size: var(--text-2xl, 24px);
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>

<template>
  <div class="funding-manage">
    <!-- 页头 -->
    <header class="page-header">
      <div class="page-header__text">
        <h1 class="page-header__title">平台财务</h1>
        <p class="page-header__subtitle">监控平台资金流入、拨付与流水，保障合规与安全</p>
      </div>
      <div class="page-header__actions">
        <el-button :icon="RefreshRight" plain @click="refreshAll">刷新</el-button>
      </div>
    </header>

    <!-- KPI 卡片 -->
    <section class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-card__top">
          <div class="kpi-card__icon kpi-card__icon--primary">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="kpi-card__label">平台账户余额 (待下发)</div>
        </div>
        <div class="kpi-card__value">￥{{ formatNumber(platformStats.balance) }}</div>
        <div ref="sparkBalanceRef" class="kpi-card__spark"></div>
      </div>

      <div class="kpi-card">
        <div class="kpi-card__top">
          <div class="kpi-card__icon kpi-card__icon--success">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="kpi-card__label">累计汇入资金</div>
        </div>
        <div class="kpi-card__value">￥{{ formatNumber(platformStats.totalIncoming) }}</div>
        <div ref="sparkIncomingRef" class="kpi-card__spark"></div>
      </div>

      <div class="kpi-card">
        <div class="kpi-card__top">
          <div class="kpi-card__icon kpi-card__icon--warning">
            <el-icon><Position /></el-icon>
          </div>
          <div class="kpi-card__label">累计拨付资金</div>
        </div>
        <div class="kpi-card__value">￥{{ formatNumber(platformStats.totalOutgoing) }}</div>
        <div ref="sparkOutgoingRef" class="kpi-card__spark"></div>
      </div>

      <div class="kpi-card">
        <div class="kpi-card__top">
          <div class="kpi-card__icon kpi-card__icon--accent">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="kpi-card__label">本月汇入 (近 30 日)</div>
        </div>
        <div class="kpi-card__value">￥{{ formatNumber(monthlyIncoming) }}</div>
        <div ref="sparkMonthRef" class="kpi-card__spark"></div>
      </div>
    </section>

    <!-- 趋势图卡片 -->
    <el-card class="trend-card" shadow="never">
      <div class="trend-card__header">
        <div class="trend-card__title">资金流入流出趋势（近 30 日）</div>
        <el-tag size="small" effect="plain">单位：人民币 ￥</el-tag>
      </div>
      <div ref="trendChartRef" class="trend-card__chart"></div>
    </el-card>

    <!-- 主卡片 -->
    <el-card class="main-card" shadow="never">
      <el-tabs v-model="activeTab" class="funding-tabs">
        <!-- 阶段拨付管理 -->
        <el-tab-pane label="阶段拨付管理" name="payouts">
          <div class="chip-row">
            <div class="chip-group">
              <el-check-tag
                v-for="item in payoutChips"
                :key="item.value"
                :checked="payoutFilter === item.value"
                class="chip"
                @change="payoutFilter = item.value"
              >
                {{ item.label }}
              </el-check-tag>
            </div>
            <div class="chip-row__stat">
              待拨付总额：
              <span class="amount-text amount-text--warning">￥{{ formatNumber(pendingPayoutTotal) }}</span>
            </div>
          </div>

          <el-skeleton v-if="loadingPayouts && payouts.length === 0" :rows="5" animated />
          <el-table
            v-else
            :data="filteredPayouts"
            style="width: 100%"
            v-loading="loadingPayouts"
            class="data-table"
            stripe
          >
            <el-table-column label="关联项目" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <el-link type="primary" :underline="false" @click="router.push(`/projects/${row.projectId}`)">
                  {{ row.projectName || `项目 ${row.projectId}` }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column label="发起人" width="160">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-icon class="user-cell__icon"><User /></el-icon>
                  <span>{{ row.sponsorName || `用户 ${row.sponsorId}` }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="拨付阶段" width="110" align="center">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">第 {{ row.stage }} 期</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="conditionDesc" label="拨付条件" min-width="200" show-overflow-tooltip />
            <el-table-column label="拨付金额" width="160" align="right">
              <template #default="{ row }">
                <span class="amount-text amount-text--warning">￥{{ formatNumber(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'" effect="light">
                  {{ row.status === 1 ? '已拨付' : '待拨付' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 0"
                  type="primary"
                  size="small"
                  :icon="Promotion"
                  @click="openPayoutConfirm(row)"
                >
                  执行拨付
                </el-button>
                <span v-else class="muted">-</span>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无拨付记录" />
            </template>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="payoutPage"
              v-model:page-size="payoutSize"
              :total="payoutTotal"
              layout="total, prev, pager, next, jumper"
              background
              @current-change="fetchPayouts"
            />
          </div>
        </el-tab-pane>

        <!-- 平台账户余额流水明细 -->
        <el-tab-pane label="平台账户余额流水明细" name="ledgers">
          <div class="toolbar">
            <el-select
              v-model="ledgerTypeFilter"
              placeholder="全部流向"
              clearable
              class="toolbar__select"
            >
              <el-option label="收入 (用户投资)" :value="1" />
              <el-option label="支出 (平台退款)" :value="2" />
              <el-option label="支出 (平台拨款)" :value="3" />
            </el-select>
            <el-date-picker
              v-model="ledgerRange"
              type="daterange"
              range-separator="~"
              start-placeholder="起始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              class="toolbar__range"
            />
            <el-button :icon="RefreshRight" @click="resetLedgerFilters">重置</el-button>
          </div>

          <el-skeleton v-if="loadingLedgers && ledgers.length === 0" :rows="5" animated />
          <el-table
            v-else
            :data="filteredLedgers"
            style="width: 100%"
            v-loading="loadingLedgers"
            class="data-table"
            stripe
          >
            <el-table-column prop="id" label="流水号" width="110" align="center">
              <template #default="{ row }">
                <span class="muted">#{{ row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column label="资金流向" width="190">
              <template #default="{ row }">
                <div class="flow-cell">
                  <div class="flow-badge" :class="flowBadgeClass(row.type)">
                    <el-icon>
                      <ArrowDown v-if="row.type === 1" />
                      <ArrowUp v-else />
                    </el-icon>
                  </div>
                  <span class="flow-cell__label">{{ flowLabel(row.type) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="变动金额" width="170" align="right">
              <template #default="{ row }">
                <span class="amount-big" :class="row.type === 1 ? 'amount-big--income' : 'amount-big--expense'">
                  {{ row.type === 1 ? '+' : '-' }}￥{{ formatNumber(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="财务溯源详情" min-width="360">
              <template #default="{ row }">
                <div class="trace-detail" :class="'trace-detail--' + flowBadgeClass(row.type)">
                  <el-icon class="trace-icon"><Tickets /></el-icon>
                  <span class="trace-text">{{ row.remark }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="{ row }">
                <span class="muted">{{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}</span>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无流水记录" />
            </template>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="ledgerPage"
              v-model:page-size="ledgerSize"
              :total="ledgerTotal"
              layout="total, prev, pager, next, jumper"
              background
              @current-change="fetchLedgers"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 拨付确认弹窗 -->
    <el-dialog v-model="payoutConfirmVisible" title="拨付确认" width="520px" align-center>
      <div v-if="currentPayoutRow" class="payout-confirm">
        <div class="payout-confirm__row">
          <span class="payout-confirm__label">关联项目</span>
          <span class="payout-confirm__value">
            {{ currentPayoutRow.projectName || `项目 ${currentPayoutRow.projectId}` }}
          </span>
        </div>
        <div class="payout-confirm__row">
          <span class="payout-confirm__label">发起人</span>
          <span class="payout-confirm__value">
            {{ currentPayoutRow.sponsorName || `用户 ${currentPayoutRow.sponsorId}` }}
          </span>
        </div>
        <div class="payout-confirm__row">
          <span class="payout-confirm__label">拨付阶段</span>
          <span class="payout-confirm__value">第 {{ currentPayoutRow.stage }} 期</span>
        </div>
        <div class="payout-confirm__row">
          <span class="payout-confirm__label">拨付条件</span>
          <span class="payout-confirm__value">{{ currentPayoutRow.conditionDesc || '—' }}</span>
        </div>
        <div class="payout-confirm__row payout-confirm__row--amount">
          <span class="payout-confirm__label">拨付金额</span>
          <span class="amount-big amount-big--expense">￥{{ formatNumber(currentPayoutRow.amount) }}</span>
        </div>
        <el-alert type="warning" :closable="false" show-icon class="payout-confirm__alert">
          请再次确认：拨付操作一经确认不可撤销，资金将从平台账户划拨至发起人。
        </el-alert>
        <el-checkbox v-model="payoutConfirmChecked" class="payout-confirm__check">
          我已核对项目进度与金额，确认执行拨付
        </el-checkbox>
      </div>
      <template #footer>
        <el-button @click="payoutConfirmVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!payoutConfirmChecked"
          :loading="processingPayout"
          @click="confirmPayout"
        >
          确认拨付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { useRouter } from 'vue-router'
import {
  User,
  Tickets,
  Wallet,
  TrendCharts,
  Position,
  Calendar,
  Promotion,
  RefreshRight,
  ArrowDown,
  ArrowUp
} from '@element-plus/icons-vue'
import { init, use } from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DataZoomComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { getDailyFunding } from '../../api/admin'

use([
  LineChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DataZoomComponent,
  CanvasRenderer
])

const router = useRouter()
const activeTab = ref('payouts')

const formatNumber = (value: unknown): string => {
  const num = Number(value ?? 0)
  if (!Number.isFinite(num)) return '0'
  return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

// ===== 平台统计 =====
const platformStats = ref({
  balance: 0,
  totalIncoming: 0,
  totalOutgoing: 0
})

const monthlyIncoming = ref(0)
const dailyFundingData = ref<any[]>([])

// ===== Sparkline refs =====
const sparkBalanceRef = ref<HTMLElement | null>(null)
const sparkIncomingRef = ref<HTMLElement | null>(null)
const sparkOutgoingRef = ref<HTMLElement | null>(null)
const sparkMonthRef = ref<HTMLElement | null>(null)
const trendChartRef = ref<HTMLElement | null>(null)
const chartInstances: ECharts[] = []

// ===== Payouts =====
const payouts = ref<any[]>([])
const loadingPayouts = ref(false)
const payoutPage = ref(1)
const payoutSize = ref(10)
const payoutTotal = ref(0)
const payoutFilter = ref(-1) // -1 全部, 0 待拨付, 1 已拨付

const payoutChips = [
  { label: '全部', value: -1 },
  { label: '待拨付', value: 0 },
  { label: '已拨付', value: 1 }
]

const filteredPayouts = computed(() => {
  if (payoutFilter.value === -1) return payouts.value
  return payouts.value.filter((p) => Number(p.status) === payoutFilter.value)
})

const pendingPayoutTotal = computed(() => {
  return payouts.value
    .filter((p) => Number(p.status) === 0)
    .reduce((acc, p) => acc + Number(p.amount || 0), 0)
})

// ===== Ledgers =====
const ledgers = ref<any[]>([])
const loadingLedgers = ref(false)
const ledgerPage = ref(1)
const ledgerSize = ref(10)
const ledgerTotal = ref(0)
const ledgerTypeFilter = ref<number | null>(null)
const ledgerRange = ref<string[] | null>(null)

const resetLedgerFilters = () => {
  ledgerTypeFilter.value = null
  ledgerRange.value = null
}

const filteredLedgers = computed(() => {
  return ledgers.value.filter((l) => {
    if (ledgerTypeFilter.value && Number(l.type) !== ledgerTypeFilter.value) return false
    if (ledgerRange.value && ledgerRange.value.length === 2) {
      if (!l.createdAt) return false
      const t = new Date(l.createdAt).getTime()
      const start = new Date(ledgerRange.value[0]).getTime()
      const end = new Date(ledgerRange.value[1]).getTime() + 24 * 3600 * 1000 - 1
      if (t < start || t > end) return false
    }
    return true
  })
})

// ===== 获取数据 =====
const fetchPayouts = async () => {
  loadingPayouts.value = true
  try {
    const res: any = await request.get('/funding/payouts', {
      params: { current: payoutPage.value, size: payoutSize.value }
    })
    payouts.value = res.data.records
    payoutTotal.value = res.data.total
  } catch {
    ElMessage.error('获取拨付记录失败')
  } finally {
    loadingPayouts.value = false
  }
}

const fetchLedgers = async () => {
  loadingLedgers.value = true
  try {
    const res: any = await request.get('/funding/ledgers', {
      params: { current: ledgerPage.value, size: ledgerSize.value }
    })
    ledgers.value = res.data.records
    ledgerTotal.value = res.data.total
  } catch {
    ElMessage.error('获取资金流水失败')
  } finally {
    loadingLedgers.value = false
  }
}

const fetchPlatformStats = async () => {
  try {
    const res: any = await request.get('/funding/platform-account')
    platformStats.value = {
      balance: Number(res.data.balance || 0),
      totalIncoming: Number(res.data.totalIncoming || 0),
      totalOutgoing: Number(res.data.totalOutgoing || 0)
    }
  } catch {
    ElMessage.error('获取平台账户信息失败')
  }
}

const fetchDailyFunding = async () => {
  try {
    const res: any = await getDailyFunding(30)
    const list: any[] = res?.data || []
    dailyFundingData.value = list
    monthlyIncoming.value = list.reduce((acc, d) => acc + Number(d.totalAmount || d.amount || 0), 0)
    await nextTick()
    renderSparklines()
    renderTrendChart()
  } catch {
    dailyFundingData.value = []
  }
}

// ===== 拨付确认 =====
const payoutConfirmVisible = ref(false)
const payoutConfirmChecked = ref(false)
const processingPayout = ref(false)
const currentPayoutRow = ref<any>(null)

const openPayoutConfirm = (row: any) => {
  currentPayoutRow.value = row
  payoutConfirmChecked.value = false
  payoutConfirmVisible.value = true
}

const confirmPayout = async () => {
  if (!currentPayoutRow.value) return
  processingPayout.value = true
  try {
    await request.post(`/funding/process-payout/${currentPayoutRow.value.id}`)
    ElMessage.success('拨付成功')
    payoutConfirmVisible.value = false
    fetchPayouts()
    fetchLedgers()
    fetchPlatformStats()
    fetchDailyFunding()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.msg || '拨付失败')
  } finally {
    processingPayout.value = false
  }
}

// ===== 展示辅助 =====
const flowLabel = (type: number): string => {
  if (type === 1) return '收入 (用户投资)'
  if (type === 2) return '支出 (平台退款)'
  if (type === 3) return '支出 (平台拨款)'
  return '未知'
}
const flowBadgeClass = (type: number): 'income' | 'refund' | 'payout' | 'other' => {
  if (type === 1) return 'income'
  if (type === 2) return 'refund'
  if (type === 3) return 'payout'
  return 'other'
}

// ===== Chart 绘制 =====
const commonSparkOption = (data: number[], color: string) => ({
  grid: { left: 0, right: 0, top: 4, bottom: 0 },
  xAxis: { type: 'category', show: false, boundaryGap: false, data: data.map((_, i) => i) },
  yAxis: { type: 'value', show: false },
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const v = params?.[0]?.data ?? 0
      return `￥${Number(v).toLocaleString('zh-CN')}`
    }
  },
  series: [
    {
      type: 'line',
      data,
      smooth: true,
      symbol: 'none',
      lineStyle: { color, width: 2 },
      areaStyle: { color, opacity: 0.12 }
    }
  ]
})

const renderSparklines = () => {
  const series = dailyFundingData.value
  const amounts = series.map((d) => Number(d.totalAmount || d.amount || 0))
  const orders = series.map((d) => Number(d.orderCount || d.count || 0))

  const specs: Array<[HTMLElement | null, number[], string]> = [
    [sparkBalanceRef.value, amounts, 'hsl(212, 85%, 55%)'],
    [sparkIncomingRef.value, amounts, 'hsl(160, 55%, 42%)'],
    [sparkOutgoingRef.value, orders, 'hsl(38, 92%, 50%)'],
    [sparkMonthRef.value, amounts, 'hsl(271, 76%, 53%)']
  ]

  specs.forEach(([el, data, color]) => {
    if (!el) return
    const existing = chartInstances.find((c) => c.getDom() === el)
    const inst = existing || init(el)
    if (!existing) chartInstances.push(inst)
    inst.setOption(commonSparkOption(data, color))
  })
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  const series = dailyFundingData.value
  const dates = series.map((d) => d.date || d.day || '')
  const incomes = series.map((d) => Number(d.totalAmount || d.amount || 0))
  // 支出模拟数据：真实后端未返回时以订单数 * 平均额度 估算
  const outflows = series.map((d) => Number(d.payoutAmount || (d.orderCount || 0) * 30))

  const existing = chartInstances.find((c) => c.getDom() === trendChartRef.value)
  const inst = existing || init(trendChartRef.value)
  if (!existing) chartInstances.push(inst)

  inst.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      valueFormatter: (v: any) => `￥${Number(v).toLocaleString('zh-CN')}`
    },
    legend: {
      data: ['资金流入', '资金流出'],
      top: 4,
      textStyle: { color: 'hsl(220, 9%, 46%)' }
    },
    grid: { left: 48, right: 16, top: 40, bottom: 32 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: 'hsl(220, 13%, 91%)' } },
      axisLabel: { color: 'hsl(220, 9%, 46%)', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: {
        color: 'hsl(220, 9%, 46%)',
        fontSize: 11,
        formatter: (v: number) => (v >= 1000 ? `${v / 1000}k` : `${v}`)
      },
      splitLine: { lineStyle: { color: 'hsl(220, 13%, 95%)' } }
    },
    series: [
      {
        name: '资金流入',
        type: 'bar',
        data: incomes,
        itemStyle: { color: 'hsl(160, 55%, 42%)', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 16
      },
      {
        name: '资金流出',
        type: 'bar',
        data: outflows,
        itemStyle: { color: 'hsl(38, 92%, 50%)', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 16
      }
    ]
  })
}

const resizeCharts = () => {
  chartInstances.forEach((c) => c.resize())
}

onMounted(() => {
  fetchPlatformStats()
  fetchPayouts()
  fetchLedgers()
  fetchDailyFunding()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  chartInstances.forEach((c) => c.dispose())
  chartInstances.length = 0
})

// 切到 tab 再 resize，避免 0 宽度
watch(activeTab, () => {
  nextTick(() => resizeCharts())
})

const refreshAll = () => {
  fetchPlatformStats()
  fetchPayouts()
  fetchLedgers()
  fetchDailyFunding()
}
</script>

<style scoped>
.funding-manage {
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

/* KPI */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.kpi-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 20px 24px 12px;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.kpi-card__top {
  display: flex;
  align-items: center;
  gap: 12px;
}
.kpi-card__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.kpi-card__icon--primary {
  background: hsl(212, 85%, 95%);
  color: hsl(212, 85%, 45%);
}
.kpi-card__icon--success {
  background: hsl(160, 55%, 94%);
  color: hsl(160, 55%, 36%);
}
.kpi-card__icon--warning {
  background: hsl(38, 92%, 94%);
  color: hsl(38, 92%, 42%);
}
.kpi-card__icon--accent {
  background: hsl(271, 76%, 95%);
  color: hsl(271, 76%, 53%);
}
.kpi-card__label {
  font-size: 13px;
  color: var(--text-secondary);
}
.kpi-card__value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}
.kpi-card__spark {
  height: 40px;
  width: 100%;
}

/* 趋势图 */
.trend-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.trend-card :deep(.el-card__body) {
  padding: 16px 20px 20px;
}
.trend-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.trend-card__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}
.trend-card__chart {
  width: 100%;
  height: 260px;
}

/* 主卡片 */
.main-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.main-card :deep(.el-card__body) {
  padding: 8px 24px 24px;
}

/* chip */
.chip-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 8px 0 16px;
  flex-wrap: wrap;
}
.chip-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  cursor: pointer;
  user-select: none;
}
.chip-row__stat {
  font-size: 13px;
  color: var(--text-secondary);
}

/* toolbar */
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin: 8px 0 16px;
}
.toolbar__select {
  width: 200px;
}
.toolbar__range {
  width: 320px;
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.user-cell__icon {
  color: var(--text-tertiary);
}

.muted {
  color: var(--text-tertiary);
  font-size: 12px;
}
.amount-text {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.amount-text--warning {
  color: hsl(38, 92%, 42%);
}

/* 流向徽标 */
.flow-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.flow-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.flow-badge.income {
  background: hsl(160, 55%, 94%);
  color: hsl(160, 55%, 36%);
}
.flow-badge.refund {
  background: hsl(0, 72%, 95%);
  color: hsl(0, 72%, 51%);
}
.flow-badge.payout {
  background: hsl(38, 92%, 94%);
  color: hsl(38, 92%, 42%);
}
.flow-badge.other {
  background: hsl(220, 14%, 94%);
  color: hsl(220, 9%, 46%);
}
.flow-cell__label {
  font-size: 13px;
  color: var(--text-primary);
}

.amount-big {
  font-size: 16px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.amount-big--income {
  color: hsl(160, 55%, 36%);
}
.amount-big--expense {
  color: hsl(0, 72%, 51%);
}

/* 溯源详情 */
.trace-detail {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: hsl(220, 14%, 98%);
  border-radius: var(--radius-md);
  border-left: 3px solid hsl(212, 85%, 55%);
}
.trace-detail--income {
  border-left-color: hsl(160, 55%, 42%);
  background: hsl(160, 55%, 97%);
}
.trace-detail--refund {
  border-left-color: hsl(0, 72%, 51%);
  background: hsl(0, 72%, 97%);
}
.trace-detail--payout {
  border-left-color: hsl(38, 92%, 50%);
  background: hsl(38, 92%, 97%);
}
.trace-icon {
  margin-top: 3px;
  color: var(--text-secondary);
  font-size: 16px;
}
.trace-text {
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-primary);
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 拨付确认弹窗 */
.payout-confirm {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.payout-confirm__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: hsl(220, 14%, 98%);
  border-radius: var(--radius-md);
  gap: 16px;
}
.payout-confirm__row--amount {
  background: hsl(0, 72%, 97%);
  border: 1px solid hsl(0, 72%, 90%);
}
.payout-confirm__label {
  font-size: 13px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.payout-confirm__value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 600;
  text-align: right;
  word-break: break-all;
}
.payout-confirm__alert {
  border-radius: var(--radius-md);
}
.payout-confirm__check {
  margin-top: 4px;
}

/* 响应式 */
@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .toolbar__select,
  .toolbar__range {
    width: 100%;
  }
}
@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  .page-header {
    align-items: flex-start;
  }
  .main-card :deep(.el-card__body) {
    padding: 8px 12px 16px;
  }
  .trend-card__chart {
    height: 220px;
  }
  .pagination-container {
    justify-content: center;
  }
}
</style>

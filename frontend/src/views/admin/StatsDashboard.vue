<template>
  <div class="stats-dashboard">
    <!-- ============ 工具条 ============ -->
    <div class="toolbar">
      <div class="toolbar-left">
        <h1 class="page-title">数据大盘</h1>
        <span class="page-subtitle">平台核心指标实时监控</span>
      </div>
      <div class="toolbar-right">
        <el-select
          v-model="rangeDays"
          size="default"
          class="range-select"
          @change="handleRangeChange"
        >
          <el-option label="近 7 天" :value="7" />
          <el-option label="近 30 天" :value="30" />
          <el-option label="近 90 天" :value="90" />
        </el-select>
        <el-button :icon="Refresh" :loading="loading" @click="loadAll">
          刷新
        </el-button>
        <el-button type="primary" :icon="Download" @click="exportData">
          导出 CSV
        </el-button>
      </div>
    </div>

    <!-- ============ KPI 卡片 ============ -->
    <el-skeleton v-if="loading && !overview" :rows="3" animated />
    <div v-else class="kpi-grid">
      <!-- 总筹款金额 -->
      <div class="kpi-card">
        <div class="kpi-head">
          <div class="kpi-icon icon-primary">
            <el-icon><Money /></el-icon>
          </div>
          <span class="kpi-title">总筹款金额</span>
        </div>
        <div class="kpi-main">
          <span class="kpi-value">¥{{ fmt(overview?.totalAmount || 0) }}</span>
        </div>
        <div class="kpi-sub">
          <span class="kpi-sub-text">今日 ¥{{ fmt(overview?.todayAmount || 0) }}</span>
          <span class="kpi-trend" :class="trendClass(overview?.dayOverDay)">
            <el-icon :size="12">
              <CaretTop v-if="(overview?.dayOverDay || 0) >= 0" />
              <CaretBottom v-else />
            </el-icon>
            {{ fmtPct(overview?.dayOverDay) }}
          </span>
        </div>
      </div>

      <!-- 今日订单数 -->
      <div class="kpi-card">
        <div class="kpi-head">
          <div class="kpi-icon icon-success">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <span class="kpi-title">今日订单数</span>
        </div>
        <div class="kpi-main">
          <span class="kpi-value">{{ fmt(overview?.todayOrders || 0) }}</span>
        </div>
        <div class="kpi-sub">
          <span class="kpi-sub-text">
            累计 {{ fmt(overview?.totalOrders || 0) }} 单
          </span>
        </div>
      </div>

      <!-- 在线项目数 -->
      <div class="kpi-card">
        <div class="kpi-head">
          <div class="kpi-icon icon-cyan">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <span class="kpi-title">在线项目</span>
        </div>
        <div class="kpi-main">
          <span class="kpi-value">{{ fmt(overview?.onlineProjects || 0) }}</span>
          <span class="kpi-value-sub">
            / {{ fmt(overview?.totalProjects || 0) }}
          </span>
        </div>
        <div class="kpi-sub">
          <span class="kpi-sub-text">在线率</span>
          <span class="kpi-sub-val">{{ onlineRate }}%</span>
        </div>
      </div>

      <!-- 待审核项目 -->
      <div class="kpi-card">
        <div class="kpi-head">
          <div class="kpi-icon icon-warning">
            <el-icon><Warning /></el-icon>
          </div>
          <span class="kpi-title">待审核项目</span>
          <span
            v-if="(overview?.pendingProjects || 0) > 0"
            class="kpi-dot"
          ></span>
        </div>
        <div class="kpi-main">
          <span class="kpi-value">
            {{ fmt(overview?.pendingProjects || 0) }}
          </span>
        </div>
        <div class="kpi-sub">
          <span class="kpi-sub-text">需要管理员处理</span>
        </div>
      </div>

      <!-- 活跃用户 -->
      <div class="kpi-card">
        <div class="kpi-head">
          <div class="kpi-icon icon-violet">
            <el-icon><User /></el-icon>
          </div>
          <span class="kpi-title">活跃用户</span>
        </div>
        <div class="kpi-main">
          <span class="kpi-value">{{ fmt(overview?.activeUsers || 0) }}</span>
          <span class="kpi-value-sub">
            / {{ fmt(overview?.totalUsers || 0) }}
          </span>
        </div>
        <div class="kpi-sub">
          <span class="kpi-sub-text">活跃率</span>
          <span class="kpi-sub-val">{{ activeRate }}%</span>
        </div>
      </div>

      <!-- 项目达标率 -->
      <div class="kpi-card">
        <div class="kpi-head">
          <div class="kpi-icon icon-pink">
            <el-icon><Trophy /></el-icon>
          </div>
          <span class="kpi-title">项目达标率</span>
        </div>
        <div class="kpi-main with-ring">
          <span class="kpi-value">{{ successRate }}%</span>
          <div class="mini-ring">
            <svg viewBox="0 0 36 36" class="ring-svg">
              <path
                class="ring-bg"
                d="M18 2 a 16 16 0 0 1 0 32 a 16 16 0 0 1 0 -32"
              />
              <path
                class="ring-fg"
                :stroke-dasharray="`${successRate}, 100`"
                d="M18 2 a 16 16 0 0 1 0 32 a 16 16 0 0 1 0 -32"
              />
            </svg>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="kpi-sub-text">
            成功 {{ fmt(overview?.successfulProjects || 0) }} 个
          </span>
        </div>
      </div>
    </div>

    <!-- ============ 图表区第一行 ============ -->
    <div class="chart-row row-2">
      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">日筹款趋势</div>
            <div class="chart-subtitle">
              近 {{ rangeDays }} 天每日筹款金额与订单数
            </div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-show="!loading" ref="fundingChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">支付时段分布</div>
            <div class="chart-subtitle">
              近 {{ rangeDays }} 天 0-23 时订单量
            </div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-show="!loading" ref="hourlyChartRef" class="chart"></div>
      </div>
    </div>

    <!-- ============ 图表区第二行 ============ -->
    <div class="chart-row row-2">
      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">项目状态分布</div>
            <div class="chart-subtitle">平台所有项目的当前状态</div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-show="!loading" ref="statusChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">分类筹款对比</div>
            <div class="chart-subtitle">各分类项目数与筹款金额</div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-show="!loading" ref="categoryChartRef" class="chart"></div>
      </div>
    </div>

    <!-- ============ 图表区第三行 ============ -->
    <div class="chart-row row-2">
      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">新用户注册趋势</div>
            <div class="chart-subtitle">近 {{ rangeDays }} 天每日新增用户</div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-show="!loading" ref="dailyUsersChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">新项目发布趋势</div>
            <div class="chart-subtitle">近 {{ rangeDays }} 天每日新增项目</div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div
          v-show="!loading"
          ref="dailyProjectsChartRef"
          class="chart"
        ></div>
      </div>
    </div>

    <!-- ============ Top 榜单 ============ -->
    <div class="chart-row row-2">
      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">Top 10 筹款项目</div>
            <div class="chart-subtitle">按累计筹款金额排序</div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="6" animated />
        <div v-else class="rank-list">
          <div
            v-for="(item, idx) in topProjects"
            :key="item.id || idx"
            class="rank-item"
          >
            <div class="rank-badge" :class="rankClass(idx)">
              <el-icon v-if="idx < 3" :size="14"><Trophy /></el-icon>
              <span v-else>{{ idx + 1 }}</span>
            </div>
            <div class="rank-main">
              <div class="rank-title">{{ item.title || item.projectTitle || '未命名项目' }}</div>
              <div class="rank-progress">
                <div class="rank-progress-track">
                  <div
                    class="rank-progress-bar"
                    :style="{
                      width: projectProgress(item) + '%',
                      backgroundColor: rankBarColor(idx)
                    }"
                  ></div>
                </div>
                <span class="rank-progress-text">
                  {{ projectProgress(item) }}%
                </span>
              </div>
            </div>
            <div class="rank-metric">
              <div class="rank-metric-main">
                ¥{{ fmt(item.currentAmount || item.totalAmount || 0) }}
              </div>
              <div class="rank-metric-sub">
                {{ fmt(item.supporterCount || item.orderCount || 0) }} 支持者
              </div>
            </div>
          </div>
          <div v-if="topProjects.length === 0" class="rank-empty">
            暂无数据
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-card-head">
          <div>
            <div class="chart-title">Top 10 投资人</div>
            <div class="chart-subtitle">按累计支持金额排序</div>
          </div>
        </div>
        <el-skeleton v-if="loading" :rows="6" animated />
        <div v-else class="rank-list">
          <div
            v-for="(item, idx) in topSupporters"
            :key="item.userId || item.id || idx"
            class="rank-item"
          >
            <div class="rank-badge" :class="rankClass(idx)">
              <el-icon v-if="idx < 3" :size="14"><Medal /></el-icon>
              <span v-else>{{ idx + 1 }}</span>
            </div>
            <el-avatar
              :size="36"
              :src="item.avatar || defaultAvatar"
              class="rank-avatar"
            />
            <div class="rank-main">
              <div class="rank-title">
                {{ item.nickname || item.account || '匿名用户' }}
              </div>
              <div class="rank-sub">
                累计 {{ fmt(item.orderCount || 0) }} 笔订单
              </div>
            </div>
            <div class="rank-metric">
              <div class="rank-metric-main">
                ¥{{ fmt(item.totalAmount || 0) }}
              </div>
            </div>
          </div>
          <div v-if="topSupporters.length === 0" class="rank-empty">
            暂无数据
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Download,
  Refresh,
  Money,
  ShoppingCart,
  FolderOpened,
  Warning,
  User,
  Trophy,
  Medal,
  CaretTop,
  CaretBottom
} from '@element-plus/icons-vue'
import { init, use } from 'echarts/core'
import type { ECharts, EChartsCoreOption } from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  ToolboxComponent,
  MarkLineComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  getOverviewStats,
  getDailyFunding,
  getHourlyDistribution,
  getTopProjects,
  getTopSupporters,
  getProjectStatusDistribution,
  getDailyProjects,
  getDailyUsers
} from '../../api/admin'
import request from '../../utils/request'

use([
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  ToolboxComponent,
  MarkLineComponent,
  CanvasRenderer
])

/* ============ 色板（纯色） ============ */
const PALETTE = [
  '#4F46E5',
  '#10B981',
  '#F59E0B',
  '#EF4444',
  '#8B5CF6',
  '#06B6D4',
  '#EC4899',
  '#14B8A6',
  '#F97316',
  '#6366F1'
]

const STATUS_LABELS: Record<number, string> = {
  0: '待审核',
  1: '筹款中',
  2: '已驳回',
  3: '已取消',
  4: '已下架',
  5: '已成功',
  6: '已失败'
}

const STATUS_COLORS: Record<number, string> = {
  0: '#F59E0B',
  1: '#4F46E5',
  2: '#EF4444',
  3: '#94A3B8',
  4: '#64748B',
  5: '#10B981',
  6: '#EC4899'
}

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 36 36\'><rect width=\'36\' height=\'36\' fill=\'%236366F1\'/><text x=\'50%25\' y=\'55%25\' font-size=\'14\' text-anchor=\'middle\' fill=\'white\' font-family=\'sans-serif\' font-weight=\'700\'>U</text></svg>'

/* ============ 状态 ============ */
const loading = ref(true)
const rangeDays = ref<7 | 30 | 90>(30)

interface OverviewData {
  totalUsers?: number
  activeUsers?: number
  totalProjects?: number
  onlineProjects?: number
  pendingProjects?: number
  successfulProjects?: number
  totalOrders?: number
  todayOrders?: number
  totalAmount?: number
  todayAmount?: number
  dayOverDay?: number
}

const overview = ref<OverviewData | null>(null)
const dailyFunding = ref<any[]>([])
const hourlyData = ref<any[]>([])
const statusData = ref<any[]>([])
const categoryData = ref<any[]>([])
const dailyUsers = ref<any[]>([])
const dailyProjects = ref<any[]>([])
const topProjects = ref<any[]>([])
const topSupporters = ref<any[]>([])

/* ============ 派生 ============ */
const onlineRate = computed(() => {
  const t = overview.value?.totalProjects || 0
  const o = overview.value?.onlineProjects || 0
  return t === 0 ? 0 : Math.round((o / t) * 100)
})

const activeRate = computed(() => {
  const t = overview.value?.totalUsers || 0
  const a = overview.value?.activeUsers || 0
  return t === 0 ? 0 : Math.round((a / t) * 100)
})

const successRate = computed(() => {
  const t = overview.value?.totalProjects || 0
  const s = overview.value?.successfulProjects || 0
  return t === 0 ? 0 : Math.round((s / t) * 100)
})

/* ============ 工具函数 ============ */
const fmt = (n: number | string | undefined | null) => {
  const v = Number(n || 0)
  if (!isFinite(v)) return '0'
  return Math.round(v).toLocaleString()
}

const fmtPct = (n: number | undefined | null) => {
  const v = Number(n || 0)
  const sign = v >= 0 ? '+' : ''
  return `${sign}${v.toFixed(1)}%`
}

const trendClass = (n: number | undefined | null) => {
  const v = Number(n || 0)
  if (v > 0) return 'is-up'
  if (v < 0) return 'is-down'
  return 'is-flat'
}

const rankClass = (idx: number) => {
  if (idx === 0) return 'rank-1'
  if (idx === 1) return 'rank-2'
  if (idx === 2) return 'rank-3'
  return 'rank-n'
}

const rankBarColor = (idx: number) => {
  if (idx === 0) return '#F59E0B'
  if (idx === 1) return '#94A3B8'
  if (idx === 2) return '#F97316'
  return '#4F46E5'
}

const projectProgress = (item: any) => {
  const cur = Number(item.currentAmount || item.totalAmount || 0)
  const tgt = Number(item.targetAmount || 0)
  if (!tgt) return 0
  return Math.min(100, Math.round((cur / tgt) * 100))
}

/* ============ 图表 refs ============ */
const fundingChartRef = ref<HTMLElement | null>(null)
const hourlyChartRef = ref<HTMLElement | null>(null)
const statusChartRef = ref<HTMLElement | null>(null)
const categoryChartRef = ref<HTMLElement | null>(null)
const dailyUsersChartRef = ref<HTMLElement | null>(null)
const dailyProjectsChartRef = ref<HTMLElement | null>(null)

const chartInstances: ECharts[] = []

const disposeCharts = () => {
  chartInstances.forEach((c) => c.dispose())
  chartInstances.length = 0
}

const handleResize = () => {
  chartInstances.forEach((c) => c.resize())
}

/* ============ 公共图表配置 ============ */
const baseAxisLine = {
  lineStyle: { color: '#E2E8F0' }
}
const baseAxisLabel = {
  color: '#64748B',
  fontSize: 11
}
const baseSplitLine = {
  lineStyle: { color: '#F1F5F9', type: 'dashed' as const }
}
const baseTooltip: any = {
  trigger: 'axis',
  backgroundColor: '#FFFFFF',
  borderColor: '#E2E8F0',
  borderWidth: 1,
  padding: [8, 12],
  textStyle: {
    color: '#334155',
    fontSize: 12
  },
  extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.08); border-radius: 8px;'
}

/* ============ 初始化图表 ============ */
const initFundingChart = () => {
  if (!fundingChartRef.value) return
  const chart = init(fundingChartRef.value)
  const dates = dailyFunding.value.map((d: any) => d.date || d.day || '')
  const amounts = dailyFunding.value.map((d: any) =>
    Number(d.amount || d.totalAmount || 0)
  )
  const orders = dailyFunding.value.map((d: any) =>
    Number(d.orderCount || d.count || 0)
  )

  const option: EChartsCoreOption = {
    color: [PALETTE[0], PALETTE[1]],
    tooltip: {
      ...baseTooltip,
      trigger: 'axis',
      axisPointer: { type: 'cross', crossStyle: { color: '#CBD5E1' } }
    },
    legend: {
      data: ['筹款金额', '订单数'],
      right: 0,
      top: 0,
      textStyle: { color: '#64748B', fontSize: 12 },
      icon: 'roundRect',
      itemWidth: 10,
      itemHeight: 10
    },
    grid: { left: 8, right: 8, top: 36, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: baseAxisLine,
      axisLabel: baseAxisLabel,
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: '金额 (¥)',
        nameTextStyle: { color: '#94A3B8', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: baseAxisLabel,
        splitLine: baseSplitLine
      },
      {
        type: 'value',
        name: '订单数',
        nameTextStyle: { color: '#94A3B8', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: baseAxisLabel,
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '筹款金额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        yAxisIndex: 0,
        data: amounts,
        lineStyle: { width: 2.5, color: PALETTE[0] },
        itemStyle: { color: PALETTE[0] },
        areaStyle: { color: 'rgba(79, 70, 229, 0.08)' }
      },
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        yAxisIndex: 1,
        data: orders,
        lineStyle: { width: 2, color: PALETTE[1] },
        itemStyle: { color: PALETTE[1] }
      }
    ]
  }

  chart.setOption(option)
  chartInstances.push(chart)
}

const initHourlyChart = () => {
  if (!hourlyChartRef.value) return
  const chart = init(hourlyChartRef.value)
  const hours = Array.from({ length: 24 }, (_, i) => `${i}时`)
  const map = new Map<number, number>()
  hourlyData.value.forEach((item: any) => {
    const h = Number(item.hour)
    const c = Number(item.orderCount || item.count || 0)
    map.set(h, c)
  })
  const counts = Array.from({ length: 24 }, (_, i) => map.get(i) || 0)
  const maxCount = Math.max(...counts, 1)

  const option: EChartsCoreOption = {
    tooltip: {
      ...baseTooltip,
      trigger: 'axis',
      formatter: (p: any) => {
        const d = p[0]
        return `${d.axisValue}<br/>订单数: <b>${fmt(d.value)}</b>`
      }
    },
    grid: { left: 8, right: 8, top: 16, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: baseAxisLine,
      axisLabel: { ...baseAxisLabel, interval: 2 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: baseAxisLabel,
      splitLine: baseSplitLine
    },
    series: [
      {
        type: 'bar',
        data: counts,
        barWidth: '60%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (params: any) => {
            const v = params.value
            return v === maxCount && v > 0
              ? PALETTE[3]
              : v >= maxCount * 0.6
              ? PALETTE[0]
              : '#C7D2FE'
          }
        }
      }
    ]
  }
  chart.setOption(option)
  chartInstances.push(chart)
}

const initStatusChart = () => {
  if (!statusChartRef.value) return
  const chart = init(statusChartRef.value)

  const data = statusData.value.map((item: any) => {
    const status = Number(item.status)
    return {
      name: STATUS_LABELS[status] || `状态${status}`,
      value: Number(item.count || 0),
      itemStyle: { color: STATUS_COLORS[status] || PALETTE[0] }
    }
  })

  const option: EChartsCoreOption = {
    tooltip: {
      ...baseTooltip,
      trigger: 'item',
      formatter: '{b}: <b>{c}</b> ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 8,
      top: 'middle',
      textStyle: { color: '#475569', fontSize: 12 },
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 10
    },
    series: [
      {
        name: '项目状态',
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          scale: true,
          scaleSize: 8,
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 700,
            color: '#0F172A'
          }
        },
        data
      }
    ]
  }
  chart.setOption(option)
  chartInstances.push(chart)
}

const initCategoryChart = () => {
  if (!categoryChartRef.value) return
  const chart = init(categoryChartRef.value)

  const cats = categoryData.value.map(
    (c: any) => c.categoryName || c.name || '未分类'
  )
  const projectCounts = categoryData.value.map((c: any) =>
    Number(c.projectCount || 0)
  )
  const amounts = categoryData.value.map((c: any) =>
    Number(c.totalAmount || 0)
  )

  const option: EChartsCoreOption = {
    color: [PALETTE[0], PALETTE[1]],
    tooltip: { ...baseTooltip, trigger: 'axis' },
    legend: {
      data: ['项目数', '筹款金额'],
      right: 0,
      top: 0,
      textStyle: { color: '#64748B', fontSize: 12 },
      icon: 'roundRect',
      itemWidth: 10,
      itemHeight: 10
    },
    grid: { left: 8, right: 8, top: 36, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: cats,
      axisLine: baseAxisLine,
      axisLabel: { ...baseAxisLabel, rotate: cats.length > 6 ? 30 : 0 },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: '项目数',
        nameTextStyle: { color: '#94A3B8', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: baseAxisLabel,
        splitLine: baseSplitLine
      },
      {
        type: 'value',
        name: '金额 (¥)',
        nameTextStyle: { color: '#94A3B8', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: baseAxisLabel,
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '项目数',
        type: 'bar',
        yAxisIndex: 0,
        data: projectCounts,
        barWidth: '32%',
        itemStyle: { color: PALETTE[0], borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '筹款金额',
        type: 'bar',
        yAxisIndex: 1,
        data: amounts,
        barWidth: '32%',
        itemStyle: { color: PALETTE[1], borderRadius: [4, 4, 0, 0] }
      }
    ]
  }
  chart.setOption(option)
  chartInstances.push(chart)
}

const initDailyUsersChart = () => {
  if (!dailyUsersChartRef.value) return
  const chart = init(dailyUsersChartRef.value)
  const dates = dailyUsers.value.map((d: any) => d.date || d.day || '')
  const counts = dailyUsers.value.map((d: any) =>
    Number(d.count || d.userCount || 0)
  )

  const option: EChartsCoreOption = {
    tooltip: { ...baseTooltip },
    grid: { left: 8, right: 8, top: 16, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: baseAxisLine,
      axisLabel: baseAxisLabel,
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: baseAxisLabel,
      splitLine: baseSplitLine
    },
    series: [
      {
        name: '新用户',
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbol: 'circle',
        symbolSize: 6,
        data: counts,
        lineStyle: { width: 2.5, color: PALETTE[4] },
        itemStyle: { color: PALETTE[4] },
        areaStyle: { color: 'rgba(139, 92, 246, 0.1)' }
      }
    ]
  }
  chart.setOption(option)
  chartInstances.push(chart)
}

const initDailyProjectsChart = () => {
  if (!dailyProjectsChartRef.value) return
  const chart = init(dailyProjectsChartRef.value)
  const dates = dailyProjects.value.map((d: any) => d.date || d.day || '')
  const counts = dailyProjects.value.map((d: any) =>
    Number(d.count || d.projectCount || 0)
  )

  const option: EChartsCoreOption = {
    tooltip: { ...baseTooltip },
    grid: { left: 8, right: 8, top: 16, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: baseAxisLine,
      axisLabel: baseAxisLabel,
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: baseAxisLabel,
      splitLine: baseSplitLine
    },
    series: [
      {
        name: '新项目',
        type: 'bar',
        data: counts,
        barWidth: '50%',
        itemStyle: { color: PALETTE[5], borderRadius: [4, 4, 0, 0] }
      }
    ]
  }
  chart.setOption(option)
  chartInstances.push(chart)
}

const renderAllCharts = async () => {
  disposeCharts()
  await nextTick()
  initFundingChart()
  initHourlyChart()
  initStatusChart()
  initCategoryChart()
  initDailyUsersChart()
  initDailyProjectsChart()
}

/* ============ 数据加载 ============ */
const unwrap = (res: any) => (res && res.data !== undefined ? res.data : res)

const loadAll = async () => {
  loading.value = true
  try {
    const [
      overviewRes,
      fundingRes,
      hourlyRes,
      statusRes,
      categoryRes,
      usersRes,
      projectsRes,
      topProjectsRes,
      topSupportersRes
    ] = await Promise.allSettled([
      getOverviewStats(),
      getDailyFunding(rangeDays.value),
      getHourlyDistribution(rangeDays.value),
      getProjectStatusDistribution(),
      request.get('/admin/stats/category'),
      getDailyUsers(rangeDays.value),
      getDailyProjects(rangeDays.value),
      getTopProjects(10),
      getTopSupporters(10)
    ])

    overview.value =
      overviewRes.status === 'fulfilled' ? unwrap(overviewRes.value) : null
    dailyFunding.value =
      fundingRes.status === 'fulfilled' ? unwrap(fundingRes.value) || [] : []
    hourlyData.value =
      hourlyRes.status === 'fulfilled' ? unwrap(hourlyRes.value) || [] : []
    statusData.value =
      statusRes.status === 'fulfilled' ? unwrap(statusRes.value) || [] : []
    categoryData.value =
      categoryRes.status === 'fulfilled'
        ? unwrap(categoryRes.value) || []
        : []
    dailyUsers.value =
      usersRes.status === 'fulfilled' ? unwrap(usersRes.value) || [] : []
    dailyProjects.value =
      projectsRes.status === 'fulfilled' ? unwrap(projectsRes.value) || [] : []
    topProjects.value =
      topProjectsRes.status === 'fulfilled'
        ? unwrap(topProjectsRes.value) || []
        : []
    topSupporters.value =
      topSupportersRes.status === 'fulfilled'
        ? unwrap(topSupportersRes.value) || []
        : []
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
    await renderAllCharts()
  }
}

const handleRangeChange = () => {
  loadAll()
}

/* ============ 导出 ============ */
const exportData = () => {
  const lines: string[] = []
  lines.push('指标,数值')
  if (overview.value) {
    lines.push(`总筹款金额,${overview.value.totalAmount || 0}`)
    lines.push(`今日筹款金额,${overview.value.todayAmount || 0}`)
    lines.push(`总订单数,${overview.value.totalOrders || 0}`)
    lines.push(`今日订单数,${overview.value.todayOrders || 0}`)
    lines.push(`总项目数,${overview.value.totalProjects || 0}`)
    lines.push(`在线项目数,${overview.value.onlineProjects || 0}`)
    lines.push(`待审核项目数,${overview.value.pendingProjects || 0}`)
    lines.push(`成功项目数,${overview.value.successfulProjects || 0}`)
    lines.push(`总用户数,${overview.value.totalUsers || 0}`)
    lines.push(`活跃用户数,${overview.value.activeUsers || 0}`)
  }
  lines.push('')
  lines.push('日期,筹款金额,订单数')
  dailyFunding.value.forEach((d: any) => {
    lines.push(
      `${d.date || d.day || ''},${d.amount || d.totalAmount || 0},${
        d.orderCount || d.count || 0
      }`
    )
  })

  const csv = '\uFEFF' + lines.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `数据大盘_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('数据导出成功')
}

/* ============ 生命周期 ============ */
watch([fundingChartRef, hourlyChartRef], () => {
  if (!loading.value) renderAllCharts()
})

onMounted(() => {
  loadAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.stats-dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ============ Toolbar ============ */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
}

.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.range-select {
  width: 120px;
}

.range-select :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
}

/* ============ KPI Grid ============ */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.kpi-card {
  background-color: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: border-color var(--transition-fast),
    box-shadow var(--transition-fast), transform var(--transition-fast);
  min-width: 0;
}

.kpi-card:hover {
  border-color: var(--color-primary-light);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.kpi-head {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}

.kpi-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.icon-primary {
  background-color: #EEF2FF;
  color: #4F46E5;
}
.icon-success {
  background-color: #ECFDF5;
  color: #10B981;
}
.icon-warning {
  background-color: #FFFBEB;
  color: #F59E0B;
}
.icon-cyan {
  background-color: #ECFEFF;
  color: #06B6D4;
}
.icon-violet {
  background-color: #F5F3FF;
  color: #8B5CF6;
}
.icon-pink {
  background-color: #FDF2F8;
  color: #EC4899;
}

.kpi-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.kpi-dot {
  width: 6px;
  height: 6px;
  background-color: var(--color-danger);
  border-radius: 50%;
  margin-left: auto;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.16);
}

.kpi-main {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 2px;
}

.kpi-main.with-ring {
  justify-content: space-between;
  align-items: center;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -0.02em;
}

.kpi-value-sub {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-tertiary);
}

.mini-ring {
  width: 40px;
  height: 40px;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: #F1F5F9;
  stroke-width: 3;
}

.ring-fg {
  fill: none;
  stroke: #EC4899;
  stroke-width: 3;
  stroke-linecap: round;
  transition: stroke-dasharray 0.4s ease;
}

.kpi-sub {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding-top: 6px;
  border-top: 1px solid var(--border-light);
  font-size: 12px;
}

.kpi-sub-text {
  color: var(--text-tertiary);
}

.kpi-sub-val {
  color: var(--text-primary);
  font-weight: 600;
}

.kpi-trend {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
}

.kpi-trend.is-up {
  color: #10B981;
  background-color: #ECFDF5;
}

.kpi-trend.is-down {
  color: #EF4444;
  background-color: #FEF2F2;
}

.kpi-trend.is-flat {
  color: #64748B;
  background-color: #F1F5F9;
}

/* ============ Chart Cards ============ */
.chart-row {
  display: grid;
  gap: 16px;
}

.chart-row.row-2 {
  grid-template-columns: 1fr 1fr;
}

.chart-card {
  background-color: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
}

.chart-subtitle {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.chart {
  height: 280px;
  width: 100%;
}

/* ============ Rank List ============ */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 420px;
  overflow-y: auto;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px;
  border-radius: var(--radius-sm);
  transition: background-color var(--transition-fast);
}

.rank-item:hover {
  background-color: var(--bg-page);
}

.rank-item + .rank-item {
  border-top: 1px solid var(--border-light);
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.rank-badge.rank-1 {
  background-color: #FEF3C7;
  color: #F59E0B;
}
.rank-badge.rank-2 {
  background-color: #F1F5F9;
  color: #64748B;
}
.rank-badge.rank-3 {
  background-color: #FFEDD5;
  color: #F97316;
}
.rank-badge.rank-n {
  background-color: var(--bg-page);
  color: var(--text-tertiary);
  border: 1px solid var(--border-light);
}

.rank-avatar {
  flex-shrink: 0;
}

.rank-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rank-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-sub {
  font-size: 12px;
  color: var(--text-tertiary);
}

.rank-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rank-progress-track {
  flex: 1;
  height: 4px;
  background-color: var(--border-light);
  border-radius: 2px;
  overflow: hidden;
}

.rank-progress-bar {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s ease;
}

.rank-progress-text {
  font-size: 11px;
  color: var(--text-tertiary);
  font-weight: 600;
  width: 36px;
  text-align: right;
}

.rank-metric {
  text-align: right;
  flex-shrink: 0;
}

.rank-metric-main {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.rank-metric-sub {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.rank-empty {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
  padding: 40px 0;
}

/* ============ Responsive ============ */
@media (max-width: 1400px) {
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .chart-row.row-2 {
    grid-template-columns: 1fr;
  }
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  .chart {
    height: 240px;
  }
}
</style>

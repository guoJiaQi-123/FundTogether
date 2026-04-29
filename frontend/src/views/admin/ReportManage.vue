<template>
  <div class="report-manage">
    <!-- 页头 -->
    <header class="page-header">
      <div class="header-content">
        <h2 class="page-title">举报管理</h2>
        <p class="page-desc">审核用户举报，维护平台内容与社区秩序</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="fetchAll" :loading="loading">刷新</el-button>
      </div>
    </header>

    <!-- 4 张统计卡片 -->
    <section class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon warning">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">待处理举报</div>
          <div class="stat-value">{{ stats.pendingCount }}</div>
          <div class="stat-trend">需要尽快核查</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">已处理举报</div>
          <div class="stat-value">{{ stats.doneCount }}</div>
          <div class="stat-trend">历史处理总量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon primary">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">本周新增</div>
          <div class="stat-value">{{ stats.weekCount }}</div>
          <div class="stat-trend">近 7 日举报</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon accent">
          <el-icon><FolderOpened /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">举报项目数</div>
          <div class="stat-value">{{ stats.projectCount }}</div>
          <div class="stat-trend">涉及项目个数</div>
        </div>
      </div>
    </section>

    <!-- 筛选 + 饼图 -->
    <section class="main-grid">
      <!-- 饼图卡片 -->
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title">处理状态分布</div>
          <div class="chart-sub">全部举报的处理进度占比</div>
        </div>
        <div ref="pieRef" class="chart-body"></div>
        <div class="chart-legend">
          <div class="legend-item">
            <span class="dot pending"></span>
            <span class="legend-label">待处理</span>
            <span class="legend-value">{{ stats.pendingCount }}</span>
          </div>
          <div class="legend-item">
            <span class="dot done"></span>
            <span class="legend-label">已处理</span>
            <span class="legend-value">{{ stats.doneCount }}</span>
          </div>
        </div>
      </div>

      <!-- 表格区 -->
      <div class="table-wrapper">
        <div class="filter-bar">
          <div class="filter-chips">
            <span
              v-for="chip in statusChips"
              :key="chip.value"
              class="chip"
              :class="{ active: filter.status === chip.value }"
              @click="setStatus(chip.value)"
            >
              {{ chip.label }}
              <span class="chip-count">{{ chip.count }}</span>
            </span>
          </div>
          <div class="filter-controls">
            <el-date-picker
              v-model="filter.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px"
            />
            <el-input
              v-model="filter.keyword"
              placeholder="搜索举报原因关键词"
              :prefix-icon="Search"
              clearable
              style="width: 220px"
            />
          </div>
        </div>

        <div class="table-card">
          <el-table
            :data="pageRecords"
            v-loading="loading"
            row-key="id"
            stripe
            class="report-table"
          >
            <el-table-column label="举报人" min-width="160">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="32" :src="row.reporterAvatar || ''">
                    {{ (row.reporterName || 'U').charAt(0).toUpperCase() }}
                  </el-avatar>
                  <div class="user-info">
                    <div class="user-name">{{ row.reporterName || '匿名用户' }}</div>
                    <div class="user-sub">ID: {{ row.reporterId ?? '-' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="项目" min-width="200">
              <template #default="{ row }">
                <a class="project-link" @click.prevent="jumpProject(row.projectId)">
                  <el-icon><Link /></el-icon>
                  <span class="project-name">{{ row.projectName || `项目 #${row.projectId}` }}</span>
                </a>
              </template>
            </el-table-column>
            <el-table-column label="举报原因" min-width="260">
              <template #default="{ row }">
                <div class="reason-cell">
                  <span class="reason-text" :class="{ expanded: isExpanded(row.id) }">
                    {{ row.reason || '—' }}
                  </span>
                  <span
                    v-if="(row.reason || '').length > 40"
                    class="expand-btn"
                    @click="toggleExpand(row.id)"
                  >{{ isExpanded(row.id) ? '收起' : '展开' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'" effect="light" round>
                  {{ row.status === 0 ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="举报时间" width="170">
              <template #default="{ row }">
                <span class="time-cell">{{ formatTime(row.createdAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="处理结果" min-width="180">
              <template #default="{ row }">
                <el-tooltip
                  v-if="row.handleResult"
                  :content="row.handleResult"
                  placement="top"
                  :show-after="200"
                >
                  <span class="handle-result">{{ row.handleResult }}</span>
                </el-tooltip>
                <span v-else class="empty-dash">—</span>
              </template>
            </el-table-column>
            <el-table-column label="处理人" width="110">
              <template #default="{ row }">
                <span class="time-cell">{{ row.handlerName || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 0"
                  type="primary"
                  size="small"
                  @click="openHandleDialog(row)"
                >处理</el-button>
                <el-button
                  v-else
                  text
                  type="primary"
                  size="small"
                  @click="openHandleDialog(row, true)"
                >查看</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="empty-state">
                <el-icon :size="48" color="var(--text-tertiary)"><DocumentRemove /></el-icon>
                <div class="empty-text">暂无举报记录</div>
              </div>
            </template>
          </el-table>
          <div class="pagination">
            <el-pagination
              v-model:current-page="current"
              :page-size="10"
              layout="total, prev, pager, next, jumper"
              :total="filteredTotal"
              background
            />
          </div>
        </div>
      </div>
    </section>

    <!-- 处理弹窗 -->
    <el-dialog
      v-model="handleDialog.visible"
      :title="handleDialog.readonly ? '举报处理详情' : '处理举报'"
      width="640px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div v-if="handleDialog.row" class="handle-dialog">
        <!-- 举报人信息 -->
        <div class="panel">
          <div class="panel-title">举报人</div>
          <div class="reporter-row">
            <el-avatar :size="40" :src="handleDialog.row.reporterAvatar || ''">
              {{ (handleDialog.row.reporterName || 'U').charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="reporter-info">
              <div class="name">{{ handleDialog.row.reporterName || '匿名' }}</div>
              <div class="meta">ID: {{ handleDialog.row.reporterId ?? '-' }} · 举报于 {{ formatTime(handleDialog.row.createdAt) }}</div>
            </div>
          </div>
        </div>

        <!-- 项目快照 -->
        <div class="panel">
          <div class="panel-title">项目快照</div>
          <div class="project-snapshot">
            <div class="snapshot-title">{{ handleDialog.row.projectName || `项目 #${handleDialog.row.projectId}` }}</div>
            <el-button link type="primary" @click="jumpProject(handleDialog.row.projectId)">
              前往项目详情
              <el-icon><Right /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 举报内容 -->
        <div class="panel">
          <div class="panel-title">举报内容</div>
          <div class="reason-content">{{ handleDialog.row.reason || '—' }}</div>
        </div>

        <!-- 处理选项 -->
        <template v-if="!handleDialog.readonly">
          <div class="panel">
            <div class="panel-title">处理方式</div>
            <el-radio-group v-model="handleDialog.action" class="action-radios">
              <el-radio value="punish" class="action-radio">
                <div class="radio-title">已处理并惩罚</div>
                <div class="radio-desc">举报属实，将下架项目 / 禁用用户</div>
              </el-radio>
              <el-radio value="reject" class="action-radio">
                <div class="radio-title">已处理（驳回举报）</div>
                <div class="radio-desc">举报不成立，仅记录处理</div>
              </el-radio>
              <el-radio value="pending" class="action-radio">
                <div class="radio-title">待核实</div>
                <div class="radio-desc">需进一步核实信息再处理</div>
              </el-radio>
            </el-radio-group>
          </div>

          <div class="panel">
            <div class="panel-title">处理说明</div>
            <el-input
              v-model="handleDialog.result"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请填写处理说明，将作为记录保存"
            />
          </div>
        </template>

        <template v-else>
          <div class="panel">
            <div class="panel-title">处理结果</div>
            <div class="result-box">{{ handleDialog.row.handleResult || '—' }}</div>
            <div class="handler-meta" v-if="handleDialog.row.handlerName">
              处理人：{{ handleDialog.row.handlerName }}
            </div>
          </div>
        </template>
      </div>
      <template #footer v-if="!handleDialog.readonly">
        <el-button @click="handleDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="handleDialog.loading" @click="confirmHandle">
          提交处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Refresh, Bell, CircleCheck, Calendar, FolderOpened,
  Search, DocumentRemove, Link, Right
} from '@element-plus/icons-vue'
import { init, use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { getReportList, handleReport } from '../../api/admin'

use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

interface ReportRecord {
  id: number
  reporterId?: number
  reporterName?: string
  reporterAvatar?: string
  projectId: number
  projectName?: string
  reason?: string
  status: number
  handleResult?: string
  handlerName?: string
  createdAt: string
}

const router = useRouter()

const records = ref<ReportRecord[]>([])
const loading = ref(false)
const current = ref(1)
const expandedIds = ref<Set<number>>(new Set())

const filter = reactive({
  status: -1 as number,
  dateRange: null as string[] | null,
  keyword: ''
})

const handleDialog = reactive({
  visible: false,
  readonly: false,
  loading: false,
  row: null as ReportRecord | null,
  action: 'reject' as 'punish' | 'reject' | 'pending',
  result: ''
})

const pieRef = ref<HTMLElement | null>(null)
let pieChart: ECharts | null = null

const stats = computed(() => {
  const now = Date.now()
  const weekAgo = now - 7 * 24 * 60 * 60 * 1000
  const pending = records.value.filter(r => r.status === 0)
  const done = records.value.filter(r => r.status !== 0)
  const week = records.value.filter(r => new Date(r.createdAt).getTime() >= weekAgo)
  const projectIds = new Set(records.value.map(r => r.projectId).filter(Boolean))
  return {
    pendingCount: pending.length,
    doneCount: done.length,
    weekCount: week.length,
    projectCount: projectIds.size
  }
})

const statusChips = computed(() => [
  { label: '全部', value: -1, count: records.value.length },
  { label: '待处理', value: 0, count: stats.value.pendingCount },
  { label: '已处理', value: 1, count: stats.value.doneCount }
])

const filteredList = computed(() => {
  let list = records.value
  if (filter.status === 0) list = list.filter(r => r.status === 0)
  if (filter.status === 1) list = list.filter(r => r.status !== 0)
  if (filter.dateRange && filter.dateRange.length === 2) {
    const [start, end] = filter.dateRange
    const s = new Date(start).getTime()
    const e = new Date(end).getTime() + 86400000
    list = list.filter(r => {
      const t = new Date(r.createdAt).getTime()
      return t >= s && t < e
    })
  }
  if (filter.keyword) {
    const k = filter.keyword.trim().toLowerCase()
    list = list.filter(r => (r.reason || '').toLowerCase().includes(k))
  }
  return list
})

const filteredTotal = computed(() => filteredList.value.length)

const pageRecords = computed(() => {
  const start = (current.value - 1) * 10
  return filteredList.value.slice(start, start + 10)
})

const setStatus = (v: number) => {
  filter.status = v
  current.value = 1
}

const isExpanded = (id: number) => expandedIds.value.has(id)
const toggleExpand = (id: number) => {
  if (expandedIds.value.has(id)) expandedIds.value.delete(id)
  else expandedIds.value.add(id)
}

const formatTime = (t?: string) => {
  if (!t) return '—'
  try { return new Date(t).toLocaleString('zh-CN') } catch { return t }
}

const jumpProject = (id: number) => {
  if (!id) return
  router.push(`/project/${id}`)
}

const fetchAll = async () => {
  loading.value = true
  try {
    const res: any = await getReportList({ current: 1, size: 9999 })
    records.value = res.data.records || []
  } catch (error) {
    console.error(error)
    ElMessage.error('获取举报列表失败')
  } finally {
    loading.value = false
  }
}

const openHandleDialog = (row: ReportRecord, readonly = false) => {
  handleDialog.row = row
  handleDialog.readonly = readonly
  handleDialog.action = 'reject'
  handleDialog.result = ''
  handleDialog.visible = true
}

const confirmHandle = async () => {
  if (!handleDialog.row) return
  if (!handleDialog.result.trim()) {
    ElMessage.warning('请填写处理说明')
    return
  }
  const prefix = handleDialog.action === 'punish'
    ? '[已处理并惩罚] '
    : handleDialog.action === 'reject'
      ? '[驳回举报] '
      : '[待核实] '
  handleDialog.loading = true
  try {
    await handleReport(handleDialog.row.id, prefix + handleDialog.result.trim())
    ElMessage.success('举报已处理')
    handleDialog.visible = false
    fetchAll()
  } catch (error) {
    console.error(error)
  } finally {
    handleDialog.loading = false
  }
}

const renderPie = () => {
  if (!pieRef.value) return
  if (!pieChart) pieChart = init(pieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    color: ['hsl(38, 92%, 50%)', 'hsl(160, 55%, 42%)'],
    series: [{
      name: '处理状态',
      type: 'pie',
      radius: ['55%', '78%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'center',
        formatter: () => `{a|${records.value.length}}\n{b|总举报数}`,
        rich: {
          a: { fontSize: 26, fontWeight: 700, color: 'hsl(220, 13%, 18%)' },
          b: { fontSize: 12, color: 'hsl(220, 9%, 46%)', padding: [6, 0, 0, 0] }
        }
      },
      labelLine: { show: false },
      data: [
        { value: stats.value.pendingCount, name: '待处理' },
        { value: stats.value.doneCount, name: '已处理' }
      ]
    }]
  })
}

const handleResize = () => pieChart?.resize()

watch(() => records.value.length, () => nextTick(renderPie))

onMounted(async () => {
  await fetchAll()
  await nextTick()
  renderPie()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  pieChart = null
})
</script>

<style scoped>
.report-manage {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
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
.header-content .page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
.header-content .page-desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--text-secondary);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
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
  font-size: 24px;
  flex-shrink: 0;
}
.stat-icon.primary { background: hsl(221, 83%, 96%); color: var(--color-primary); }
.stat-icon.success { background: hsl(160, 55%, 94%); color: var(--color-success); }
.stat-icon.warning { background: hsl(38, 92%, 94%); color: var(--color-warning); }
.stat-icon.accent  { background: hsl(262, 83%, 96%); color: hsl(262, 83%, 58%); }
.stat-body { flex: 1; min-width: 0; }
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}
.stat-trend {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

/* 主区 */
.main-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: flex-start;
}

/* 饼图卡 */
.chart-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chart-header .chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.chart-header .chart-sub {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}
.chart-body {
  height: 220px;
  width: 100%;
}
.chart-legend {
  display: flex;
  justify-content: space-around;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.legend-item .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.legend-item .dot.pending { background: var(--color-warning); }
.legend-item .dot.done    { background: var(--color-success); }
.legend-label {
  color: var(--text-secondary);
}
.legend-value {
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

/* 表格区 */
.table-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.filter-bar {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 14px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  box-shadow: var(--shadow-sm);
}
.filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}
.chip:hover { border-color: var(--color-primary); color: var(--color-primary); }
.chip.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.chip-count {
  font-size: 12px;
  padding: 0 6px;
  border-radius: 10px;
  background: hsla(0, 0%, 100%, 0.25);
}
.chip:not(.active) .chip-count {
  background: hsl(220, 13%, 95%);
  color: var(--text-tertiary);
}
.filter-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.table-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 8px 8px 16px;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.report-table :deep(.el-table__header) th {
  background: hsl(220, 14%, 98%);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-info { min-width: 0; }
.user-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.2;
}
.user-sub {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.project-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-primary);
  text-decoration: none;
  font-size: 13px;
  cursor: pointer;
}
.project-link:hover { text-decoration: underline; }
.project-name {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reason-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.reason-text {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.reason-text.expanded {
  -webkit-line-clamp: unset;
  display: block;
}
.expand-btn {
  font-size: 12px;
  color: var(--color-primary);
  cursor: pointer;
  align-self: flex-start;
  user-select: none;
}
.expand-btn:hover { text-decoration: underline; }

.time-cell {
  font-size: 13px;
  color: var(--text-secondary);
}
.handle-result {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
  font-size: 13px;
  color: var(--text-primary);
}
.empty-dash { color: var(--text-tertiary); }

.empty-state {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.empty-text {
  color: var(--text-tertiary);
  font-size: 14px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 8px 0;
}

/* 处理弹窗 */
.handle-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel {
  background: hsl(220, 14%, 98%);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 14px 16px;
}
.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.panel-title::before {
  content: '';
  width: 3px;
  height: 12px;
  background: var(--color-primary);
  border-radius: 2px;
}
.reporter-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.reporter-info .name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}
.reporter-info .meta {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}
.project-snapshot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.snapshot-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}
.reason-content {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.action-radios {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.action-radio {
  display: flex;
  align-items: flex-start;
  margin-right: 0;
  padding: 10px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  width: 100%;
  height: auto;
  transition: all 0.2s;
}
.action-radio :deep(.el-radio__label) {
  white-space: normal;
  padding-left: 10px;
}
.action-radio:hover {
  border-color: var(--color-primary);
}
.radio-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
}
.radio-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.result-box {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap;
}
.handler-meta {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 10px;
}

/* 响应式 */
@media (max-width: 1100px) {
  .main-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .report-manage { padding: 16px; }
  .stats-grid { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .filter-bar { flex-direction: column; align-items: flex-start; }
  .filter-controls { width: 100%; flex-direction: column; }
  .filter-controls > * { width: 100% !important; }
}
</style>

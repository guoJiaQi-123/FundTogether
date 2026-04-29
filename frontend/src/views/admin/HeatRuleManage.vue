<template>
  <div class="heat-rule-manage-container">
    <!-- 页头 + 状态摘要 -->
    <header class="page-header">
      <div class="header-content">
        <h2 class="page-title">热度规则管理</h2>
        <p class="page-desc">自定义项目热度计算因素与权重，权衡曝光公平性</p>
      </div>
      <div class="header-actions">
        <el-button :icon="RefreshLeft" @click="handleReset" :loading="resetLoading">
          重置默认
        </el-button>
        <el-button type="primary" :icon="Refresh" @click="handleRecalculate" :loading="calcLoading">
          立即重算热度
        </el-button>
      </div>
    </header>

    <!-- 状态摘要条 -->
    <section class="summary-bar">
      <div class="summary-item">
        <div class="summary-icon success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="summary-body">
          <div class="summary-label">启用规则</div>
          <div class="summary-value">
            <span class="strong">{{ enabledRules.length }}</span>
            <span class="sub">/ {{ rules.length }}</span>
          </div>
        </div>
      </div>
      <div class="divider-v"></div>
      <div class="summary-item">
        <div class="summary-icon primary">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div class="summary-body">
          <div class="summary-label">累计启用权重</div>
          <div class="summary-value">
            <span class="strong">{{ totalEnabledWeight.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <div class="divider-v"></div>
      <div class="summary-item">
        <div class="summary-icon warning">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="summary-body">
          <div class="summary-label">上次重算</div>
          <div class="summary-value">
            <span class="strong-sm">{{ lastRecalcDisplay }}</span>
          </div>
        </div>
      </div>
      <div class="divider-v"></div>
      <div class="summary-item">
        <div class="summary-icon accent">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="summary-body">
          <div class="summary-label">规则总数</div>
          <div class="summary-value">
            <span class="strong">{{ rules.length }}</span>
            <span class="sub">个因素</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 主区：雷达图 + 因素卡片 -->
    <section class="main-grid">
      <div class="radar-card">
        <div class="card-title-row">
          <div>
            <div class="card-title">权重雷达图</div>
            <div class="card-desc">可视化查看各因素权重分布</div>
          </div>
          <el-tag type="info" effect="light" round>{{ enabledRules.length }} 项启用</el-tag>
        </div>
        <div ref="radarRef" class="radar-body" v-loading="loading"></div>
      </div>

      <div class="factors-grid">
        <div
          v-for="rule in rules"
          :key="rule.id"
          class="factor-card"
          :class="{ disabled: rule.enabled === 0 }"
          :style="{ '--accent': factorColor(rule.factorKey) }"
        >
          <div class="factor-head">
            <div class="factor-meta">
              <div class="factor-name">{{ rule.factorName }}</div>
              <el-tag size="small" type="info" effect="plain">{{ rule.factorKey }}</el-tag>
            </div>
            <el-switch
              v-model="rule.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleToggle(rule.id, val as number)"
            />
          </div>
          <div class="factor-weight-row">
            <span class="weight-value">{{ Number(rule.weight).toFixed(2) }}</span>
            <span class="weight-label">权重值</span>
          </div>
          <div class="weight-progress">
            <div
              class="weight-bar"
              :style="{ width: ((Number(rule.weight) / 5) * 100) + '%' }"
            ></div>
          </div>
          <div class="factor-desc">{{ getFactorDetail(rule.factorKey) }}</div>
        </div>
      </div>
    </section>

    <!-- 详细规则表格 -->
    <section class="rule-table-card">
      <div class="card-title-row">
        <div>
          <div class="card-title">规则详情</div>
          <div class="card-desc">拖动滑块精细调整权重，修改会即时生效</div>
        </div>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="max-width: 460px; margin: 0"
        >
          热度 = Σ(因素权重 × 因素值)，系统每小时自动重算
        </el-alert>
      </div>
      <el-table
        :data="rules"
        v-loading="loading"
        class="rule-table"
        :row-class-name="getRowClass"
        stripe
      >
        <el-table-column label="因素" min-width="160">
          <template #default="{ row }">
            <div class="factor-cell">
              <span class="factor-dot" :style="{ background: factorColor(row.factorKey) }"></span>
              <span class="factor-cell-name">{{ row.factorName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="factorKey" label="标识" width="170">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">{{ row.factorKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权重" min-width="260">
          <template #default="{ row }">
            <div class="weight-cell">
              <el-slider
                v-model="row.weight"
                :min="0"
                :max="5"
                :step="0.1"
                :disabled="row.enabled === 0"
                :format-tooltip="(val: number) => val.toFixed(2)"
                @change="(val) => handleWeightChange(row.id, val as number)"
                style="flex: 1"
              />
              <el-input-number
                v-model="row.weight"
                :min="0"
                :max="5"
                :step="0.1"
                :precision="2"
                :disabled="row.enabled === 0"
                size="small"
                style="width: 110px"
                @change="(val) => handleWeightChange(row.id, (val ?? 0) as number)"
              />
            </div>
            <div class="weight-ratio">
              <span>占比</span>
              <span class="ratio-value">{{ ratioOf(row) }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleToggle(row.id, val as number)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220">
          <template #default="{ row }">
            <span class="desc-cell">{{ row.description || getFactorDetail(row.factorKey) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 公式卡片 -->
    <section class="formula-card">
      <div class="card-title-row">
        <div>
          <div class="card-title">热度计算公式</div>
          <div class="card-desc">实时根据启用状态生成</div>
        </div>
      </div>
      <div class="formula-content">
        <div class="formula-line">
          <span class="formula-label">热度 =</span>
          <template v-if="enabledRules.length">
            <span
              v-for="(rule, index) in enabledRules"
              :key="rule.id"
              class="formula-part"
            >
              <span class="formula-weight">{{ Number(rule.weight).toFixed(2) }}</span>
              <span class="formula-times">×</span>
              <span class="formula-factor">{{ rule.factorName }}</span>
              <span v-if="index < enabledRules.length - 1" class="formula-plus">+</span>
            </span>
          </template>
          <span v-else class="formula-empty">暂未启用任何规则</span>
        </div>
        <div class="formula-detail">
          <div
            v-for="rule in rules"
            :key="rule.id"
            class="formula-item"
            :class="{ disabled: rule.enabled === 0 }"
          >
            <el-tag size="small" :type="rule.enabled === 1 ? 'primary' : 'info'">
              {{ rule.factorName }}
            </el-tag>
            <span class="formula-desc">{{ getFactorDetail(rule.factorKey) }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import {
  Refresh, RefreshLeft, CircleCheck, DataAnalysis, Clock, TrendCharts
} from '@element-plus/icons-vue'
import { init, use } from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, RadarComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import {
  getHeatRules,
  updateHeatRuleWeight,
  toggleHeatRule,
  resetHeatRules,
  recalculateHeat
} from '../../api/admin'

use([RadarChart, TooltipComponent, LegendComponent, RadarComponent, CanvasRenderer])

interface HeatRule {
  id: number
  factorKey: string
  factorName: string
  weight: number
  enabled: number
  description?: string
}

const loading = ref(false)
const calcLoading = ref(false)
const resetLoading = ref(false)
const rules = ref<HeatRule[]>([])
const lastRecalcTs = ref<number>(Number(localStorage.getItem('heat_last_recalc') || 0))

const radarRef = ref<HTMLElement | null>(null)
let radarChart: ECharts | null = null

const enabledRules = computed(() => rules.value.filter(r => r.enabled === 1))

const totalEnabledWeight = computed(() =>
  enabledRules.value.reduce((s, r) => s + Number(r.weight || 0), 0)
)

const lastRecalcDisplay = computed(() => {
  if (!lastRecalcTs.value) return '尚未重算'
  try {
    return new Date(lastRecalcTs.value).toLocaleString('zh-CN')
  } catch {
    return '尚未重算'
  }
})

const factorDetails: Record<string, string> = {
  supporter_count: '支持人数 × 权重（直接乘以支持者数量）',
  funding_amount: '筹款金额 ÷ 100 × 权重（每100元计1个单位）',
  funding_progress: '筹款进度% × 权重（完成百分比）',
  favorite_count: '收藏人数 × 权重（直接乘以收藏者数量）',
  comment_count: '评论数量 × 权重（直接乘以评论条数）',
  time_decay: '100 ÷ (1+天数) × 权重（越新分数越高）'
}

const factorColorMap: Record<string, string> = {
  supporter_count: 'hsl(221, 83%, 53%)',
  funding_amount: 'hsl(160, 55%, 42%)',
  funding_progress: 'hsl(38, 92%, 50%)',
  favorite_count: 'hsl(340, 82%, 52%)',
  comment_count: 'hsl(262, 83%, 58%)',
  time_decay: 'hsl(199, 89%, 48%)'
}

const getFactorDetail = (key: string) => factorDetails[key] || ''
const factorColor = (key: string) => factorColorMap[key] || 'hsl(221, 83%, 53%)'

const ratioOf = (row: HeatRule) => {
  if (row.enabled === 0) return '0.0'
  const total = totalEnabledWeight.value
  if (total <= 0) return '0.0'
  return ((Number(row.weight) / total) * 100).toFixed(1)
}

const getRowClass = ({ row }: { row: HeatRule }) =>
  row.enabled === 1 ? 'enabled-row' : 'disabled-row'

const fetchRules = async () => {
  loading.value = true
  try {
    const res: any = await getHeatRules()
    rules.value = (res.data || []).map((r: any) => ({ ...r, weight: Number(r.weight) }))
  } catch (error) {
    console.error(error)
    ElMessage.error('获取热度规则失败')
  } finally {
    loading.value = false
  }
}

const handleWeightChange = async (id: number, weight: number) => {
  try {
    await updateHeatRuleWeight(id, weight)
    renderRadar()
  } catch (error: any) {
    ElMessage.error(error.message || '权重更新失败')
    fetchRules()
  }
}

const handleToggle = async (id: number, enabled: number) => {
  try {
    await toggleHeatRule(id, enabled === 1)
    renderRadar()
  } catch (error: any) {
    ElMessage.error(error.message || '状态切换失败')
    fetchRules()
  }
}

const handleReset = () => {
  ElMessageBox.confirm('确认将所有规则重置为默认值？', '提示', { type: 'warning' })
    .then(async () => {
      resetLoading.value = true
      try {
        await resetHeatRules()
        ElMessage.success('规则已重置为默认值')
        await fetchRules()
        renderRadar()
      } catch (error: any) {
        ElMessage.error(error.message || '重置失败')
      } finally {
        resetLoading.value = false
      }
    })
    .catch(() => {})
}

const handleRecalculate = async () => {
  calcLoading.value = true
  try {
    const res: any = await recalculateHeat()
    const count = res.data?.updatedCount ?? 0
    lastRecalcTs.value = Date.now()
    localStorage.setItem('heat_last_recalc', String(lastRecalcTs.value))
    ElNotification({
      title: '热度重算完成',
      message: `本次共更新了 ${count} 个项目的热度值`,
      type: 'success',
      duration: 4000
    })
  } catch (error: any) {
    ElMessage.error(error.message || '重算失败')
  } finally {
    calcLoading.value = false
  }
}

const renderRadar = () => {
  if (!radarRef.value) return
  if (!radarChart) radarChart = init(radarRef.value)

  const allWeights = rules.value.map(r => Number(r.weight))
  const maxWeight = allWeights.length > 0 ? Math.max(...allWeights, 0.1) : 100
  const indicatorMax = Math.ceil(maxWeight * 1.25)

  const indicator = rules.value.map(r => ({
    name: r.factorName,
    max: indicatorMax
  }))
  const enabledValues = rules.value.map(r => r.enabled === 1 ? Number(r.weight) : 0)
  const disabledValues = rules.value.map(r => r.enabled === 0 ? Number(r.weight) : 0)

  const splitNum = indicatorMax <= 5 ? indicatorMax : 4

  radarChart.setOption({
    tooltip: {
      trigger: 'item',
      valueFormatter: (val: number | number[]) => {
        if (Array.isArray(val)) return val.map(v => v.toFixed(2)).join(', ')
        return val.toFixed(2)
      }
    },
    color: ['hsl(221, 83%, 53%)', 'hsl(220, 9%, 75%)'],
    legend: {
      bottom: 0,
      textStyle: { color: 'hsl(220, 13%, 28%)', fontSize: 12 }
    },
    radar: {
      indicator,
      splitNumber: splitNum,
      axisName: {
        color: 'hsl(220, 13%, 28%)',
        fontSize: 12,
        fontWeight: 600
      },
      splitLine: { lineStyle: { color: 'hsl(220, 13%, 92%)' } },
      splitArea: {
        areaStyle: { color: ['hsl(220, 14%, 99%)', 'hsl(220, 14%, 97%)'] }
      },
      axisLine: { lineStyle: { color: 'hsl(220, 13%, 88%)' } }
    },
    series: [{
      type: 'radar',
      data: [
        {
          value: enabledValues,
          name: '启用权重',
          areaStyle: { color: 'hsla(221, 83%, 53%, 0.2)' },
          lineStyle: { width: 2 },
          itemStyle: { color: 'hsl(221, 83%, 53%)' },
          symbol: 'circle',
          symbolSize: 6
        },
        {
          value: disabledValues,
          name: '禁用权重',
          areaStyle: { color: 'hsla(220, 9%, 75%, 0.12)' },
          lineStyle: { width: 1, type: 'dashed', color: 'hsl(220, 9%, 65%)' },
          itemStyle: { color: 'hsl(220, 9%, 65%)' },
          symbol: 'circle',
          symbolSize: 4
        }
      ]
    }]
  }, true)
}

const handleResize = () => radarChart?.resize()

watch(
  () => rules.value.map(r => `${r.id}:${r.weight}:${r.enabled}`).join(','),
  () => nextTick(renderRadar)
)

onMounted(async () => {
  await fetchRules()
  await nextTick()
  renderRadar()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  radarChart?.dispose()
  radarChart = null
})
</script>

<style scoped>
.heat-rule-manage-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
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
.header-actions {
  display: flex;
  gap: 8px;
}

/* 摘要条 */
.summary-bar {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px 24px;
  display: flex;
  align-items: stretch;
  gap: 16px;
  box-shadow: var(--shadow-sm);
}
.summary-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}
.summary-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.summary-icon.primary { background: hsl(221, 83%, 96%); color: var(--color-primary); }
.summary-icon.success { background: hsl(160, 55%, 94%); color: var(--color-success); }
.summary-icon.warning { background: hsl(38, 92%, 94%); color: var(--color-warning); }
.summary-icon.accent  { background: hsl(262, 83%, 96%); color: hsl(262, 83%, 58%); }
.summary-label {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 2px;
}
.summary-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.summary-value .strong {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}
.summary-value .strong-sm {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.summary-value .sub {
  font-size: 13px;
  color: var(--text-tertiary);
}
.divider-v {
  width: 1px;
  background: var(--border-light);
}

/* 主网格：雷达图 + 因素卡片 */
.main-grid {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 16px;
  align-items: flex-start;
}
.radar-card,
.rule-table-card,
.formula-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.2s, transform 0.2s;
}
.radar-card:hover,
.rule-table-card:hover,
.formula-card:hover {
  box-shadow: var(--shadow-md);
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}
.card-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.radar-body {
  width: 100%;
  height: 360px;
}

/* 因素卡片 */
.factors-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  align-content: start;
}
.factor-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-left: 4px solid var(--accent, var(--color-primary));
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}
.factor-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.factor-card.disabled {
  opacity: 0.6;
  filter: grayscale(0.4);
}
.factor-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}
.factor-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.factor-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}
.factor-weight-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.weight-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}
.weight-label {
  font-size: 12px;
  color: var(--text-tertiary);
}
.weight-progress {
  height: 6px;
  background: hsl(220, 13%, 95%);
  border-radius: 3px;
  overflow: hidden;
}
.weight-bar {
  height: 100%;
  background: var(--accent);
  border-radius: 3px;
  transition: width 0.3s;
}
.factor-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 规则表格 */
.rule-table :deep(.el-table__header) th {
  background: hsl(220, 14%, 98%);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
}
.rule-table :deep(.enabled-row) td:first-child {
  border-left: 3px solid var(--color-success);
}
.rule-table :deep(.disabled-row) {
  opacity: 0.65;
}
.rule-table :deep(.disabled-row) td:first-child {
  border-left: 3px solid var(--border-light);
}
.factor-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.factor-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.factor-cell-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}
.weight-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.weight-ratio {
  margin-top: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--text-tertiary);
}
.ratio-value {
  font-weight: 700;
  color: var(--color-primary);
  font-variant-numeric: tabular-nums;
}
.desc-cell {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 公式 */
.formula-content { padding: 8px 0 0; }
.formula-line {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 16px;
  padding: 16px;
  background: hsl(220, 14%, 98%);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}
.formula-label {
  margin-right: 8px;
  color: var(--text-secondary);
}
.formula-part { display: inline-flex; align-items: center; gap: 4px; }
.formula-weight {
  color: var(--color-primary);
  font-weight: 700;
  font-size: 17px;
  font-variant-numeric: tabular-nums;
}
.formula-times { color: var(--text-tertiary); }
.formula-factor { color: var(--text-primary); }
.formula-plus { color: var(--text-tertiary); margin: 0 4px; }
.formula-empty {
  color: var(--text-tertiary);
  font-weight: 500;
  font-style: italic;
}
.formula-detail {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}
.formula-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  background: hsl(220, 14%, 98%);
  border: 1px solid var(--border-light);
  transition: opacity 0.2s;
}
.formula-item.disabled { opacity: 0.45; }
.formula-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 响应式 */
@media (max-width: 1100px) {
  .main-grid { grid-template-columns: 1fr; }
  .factors-grid { grid-template-columns: repeat(2, 1fr); }
  .summary-bar { flex-wrap: wrap; }
  .summary-item { flex: 1 1 45%; }
  .divider-v { display: none; }
}
@media (max-width: 768px) {
  .heat-rule-manage-container { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .header-actions { width: 100%; justify-content: flex-start; flex-wrap: wrap; }
  .factors-grid { grid-template-columns: 1fr; }
  .summary-bar { padding: 12px 16px; }
  .summary-item { flex: 1 1 100%; }
  .formula-detail { grid-template-columns: 1fr; }
  .card-title-row { flex-direction: column; align-items: flex-start; }
}
</style>

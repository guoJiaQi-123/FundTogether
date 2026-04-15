<template>
  <div class="stats-dashboard">
    <div class="header">
      <h2>数据统计大盘</h2>
      <el-button type="primary" @click="exportData" class="export-btn">
        <el-icon class="el-icon--left"><Download /></el-icon>
        导出数据 (CSV)
      </el-button>
    </div>
    <el-row :gutter="24" class="summary-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-icon blue">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">总用户数</div>
            <div class="card-value">{{ stats.totalUsers }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-icon green">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">总项目数</div>
            <div class="card-value">{{ stats.totalProjects }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-icon purple">
            <el-icon><Money /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">总筹款金额</div>
            <div class="card-value">￥{{ stats.totalAmount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-icon orange">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">成功项目数</div>
            <div class="card-value">{{ stats.successfulProjects }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="24" class="charts-row">
      <el-col :xs="24" :md="10">
        <el-card class="chart-card">
          <div ref="pieChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card class="chart-card">
          <div ref="barChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { init, use, graphic } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import { Download, User, FolderOpened, Money, Trophy } from '@element-plus/icons-vue'

use([PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const pieChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)
const chartInstances: ECharts[] = []

const stats = ref({
  totalUsers: 0,
  totalProjects: 0,
  totalAmount: 0,
  successfulProjects: 0
})

const categoryStats = ref<any[]>([])
const trendStats = ref<any[]>([])

const exportData = () => {
  const csvContent = `data:text/csv;charset=utf-8,类型,数值\n总用户数,${stats.value.totalUsers}\n总项目数,${stats.value.totalProjects}\n总筹款金额,${stats.value.totalAmount}\n成功项目数,${stats.value.successfulProjects}`
  
  const encodedUri = encodeURI(csvContent)
  const link = document.createElement("a")
  link.setAttribute("href", encodedUri)
  link.setAttribute("download", "数据统计导出.csv")
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('数据导出成功')
}

const handleResize = () => {
  chartInstances.forEach(chart => chart.resize())
}

onMounted(async () => {
  try {
    const [statsRes, categoryRes, trendRes] = await Promise.all([
      request.get('/admin/stats'),
      request.get('/admin/stats/category'),
      request.get('/admin/stats/trend')
    ])
    
    if ((statsRes as any).data) {
      stats.value = (statsRes as any).data
    }
    if ((categoryRes as any).data) {
      categoryStats.value = (categoryRes as any).data
    }
    if ((trendRes as any).data) {
      trendStats.value = (trendRes as any).data
    }
  } catch (error) {
    ElMessage.error('获取数据大盘失败')
  } finally {
    initCharts()
  }
})

const initCharts = () => {
  if (pieChartRef.value) {
    const pieChart = init(pieChartRef.value)
    
    const pieData = categoryStats.value.map(item => ({
      name: item.categoryName,
      value: item.projectCount
    }))

    pieChart.setOption({
      title: { text: '项目分类占比', left: 'center', textStyle: { color: '#0F172A', fontWeight: 700 } },
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', textStyle: { fontSize: 12, color: '#475569' } },
      color: ['#4F46E5', '#10B981', '#F59E0B', '#EC4899', '#8B5CF6', '#06B6D4', '#F43F5E', '#14B8A6'],
      series: [
        {
          name: '分类',
          type: 'pie',
          radius: ['45%', '75%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false },
          data: pieData
        }
      ]
    })
    chartInstances.push(pieChart)
  }

  if (barChartRef.value) {
    const barChart = init(barChartRef.value)
    
    // Generate last 6 months labels if no data exists
    const xData = []
    const yData = []
    
    // Sort trend stats just in case
    const sortedTrend = [...trendStats.value].sort((a, b) => a.month.localeCompare(b.month))
    
    // Create a map for quick lookup
    const trendMap = new Map()
    sortedTrend.forEach(item => {
      trendMap.set(item.month, item.totalAmount)
    })

    // Get last 6 months
    const d = new Date()
    for (let i = 5; i >= 0; i--) {
      const monthDate = new Date(d.getFullYear(), d.getMonth() - i, 1)
      const monthStr = monthDate.getFullYear() + '-' + String(monthDate.getMonth() + 1).padStart(2, '0')
      const displayStr = (monthDate.getMonth() + 1) + '月'
      
      xData.push(displayStr)
      yData.push(trendMap.get(monthStr) || 0)
    }

    barChart.setOption({
      title: { text: '近6个月筹款趋势', textStyle: { color: '#0F172A', fontWeight: 700 } },
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: xData,
        axisLine: { lineStyle: { color: '#E2E8F0' } },
        axisLabel: { color: '#475569' }
      },
      yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { color: '#F1F5F9', type: 'dashed' } },
        axisLabel: { color: '#475569' }
      },
      series: [
        {
          data: yData,
          type: 'bar',
          name: '金额 (元)',
          barWidth: '40%',
          itemStyle: { 
            color: new graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#818CF8' },
              { offset: 1, color: '#4F46E5' }
            ]),
            borderRadius: [6, 6, 0, 0]
          }
        }
      ]
    })
    chartInstances.push(barChart)
  }

  window.addEventListener('resize', handleResize)
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(chart => chart.dispose())
  chartInstances.length = 0
})
</script>

<style scoped>
.stats-dashboard {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.02em;
}

.export-btn {
  border-radius: var(--radius-md);
  padding: 10px 20px;
}

.summary-cards {
  margin-bottom: 24px;
}

.summary-cards .el-col {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 20px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.card-icon.blue { background-color: #EEF2FF; color: #4F46E5; }
.card-icon.green { background-color: #ECFDF5; color: #10B981; }
.card-icon.purple { background-color: #FAF5FF; color: #8B5CF6; }
.card-icon.orange { background-color: #FFFBEB; color: #F59E0B; }

.card-content {
  flex: 1;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
}

.charts-row .el-col {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
}

.chart-card:deep(.el-card__body) {
  padding: 24px;
}

.chart {
  height: 380px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .stat-card:deep(.el-card__body) {
    padding: 16px;
    gap: 16px;
  }
  .card-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }
  .card-value {
    font-size: 24px;
  }
  .chart {
    height: 300px;
  }
}
</style>

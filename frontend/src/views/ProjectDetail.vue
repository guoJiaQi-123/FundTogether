<template>
  <div class="detail-container">
    <main class="main-content" v-loading="loading" v-if="project">
      <div class="project-header">
        <div class="media-section">
          <video v-if="project.videoUrl" :src="project.videoUrl" controls class="main-media"></video>
          <img v-else :src="project.coverImage" class="main-media" />
        </div>
        <div class="info-section">
          <h1 class="title">{{ project.title }}</h1>
          <p class="summary">{{ project.summary }}</p>
          
          <div class="stats-box">
            <div class="stat-row">
              <span class="label">已筹金额</span>
              <span class="value highlight">￥{{ project.currentAmount }}</span>
            </div>
            <div class="stat-row">
              <span class="label">目标金额</span>
              <span class="value">￥{{ project.targetAmount }}</span>
            </div>
            <div class="stat-row">
              <span class="label">支持人数</span>
              <span class="value">{{ project.supporterCount }} 人</span>
            </div>
          </div>
          
          <div class="time-info">
            <el-icon><Calendar /></el-icon>
            <span v-if="project.status === 0">筹款未开始 (待审核)</span>
            <span v-else-if="project.status === 1">
              剩余时间：<span class="highlight-time">{{ calculateRemainingDays(project.endTime) }}</span> 天 
              <span class="time-range">({{ formatDate(project.startTime) }} 至 {{ formatDate(project.endTime) }})</span>
            </span>
            <span v-else>筹款已结束 <span class="time-range">({{ formatDate(project.startTime) }} 至 {{ formatDate(project.endTime) }})</span></span>
          </div>

          <div class="status-banner" v-if="project.status === 1 && project.currentAmount >= project.targetAmount">
            <el-alert title="筹款已达标" type="success" description="该项目已成功达到目标金额，您可以继续支持以获取回报。" show-icon :closable="false" />
          </div>
          <div class="status-banner" v-if="project.status === 5">
            <el-alert title="筹款成功" type="success" description="该项目已成功结束筹款。" show-icon :closable="false" />
          </div>
          <div class="status-banner" v-if="project.status === 6">
            <el-alert title="筹款失败" type="error" description="该项目未在规定时间内达到目标金额，筹款失败。" show-icon :closable="false" />
          </div>

          <div class="progress-section">
            <el-progress 
              :percentage="Math.min(100, Math.round((project.currentAmount / project.targetAmount) * 100))" 
              :stroke-width="12"
            />
          </div>

          <div class="action-section">
            <el-button 
              type="primary" 
              size="large" 
              class="support-btn" 
              @click="handleSupportClick"
              :disabled="project.status !== 1"
            >
              {{ project.status === 1 ? '立即支持' : '不在筹款中' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- Funding Chart Section -->
      <div class="funding-progress-section" v-show="hasChartData">
        <div class="section-header">
          <h2>实时筹款进度</h2>
          <p>记录每一份爱心的汇聚</p>
        </div>
        <div class="funding-chart-container">
          <div ref="fundingChartRef" class="funding-chart"></div>
        </div>
      </div>

      <div class="project-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="项目详情" name="detail">
            <div class="html-content" v-html="project.content"></div>
          </el-tab-pane>
          <el-tab-pane label="项目回报" name="rewards">
            <div class="rewards-section" v-loading="rewardsLoading">
              <el-empty v-if="rewards.length === 0" description="暂无回报信息" />
              <div v-else class="rewards-grid">
                <el-card v-for="reward in rewards" :key="reward.id" class="reward-card" shadow="hover">
                  <div class="reward-header">
                    <span class="reward-amount">￥{{ reward.amount }}</span>
                    <span class="reward-limit" v-if="reward.limitCount > 0">
                      限量: {{ reward.currentCount || 0 }} / {{ reward.limitCount }}
                    </span>
                    <span class="reward-limit" v-else>不限量</span>
                  </div>
                  <h3 class="reward-title">支持 ￥{{ reward.amount }}</h3>
                  <p class="reward-content">{{ reward.content }}</p>
                  <div class="reward-footer">
                    <span class="reward-time">预计回报时间: {{ reward.returnTime }} 天后</span>
                    <el-button 
                      type="primary" 
                      @click="handleRewardClick(reward)"
                      :disabled="project.status !== 1 || (reward.limitCount > 0 && reward.currentCount >= reward.limitCount)"
                    >
                      支持 ￥{{ reward.amount }}
                    </el-button>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="项目动态" name="updates">
            <div class="updates-section" v-loading="updatesLoading">
              <el-empty v-if="updates.length === 0" description="暂无动态" />
              <div v-else class="update-item" v-for="update in updates" :key="update.id">
                <div class="update-header">
                  <h3 class="update-title">{{ update.title }}</h3>
                  <span class="update-time">{{ new Date(update.createdAt).toLocaleString() }}</span>
                </div>
                <div class="update-content">{{ update.content }}</div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="评论留言" name="comments">
            <div class="comment-section">
              <div class="comment-input" v-if="userStore.token">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="说点什么吧..."
                  maxlength="500"
                  show-word-limit
                />
                <div class="comment-action">
                  <el-button type="primary" :loading="submittingComment" @click="submitComment(null)">发布评论</el-button>
                </div>
              </div>
              <div v-else class="login-tip">
                请先 <el-button type="text" @click="router.push('/login')">登录</el-button> 后发表评论
              </div>

              <div class="comment-list" v-loading="commentsLoading">
                <div v-if="comments.length === 0" class="empty-comment">
                  <el-empty description="暂无评论，快来抢沙发吧！" />
                </div>
                <div v-else class="comment-item" v-for="item in comments" :key="item.id">
                  <div class="comment-header">
                    <span class="comment-user">用户 ID: {{ item.userId }}</span>
                    <span class="comment-time">{{ new Date(item.createTime).toLocaleString() }}</span>
                  </div>
                  <div class="comment-content">{{ item.content }}</div>
                  <div class="comment-footer">
                    <el-button type="text" size="small" @click="likeComment(item)">
                      <el-icon><Star /></el-icon> 点赞 ({{ item.likeCount }})
                    </el-button>
                    <el-button type="text" size="small" @click="showReplyInput(item)">
                      <el-icon><ChatDotRound /></el-icon> 回复
                    </el-button>
                  </div>
                  
                  <!-- Reply Input -->
                  <div class="reply-input" v-if="activeReplyId === item.id">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 用户 ID: ${item.userId}`"
                    />
                    <div class="reply-action">
                      <el-button size="small" @click="activeReplyId = null">取消</el-button>
                      <el-button type="primary" size="small" :loading="submittingComment" @click="submitComment(item.id)">回复</el-button>
                    </div>
                  </div>

                  <!-- Replies -->
                  <div class="reply-list" v-if="item.replies && item.replies.length > 0">
                    <div class="reply-item" v-for="reply in item.replies" :key="reply.id">
                      <div class="reply-header">
                        <span class="reply-user">用户 ID: {{ reply.userId }}</span>
                        <span class="reply-time">{{ new Date(reply.createTime).toLocaleString() }}</span>
                      </div>
                      <div class="reply-content">{{ reply.content }}</div>
                    </div>
                  </div>
                </div>
                
                <div class="pagination-container" v-if="commentTotal > 0">
                  <el-pagination
                    v-model:current-page="commentPage"
                    :page-size="10"
                    layout="prev, pager, next"
                    :total="commentTotal"
                    @current-change="fetchComments"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </main>

    <el-dialog v-model="showSupportDialog" title="支持项目" width="500px">
      <el-form :model="supportForm" label-width="80px">
        <el-form-item label="支持金额">
          <el-input-number v-model="supportForm.amount" :min="1" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="留言寄语">
          <el-input v-model="supportForm.message" type="textarea" rows="3" placeholder="给发起人留个言吧~" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="supportForm.payChannel">
            <el-radio value="3">余额支付</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSupportDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSupport" :loading="submitting">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ChatDotRound, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { init, use, graphic } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'

use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const project = ref<any>(null)
const hasChartData = ref(false)
const loading = ref(false)
const activeTab = ref('detail')

const showSupportDialog = ref(false)
const submitting = ref(false)
const supportForm = ref({
  amount: 10,
  message: '',
  payChannel: '3'
})

// Rewards related
const rewards = ref<any[]>([])
const rewardsLoading = ref(false)

const fetchRewards = async () => {
  if (!route.params.id) return
  rewardsLoading.value = true
  try {
    const res = await request.get(`/projects/${route.params.id}/rewards`)
    rewards.value = res.data
  } catch (error) {
    console.error('Failed to fetch rewards', error)
  } finally {
    rewardsLoading.value = false
  }
}

const handleRewardClick = (reward: any) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  supportForm.value.amount = reward.amount
  showSupportDialog.value = true
}

// Chart related
const fundingChartRef = ref<HTMLElement | null>(null)
let chartInstance: ECharts | null = null
const handleChartResize = () => chartInstance?.resize()

const initChart = async () => {
  if (!route.params.id) return
  
  try {
    const res = await request.get(`/public/project/${route.params.id}/funding-progress`)
    const data = res.data || []
    
    if (data.length === 0) {
      hasChartData.value = false
      return
    }
    
    hasChartData.value = true
    await nextTick()
    
    if (!fundingChartRef.value) return
    if (!chartInstance) {
      chartInstance = init(fundingChartRef.value)
    }
    
    const dates = data.map((item: any) => item.date)
    const amounts = data.map((item: any) => item.amount)
    
    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>累计金额: ￥{c}',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderColor: '#e4e7ed',
        textStyle: {
          color: '#303133'
        }
      },
      grid: {
        left: '2%',
        right: '2%',
        bottom: '0%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLine: { lineStyle: { color: '#dcdfe6' } },
        axisLabel: { color: '#606266' }
      },
      yAxis: {
        type: 'value',
        name: '金额 (元)',
        splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } },
        axisLabel: { color: '#606266' }
      },
      series: [
        {
          name: '筹款金额',
          type: 'line',
          smooth: true,
          data: amounts,
          symbolSize: 8,
          itemStyle: {
            color: '#409eff',
            borderWidth: 2
          },
          areaStyle: {
            color: new graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.4)' },
              { offset: 1, color: 'rgba(64,158,255,0.0)' }
            ])
          }
        }
      ]
    }
    
    chartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to fetch chart data', error)
  }
}

// Updates related
const updates = ref<any[]>([])
const updatesLoading = ref(false)

const fetchUpdates = async () => {
  if (!route.params.id) return
  updatesLoading.value = true
  try {
    const res = await request.get(`/public/project/${route.params.id}/updates`)
    updates.value = res.data
  } catch (error) {
    console.error('Failed to fetch updates', error)
  } finally {
    updatesLoading.value = false
  }
}

// Comments related
const comments = ref<any[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentsLoading = ref(false)
const commentContent = ref('')
const submittingComment = ref(false)
const activeReplyId = ref<number | null>(null)
const replyContent = ref('')

const fetchComments = async () => {
  if (!route.params.id) return
  commentsLoading.value = true
  try {
    const res = await request.get(`/comment/project/${route.params.id}`, {
      params: { current: commentPage.value, size: 10 }
    })
    comments.value = res.data.records
    commentTotal.value = res.data.total
  } catch (error) {
    console.error('Failed to fetch comments', error)
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async (parentId: number | null) => {
  const content = parentId ? replyContent.value : commentContent.value
  if (!content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  submittingComment.value = true
  try {
    await request.post('/comment/create', {
      projectId: project.value.id,
      parentId: parentId,
      content: content.trim()
    })
    ElMessage.success('评论发布成功')
    if (parentId) {
      replyContent.value = ''
      activeReplyId.value = null
    } else {
      commentContent.value = ''
    }
    fetchComments()
  } catch (error) {
    console.error(error)
  } finally {
    submittingComment.value = false
  }
}

const likeComment = async (item: any) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await request.post(`/comment/${item.id}/like`)
    ElMessage.success('点赞成功')
    item.likeCount = (item.likeCount || 0) + 1
  } catch (error) {
    console.error(error)
  }
}

const showReplyInput = (item: any) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  activeReplyId.value = activeReplyId.value === item.id ? null : item.id
  replyContent.value = ''
}

const handleSupportClick = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  showSupportDialog.value = true
}

const calculateRemainingDays = (endTime: string) => {
  if (!endTime) return 0
  const end = new Date(endTime).getTime()
  const now = new Date().getTime()
  const diff = end - now
  return diff > 0 ? Math.ceil(diff / (1000 * 60 * 60 * 24)) : 0
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const fetchProjectDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/public/project/${route.params.id}`)
    project.value = res.data
    nextTick(() => {
      initChart()
    })
  } catch (error) {
    ElMessage.error('获取项目详情失败')
    router.push('/home')
  } finally {
    loading.value = false
  }
}

const submitSupport = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!supportForm.value.payChannel) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  submitting.value = true
  try {
    await request.post('/order/create', {
      projectId: project.value.id,
      amount: supportForm.value.amount,
      message: supportForm.value.message,
      payChannel: supportForm.value.payChannel
    })
    ElMessage.success('支付成功，感谢您的支持！')
    showSupportDialog.value = false
    fetchProjectDetail() // Refresh data
  } catch (error: any) {
    if (error.message && error.message.includes('余额不足')) {
      ElMessageBox.confirm('余额不足，是否前往充值？', '提示', {
        confirmButtonText: '去充值',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        router.push('/user/account')
      }).catch(() => {})
    } else {
      console.error(error)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleChartResize)
  fetchProjectDetail()
  fetchComments()
  fetchUpdates()
  fetchRewards()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: 60px;
}

.funding-progress-section {
  background: var(--bg-surface);
  padding: 40px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  margin-bottom: 40px;
}

.funding-progress-section .section-header {
  text-align: center;
  margin-bottom: 32px;
}

.funding-progress-section .section-header h2 {
  font-family: var(--font-heading);
  font-size: 28px;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  font-weight: 800;
}

.funding-progress-section .section-header p {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0;
}

.funding-chart-container {
  width: 100%;
  height: 350px;
}

.funding-chart {
  width: 100%;
  height: 100%;
}

/* Rewards Styles */
.rewards-section {
  padding: 32px 0;
}

.rewards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.reward-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-color) !important;
}

.reward-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.reward-amount {
  font-family: var(--font-heading);
  font-size: 32px;
  font-weight: 800;
  color: var(--color-primary);
}

.reward-limit {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-warning);
  background: #FEF3C7;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
}

.reward-title {
  font-family: var(--font-heading);
  font-size: 18px;
  margin: 0 0 12px 0;
  color: var(--text-primary);
}

.reward-content {
  color: var(--text-secondary);
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 24px;
  flex: 1;
}

.reward-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px dashed var(--border-color);
}

.reward-time {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-tertiary);
}

/* Updates Styles */
.updates-section {
  padding: 32px 0;
  min-height: 200px;
}

.update-item {
  padding: 24px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin-bottom: 20px;
  background: var(--bg-surface);
}

.update-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.update-title {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 20px;
  color: var(--text-primary);
}

.update-time {
  color: var(--text-tertiary);
  font-size: 14px;
  font-weight: 500;
}

.update-content {
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 16px;
  white-space: pre-wrap;
}

.main-content {
  max-width: 1400px;
  margin: 40px auto;
  padding: 0 32px;
}

.project-header {
  display: flex;
  gap: 48px;
  background: var(--bg-surface);
  padding: 40px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  margin-bottom: 40px;
}

.media-section {
  flex: 0 0 600px;
}

.main-media {
  width: 100%;
  height: 440px;
  object-fit: cover;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0 0 16px 0;
  font-family: var(--font-heading);
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--text-primary);
  line-height: 1.2;
}

.summary {
  color: var(--text-secondary);
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 32px;
}

.stats-box {
  background: var(--bg-page);
  padding: 24px;
  border-radius: var(--radius-lg);
  margin-bottom: 32px;
  border: 1px solid var(--border-color);
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background-color: #f4f4f5;
  border-radius: 6px;
  color: #606266;
  font-size: 14px;
  margin-bottom: 20px;
}

.highlight-time {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
  margin: 0 4px;
}

.time-range {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.status-banner {
  margin-bottom: 20px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.stat-row:last-child {
  margin-bottom: 0;
}

.stat-row .label {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 15px;
}

.stat-row .value {
  font-family: var(--font-heading);
  font-weight: 700;
  font-size: 18px;
  color: var(--text-primary);
}

.stat-row .highlight {
  color: var(--color-primary);
  font-size: 32px;
  font-weight: 800;
}

.progress-section {
  margin-bottom: 40px;
}

.action-section {
  margin-top: auto;
}

.support-btn {
  width: 100%;
  height: 60px;
  font-size: 20px;
  font-weight: 700;
  border-radius: var(--radius-md);
  letter-spacing: 0.02em;
}

.project-content {
  background: var(--bg-surface);
  padding: 40px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
}

.html-content {
  line-height: 1.8;
  font-size: 16px;
  color: var(--text-primary);
}
.html-content img {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: 24px 0;
}

/* Comment Styles */
.comment-section {
  padding: 32px 0;
}

.comment-input {
  margin-bottom: 40px;
}

.comment-action {
  margin-top: 16px;
  text-align: right;
}

.login-tip {
  text-align: center;
  padding: 32px;
  background: var(--bg-page);
  border-radius: var(--radius-lg);
  margin-bottom: 40px;
  color: var(--text-secondary);
  font-weight: 500;
}

.comment-list {
  min-height: 200px;
}

.comment-item {
  padding: 24px 0;
  border-bottom: 1px solid var(--border-color);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.comment-user {
  font-weight: 700;
  color: var(--text-primary);
}

.comment-time {
  color: var(--text-tertiary);
  font-size: 13px;
}

.comment-content {
  color: var(--text-secondary);
  line-height: 1.6;
  font-size: 15px;
  margin-bottom: 16px;
  word-break: break-all;
}

.comment-footer {
  display: flex;
  gap: 20px;
}

.reply-input {
  margin-top: 20px;
  padding: 20px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
}

.reply-action {
  margin-top: 12px;
  text-align: right;
}

.reply-list {
  margin-top: 20px;
  padding-left: 24px;
  border-left: 3px solid var(--border-color);
}

.reply-item {
  margin-bottom: 20px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.reply-user {
  font-weight: 700;
  font-size: 14px;
  color: var(--text-primary);
}

.reply-time {
  color: var(--text-tertiary);
  font-size: 12px;
}

.reply-content {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

/* Responsive adjustments */
@media (max-width: 1024px) {
  .project-header {
    flex-direction: column;
    gap: 32px;
  }
  .media-section {
    flex: none;
    width: 100%;
  }
  .main-media {
    height: 360px;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin: 20px auto;
    padding: 0 16px;
  }
  .project-header {
    padding: 24px;
  }
  .title {
    font-size: 28px;
  }
  .main-media {
    height: 240px;
  }
  .rewards-grid {
    grid-template-columns: 1fr;
  }
  .project-content {
    padding: 24px;
  }
  .funding-progress-section {
    padding: 24px;
    margin-bottom: 24px;
  }
  .funding-progress-section .section-header h2 {
    font-size: 22px;
  }
  .funding-chart-container {
    height: 250px;
  }
}
</style>

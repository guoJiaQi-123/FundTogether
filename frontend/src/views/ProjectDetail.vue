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

          <div class="progress-section">
            <el-progress 
              :percentage="Math.min(100, Math.round((project.currentAmount / project.targetAmount) * 100))" 
              :stroke-width="12"
            />
          </div>

          <!-- Funding Chart -->
          <div class="funding-chart-container">
            <div ref="fundingChartRef" class="funding-chart"></div>
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
            <el-radio value="1">微信支付</el-radio>
            <el-radio value="2">支付宝</el-radio>
            <el-radio value="3">银行卡</el-radio>
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
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Star, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const project = ref<any>(null)
const loading = ref(false)
const activeTab = ref('detail')

const showSupportDialog = ref(false)
const submitting = ref(false)
const supportForm = ref({
  amount: 10,
  message: '',
  payChannel: '1'
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
let chartInstance: echarts.ECharts | null = null

const initChart = async () => {
  if (!fundingChartRef.value || !route.params.id) return
  
  try {
    const res = await request.get(`/public/project/${route.params.id}/funding-progress`)
    const data = res.data || []
    
    if (data.length === 0) return
    
    chartInstance = echarts.init(fundingChartRef.value)
    
    const dates = data.map((item: any) => item.date)
    const amounts = data.map((item: any) => item.amount)
    
    const option = {
      title: {
        text: '实时筹款进度',
        left: 'center',
        textStyle: {
          fontSize: 16,
          color: '#303133'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>累计金额: ￥{c}'
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
        data: dates
      },
      yAxis: {
        type: 'value',
        name: '金额 (元)'
      },
      series: [
        {
          name: '筹款金额',
          type: 'line',
          smooth: true,
          data: amounts,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.5)' },
              { offset: 1, color: 'rgba(64,158,255,0.1)' }
            ])
          },
          itemStyle: {
            color: '#409eff'
          }
        }
      ]
    }
    
    chartInstance.setOption(option)
    
    window.addEventListener('resize', () => {
      chartInstance?.resize()
    })
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
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProjectDetail()
  fetchComments()
  fetchUpdates()
  fetchRewards()
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.funding-chart-container {
  width: 100%;
  height: 300px;
  margin-bottom: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 10px;
}

.funding-chart {
  width: 100%;
  height: 100%;
}

/* Rewards Styles */
.rewards-section {
  padding: 20px 0;
}

.rewards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.reward-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.reward-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.reward-amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.reward-limit {
  font-size: 13px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.reward-title {
  font-size: 16px;
  margin: 0 0 10px 0;
  color: #303133;
}

.reward-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
  flex: 1;
}

.reward-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.reward-time {
  font-size: 12px;
  color: #909399;
}

/* Updates Styles */
.updates-section {
  padding: 20px 0;
  min-height: 200px;
}

.update-item {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.update-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.update-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.update-time {
  color: #909399;
  font-size: 14px;
}

.update-content {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.main-content {
  max-width: 1200px;
  margin: 30px auto;
}

.project-header {
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  margin-bottom: 30px;
}

.media-section {
  flex: 0 0 600px;
}

.main-media {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0 0 15px 0;
  font-size: 28px;
  color: #303133;
}

.summary {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 30px;
}

.stats-box {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 25px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 16px;
}

.stat-row:last-child {
  margin-bottom: 0;
}

.stat-row .label {
  color: #909399;
}

.stat-row .value {
  font-weight: bold;
  color: #303133;
}

.stat-row .highlight {
  color: #f56c6c;
  font-size: 24px;
}

.progress-section {
  margin-bottom: 40px;
}

.action-section {
  margin-top: auto;
}

.support-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
}

.project-content {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.html-content {
  line-height: 1.8;
  color: #333;
}
.html-content img {
  max-width: 100%;
  border-radius: 4px;
}

/* Comment Styles */
.comment-section {
  padding: 20px 0;
}

.comment-input {
  margin-bottom: 30px;
}

.comment-action {
  margin-top: 10px;
  text-align: right;
}

.login-tip {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 30px;
  color: #909399;
}

.comment-list {
  min-height: 200px;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.comment-user {
  font-weight: bold;
  color: #303133;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
  word-break: break-all;
}

.comment-footer {
  display: flex;
  gap: 15px;
}

.reply-input {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.reply-action {
  margin-top: 10px;
  text-align: right;
}

.reply-list {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid #ebeef5;
}

.reply-item {
  margin-bottom: 15px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.reply-user {
  font-weight: bold;
  font-size: 13px;
  color: #303133;
}

.reply-time {
  color: #909399;
  font-size: 12px;
}

.reply-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
/* Responsive adjustments */
@media (max-width: 992px) {
  .project-header {
    flex-direction: column;
    gap: 20px;
  }
  .media-section {
    flex: none;
    width: 100%;
  }
  .main-media {
    height: 300px;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin: 15px auto;
  }
  .project-header {
    padding: 15px;
  }
  .title {
    font-size: 22px;
  }
  .main-media {
    height: 200px;
  }
  .rewards-grid {
    grid-template-columns: 1fr;
  }
  .project-content {
    padding: 15px;
  }
  .funding-chart-container {
    height: 250px;
  }
}
</style>

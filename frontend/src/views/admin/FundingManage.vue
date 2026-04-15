<template>
  <div class="funding-manage">
    <div class="page-header">
      <h2>平台财务</h2>
    </div>
    
    <el-row :gutter="20" class="platform-stats">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">平台账户余额 (待下发)</div>
          <div class="stat-value balance">¥ {{ platformStats.balance.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">累计汇入资金 (用户支持)</div>
          <div class="stat-value incoming">¥ {{ platformStats.totalIncoming.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">累计拨付资金 (项目阶段)</div>
          <div class="stat-value outgoing">¥ {{ platformStats.totalOutgoing.toFixed(2) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <!-- 阶段拨付管理 -->
      <el-tab-pane label="阶段拨付管理" name="payouts">
        <el-table :data="payouts" style="width: 100%" v-loading="loadingPayouts">
          <el-table-column label="关联项目" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link type="primary" :underline="false" @click="router.push(`/projects/${row.projectId}`)">
                {{ row.projectName || `项目 ${row.projectId}` }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="发起人" width="120">
            <template #default="{ row }">
              <el-tag size="small" type="info" effect="plain">
                <el-icon><User /></el-icon>
                {{ row.sponsorName || `用户 ${row.sponsorId}` }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="拨付阶段" width="120">
            <template #default="scope">
              第 {{ scope.row.stage }} 期
            </template>
          </el-table-column>
          <el-table-column prop="conditionDesc" label="拨付条件" />
          <el-table-column label="拨付金额" width="120">
            <template #default="scope">
              ¥ {{ scope.row.amount }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                {{ scope.row.status === 1 ? '已拨付' : '待拨付' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button 
                v-if="scope.row.status === 0"
                type="primary" 
                size="small" 
                @click="handleProcessPayout(scope.row)"
              >
                执行拨付
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="payoutPage"
            v-model:page-size="payoutSize"
            :total="payoutTotal"
            layout="prev, pager, next"
            @current-change="fetchPayouts"
          />
        </div>
      </el-tab-pane>

      <!-- 平台账户余额流水明细 -->
      <el-tab-pane label="平台账户余额流水明细" name="ledgers">
        <el-table :data="ledgers" style="width: 100%" v-loading="loadingLedgers">
          <el-table-column prop="id" label="流水号" width="100" />
          <el-table-column label="资金流向" width="150">
            <template #default="scope">
              <el-tag v-if="scope.row.type === 1" type="success" effect="dark">收入 (用户投资)</el-tag>
              <el-tag v-else-if="scope.row.type === 2" type="danger" effect="dark">支出 (平台退款)</el-tag>
              <el-tag v-else-if="scope.row.type === 3" type="warning" effect="dark">支出 (平台拨款)</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="变动金额" width="150">
            <template #default="scope">
              <span :style="{ color: scope.row.type === 1 ? '#67C23A' : '#F56C6C', fontWeight: 'bold', fontSize: '16px' }">
                {{ scope.row.type === 1 ? '+' : '-' }} ¥ {{ scope.row.amount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="财务溯源详情 (业务场景与流向)" min-width="350">
            <template #default="scope">
              <div class="trace-detail">
                <el-icon class="trace-icon"><Tickets /></el-icon>
                <span class="trace-text">{{ scope.row.remark }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="scope">
              {{ new Date(scope.row.createdAt).toLocaleString() }}
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="ledgerPage"
            v-model:page-size="ledgerSize"
            :total="ledgerTotal"
            layout="prev, pager, next"
            @current-change="fetchLedgers"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { useRouter } from 'vue-router'
import { User, Tickets } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('payouts')

const platformStats = ref({
  balance: 0,
  totalIncoming: 0,
  totalOutgoing: 0
})

// Payouts state
const payouts = ref<any[]>([])
const loadingPayouts = ref(false)
const payoutPage = ref(1)
const payoutSize = ref(10)
const payoutTotal = ref(0)

// Ledgers state
const ledgers = ref<any[]>([])
const loadingLedgers = ref(false)
const ledgerPage = ref(1)
const ledgerSize = ref(10)
const ledgerTotal = ref(0)

const fetchPayouts = async () => {
  loadingPayouts.value = true
  try {
    const res = await request.get('/funding/payouts', {
      params: { current: payoutPage.value, size: payoutSize.value }
    })
    payouts.value = res.data.records
    payoutTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('获取拨付记录失败')
  } finally {
    loadingPayouts.value = false
  }
}

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

const fetchPlatformStats = async () => {
  try {
    const res = await request.get('/funding/platform-account')
    platformStats.value = {
      balance: Number(res.data.balance || 0),
      totalIncoming: Number(res.data.totalIncoming || 0),
      totalOutgoing: Number(res.data.totalOutgoing || 0)
    }
  } catch (error) {
    ElMessage.error('获取平台账户信息失败')
  }
}

const handleProcessPayout = (row: any) => {
  ElMessageBox.confirm(`确认向发起人拨付 第 ${row.stage} 期资金 ￥${row.amount} 吗？`, '拨付确认', {
    confirmButtonText: '确认拨付',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/funding/process-payout/${row.id}`)
      ElMessage.success('拨付成功')
      fetchPayouts()
      fetchLedgers()
      fetchPlatformStats()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.msg || '拨付失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchPlatformStats()
  fetchPayouts()
  fetchLedgers()
})
</script>

<style scoped>
.funding-manage {
  background: white;
  padding: 24px;
  border-radius: 8px;
}
.platform-stats {
  margin-bottom: 24px;
}
.stat-card {
  text-align: center;
  border-radius: 8px;
}
.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}
.stat-value.balance {
  color: #409EFF;
}
.stat-value.incoming {
  color: #67C23A;
}
.stat-value.outgoing {
  color: #E6A23C;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.trace-detail {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  border-left: 3px solid var(--el-color-primary);
}
.trace-icon {
  margin-top: 3px;
  color: var(--el-color-primary);
  font-size: 16px;
}
.trace-text {
  font-size: 13px;
  line-height: 1.5;
  color: var(--el-text-color-regular);
}
</style>
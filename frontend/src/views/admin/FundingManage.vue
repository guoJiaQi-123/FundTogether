<template>
  <div class="funding-manage">
    <h2>资金与拨付管理</h2>
    
    <el-tabs v-model="activeTab">
      <!-- 阶段拨付管理 -->
      <el-tab-pane label="阶段拨付管理" name="payouts">
        <el-table :data="payouts" style="width: 100%" v-loading="loadingPayouts">
          <el-table-column prop="projectId" label="项目ID" width="100" />
          <el-table-column prop="sponsorId" label="发起人ID" width="100" />
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

      <!-- 资金流水大盘 -->
      <el-tab-pane label="资金流水大盘" name="ledgers">
        <el-table :data="ledgers" style="width: 100%" v-loading="loadingLedgers">
          <el-table-column prop="id" label="流水号" width="100" />
          <el-table-column prop="projectId" label="项目ID" width="100" />
          <el-table-column prop="userId" label="关联用户ID" width="120" />
          <el-table-column label="类型" width="150">
            <template #default="scope">
              <el-tag v-if="scope.row.type === 1" type="success">用户支付</el-tag>
              <el-tag v-else-if="scope.row.type === 2" type="danger">平台退款</el-tag>
              <el-tag v-else-if="scope.row.type === 3" type="warning">阶段拨付</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="scope">
              ¥ {{ scope.row.amount }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注说明" />
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

const activeTab = ref('payouts')

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
    const res = await request.get('/api/funding/payouts', {
      params: { current: payoutPage.value, size: payoutSize.value }
    })
    payouts.value = res.data.data.records
    payoutTotal.value = res.data.data.total
  } catch (error) {
    ElMessage.error('获取拨付记录失败')
  } finally {
    loadingPayouts.value = false
  }
}

const fetchLedgers = async () => {
  loadingLedgers.value = true
  try {
    const res = await request.get('/api/funding/ledgers', {
      params: { current: ledgerPage.value, size: ledgerSize.value }
    })
    ledgers.value = res.data.data.records
    ledgerTotal.value = res.data.data.total
  } catch (error) {
    ElMessage.error('获取资金流水失败')
  } finally {
    loadingLedgers.value = false
  }
}

const handleProcessPayout = (row: any) => {
  ElMessageBox.confirm(`确认向发起人拨付 第 ${row.stage} 期资金 ￥${row.amount} 吗？`, '拨付确认', {
    confirmButtonText: '确认拨付',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/funding/process-payout/${row.id}`)
      ElMessage.success('拨付成功')
      fetchPayouts()
      fetchLedgers()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.msg || '拨付失败')
    }
  }).catch(() => {})
}

onMounted(() => {
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
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
<template>
  <div class="user-container">
    <main class="main-content">
      <div class="page-header">
        <h2>我的支持</h2>
      </div>

      <el-row :gutter="20" class="stats-row" style="margin-bottom: 20px;">
        <el-col :span="12">
          <el-card shadow="hover">
            <div class="stat-box">
              <div class="stat-title">累计支持金额</div>
              <div class="stat-value highlight">￥{{ stats.totalAmount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <div class="stat-box">
              <div class="stat-title">支持项目数</div>
              <div class="stat-value">{{ stats.projectCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="支持订单" name="orders">
            <el-table :data="orders" style="width: 100%" v-loading="loading">
              <el-table-column prop="orderNo" label="订单号" width="200" />
              <el-table-column prop="projectId" label="项目ID" width="100" />
              <el-table-column prop="amount" label="支持金额" width="120">
                <template #default="{ row }">
                  <span style="color: #f56c6c; font-weight: bold;">￥{{ row.amount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="留言" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : (row.status === 0 ? 'warning' : (row.status === 3 ? 'danger' : 'info'))">
                    {{ row.status === 1 ? '已支付' : (row.status === 0 ? '待支付' : (row.status === 3 ? '已退款' : '已取消')) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="payChannel" label="支付渠道" width="120" />
              <el-table-column prop="createdAt" label="支持时间" width="180" />
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="router.push(`/projects/${row.projectId}`)">
                    查看项目
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="fetchOrders"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="资金流水" name="ledgers">
            <el-table :data="ledgers" style="width: 100%" v-loading="loadingLedgers">
              <el-table-column prop="id" label="流水号" width="100" />
              <el-table-column prop="projectId" label="项目ID" width="100" />
              <el-table-column label="类型" width="150">
                <template #default="scope">
                  <el-tag v-if="scope.row.type === 1" type="success">支持支付</el-tag>
                  <el-tag v-else-if="scope.row.type === 2" type="danger">平台退款</el-tag>
                  <el-tag v-else-if="scope.row.type === 3" type="warning">阶段拨付</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="金额" width="120">
                <template #default="scope">
                  <span :style="{ color: scope.row.type === 2 ? '#67c23a' : '#f56c6c' }">
                    {{ scope.row.type === 2 ? '+' : '-' }} ￥{{ scope.row.amount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注说明" />
              <el-table-column prop="createdAt" label="时间" width="180">
                <template #default="scope">
                  {{ new Date(scope.row.createdAt).toLocaleString() }}
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="ledgerTotal"
                :page-size="ledgerSize"
                v-model:current-page="ledgerPage"
                @current-change="fetchLedgers"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()

const activeTab = ref('orders')

const orders = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = ref<any>({})

// Ledgers state
const ledgers = ref<any[]>([])
const loadingLedgers = ref(false)
const ledgerPage = ref(1)
const ledgerSize = ref(10)
const ledgerTotal = ref(0)

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
    const res = await request.get('/order/my-list', {
      params: { current: currentPage.value, size: pageSize.value }
    })
    orders.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchOrders()
  fetchLedgers()
})
</script>

<style scoped>
.user-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  max-width: 1200px;
  margin: 30px auto;
}

.page-header {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.stat-box {
  text-align: center;
  padding: 20px;
}
.stat-title {
  font-size: 16px;
  color: #909399;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-value.highlight {
  color: #f56c6c;
}
</style>

<template>
  <div class="withdrawal-manage">
    <h2>提现审批</h2>
    <el-table :data="records" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="单号" width="200" />
      <el-table-column prop="userName" label="发起人" width="120" />
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }">￥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="type" label="方式" width="100">
        <template #default="{ row }">{{ row.type === 1 ? '支付宝' : '银行卡' }}</template>
      </el-table-column>
      <el-table-column prop="accountNo" label="账号" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 0 ? '待审核' : row.status === 1 ? '已通过' : '已驳回' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row.id)">驳回</el-button>
          </template>
          <span v-else style="color: #909399">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination" v-if="total > 10">
      <el-pagination v-model:current-page="current" :page-size="10" layout="prev, pager, next" :total="total" @current-change="fetchData" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWithdrawalList, approveWithdrawal, rejectWithdrawal } from '../../api/admin'

const records = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getWithdrawalList({ current: current.value, size: 10 })
    records.value = res.data.records
    total.value = res.data.total
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const handleApprove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认通过该提现申请？', '审批确认', { type: 'warning' })
    await approveWithdrawal(id)
    ElMessage.success('审批通过')
    fetchData()
  } catch (error) { if (error !== 'cancel') console.error(error) }
}

const handleReject = async (id: number) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回提现', { inputPattern: /.+/, inputErrorMessage: '驳回原因不能为空' })
    await rejectWithdrawal(id, value)
    ElMessage.success('已驳回')
    fetchData()
  } catch (error) { if (error !== 'cancel') console.error(error) }
}

onMounted(fetchData)
</script>

<style scoped>
.withdrawal-manage { padding: 24px; }
.withdrawal-manage h2 { margin-bottom: 24px; }
.pagination { margin-top: 24px; display: flex; justify-content: center; }
</style>

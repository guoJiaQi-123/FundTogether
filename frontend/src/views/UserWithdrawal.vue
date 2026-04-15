<template>
  <div class="withdrawal-page">
    <h2>提现管理</h2>
    <el-card class="balance-card">
      <div class="balance-info">
        <span class="label">当前余额</span>
        <span class="amount">￥{{ balance }}</span>
      </div>
      <el-button type="primary" @click="showDialog = true">申请提现</el-button>
    </el-card>

    <h3 style="margin-top: 32px">提现记录</h3>
    <el-table :data="records" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="单号" width="200" />
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
      <el-table-column prop="rejectReason" label="驳回原因" />
      <el-table-column prop="createdAt" label="申请时间" width="180">
        <template #default="{ row }">{{ new Date(row.createdAt).toLocaleString() }}</template>
      </el-table-column>
    </el-table>
    <div class="pagination" v-if="total > 10">
      <el-pagination v-model:current-page="current" :page-size="10" layout="prev, pager, next" :total="total" @current-change="fetchRecords" />
    </div>

    <el-dialog v-model="showDialog" title="申请提现" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="提现金额">
          <el-input-number v-model="form.amount" :min="1" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="提现方式">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">支付宝</el-radio>
            <el-radio :value="2">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账户名">
          <el-input v-model="form.accountName" placeholder="请输入账户名" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.accountNo" placeholder="请输入支付宝账号或银行卡号" />
        </el-form-item>
        <el-form-item label="银行名称" v-if="form.type === 2">
          <el-input v-model="form.bankName" placeholder="请输入银行名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBalance, applyWithdrawal, getMyWithdrawals } from '../api/user'

const balance = ref('0.00')
const records = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const total = ref(0)
const showDialog = ref(false)
const submitting = ref(false)
const form = ref({ amount: 100, type: 1, accountName: '', accountNo: '', bankName: '' })

const fetchBalance = async () => {
  try {
    const res = await getBalance()
    balance.value = res.data || '0.00'
  } catch (error) { console.error(error) }
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getMyWithdrawals({ current: current.value, size: 10 })
    records.value = res.data.records
    total.value = res.data.total
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const handleSubmit = async () => {
  if (!form.value.accountNo) { ElMessage.warning('请输入账号'); return }
  submitting.value = true
  try {
    await applyWithdrawal(form.value)
    ElMessage.success('提现申请已提交')
    showDialog.value = false
    fetchBalance()
    fetchRecords()
  } catch (error: any) { console.error(error) }
  finally { submitting.value = false }
}

onMounted(() => { fetchBalance(); fetchRecords() })
</script>

<style scoped>
.withdrawal-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-5) var(--spacing-4);
}
.withdrawal-page h2 {
  font-family: var(--font-heading);
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-3);
}
.balance-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-3);
  transition: all var(--transition-fast);
}
.balance-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}
.balance-info .label {
  color: var(--text-tertiary);
  font-size: var(--text-base);
}
.balance-info .amount {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-extrabold);
  color: var(--color-primary);
}
.pagination {
  margin-top: var(--spacing-3);
  display: flex;
  justify-content: center;
}
</style>

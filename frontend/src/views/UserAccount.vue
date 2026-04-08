<template>
  <div class="user-account">
    <el-card class="account-card">
      <template #header>
        <div class="card-header">
          <span>我的账户</span>
        </div>
      </template>

      <div class="balance-section">
        <div class="balance-label">当前余额</div>
        <div class="balance-amount">
          <span class="currency">¥</span>
          <span class="amount">{{ balance.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" @click="showRechargeDialog = true" class="recharge-btn">
          充值
        </el-button>
      </div>
    </el-card>

    <el-dialog v-model="showRechargeDialog" title="账户充值" width="400px">
      <div class="recharge-dialog-content">
        <div class="recharge-amount-input">
          <span class="label">充值金额：</span>
          <el-input-number 
            v-model="rechargeAmount" 
            :min="1" 
            :max="100000" 
            :precision="2" 
            :step="100"
            style="width: 200px"
          >
            <template #prefix>¥</template>
          </el-input-number>
        </div>
        <div class="payment-method">
          <span class="label">支付方式：</span>
          <div class="alipay-option">
            <el-icon><Platform /></el-icon> 支付宝
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRechargeDialog = false">取消</el-button>
          <el-button type="primary" @click="handleRecharge" :loading="recharging">
            前往支付
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
export default {
  name: 'UserAccount'
}
</script>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Platform } from '@element-plus/icons-vue'
import request from '../utils/request'

const balance = ref(0)
const showRechargeDialog = ref(false)
const rechargeAmount = ref(100)
const recharging = ref(false)

const fetchBalance = async () => {
  try {
    const res: any = await request.get('/user/account')
    if (res.code === 200) {
      balance.value = res.data
    } else {
      ElMessage.error(res.message || '获取余额失败')
    }
  } catch (error) {
    console.error('Fetch balance error:', error)
    ElMessage.error('获取余额失败')
  }
}

const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  
  recharging.value = true
  try {
    const res: any = await request.post('/user/account/recharge', {
      amount: rechargeAmount.value
    })
    
    if (res.code === 200 && res.data) {
      // res.data is the Alipay form HTML
      const formHtml = res.data
      const div = document.createElement('div')
      div.innerHTML = formHtml
      document.body.appendChild(div)
      document.forms[document.forms.length - 1].submit()
    } else {
      ElMessage.error(res.message || '发起充值失败')
    }
  } catch (error) {
    console.error('Recharge error:', error)
    ElMessage.error('发起充值失败')
  } finally {
    recharging.value = false
  }
}

onMounted(() => {
  fetchBalance()
})
</script>

<style scoped>
.user-account {
  padding: 20px;
}

.account-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.balance-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
}

.balance-label {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.balance-amount {
  color: #f56c6c;
  margin-bottom: 30px;
}

.currency {
  font-size: 24px;
  margin-right: 5px;
}

.amount {
  font-size: 48px;
  font-weight: bold;
}

.recharge-btn {
  width: 200px;
}

.recharge-dialog-content {
  padding: 20px 0;
}

.recharge-amount-input, .payment-method {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.label {
  width: 80px;
  color: #606266;
}

.alipay-option {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 15px;
  border: 1px solid #409eff;
  color: #409eff;
  border-radius: 4px;
  background-color: #ecf5ff;
}
</style>
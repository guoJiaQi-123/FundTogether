<template>
  <div class="withdrawal-manage">
    <!-- 页头 -->
    <header class="page-header">
      <div class="header-content">
        <h2 class="page-title">提现审批</h2>
        <p class="page-desc">审核用户提现申请，保障资金安全合规下发</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="fetchAll" :loading="loading">刷新</el-button>
      </div>
    </header>

    <!-- 4 张统计卡片 -->
    <section class="stats-grid">
      <div class="stat-card" :class="{ 'is-warning': true }">
        <div class="stat-icon warning">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">待审核提现</div>
          <div class="stat-value">{{ stats.pendingCount }}</div>
          <div class="stat-trend">笔申请等待处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon primary">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">累计提现笔数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
          <div class="stat-trend">全部历史申请</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">累计已下发</div>
          <div class="stat-value">￥{{ formatAmount(stats.paidAmount) }}</div>
          <div class="stat-trend">成功下发金额</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">
          <el-icon><Wallet /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">待下发金额</div>
          <div class="stat-value">￥{{ formatAmount(stats.pendingAmount) }}</div>
          <div class="stat-trend">待审核资金池</div>
        </div>
      </div>
    </section>

    <!-- 筛选工具条 -->
    <section class="filter-bar">
      <div class="filter-chips">
        <span
          v-for="chip in statusChips"
          :key="chip.value"
          class="chip"
          :class="{ active: filter.status === chip.value }"
          @click="setStatus(chip.value)"
        >
          {{ chip.label }}
          <span class="chip-count">{{ chip.count }}</span>
        </span>
      </div>
      <div class="filter-controls">
        <el-select v-model="filter.type" placeholder="提现方式" clearable style="width: 140px">
          <el-option label="支付宝" :value="1" />
          <el-option label="银行卡" :value="2" />
        </el-select>
        <el-input
          v-model="filter.keyword"
          placeholder="搜索用户昵称 / 单号"
          :prefix-icon="Search"
          clearable
          style="width: 240px"
        />
      </div>
    </section>

    <!-- 表格 -->
    <section class="table-card">
      <el-table
        :data="filteredRecords"
        v-loading="loading"
        :element-loading-text="'加载中...'"
        row-key="id"
        stripe
        class="withdrawal-table"
      >
        <el-table-column prop="orderNo" label="单号" min-width="180">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发起人" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatar || ''">
                {{ (row.userName || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ row.userName || '未知用户' }}</div>
                <div class="user-sub">ID: {{ row.userId ?? '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-cell">￥{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="方式" width="120">
          <template #default="{ row }">
            <div class="pay-method">
              <el-icon class="method-icon" :class="row.type === 1 ? 'alipay' : 'bank'">
                <WalletFilled v-if="row.type === 1" />
                <CreditCard v-else />
              </el-icon>
              <span>{{ row.type === 1 ? '支付宝' : '银行卡' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="账号" min-width="160">
          <template #default="{ row }">
            <span class="masked">{{ maskAccount(row.accountNo) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">
            <span class="time-cell">{{ formatTime(row.createdAt || row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light" round>
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon :size="48" color="var(--text-tertiary)"><DocumentRemove /></el-icon>
            <div class="empty-text">暂无提现记录</div>
          </div>
        </template>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="current"
          :page-size="10"
          layout="total, prev, pager, next, jumper"
          :total="filteredTotal"
          background
        />
      </div>
    </section>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="提现申请详情"
      size="480px"
      :destroy-on-close="true"
    >
      <div v-if="detailRow" class="detail-container">
        <div class="detail-hero">
          <el-avatar :size="56" :src="detailRow.avatar || ''">
            {{ (detailRow.userName || 'U').charAt(0).toUpperCase() }}
          </el-avatar>
          <div>
            <div class="hero-name">{{ detailRow.userName || '未知用户' }}</div>
            <div class="hero-sub">用户 ID：{{ detailRow.userId ?? '-' }}</div>
          </div>
          <el-tag :type="statusTagType(detailRow.status)" effect="light" round size="large">
            {{ statusLabel(detailRow.status) }}
          </el-tag>
        </div>

        <div class="detail-amount">
          <span class="label">提现金额</span>
          <span class="value">￥{{ formatAmount(detailRow.amount) }}</span>
        </div>

        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="k">提现单号</span>
              <span class="v">{{ detailRow.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="k">申请时间</span>
              <span class="v">{{ formatTime(detailRow.createdAt || detailRow.createTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="k">处理时间</span>
              <span class="v">{{ formatTime(detailRow.updatedAt || detailRow.updateTime) || '—' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">账户信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="k">提现方式</span>
              <span class="v">{{ detailRow.type === 1 ? '支付宝' : '银行卡' }}</span>
            </div>
            <div class="detail-item">
              <span class="k">实名</span>
              <span class="v">{{ detailRow.realName || detailRow.accountName || '—' }}</span>
            </div>
            <div class="detail-item" v-if="detailRow.type === 2">
              <span class="k">开户行</span>
              <span class="v">{{ detailRow.bankName || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="k">账号</span>
              <span class="v">{{ detailRow.accountNo }}</span>
            </div>
          </div>
        </div>

        <div v-if="detailRow.rejectReason || detailRow.status === 2" class="detail-section">
          <div class="section-title danger">驳回原因</div>
          <div class="reject-box">{{ detailRow.rejectReason || '—' }}</div>
        </div>

        <div v-if="detailRow.status === 0" class="detail-actions">
          <el-button type="danger" @click="handleReject(detailRow)">驳回</el-button>
          <el-button type="success" @click="handleApprove(detailRow.id)">通过申请</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 驳回对话框 -->
    <el-dialog
      v-model="rejectDialog.visible"
      title="驳回提现申请"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 16px">
        驳回后该笔提现将退回用户余额，请谨慎填写原因。
      </el-alert>
      <el-form label-position="top">
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectDialog.reason"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入驳回原因，将发送给用户"
          />
        </el-form-item>
        <el-form-item label="常用原因">
          <div class="reason-chips">
            <el-tag
              v-for="r in commonReasons"
              :key="r"
              class="reason-chip"
              :class="{ active: rejectDialog.reason === r }"
              @click="rejectDialog.reason = r"
            >{{ r }}</el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" :loading="rejectDialog.loading" @click="confirmReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Timer, Document, Money, Wallet, Search,
  DocumentRemove, WalletFilled, CreditCard
} from '@element-plus/icons-vue'
import { getWithdrawalList, approveWithdrawal, rejectWithdrawal } from '../../api/admin'

interface WithdrawalRecord {
  id: number
  orderNo: string
  userId?: number
  userName?: string
  avatar?: string
  amount: number
  type: number
  accountNo: string
  accountName?: string
  realName?: string
  bankName?: string
  status: number
  createdAt?: string
  createTime?: string
  updatedAt?: string
  updateTime?: string
  rejectReason?: string
}

const records = ref<WithdrawalRecord[]>([])
const loading = ref(false)
const current = ref(1)

const filter = reactive({
  status: -1 as number, // -1 全部
  type: null as number | null,
  keyword: ''
})

const drawerVisible = ref(false)
const detailRow = ref<WithdrawalRecord | null>(null)

const rejectDialog = reactive({
  visible: false,
  loading: false,
  id: 0,
  reason: ''
})

const commonReasons = [
  '账户信息不匹配',
  '实名认证未通过',
  '涉嫌违规操作',
  '银行卡信息有误',
  '申请金额超出限额'
]

const stats = computed(() => {
  const pending = records.value.filter(r => r.status === 0)
  const paid = records.value.filter(r => r.status === 1)
  return {
    pendingCount: pending.length,
    totalCount: records.value.length,
    paidAmount: paid.reduce((s, r) => s + Number(r.amount || 0), 0),
    pendingAmount: pending.reduce((s, r) => s + Number(r.amount || 0), 0)
  }
})

const statusChips = computed(() => [
  { label: '全部', value: -1, count: records.value.length },
  { label: '待审核', value: 0, count: records.value.filter(r => r.status === 0).length },
  { label: '已通过', value: 1, count: records.value.filter(r => r.status === 1).length },
  { label: '已驳回', value: 2, count: records.value.filter(r => r.status === 2).length }
])

const filteredRecords = computed(() => {
  let list = records.value
  if (filter.status !== -1) list = list.filter(r => r.status === filter.status)
  if (filter.type != null) list = list.filter(r => r.type === filter.type)
  if (filter.keyword) {
    const k = filter.keyword.trim().toLowerCase()
    list = list.filter(r =>
      (r.userName || '').toLowerCase().includes(k) ||
      (r.orderNo || '').toLowerCase().includes(k)
    )
  }
  // 分页切片
  const start = (current.value - 1) * 10
  return list.slice(start, start + 10)
})

const filteredTotal = computed(() => {
  let list = records.value
  if (filter.status !== -1) list = list.filter(r => r.status === filter.status)
  if (filter.type != null) list = list.filter(r => r.type === filter.type)
  if (filter.keyword) {
    const k = filter.keyword.trim().toLowerCase()
    list = list.filter(r =>
      (r.userName || '').toLowerCase().includes(k) ||
      (r.orderNo || '').toLowerCase().includes(k)
    )
  }
  return list.length
})

const setStatus = (v: number) => {
  filter.status = v
  current.value = 1
}

const statusLabel = (s: number) => s === 0 ? '待审核' : s === 1 ? '已通过' : '已驳回'
const statusTagType = (s: number): 'warning' | 'success' | 'danger' =>
  s === 0 ? 'warning' : s === 1 ? 'success' : 'danger'

const formatAmount = (v: number | string | undefined) => {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatTime = (t?: string) => {
  if (!t) return ''
  try {
    return new Date(t).toLocaleString('zh-CN')
  } catch {
    return t
  }
}

const maskAccount = (a?: string) => {
  if (!a) return '—'
  if (a.length <= 6) return a
  return a.slice(0, 3) + '****' + a.slice(-4)
}

const fetchAll = async () => {
  loading.value = true
  try {
    const res: any = await getWithdrawalList({ current: 1, size: 9999 })
    records.value = res.data.records || []
  } catch (error) {
    console.error(error)
    ElMessage.error('获取提现列表失败')
  } finally {
    loading.value = false
  }
}

const openDetail = (row: WithdrawalRecord) => {
  detailRow.value = row
  drawerVisible.value = true
}

const handleApprove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认通过该提现申请？通过后资金将直接打款到用户账户。', '审批确认', {
      type: 'warning',
      confirmButtonText: '确认通过',
      cancelButtonText: '再想想'
    })
    await approveWithdrawal(id)
    ElMessage.success('审批通过')
    drawerVisible.value = false
    fetchAll()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleReject = (row: WithdrawalRecord) => {
  rejectDialog.id = row.id
  rejectDialog.reason = ''
  rejectDialog.visible = true
}

const confirmReject = async () => {
  if (!rejectDialog.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  rejectDialog.loading = true
  try {
    await rejectWithdrawal(rejectDialog.id, rejectDialog.reason.trim())
    ElMessage.success('已驳回')
    rejectDialog.visible = false
    drawerVisible.value = false
    fetchAll()
  } catch (error) {
    console.error(error)
  } finally {
    rejectDialog.loading = false
  }
}

onMounted(fetchAll)
</script>

<style scoped>
.withdrawal-manage {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
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
  line-height: 1.2;
}
.header-content .page-desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--text-secondary);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}
.stat-icon.primary { background: hsl(221, 83%, 96%); color: var(--color-primary); }
.stat-icon.success { background: hsl(160, 55%, 94%); color: var(--color-success); }
.stat-icon.warning { background: hsl(38, 92%, 94%); color: var(--color-warning); }
.stat-icon.danger  { background: hsl(0, 72%, 96%);  color: var(--color-danger); }
.stat-body { flex: 1; min-width: 0; }
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}
.stat-trend {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

/* 筛选 */
.filter-bar {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  box-shadow: var(--shadow-sm);
}
.filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}
.chip:hover { border-color: var(--color-primary); color: var(--color-primary); }
.chip.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.chip-count {
  font-size: 12px;
  padding: 0 6px;
  border-radius: 10px;
  background: hsla(0, 0%, 100%, 0.25);
  color: inherit;
}
.chip:not(.active) .chip-count {
  background: hsl(220, 13%, 95%);
  color: var(--text-tertiary);
}
.filter-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 表格卡片 */
.table-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 8px 8px 16px;
  box-shadow: var(--shadow-sm);
}
.withdrawal-table :deep(.el-table__header) th {
  background: hsl(220, 14%, 98%);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
}
.order-no {
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  font-size: 13px;
  color: var(--text-primary);
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-info { min-width: 0; }
.user-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.2;
}
.user-sub {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}
.amount-cell {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-danger);
  font-variant-numeric: tabular-nums;
}
.pay-method {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-primary);
  font-size: 13px;
}
.method-icon {
  font-size: 16px;
}
.method-icon.alipay { color: hsl(205, 90%, 50%); }
.method-icon.bank   { color: hsl(221, 83%, 53%); }
.masked {
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  font-size: 13px;
  color: var(--text-secondary);
}
.time-cell {
  color: var(--text-secondary);
  font-size: 13px;
}
.empty-state {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.empty-text {
  color: var(--text-tertiary);
  font-size: 14px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 8px 0;
}

/* 详情抽屉 */
.detail-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.detail-hero {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: hsl(220, 14%, 98%);
  border-radius: var(--radius-md);
}
.detail-hero .hero-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.detail-hero .hero-sub {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}
.detail-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: hsl(0, 72%, 98%);
  border-radius: var(--radius-md);
  border-left: 4px solid var(--color-danger);
}
.detail-amount .label {
  font-size: 14px;
  color: var(--text-secondary);
}
.detail-amount .value {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-danger);
  font-variant-numeric: tabular-nums;
}
.detail-section {
  border-top: 1px solid var(--border-light);
  padding-top: 16px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.section-title.danger { color: var(--color-danger); }
.section-title::before {
  content: '';
  width: 3px;
  height: 14px;
  background: var(--color-primary);
  border-radius: 2px;
}
.section-title.danger::before { background: var(--color-danger); }
.detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 8px 12px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}
.detail-item .k { color: var(--text-secondary); }
.detail-item .v {
  color: var(--text-primary);
  font-weight: 500;
  text-align: right;
  max-width: 60%;
  word-break: break-all;
}
.reject-box {
  background: hsl(0, 72%, 98%);
  border: 1px solid hsl(0, 72%, 92%);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  color: var(--color-danger);
  font-size: 13px;
  line-height: 1.6;
}
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
}

/* 驳回弹窗 chips */
.reason-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.reason-chip {
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}
.reason-chip.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

/* 响应式 */
@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .withdrawal-manage { padding: 16px; }
  .stats-grid { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .filter-bar { flex-direction: column; align-items: flex-start; }
  .filter-controls { width: 100%; }
  .filter-controls > * { flex: 1; }
}
</style>

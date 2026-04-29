<template>
  <div class="admin-container">
    <main class="main-content">
      <!-- 页头 -->
      <header class="page-header">
        <div class="header-text">
          <h2 class="page-title">用户等级管理</h2>
          <p class="page-desc">配置不同充值区间对应的会员等级标签</p>
        </div>
        <el-button type="primary" class="action-btn" @click="openDialog()">
          <el-icon class="el-icon--left"><Plus /></el-icon>
          添加等级
        </el-button>
      </header>

      <!-- 等级卡片区 -->
      <section class="level-grid" v-loading="loading">
        <article
          v-for="lv in sortedLevels"
          :key="lv.id"
          class="level-card"
        >
          <div class="level-card-header">
            <div
              class="level-badge"
              :style="{ backgroundColor: lv.icon || 'var(--color-primary)' }"
            >
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="level-header-text">
              <div class="level-name">{{ lv.levelName }}</div>
              <div class="level-range">
                ￥{{ formatAmount(lv.minAmount) }}
                <span class="level-range-sep">—</span>
                ￥{{ formatAmount(lv.maxAmount) }}
              </div>
            </div>
          </div>
          <p class="level-desc">{{ lv.description || '暂无描述' }}</p>
          <div class="level-meta">
            <div class="level-meta-item">
              <span class="level-meta-label">覆盖用户</span>
              <span class="level-meta-value">{{ lv.userCount ?? '-' }}</span>
            </div>
            <div class="level-meta-item">
              <span class="level-meta-label">色值</span>
              <span class="level-meta-value color-dot-wrapper">
                <span class="color-dot" :style="{ backgroundColor: lv.icon }"></span>
                {{ lv.icon }}
              </span>
            </div>
          </div>
          <div class="level-card-footer">
            <el-button
              size="small"
              class="level-op"
              @click="openDialog(lv)"
            >
              <el-icon class="el-icon--left"><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              class="level-op level-op--danger"
              @click="handleDelete(lv)"
            >
              <el-icon class="el-icon--left"><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </article>

        <div v-if="!loading && sortedLevels.length === 0" class="empty-state">
          <el-empty description="暂未配置等级，点击右上角添加" />
        </div>
      </section>

      <!-- 详情表格 -->
      <section class="table-section">
        <div class="section-header">
          <h3 class="section-title">等级详情列表</h3>
          <span class="section-hint">共 {{ total }} 个等级</span>
        </div>
        <el-table
          :data="levels"
          v-loading="loading"
          stripe
          class="responsive-table"
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="等级名称" min-width="140">
            <template #default="{ row }">
              <div class="name-cell">
                <span class="color-dot" :style="{ backgroundColor: row.icon }"></span>
                <span>{{ row.levelName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="最小金额" min-width="120" align="right">
            <template #default="{ row }">
              <span class="amount-text">￥{{ formatAmount(row.minAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最大金额" min-width="120" align="right">
            <template #default="{ row }">
              <span class="amount-text">￥{{ formatAmount(row.maxAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="颜色" min-width="140">
            <template #default="{ row }">
              <div class="color-cell">
                <span class="color-dot" :style="{ backgroundColor: row.icon }"></span>
                <span class="mono">{{ row.icon }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <div class="op-group">
                <el-tooltip content="编辑" placement="top">
                  <el-button circle size="small" class="op-btn" @click="openDialog(row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button circle size="small" class="op-btn op-btn--danger" @click="handleDelete(row)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无数据" />
          </template>
        </el-table>

        <div class="pagination">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="fetchLevels"
          />
        </div>
      </section>
    </main>

    <!-- 编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑等级' : '添加等级'"
      width="540px"
      class="level-dialog"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" label-position="top">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="form.levelName" placeholder="请输入等级名称，如「黄金会员」" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="最小金额 (￥)" prop="minAmount">
            <el-input-number v-model="form.minAmount" :min="0" :step="100" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="最大金额 (￥)" prop="maxAmount">
            <el-input-number v-model="form.maxAmount" :min="0" :step="100" controls-position="right" style="width: 100%" />
          </el-form-item>
        </div>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述该等级的权益或适用场景"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标签颜色" prop="icon">
          <div class="color-picker-wrapper">
            <el-color-picker v-model="form.icon" size="default" />
            <div class="color-preset-list">
              <button
                v-for="c in presetColors"
                :key="c"
                type="button"
                class="color-preset"
                :class="{ 'color-preset--active': form.icon === c }"
                :style="{ backgroundColor: c }"
                @click="form.icon = c"
                :title="c"
              ></button>
            </div>
          </div>
          <div class="form-hint">用于用户主页等级标签与奖章背景色</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Edit, Delete, Plus, Trophy } from '@element-plus/icons-vue'

interface LevelItem {
  id?: number
  levelName: string
  minAmount: number
  maxAmount: number
  description?: string
  icon: string
  userCount?: number
}

const levels = ref<LevelItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const defaultForm = (): LevelItem => ({
  id: undefined,
  levelName: '',
  minAmount: 0,
  maxAmount: 0,
  description: '',
  icon: '#cd7f32'
})

const form = ref<LevelItem>(defaultForm())

const presetColors = [
  '#cd7f32', // 青铜
  '#c0c0c0', // 白银
  '#ffd700', // 黄金
  '#e5e4e2', // 铂金
  '#b9f2ff', // 钻石
  '#1e90ff',
  '#90ee90',
  '#ff8c00',
  '#c71585',
  '#606266'
]

const rules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  minAmount: [{ required: true, message: '请输入最小金额', trigger: 'blur' }],
  maxAmount: [{ required: true, message: '请输入最大金额', trigger: 'blur' }]
}

const sortedLevels = computed(() => {
  return [...levels.value].sort((a, b) => (a.minAmount ?? 0) - (b.minAmount ?? 0))
})

const formatAmount = (val?: number): string => {
  if (val === undefined || val === null) return '0'
  return Number(val).toLocaleString('zh-CN')
}

const fetchLevels = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/sys-user-level/page', {
      params: { current: currentPage.value, size: pageSize.value }
    })
    levels.value = res.data?.records || res.data || []
    total.value = res.data?.total || levels.value.length || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openDialog = (row?: LevelItem) => {
  dialogVisible.value = true
  nextTick(() => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
    if (row) {
      form.value = { ...row }
    } else {
      form.value = defaultForm()
    }
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.id) {
          await request.put('/admin/sys-user-level', form.value)
          ElMessage.success('修改成功')
        } else {
          await request.post('/admin/sys-user-level', form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchLevels()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (row: LevelItem) => {
  ElMessageBox.confirm(
    `确认删除「${row.levelName}」等级吗？此操作不可恢复。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  ).then(async () => {
    try {
      await request.delete(`/admin/sys-user-level/${row.id}`)
      ElMessage.success('删除成功')
      fetchLevels()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchLevels()
})
</script>

<style scoped>
.admin-container {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
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

.page-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: -0.02em;
}

.page-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.action-btn {
  height: 36px;
  border-radius: var(--radius-md);
  font-weight: 500;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* 等级卡片网格 */
.level-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  min-height: 80px;
}

.level-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), border-color var(--transition-fast);
}

.level-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.level-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.level-badge {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.level-header-text {
  min-width: 0;
  flex: 1;
}

.level-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.level-range {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

.level-range-sep {
  margin: 0 4px;
  color: var(--text-tertiary);
}

.level-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.level-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 12px;
  background: hsl(220, 14%, 98%);
  border-radius: var(--radius-md);
}

.level-meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.level-meta-label {
  font-size: 11px;
  color: var(--text-secondary);
}

.level-meta-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.color-dot-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-weight: 500;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid var(--border-light);
  flex-shrink: 0;
}

.level-card-footer {
  display: flex;
  gap: 8px;
  margin-top: auto;
  padding-top: 4px;
}

.level-op {
  flex: 1;
  border-radius: var(--radius-md);
  transition: transform var(--transition-fast);
}

.level-op:hover {
  transform: translateY(-1px);
}

.level-op--danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px 0;
}

/* 详情表格 */
.table-section {
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
  padding: 0 4px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.responsive-table {
  border-radius: var(--radius-md);
  overflow: auto;
}

:deep(.el-table) {
  --el-table-border-color: var(--border-light);
  --el-table-row-hover-bg-color: var(--color-primary-light);
  font-size: 13px;
}

:deep(.el-table th.el-table__cell) {
  background: hsl(220, 14%, 98%);
  color: var(--text-primary);
  font-weight: 600;
}

.name-cell,
.color-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.mono {
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

.amount-text {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: var(--text-primary);
}

.op-group {
  display: inline-flex;
  gap: 6px;
}

.op-btn {
  transition: transform var(--transition-fast);
}

.op-btn:hover {
  transform: translateY(-1px);
}

.op-btn--danger {
  color: var(--color-danger);
  border-color: var(--color-danger);
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* Dialog 样式 */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.color-preset-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-preset {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid var(--border-light);
  cursor: pointer;
  padding: 0;
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.color-preset:hover {
  transform: scale(1.08);
}

.color-preset--active {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-light);
}

.form-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 响应式 */
@media (max-width: 1100px) {
  .level-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .page-title { font-size: 20px; }
  .level-grid { grid-template-columns: 1fr; }
  .form-row { grid-template-columns: 1fr; }
}
</style>

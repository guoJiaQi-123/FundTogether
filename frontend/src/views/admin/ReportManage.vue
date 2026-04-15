<template>
  <div class="report-manage">
    <h2>举报管理</h2>
    <el-table :data="records" v-loading="loading" stripe>
      <el-table-column prop="reporterName" label="举报人" width="120" />
      <el-table-column prop="projectName" label="项目" width="200" />
      <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : 'success'">
            {{ row.status === 0 ? '待处理' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleResult" label="处理结果" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="举报时间" width="180">
        <template #default="{ row }">{{ new Date(row.createdAt).toLocaleString() }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="handleProcess(row.id)">处理</el-button>
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
import { getReportList, handleReport } from '../../api/admin'

const records = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getReportList({ current: current.value, size: 10 })
    records.value = res.data.records
    total.value = res.data.total
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const handleProcess = async (id: number) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理结果', '处理举报', { inputPattern: /.+/, inputErrorMessage: '处理结果不能为空' })
    await handleReport(id, value)
    ElMessage.success('举报已处理')
    fetchData()
  } catch (error) { if (error !== 'cancel') console.error(error) }
}

onMounted(fetchData)
</script>

<style scoped>
.report-manage { padding: 24px; }
.report-manage h2 { margin-bottom: 24px; }
.pagination { margin-top: 24px; display: flex; justify-content: center; }
</style>

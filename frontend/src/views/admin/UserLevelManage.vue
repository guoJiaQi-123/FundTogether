<template>
  <div class="admin-container">
    <main class="main-content">
      <div class="page-header">
        <h2>用户等级管理</h2>
        <el-button type="primary" @click="openDialog()">添加等级</el-button>
      </div>
      
      <el-card class="table-card">
        <el-table :data="levels" style="width: 100%" v-loading="loading" class="responsive-table">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="levelName" label="等级名称" min-width="120" />
          <el-table-column prop="minAmount" label="最小金额" min-width="120" />
          <el-table-column prop="maxAmount" label="最大金额" min-width="120" />
          <el-table-column prop="description" label="描述" min-width="150" />
          <el-table-column prop="icon" label="图标" min-width="120" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                circle
                @click="openDialog(row)"
                title="编辑"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                circle
                @click="handleDelete(row)"
                title="删除"
              >
                <el-icon><Delete /></el-icon>
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
            @current-change="fetchLevels"
            small
          />
        </div>
      </el-card>
    </main>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑等级' : '添加等级'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="form.levelName" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="最小金额" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大金额" prop="maxAmount">
          <el-input-number v-model="form.maxAmount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标 URL 或 class" />
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
import { ref, onMounted, nextTick } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'

const levels = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  id: undefined as number | undefined,
  levelName: '',
  minAmount: 0,
  maxAmount: 0,
  description: '',
  icon: ''
})

const rules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  minAmount: [{ required: true, message: '请输入最小金额', trigger: 'blur' }],
  maxAmount: [{ required: true, message: '请输入最大金额', trigger: 'blur' }]
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

const openDialog = (row?: any) => {
  dialogVisible.value = true
  nextTick(() => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
    if (row) {
      form.value = { ...row }
    } else {
      form.value = {
        id: undefined,
        levelName: '',
        minAmount: 0,
        maxAmount: 0,
        description: '',
        icon: ''
      }
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

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该用户等级吗？', '提示', {
    type: 'warning'
  }).then(async () => {
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
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.02em;
}

.table-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .page-header h2 {
    font-size: 20px;
  }
  .responsive-table :deep(.el-table__cell) {
    padding: 8px 0;
  }
}
</style>
<template>
  <div class="user-profile-container">
    <div class="content-layout">
      <!-- 左侧个人信息概览 -->
      <aside class="sidebar">
        <el-card class="profile-sidebar" shadow="hover">
          <div class="user-info-overview">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
            >
              <el-avatar :size="100" :src="profileForm.avatar" class="avatar-img">
                <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
              </el-avatar>
              <div class="avatar-hover-mask">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
            <h2 class="user-nickname">
              {{ profileForm.nickname || '未设置昵称' }}
              <el-tag v-if="userLevel" :color="userLevel.icon" effect="dark" size="small" class="level-tag" style="border: none; margin-left: 8px;">
                {{ userLevel.levelName }}
              </el-tag>
            </h2>
            <p class="user-bio">{{ profileForm.bio || '这个人很懒，什么都没留下...' }}</p>
            
            <div class="user-tags">
              <el-tag v-if="profileForm.profession" size="small" effect="plain" type="info">{{ profileForm.profession }}</el-tag>
              <el-tag v-if="profileForm.location" size="small" effect="plain" type="info">{{ profileForm.location }}</el-tag>
            </div>
          </div>

          <el-menu :default-active="activeTab" class="profile-menu" @select="handleSelectMenu" :border="false">
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>基础资料</span>
            </el-menu-item>
            <el-menu-item index="auth">
              <el-icon><Postcard /></el-icon>
              <span>实名认证</span>
            </el-menu-item>
            <el-menu-item index="payment">
              <el-icon><Wallet /></el-icon>
              <span>支付方式</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </aside>

      <!-- 右侧主要内容区 -->
      <main class="main-content-area">
        <el-card class="profile-content-card" shadow="hover">
          <div class="card-header-title">
            <span>{{ currentTabTitle }}</span>
          </div>

          <!-- 基础信息 -->
          <div v-show="activeTab === 'profile'" class="tab-content fade-in">
            <el-form :model="profileForm" label-width="100px" label-position="top" class="modern-form">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="昵称">
                    <el-input v-model="profileForm.nickname" placeholder="请输入昵称" size="large" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-select v-model="profileForm.gender" placeholder="请选择性别" size="large" style="width: 100%;">
                      <el-option label="保密" :value="0" />
                      <el-option label="男" :value="1" />
                      <el-option label="女" :value="2" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="生日">
                    <el-date-picker v-model="profileForm.birthday" type="date" placeholder="选择生日" format="YYYY-MM-DD" value-format="YYYY-MM-DD" size="large" style="width: 100%;" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所在地">
                    <el-input v-model="profileForm.location" placeholder="例如：中国，北京" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="学历">
                    <el-select v-model="profileForm.education" placeholder="请选择学历" size="large" style="width: 100%;">
                      <el-option label="高中及以下" value="高中及以下" />
                      <el-option label="大专" value="大专" />
                      <el-option label="本科" value="本科" />
                      <el-option label="硕士" value="硕士" />
                      <el-option label="博士及以上" value="博士及以上" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="职业">
                    <el-input v-model="profileForm.profession" placeholder="请输入职业" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="公司 / 组织">
                    <el-input v-model="profileForm.company" placeholder="请输入公司或组织名称" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="个人简介">
                    <el-input v-model="profileForm.bio" type="textarea" :rows="4" placeholder="向大家介绍一下你自己吧..." resize="none" />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions">
                <el-button type="primary" size="large" @click="handleUpdateProfile" :loading="updating">保存更改</el-button>
              </div>
            </el-form>
          </div>

          <!-- 实名认证 -->
          <div v-show="activeTab === 'auth'" class="tab-content fade-in">
            <div class="auth-status-container" v-if="authInfo">
              <el-result v-if="authInfo.status === 0" icon="info" title="审核中" sub-title="您的实名认证信息正在审核中，请耐心等待。" />
              <el-result v-else-if="authInfo.status === 1" icon="success" title="认证成功" sub-title="您已完成实名认证，享受更多平台权益。" />
              <el-result v-else-if="authInfo.status === 2" icon="error" title="认证驳回" :sub-title="`驳回原因: ${authInfo.auditReason}`" />
            </div>

            <el-form :model="authForm" label-position="top" class="modern-form" :disabled="authInfo && (authInfo.status === 0 || authInfo.status === 1)">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="真实姓名" required>
                    <el-input v-model="authForm.realName" placeholder="请输入真实姓名" size="large" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证号" required>
                    <el-input v-model="authForm.idCard" placeholder="请输入18位身份证号" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="身份证人像面" required>
                    <el-upload
                      class="idcard-uploader"
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :on-success="handleIdCardFrontSuccess"
                      :disabled="authInfo && (authInfo.status === 0 || authInfo.status === 1)"
                    >
                      <img v-if="authForm.idCardFront" :src="authForm.idCardFront" class="idcard" />
                      <div v-else class="upload-placeholder">
                        <el-icon><Plus /></el-icon>
                        <span>点击上传人像面</span>
                      </div>
                    </el-upload>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证国徽面" required>
                    <el-upload
                      class="idcard-uploader"
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :on-success="handleIdCardBackSuccess"
                      :disabled="authInfo && (authInfo.status === 0 || authInfo.status === 1)"
                    >
                      <img v-if="authForm.idCardBack" :src="authForm.idCardBack" class="idcard" />
                      <div v-else class="upload-placeholder">
                        <el-icon><Plus /></el-icon>
                        <span>点击上传国徽面</span>
                      </div>
                    </el-upload>
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions" v-if="!authInfo || authInfo.status === 2">
                <el-button type="primary" size="large" @click="submitAuth">提交认证</el-button>
              </div>
            </el-form>
          </div>

          <!-- 支付方式 -->
          <div v-show="activeTab === 'payment'" class="tab-content fade-in">
            <div class="action-bar">
              <el-button type="primary" @click="showPaymentDialog = true" icon="Plus">绑定新账号</el-button>
            </div>
            
            <div class="payment-list">
              <el-empty v-if="paymentMethods.length === 0" description="暂无绑定的支付方式" />
              <el-row :gutter="20" v-else>
                <el-col :xs="24" :sm="12" :md="12" v-for="item in paymentMethods" :key="item.id" style="margin-bottom: 20px;">
                  <div class="payment-card" :class="getPaymentClass(item.type)">
                    <div class="payment-header">
                      <div class="payment-type">
                        <el-icon v-if="item.type === 1"><ChatDotRound /></el-icon>
                        <el-icon v-else-if="item.type === 2"><Wallet /></el-icon>
                        <el-icon v-else-if="item.type === 3"><CreditCard /></el-icon>
                        <span>{{ getPaymentName(item.type) }}</span>
                      </div>
                      <span class="payment-name">{{ item.name }}</span>
                    </div>
                    <div class="payment-body">
                      <div class="payment-account">{{ formatAccount(item.account) }}</div>
                      <div class="payment-bank" v-if="item.type === 3">{{ item.bankName }}</div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>

          <!-- 修改密码 -->
          <div v-show="activeTab === 'password'" class="tab-content fade-in">
            <el-form :model="passwordForm" label-position="top" class="modern-form" style="max-width: 400px;">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password size="large" placeholder="请输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password size="large" placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password size="large" placeholder="请再次输入新密码" />
              </el-form-item>
              <div class="form-actions">
                <el-button type="primary" size="large" @click="handleUpdatePassword">确认修改</el-button>
              </div>
            </el-form>
          </div>

        </el-card>
      </main>
    </div>

    <!-- 绑定支付方式弹窗 -->
    <el-dialog v-model="showPaymentDialog" title="绑定支付账号" width="450px" custom-class="modern-dialog">
      <el-form :model="paymentForm" label-position="top">
        <el-form-item label="支付平台" required>
          <el-radio-group v-model="paymentForm.type" size="large" style="width: 100%; display: flex;">
            <el-radio-button :label="2" style="flex: 1; text-align: center;">支付宝</el-radio-button>
            <el-radio-button :label="3" style="flex: 1; text-align: center;">银行卡</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账号" required>
          <el-input v-model="paymentForm.account" placeholder="请输入支付账号/银行卡号" size="large" />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="paymentForm.name" placeholder="请输入绑定的真实姓名" size="large" />
        </el-form-item>
        <el-form-item label="所属银行" v-if="paymentForm.type === 3" required>
          <el-input v-model="paymentForm.bankName" placeholder="例如：招商银行" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPaymentDialog = false" size="large">取消</el-button>
          <el-button type="primary" @click="handleBindPaymentMethod" size="large">确认绑定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Camera, User, Postcard, Wallet, Lock, ChatDotRound, CreditCard } from '@element-plus/icons-vue'
import { updateProfile, updatePassword, bindPaymentMethod, getPaymentMethods, getUserInfo } from '../api/user'
import request from '../utils/request'
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'

const uploadUrl = 'http://localhost:8080/api/file/upload'
const uploadHeaders = {
  Authorization: 'Bearer ' + localStorage.getItem('token')
}

const activeTab = ref('profile')
const updating = ref(false)
const userStore = useUserStore()
const router = useRouter()
const userLevel = ref<any>(null)

const tabTitles: Record<string, string> = {
  profile: '基础资料',
  auth: '实名认证',
  payment: '支付方式管理',
  password: '安全设置'
}

const currentTabTitle = computed(() => tabTitles[activeTab.value])

const handleSelectMenu = (index: string) => {
  activeTab.value = index
}

const profileForm = ref({
  nickname: '',
  bio: '',
  avatar: '',
  gender: 0,
  birthday: '',
  location: '',
  education: '',
  profession: '',
  company: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const paymentMethods = ref<any[]>([])
const showPaymentDialog = ref(false)
const paymentForm = ref({
  type: 2,
  account: '',
  name: '',
  bankName: ''
})

const authInfo = ref<any>(null)
const authForm = ref({
  realName: '',
  idCard: '',
  idCardFront: '',
  idCardBack: ''
})

const loadUserInfo = async () => {
  try {
    const res: any = await getUserInfo()
    if (res.code === 200 && res.data) {
      const data = res.data
      profileForm.value = {
        nickname: data.nickname || '',
        bio: data.bio || '',
        avatar: data.avatar || '',
        gender: data.gender || 0,
        birthday: data.birthday || '',
        location: data.location || '',
        education: data.education || '',
        profession: data.profession || '',
        company: data.company || ''
      }
      userStore.userInfo = { ...userStore.userInfo, ...data }
    }
  } catch (error) {
    console.error('Failed to load user info', error)
  }
}

const handleAvatarSuccess = (res: any) => {
  if (res.code === 200) {
    profileForm.value.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handleIdCardFrontSuccess = (res: any) => {
  if (res.code === 200) {
    authForm.value.idCardFront = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handleIdCardBackSuccess = (res: any) => {
  if (res.code === 200) {
    authForm.value.idCardBack = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const fetchAuthInfo = async () => {
  try {
    const res: any = await request.get('/user/auth-info')
    authInfo.value = res.data
    if (res.data) {
      authForm.value.realName = res.data.realName || ''
      authForm.value.idCard = res.data.idCard || ''
      authForm.value.idCardFront = res.data.idCardFront || ''
      authForm.value.idCardBack = res.data.idCardBack || ''
    }
  } catch (error) {
    console.error(error)
  }
}

const submitAuth = async () => {
  if (!authForm.value.realName || !authForm.value.idCard || !authForm.value.idCardFront || !authForm.value.idCardBack) {
    ElMessage.warning('请填写完整的实名认证信息')
    return
  }
  try {
    await request.post('/user/auth-info', authForm.value)
    ElMessage.success('提交成功，等待审核')
    fetchAuthInfo()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

const fetchPaymentMethods = async () => {
  try {
    const res: any = await getPaymentMethods()
    paymentMethods.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const getPaymentName = (type: number) => {
  if (type === 1) return '微信支付'
  if (type === 2) return '支付宝'
  if (type === 3) return '银行卡'
  return '未知'
}

const getPaymentClass = (type: number) => {
  if (type === 1) return 'payment-wechat'
  if (type === 2) return 'payment-alipay'
  if (type === 3) return 'payment-bank'
  return ''
}

const formatAccount = (account: string) => {
  if (!account) return ''
  if (account.length > 8) {
    return account.substring(0, 4) + ' **** **** ' + account.substring(account.length - 4)
  }
  return account
}

const loadUserLevel = async () => {
  try {
    const res: any = await request.get('/user-level/current')
    if (res.code === 200 && res.data) {
      userLevel.value = res.data
    }
  } catch (error) {
    console.error('获取用户等级失败:', error)
  }
}

onMounted(() => {
  loadUserInfo()
  loadUserLevel()
  fetchPaymentMethods()
  fetchAuthInfo()
})

const handleUpdateProfile = async () => {
  updating.value = true
  try {
    await updateProfile(profileForm.value)
    ElMessage.success('个人资料已保存')
    loadUserInfo() // reload to sync
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    updating.value = false
  }
}

const handleUpdatePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  }
}

const handleBindPaymentMethod = async () => {
  if (!paymentForm.value.account || !paymentForm.value.name) {
    ElMessage.warning('请填写完整的账号和姓名信息')
    return
  }
  if (paymentForm.value.type === 3 && !paymentForm.value.bankName) {
    ElMessage.warning('请填写所属银行')
    return
  }
  try {
    await bindPaymentMethod(paymentForm.value)
    ElMessage.success('支付账号绑定成功')
    showPaymentDialog.value = false
    paymentForm.value = { type: 2, account: '', name: '', bankName: '' }
    fetchPaymentMethods()
  } catch (error: any) {
    ElMessage.error(error.message || '绑定失败')
  }
}
</script>

<style scoped>
.user-profile-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
  animation: fadeIn 0.4s ease-out;
}

.content-layout {
  display: flex;
  gap: 40px;
}

.sidebar {
  width: 340px;
  flex-shrink: 0;
}

.main-content-area {
  flex: 1;
  min-width: 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-sidebar {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
  margin-bottom: 24px;
}

.profile-sidebar :deep(.el-card__body) {
  padding: 0;
}

.user-info-overview {
  padding: 32px 24px 24px;
  text-align: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.avatar-img {
  border: 4px solid var(--el-bg-color);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  background: var(--el-fill-color-light);
}

.avatar-hover-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 14px;
}

.avatar-uploader:hover .avatar-hover-mask {
  opacity: 1;
}

.user-nickname {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.user-bio {
  margin: 0 0 16px;
  font-size: 14px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.user-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.profile-menu {
  padding: 12px 0;
}

.profile-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 16px;
  border-radius: var(--radius-md);
  color: var(--text-primary);
}

.profile-menu .el-menu-item.is-active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}

.profile-menu .el-icon {
  font-size: 18px;
  margin-right: 12px;
}

.profile-content-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
  min-height: 600px;
}

.profile-content-card :deep(.el-card__body) {
  padding: 32px;
}

.card-header-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.tab-content {
  max-width: 800px;
}

.fade-in {
  animation: fadeIn 0.3s ease-out;
}

.modern-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 8px;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.auth-status-container {
  margin-bottom: 32px;
  background: var(--el-fill-color-light);
  border-radius: var(--radius-md);
  padding: 16px;
}

.idcard-uploader {
  width: 100%;
}

.idcard-uploader :deep(.el-upload) {
  width: 100%;
  border: 1px dashed var(--el-border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  background: var(--el-fill-color-light);
}

.idcard-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.upload-placeholder {
  height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--text-secondary);
}

.upload-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
  color: var(--el-text-color-placeholder);
}

.idcard {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.action-bar {
  margin-bottom: 24px;
}

.payment-list {
  margin-top: 16px;
}

.payment-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.payment-card:hover {
  transform: translateY(-4px);
}

.payment-wechat {
  background: linear-gradient(135deg, #07c160, #06ad56);
}

.payment-alipay {
  background: linear-gradient(135deg, #1677ff, #1662fa);
}

.payment-bank {
  background: linear-gradient(135deg, #f5222d, #cf1322);
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.payment-type {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.payment-name {
  font-size: 14px;
  opacity: 0.9;
}

.payment-account {
  font-size: 20px;
  font-family: monospace;
  letter-spacing: 2px;
}

.payment-bank {
  margin-top: 8px;
  font-size: 14px;
  opacity: 0.9;
}

@media (max-width: 1024px) {
  .content-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: 16px;
  }
}
</style>

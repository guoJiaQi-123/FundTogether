<template>
  <div class="user-profile-container">
    <div class="content-layout">
      <!-- 左侧个人信息概览 -->
      <aside class="sidebar">
        <el-card class="profile-sidebar" shadow="hover">
          <div class="sidebar-header-bg"></div>
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
            <el-menu-item index="account">
              <el-icon><Wallet /></el-icon>
              <span>我的账户</span>
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

          <!-- 我的账户 -->
          <div v-show="activeTab === 'account'" class="tab-content fade-in">
            <div class="account-card modern-stat-card">
              <div class="stat-card-bg-decoration"></div>
              <div class="stat-content">
                <div class="balance-label">
                  <el-icon><Wallet /></el-icon>
                  <span>可用余额</span>
                </div>
                <div class="balance-amount">
                  <span class="currency">¥</span>
                  <span class="amount">{{ balance.toFixed(2) }}</span>
                </div>
              </div>
              <div class="stat-actions">
                <el-button size="large" @click="showRechargeDialog = true" class="recharge-btn premium-btn">
                  <el-icon><Money /></el-icon> <span>立即充值</span>
                </el-button>
              </div>
            </div>
          </div>

          <!-- 修改密码 -->
          <div v-show="activeTab === 'password'" class="tab-content fade-in">
            <el-form :model="passwordForm" label-position="top" class="modern-form">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="原密码" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password size="large" placeholder="请输入当前密码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password size="large" placeholder="请输入新密码" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password size="large" placeholder="请再次输入新密码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="form-actions">
                <el-button type="primary" size="large" @click="handleUpdatePassword">确认修改</el-button>
              </div>
            </el-form>
          </div>

        </el-card>
      </main>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog v-model="showRechargeDialog" title="账户充值" width="400px" custom-class="modern-dialog">
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
          <el-radio-group v-model="rechargeMethod">
            <el-radio value="alipay">
              <div class="alipay-option">
                <el-icon><Platform /></el-icon> 支付宝 (Alipay)
              </div>
            </el-radio>
            <el-radio value="mock">
              <div class="mock-option">
                <el-icon><Money /></el-icon> 本地模拟充值 (Dev)
              </div>
            </el-radio>
          </el-radio-group>
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

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Camera, User, Postcard, Wallet, Lock, Platform, Money } from '@element-plus/icons-vue'
import { updateProfile, updatePassword, getUserInfo } from '../api/user'
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
  account: '我的账户',
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

const balance = ref(0)
const showRechargeDialog = ref(false)
const rechargeAmount = ref(100)
const rechargeMethod = ref('mock')
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
    if (rechargeMethod.value === 'mock') {
      const res: any = await request.post('/user/account/recharge/mock', {
        amount: rechargeAmount.value
      })
      if (res.code === 200) {
        ElMessage.success('模拟充值成功')
        showRechargeDialog.value = false
        fetchBalance() // 刷新余额
      } else {
        ElMessage.error(res.message || '模拟充值失败')
      }
    } else {
      const res: any = await request.post('/user/account/recharge', {
        amount: rechargeAmount.value
      })
      
      if (res.code === 200 && res.data) {
        const formHtml = res.data
        const div = document.createElement('div')
        div.innerHTML = formHtml
        document.body.appendChild(div)
        document.forms[document.forms.length - 1].submit()
      } else {
        ElMessage.error(res.message || '发起充值失败')
      }
    }
  } catch (error) {
    console.error('Recharge error:', error)
    ElMessage.error('发起充值失败')
  } finally {
    recharging.value = false
  }
}

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
  fetchAuthInfo()
  fetchBalance()
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
  border-radius: 20px;
  border: none;
  box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.profile-sidebar:hover {
  box-shadow: 0 12px 32px -4px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.profile-sidebar :deep(.el-card__body) {
  padding: 0;
}

.sidebar-header-bg {
  height: 120px;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  position: absolute;
  top: 0; left: 0; right: 0;
  z-index: 0;
}

.user-info-overview {
  position: relative;
  z-index: 1;
  padding: 60px 24px 24px;
  text-align: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: linear-gradient(to bottom, rgba(255,255,255,0) 0%, rgba(255,255,255,1) 40px);
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
  border: 4px solid white;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  background: white;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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

.avatar-uploader:hover .avatar-img {
  transform: scale(1.05);
}

.avatar-uploader:hover .avatar-hover-mask {
  opacity: 1;
}

.user-nickname {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 800;
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
  padding: 16px 0;
}

.profile-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 8px 16px;
  border-radius: 12px;
  color: var(--text-primary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.profile-menu .el-menu-item:hover {
  background-color: var(--el-fill-color-light);
  transform: translateX(4px);
}

.profile-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, var(--el-color-primary-light-9) 0%, transparent 100%);
  color: var(--el-color-primary);
  font-weight: 600;
  box-shadow: inset 4px 0 0 0 var(--el-color-primary);
  border-radius: 4px 12px 12px 4px;
}

.profile-menu .el-icon {
  font-size: 18px;
  margin-right: 12px;
}

.profile-content-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.05);
  min-height: 600px;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.profile-content-card:hover {
  box-shadow: 0 12px 32px -4px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.profile-content-card :deep(.el-card__body) {
  padding: 40px;
}

.card-header-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 40px;
  position: relative;
  padding-bottom: 8px;
}

.card-header-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 48px;
  height: 4px;
  background: var(--el-color-primary);
  border-radius: 2px;
}

.tab-content {
  width: 100%;
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
  height: 240px;
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
  height: 240px;
  object-fit: cover;
  display: block;
}

.modern-stat-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #3db5ff 0%, #1677ff 100%);
  border-radius: 20px;
  padding: 40px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 20px 40px -10px rgba(22, 119, 255, 0.4);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.4s ease;
}

.modern-stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 24px 48px -12px rgba(22, 119, 255, 0.6);
}

.stat-card-bg-decoration {
  position: absolute;
  top: -50px;
  right: -50px;
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 70%);
  border-radius: 50%;
  pointer-events: none;
}

.stat-card-bg-decoration::after {
  content: '';
  position: absolute;
  bottom: 20px;
  left: -50px;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
  border-radius: 50%;
}

.balance-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  opacity: 0.9;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.balance-label .el-icon {
  font-size: 20px;
}

.balance-amount {
  color: white;
  margin-bottom: 0;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.currency {
  font-size: 28px;
  font-weight: 600;
  opacity: 0.9;
}

.amount {
  font-size: 64px;
  font-weight: 800;
  letter-spacing: -2px;
  line-height: 1;
}

.premium-btn {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  color: white !important;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  border-radius: 12px !important;
  padding: 0 20px !important;
  height: 46px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  min-width: 130px;
}

.premium-btn:hover {
  background: white !important;
  color: #1677ff !important;
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.premium-btn .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.modern-form :deep(.el-input__wrapper), 
.modern-form :deep(.el-select__wrapper),
.modern-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px var(--el-border-color-lighter) inset;
  transition: all 0.3s;
  background-color: var(--el-fill-color-blank);
}

.modern-form :deep(.el-input__wrapper:hover), 
.modern-form :deep(.el-select__wrapper:hover),
.modern-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.modern-form :deep(.el-input__wrapper.is-focus), 
.modern-form :deep(.el-select__wrapper.is-focus),
.modern-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px var(--el-color-primary-light-3) inset;
  background-color: var(--el-color-primary-light-9);
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

.alipay-option, .mock-option {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 15px;
  border: 1px solid #409eff;
  color: #409eff;
  border-radius: 4px;
  background-color: #ecf5ff;
}

.mock-option {
  border-color: #67c23a;
  color: #67c23a;
  background-color: #f0f9eb;
}

.payment-method .el-radio {
  margin-right: 16px;
  margin-bottom: 8px;
}

@media (max-width: 1024px) {
  .content-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .modern-stat-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  .stat-actions {
    width: 100%;
    display: flex;
    justify-content: flex-start;
  }
  .premium-btn {
    width: 100%;
    max-width: 220px;
  }
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: 16px;
  }
}
</style>

<template>
  <div class="login-container">
    <div class="top-nav">
      <el-dropdown @command="handleSetLanguage" style="margin-right: 12px; cursor: pointer;">
        <span class="el-dropdown-link" style="display: flex; align-items: center; color: var(--text-primary);">
          {{ t('common.language') }}
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="zh" :disabled="locale === 'zh'">中文</el-dropdown-item>
            <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      
      <a href="https://github.com/guoJiaQi-123/FundTogether" target="_blank" class="github-link" title="GitHub">
        <svg viewBox="0 0 1024 1024" width="22" height="22" style="margin-top: 5px;"><path d="M511.6 76.3C264.3 76.2 64 276.4 64 523.5 64 718.9 189.3 885 363.8 946c23.5 5.9 19.9-10.8 19.9-22.2v-77.5c-135.7 15.9-141.2-73.9-150.3-88.9C215 726 171.5 718 184.5 703c30.9-15.9 62.4 4 98.9 57.9 26.4 39.1 77.9 32.5 104 26 5.7-23.5 17.9-44.5 34.7-60.8-140.6-25.2-199.2-111-199.2-213 0-49.5 16.3-95 48.3-131.7-20.4-60.5 1.9-112.3 4.9-120 58.1-5.2 118.5 41.6 123.2 45.3 33-8.9 70.7-13.6 112.9-13.6 42.4 0 80.2 4.9 113.5 13.9 11.3-8.6 67.3-48.8 121.3-43.9 2.9 7.7 24.7 58.3 5.5 118 32.4 36.8 48.9 82.7 48.9 132.3 0 102.2-59 188.1-200 212.9 23.5 23.2 38.1 55.4 38.1 91v112.5c0.8 9 0 27.9 22.4 22.4C835 885 960 719 960 523.6 960 276.4 759.7 76.3 511.6 76.3z" fill="var(--text-primary)"></path></svg>
      </a>
    </div>
    <div class="login-wrapper">
      <div class="login-illustration">
        <div class="illustration-content">
          <h1>FundTogether</h1>
          <p>{{ t('home.heroSubtitle') }}</p>
        </div>
      </div>
      <div class="login-form-container">
        <div class="login-header">
          <h2>{{ t('login.welcome') }}</h2>
          <p>{{ t('login.subtitle') }}</p>
        </div>
        
        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="rules" 
          label-width="0"
          class="login-form"
        >
          <el-form-item prop="account">
            <el-input 
              v-model="loginForm.account" 
              :placeholder="t('login.accountPlaceholder')"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              :placeholder="t('login.passwordPlaceholder')" 
              show-password
              size="large"
              class="custom-input"
              @keyup.enter="submitForm(loginFormRef)"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-actions">
            <el-checkbox v-model="loginForm.rememberMe">{{ t('login.rememberMe') }}</el-checkbox>
            <el-link type="primary" :underline="false" @click="showResetDialog = true">{{ t('login.forgotPassword') }}</el-link>
          </div>

          <el-form-item>
            <el-button type="primary" class="submit-btn" size="large" @click="submitForm(loginFormRef)" :loading="loading">
              {{ t('login.signIn') }}
            </el-button>
          </el-form-item>

          <div class="register-link">
            {{ t('login.noAccount') }} <router-link to="/register">{{ t('login.createOne') }}</router-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 找回密码弹窗 -->
    <el-dialog v-model="showResetDialog" :title="t('login.resetTitle')" width="400px" custom-class="custom-dialog">
      <el-form :model="resetForm" label-width="80px">
        <el-form-item :label="t('login.account')" required>
          <el-input v-model="resetForm.account" :placeholder="t('login.accountPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('login.code')" required>
          <div style="display: flex; gap: 10px;">
            <el-input v-model="resetForm.code" :placeholder="t('login.code')" />
            <el-button type="primary" plain>{{ t('login.getCode') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item :label="t('login.newPassword')" required>
          <el-input v-model="resetForm.newPassword" type="password" show-password :placeholder="t('login.newPassword')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showResetDialog = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleResetPassword" :loading="resetting">{{ t('login.reset') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, ArrowDown } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { loginUser, resetPassword } from '../api/user'
import { useUserStore } from '../store/user'

const { t, locale } = useI18n()

const handleSetLanguage = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
}

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const showResetDialog = ref(false)
const resetting = ref(false)
const resetForm = reactive({
  account: '',
  code: '',
  newPassword: ''
})

const loginForm = reactive({
  account: '',
  password: '',
  rememberMe: false
})

const rules = computed<FormRules>(() => ({
  account: [
    { required: true, message: t('login.accountPlaceholder'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.passwordPlaceholder'), trigger: 'blur' }
  ]
}))

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await loginUser({
          account: loginForm.account,
          password: loginForm.password
        })
        userStore.setUserInfo(res.data)
        ElMessage.success(t('login.loginSuccess'))
        router.push('/home')
      } catch (error) {
        // Error is handled in request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}

const handleResetPassword = async () => {
  if (!resetForm.account || !resetForm.code || !resetForm.newPassword) {
    ElMessage.warning(t('login.fillInfo'))
    return
  }
  resetting.value = true
  try {
    await resetPassword(resetForm)
    ElMessage.success(t('login.resetSuccess'))
    showResetDialog.value = false
    resetForm.account = ''
    resetForm.code = ''
    resetForm.newPassword = ''
  } catch (error: any) {
    ElMessage.error(error.message || t('login.resetFailed'))
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-3);
  position: relative;
}

.top-nav {
  position: absolute;
  top: 24px;
  right: 32px;
  display: flex;
  align-items: center;
  z-index: 10;
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}

.login-illustration {
  flex: 1;
  background: var(--primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: calc(var(--spacing-unit) * 5);
  position: relative;
  overflow: hidden;
}

.login-illustration::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.05;
  background-image: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 20px,
    rgba(255, 255, 255, 1) 20px,
    rgba(255, 255, 255, 1) 21px
  );
}

.illustration-content {
  position: relative;
  z-index: 1;
  max-width: 400px;
}

.illustration-content h1 {
  font-family: var(--font-heading);
  font-size: var(--text-2xl);
  font-weight: 800;
  margin-bottom: 20px;
  line-height: 1.2;
  color: white;
}

.illustration-content p {
  font-size: var(--text-base);
  line-height: 1.6;
  opacity: 0.9;
}

.login-form-container {
  flex: 1;
  padding: calc(var(--spacing-unit) * 8);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  margin-bottom: calc(var(--spacing-unit) * 5);
}

.login-header h2 {
  font-family: var(--font-heading);
  font-size: var(--text-xl);
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-1);
}

.login-header p {
  color: var(--text-secondary);
  font-size: var(--text-base);
}

.login-form {
  width: 100%;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 8px 15px;
  background-color: var(--bg-page);
  border: 1px solid var(--border-color);
  box-shadow: none !important;
  border-radius: var(--radius-sm);
  transition: border-color 200ms ease-out, box-shadow 200ms ease-out, background-color 200ms ease-out;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-light) !important;
  background-color: var(--bg-surface);
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: var(--text-sm);
  font-weight: 700;
  border-radius: var(--radius-md);
  margin-top: 10px;
  transition: all 200ms ease-out;
}

.register-link {
  text-align: center;
  margin-top: var(--spacing-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.register-link a {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: text-decoration 200ms ease-out;
}

.register-link a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    min-height: auto;
  }
  .login-illustration {
    padding: calc(var(--spacing-unit) * 5) var(--spacing-3);
    text-align: center;
  }
  .illustration-content h1 {
    font-size: var(--text-xl);
  }
  .login-form-container {
    padding: calc(var(--spacing-unit) * 5) var(--spacing-3);
  }
}
</style>

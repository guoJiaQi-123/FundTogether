<template>
  <div class="register-container">
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
    <div class="register-wrapper">
      <div class="register-illustration">
        <div class="illustration-content">
          <h1>{{ t('register.journeyTitle') }}</h1>
          <p>{{ t('register.journeySubtitle') }}</p>
        </div>
      </div>
      <div class="register-form-container">
        <div class="register-header">
          <h2>{{ t('register.createAccount') }}</h2>
          <p>{{ t('register.joinToday') }}</p>
        </div>
        
        <el-form 
          ref="registerFormRef" 
          :model="registerForm" 
          :rules="rules" 
          label-width="0"
          class="register-form"
        >
          <el-form-item prop="registerType">
            <el-radio-group v-model="registerForm.registerType" class="type-selector">
              <el-radio-button label="phone">{{ t('register.phone') }}</el-radio-button>
              <el-radio-button label="email">{{ t('register.email') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item prop="account">
            <el-input 
              v-model="registerForm.account" 
              :placeholder="registerForm.registerType === 'phone' ? t('register.phonePlaceholder') : t('register.emailPlaceholder')"
              size="large"
              class="custom-input"
            ></el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              :placeholder="t('register.passwordPlaceholder')" 
              show-password
              size="large"
              class="custom-input"
            ></el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              :placeholder="t('register.confirmPassword')" 
              show-password
              size="large"
              class="custom-input"
            ></el-input>
          </el-form-item>

          <el-form-item prop="code">
            <div class="code-container">
              <el-input 
                v-model="registerForm.code" 
                :placeholder="t('register.codePlaceholder')"
                size="large"
                class="custom-input"
              ></el-input>
              <el-button type="primary" class="code-btn" @click="sendCode" :disabled="countdown > 0" size="large" plain>
                {{ countdown > 0 ? `${countdown}s` : t('register.getCode') }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="agreement" class="agreement-item">
            <el-checkbox v-model="registerForm.agreement">
              {{ t('register.agreeTo') }} <el-link type="primary" :underline="false">{{ t('register.terms') }}</el-link>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="submitForm(registerFormRef)" :loading="loading" size="large">
              {{ t('register.submit') }}
            </el-button>
          </el-form-item>

          <div class="login-link">
            {{ t('register.hasAccount') }} <router-link to="/login">{{ t('register.signIn') }}</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { registerUser } from '../api/user'

const { t, locale } = useI18n()

const handleSetLanguage = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
}

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)

const registerForm = reactive({
  registerType: 'phone',
  account: '',
  password: '',
  confirmPassword: '',
  code: '',
  agreement: false
})

const validateAccount = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error(registerForm.registerType === 'phone' ? t('register.enterPhone') : t('register.enterEmail')))
  } else {
    if (registerForm.registerType === 'phone') {
      const phoneReg = /^1[3-9]\d{9}$/
      if (!phoneReg.test(value)) {
        callback(new Error(t('register.invalidPhone')))
        return
      }
    } else {
      const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
      if (!emailReg.test(value)) {
        callback(new Error(t('register.invalidEmail')))
        return
      }
    }
    callback()
  }
}

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error(t('register.enterConfirm')))
  } else if (value !== registerForm.password) {
    callback(new Error(t('register.passwordMismatch')))
  } else {
    callback()
  }
}

const validateAgreement = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error(t('register.checkAgreement')))
  } else {
    callback()
  }
}

const rules = computed<FormRules>(() => ({
  account: [
    { required: true, validator: validateAccount, trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('register.enterPassword'), trigger: 'blur' },
    { min: 6, max: 20, message: t('register.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  code: [
    { required: true, message: t('register.enterCode'), trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}))

const sendCode = () => {
  if (!registerForm.account) {
    ElMessage.warning(registerForm.registerType === 'phone' ? t('register.enterPhone') : t('register.enterEmail'))
    return
  }
  ElMessage.success(t('register.codeSent'))
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await registerUser({
          registerType: registerForm.registerType,
          account: registerForm.account,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword,
          code: registerForm.code
        })
        ElMessage.success(t('register.success'))
        router.push('/login')
      } catch (error) {
        // Error is handled in request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
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

.register-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}

.register-illustration {
  flex: 1;
  background: var(--secondary-3);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: calc(var(--spacing-unit) * 5);
  position: relative;
  overflow: hidden;
}

.register-illustration::before {
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

.register-form-container {
  flex: 1;
  padding: calc(var(--spacing-unit) * 8);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.register-header {
  margin-bottom: calc(var(--spacing-unit) * 5);
}

.register-header h2 {
  font-family: var(--font-heading);
  font-size: var(--text-xl);
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-1);
}

.register-header p {
  color: var(--text-secondary);
  font-size: var(--text-base);
}

.register-form {
  width: 100%;
}

.type-selector {
  width: 100%;
  display: flex;
}

.type-selector :deep(.el-radio-button) {
  flex: 1;
}

.type-selector :deep(.el-radio-button__inner) {
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

.code-container {
  display: flex;
  gap: var(--spacing-2);
  width: 100%;
}

.code-btn {
  width: 120px;
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

.agreement-item {
  margin-bottom: var(--spacing-3);
}

.login-link {
  text-align: center;
  margin-top: var(--spacing-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.login-link a {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: text-decoration 200ms ease-out;
}

.login-link a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .register-wrapper {
    flex-direction: column;
    min-height: auto;
  }
  .register-illustration {
    padding: calc(var(--spacing-unit) * 5) var(--spacing-3);
    text-align: center;
  }
  .illustration-content h1 {
    font-size: var(--text-xl);
  }
  .register-form-container {
    padding: calc(var(--spacing-unit) * 5) var(--spacing-3);
  }
}
</style>

<template>
  <div class="user-home-container">
    <el-card class="user-home-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button text @click="router.back()">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="title">用户主页</span>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" animated :rows="6" />

      <div v-else class="profile-layout">
        <div class="profile-hero">
          <el-avatar :size="84" :src="avatarSrc" class="avatar" />
          <div class="hero-main">
            <div class="name-row">
              <span class="nickname">{{ profile?.nickname || `用户 ${userId}` }}</span>
              <el-tag v-if="profile?.gender !== null && profile?.gender !== undefined" size="small" effect="plain" type="info">
                {{ genderText(profile?.gender) }}
              </el-tag>
            </div>
            <div class="bio">{{ profile?.bio || '这个人很懒，什么都没留下...' }}</div>
            <div class="meta-tags">
              <el-tag v-if="profile?.profession" size="small" effect="plain" type="info">{{ profile.profession }}</el-tag>
              <el-tag v-if="profile?.company" size="small" effect="plain" type="info">{{ profile.company }}</el-tag>
              <el-tag v-if="profile?.location" size="small" effect="plain" type="info">{{ profile.location }}</el-tag>
              <el-tag v-if="profile?.education" size="small" effect="plain" type="info">{{ profile.education }}</el-tag>
            </div>
          </div>
        </div>

        <el-divider />

        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ profile?.id ?? userId }}</el-descriptions-item>
          <el-descriptions-item label="加入时间">{{ formatDateTime(profile?.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ profile?.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所在地">{{ profile?.location || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getPublicUserProfile } from '../api/user'
import defaultAvatar from '../assets/default-avatar.svg'

const route = useRoute()
const router = useRouter()

const userId = computed(() => Number(route.params.id))
const loading = ref(false)
const profile = ref<any>(null)

const avatarSrc = computed(() => profile.value?.avatar || defaultAvatar)

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

const genderText = (gender?: number) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '保密'
}

const loadProfile = async () => {
  if (!userId.value || Number.isNaN(userId.value)) {
    ElMessage.error('用户ID不合法')
    return
  }
  loading.value = true
  try {
    const res: any = await getPublicUserProfile(userId.value)
    profile.value = res.data
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.user-home-container {
  max-width: 1200px;
  margin: 0 auto;
  animation: fadeIn 0.35s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-home-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
}

.profile-layout {
  padding: 6px 0;
}

.profile-hero {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.avatar {
  box-shadow: 0 10px 26px -18px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.6);
}

.hero-main {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.nickname {
  font-size: 22px;
  font-weight: 900;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.bio {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.meta-tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>

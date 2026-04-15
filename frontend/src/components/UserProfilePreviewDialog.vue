<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    width="560px"
    align-center
    class="user-preview-dialog"
  >
    <template #header>
      <div class="dialog-hero">
        <div class="hero-bg" />
        <div class="hero-row">
          <el-avatar :size="72" :src="avatarSrc" class="hero-avatar" />
          <div class="hero-main">
            <div class="hero-title">
              <span class="name">{{ profile?.nickname || `用户 ${safeUserId ?? '-'}` }}</span>
              <el-tag v-if="profileLoaded" size="small" effect="plain" type="info" class="gender-tag">
                {{ genderText(profile?.gender) }}
              </el-tag>
            </div>
            <div class="hero-sub">
              <span class="sub-item">ID: {{ profile?.id ?? safeUserId ?? '-' }}</span>
              <span class="dot">·</span>
              <span class="sub-item">加入: {{ formatDate(profile?.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div class="dialog-body">
      <el-skeleton v-if="loading" animated :rows="5" />

      <template v-else>
        <div class="bio">
          {{ profile?.bio || '这个人很懒，什么都没留下...' }}
        </div>

        <div class="chips">
          <el-tag v-if="profile?.profession" size="small" effect="plain" type="info">{{ profile.profession }}</el-tag>
          <el-tag v-if="profile?.company" size="small" effect="plain" type="info">{{ profile.company }}</el-tag>
          <el-tag v-if="profile?.location" size="small" effect="plain" type="info">{{ profile.location }}</el-tag>
          <el-tag v-if="profile?.education" size="small" effect="plain" type="info">{{ profile.education }}</el-tag>
        </div>

        <el-divider class="divider" />

        <el-descriptions :column="2" border>
          <el-descriptions-item label="生日">{{ profile?.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所在地">{{ profile?.location || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getPublicUserProfile } from '../api/user'
import defaultAvatar from '../assets/default-avatar.svg'

const props = defineProps<{
  modelValue: boolean
  userId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
}>()

const loading = ref(false)
const profile = ref<any>(null)

const safeUserId = computed(() => {
  const id = props.userId
  return typeof id === 'number' && !Number.isNaN(id) && id > 0 ? id : null
})

const profileLoaded = computed(() => !!profile.value)

const avatarSrc = computed(() => profile.value?.avatar || defaultAvatar)

const formatDate = (value?: string) => {
  if (!value) return '-'
  return new Date(value).toLocaleDateString()
}

const genderText = (gender?: number) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '保密'
}

const loadProfile = async () => {
  if (!safeUserId.value) return
  loading.value = true
  try {
    const res: any = await getPublicUserProfile(safeUserId.value)
    profile.value = res.data
  } catch (e) {
    ElMessage.error('获取用户信息失败')
    profile.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue, safeUserId.value] as const,
  ([visible, id], [prevVisible, prevId]) => {
    if (!visible) return
    // Fetch on open or when switching user while dialog open.
    if (!prevVisible || id !== prevId) {
      loadProfile()
    }
  }
)
</script>

<style scoped>
.user-preview-dialog :deep(.el-dialog) {
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 30px 90px -40px rgba(15, 23, 42, 0.6);
}

.user-preview-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin-right: 0;
}

.user-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-hero {
  position: relative;
  padding: 18px 18px 14px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(800px 200px at 0% 0%, rgba(99, 102, 241, 0.18), transparent 60%),
    radial-gradient(520px 220px at 100% 0%, rgba(34, 211, 238, 0.14), transparent 62%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.86));
}

.hero-row {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-avatar {
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 18px 40px -28px rgba(15, 23, 42, 0.55);
  background: #fff;
}

.hero-main {
  min-width: 0;
}

.hero-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.name {
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -0.01em;
  color: rgba(15, 23, 42, 0.92);
}

.gender-tag {
  transform: translateY(1px);
}

.hero-sub {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(15, 23, 42, 0.55);
  font-size: 12px;
}

.dot {
  opacity: 0.8;
}

.dialog-body {
  padding: 16px 18px 18px;
  background: rgba(255, 255, 255, 0.95);
}

.bio {
  color: rgba(15, 23, 42, 0.76);
  font-size: 14px;
  line-height: 1.65;
  padding: 12px 12px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.03);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.chips {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.divider {
  margin: 14px 0;
}
</style>


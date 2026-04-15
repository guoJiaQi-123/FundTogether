<template>
  <el-button
    v-if="!isSelf"
    :type="isFollowing ? 'info' : 'primary'"
    :plain="isFollowing"
    :loading="loading"
    :size="size"
    round
    @click="handleClick"
  >
    <el-icon v-if="!loading"><Plus v-if="!isFollowing" /><Check v-else /></el-icon>
    {{ isFollowing ? '已关注' : '关注' }}
  </el-button>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Check } from '@element-plus/icons-vue'
import { followUser, unfollowUser, getFollowStatus } from '../api/follow'
import { useUserStore } from '../store/user'

const props = defineProps<{
  userId: number
  size?: 'small' | 'default' | 'large'
  initialFollowing?: boolean
}>()

const emit = defineEmits<{
  (e: 'followChanged', data: { isFollowing: boolean; followingCount: number; followerCount: number }): void
}>()

const userStore = useUserStore()
const isFollowing = ref(false)
const loading = ref(false)

const isSelf = computed(() => {
  return userStore.userInfo?.id === props.userId
})

const loadStatus = async () => {
  if (isSelf.value) return
  try {
    const res: any = await getFollowStatus(props.userId)
    if (res.code === 200 && res.data) {
      isFollowing.value = res.data.isFollowing
    }
  } catch (e) {
    // ignore
  }
}

const handleClick = async () => {
  if (loading.value) return
  loading.value = true
  try {
    if (isFollowing.value) {
      await ElMessageBox.confirm('确定要取消关注吗？', '取消关注', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res: any = await unfollowUser(props.userId)
      if (res.code === 200 && res.data) {
        isFollowing.value = res.data.isFollowing
        ElMessage.success('已取消关注')
        emit('followChanged', {
          isFollowing: res.data.isFollowing,
          followingCount: res.data.followingCount,
          followerCount: res.data.followerCount
        })
      }
    } else {
      const res: any = await followUser(props.userId)
      if (res.code === 200 && res.data) {
        isFollowing.value = res.data.isFollowing
        ElMessage.success('关注成功')
        emit('followChanged', {
          isFollowing: res.data.isFollowing,
          followingCount: res.data.followingCount,
          followerCount: res.data.followerCount
        })
      }
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error(e?.message || '操作失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (props.initialFollowing !== undefined) {
    isFollowing.value = props.initialFollowing
  } else {
    loadStatus()
  }
})

watch(() => props.userId, () => {
  loadStatus()
})
</script>

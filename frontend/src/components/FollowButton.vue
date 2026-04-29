<!--
  @组件名称: FollowButton
  @组件用途: 关注/取消关注按钮组件。用于在用户个人资料、关注列表等场景中
            提供关注与取消关注操作，支持加载状态、自身用户隐藏、
            取消关注二次确认等功能。
  @外部依赖: Element Plus 按钮/图标/消息框/消息提示，关注相关 API，用户状态仓库
-->
<template>
  <!-- 仅当目标用户不是当前登录用户自身时才显示按钮 -->
  <el-button
    v-if="!isSelf"
    :type="isFollowing ? 'info' : 'primary'"
    :plain="isFollowing"
    :loading="loading"
    :size="size"
    round
    @click="handleClick"
  >
    <!-- 根据关注状态显示不同图标：未关注显示加号，已关注显示对勾 -->
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

/** 组件属性定义 */
const props = defineProps<{
  /** 目标用户ID */
  userId: number
  /** 按钮尺寸，可选值为 small / default / large */
  size?: 'small' | 'default' | 'large'
  /** 初始关注状态，若传入则跳过接口查询，直接使用该值 */
  initialFollowing?: boolean
}>()

/** 组件事件定义 */
const emit = defineEmits<{
  /** 关注状态变化时触发，携带最新的关注状态及关注/粉丝计数 */
  (e: 'followChanged', data: { isFollowing: boolean; followingCount: number; followerCount: number }): void
}>()

/** 用户状态仓库实例，用于获取当前登录用户信息 */
const userStore = useUserStore()

/** 当前是否已关注目标用户 */
const isFollowing = ref(false)

/** 按钮是否处于加载状态（请求进行中） */
const loading = ref(false)

/** 计算属性：判断目标用户是否为当前登录用户自身 */
const isSelf = computed(() => {
  return userStore.userInfo?.id === props.userId
})

/**
 * 从服务端加载当前用户对目标用户的关注状态
 * @returns {Promise<void>} 无返回值
 */
const loadStatus = async () => {
  if (isSelf.value) return
  try {
    const res: any = await getFollowStatus(props.userId)
    if (res.code === 200 && res.data) {
      isFollowing.value = res.data.isFollowing
    }
  } catch (e) {
    // 忽略加载失败，保持当前状态
  }
}

/**
 * 处理按钮点击事件
 * - 已关注状态：弹出确认框后取消关注
 * - 未关注状态：直接发起关注请求
 * @returns {Promise<void>} 无返回值
 */
const handleClick = async () => {
  if (loading.value) return
  loading.value = true
  try {
    if (isFollowing.value) {
      // 已关注 → 取消关注（需二次确认）
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
      // 未关注 → 发起关注
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
    // 用户取消确认框时不提示错误，其他异常才提示
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error(e?.message || '操作失败')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 组件挂载时的生命周期钩子
 * 优先使用 initialFollowing 属性初始化关注状态，
 * 若未传入则从服务端查询
 */
onMounted(() => {
  if (props.initialFollowing !== undefined) {
    isFollowing.value = props.initialFollowing
  } else {
    loadStatus()
  }
})

/**
 * 监听目标用户ID变化，重新加载关注状态
 * 用于同一组件实例切换不同用户的场景
 */
watch(() => props.userId, () => {
  loadStatus()
})
</script>

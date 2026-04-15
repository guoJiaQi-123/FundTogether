<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    width="480px"
    align-center
    class="follow-list-dialog"
  >
    <template #header>
      <div class="dialog-tabs">
        <span
          class="tab-item"
          :class="{ active: activeTab === 'following' }"
          @click="activeTab = 'following'"
        >关注 {{ followingCount }}</span>
        <span
          class="tab-item"
          :class="{ active: activeTab === 'followers' }"
          @click="activeTab = 'followers'"
        >粉丝 {{ followerCount }}</span>
      </div>
    </template>

    <div class="follow-list-body" v-loading="listLoading">
      <div v-if="list.length === 0 && !listLoading" class="empty-state">
        <el-empty :description="activeTab === 'following' ? '暂无关注' : '暂无粉丝'" :image-size="80" />
      </div>
      <div v-else class="user-list">
        <div v-for="user in list" :key="user.id" class="user-item">
          <el-avatar :size="40" :src="user.avatar || defaultAvatar" class="user-avatar" />
          <div class="user-info">
            <span class="user-nickname">{{ user.nickname || `用户 ${user.id}` }}</span>
            <span class="user-bio">{{ user.bio || '' }}</span>
          </div>
          <FollowButton
            :user-id="user.id"
            :initial-following="user.isFollowing"
            size="small"
            @follow-changed="onFollowChanged"
          />
        </div>
      </div>
      <div v-if="hasMore" class="load-more">
        <el-button text type="primary" @click="loadMore" :loading="loadingMore">
          加载更多
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getFollowingList, getFollowerList } from '../api/follow'
import FollowButton from './FollowButton.vue'
import defaultAvatar from '../assets/default-avatar.svg'

const props = defineProps<{
  modelValue: boolean
  userId: number
  followingCount: number
  followerCount: number
  initialTab?: 'following' | 'followers'
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'countsChanged', data: { followingCount: number; followerCount: number }): void
}>()

const activeTab = ref<'following' | 'followers'>(props.initialTab || 'following')
const list = ref<any[]>([])
const listLoading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const hasMore = ref(false)
const pageSize = 20

const fetchList = async (reset: boolean = true) => {
  if (reset) {
    page.value = 1
    list.value = []
  }

  listLoading.value = reset
  loadingMore.value = !reset

  try {
    const fetchFn = activeTab.value === 'following' ? getFollowingList : getFollowerList
    const res: any = await fetchFn(props.userId, page.value, pageSize)
    if (res.code === 200 && res.data) {
      if (reset) {
        list.value = res.data
      } else {
        list.value = [...list.value, ...res.data]
      }
      hasMore.value = res.data.length === pageSize
    }
  } catch (e) {
    // ignore
  } finally {
    listLoading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchList(false)
}

const onFollowChanged = (data: { isFollowing: boolean; followingCount: number; followerCount: number }) => {
  emit('countsChanged', {
    followingCount: data.followingCount,
    followerCount: data.followerCount
  })
  fetchList()
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      activeTab.value = props.initialTab || 'following'
      fetchList()
    }
  }
)
</script>

<style scoped>
.dialog-tabs {
  display: flex;
  gap: 32px;
  justify-content: center;
}

.tab-item {
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  padding-bottom: 8px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.tab-item.active {
  color: var(--el-color-primary);
  border-bottom-color: var(--el-color-primary);
}

.tab-item:hover {
  color: var(--el-color-primary);
}

.follow-list-body {
  min-height: 200px;
  max-height: 480px;
  overflow-y: auto;
}

.empty-state {
  padding: 40px 0;
}

.user-list {
  padding: 0 4px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-radius: 12px;
  transition: background 0.2s;
}

.user-item:hover {
  background: var(--el-fill-color-light);
}

.user-avatar {
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-nickname {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-bio {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.load-more {
  text-align: center;
  padding: 12px 0;
}
</style>

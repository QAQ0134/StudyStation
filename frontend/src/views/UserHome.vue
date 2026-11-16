<template>
  <div class="user-home-container">
    <!-- 顶部导航栏 (复用之前的) -->
    <el-header class="header">
      <div class="global-logo" @click="goHome">
        <el-icon :size="26" color="#409eff"><Reading /></el-icon>
        <span class="logo-text">StudyStation</span>
      </div>
      <div class="header-content">
        <div class="left-section">
          <div class="menu-item home-btn" @click="goHome">
            <el-icon :size="20" class="icon"><HomeFilled /></el-icon>
            <span>返回首页</span>
          </div>
        </div>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <div class="main-wrapper" v-loading="loading">
      <div class="user-profile-header">
        <!-- 左侧：头像 -->
        <el-avatar :size="100" :src="getImageUrl(userInfo.avatar) || defaultAvatar" />
        <!-- 右侧：信息 -->
        <div class="user-info">
          <h2 class="nickname">{{ userInfo.nickname || userInfo.username }}</h2>
          <p class="username">@{{ userInfo.username }}</p>
          <p class="signature">{{ userInfo.signature || '这个人很懒，什么都没留下~' }}</p>
          <div class="tags">
            <el-tag v-if="userInfo.gender === 1" type="primary" effect="plain">男</el-tag>
            <el-tag v-else-if="userInfo.gender === 0" type="danger" effect="plain">女</el-tag>
            <el-tag v-if="userInfo.region" type="info" effect="plain">{{ userInfo.region }}</el-tag>
          </div>
        </div>
      </div>

      <el-divider content-position="left">Ta 的作品 ({{ userWorks.length }} 篇)</el-divider>

      <!-- 作品列表 -->
      <div class="work-list" v-if="userWorks.length > 0">
        <el-card 
          v-for="work in userWorks" 
          :key="work.id" 
          class="work-card" 
          shadow="hover"
          @click="goToDetail(work.id)"
        >
          <div class="card-cover" v-if="work.coverImage">
            <img :src="getImageUrl(work.coverImage)" alt="cover" />
          </div>
          <div class="card-info">
            <h4 class="work-title">{{ work.title }}</h4>
            <p class="work-summary">{{ getContentSummary(work.content) }}</p>
            <div class="card-footer">
              <span class="create-time">{{ formatTime(work.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="Ta 还没有发布过作品~" :image-size="120" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Reading, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import defaultAvatar from '@/assets/default-avatar.png'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const userInfo = ref({})
const userWorks = ref([])

const goHome = () => router.push('/home')
const goToDetail = (id) => router.push(`/work/${id}`)

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const getContentSummary = (contentJson) => {
  try {
    if (!contentJson) return ''
    const blocks = JSON.parse(contentJson)
    const textBlock = blocks.find(b => b.type === 'text')
    if (textBlock && textBlock.value) {
      return textBlock.value.length > 80 ? textBlock.value.substring(0, 80) + '...' : textBlock.value
    }
    return '点击查看详情...'
  } catch (e) {
    return '内容解析失败'
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 加载数据
const loadUserData = async () => {
  const userId = route.params.userId
  if (!userId) return

  loading.value = true
  try {
    // 并行请求用户信息和作品
    const [userRes, worksRes] = await Promise.all([
      request.get(`/user/${userId}`),
      request.get(`/work/user/${userId}`)
    ])

    if (userRes.code === 1) {
      userInfo.value = userRes.data
    }
    if (worksRes.code === 1) {
      userWorks.value = worksRes.data
    }
  } catch (error) {
    ElMessage.error('获取用户资料失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.user-home-container { min-height: 100vh; background-color: #f5f7fa; display: flex; flex-direction: column; }
.header { background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); height: 60px; padding: 0; flex-shrink: 0; position: sticky; top: 0; z-index: 100; }
.global-logo { position: absolute; left: 20px; top: 0; height: 60px; display: flex; align-items: center; gap: 8px; cursor: pointer; z-index: 101; }
.logo-text { font-size: 22px; font-weight: 800; background: linear-gradient(45deg, #409eff, #79bbff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-content { max-width: 900px; margin: 0 auto; height: 100%; display: flex; align-items: center; justify-content: space-between; padding-left: 260px; padding-right: 20px; }
.menu-item { display: flex; align-items: center; gap: 4px; cursor: pointer; color: #606266; font-size: 14px; }
.menu-item:hover { color: #409eff; }

.main-wrapper { flex: 1; max-width: 900px; width: 100%; margin: 30px auto; padding: 0 20px; box-sizing: border-box; }

/* 用户头部信息 */
.user-profile-header {
  background-color: #fff;
  padding: 30px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 30px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.user-info .nickname { margin: 0; font-size: 26px; color: #303133; }
.user-info .username { margin: 5px 0 10px; font-size: 14px; color: #909399; }
.user-info .signature { margin: 0 0 15px; font-size: 14px; color: #606266; }
.user-info .tags { display: flex; gap: 10px; }

/* 作品列表复用 */
.work-list { display: flex; flex-direction: column; gap: 16px; margin-top: 20px; }
.work-card { cursor: pointer; transition: transform 0.2s; border-radius: 8px; }
.work-card:hover { transform: translateY(-2px); }
.work-card :deep(.el-card__body) { display: flex; padding: 16px; gap: 20px; }
.card-cover { width: 200px; height: 130px; flex-shrink: 0; border-radius: 6px; overflow: hidden; }
.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.card-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.work-title { margin: 0 0 10px 0; font-size: 18px; color: #303133; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; }
.work-summary { margin: 0; font-size: 14px; color: #606266; line-height: 1.6; flex: 1; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.create-time { font-size: 13px; color: #909399; }
</style>
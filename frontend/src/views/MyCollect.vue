<template>
  <div class="collect-container">
    <!-- 顶部导航栏 -->
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
      <div class="page-header">
        <h2>我的收藏</h2>
        <span class="count">共 {{ collectList.length }} 篇</span>
      </div>

      <div class="work-list" v-if="collectList.length > 0">
        <el-card 
          v-for="work in collectList" 
          :key="work.id" 
          class="work-card" 
          shadow="hover"
          @click="goToDetail(work.id)"
        >
          <!-- 封面图 -->
          <div class="card-cover" v-if="work.coverImage">
            <img :src="getImageUrl(work.coverImage)" alt="cover" />
          </div>
          
          <div class="card-info">
            <h4 class="work-title">{{ work.title }}</h4>
            <p class="work-summary">{{ getContentSummary(work.content) }}</p>
            <div class="card-footer">
              <div class="author-info">
                <el-avatar :size="24" :src="work.authorAvatar ? getImageUrl(work.authorAvatar) : defaultAvatar" />
                <span class="author-name">{{ work.authorName || '匿名用户' }}</span>
              </div>
              <span class="create-time">{{ formatTime(work.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-else-if="!loading">
        <el-empty description="你还没有收藏任何作品，快去逛逛吧~" :image-size="150">
          <el-button type="primary" @click="goHome">去首页看看</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import defaultAvatar from '@/assets/default-avatar.png'

const router = useRouter()
const loading = ref(false)
const collectList = ref([])
//const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

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
    return '点击查看图片详情...'
  } catch (e) {
    return '内容解析失败'
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 加载收藏数据
const loadCollectList = async () => {
  loading.value = true
  try {
    const res = await request.get('/work/myCollect')
    if (res.code === 1) {
      collectList.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCollectList()
})
</script>

<style scoped>
.collect-container { min-height: 100vh; background-color: #f5f7fa; display: flex; flex-direction: column; }
.header { background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); height: 60px; padding: 0; flex-shrink: 0; position: sticky; top: 0; z-index: 100; }
.global-logo { position: absolute; left: 20px; top: 0; height: 60px; display: flex; align-items: center; gap: 8px; cursor: pointer; z-index: 101; }
.logo-text { font-size: 22px; font-weight: 800; background: linear-gradient(45deg, #409eff, #79bbff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-content { max-width: 1000px; margin: 0 auto; height: 100%; display: flex; align-items: center; justify-content: space-between; padding-left: 260px; padding-right: 20px; }
.menu-item { display: flex; align-items: center; gap: 4px; cursor: pointer; color: #606266; font-size: 14px; }
.menu-item:hover { color: #409eff; }

.main-wrapper {
  flex: 1;
  max-width: 1000px;
  width: 100%;
  margin: 30px auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: baseline;
  gap: 15px;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 24px; color: #303133; }
.page-header .count { font-size: 14px; color: #909399; }

/* 卡片流样式（复用首页） */
.work-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.work-card {
  cursor: pointer;
  transition: transform 0.2s;
  border-radius: 8px;
}
.work-card:hover { transform: translateY(-2px); }
.work-card :deep(.el-card__body) {
  display: flex;
  padding: 16px;
  gap: 20px;
}
.card-cover {
  width: 200px;
  height: 130px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
}
.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.work-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}
.work-summary {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}
.author-info { display: flex; align-items: center; gap: 8px; }
.author-name { font-size: 13px; color: #909399; }
.create-time { font-size: 13px; color: #909399; }

.empty-state { margin-top: 80px; text-align: center; }
</style>
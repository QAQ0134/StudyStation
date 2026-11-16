<template>
  <div class="detail-container">
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
      <el-card class="article-card" v-if="workInfo.id">
        <!-- 文章头部 -->
        <div class="article-header">
          <h1 class="title">{{ workInfo.title }}</h1>
          <div class="meta-info">
            <!-- 点击头像或名字跳转到用户主页 -->
            <div class="author-box" style="cursor: pointer;" @click="goToUserProfile(workInfo.userId)">
              <el-avatar :size="40" :src="getImageUrl(workInfo.authorAvatar) || defaultAvatar" />
              <div class="author-text">
                <span class="name">{{ workInfo.authorName || '匿名用户' }}</span>
                <span class="time">
                  发布于 {{ formatTime(workInfo.createTime) }} · 
                  <el-icon><View /></el-icon> {{ workInfo.viewCount || 0 }} 阅读
                </span>
              </div>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 文章正文（动态渲染块） -->
        <div class="article-content">
          <div 
            v-for="(block, index) in parsedContent" 
            :key="index"
            class="content-block"
          >
            <!-- 渲染文本 -->
            <p v-if="block.type === 'text'" class="text-block">
              {{ block.value }}
            </p>
            <!-- 渲染图片 -->
            <div v-if="block.type === 'image'" class="image-block">
              <el-image 
                :src="getImageUrl(block.value)" 
                :preview-src-list="[getImageUrl(block.value)]"
                fit="contain"
              />
            </div>
          </div>
        </div>

        <!-- 知识点标签区域 -->
        <div class="tag-section" v-if="workInfo.knowledgeTags && workInfo.knowledgeTags.length > 0">
          <span class="tag-label">相关知识点：</span>
          <el-tag 
            v-for="tag in workInfo.knowledgeTags" 
            :key="tag.id" 
            type="info" 
            effect="plain"
            class="kp-tag"
          >
            # {{ tag.name }}
          </el-tag>
        </div>

        <el-divider />

        <!-- 底部互动区 -->
        <div class="interaction-bar">
          <el-button 
            :type="isLiked ? 'primary' : 'default'" 
            round 
            size="large"
            @click="handleLike"
            :loading="likeLoading"
          >
            <el-icon v-if="!isLiked"><Pointer /></el-icon>
            <el-icon v-else><Check /></el-icon>
            {{ isLiked ? '已点赞' : '点赞' }} ({{ workInfo.likeCount || 0 }})
          </el-button>

          <el-button 
            :type="isCollected ? 'warning' : 'default'" 
            round 
            size="large"
            @click="handleCollect"
            :loading="collectLoading"
          >
            <el-icon v-if="!isCollected"><Star /></el-icon>
            <el-icon v-else><StarFilled /></el-icon>
            {{ isCollected ? '已收藏' : '收藏' }} ({{ workInfo.collectCount || 0 }})
          </el-button>
        </div>

        <!-- 👇 作者或管理员删除文章按钮 👇 -->
        <div class="delete-article-bar" v-if="canDeleteWork">
          <el-button type="danger" plain @click="handleDeleteWork">
            <el-icon><Delete /></el-icon> 删除此文章
          </el-button>
        </div>

        <!-- ================= 评论区 ================= -->
        <el-divider content-position="center">评论区</el-divider>
        
        <div class="comment-section">
          <!-- 发评论输入框 -->
          <div class="comment-input-box">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="留下你的看法吧..."
              maxlength="200"
              show-word-limit
            />
            <div class="comment-action">
              <el-button type="primary" @click="submitComment" :loading="commentLoading">
                发表评论
              </el-button>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list" v-if="comments.length > 0">
            <div class="comment-item" v-for="item in comments" :key="item.id">
              <!-- 点击评论者头像跳转 -->
              <div style="cursor: pointer;" @click="goToUserProfile(item.userId)">
                <el-avatar :size="36" :src="getImageUrl(item.avatar) || defaultAvatar" />
              </div>
              <div class="comment-content">
                <div class="comment-header">
                  <!-- 点击评论者名字跳转 -->
                  <span class="name" style="cursor: pointer;" @click="goToUserProfile(item.userId)">
                    {{ item.nickname || item.username }}
                  </span>
                  <!-- 如果评论人 ID 和文章作者 ID 一样，显示作者标签 -->
                  <el-tag v-if="item.userId === workInfo.userId" type="warning" size="small" effect="dark">
                    作者
                  </el-tag>
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                  <el-button 
                    v-if="canDeleteComment(item)" 
                    type="danger" 
                    link 
                    size="small" 
                    class="delete-btn"
                    @click="handleDeleteComment(item.id)"
                  >
                    删除
                  </el-button>
                </div>
                <div class="comment-text">{{ item.content }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无评论，快来抢沙发吧！" :image-size="80" />
        </div>
      </el-card>

      <!-- 未找到或加载失败 -->
      <el-empty v-else-if="!loading" description="文章不存在或已被删除" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Reading, HomeFilled, View, Pointer, Check, Star, StarFilled, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import defaultAvatar from '@/assets/default-avatar.png'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const workInfo = ref({})

// 互动状态
const isLiked = ref(false)
const isCollected = ref(false)
const likeLoading = ref(false)
const collectLoading = ref(false)

// 当前登录用户ID
const currentUserId = ref(null)

// 评论数据
const comments = ref([])
const newComment = ref('')
const commentLoading = ref(false)

// 解析 JSON 内容
const parsedContent = computed(() => {
  if (!workInfo.value.content) return []
  try {
    return JSON.parse(workInfo.value.content)
  } catch (e) {
    console.error('内容解析失败', e)
    return []
  }
})

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const goHome = () => router.push('/home')

// 👇 新增：跳转到指定用户的主页
const goToUserProfile = (userId) => {
  if (!userId) return
  // 如果点的是自己的头像/名字，直接跳普通个人中心
  if (userId === currentUserId.value) {
    router.push('/profile')
  } else {
    // 如果点的是别人，带上 ?id=xxx 参数
    router.push(`/profile?id=${userId}`)
  }
}

// 获取文章详情
const loadDetail = async () => {
  const id = route.params.id
  if (!id) return
  
  loading.value = true
  try {
    const res = await request.get(`/work/${id}`)
    if (res.code === 1) {
      workInfo.value = res.data
    } else {
      ElMessage.error(res.msg || '文章不存在')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

// 获取互动状态
const loadInteractionStatus = async () => {
  const token = localStorage.getItem('token')
  if (!token) return 

  try {
    const res = await request.get(`/work/${workInfo.value.id}/status`)
    if (res.code === 1) {
      isLiked.value = res.data.isLiked
      isCollected.value = res.data.isCollected
    }
  } catch (error) {
    console.error('获取互动状态失败', error)
  }
}

// 点赞
const handleLike = async () => {
  if (!localStorage.getItem('token')) return ElMessage.warning('请先登录后再点赞')

  likeLoading.value = true
  try {
    const newLikeState = !isLiked.value
    const res = await request.post(`/work/${workInfo.value.id}/like?isLike=${newLikeState}`)
    if (res.code === 1) {
      isLiked.value = newLikeState
      workInfo.value.likeCount += newLikeState ? 1 : -1
      ElMessage.success(newLikeState ? '点赞成功' : '已取消点赞')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    likeLoading.value = false
  }
}

// 收藏
const handleCollect = async () => {
  if (!localStorage.getItem('token')) return ElMessage.warning('请先登录后再收藏')

  collectLoading.value = true
  try {
    const newCollectState = !isCollected.value
    const res = await request.post(`/work/${workInfo.value.id}/collect?isCollect=${newCollectState}`)
    if (res.code === 1) {
      isCollected.value = newCollectState
      workInfo.value.collectCount += newCollectState ? 1 : -1
      ElMessage.success(newCollectState ? '收藏成功' : '已取消收藏')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    collectLoading.value = false
  }
}

// 加载评论列表
const loadComments = async () => {
  try {
    const res = await request.get(`/comment/work/${workInfo.value.id}`)
    if (res.code === 1) {
      comments.value = res.data
    }
  } catch (error) {
    console.error('获取评论失败', error)
  }
}

// 提交评论
const submitComment = async () => {
  if (!localStorage.getItem('token')) return ElMessage.warning('请先登录后再评论')
  if (!newComment.value.trim()) return ElMessage.warning('评论内容不能为空')

  commentLoading.value = true
  try {
    const res = await request.post('/comment', {
      workId: workInfo.value.id,
      content: newComment.value.trim()
    })
    if (res.code === 1) {
      ElMessage.success('评论成功')
      newComment.value = ''
      loadComments()
    } else {
      ElMessage.error(res.msg || '评论失败')
    }
  } catch (error) {
    ElMessage.error('网络异常，评论失败')
  } finally {
    commentLoading.value = false
  }
}

// 判断是否有权限删除评论（作者本人、文章作者、管理员）
const canDeleteComment = (comment) => {
  if (!currentUserId.value) return false
  
  // 1. 如果当前登录人是管理员 (role == 1)
  const isAdmin = localStorage.getItem('role') === '1'
  if (isAdmin) return true

  // 2. 如果是文章作者
  if (currentUserId.value === workInfo.value.userId) return true

  // 3. 如果是评论者本人
  if (currentUserId.value === comment.userId) return true

  return false
}

// 执行删除评论
const handleDeleteComment = (commentId) => {
  ElMessageBox.confirm('确认要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await request.delete(`/comment/${commentId}`)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        loadComments()
      } else {
        ElMessage.error(res.msg)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 判断是否有权限删除文章
const canDeleteWork = computed(() => {
  if (!currentUserId.value || !workInfo.value.id) return false
  // 当前登录人是文章作者，或者当前登录人是管理员(role 1)
  return currentUserId.value === workInfo.value.userId || localStorage.getItem('role') === '1'
})

// 执行删除文章
const handleDeleteWork = () => {
  ElMessageBox.confirm('确认要删除这篇文章吗？删除后不可恢复，所有评论和收藏也会一并清空！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
  }).then(async () => {
    try {
      const res = await request.delete(`/work/${workInfo.value.id}`)
      if (res.code === 1) {
        ElMessage.success('文章已删除')
        router.push('/home')
      } else {
        ElMessage.error(res.msg)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 统一初始化
onMounted(async () => {
  // 1. 解析 Token 获取当前用户 ID
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      currentUserId.value = payload.id
    } catch (e) {
      console.error('解析Token失败', e)
    }
  }

  // 2. 加载文章详情
  await loadDetail()
  
  // 3. 如果文章加载成功，再去拉取互动状态和评论
  if (workInfo.value.id) {
    loadInteractionStatus()
    loadComments()
  }
})
</script>

<style scoped>
/* 导航栏复用之前的样式 */
.detail-container { min-height: 100vh; background-color: #f5f7fa; display: flex; flex-direction: column; }
.header { background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); height: 60px; padding: 0; flex-shrink: 0; position: sticky; top: 0; z-index: 100; }
.global-logo { position: absolute; left: 20px; top: 0; height: 60px; display: flex; align-items: center; gap: 8px; cursor: pointer; z-index: 101; }
.logo-text { font-size: 22px; font-weight: 800; background: linear-gradient(45deg, #409eff, #79bbff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-content { max-width: 900px; margin: 0 auto; height: 100%; display: flex; align-items: center; justify-content: space-between; padding-left: 260px; padding-right: 20px; }
.menu-item { display: flex; align-items: center; gap: 4px; cursor: pointer; color: #606266; font-size: 14px; }
.menu-item:hover { color: #409eff; }

/* 文章主体 */
.main-wrapper {
  flex: 1;
  max-width: 900px;
  width: 100%;
  margin: 30px auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.article-card {
  border-radius: 12px;
  padding: 20px 40px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.article-header .title {
  font-size: 32px;
  color: #303133;
  margin-bottom: 24px;
  line-height: 1.4;
  font-weight: 700;
}

.meta-info { display: flex; align-items: center; }
.author-box { display: flex; align-items: center; gap: 12px; }
.author-text { display: flex; flex-direction: column; }
.author-text .name { font-size: 15px; color: #303133; font-weight: 500; }
.author-text .time { 
  font-size: 13px; 
  color: #909399; 
  margin-top: 4px; 
  display: flex; 
  align-items: center; 
  gap: 4px; 
}

/* 正文渲染 */
.article-content { margin-top: 20px; font-size: 16px; line-height: 1.8; color: #303133; }
.content-block { margin-bottom: 24px; }
.text-block { margin: 0; white-space: pre-wrap; text-align: justify; }
.image-block { text-align: center; margin: 30px 0; }
.image-block :deep(.el-image) { max-width: 100%; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }

/* 知识点标签 */
.tag-section { margin-top: 40px; display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.tag-label { font-size: 14px; color: #909399; margin-right: 5px; }
.kp-tag { font-size: 14px; border-radius: 12px; padding: 0 12px; }

/* 互动区 */
.interaction-bar { display: flex; justify-content: center; gap: 30px; margin: 40px 0 20px; }
.interaction-bar .el-button { padding: 12px 30px; font-size: 16px; }

/* 文章删除按钮 */
.delete-article-bar {
  text-align: center;
  margin-bottom: 30px;
}

/* ================= 评论区样式 ================= */
.comment-section { margin-top: 20px; padding-bottom: 40px; }
.comment-input-box { margin-bottom: 30px; }
.comment-action { margin-top: 10px; text-align: right; }
.comment-list { display: flex; flex-direction: column; gap: 20px; }
.comment-item { display: flex; gap: 12px; }
.comment-content { flex: 1; background-color: #f9f9f9; padding: 12px 16px; border-radius: 8px; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.comment-header .name { font-size: 14px; font-weight: 600; color: #409eff; }
.comment-header .name:hover { text-decoration: underline; }
.comment-header .time { font-size: 12px; color: #909399; margin-left: auto; }
.delete-btn { margin-left: 10px; color: #f56c6c; }
.comment-text { font-size: 14px; color: #303133; line-height: 1.6; white-space: pre-wrap; }
</style>
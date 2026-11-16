<template>
  <div class="home-container">
    <!-- ================= 顶部导航栏 ================= -->
    <el-header class="header">
      <div class="global-logo" @click="goHome">
        <el-icon :size="26" color="#409eff"><Reading /></el-icon>
        <span class="logo-text">StudyStation</span>
      </div>

      <div class="header-content">
        <div class="left-section">
          <div class="menu-item home-btn" @click="goHome">
            <el-icon :size="20" class="icon"><HomeFilled /></el-icon>
            <span>首页</span>
          </div>
        </div>

        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索你感兴趣的内容..."
            class="search-input"
            clearable
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <div class="right-menu">
          <template v-if="!isLoggedIn">
            <el-button type="primary" plain @click="goToLogin">登录</el-button>
            <el-button type="primary" @click="goToRegister">注册</el-button>
          </template>

          <template v-else>
            <el-badge :value="3" class="menu-item" :max="99">
              <el-icon :size="20" class="icon"><ChatDotRound /></el-icon>
              <span>消息</span>
            </el-badge>

            <div class="menu-item" @click="router.push('/myCollect')">
              <el-icon :size="20" class="icon"><Star /></el-icon>
              <span>收藏</span>
            </div>

            <div class="menu-item" @click="handleAction('关注')">
              <el-icon :size="20" class="icon"><UserFilled /></el-icon>
              <span>关注</span>
            </div>

            <div class="menu-item" @click="router.push('/create')">
              <el-icon :size="20" class="icon"><EditPen /></el-icon>
              <span>创作</span>
            </div>

            <el-dropdown trigger="click" @command="handleCommand">
              <div class="avatar-box">
                <el-avatar 
                  :size="36" 
                  :src="userAvatar ? getImageUrl(userAvatar) : defaultAvatar" 
                />
                <span class="nickname">{{ username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="userRole === '1'" command="admin">后台管理</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </el-header>

    <!-- ================= 页面主体布局 ================= -->
    <el-container class="main-layout">
      <!-- 左侧：常用学科导航 -->
      <el-aside width="200px" class="left-sidebar">
        <el-menu
          :default-active="activeSubjectId"
          class="custom-menu"
          @select="handleMenuSelect"
        >
          <!-- 推荐 -->
          <el-menu-item index="recommend">
            <el-icon><Compass /></el-icon>
            <span>推荐</span>
          </el-menu-item>

          <!-- 动态展示前 6 个学科 -->
          <el-menu-item 
            v-for="subject in displaySubjects" 
            :key="subject.id" 
            :index="subject.id.toString()"
          >
            <el-icon><Collection /></el-icon>
            <span>{{ subject.name }}</span>
          </el-menu-item>

          <!-- 更多（点击弹窗） -->
          <el-menu-item index="more" @click="moreDialogVisible = true">
            <el-icon><MoreFilled /></el-icon>
            <span>更多学科</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧：内容展示区 -->
      <el-main class="right-content" v-loading="loading">
        <div class="content-header">
          <h3 v-if="activeSubjectId === 'recommend'">为你推荐</h3>
          <h3 v-else>{{ getCurrentSubjectName() }} 相关内容</h3>
        </div>

        <!-- 作品列表卡片流 -->
        <div class="work-list" v-if="workList.length > 0">
          <el-card 
            v-for="work in workList" 
            :key="work.id" 
            class="work-card" 
            shadow="hover"
            @click="goToDetail(work.id)"
          >
            <!-- 封面图（如果有） -->
            <div class="card-cover" v-if="work.coverImage">
              <img :src="getImageUrl(work.coverImage)" alt="cover" />
            </div>
            
            <div class="card-info">
              <h4 class="work-title">{{ work.title }}</h4>
              
              <!-- 内容摘要 (解析 JSON 中的第一段文字) -->
              <p class="work-summary">{{ getContentSummary(work.content) }}</p>
              
              <!-- 底部作者与时间 -->
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
          <el-empty description="该板块暂无内容，快来成为第一个创作者吧！" :image-size="150" />
        </div>
      </el-main>
    </el-container>

    <!-- ================= 更多学科弹窗 ================= -->
    <el-dialog v-model="moreDialogVisible" title="全部学科" width="650px" append-to-body>
      <div class="subject-grid">
        <div 
          v-for="subject in allSubjects" 
          :key="subject.id"
          :class="['subject-item-card', { active: activeSubjectId === subject.id.toString() }]"
          @click="selectMoreSubject(subject.id)"
        >
          {{ subject.name }}
        </div>
        <el-empty v-if="allSubjects.length === 0" description="暂无学科数据" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, ChatDotRound, Star, UserFilled, Reading, HomeFilled, EditPen,
  Compass, Collection, MoreFilled 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import defaultAvatar from '@/assets/default-avatar.png'

const router = useRouter()
const searchKeyword = ref('')
const username = ref('用户')
const userRole = ref('')
const isLoggedIn = ref(false)
const userAvatar = ref('')

// ================= 学科数据与状态 =================
const allSubjects = ref([]) 
const activeSubjectId = ref('recommend') 
const moreDialogVisible = ref(false)

// ================= 作品列表数据 =================
const workList = ref([])
const loading = ref(false)

const displaySubjects = computed(() => {
  return allSubjects.value.slice(0, 6)
})

const getCurrentSubjectName = () => {
  const subject = allSubjects.value.find(s => s.id.toString() === activeSubjectId.value)
  return subject ? subject.name : '未知'
}

const loadSubjects = async () => {
  try {
    const res = await request.get('/subjectArea')
    if (res.code === 1) {
      allSubjects.value = res.data
    }
  } catch (error) {
    console.error('获取学科失败', error)
  }
}

const loadWorks = async () => {
  loading.value = true
  try {
    let res;
    if (activeSubjectId.value === 'recommend') {
      res = await request.get('/work/recommend')
    } else {
      res = await request.get(`/work/subject/${activeSubjectId.value}`)
    }

    if (res.code === 1) {
      workList.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取作品列表失败')
  } finally {
    loading.value = false
  }
}

const handleMenuSelect = (index) => {
  if (index === 'more') return
  activeSubjectId.value = index
  loadWorks()
}

const selectMoreSubject = (id) => {
  activeSubjectId.value = id.toString()
  moreDialogVisible.value = false
  loadWorks()
}

// ================= 工具函数 =================
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

const goToDetail = (id) => {
  router.push(`/work/${id}`)
}

// ================= 用户信息与导航逻辑 =================
const loadUserData = async () => {
  const token = localStorage.getItem('token')
  const savedRole = localStorage.getItem('role')

  if (token) {
    isLoggedIn.value = true
    if (savedRole) userRole.value = savedRole
    
    try {
      const res = await request.get('/user/info')
      if (res.code === 1) {
        username.value = res.data.nickname || res.data.username
        userAvatar.value = res.data.avatar || ''
        localStorage.setItem('username', username.value)
      }
    } catch (error) {
      console.error("获取用户信息失败", error)
    }
  } else {
    isLoggedIn.value = false
  }
}

onMounted(() => {
  loadUserData()
  loadSubjects()
  loadWorks()
})

onActivated(() => {
  loadUserData()
  loadSubjects()
  loadWorks()
})

const goToLogin = () => router.push('/login')
const goToRegister = () => router.push('/register')
const goHome = () => {
  if (router.currentRoute.value.path === '/home') router.go(0)
  else router.push('/home')
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return ElMessage.warning('请输入搜索内容')
  ElMessage.info(`正在搜索：${searchKeyword.value}（功能待开发）`)
}

const handleAction = (actionName) => ElMessage.info(`${actionName}功能待开发...`)

// 👇 修改了这里的退出逻辑
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await request.post('/user/logout') // 通知后端记录日志
    } catch (e) {
      console.error(e)
    } finally {
      ElMessage.success('已退出登录')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      router.push('/login')
    }
  } else if (command === 'admin') {
    if (localStorage.getItem('role') === '1') router.push('/admin')
    else ElMessage.warning('您没有管理员权限')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
/* 原有样式不变 */
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  height: 60px;
  padding: 0;
  flex-shrink: 0;
  position: relative;
  z-index: 100;
}
.global-logo {
  position: absolute;
  left: 20px;
  top: 0;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  z-index: 101;
}
.logo-text {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(45deg, #409eff, #79bbff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 260px;
  padding-right: 20px;
}
.left-section { display: flex; align-items: center; }
.home-btn { margin-right: 20px; }
.search-box { flex: 1; max-width: 500px; margin: 0 20px; }
.search-input :deep(.el-input-group__append) { background-color: #409eff; color: white; border-color: #409eff; }
.search-input :deep(.el-input-group__append:hover) { background-color: #66b1ff; }
.right-menu { display: flex; align-items: center; gap: 24px; }
.menu-item { display: flex; align-items: center; gap: 4px; cursor: pointer; color: #606266; font-size: 14px; transition: color 0.3s; }
.menu-item:hover { color: #409eff; }
.avatar-box { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.nickname { font-size: 14px; color: #303133; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.main-layout {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  overflow: hidden;
}

.left-sidebar {
  background-color: #fff;
  margin: 20px 20px 20px 0;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.custom-menu { border-right: none; }
.custom-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin-bottom: 4px;
  border-radius: 6px;
  margin: 4px 10px;
  color: #606266;
}
.custom-menu :deep(.el-menu-item.is-active) {
  background-color: #e6f1fc;
  color: #409eff;
  font-weight: bold;
}
.custom-menu :deep(.el-menu-item:hover) { background-color: #f5f7fa; }

.right-content {
  margin: 20px 0;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
  padding: 24px;
}

.content-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 16px;
  margin-bottom: 20px;
}
.content-header h3 { margin: 0; font-size: 20px; color: #303133; font-weight: bold; }
.empty-state { margin-top: 80px; text-align: center; }

/* ================= 作品列表样式 ================= */
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
.work-card:hover {
  transform: translateY(-2px);
}

/* 让卡片横向排列封面和文字 */
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

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

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
  -webkit-line-clamp: 2; /* 最多两行 */
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 13px;
  color: #909399;
}

.create-time {
  font-size: 13px;
  color: #909399;
}

/* ================= 更多学科弹窗样式 ================= */
.subject-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 10px;
}
.subject-item-card {
  text-align: center;
  padding: 12px 0;
  background-color: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  color: #606266;
  font-size: 15px;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.subject-item-card:hover {
  background-color: #e6f1fc;
  color: #409eff;
}
.subject-item-card.active {
  background-color: #409eff;
  color: white;
  font-weight: bold;
  border-color: #409eff;
}
</style>
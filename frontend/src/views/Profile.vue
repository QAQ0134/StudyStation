<template>
  <div class="profile-container">
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

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input v-model="searchKeyword" placeholder="搜索你感兴趣的内容..." class="search-input" clearable>
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <!-- 右侧导航 -->
        <div class="right-menu">
          <template v-if="isLoggedIn">
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
                <el-avatar :size="36" :src="navAvatar ? getImageUrl(navAvatar) : defaultAvatar" />
                <span class="nickname">{{ navUsername }}</span>
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
    <div class="main-layout">
      <!-- 左侧导航栏 -->
      <el-aside width="200px" class="left-sidebar">
        <el-menu :default-active="activeTab" class="custom-menu" @select="handleTabSelect">
          <el-menu-item index="info">
            <el-icon><User /></el-icon>
            <span>{{ isOtherUser ? '他的资料' : '个人信息' }}</span>
          </el-menu-item>
          <el-menu-item index="works">
            <el-icon><Document /></el-icon>
            <span>{{ isOtherUser ? '他的作品' : '我的作品' }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-main class="right-content">
        
        <!-- ================= Tab 1：个人信息 ================= -->
        <el-card v-if="activeTab === 'info'" class="profile-card">
          <template #header>
            <div class="card-header">
              <span>{{ isOtherUser ? '用户资料' : '个人资料' }}</span>
            </div>
          </template>

          <div class="profile-content">
            <!-- 左侧：头像与用户名 -->
            <div class="avatar-section">
              <!-- 看别人：只读头像 -->
              <el-avatar 
                v-if="isOtherUser" 
                :size="120" 
                :src="userInfo.avatar ? getImageUrl(userInfo.avatar) : defaultAvatar" 
              />
              <!-- 看自己：可上传头像 -->
              <el-upload
                v-else
                class="avatar-uploader"
                action="/api/upload/avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                name="file"
                :headers="{ token: token }"
              >
                <img v-if="userInfo.avatar" :src="getImageUrl(userInfo.avatar)" class="avatar-img" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <p class="username-text">@{{ userInfo.username }}</p>
              
              <!-- 预留：如果是看别人，显示关注按钮 -->
              <el-button v-if="isOtherUser" type="primary" round style="margin-top: 10px;">关注 TA</el-button>
            </div>

            <!-- 右侧：表单信息 -->
            <el-form :model="userInfo" label-width="100px" class="info-form">
              <el-form-item label="昵称">
                <el-input v-model="userInfo.nickname" :disabled="isOtherUser" placeholder="未填写" />
              </el-form-item>

              <el-form-item label="性别">
                <el-radio-group v-model="userInfo.gender" :disabled="isOtherUser">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="0">女</el-radio>
                  <el-radio :label="2">保密</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="地区">
                <el-input
                  v-model="userInfo.region"
                  :disabled="isOtherUser"
                  placeholder="未填写"
                  :style="{ cursor: isOtherUser ? 'not-allowed' : 'pointer' }"
                  @click="!isOtherUser && (regionDialogVisible = true)"
                >
                  <template #suffix v-if="!isOtherUser">
                    <el-icon><Location /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="个性签名">
                <el-input v-model="userInfo.signature" type="textarea" :rows="3" :disabled="isOtherUser" placeholder="未填写" />
              </el-form-item>

              <el-form-item label="兴趣爱好">
                <el-input
                  v-model="hobbyDisplayText"
                  :disabled="isOtherUser"
                  placeholder="未填写"
                  :style="{ cursor: isOtherUser ? 'not-allowed' : 'pointer' }"
                  @click="!isOtherUser && (hobbyDialogVisible = true)"
                >
                  <template #suffix v-if="!isOtherUser">
                    <el-icon><ArrowDown /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <!-- 只有看自己，才显示保存按钮 -->
              <el-form-item v-if="!isOtherUser">
                <el-button type="primary" @click="handleSave" :loading="loading">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <!-- ================= Tab 2：作品 ================= -->
        <el-card v-if="activeTab === 'works'" class="works-card" v-loading="worksLoading">
          <template #header>
            <div class="card-header">
              <span>{{ isOtherUser ? '他的作品' : '我的作品' }} (共 {{ myWorks.length }} 篇)</span>
            </div>
          </template>

          <div class="work-list" v-if="myWorks.length > 0">
            <el-card 
              v-for="work in myWorks" 
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
                  <span class="create-time">发布于 {{ formatTime(work.createTime) }}</span>
                </div>
              </div>
            </el-card>
          </div>

          <el-empty v-else :description="isOtherUser ? '该用户暂无作品' : '你还没有发布过作品，快去创作吧~'" :image-size="120" />
        </el-card>

      </el-main>
    </div>

    <!-- ================= 弹窗区域 ================= -->
    <!-- 1. 兴趣爱好选择弹窗 (只有自己能用) -->
    <el-dialog v-model="hobbyDialogVisible" title="选择感兴趣的知识点" width="600px" append-to-body>
      <div class="dialog-content">
        <div class="dialog-left">
          <ul class="menu-list">
            <li 
              v-for="subject in hobbyOptions" 
              :key="subject.id"
              :class="['menu-item', { active: currentSubjectId === subject.id }]"
              @click="currentSubjectId = subject.id"
            >
              {{ subject.name }}
            </li>
          </ul>
        </div>
        <div class="dialog-right">
          <el-checkbox-group v-model="selectedHobbies">
            <el-checkbox 
              v-for="point in currentPoints" 
              :key="point.id" 
              :label="point.id"
              class="point-checkbox"
            >
              {{ point.name }}
            </el-checkbox>
          </el-checkbox-group>
          <el-empty v-if="currentPoints.length === 0" description="该学科下暂无知识点" :image-size="60" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="hobbyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="hobbyDialogVisible = false">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 2. 地区选择弹窗 (只有自己能用) -->
    <el-dialog v-model="regionDialogVisible" title="选择所在地区" width="550px" append-to-body>
      <div class="region-grid">
        <div 
          v-for="province in provinces" 
          :key="province"
          :class="['region-item', { active: userInfo.region === province }]"
          @click="selectRegion(province)"
        >
          {{ province }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { ArrowDown, Location, Plus, Search, ChatDotRound, Star, UserFilled, Reading, HomeFilled, EditPen, User, Document } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/default-avatar.png'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const token = localStorage.getItem('token') || '' 

// ================= 身份判断逻辑 =================
const queryId = computed(() => {
  return route.query.id ? Number(route.query.id) : null
})

const currentUserId = ref(null)

if (token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    currentUserId.value = payload.id
  } catch (e) {}
}

const isOtherUser = computed(() => {
  return queryId.value !== null && queryId.value !== currentUserId.value
})

const targetUserId = computed(() => {
  return queryId.value || currentUserId.value
})

// ================= 导航栏与 Tab 状态 =================
const searchKeyword = ref('')
const isLoggedIn = ref(true) 
const navUsername = ref('')
const navAvatar = ref('')
const userRole = ref(localStorage.getItem('role') || '')
const activeTab = ref('info')

const handleTabSelect = (index) => {
  activeTab.value = index
  if (index === 'works') {
    loadWorks()
  }
}

// ================= 用户数据 =================
const userInfo = ref({
  username: '',
  nickname: '',
  avatar: '',
  signature: '',
  gender: 2,
  region: '',
  hobbyIds: []
})

// ================= 作品数据 =================
const myWorks = ref([])
const worksLoading = ref(false)

const loadWorks = async () => {
  if (!targetUserId.value) return
  worksLoading.value = true
  try {
    const res = await request.get('/work/userWorks', {
      params: { userId: targetUserId.value }
    })
    if (res.code === 1) {
      myWorks.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取作品列表失败')
  } finally {
    worksLoading.value = false
  }
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

const goToDetail = (id) => {
  router.push(`/work/${id}`)
}

// ================= 导航栏交互逻辑 =================
const goHome = () => router.push('/home')
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
      localStorage.clear()
      router.push('/login')
    }
  } else if (command === 'admin') {
    if (localStorage.getItem('role') === '1') router.push('/admin')
    else ElMessage.warning('您没有管理员权限')
  } else if (command === 'profile') {
    if (route.path === '/profile') {
      router.push('/profile') 
    } else {
      router.push('/profile')
    }
  }
}

// ================= 表单与上传逻辑 =================
const beforeAvatarUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('头像只能是 JPG/PNG 格式!')
  if (!isLt2M) ElMessage.error('头像大小不能超过 2MB!')
  return isImage && isLt2M
}

const handleAvatarSuccess = (response) => {
  if (response.code === 1) {
    userInfo.value.avatar = response.data
    ElMessage.success('头像上传成功，请记得点击下方的保存修改')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// ================= 爱好与地区逻辑 =================
const hobbyDialogVisible = ref(false)
const selectedHobbies = ref([]) 
const hobbyOptions = ref([]) 
const allKnowledgePoints = ref([]) 
const currentSubjectId = ref(null) 

const currentPoints = computed(() => {
  if (!currentSubjectId.value) return []
  return allKnowledgePoints.value.filter(p => p.subjectAreaId === currentSubjectId.value)
})

const hobbyDisplayText = computed(() => {
  if (selectedHobbies.value.length === 0) return ''
  const names = selectedHobbies.value.map(id => {
    const point = allKnowledgePoints.value.find(p => p.id === id)
    return point ? point.name : ''
  })
  return names.filter(name => name !== '').join('，')
})

const regionDialogVisible = ref(false)
const provinces = [
  '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
  '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南', '湖北', '湖南',
  '广东', '广西', '海南', '重庆', '四川', '贵州', '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆',
  '香港', '澳门', '台湾'
]
const selectRegion = (province) => {
  userInfo.value.region = province
  regionDialogVisible.value = false
}

// ================= 数据加载与保存 =================
const loadNavUserInfo = async () => {
  if (!token) return 
  try {
    const res = await request.get('/user/info')
    if (res.code === 1) {
      navUsername.value = res.data.nickname || res.data.username
      navAvatar.value = res.data.avatar || ''
    }
  } catch (error) {
    console.error('获取导航栏用户信息失败', error)
  }
}

const loadUserInfo = async () => {
  if (!targetUserId.value) return
  try {
    const url = isOtherUser.value ? `/user/public/${targetUserId.value}` : '/user/info'
    const res = await request.get(url)
    
    if (res.code === 1) {
      userInfo.value = res.data
      if (!isOtherUser.value && res.data.hobbyIds) {
        selectedHobbies.value = res.data.hobbyIds
      }
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const loadHobbyOptions = async () => {
  if (isOtherUser.value) return 
  
  try {
    const [subjectRes, kpRes] = await Promise.all([
      request.get('/subjectArea'),
      request.get('/knowledgePoint')
    ])

    if (subjectRes.code === 1 && kpRes.code === 1) {
      hobbyOptions.value = subjectRes.data
      allKnowledgePoints.value = kpRes.data
      if (hobbyOptions.value.length > 0) {
        currentSubjectId.value = hobbyOptions.value[0].id
      }
    }
  } catch (error) {
    ElMessage.error('获取爱好选项失败')
  }
}

const handleSave = async () => {
  if (!userInfo.value.nickname) return ElMessage.warning('昵称不能为空')
  
  loading.value = true
  try {
    userInfo.value.hobbyIds = selectedHobbies.value
    const res = await request.put('/user/info', userInfo.value)
    if (res.code === 1) {
      ElMessage.success('保存成功')
      navUsername.value = userInfo.value.nickname
      navAvatar.value = userInfo.value.avatar
      localStorage.setItem('username', userInfo.value.nickname)
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.id,
  () => {
    loadUserInfo()
    if (!isOtherUser.value) {
      loadHobbyOptions()
      selectedHobbies.value = []
    }
    activeTab.value = 'info'
  }
)

onMounted(() => {
  loadNavUserInfo() 
  loadUserInfo()    
  loadHobbyOptions()
})
</script>

<style scoped>
/* 这里保留你之前的样式，无需更改 */
.profile-container { min-height: 100vh; background-color: #f5f7fa; display: flex; flex-direction: column; }
.header { background-color: #fff; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); height: 60px; padding: 0; position: sticky; top: 0; z-index: 100; }
.global-logo { position: absolute; left: 20px; top: 0; height: 60px; display: flex; align-items: center; gap: 8px; cursor: pointer; z-index: 101; }
.logo-text { font-size: 22px; font-weight: 800; letter-spacing: 1px; background: linear-gradient(45deg, #409eff, #79bbff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-content { max-width: 1200px; margin: 0 auto; height: 100%; display: flex; align-items: center; justify-content: space-between; padding-left: 260px; padding-right: 20px; }
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

.main-layout { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; overflow: hidden; display: flex; gap: 20px; padding: 20px; box-sizing: border-box; }
.left-sidebar { background-color: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); height: fit-content; }
.custom-menu { border-right: none; }
.custom-menu :deep(.el-menu-item) { height: 50px; line-height: 50px; border-radius: 6px; margin: 4px 10px; color: #606266; }
.custom-menu :deep(.el-menu-item.is-active) { background-color: #e6f1fc; color: #409eff; font-weight: bold; }
.custom-menu :deep(.el-menu-item:hover) { background-color: #f5f7fa; }

.right-content { flex: 1; padding: 0; background-color: transparent; box-shadow: none; }
.profile-card, .works-card { border-radius: 12px; width: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }

.profile-content { display: flex; gap: 40px; padding: 20px; }
.avatar-section { width: 200px; display: flex; flex-direction: column; align-items: center; padding-top: 20px; }
.username-text { margin-top: 15px; color: #909399; font-size: 16px; }
.info-form { flex: 1; }

.avatar-uploader { width: 120px; height: 120px; border: 1px dashed #d9d9d9; border-radius: 50%; cursor: pointer; position: relative; overflow: hidden; transition: border-color 0.3s; flex-shrink: 0; display: flex; justify-content: center; align-items: center; background-color: #fafafa; }
.avatar-uploader:hover { border-color: #409eff; }
.avatar-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }

.work-list { display: flex; flex-direction: column; gap: 16px; }
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

.dialog-content { display: flex; height: 320px; border: 1px solid #ebeef5; border-radius: 4px; overflow: hidden; }
.dialog-left { width: 180px; background-color: #f9f9f9; border-right: 1px solid #ebeef5; overflow-y: auto; }
.menu-list { list-style: none; padding: 0; margin: 0; }
.menu-list .menu-item { padding: 12px 20px; cursor: pointer; color: #606266; transition: all 0.3s; font-size: 14px; }
.menu-list .menu-item:hover { background-color: #f0f2f5; }
.menu-list .menu-item.active { background-color: #e6f1fc; color: #409eff; font-weight: bold; border-right: 3px solid #409eff; }
.dialog-right { flex: 1; padding: 20px; overflow-y: auto; }
.point-checkbox { display: flex; margin-bottom: 15px; margin-right: 0; }

.region-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; padding: 10px; }
.region-item { text-align: center; padding: 8px 0; background-color: #f5f7fa; border-radius: 4px; cursor: pointer; color: #606266; font-size: 14px; transition: all 0.2s; }
.region-item:hover { background-color: #e6f1fc; color: #409eff; }
.region-item.active { background-color: #409eff; color: white; font-weight: bold; }
</style>
<template>
  <div class="create-container">
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
        <div class="right-menu">
          <el-button type="primary" @click="handlePublish" :loading="publishing">发布作品</el-button>
        </div>
      </div>
    </el-header>

    <!-- 编辑主体区域 -->
    <div class="main-wrapper">
      <el-card class="editor-card">
        <el-form :model="workForm" label-position="top">
          <!-- 标题 -->
          <el-form-item label="作品标题" required>
            <el-input v-model="workForm.title" placeholder="请输入一个吸引人的标题..." size="large" maxlength="50" show-word-limit />
          </el-form-item>

          <!-- 1. 选择所属学科 (多选) -->
          <el-form-item label="所属学科领域 (可多选，建议选择1-2个)">
            <el-select 
              v-model="workForm.subjectIds" 
              multiple 
              placeholder="请选择所属学科" 
              style="width: 100%"
              @change="handleSubjectChange"
            >
              <el-option
                v-for="item in subjectOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <!-- 2. 关联知识点 (多选标签) -->
          <el-form-item label="关联知识点标签 (可多选，选填)">
            <el-select
              v-model="workForm.knowledgePointIds"
              multiple
              filterable
              placeholder="请先选择上方的学科，再挑选具体知识点"
              style="width: 100%"
              :disabled="workForm.subjectIds.length === 0"
            >
              <!-- 按学科分组展示知识点 -->
              <el-option-group
                v-for="group in filteredKpOptions"
                :key="group.subjectId"
                :label="group.subjectName"
              >
                <el-option
                  v-for="kp in group.points"
                  :key="kp.id"
                  :label="kp.name"
                  :value="kp.id"
                />
              </el-option-group>
            </el-select>
          </el-form-item>

          <!-- 封面图上传 -->
          <el-form-item label="封面图 (选填，不上传则默认无封面)">
            <el-upload
              class="cover-uploader"
              action="/api/upload/avatar"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeImageUpload"
              name="file"
              :headers="{ token: token }"
            >
              <img v-if="workForm.coverImage" :src="getImageUrl(workForm.coverImage)" class="cover-img" />
              <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-divider content-position="left">正文内容编辑 (图文混排)</el-divider>

          <!-- 动态块编辑器 -->
          <div class="blocks-editor">
            <div 
              v-for="(block, index) in blocks" 
              :key="index" 
              class="edit-block"
            >
              <!-- 文本块 -->
              <div v-if="block.type === 'text'" class="block-item text-block">
                <el-input
                  v-model="block.value"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 10 }"
                  placeholder="请输入段落文字..."
                />
                <el-button class="delete-btn" type="danger" icon="Delete" circle size="small" @click="removeBlock(index)" />
              </div>

              <!-- 图片块 -->
              <div v-if="block.type === 'image'" class="block-item image-block">
                <img :src="getImageUrl(block.value)" class="content-image" />
                <el-button class="delete-btn" type="danger" icon="Delete" circle size="small" @click="removeBlock(index)" />
              </div>
            </div>

            <!-- 添加块按钮 -->
            <div class="add-block-actions">
              <el-button type="primary" plain @click="addTextBlock">+ 添加段落</el-button>
              
              <el-upload
                action="/api/upload/avatar"
                :show-file-list="false"
                :on-success="handleContentImageSuccess"
                :before-upload="beforeImageUpload"
                name="file"
                :headers="{ token: token }"
                style="display: inline-block; margin-left: 10px;"
              >
                <el-button type="success" plain>+ 插入图片</el-button>
              </el-upload>
            </div>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, HomeFilled, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const token = localStorage.getItem('token') || ''
const publishing = ref(false)

// ================= 表单数据 =================
const workForm = ref({
  title: '',
  coverImage: '',
  subjectIds: [],        // 多选学科
  knowledgePointIds: []  // 多选知识点
})

// 图文块数据
const blocks = ref([
  { type: 'text', value: '' } // 默认给一个空文本块
])

// ================= 学科与知识点数据源 =================
const subjectOptions = ref([]) 
const allKnowledgePoints = ref([]) 

// 根据选中的学科，过滤出对应的知识点用于分组展示
const filteredKpOptions = computed(() => {
  if (workForm.value.subjectIds.length === 0) return []
  
  return workForm.value.subjectIds.map(subId => {
    const subject = subjectOptions.value.find(s => s.id === subId)
    const points = allKnowledgePoints.value.filter(kp => kp.subjectAreaId === subId)
    
    return {
      subjectId: subId,
      subjectName: subject ? subject.name : '未知学科',
      points: points
    }
  }).filter(group => group.points.length > 0) 
})

// ================= 交互逻辑 =================

// 学科改变时，清空已选的知识点，防止跨学科脏数据
const handleSubjectChange = () => {
  workForm.value.knowledgePointIds = []
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const goHome = () => router.push('/home')

const addTextBlock = () => {
  blocks.value.push({ type: 'text', value: '' })
}

const removeBlock = (index) => {
  blocks.value.splice(index, 1)
}

// 上传前校验
const beforeImageUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能是 JPG/PNG 格式!')
  if (!isLt5M) ElMessage.error('图片大小不能超过 5MB!')
  return isImage && isLt5M
}

// 封面上传成功
const handleCoverSuccess = (response) => {
  if (response.code === 1) {
    workForm.value.coverImage = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 正文图片上传成功
const handleContentImageSuccess = (response) => {
  if (response.code === 1) {
    blocks.value.push({ type: 'image', value: response.data })
    ElMessage.success('图片插入成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 加载所有学科和知识点
const loadOptions = async () => {
  try {
    const [subjectRes, kpRes] = await Promise.all([
      request.get('/subjectArea'),
      request.get('/knowledgePoint')
    ])
    if (subjectRes.code === 1 && kpRes.code === 1) {
      subjectOptions.value = subjectRes.data
      allKnowledgePoints.value = kpRes.data
    }
  } catch (error) {
    console.error('获取分类数据失败', error)
  }
}

// ================= 发布逻辑 =================
const handlePublish = async () => {
  if (!workForm.value.title.trim()) {
    return ElMessage.warning('请输入作品标题')
  }
  if (workForm.value.subjectIds.length === 0) {
    return ElMessage.warning('请至少选择一个所属学科')
  }

  // 校验并过滤空的内容块
  const validBlocks = blocks.value.filter(block => {
    if (block.type === 'text' && !block.value.trim()) return false
    if (block.type === 'image' && !block.value) return false
    return true
  })

  if (validBlocks.length === 0) {
    return ElMessage.warning('正文内容不能为空')
  }

  publishing.value = true
  try {
    const contentJson = JSON.stringify(validBlocks)

    const payload = {
      title: workForm.value.title,
      content: contentJson,
      coverImage: workForm.value.coverImage,
      subjectIds: workForm.value.subjectIds, 
      knowledgePointIds: workForm.value.knowledgePointIds 
    }

    const res = await request.post('/work/publish', payload)
    if (res.code === 1) {
      ElMessage.success('发布成功！')
      router.push('/home') // 发布后跳回首页
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch (error) {
    ElMessage.error('网络异常，发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  loadOptions()
})
</script>

<style scoped>
.create-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* 导航栏样式复用 */
.header { background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); height: 60px; padding: 0; flex-shrink: 0; position: sticky; top: 0; z-index: 100; }
.global-logo { position: absolute; left: 20px; top: 0; height: 60px; display: flex; align-items: center; gap: 8px; cursor: pointer; z-index: 101; }
.logo-text { font-size: 22px; font-weight: 800; background: linear-gradient(45deg, #409eff, #79bbff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-content { max-width: 900px; margin: 0 auto; height: 100%; display: flex; align-items: center; justify-content: space-between; padding-left: 260px; padding-right: 20px; }
.menu-item { display: flex; align-items: center; gap: 4px; cursor: pointer; color: #606266; font-size: 14px; }
.menu-item:hover { color: #409eff; }

/* 主体区域 */
.main-wrapper {
  flex: 1;
  max-width: 900px;
  width: 100%;
  margin: 20px auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.editor-card {
  border-radius: 8px;
}

/* 封面图上传 */
.cover-uploader {
  width: 200px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fafafa;
}
.cover-uploader:hover { border-color: #409eff; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.cover-uploader-icon { font-size: 28px; color: #8c939d; }

/* 动态块编辑器 */
.blocks-editor {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.edit-block {
  position: relative;
}

.block-item {
  position: relative;
  border-radius: 6px;
}

.text-block :deep(.el-textarea__inner) {
  font-size: 15px;
  line-height: 1.6;
  padding: 12px;
}

.image-block {
  text-align: center;
  background-color: #fafafa;
  padding: 10px;
  border: 1px solid #ebeef5;
}

.content-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
}

/* 悬浮显示的删除按钮 */
.delete-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 10;
}
.block-item:hover .delete-btn {
  opacity: 1;
}

.add-block-actions {
  margin-top: 20px;
  padding: 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  text-align: center;
  background-color: #fafafa;
}
</style>
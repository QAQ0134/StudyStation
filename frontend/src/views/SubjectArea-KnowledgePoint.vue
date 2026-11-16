<template>
  <div class="study-manage-container">
    <!-- 左侧：学科分类管理 -->
    <el-card class="left-menu">
      <template #header>
        <div class="card-header">
          <span>学科分类</span>
          <el-button type="primary" size="small" @click="openSubjectDialog('add')">新增</el-button>
        </div>
      </template>

      <!-- 自定义学科列表 -->
      <ul class="subject-list">
        <li 
          v-for="subject in subjectOptions" 
          :key="subject.id"
          :class="['subject-item', { active: activeSubjectId === subject.id }]"
          @click="handleSubjectSelect(subject.id)"
        >
          <span class="subject-name">{{ subject.name }}</span>
          <!-- 悬浮时显示修改和删除按钮 -->
          <div class="subject-actions" @click.stop>
            <el-button link type="primary" size="small" @click="openSubjectDialog('edit', subject)">修改</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteSubject(subject.id)">删除</el-button>
          </div>
        </li>
        <li v-if="subjectOptions.length === 0" class="empty-tip">暂无学科，请添加</li>
      </ul>
    </el-card>

    <!-- 右侧：知识点列表 -->
    <el-card class="right-content">
      <template #header>
        <div class="card-header">
          <span v-if="activeSubjectId">【{{ activeSubjectName }}】 知识点列表</span>
          <span v-else>请先在左侧选择学科</span>
          
          <el-button 
            type="primary" 
            :disabled="!activeSubjectId" 
            @click="openKnowledgeDialog('add')"
          >
            新增知识点
          </el-button>
        </div>
      </template>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border 
        style="width: 100%"
        empty-text="暂无知识点，快去添加吧"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="知识点名称" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openKnowledgeDialog('edit', scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteKnowledge(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ================= 弹窗区域 ================= -->
    
    <!-- 1. 学科新增/编辑弹窗 -->
    <el-dialog
      v-model="subjectDialogVisible"
      :title="subjectDialogType === 'add' ? '新增学科' : '编辑学科'"
      width="400px"
    >
      <el-form :model="subjectFormData" label-width="80px">
        <el-form-item label="学科名称">
          <el-input v-model="subjectFormData.name" placeholder="请输入学科名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="subjectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSubjectForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 2. 知识点新增/编辑弹窗 -->
    <el-dialog
      v-model="knowledgeDialogVisible"
      :title="knowledgeDialogTitle"
      width="450px"
    >
      <el-form :model="knowledgeFormData" label-width="100px">
        <!-- 已删除“所属学科”下拉框 -->
        <el-form-item label="知识点名称">
          <el-input v-model="knowledgeFormData.name" placeholder="请输入知识点名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="knowledgeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitKnowledgeForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// ================= 状态定义 =================

const subjectOptions = ref([])
const activeSubjectId = ref(null)
const activeSubjectName = computed(() => {
  const subject = subjectOptions.value.find(s => s.id === activeSubjectId.value)
  return subject ? subject.name : ''
})

const subjectDialogVisible = ref(false)
const subjectDialogType = ref('add')
const subjectFormData = ref({ id: null, name: '' })

const tableData = ref([])
const loading = ref(false)

const knowledgeDialogVisible = ref(false)
const knowledgeDialogType = ref('add')
const knowledgeFormData = ref({ id: null, name: '', subjectAreaId: null })

const knowledgeDialogTitle = computed(() => {
  if (knowledgeDialogType.value === 'add') {
    return `为【${activeSubjectName.value || '当前学科'}】添加知识点`
  }
  return '编辑知识点'
})


// ================= 学科相关逻辑 =================

const loadSubjects = async () => {
  try {
    const res = await request.get('/subjectArea')
    if (res.code === 1) {
      subjectOptions.value = res.data
      if (!activeSubjectId.value && subjectOptions.value.length > 0) {
        activeSubjectId.value = subjectOptions.value[0].id
        loadKnowledgePoints()
      } else if (subjectOptions.value.length === 0) {
        activeSubjectId.value = null
        tableData.value = []
      }
    }
  } catch (error) {
    ElMessage.error('获取学科分类失败')
  }
}

const handleSubjectSelect = (id) => {
  activeSubjectId.value = id
  loadKnowledgePoints()
}

const openSubjectDialog = (type, row = null) => {
  subjectDialogType.value = type
  if (type === 'add') {
    subjectFormData.value = { id: null, name: '' }
  } else {
    subjectFormData.value = { id: row.id, name: row.name }
  }
  subjectDialogVisible.value = true
}

const submitSubjectForm = async () => {
  if (!subjectFormData.value.name || subjectFormData.value.name.trim() === '') {
    ElMessage.warning('请输入学科名称')
    return
  }
  try {
    let res
    if (subjectDialogType.value === 'add') {
      res = await request.post('/subjectArea', subjectFormData.value)
    } else {
      res = await request.put('/subjectArea', subjectFormData.value)
    }

    if (res.code === 1) {
      ElMessage.success(subjectDialogType.value === 'add' ? '添加成功' : '修改成功')
      subjectDialogVisible.value = false
      loadSubjects()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeleteSubject = (id) => {
  ElMessageBox.confirm('确认删除该学科吗？该学科下的知识点也会被一并删除！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await request.delete(`/subjectArea/${id}`)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        if (activeSubjectId.value === id) {
          activeSubjectId.value = null
          tableData.value = []
        }
        loadSubjects()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}


// ================= 知识点相关逻辑 =================

const loadKnowledgePoints = async () => {
  if (!activeSubjectId.value) return
  loading.value = true
  try {
    const res = await request.get(`/knowledgePoint/subject/${activeSubjectId.value}`)
    if (res.code === 1) {
      tableData.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取知识点失败')
  } finally {
    loading.value = false
  }
}

const openKnowledgeDialog = (type, row = null) => {
  knowledgeDialogType.value = type
  if (type === 'add') {
    knowledgeFormData.value = { id: null, name: '', subjectAreaId: activeSubjectId.value }
  } else {
    knowledgeFormData.value = { id: row.id, name: row.name, subjectAreaId: row.subjectAreaId }
  }
  knowledgeDialogVisible.value = true
}

const submitKnowledgeForm = async () => {
  if (!knowledgeFormData.value.name || knowledgeFormData.value.name.trim() === '') {
    ElMessage.warning('请输入知识点名称')
    return
  }

  try {
    let res
    if (knowledgeDialogType.value === 'add') {
      res = await request.post('/knowledgePoint', knowledgeFormData.value)
    } else {
      res = await request.put('/knowledgePoint', knowledgeFormData.value)
    }

    if (res.code === 1) {
      ElMessage.success(knowledgeDialogType.value === 'add' ? '添加成功' : '修改成功')
      knowledgeDialogVisible.value = false
      loadKnowledgePoints()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeleteKnowledge = (id) => {
  ElMessageBox.confirm('确认删除该知识点吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await request.delete(`/knowledgePoint/${id}`)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        loadKnowledgePoints()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped>
.study-manage-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  height: 100%; 
  box-sizing: border-box;
}

/* 左侧面板 */
.left-menu {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 自定义学科列表样式 */
.subject-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.subject-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  border-radius: 6px;
  cursor: pointer;
  background-color: #f5f7fa;
  transition: all 0.3s;
}
.subject-item:hover {
  background-color: #e6f1fc;
}
.subject-item.active {
  background-color: #409eff;
  color: white;
}
.subject-item.active .subject-actions :deep(.el-button) {
  color: white !important;
}

.subject-name {
  font-size: 15px;
  font-weight: 500;
}

.subject-actions {
  display: none;
}
.subject-item:hover .subject-actions {
  display: block;
}

.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-top: 20px;
}

/* 右侧内容面板 */
.right-content {
  flex: 1;
}
</style>
<template>
  <div class="log-manage-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.type" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="登录" value="登录" />
            <el-option label="注册" value="注册" />
            <el-option label="退出" value="退出" />
          </el-select>
        </el-form-item>

        <el-form-item label="操作描述">
          <el-input v-model="queryForm.description" placeholder="输入关键字搜索" clearable />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="logList" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="操作人" width="120" align="center" />
        <el-table-column prop="module" label="模块" width="100" align="center" />
        
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" align="center" />
        <el-table-column prop="ip" label="IP地址" width="150" align="center" />
        <el-table-column prop="createTime" label="操作时间" width="180" align="center" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50]"
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const logList = ref([])
const loading = ref(false)
const total = ref(0)

const queryForm = ref({
  pageNum: 1,
  pageSize: 10,
  type: '',
  description: ''
})

// 根据类型返回不同的 Tag 颜色
const getTagType = (type) => {
  if (type === '登录') return 'success'
  if (type === '注册') return 'primary'
  if (type === '退出') return 'warning'
  return 'info'
}

// 加载日志
const loadLogs = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/log', {
      params: queryForm.value
    })
    if (res.code === 1) {
      logList.value = res.data.rows
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  queryForm.value.pageNum = 1
  loadLogs()
}

// 重置
const handleReset = () => {
  queryForm.value = {
    pageNum: 1,
    pageSize: 10,
    type: '',
    description: ''
  }
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-manage-container {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
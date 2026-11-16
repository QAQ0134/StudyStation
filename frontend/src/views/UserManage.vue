<template>
  <div class="user-manage-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>

        <el-form-item label="账户状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="封禁" :value="0" />
            <el-option label="已注销" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

            <el-table :data="userList" v-loading="loading" border style="width: 100%">
        <!-- 前面的列保持不变 -->
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" align="center" />
        <el-table-column prop="nickname" label="昵称" align="center" />
        <el-table-column prop="createTime" label="注册时间" align="center" />
        
        <el-table-column label="账号状态" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isDeleted === 1" type="info">已注销</el-tag>
            <el-tag v-else-if="scope.row.status === 0" type="danger">已封禁</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>

        <!-- 新增：操作列 -->
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <!-- 如果该用户已注销，则不允许修改状态 -->
            <span v-if="scope.row.isDeleted === 1" style="color: #999; font-size: 13px;">不可操作</span>
            
            <!-- 正常状态：显示"封禁"按钮 -->
            <el-button 
              v-else-if="scope.row.status === 1" 
              type="danger" 
              size="small" 
              @click="handleChangeStatus(scope.row, 0)"
            >
              封禁账号
            </el-button>
            
            <!-- 封禁状态：显示"解封"按钮 -->
            <el-button 
              v-else-if="scope.row.status === 0" 
              type="success" 
              size="small" 
              @click="handleChangeStatus(scope.row, 1)"
            >
              解封账号
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userList = ref([])
const loading = ref(false)
const total = ref(0)
const dateRange = ref([]) 

// 查询参数对象
const queryForm = ref({
  pageNum: 1,
  pageSize: 10,
  username: '',
  beginTime: '',
  endTime: '',
  status: null
})

// 处理日期范围变化
const handleDateChange = (val) => {
  if (val && val.length === 2) {
    queryForm.value.beginTime = val[0] + ' 00:00:00'
    queryForm.value.endTime = val[1] + ' 23:59:59'
  } else {
    queryForm.value.beginTime = ''
    queryForm.value.endTime = ''
  }
}

// 查询数据
const loadUsers = async () => {
  loading.value = true
  try {
    // 1. 使用 request，去掉 /api
    const res = await request.get('/user', { params: queryForm.value })
    
    // 2. request 拦截器已经把最外层的 axios response 剥掉了
    // 所以这里直接判断 res.code，数据也是 res.data
    if (res.code === 1) {
      userList.value = res.data.rows
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 点击查询按钮
const handleSearch = () => {
  queryForm.value.pageNum = 1 
  loadUsers()
}

// 点击重置按钮
const handleReset = () => {
  queryForm.value = {
    pageNum: 1,
    pageSize: 10,
    username: '',
    beginTime: '',
    endTime: '',
    status: null
  }
  dateRange.value = []
  loadUsers()
}

// 修改用户状态
const handleChangeStatus = (row, newStatus) => {
  const actionName = newStatus === 0 ? '封禁' : '解封'
  ElMessageBox.confirm(`确认要${actionName}用户【${row.username}】吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      // 1. 使用 request，去掉 /api
      const res = await request.put(`/user/status?id=${row.id}&status=${newStatus}`)
      
      // 2. 直接判断 res.code
      if (res.code === 1) {
        ElMessage.success(`${actionName}成功`)
        loadUsers() 
      } else {
        ElMessage.error(res.msg)
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-manage-container {
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
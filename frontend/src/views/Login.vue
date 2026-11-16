<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>StudyStation 登录</h2>
        </div>
      </template>

      <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
        
        <div class="register-link">
          没有账号？<el-link type="primary" @click="goToRegister">去注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 找到 Login.vue 的 handleLogin 方法
const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('/api/user/login', loginForm)
        if (res.data.code === 1) {
          ElMessage.success('登录成功')
          
          // 现在的数据结构是 res.data.data = { token: "...", role: 1 }
          const { token, role } = res.data.data
          
          localStorage.setItem('token', token)
          localStorage.setItem('role', role) // 0 或 1
          localStorage.setItem('username', loginForm.username)
          
          router.push('/home')
        } else {
          ElMessage.error(res.data.msg)
        }
      } catch (error) {
        ElMessage.error('网络异常，请稍后再试')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  router.push('/register') // 跳转到注册页（需要你配置一下路由）
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card { width: 400px; }
.login-header { text-align: center; }
.register-link { text-align: center; font-size: 14px; margin-top: 10px; color: #606266; }
</style>
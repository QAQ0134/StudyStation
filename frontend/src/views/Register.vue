<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <h2>StudyStation 注册</h2>
        </div>
      </template>

      <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">立即注册</el-button>
        </el-form-item>
        
        <div class="login-link">
          已有账号？<el-link type="primary" @click="goToLogin">去登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router' // 1. 引入路由

const router = useRouter() // 2. 获取路由实例
const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

// 自定义校验：确认密码必须与密码一致
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 提交注册
const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('/api/user/register', {
          username: registerForm.username,
          password: registerForm.password
        })
        
        if (res.data.code === 1) {
          ElMessage.success('注册成功，即将跳转登录页')
          // 3. 延迟一小会儿跳转，让用户能看清成功提示
          setTimeout(() => {
            router.push('/login')
          }, 1000)
        } else {
          ElMessage.error(res.data.msg)
        }
      } catch (error) {
        ElMessage.error('注册失败，请稍后再试')
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确')
    }
  })
}

// 跳转登录
const goToLogin = () => {
  router.push('/login') // 4. 直接跳转
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.register-card {
  width: 450px;
}
.register-header {
  text-align: center;
}
.register-header h2 {
  margin: 0;
  color: #303133;
}
.login-link {
  text-align: center;
  font-size: 14px;
  margin-top: 10px;
  color: #606266;
}
</style>
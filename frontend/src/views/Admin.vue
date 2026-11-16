<template>
  <el-container class="layout-container">
    <!-- 左侧导航栏 -->
    <el-aside width="220px" class="aside-menu">
      <div class="logo">StudyStation 后台</div>
      
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        @select="handleMenuSelect"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="subject">
          <el-icon><Reading /></el-icon>
          <span>学科领域管理</span>
        </el-menu-item>
        <el-menu-item index="user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="log">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体内容 -->
    <el-container>
      <el-header class="header" style="display: flex; justify-content: space-between; align-items: center;">
        <span>欢迎使用系统</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </el-header>
      
      <el-main class="main-content">
        <!-- 动态切换组件 -->
        <SubjectAreaAndKnowledgePoint v-if="activeMenu === 'subject'" />
        <UserManage v-if="activeMenu === 'user'" />
        <LogManage v-if="activeMenu === 'log'" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { Reading, User, Document } from '@element-plus/icons-vue' // 需要引入图标
import SubjectAreaAndKnowledgePoint from './SubjectArea-KnowledgePoint.vue'
import UserManage from './UserManage.vue'

import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import LogManage from './LogManage.vue'
const router = useRouter()

// 当前选中的菜单项
const activeMenu = ref('subject')

const handleLogout = () => {
  ElMessage.success('已退出登录')
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/login')
}

// 点击菜单切换组件
const handleMenuSelect = (index) => {
  activeMenu.value = index
}
</script>

<style>
/* 全局样式重置 */
body {
  margin: 0;
  padding: 0;
  background-color: #f0f2f5;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}

.layout-container {
  height: 100vh; /* 撑满全屏 */
}

/* 侧边栏 */
.aside-menu {
  background-color: #304156;
  height: 100%;
  overflow-y: auto;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3643;
  letter-spacing: 1px;
}

.el-menu-vertical {
  border-right: none; /* 去掉菜单默认的右边框 */
}

/* 顶部 Header */
.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

/* 主内容区 */
.main-content {
  padding: 0; /* 让内部组件自己控制 padding */
  overflow-y: auto;
}
</style>
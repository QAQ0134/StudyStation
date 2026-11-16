import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Admin from '../views/Admin.vue'
import Home from '../views/Home.vue'
import { ElMessage } from 'element-plus'
import Profile from '../views/Profile.vue'
import Create from '../views/Create.vue'
import WorkDetail from '../views/WorkDetail.vue'
import MyCollect from '../views/MyCollect.vue'
import UserHome from '../views/UserHome.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home' }, 
    
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/home', component: Home },
    { path: '/admin', component: Admin },
    { path: '/profile', component: Profile },
    { path: '/create', component: Create },
    { path: '/work/:id', component: WorkDetail },
    { path: '/myCollect', component: MyCollect },
    { path: '/user/:userId', component: UserHome }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 1. 白名单：登录、注册、首页，不需要登录就能访问
  if (to.path === '/login' || to.path === '/register' || to.path === '/home' || to.path.startsWith('/work/')) {
    next()
    return
  }

  // 2. 去其他页面必须有 token
  if (!token) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // 3. 解析 JWT 中的过期时间
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000
    
    if (Date.now() >= exp) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      next('/login')
      return
    }
  } catch (e) {
    localStorage.clear()
    next('/login')
    return
  }

  // 4. 专门针对后台页面的管理员权限校验
  if (to.path === '/admin') {
    if (role !== '1') {
      ElMessage.error('您没有管理员权限')
      next('/home') 
      return
    }
  }

  next()
})

export default router
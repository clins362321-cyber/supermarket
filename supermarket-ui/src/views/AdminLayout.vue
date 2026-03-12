<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const title = computed(() => {
  if (route.path.startsWith('/admin/products/')) {
    return '商品详情'
  }
  const map = {
    '/admin/products': '商品管理',
    '/admin/personnel': '人员管理',
    '/admin/delivery': '配送管理',
    '/admin/warehouse': '仓储管理',
    '/admin/procurement': '采购管理',
    '/admin/system': '系统管理',
    '/admin/after-sales': '售后管理',
  }
  return map[route.path] || '管理员首页'
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('adminRole')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<template>
  <div class="admin-layout">
    <nav class="nav-bar">
      <span class="nav-brand">生鲜超市物流配送系统</span>
      <RouterLink class="nav-link" to="/admin/products">商品管理</RouterLink>
      <RouterLink class="nav-link" to="/admin/personnel">人员管理</RouterLink>
      <RouterLink class="nav-link" to="/admin/delivery">配送管理</RouterLink>
      <RouterLink class="nav-link" to="/admin/warehouse">仓储管理</RouterLink>
      <RouterLink class="nav-link" to="/admin/procurement">采购管理</RouterLink>
      <RouterLink class="nav-link" to="/admin/after-sales">售后管理</RouterLink>
      <RouterLink class="nav-link" to="/admin/system">系统管理</RouterLink>
      <button class="nav-logout" type="button" @click="logout">退出登录</button>
    </nav>

    <div class="container">
      <div class="page-title">{{ title }}</div>
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f5f7fa;
  color: #111827;
}

.nav-bar {
  background: #1e3a5f;
  color: #fff;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.nav-brand {
  font-weight: 600;
  margin-right: 8px;
}

.nav-link {
  color: #93c5fd;
  text-decoration: none;
  font-size: 14px;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #fff;
}

.nav-logout {
  margin-left: auto;
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: transparent;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.nav-logout:hover {
  background: rgba(255, 255, 255, 0.15);
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.content {
  min-height: 0;
}

@media (max-width: 900px) {
  .nav-bar {
    gap: 12px;
  }
}
</style>

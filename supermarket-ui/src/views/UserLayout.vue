<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCart } from '../composables/useCart'

const route = useRoute()
const router = useRouter()
const { cartCount } = useCart()

const title = computed(() => {
  if (route.path.startsWith('/user/shop/')) {
    return '商品详情'
  }
  const map = {
    '/user/profile': '个人信息',
    '/user/shop': '商城',
    '/user/cart': '购物车',
    '/user/after-sales': '售后',
  }
  return map[route.path] || '用户首页'
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<template>
  <div class="user-layout">
    <nav class="nav-bar">
      <span class="nav-brand">生鲜超市物流配送系统</span>
      <RouterLink class="nav-link" to="/user/shop">商城</RouterLink>
      <RouterLink class="nav-link" to="/user/cart">
        购物车 <span class="badge">{{ cartCount }}</span>
      </RouterLink>
      <RouterLink class="nav-link" to="/user/profile">个人信息</RouterLink>
      <RouterLink class="nav-link" to="/user/after-sales">售后</RouterLink>
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
.user-layout {
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #fff;
}

.badge {
  min-width: 20px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #1e3a5f;
  background: #93c5fd;
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

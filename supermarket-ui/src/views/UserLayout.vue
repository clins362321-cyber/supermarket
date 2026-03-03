<script setup>
import { computed, ref, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

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

const cartCount = ref(0)
watchEffect(() => {
  const raw = localStorage.getItem('cart')
  const cart = raw ? JSON.parse(raw) : []
  cartCount.value = cart.reduce((sum, it) => sum + (it.qty || 0), 0)
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-title">生鲜配送系统</div>
        <div class="brand-subtitle">用户</div>
      </div>

      <nav class="nav">
        <RouterLink class="nav-item" to="/user/profile">个人信息</RouterLink>
        <RouterLink class="nav-item" to="/user/shop">商城</RouterLink>
        <RouterLink class="nav-item" to="/user/cart">
          购物车 <span class="badge">{{ cartCount }}</span>
        </RouterLink>
        <RouterLink class="nav-item" to="/user/after-sales">售后</RouterLink>
      </nav>

      <button class="logout" type="button" @click="logout">退出登录</button>
    </aside>

    <section class="main">
      <header class="header">
        <div class="header-title">{{ title }}</div>
      </header>
      <main class="content">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  background: #f5f7fa;
}

.sidebar {
  padding: 18px 14px;
  border-right: 1px solid #e5e7eb;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.brand {
  padding: 12px 10px;
  border-radius: 12px;
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
}

.brand-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.brand-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  padding: 10px 12px;
  border-radius: 10px;
  color: #111827;
  text-decoration: none;
  border: 1px solid transparent;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-item.router-link-active {
  border-color: #2563eb;
  background: #eff6ff;
}

.badge {
  min-width: 22px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #0b1220;
  background: #22c55e;
}

.logout {
  margin-top: auto;
}

.main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.content {
  padding: 18px;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 220px 1fr;
  }
}

@media (max-width: 720px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid rgba(148, 163, 184, 0.18);
  }
}
</style>


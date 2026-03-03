<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const error = ref('')
const products = ref([])

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const resp = await fetch('/api/products')
    const data = await resp.json()
    products.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = '加载商品失败'
  } finally {
    loading.value = false
  }
}

const cart = computed(() => {
  const raw = localStorage.getItem('cart')
  return raw ? JSON.parse(raw) : []
})

const saveCart = (next) => {
  localStorage.setItem('cart', JSON.stringify(next))
}

const addToCart = (p) => {
  const next = [...cart.value]
  const idx = next.findIndex((it) => it.productId === p.id)
  if (idx >= 0) {
    next[idx].qty += 1
  } else {
    next.push({
      productId: p.id,
      name: p.name,
      price: Number(p.price),
      qty: 1,
    })
  }
  saveCart(next)
}

const goDetail = (id) => {
  router.push(`/user/shop/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="top">
      <div>
        <div class="title">商城</div>
        <div class="subtitle">选择商品加入购物车</div>
      </div>
      <button class="btn" type="button" @click="load" :disabled="loading">
        {{ loading ? '刷新中...' : '刷新商品' }}
      </button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="loading" class="hint">加载中...</div>
    <div v-else-if="products.length === 0" class="hint">暂无商品</div>

    <div v-else class="grid">
      <div v-for="p in products" :key="p.id" class="card">
        <div class="name">{{ p.name }}</div>
        <div class="desc" v-if="p.description">{{ p.description }}</div>
        <div class="meta">
          <span>¥{{ p.price }}</span>
          <span>库存：{{ p.stock }}</span>
        </div>
        <div class="actions">
          <button class="btn" type="button" @click="goDetail(p.id)">查看详情</button>
          <button class="btn primary" type="button" @click="addToCart(p)" :disabled="p.stock <= 0">
            {{ p.stock <= 0 ? '售罄' : '加入购物车' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.title {
  font-weight: 700;
  color: #111827;
}

.subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name {
  font-weight: 700;
  color: #111827;
}

.desc {
  font-size: 12px;
  color: #6b7280;
}

.meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: #4b5563;
}

.actions {
  margin-top: 4px;
  display: flex;
  gap: 8px;
}

.btn {
  height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  background: #f3f4f6;
  color: #111827;
}

.btn.primary {
  border-color: #16a34a;
  background: #22c55e;
  color: #ffffff;
}

.error {
  margin: 0 0 10px;
  color: #fca5a5;
  font-size: 12px;
}

.hint {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}
</style>


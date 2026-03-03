<script setup>
import { onMounted, ref } from 'vue'

const orders = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  error.value = ''
  const username = localStorage.getItem('username') || ''
  if (!username) {
    error.value = '请先登录用户账号'
    return
  }
  loading.value = true
  try {
    const resp = await fetch(`/api/orders/by-user?username=${encodeURIComponent(username)}`)
    const data = await resp.json()
    if (!resp.ok) {
      error.value = typeof data === 'string' ? data : '加载订单失败'
      return
    }
    orders.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = '加载订单失败，请检查网络或后端服务'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel-title-row">
      <div class="panel-title">售后</div>
      <button class="btn" type="button" @click="load" :disabled="loading">
        {{ loading ? '刷新中...' : '刷新订单' }}
      </button>
    </div>
    <p class="hint">这里展示已下单的商品，可以作为售后处理入口。</p>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="loading" class="hint">加载中...</div>
    <div v-else-if="orders.length === 0" class="hint">暂无订单记录。</div>

    <div v-else class="order-list">
      <div v-for="o in orders" :key="o.order.id" class="order">
        <div class="order-head">
          <div>
            <div class="order-id">订单号：{{ o.order.id }}</div>
            <div class="order-time">下单时间：{{ o.order.createdAt }}</div>
          </div>
          <div class="order-total">合计：¥{{ o.order.totalPrice }}</div>
        </div>
        <div class="items">
          <div v-for="it in o.items" :key="it.id" class="item">
            <div class="name">{{ it.productName }}</div>
            <div class="meta">
              <span>单价：¥{{ it.price }}</span>
              <span>数量：{{ it.qty }}</span>
            </div>
            <div class="sub">小计：¥{{ (Number(it.price) * Number(it.qty)).toFixed(2) }}</div>
            <button class="btn small" type="button" disabled>申请售后（待接入）</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.panel {
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.92);
  padding: 14px;
}

.panel-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
}

.panel-title {
  font-weight: 600;
  color: #e5e7eb;
}

.hint {
  margin: 0 0 8px;
  color: #94a3b8;
  font-size: 13px;
}

.error {
  margin: 0 0 8px;
  color: #fca5a5;
  font-size: 12px;
}

.btn {
  height: 30px;
  padding: 0 10px;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(2, 6, 23, 0.45);
  color: #e5e7eb;
  font-size: 13px;
}

.btn.small {
  height: 28px;
  padding: 0 8px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order {
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(2, 6, 23, 0.5);
  padding: 10px;
}

.order-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.order-id {
  font-size: 13px;
  color: #e5e7eb;
}

.order-time {
  margin-top: 2px;
  font-size: 12px;
  color: #9ca3af;
}

.order-total {
  font-weight: 700;
  color: #22c55e;
}

.items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item {
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  padding: 8px 10px;
}

.name {
  font-weight: 600;
  color: #e5e7eb;
}

.meta {
  margin-top: 2px;
  font-size: 12px;
  color: #cbd5e1;
  display: flex;
  gap: 12px;
}

.sub {
  margin-top: 2px;
  font-size: 12px;
  color: #a5b4fc;
}

@media (max-width: 900px) {
  .order-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>


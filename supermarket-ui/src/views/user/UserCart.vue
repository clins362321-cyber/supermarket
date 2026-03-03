<script setup>
import { computed, ref } from 'vue'

const error = ref('')
const success = ref('')

const readCart = () => {
  const raw = localStorage.getItem('cart')
  return raw ? JSON.parse(raw) : []
}

const cart = ref(readCart())

const save = () => {
  localStorage.setItem('cart', JSON.stringify(cart.value))
}

const removeItem = (productId) => {
  cart.value = cart.value.filter((it) => it.productId !== productId)
  save()
}

const changeQty = (productId, delta) => {
  const next = cart.value.map((it) => ({ ...it }))
  const idx = next.findIndex((it) => it.productId === productId)
  if (idx < 0) return
  next[idx].qty = Math.max(1, (next[idx].qty || 1) + delta)
  cart.value = next
  save()
}

const clear = () => {
  if (!confirm('确认清空购物车吗？')) return
  cart.value = []
  save()
}

const total = computed(() =>
  cart.value.reduce((sum, it) => sum + Number(it.price || 0) * Number(it.qty || 0), 0),
)

const submitOrder = async () => {
  error.value = ''
  success.value = ''
  if (!cart.value.length) {
    error.value = '购物车为空，无法下单'
    return
  }
  const username = localStorage.getItem('username') || ''
  if (!username) {
    error.value = '请先登录用户账号，再下单'
    return
  }

  try {
    const resp = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username,
        items: cart.value.map((it) => ({
          productId: it.productId,
          qty: it.qty,
        })),
      }),
    })
    const text = await resp.text()
    if (!resp.ok) {
      error.value = text || '下单失败'
      return
    }
    let orderId = ''
    try {
      const parsed = JSON.parse(text)
      orderId = typeof parsed === 'number' || typeof parsed === 'string' ? parsed : ''
    } catch (_) {
      // ignore parse error, treat as plain text
    }
    clear()
    success.value = orderId ? `下单成功，订单号：${orderId}` : '下单成功'
  } catch (e) {
    error.value = '下单失败，请检查网络或后端服务'
  }
}
</script>

<template>
  <div class="panel">
    <div class="top">
      <div>
        <div class="title">购物车</div>
        <div class="subtitle">已加入的商品</div>
      </div>
      <button class="btn" type="button" @click="clear" :disabled="cart.length === 0">清空</button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">{{ success }}</p>
    <div v-if="cart.length === 0" class="hint">购物车为空，去商城添加商品吧。</div>

    <div v-else class="list">
      <div v-for="it in cart" :key="it.productId" class="item">
        <div class="name">{{ it.name }}</div>
        <div class="price">¥{{ it.price }}</div>
        <div class="qty">
          <button class="btn small" type="button" @click="changeQty(it.productId, -1)">-</button>
          <span class="num">{{ it.qty }}</span>
          <button class="btn small" type="button" @click="changeQty(it.productId, 1)">+</button>
        </div>
        <div class="sub">小计：¥{{ (Number(it.price) * Number(it.qty)).toFixed(2) }}</div>
        <button class="btn small danger" type="button" @click="removeItem(it.productId)">移除</button>
      </div>
    </div>

    <div v-if="cart.length" class="footer">
      <div>合计：<span class="money">¥{{ total.toFixed(2) }}</span></div>
      <button class="btn primary" type="button" @click="submitOrder">去结算</button>
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

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.title {
  font-weight: 700;
  color: #e5e7eb;
}

.subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item {
  display: grid;
  grid-template-columns: 1fr 120px 160px 160px 90px;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(2, 6, 23, 0.35);
}

.name {
  font-weight: 700;
  color: #e5e7eb;
}

.price,
.sub {
  color: #cbd5e1;
  font-size: 13px;
}

.qty {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.num {
  min-width: 28px;
  text-align: center;
}

.footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.money {
  font-weight: 800;
  color: #22c55e;
}

.btn {
  height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(2, 6, 23, 0.45);
  color: #e5e7eb;
}

.btn.primary {
  border-color: rgba(34, 197, 94, 0.45);
  background: rgba(34, 197, 94, 0.16);
}

.btn.danger {
  border-color: rgba(248, 113, 113, 0.4);
  background: rgba(248, 113, 113, 0.14);
}

.btn.small {
  height: 30px;
  padding: 0 10px;
  border-radius: 9px;
}

.error {
  margin: 0 0 10px;
  color: #fca5a5;
  font-size: 12px;
}

.success {
  margin: 0 0 10px;
  color: #4ade80;
  font-size: 12px;
}

.hint {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .item {
    grid-template-columns: 1fr;
    align-items: start;
  }
  .qty {
    justify-content: flex-start;
  }
}
</style>


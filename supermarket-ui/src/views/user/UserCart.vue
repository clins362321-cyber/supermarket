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
  window.dispatchEvent(new CustomEvent('cart-updated'))
}

const setQty = (productId, val) => {
  const n = Math.max(1, parseInt(val, 10) || 1)
  const next = cart.value.map((it) => ({ ...it }))
  const idx = next.findIndex((it) => it.productId === productId)
  if (idx < 0) return
  next[idx].qty = n
  cart.value = next
  save()
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
      <button class="btn secondary" type="button" @click="clear" :disabled="cart.length === 0">清空</button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">{{ success }}</p>
    <div v-if="cart.length === 0" class="hint">购物车为空，去商城添加商品吧。</div>

    <div v-else class="list">
      <div v-for="it in cart" :key="it.productId" class="item">
        <div class="name">{{ it.name }}</div>
        <div class="price">¥{{ it.price }}</div>
        <div class="qty-wrap">
          <button class="qty-btn" type="button" @click="changeQty(it.productId, -1)">−</button>
          <input
            type="number"
            min="1"
            :value="it.qty"
            class="qty-input"
            @change="(e) => setQty(it.productId, e.target.value)"
          />
          <button class="qty-btn" type="button" @click="changeQty(it.productId, 1)">+</button>
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
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 20px;
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.title {
  font-weight: 700;
  font-size: 18px;
  color: #111827;
}

.subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #6b7280;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  display: grid;
  grid-template-columns: 1fr 100px 180px 120px 80px;
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
}

.name {
  font-weight: 600;
  font-size: 15px;
  color: #111827;
}

.price {
  font-size: 14px;
  color: #374151;
}

.qty-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #fff;
  font-size: 16px;
  color: #374151;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qty-btn:hover {
  background: #f3f4f6;
}

.qty-input {
  width: 56px;
  height: 32px;
  padding: 0 6px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  box-sizing: border-box;
}

.qty-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
}

.sub {
  font-size: 13px;
  color: #6b7280;
}

.footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.money {
  font-weight: 700;
  font-size: 18px;
  color: #22c55e;
}

.btn {
  height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.btn.secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn.primary {
  background: #2563eb;
  color: #fff;
}

.btn.danger {
  background: #ef4444;
  color: #fff;
}

.btn.small {
  height: 32px;
  padding: 6px 12px;
  font-size: 13px;
}

.error {
  margin: 0 0 12px;
  color: #dc2626;
  font-size: 13px;
}

.success {
  margin: 0 0 12px;
  color: #16a34a;
  font-size: 13px;
}

.hint {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

@media (max-width: 900px) {
  .item {
    grid-template-columns: 1fr;
    align-items: start;
  }
  .qty-wrap {
    justify-content: flex-start;
  }
}
</style>

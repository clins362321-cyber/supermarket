<script setup>
import { onMounted, ref } from 'vue'

const orders = ref([])
const afterSalesList = ref([])
const commentedIds = ref(new Set())
const loading = ref(false)
const error = ref('')

const asModal = ref(false)
const asItem = ref(null)
const asReason = ref('')
const asError = ref('')
const asSubmitting = ref(false)

const commentModal = ref(false)
const commentItem = ref(null)
const commentRating = ref(5)
const commentContent = ref('')
const commentError = ref('')
const commentSubmitting = ref(false)

const username = () => localStorage.getItem('username') || ''

const load = async () => {
  error.value = ''
  const un = username()
  if (!un) {
    error.value = '请先登录用户账号'
    return
  }
  loading.value = true
  try {
    const [ordersResp, asResp, commentedResp] = await Promise.all([
      fetch(`/api/orders/by-user?username=${encodeURIComponent(un)}`),
      fetch(`/api/after-sales/by-user?username=${encodeURIComponent(un)}`),
      fetch(`/api/products/commented-by-user?username=${encodeURIComponent(un)}`),
    ])

    const parseJson = (text) => {
      try {
        return JSON.parse(text)
      } catch {
        return null
      }
    }

    const ordersText = await ordersResp.text()
    const ordersData = ordersResp.ok ? parseJson(ordersText) : null
    orders.value = Array.isArray(ordersData) ? ordersData : []
    if (!ordersResp.ok) error.value = ordersText || '加载订单失败'

    const asText = await asResp.text()
    const asData = asResp.ok ? parseJson(asText) : null
    afterSalesList.value = Array.isArray(asData) ? asData : []

    const commentedText = await commentedResp.text()
    const commentedData = commentedResp.ok ? parseJson(commentedText) : null
    commentedIds.value = new Set(Array.isArray(commentedData) ? commentedData : [])
  } catch (e) {
    error.value = '加载失败，请检查网络或后端服务'
  } finally {
    loading.value = false
  }
}

const hasAfterSales = (orderItemId) => {
  return afterSalesList.value.some((as) => as.orderItemId === orderItemId)
}

const hasCommented = (productId) => {
  return commentedIds.value.has(productId)
}

const openAsModal = (item, order) => {
  asItem.value = { item, order }
  asReason.value = ''
  asError.value = ''
  asModal.value = true
}

const closeAsModal = () => {
  asModal.value = false
  asItem.value = null
}

const submitAfterSales = async () => {
  if (!asItem.value || !asReason.value.trim()) {
    asError.value = '请输入售后原因'
    return
  }
  asSubmitting.value = true
  asError.value = ''
  try {
    const resp = await fetch('/api/after-sales', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: asItem.value.order.order.id,
        orderItemId: asItem.value.item.id,
        productId: asItem.value.item.productId,
        productName: asItem.value.item.productName,
        username: username(),
        reason: asReason.value.trim(),
      }),
    })
    const text = await resp.text()
    if (!resp.ok) {
      asError.value = text || '申请失败'
      return
    }
    closeAsModal()
    await load()
  } catch (e) {
    asError.value = '申请失败，请稍后重试'
  } finally {
    asSubmitting.value = false
  }
}

const openCommentModal = (item) => {
  commentItem.value = item
  commentRating.value = 5
  commentContent.value = ''
  commentError.value = ''
  commentModal.value = true
}

const closeCommentModal = () => {
  commentModal.value = false
  commentItem.value = null
}

const submitComment = async () => {
  if (!commentItem.value || !commentContent.value.trim()) {
    commentError.value = '请输入评价内容'
    return
  }
  commentSubmitting.value = true
  commentError.value = ''
  try {
    const resp = await fetch(`/api/products/${commentItem.value.productId}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username(),
        rating: commentRating.value,
        content: commentContent.value.trim(),
      }),
    })
    const text = await resp.text()
    if (!resp.ok) {
      commentError.value = text || '提交失败'
      return
    }
    closeCommentModal()
    await load()
  } catch (e) {
    commentError.value = '提交失败，请稍后重试'
  } finally {
    commentSubmitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel-title-row">
      <div class="panel-title">售后</div>
      <button class="btn refresh" type="button" @click="load" :disabled="loading">
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>
    <p class="hint">展示已下单商品，可申请售后或提交评价。</p>

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
            <div class="item-actions">
              <button
                v-if="!hasAfterSales(it.id)"
                class="btn small primary"
                type="button"
                @click="openAsModal(it, o)"
              >
                申请售后
              </button>
              <span v-else class="badge">售后申请中</span>
              <button
                v-if="!hasCommented(it.productId)"
                class="btn small secondary"
                type="button"
                @click="openCommentModal(it)"
              >
                提交评价
              </button>
              <span v-else class="badge done">已评价</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 申请售后弹窗 -->
  <Transition name="modal">
    <div v-if="asModal" class="modal-overlay" @click="closeAsModal">
      <div class="modal-panel" @click.stop>
        <div class="modal-title">申请售后</div>
        <p v-if="asItem" class="modal-product">{{ asItem.item.productName }}</p>
        <textarea
          v-model="asReason"
          rows="4"
          placeholder="请输入售后原因..."
          class="modal-textarea"
        ></textarea>
        <p v-if="asError" class="modal-error">{{ asError }}</p>
        <div class="modal-actions">
          <button class="btn secondary" type="button" @click="closeAsModal">取消</button>
          <button class="btn primary" type="button" @click="submitAfterSales" :disabled="asSubmitting">
            {{ asSubmitting ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- 提交评价弹窗 -->
  <Transition name="modal">
    <div v-if="commentModal" class="modal-overlay" @click="closeCommentModal">
      <div class="modal-panel" @click.stop>
        <div class="modal-title">提交评价</div>
        <p v-if="commentItem" class="modal-product">{{ commentItem.productName }}</p>
        <div class="modal-rating">
          <span class="rating-label">评分：</span>
          <select v-model.number="commentRating" class="rating-select">
            <option :value="5">5 星</option>
            <option :value="4">4 星</option>
            <option :value="3">3 星</option>
            <option :value="2">2 星</option>
            <option :value="1">1 星</option>
          </select>
        </div>
        <textarea
          v-model="commentContent"
          rows="4"
          placeholder="说说你对这个商品的评价..."
          class="modal-textarea"
        ></textarea>
        <p v-if="commentError" class="modal-error">{{ commentError }}</p>
        <div class="modal-actions">
          <button class="btn secondary" type="button" @click="closeCommentModal">取消</button>
          <button class="btn primary" type="button" @click="submitComment" :disabled="commentSubmitting">
            {{ commentSubmitting ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.panel {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 20px;
}

.panel-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.panel-title {
  font-weight: 600;
  font-size: 18px;
  color: #111827;
}

.hint {
  margin: 0 0 16px;
  color: #6b7280;
  font-size: 13px;
}

.error {
  margin: 0 0 16px;
  color: #dc2626;
  font-size: 13px;
}

.btn {
  height: 36px;
  padding: 0 14px;
  border-radius: 10px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.btn.refresh {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn.small {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
}

.btn.primary {
  background: #2563eb;
  color: #fff;
}

.btn.secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  padding: 16px;
}

.order-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.order-id {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.order-time {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.order-total {
  font-weight: 700;
  font-size: 16px;
  color: #22c55e;
}

.items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 14px;
}

.name {
  font-weight: 600;
  color: #111827;
  font-size: 15px;
}

.meta {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
  display: flex;
  gap: 16px;
}

.sub {
  margin-top: 4px;
  font-size: 13px;
  color: #2563eb;
}

.item-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 4px;
}

.badge {
  font-size: 12px;
  color: #6b7280;
  padding: 4px 10px;
  border-radius: 8px;
  background: #f3f4f6;
}

.badge.done {
  color: #059669;
  background: #d1fae5;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-panel {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.modal-product {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px;
}

.modal-rating {
  margin-bottom: 12px;
}

.rating-label {
  font-size: 14px;
  color: #374151;
  margin-right: 8px;
}

.rating-select {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  background: #fff;
}

.modal-textarea {
  width: 100%;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
}

.modal-textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
}

.modal-error {
  margin: 8px 0 0;
  font-size: 13px;
  color: #dc2626;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.modal-actions .btn {
  flex: 1;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .modal-panel,
.modal-leave-active .modal-panel {
  transition: transform 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-panel,
.modal-leave-to .modal-panel {
  transform: scale(0.95);
}

@media (max-width: 900px) {
  .order-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

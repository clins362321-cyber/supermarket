<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCart } from '../../composables/useCart'

const route = useRoute()
const router = useRouter()
const { addToCart } = useCart()

const loading = ref(false)
const error = ref('')

const product = ref(null)
const medias = ref([])
const comments = ref([])

const drawerVisible = ref(false)
const drawerMode = ref('add')
const drawerQty = ref(1)
const addToast = ref(false)

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const id = route.params.id
    const resp = await fetch(`/api/products/${id}`)
    if (!resp.ok) {
      error.value = '加载商品详情失败'
      return
    }
    const data = await resp.json()
    product.value = data.product
    medias.value = data.medias || []
    comments.value = data.comments || []
  } catch (e) {
    error.value = '加载商品详情失败'
  } finally {
    loading.value = false
  }
}

const openAddDrawer = () => {
  if (!product.value || product.value.stock <= 0) return
  drawerMode.value = 'add'
  drawerQty.value = 1
  drawerVisible.value = true
}

const openBuyDrawer = () => {
  if (!product.value || product.value.stock <= 0) return
  drawerMode.value = 'buy'
  drawerQty.value = 1
  drawerVisible.value = true
}

const closeDrawer = () => {
  drawerVisible.value = false
}

const clampQty = (v) => {
  const n = parseInt(v, 10)
  if (isNaN(n) || n < 1) return 1
  return Math.min(product.value?.stock || 999, n)
}

const onQtyBlur = () => {
  drawerQty.value = clampQty(drawerQty.value)
}

const doAddToCart = () => {
  if (!product.value) return
  const qty = clampQty(drawerQty.value)
  addToCart(product.value, qty)
  closeDrawer()
  addToast.value = true
  setTimeout(() => (addToast.value = false), 2000)
}

const doBuyNow = () => {
  if (!product.value) return
  const qty = clampQty(drawerQty.value)
  addToCart(product.value, qty)
  closeDrawer()
  router.push('/user/cart')
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div v-if="loading" class="hint">加载中...</div>
    <div v-else-if="!product" class="hint">未找到商品</div>
    <template v-else>
      <div class="top">
        <div>
          <div class="title">{{ product.name }}</div>
          <div class="subtitle" v-if="product.description">{{ product.description }}</div>
        </div>
        <div class="price">¥{{ product.price }}</div>
      </div>

      <div class="media" v-if="medias.length">
        <div v-for="m in medias" :key="m.id" class="media-item">
          <img v-if="m.mediaType === 'IMAGE'" :src="m.url" alt="" />
          <video v-else-if="m.mediaType === 'VIDEO'" :src="m.url" controls />
        </div>
      </div>

      <div class="action-bar">
        <button class="btn secondary" type="button" @click="openAddDrawer" :disabled="product.stock <= 0">
          加入购物车
        </button>
        <button class="btn primary" type="button" @click="openBuyDrawer" :disabled="product.stock <= 0">
          立即购买
        </button>
      </div>

      <div class="comments">
        <div class="section-title">商品评价</div>
        <div v-if="comments.length === 0" class="hint small">暂无评价</div>
        <div v-else class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment">
            <div class="comment-head">
              <span class="user">{{ c.username }}</span>
              <span class="rating">{{ c.rating }}★</span>
              <span class="time">{{ c.createdAt }}</span>
            </div>
            <div class="content">{{ c.content }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>

  <Transition name="toast">
    <div v-if="addToast" class="toast">已加入购物车</div>
  </Transition>

  <Transition name="drawer">
    <div v-if="drawerVisible" class="drawer-overlay" @click="closeDrawer">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-handle" @click="closeDrawer"></div>
        <div class="drawer-content" v-if="product">
          <div class="drawer-product">
            <div class="drawer-name">{{ product.name }}</div>
            <div class="drawer-price">¥{{ product.price }}</div>
          </div>
          <div class="drawer-qty">
            <span class="qty-label">数量</span>
            <div class="qty-wrap">
              <button type="button" class="qty-btn" @click="drawerQty = Math.max(1, drawerQty - 1)">−</button>
              <input
                type="number"
                min="1"
                :max="product.stock || 999"
                v-model.number="drawerQty"
                class="qty-input"
                @blur="onQtyBlur"
              />
              <button type="button" class="qty-btn" @click="drawerQty = Math.min(product.stock || 999, drawerQty + 1)">+</button>
            </div>
          </div>
          <div class="drawer-actions">
            <template v-if="drawerMode === 'add'">
              <button class="btn full secondary" type="button" @click="closeDrawer">取消</button>
              <button class="btn full primary" type="button" @click="doAddToCart">加入购物车</button>
            </template>
            <template v-else>
              <button class="btn full secondary" type="button" @click="closeDrawer">取消</button>
              <button class="btn full primary" type="button" @click="doBuyNow">立即购买</button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.page {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 20px;
}

.top {
  display: flex;
  align-items: flex-end;
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

.price {
  font-size: 18px;
  font-weight: 700;
  color: #22c55e;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border: none;
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

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.media {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.media-item {
  width: 180px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
}

.media-item img,
.media-item video {
  display: block;
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.comments {
  margin-top: 8px;
}

.section-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
  font-size: 16px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
}

.comment-head {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
}

.user {
  font-weight: 600;
  color: #111827;
}

.rating {
  color: #f59e0b;
}

.content {
  font-size: 14px;
  color: #374151;
}

.hint {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.hint.small {
  font-size: 12px;
}

.toast {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  background: #111827;
  color: #fff;
  border-radius: 12px;
  font-size: 14px;
  z-index: 200;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(10px);
}

.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 100;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.drawer-panel {
  width: 100%;
  max-width: 480px;
  height: 50vh;
  min-height: 280px;
  background: #fff;
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.15);
  padding: 12px 20px 24px;
  display: flex;
  flex-direction: column;
}

.drawer-handle {
  width: 40px;
  height: 4px;
  background: #d1d5db;
  border-radius: 2px;
  margin: 0 auto 16px;
  cursor: pointer;
}

.drawer-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.drawer-product {
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.drawer-name {
  font-size: 17px;
  font-weight: 600;
  color: #111827;
}

.drawer-price {
  font-size: 20px;
  font-weight: 700;
  color: #22c55e;
  margin-top: 4px;
}

.drawer-qty {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.qty-label {
  font-size: 15px;
  color: #374151;
}

.qty-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  font-size: 18px;
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
  width: 64px;
  height: 36px;
  padding: 0 8px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font-size: 15px;
  text-align: center;
  box-sizing: border-box;
}

.qty-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
}

.drawer-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.drawer-actions .btn.full {
  flex: 1;
  margin: 0;
}

.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.25s ease;
}

.drawer-enter-active .drawer-panel,
.drawer-leave-active .drawer-panel {
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

.drawer-enter-from .drawer-panel,
.drawer-leave-to .drawer-panel {
  transform: translateY(100%);
}
</style>

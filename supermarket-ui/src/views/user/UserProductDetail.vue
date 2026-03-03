<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(false)
const error = ref('')

const product = ref(null)
const medias = ref([])
const comments = ref([])

const commentForm = reactive({
  username: '',
  rating: 5,
  content: '',
})

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

const submitComment = async () => {
  error.value = ''
  if (!commentForm.content) {
    error.value = '请输入评价内容'
    return
  }
  const id = route.params.id
  try {
    const resp = await fetch(`/api/products/${id}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: commentForm.username,
        rating: Number(commentForm.rating),
        content: commentForm.content,
      }),
    })
    if (!resp.ok) {
      const msg = await resp.text()
      error.value = msg || '发表评论失败'
      return
    }
    commentForm.content = ''
    await load()
  } catch (e) {
    error.value = '发表评论失败'
  }
}

onMounted(async () => {
  // 自动从本地获取当前登录用户名，用于评价绑定
  commentForm.username = localStorage.getItem('username') || ''
  await load()
})
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
        <div
          v-for="m in medias"
          :key="m.id"
          class="media-item"
        >
          <img v-if="m.mediaType === 'IMAGE'" :src="m.url" alt="" />
          <video
            v-else-if="m.mediaType === 'VIDEO'"
            :src="m.url"
            controls
          />
        </div>
      </div>

      <div class="comments">
        <div class="section-title">商品评价</div>

        <div v-if="comments.length === 0" class="hint small">还没有评价，快来抢沙发。</div>
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

        <form class="comment-form" @submit.prevent="submitComment">
          <div class="row">
            <input v-model="commentForm.username" placeholder="当前登录用户" disabled />
            <select v-model="commentForm.rating">
              <option :value="5">5 星</option>
              <option :value="4">4 星</option>
              <option :value="3">3 星</option>
              <option :value="2">2 星</option>
              <option :value="1">1 星</option>
            </select>
          </div>
          <textarea
            v-model="commentForm.content"
            rows="3"
            placeholder="说说你对这个商品的评价..."
          ></textarea>
          <p v-if="error" class="error">{{ error }}</p>
          <button class="btn primary" type="submit">提交评价</button>
        </form>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page {
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.92);
  padding: 14px;
}

.top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.title {
  font-weight: 700;
  font-size: 18px;
  color: #e5e7eb;
}

.subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #94a3b8;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: #22c55e;
}

.media {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.media-item {
  width: 180px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(2, 6, 23, 0.8);
}

.media-item img,
.media-item video {
  display: block;
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.comments {
  margin-top: 4px;
}

.section-title {
  font-weight: 600;
  color: #e5e7eb;
  margin-bottom: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}

.comment {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(2, 6, 23, 0.45);
}

.comment-head {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.user {
  font-weight: 600;
  color: #e5e7eb;
}

.rating {
  color: #facc15;
}

.content {
  font-size: 13px;
  color: #e5e7eb;
}

.comment-form {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.row {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 8px;
}

input,
select,
textarea {
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(2, 6, 23, 0.55);
  color: #e5e7eb;
  padding: 6px 10px;
  box-sizing: border-box;
  outline: none;
  font-size: 13px;
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

.hint {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

.hint.small {
  font-size: 12px;
}

.error {
  margin: 0;
  color: #fca5a5;
  font-size: 12px;
}

@media (max-width: 900px) {
  .row {
    grid-template-columns: 1fr;
  }
}
</style>


<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import MediaUploader from '../../components/MediaUploader.vue'

const router = useRouter()

const loading = ref(false)
const error = ref('')
const items = ref([])

const editingId = ref(null)
const form = reactive({
  name: '',
  description: '',
  price: '',
  stock: '',
  medias: [],
})

const resetForm = () => {
  editingId.value = null
  form.name = ''
  form.description = ''
  form.price = ''
  form.stock = ''
  form.medias = []
  error.value = ''
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const resp = await fetch('/api/products')
    const data = await resp.json()
    items.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = '加载商品失败，请检查后端是否启动'
  } finally {
    loading.value = false
  }
}

const startEdit = async (p) => {
  editingId.value = p.id
  form.name = p.name || ''
  form.description = p.description || ''
  form.price = p.price ?? ''
  form.stock = p.stock ?? ''
  form.medias = []

  try {
    const resp = await fetch(`/api/products/${p.id}`)
    if (!resp.ok) return
    const detail = await resp.json()
    const medias = detail.medias || []
    form.medias = medias.map((m) => ({
      id: m.id,
      url: m.url,
      mediaType: m.mediaType,
    }))
  } catch (_) {
    // 忽略详情加载失败，仅编辑基础信息
  }
}

const submit = async () => {
  error.value = ''
  if (!form.name) {
    error.value = '请输入商品名称'
    return
  }
  if (form.price === '' || Number.isNaN(Number(form.price))) {
    error.value = '请输入正确的价格'
    return
  }
  if (form.stock === '' || Number.isNaN(Number(form.stock))) {
    error.value = '请输入正确的库存'
    return
  }

  const images = form.medias
    .filter((m) => m.mediaType === 'IMAGE')
    .map((m) => m.url)

  const videos = form.medias
    .filter((m) => m.mediaType === 'VIDEO')
    .map((m) => m.url)

  const payload = {
    name: form.name,
    description: form.description || null,
    price: Number(form.price),
    stock: Number(form.stock),
    images,
    videos,
  }

  try {
    const isEdit = editingId.value != null
    const resp = await fetch(isEdit ? `/api/products/${editingId.value}` : '/api/products', {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!resp.ok) {
      const msg = await resp.text()
      error.value = msg || '保存失败'
      return
    }

    resetForm()
    await load()
  } catch (e) {
    error.value = '保存失败，请检查网络或后端'
  }
}

const removeItem = async (id) => {
  if (!confirm('确认删除该商品吗？')) return
  try {
    const resp = await fetch(`/api/products/${id}`, { method: 'DELETE' })
    if (!resp.ok) {
      error.value = '删除失败'
      return
    }
    await load()
  } catch (e) {
    error.value = '删除失败，请检查网络或后端'
  }
}

const goDetail = (id) => {
  router.push(`/admin/products/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="wrap">
    <div class="panel">
      <div class="panel-title">{{ editingId ? '编辑商品' : '新增商品' }}</div>
      <div class="form">
        <div class="field">
          <div class="label">名称</div>
          <input v-model="form.name" placeholder="如：新鲜苹果" />
        </div>
        <div class="field">
          <div class="label">描述</div>
          <input v-model="form.description" placeholder="可选" />
        </div>
        <div class="row">
          <div class="field">
            <div class="label">价格</div>
            <input v-model="form.price" placeholder="如：8.80" />
          </div>
          <div class="field">
            <div class="label">库存</div>
            <input v-model="form.stock" placeholder="如：200" />
          </div>
        </div>

        <div class="field">
          <div class="label">商品图片 / 视频</div>
          <MediaUploader v-model="form.medias" />
        </div>

        <p v-if="error" class="error">{{ error }}</p>

        <div class="actions">
          <button class="btn primary" type="button" @click="submit">
            {{ editingId ? '保存修改' : '新增商品' }}
          </button>
          <button class="btn" type="button" @click="resetForm">清空</button>
          <button class="btn" type="button" @click="load" :disabled="loading">
            {{ loading ? '刷新中...' : '刷新列表' }}
          </button>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-title">商品列表</div>
      <div v-if="loading" class="hint">加载中...</div>
      <div v-else-if="items.length === 0" class="hint">暂无商品</div>
      <div v-else class="table">
        <div class="tr head">
          <div>ID</div>
          <div>名称</div>
          <div>价格</div>
          <div>库存</div>
          <div>操作</div>
        </div>
        <div v-for="p in items" :key="p.id" class="tr">
          <div>{{ p.id }}</div>
          <div class="name">
            <div class="n clickable" @click="goDetail(p.id)">{{ p.name }}</div>
            <div class="d" v-if="p.description">{{ p.description }}</div>
          </div>
          <div>¥{{ p.price }}</div>
          <div>{{ p.stock }}</div>
          <div class="ops">
            <button class="btn small" type="button" @click="goDetail(p.id)">详情</button>
            <button class="btn small" type="button" @click="startEdit(p)">编辑</button>
            <button class="btn small danger" type="button" @click="removeItem(p.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
}

.panel {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
}

.panel-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 10px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.field .label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
}

input,
textarea {
  width: 100%;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  color: #111827;
  padding: 8px 10px;
  box-sizing: border-box;
  outline: none;
}

input:focus,
textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 4px;
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

.btn.danger {
  border-color: #dc2626;
  background: #ef4444;
  color: #ffffff;
}

.btn.small {
  height: 30px;
  padding: 0 10px;
  border-radius: 9px;
}

.error {
  margin: 0;
  color: #dc2626;
  font-size: 12px;
}

.hint {
  color: #6b7280;
  font-size: 13px;
}

.table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tr {
  display: grid;
  grid-template-columns: 64px 1fr 120px 90px 160px;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
}

.tr.head {
  color: #6b7280;
  font-size: 12px;
  background: transparent;
  border-color: transparent;
  padding-top: 0;
}

.name .n {
  font-weight: 600;
  color: #111827;
}

.name .n.clickable {
  cursor: pointer;
  text-decoration: underline;
}

.name .d {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.ops {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .wrap {
    grid-template-columns: 1fr;
  }
}
</style>


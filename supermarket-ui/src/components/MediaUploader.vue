<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue'])

const innerList = ref([])

watch(
  () => props.modelValue,
  (val) => {
    innerList.value = Array.isArray(val) ? [...val] : []
  },
  { immediate: true, deep: true },
)

const fileInput = ref(null)
const uploading = ref(false)
const error = ref('')
const isDragOver = ref(false)

const detectMediaType = (file) => {
  const name = file.name || ''
  const ext = name.slice(name.lastIndexOf('.')).toLowerCase()
  if (file.type && file.type.startsWith('video/')) return 'VIDEO'
  if (ext === '.mp4' || ext === '.mov' || ext === '.avi' || ext === '.mkv') return 'VIDEO'
  return 'IMAGE'
}

const updateListWithUploaded = (files, urls) => {
  const next = [...innerList.value]
  files.forEach((file, index) => {
    const url = urls[index] ?? urls[urls.length - 1]
    if (!url) return
    next.push({
      id: Date.now() + Math.random() + index,
      url,
      mediaType: detectMediaType(file),
    })
  })
  innerList.value = next
  emit('update:modelValue', next)
}

const uploadFiles = async (files) => {
  const list = Array.from(files || [])
  if (!list.length) return
  uploading.value = true
  error.value = ''
  try {
    const formData = new FormData()
    list.forEach((f) => formData.append('files', f))
    const resp = await fetch('/api/products/upload-files', {
      method: 'POST',
      body: formData,
    })
    if (!resp.ok) {
      const msg = await resp.text()
      throw new Error(msg || '上传失败')
    }
    const urls = await resp.json()
    if (!Array.isArray(urls)) {
      throw new Error('上传返回数据格式不正确')
    }
    updateListWithUploaded(list, urls)
  } catch (e) {
    error.value = e?.message || '上传失败，请稍后重试'
  } finally {
    uploading.value = false
    isDragOver.value = false
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

const onFileChange = (e) => {
  const files = e.target.files
  uploadFiles(files)
}

const onDrop = (e) => {
  const files = e.dataTransfer?.files
  uploadFiles(files)
}

const onDragOver = () => {
  isDragOver.value = true
}

const onDragLeave = () => {
  isDragOver.value = false
}

const triggerSelect = () => {
  if (fileInput.value && !uploading.value) {
    fileInput.value.click()
  }
}

const removeItem = (idx) => {
  const next = innerList.value.slice()
  next.splice(idx, 1)
  innerList.value = next
  emit('update:modelValue', next)
}
</script>

<template>
  <div class="media-uploader">
    <div class="list">
      <div
        v-for="(m, index) in innerList"
        :key="m.id ?? m.url + index"
        class="item"
      >
        <div class="preview">
          <img
            v-if="m.mediaType === 'IMAGE'"
            :src="m.url"
            alt=""
          />
          <video
            v-else-if="m.mediaType === 'VIDEO'"
            :src="m.url"
            controls
          />
          <div v-else class="placeholder">未知类型</div>
        </div>
        <div class="meta">
          <div class="tag">
            {{ m.mediaType === 'IMAGE' ? '图片' : m.mediaType === 'VIDEO' ? '视频' : '其他' }}
          </div>
          <div class="url" :title="m.url">{{ m.url }}</div>
        </div>
        <button class="remove" type="button" @click="removeItem(index)">删除</button>
      </div>

      <div
        class="add"
        :class="{ 'drag-over': isDragOver }"
        @click="triggerSelect"
        @dragover.prevent="onDragOver"
        @dragenter.prevent="onDragOver"
        @dragleave.prevent="onDragLeave"
        @drop.prevent="onDrop"
      >
        <span class="plus">＋</span>
        <span class="text">
          {{ uploading ? '上传中...' : '点击或拖拽文件到此处' }}
        </span>
        <span class="hint-text">支持图片、视频，多文件自动排队上传</span>
      </div>
    </div>

    <input
      ref="fileInput"
      type="file"
      multiple
      accept="image/*,video/*"
      class="hidden-input"
      @change="onFileChange"
    />

    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<style scoped>
.media-uploader {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.item {
  width: 140px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview {
  width: 100%;
  height: 90px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.preview img,
.preview video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder {
  font-size: 12px;
  color: #9ca3af;
}

.meta {
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 0 6px;
  height: 18px;
  border-radius: 999px;
  font-size: 11px;
  color: #111827;
  background: #dbeafe;
}

.url {
  font-size: 11px;
  color: #6b7280;
  word-break: break-all;
  max-height: 32px;
  overflow: hidden;
}

.remove {
  border: none;
  border-top: 1px solid #e5e7eb;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 12px;
  padding: 4px 0;
  cursor: pointer;
}

.add {
  width: 140px;
  height: 140px;
  border-radius: 10px;
  border: 1px dashed #9ca3af;
  background: #f9fafb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #6b7280;
  cursor: pointer;
}

.add:hover {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
}

.add.drag-over {
  border-color: #22c55e;
  background: #ecfdf5;
  color: #15803d;
}

.plus {
  font-size: 22px;
  line-height: 1;
}

.text {
  font-size: 12px;
}

.hint-text {
  margin-top: 2px;
  font-size: 11px;
  color: #9ca3af;
  text-align: center;
}

.hidden-input {
  display: none;
}

.error {
  margin-top: 4px;
  font-size: 12px;
  color: #dc2626;
}

.editor {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.editor-row {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 8px;
}

.input,
.select {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  padding: 6px 8px;
  font-size: 13px;
  outline: none;
}

.input:focus,
.select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.15);
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.btn {
  height: 30px;
  padding: 0 10px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #f3f4f6;
  font-size: 13px;
  color: #111827;
}

.btn.primary {
  border-color: #16a34a;
  background: #22c55e;
  color: #ffffff;
}

.btn[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 700px) {
  .editor-row {
    grid-template-columns: 1fr;
  }
}
</style>


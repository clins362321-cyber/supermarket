<script setup>
import { onMounted, ref } from 'vue'

const STATUS_MAP = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  CLOSED: '已关闭',
  REJECTED: '已驳回',
}

const list = ref([])
const loading = ref(false)
const error = ref('')

const statusModal = ref({ show: false, item: null, status: '' })

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const resp = await fetch('/api/after-sales')
    list.value = await resp.json()
    if (!Array.isArray(list.value)) list.value = []
  } catch (e) {
    error.value = '加载售后列表失败'
  } finally {
    loading.value = false
  }
}

const openStatusModal = (item) => {
  statusModal.value = { show: true, item, status: item.status || 'PENDING' }
}

const saveStatus = async () => {
  const { item, status } = statusModal.value
  if (!item) return
  try {
    const resp = await fetch(`/api/after-sales/${item.id}/status?status=${encodeURIComponent(status)}`, {
      method: 'PUT',
    })
    const text = await resp.text()
    if (!resp.ok) {
      error.value = text || '更新状态失败'
      return
    }
    statusModal.value.show = false
    await load()
  } catch (e) {
    error.value = '更新状态失败'
  }
}

const formatDate = (d) => {
  if (!d) return '-'
  const dt = typeof d === 'string' ? new Date(d) : d
  return dt.toLocaleString('zh-CN')
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel-title-row">
      <div class="panel-title">售后管理</div>
      <button class="btn" type="button" @click="load" :disabled="loading">
        {{ loading ? '加载中...' : '刷新' }}
      </button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="loading" class="hint">加载中...</div>
    <div v-else-if="list.length === 0" class="hint">暂无售后申请</div>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>订单号</th>
            <th>用户</th>
            <th>商品</th>
            <th>售后原因</th>
            <th>状态</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.orderId }}</td>
            <td>{{ item.username }}</td>
            <td>{{ item.productName }}</td>
            <td class="reason">{{ item.reason || '-' }}</td>
            <td>
              <span :class="['status', 'status-' + (item.status || 'PENDING')]">
                {{ STATUS_MAP[item.status] || item.status }}
              </span>
            </td>
            <td>{{ formatDate(item.createdAt) }}</td>
            <td>
              <button class="btn small primary" type="button" @click="openStatusModal(item)">
                改状态
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <div v-if="statusModal.show" class="modal-overlay" @click.self="statusModal.show = false">
    <div class="modal">
      <div class="modal-title">更新售后状态</div>
      <p v-if="statusModal.item" class="modal-product">{{ statusModal.item.productName }} - {{ statusModal.item.username }}</p>
      <div class="form-row">
        <label>状态</label>
        <select v-model="statusModal.status">
          <option value="PENDING">待处理</option>
          <option value="PROCESSING">处理中</option>
          <option value="CLOSED">已关闭</option>
          <option value="REJECTED">已驳回</option>
        </select>
      </div>
      <div class="modal-actions">
        <button class="btn" type="button" @click="statusModal.show = false">取消</button>
        <button class="btn primary" type="button" @click="saveStatus">确定</button>
      </div>
    </div>
  </div>
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
  margin-bottom: 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.btn {
  height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  border: none;
  background: #e5e7eb;
  color: #374151;
  font-size: 14px;
}

.btn.primary {
  background: #2563eb;
  color: #fff;
}

.btn.small {
  height: 30px;
  padding: 6px 12px;
  font-size: 13px;
}

.error {
  margin: 0 0 12px;
  color: #dc2626;
  font-size: 13px;
}

.hint {
  color: #6b7280;
  font-size: 13px;
}

.table-wrap {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th,
.table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.table th {
  background: #f9fafb;
  color: #6b7280;
  font-weight: 500;
}

.reason {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
}

.status-PENDING {
  background: #fef3c7;
  color: #92400e;
}

.status-PROCESSING {
  background: #dbeafe;
  color: #1e40af;
}

.status-CLOSED {
  background: #d1fae5;
  color: #065f46;
}

.status-REJECTED {
  background: #fee2e2;
  color: #991b1b;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  min-width: 360px;
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
  margin: 0 0 16px;
}

.form-row {
  margin-bottom: 16px;
}

.form-row label {
  display: block;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
}

.form-row select {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

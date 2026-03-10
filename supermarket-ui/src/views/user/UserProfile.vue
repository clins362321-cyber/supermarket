<script setup>
import { onMounted, reactive, ref } from 'vue'

const loading = ref(false)
const savingProfile = ref(false)
const savingAddress = ref(false)
const error = ref('')
const success = ref('')

const profile = reactive({
  username: '',
  fullName: '',
  phone: '',
  balance: 0,
})

const addresses = ref([])

const addrForm = reactive({
  id: null,
  receiverName: '',
  phone: '',
  detail: '',
  isDefault: false,
})

// 当前登录账号（从 localStorage 中读取），避免在模板里直接访问 localStorage
const loginUsername = ref('')

const resetAddrForm = () => {
  addrForm.id = null
  addrForm.receiverName = ''
  addrForm.phone = ''
  addrForm.detail = ''
  addrForm.isDefault = false
}

const load = async () => {
  error.value = ''
  success.value = ''
  const username = loginUsername.value
  if (!username) {
    error.value = '请先登录用户账号'
    return
  }
  loading.value = true
  try {
    const resp = await fetch(`/api/users/profile?username=${encodeURIComponent(username)}`)
    const data = await resp.json()
    if (!resp.ok) {
      error.value = typeof data === 'string' ? data : '加载个人信息失败'
      return
    }
    profile.username = data.username
    profile.fullName = data.fullName || ''
    profile.phone = data.phone || ''
    profile.balance = data.balance ?? 0
    addresses.value = Array.isArray(data.addresses) ? data.addresses : []
  } catch (e) {
    error.value = '加载个人信息失败，请检查网络或后端服务'
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  error.value = ''
  success.value = ''
  const username = profile.username || loginUsername.value
  if (!username) {
    error.value = '请先登录用户账号'
    return
  }
  savingProfile.value = true
  try {
    const resp = await fetch(`/api/users/profile?username=${encodeURIComponent(username)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fullName: profile.fullName,
        phone: profile.phone,
      }),
    })
    const text = await resp.text()
    if (!resp.ok) {
      error.value = text || '保存个人资料失败'
      return
    }
    success.value = '个人资料已保存'
  } catch (e) {
    error.value = '保存个人资料失败，请检查网络或后端服务'
  } finally {
    savingProfile.value = false
  }
}

const editAddress = (addr) => {
  addrForm.id = addr.id
  addrForm.receiverName = addr.receiverName
  addrForm.phone = addr.phone
  addrForm.detail = addr.detail
  addrForm.isDefault = !!addr.default
}

const saveAddress = async () => {
  error.value = ''
  success.value = ''
  const username = profile.username || loginUsername.value
  if (!username) {
    error.value = '请先登录用户账号'
    return
  }
  if (!addrForm.receiverName || !addrForm.phone || !addrForm.detail) {
    error.value = '请填写完整的收货人、手机号和详细地址'
    return
  }
  savingAddress.value = true
  try {
    const resp = await fetch(`/api/users/addresses?username=${encodeURIComponent(username)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id: addrForm.id,
        receiverName: addrForm.receiverName,
        phone: addrForm.phone,
        detail: addrForm.detail,
        default: addrForm.isDefault,
      }),
    })
    const data = await resp.json()
    if (!resp.ok) {
      error.value = typeof data === 'string' ? data : '保存地址失败'
      return
    }
    // 更新本地列表：如果有 id 就替换，否则追加
    const idx = addresses.value.findIndex((a) => a.id === data.id)
    if (idx >= 0) {
      addresses.value.splice(idx, 1, data)
    } else {
      addresses.value.unshift(data)
    }
    resetAddrForm()
    success.value = '地址已保存'
  } catch (e) {
    error.value = '保存地址失败，请检查网络或后端服务'
  } finally {
    savingAddress.value = false
  }
}

const deleteAddress = async (id) => {
  if (!confirm('确认删除该地址吗？')) return
  error.value = ''
  success.value = ''
  try {
    const resp = await fetch(`/api/users/addresses/${id}`, { method: 'DELETE' })
    if (!resp.ok && resp.status !== 204) {
      const text = await resp.text()
      error.value = text || '删除地址失败'
      return
    }
    addresses.value = addresses.value.filter((a) => a.id !== id)
    success.value = '地址已删除'
  } catch (e) {
    error.value = '删除地址失败，请检查网络或后端服务'
  }
}

onMounted(() => {
  if (typeof window !== 'undefined' && window.localStorage) {
    loginUsername.value = window.localStorage.getItem('username') || ''
  }
  load()
})
</script>

<template>
  <div class="panel">
    <div class="panel-title">个人信息</div>
    <p class="hint">管理用户资料、手机号和收货地址。</p>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">{{ success }}</p>

    <div class="section">
      <div class="section-title">基本资料</div>
      <div class="form">
        <div class="field">
          <div class="label">登录账号</div>
          <input :value="profile.username || loginUsername || ''" disabled />
        </div>
        <div class="field">
          <div class="label">姓名 / 昵称</div>
          <input v-model="profile.fullName" placeholder="请输入姓名或昵称" />
        </div>
        <div class="field">
          <div class="label">手机号</div>
          <input v-model="profile.phone" placeholder="请输入手机号" />
        </div>
        <div class="field">
          <div class="label">账户余额</div>
          <input :value="profile.balance ?? 0" disabled />
        </div>
        <div class="actions">
          <button class="btn primary" type="button" @click="saveProfile" :disabled="savingProfile">
            {{ savingProfile ? '保存中...' : '保存资料' }}
          </button>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-title">收货地址管理</div>

      <div class="addr-form">
        <div class="row">
          <div class="field">
            <div class="label">收货人</div>
            <input v-model="addrForm.receiverName" placeholder="姓名" />
          </div>
          <div class="field">
            <div class="label">手机号</div>
            <input v-model="addrForm.phone" placeholder="手机号" />
          </div>
        </div>
        <div class="field">
          <div class="label">详细地址</div>
          <input v-model="addrForm.detail" placeholder="如：XX路XX小区X栋X单元XXX室" />
        </div>
        <div class="field inline">
          <label>
            <input v-model="addrForm.isDefault" type="checkbox" />
            设为默认地址
          </label>
        </div>
        <div class="actions">
          <button class="btn primary" type="button" @click="saveAddress" :disabled="savingAddress">
            {{ savingAddress ? '保存中...' : addrForm.id ? '保存修改' : '新增地址' }}
          </button>
          <button class="btn" type="button" @click="resetAddrForm">重置</button>
        </div>
      </div>

      <div v-if="loading" class="hint">加载地址中...</div>
      <div v-else-if="addresses.length === 0" class="hint">暂无地址，请先添加一个。</div>
      <div v-else class="addr-list">
        <div v-for="a in addresses" :key="a.id" class="addr-item">
          <div class="addr-main">
            <div class="addr-line">
              <span class="name">{{ a.receiverName }}</span>
              <span class="phone">{{ a.phone }}</span>
              <span v-if="a.default" class="tag">默认</span>
            </div>
            <div class="addr-detail">{{ a.detail }}</div>
          </div>
          <div class="addr-ops">
            <button class="btn small" type="button" @click="editAddress(a)">编辑</button>
            <button class="btn small danger" type="button" @click="deleteAddress(a.id)">删除</button>
          </div>
        </div>
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

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 12px;
}

.section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.hint {
  margin: 0 0 8px;
  color: #6b7280;
  font-size: 13px;
}

.error {
  margin: 0 0 6px;
  color: #dc2626;
  font-size: 12px;
}

.success {
  margin: 0 0 6px;
  color: #16a34a;
  font-size: 12px;
}

.form,
.addr-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field .label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
}

input {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #111827;
  padding: 10px 12px;
  box-sizing: border-box;
  outline: none;
  font-size: 14px;
}

input:disabled {
  opacity: 0.8;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.field.inline {
  margin-top: 2px;
}

.field.inline label {
  font-size: 13px;
  color: #6b7280;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
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

.btn.danger {
  background: #ef4444;
  color: #fff;
}

.btn.small {
  height: 30px;
  padding: 6px 12px;
  font-size: 13px;
}

.addr-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.addr-item {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.addr-main {
  flex: 1;
  min-width: 0;
}

.addr-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  font-size: 14px;
  color: #111827;
}

.addr-line .name {
  font-weight: 600;
}

.addr-line .phone {
  color: #6b7280;
}

.tag {
  padding: 2px 8px;
  border-radius: 6px;
  background: #d1fae5;
  color: #065f46;
  font-size: 12px;
}

.addr-detail {
  margin-top: 4px;
  font-size: 13px;
  color: #6b7280;
}

.addr-ops {
  display: flex;
  gap: 8px;
}

@media (max-width: 900px) {
  .row {
    grid-template-columns: 1fr;
  }
  .addr-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>


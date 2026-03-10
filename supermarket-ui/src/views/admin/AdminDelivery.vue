<script setup>
import { onMounted, ref } from 'vue'

const STATUS_MAP = {
  PENDING_ACCEPT: '待接单',
  IN_DELIVERY: '配送中',
  DELIVERED: '已签收',
  EXCEPTION: '异常',
}

const orders = ref([])
const persons = ref([])
const routes = ref([])
const loading = ref(false)
const error = ref('')

const personForm = ref({ name: '', phone: '' })
const routeForm = ref({ name: '', description: '' })

const assignModal = ref({ show: false, order: null, personId: '', routeId: '', addressId: '' })
const statusModal = ref({ show: false, order: null, status: '', remark: '' })
const addressesCache = ref({})

const loadOrders = async () => {
  try {
    const resp = await fetch('/api/delivery/orders')
    orders.value = await resp.json()
  } catch (e) {
    error.value = '加载订单失败'
  }
}

const loadPersons = async () => {
  try {
    const resp = await fetch('/api/delivery/persons')
    persons.value = await resp.json()
  } catch (e) {
    error.value = '加载配送员失败'
  }
}

const loadRoutes = async () => {
  try {
    const resp = await fetch('/api/delivery/routes')
    routes.value = await resp.json()
  } catch (e) {
    error.value = '加载路线失败'
  }
}

const loadAll = async () => {
  loading.value = true
  error.value = ''
  await Promise.all([loadOrders(), loadPersons(), loadRoutes()])
  loading.value = false
}

const openAssign = async (dto) => {
  assignModal.value = {
    show: true,
    order: dto.order,
    personId: dto.order.deliveryPersonId || '',
    routeId: dto.order.deliveryRouteId || '',
    addressId: dto.order.addressId || '',
  }
  if (dto.order?.username) {
    await getAddresses(dto.order.username)
  }
}

const saveAssign = async () => {
  const { order, personId, routeId, addressId } = assignModal.value
  if (!order) return
  try {
    const params = new URLSearchParams()
    if (personId != null && personId !== '') params.set('personId', personId)
    if (routeId != null && routeId !== '') params.set('routeId', routeId)
    if (addressId != null && addressId !== '') params.set('addressId', addressId)
    const resp = await fetch(`/api/delivery/orders/${order.id}/assign?${params}`, { method: 'PUT' })
    if (!resp.ok) {
      error.value = '分配失败'
      return
    }
    assignModal.value.show = false
    await loadOrders()
  } catch (e) {
    error.value = '分配失败'
  }
}

const openStatus = (order) => {
  statusModal.value = { show: true, order, status: order.deliveryStatus || 'PENDING_ACCEPT', remark: order.deliveryRemark || '' }
}

const saveStatus = async () => {
  const { order, status, remark } = statusModal.value
  if (!order) return
  try {
    const params = new URLSearchParams({ status })
    if (remark) params.set('remark', remark)
    const resp = await fetch(`/api/delivery/orders/${order.id}/status?${params}`, { method: 'PUT' })
    if (!resp.ok) {
      error.value = '更新状态失败'
      return
    }
    statusModal.value.show = false
    await loadOrders()
  } catch (e) {
    error.value = '更新状态失败'
  }
}

const createPerson = async () => {
  if (!personForm.value.name || !personForm.value.phone) {
    error.value = '请输入姓名和手机号'
    return
  }
  try {
    const resp = await fetch('/api/delivery/persons', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: personForm.value.name, phone: personForm.value.phone, status: 'ACTIVE' }),
    })
    if (!resp.ok) {
      error.value = '新增配送员失败'
      return
    }
    personForm.value = { name: '', phone: '' }
    await loadPersons()
  } catch (e) {
    error.value = '新增配送员失败'
  }
}

const createRoute = async () => {
  if (!routeForm.value.name) {
    error.value = '请输入路线名称'
    return
  }
  try {
    const resp = await fetch('/api/delivery/routes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: routeForm.value.name, description: routeForm.value.description || '' }),
    })
    if (!resp.ok) {
      error.value = '新增路线失败'
      return
    }
    routeForm.value = { name: '', description: '' }
    await loadRoutes()
  } catch (e) {
    error.value = '新增路线失败'
  }
}

const getAddresses = async (username) => {
  if (addressesCache.value[username]) return addressesCache.value[username]
  try {
    const resp = await fetch(`/api/delivery/addresses?username=${encodeURIComponent(username)}`)
    const list = await resp.json()
    addressesCache.value[username] = list
    return list
  } catch (_) {
    return []
  }
}

const formatDate = (d) => {
  if (!d) return '-'
  const dt = typeof d === 'string' ? new Date(d) : d
  return dt.toLocaleString('zh-CN')
}

onMounted(loadAll)
</script>

<template>
  <div class="wrap">
    <div class="panel main-panel">
      <div class="panel-title">配送订单</div>
      <button class="btn" type="button" @click="loadAll" :disabled="loading">
        {{ loading ? '加载中...' : '刷新' }}
      </button>
      <p v-if="error" class="error">{{ error }}</p>
      <div v-if="orders.length === 0" class="hint">暂无订单</div>
      <div v-else class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>订单ID</th>
              <th>用户</th>
              <th>总价</th>
              <th>下单时间</th>
              <th>收货地址</th>
              <th>配送员</th>
              <th>路线</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="dto in orders" :key="dto.order.id">
              <td>{{ dto.order.id }}</td>
              <td>{{ dto.order.username }}</td>
              <td>¥{{ dto.order.totalPrice }}</td>
              <td>{{ formatDate(dto.order.createdAt) }}</td>
              <td class="addr">
                <template v-if="dto.address">
                  {{ dto.address.receiverName }} {{ dto.address.phone }}<br />{{ dto.address.detail }}
                </template>
                <span v-else class="muted">未分配</span>
              </td>
              <td>{{ dto.deliveryPersonName || '-' }}</td>
              <td>{{ dto.deliveryRouteName || '-' }}</td>
              <td>
                <span :class="['status', dto.order.deliveryStatus]">{{ STATUS_MAP[dto.order.deliveryStatus] || dto.order.deliveryStatus }}</span>
              </td>
              <td class="ops">
                <button class="btn small" @click="openAssign(dto)">分配</button>
                <button class="btn small primary" @click="openStatus(dto.order)">改状态</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="side">
      <div class="panel">
        <div class="panel-title">配送员</div>
        <div class="create">
          <input v-model="personForm.name" placeholder="姓名" />
          <input v-model="personForm.phone" placeholder="手机" />
          <button class="btn primary" @click="createPerson">新增</button>
        </div>
        <ul class="list">
          <li v-for="p in persons" :key="p.id">{{ p.name }} {{ p.phone }}</li>
        </ul>
      </div>
      <div class="panel">
        <div class="panel-title">配送路线</div>
        <div class="create">
          <input v-model="routeForm.name" placeholder="路线名称" />
          <input v-model="routeForm.description" placeholder="描述" />
          <button class="btn primary" @click="createRoute">新增</button>
        </div>
        <ul class="list">
          <li v-for="r in routes" :key="r.id">{{ r.name }} <span v-if="r.description" class="muted">({{ r.description }})</span></li>
        </ul>
      </div>
    </div>
  </div>

  <!-- 分配弹窗 -->
  <div v-if="assignModal.show" class="modal-overlay" @click.self="assignModal.show = false">
    <div class="modal">
      <div class="modal-title">分配配送</div>
      <div class="form-row">
        <label>配送员</label>
        <select v-model="assignModal.personId">
          <option value="">请选择</option>
          <option v-for="p in persons" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>
      <div class="form-row">
        <label>路线</label>
        <select v-model="assignModal.routeId">
          <option value="">请选择</option>
          <option v-for="r in routes" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>
      </div>
      <div class="form-row">
        <label>收货地址</label>
        <select v-model="assignModal.addressId">
          <option value="">请选择</option>
          <option v-for="a in (addressesCache[assignModal.order?.username] || [])" :key="a.id" :value="a.id">
            {{ a.receiverName }} {{ a.detail }}
          </option>
        </select>
      </div>
      <div class="modal-actions">
        <button class="btn" @click="assignModal.show = false">取消</button>
        <button class="btn primary" @click="saveAssign">确定</button>
      </div>
    </div>
  </div>

  <!-- 状态弹窗 -->
  <div v-if="statusModal.show" class="modal-overlay" @click.self="statusModal.show = false">
    <div class="modal">
      <div class="modal-title">更新配送状态</div>
      <div class="form-row">
        <label>状态</label>
        <select v-model="statusModal.status">
          <option value="PENDING_ACCEPT">待接单</option>
          <option value="IN_DELIVERY">配送中</option>
          <option value="DELIVERED">已签收</option>
          <option value="EXCEPTION">异常</option>
        </select>
      </div>
      <div class="form-row">
        <label>备注</label>
        <input v-model="statusModal.remark" placeholder="异常时可填写" />
      </div>
      <div class="modal-actions">
        <button class="btn" @click="statusModal.show = false">取消</button>
        <button class="btn primary" @click="saveStatus">确定</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 16px;
}

.panel {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 20px;
  margin-bottom: 20px;
}

.main-panel {
  min-width: 0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 12px;
}

.btn {
  height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  border: none;
  background: #e5e7eb;
  color: #374151;
  margin-bottom: 10px;
}

.btn.primary {
  background: #2563eb;
  color: #fff;
}

.btn.small {
  height: 30px;
  padding: 0 10px;
  margin-right: 6px;
}

.error { color: #dc2626; font-size: 12px; margin: 8px 0; }
.hint { color: #6b7280; font-size: 13px; }
.muted { color: #9ca3af; font-size: 12px; }

.table-wrap { overflow-x: auto; margin-top: 12px; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th, .table td { padding: 10px; border-bottom: 1px solid #e5e7eb; text-align: left; }
.table th { color: #6b7280; font-weight: 500; }
.table .addr { max-width: 180px; font-size: 12px; }
.table .ops { white-space: nowrap; }

.status {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
}
.status.PENDING_ACCEPT { background: #fef3c7; color: #92400e; }
.status.IN_DELIVERY { background: #dbeafe; color: #1e40af; }
.status.DELIVERED { background: #d1fae5; color: #065f46; }
.status.EXCEPTION { background: #fee2e2; color: #991b1b; }

.side { display: flex; flex-direction: column; gap: 16px; }
.create { display: flex; flex-direction: column; gap: 8px; margin-bottom: 10px; }
.create input { height: 36px; padding: 0 10px; border: 1px solid #d1d5db; border-radius: 8px; }
.list { list-style: none; padding: 0; margin: 0; font-size: 13px; }
.list li { padding: 6px 0; border-bottom: 1px solid #f3f4f6; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  min-width: 320px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15);
}
.modal-title { font-weight: 600; margin-bottom: 16px; color: #111827; }
.form-row { margin-bottom: 12px; }
.form-row label { display: block; font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.form-row input, .form-row select { width: 100%; height: 36px; padding: 0 10px; border: 1px solid #d1d5db; border-radius: 8px; box-sizing: border-box; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 16px; }

@media (max-width: 900px) {
  .wrap { grid-template-columns: 1fr; }
}
</style>

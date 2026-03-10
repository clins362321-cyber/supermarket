<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const STATUS_MAP = { PENDING: '审批中', APPROVED: '已通过', REJECTED: '已驳回' }
const applies = ref([])      // 我的申请
const allApplies = ref([])   // 全部采购订单
const pendingApplies = ref([])
const suppliers = ref([])
const products = ref([])
const loading = ref(false)
const error = ref('')
const activeTab = ref(route.query.tab === 'pending' ? 'pending' : route.query.tab === 'orders' ? 'orders' : route.query.tab === 'suppliers' ? 'suppliers' : 'my')

const username = () => localStorage.getItem('username') || ''
const canApprove = computed(() => {
  const r = (localStorage.getItem('adminRole') || '').toUpperCase()
  return r === 'ADMIN' || r === 'APPROVER'
})

const loadApplies = async () => {
  try {
    const resp = await fetch(`/api/procurement/applies?username=${encodeURIComponent(username())}`)
    applies.value = await resp.json()
  } catch (e) {
    error.value = '加载失败'
  }
}

const loadPendingApplies = async () => {
  try {
    const resp = await fetch('/api/procurement/applies/pending')
    pendingApplies.value = await resp.json()
  } catch (e) {
    error.value = '加载待审批失败'
  }
}

const loadAllApplies = async () => {
  try {
    const resp = await fetch('/api/procurement/applies')
    allApplies.value = await resp.json()
  } catch (e) {
    error.value = '加载失败'
  }
}

const loadSuppliers = async () => {
  try {
    const resp = await fetch('/api/procurement/suppliers')
    suppliers.value = await resp.json()
  } catch (e) {
    error.value = '加载供应商失败'
  }
}

const loadProducts = async () => {
  try {
    const resp = await fetch('/api/products')
    products.value = await resp.json()
  } catch (e) {
    products.value = []
  }
}

const loadAll = async () => {
  loading.value = true
  error.value = ''
  await Promise.all([loadApplies(), loadPendingApplies(), loadSuppliers(), loadProducts(), loadAllApplies()])
  loading.value = false
}

// 新建采购弹窗
const showNewApply = ref(false)
const newApplyForm = ref({
  title: '采购申请',
  supplierId: '',
  items: [{ productId: '', productName: '', qty: 1, price: '' }],
})

const addApplyItem = () => {
  newApplyForm.value.items.push({ productId: '', productName: '', qty: 1, price: '' })
}

const removeApplyItem = (idx) => {
  newApplyForm.value.items.splice(idx, 1)
}

const onProductSelect = (idx, pid) => {
  const p = products.value.find(x => x.id === Number(pid))
  if (p) {
    newApplyForm.value.items[idx].productName = p.name
    newApplyForm.value.items[idx].price = p.price
  }
}

const submitNewApply = async () => {
  const items = newApplyForm.value.items
    .filter(it => it.productId && it.qty > 0)
    .map(it => ({
      productId: Number(it.productId),
      productName: it.productName || '',
      qty: Number(it.qty),
      price: it.price ? Number(it.price) : null,
    }))
  if (items.length === 0) {
    error.value = '请至少添加一条采购明细'
    return
  }
  try {
    const resp = await fetch('/api/procurement/applies', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: newApplyForm.value.title,
        supplierId: newApplyForm.value.supplierId ? Number(newApplyForm.value.supplierId) : null,
        username: username(),
        items,
      }),
    })
    if (!resp.ok) { error.value = '提交失败'; return }
    showNewApply.value = false
    newApplyForm.value = { title: '采购申请', supplierId: '', items: [{ productId: '', productName: '', qty: 1, price: '' }] }
    await loadAll()
  } catch (e) {
    error.value = '提交失败'
  }
}

// 审批
const approveApply = async (id) => {
  try {
    const resp = await fetch(`/api/procurement/applies/${id}/approve?approver=${encodeURIComponent(username())}`, { method: 'PUT' })
    if (!resp.ok) { error.value = '审批失败'; return }
    await loadAll()
  } catch (e) {
    error.value = '审批失败'
  }
}

const rejectApply = async (id) => {
  try {
    const resp = await fetch(`/api/procurement/applies/${id}/reject?approver=${encodeURIComponent(username())}`, { method: 'PUT' })
    if (!resp.ok) { error.value = '驳回失败'; return }
    await loadAll()
  } catch (e) {
    error.value = '驳回失败'
  }
}

// 供应商 CRUD
const showSupplierModal = ref(false)
const supplierForm = ref({ id: null, name: '', contact: '', phone: '' })

const openAddSupplier = () => {
  supplierForm.value = { id: null, name: '', contact: '', phone: '' }
  showSupplierModal.value = true
}

const openEditSupplier = (s) => {
  supplierForm.value = { id: s.id, name: s.name, contact: s.contact || '', phone: s.phone || '' }
  showSupplierModal.value = true
}

const saveSupplier = async () => {
  if (!supplierForm.value.name?.trim()) { error.value = '请输入供应商名称'; return }
  try {
    if (supplierForm.value.id) {
      const resp = await fetch(`/api/procurement/suppliers/${supplierForm.value.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(supplierForm.value),
      })
      if (!resp.ok) { error.value = '更新失败'; return }
    } else {
      const resp = await fetch('/api/procurement/suppliers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(supplierForm.value),
      })
      if (!resp.ok) { error.value = '新增失败'; return }
    }
    showSupplierModal.value = false
    await loadSuppliers()
  } catch (e) {
    error.value = '保存失败'
  }
}

const getSupplierName = (id) => suppliers.value.find(s => s.id === id)?.name || '-'

watch(() => route.query.tab, (tab) => {
  if (tab === 'pending' || tab === 'orders' || tab === 'suppliers') activeTab.value = tab
  else if (tab === 'my') activeTab.value = 'my'
})

onMounted(loadAll)
</script>

<template>
  <div class="admin-page">
    <div class="page-title">采购管理</div>
    <div class="admin-layout-row">
      <div class="admin-sidebar">
        <div class="admin-card-title">菜单</div>
        <div class="admin-sidebar-item" :class="{ active: activeTab === 'my' }" @click="activeTab = 'my'; router.replace({ query: { tab: 'my' } })">我的申请</div>
        <div class="admin-sidebar-item" :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'; router.replace({ query: { tab: 'pending' } })">待我审批</div>
        <div class="admin-sidebar-item" :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'; router.replace({ query: { tab: 'orders' } })">采购订单</div>
        <div class="admin-sidebar-item" :class="{ active: activeTab === 'suppliers' }" @click="activeTab = 'suppliers'; router.replace({ query: { tab: 'suppliers' } })">供应商档案</div>
      </div>
      <div class="admin-main">
        <!-- 我的申请 -->
        <div v-show="activeTab === 'my'" class="admin-card">
          <div class="admin-card-title">我的采购申请</div>
          <div class="admin-toolbar">
            <button class="admin-btn admin-btn-primary" @click="showNewApply = true">新建申请</button>
            <button class="admin-btn admin-btn-secondary" @click="loadAll" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
          </div>
          <p v-if="error" class="admin-error">{{ error }}</p>
          <div v-if="applies.length === 0" class="admin-hint">暂无申请</div>
          <table v-else class="admin-table">
            <thead>
              <tr>
                <th>申请单号</th>
                <th>标题</th>
                <th>供应商</th>
                <th>状态</th>
                <th>申请时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in applies" :key="a.id">
                <td>{{ a.applyNo }}</td>
                <td>{{ a.title }}</td>
                <td>{{ getSupplierName(a.supplierId) }}</td>
                <td><span :class="['admin-status', 'admin-status-' + (a.status === 'APPROVED' ? 'approved' : a.status === 'REJECTED' ? 'rejected' : 'pending')]">{{ STATUS_MAP[a.status] || a.status }}</span></td>
                <td>{{ a.createdAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 待我审批 -->
        <div v-show="activeTab === 'pending'" class="admin-card">
          <div class="admin-card-title">待我审批</div>
          <div class="admin-toolbar">
            <button class="admin-btn admin-btn-secondary" @click="loadAll" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
          </div>
          <p v-if="!canApprove" class="admin-hint">您无审批权限（需 APPROVER 或 ADMIN 角色）</p>
          <p v-else-if="error" class="admin-error">{{ error }}</p>
          <div v-else-if="pendingApplies.length === 0" class="admin-hint">暂无待审批申请</div>
          <table v-else class="admin-table">
            <thead>
              <tr>
                <th>申请单号</th>
                <th>标题</th>
                <th>供应商</th>
                <th>申请人</th>
                <th>申请时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in pendingApplies" :key="a.id">
                <td>{{ a.applyNo }}</td>
                <td>{{ a.title }}</td>
                <td>{{ getSupplierName(a.supplierId) }}</td>
                <td>{{ a.username || '-' }}</td>
                <td>{{ a.createdAt }}</td>
                <td>
                  <button class="admin-btn admin-btn-success admin-btn-small" @click="approveApply(a.id)">通过</button>
                  <button class="admin-btn admin-btn-danger admin-btn-small" @click="rejectApply(a.id)">驳回</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 采购订单（全部申请） -->
        <div v-show="activeTab === 'orders'" class="admin-card">
          <div class="admin-card-title">采购订单</div>
          <div class="admin-toolbar">
            <button class="admin-btn admin-btn-secondary" @click="loadAll" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
          </div>
          <p v-if="error" class="admin-error">{{ error }}</p>
          <div v-if="allApplies.length === 0" class="admin-hint">暂无采购订单</div>
          <table v-else class="admin-table">
            <thead>
              <tr>
                <th>申请单号</th>
                <th>标题</th>
                <th>供应商</th>
                <th>申请人</th>
                <th>状态</th>
                <th>申请时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in allApplies" :key="a.id">
                <td>{{ a.applyNo }}</td>
                <td>{{ a.title }}</td>
                <td>{{ getSupplierName(a.supplierId) }}</td>
                <td>{{ a.username || '-' }}</td>
                <td><span :class="['admin-status', 'admin-status-' + (a.status === 'APPROVED' ? 'approved' : a.status === 'REJECTED' ? 'rejected' : 'pending')]">{{ STATUS_MAP[a.status] || a.status }}</span></td>
                <td>{{ a.createdAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 供应商档案 -->
        <div v-show="activeTab === 'suppliers'" class="admin-card">
          <div class="admin-card-title">供应商档案</div>
          <div class="admin-toolbar">
            <button class="admin-btn admin-btn-primary" @click="openAddSupplier">新增供应商</button>
            <button class="admin-btn admin-btn-secondary" @click="loadSuppliers" :disabled="loading">刷新</button>
          </div>
          <p v-if="error" class="admin-error">{{ error }}</p>
          <div v-if="suppliers.length === 0" class="admin-hint">暂无供应商</div>
          <table v-else class="admin-table">
            <thead>
              <tr><th>名称</th><th>联系人</th><th>电话</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="s in suppliers" :key="s.id">
                <td>{{ s.name }}</td>
                <td>{{ s.contact || '-' }}</td>
                <td>{{ s.phone || '-' }}</td>
                <td><button class="admin-btn admin-btn-secondary admin-btn-small" @click="openEditSupplier(s)">编辑</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 新建采购弹窗 -->
  <div v-if="showNewApply" class="modal-overlay" @click.self="showNewApply = false">
    <div class="modal">
      <div class="modal-title">新建采购申请</div>
      <div class="form-row">
        <label>标题</label>
        <input v-model="newApplyForm.title" placeholder="采购申请标题" />
      </div>
      <div class="form-row">
        <label>供应商</label>
        <select v-model="newApplyForm.supplierId">
          <option value="">请选择</option>
          <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>
      <div class="form-section">
        <label>采购明细</label>
        <div v-for="(it, idx) in newApplyForm.items" :key="idx" class="item-row">
          <select v-model="it.productId" @change="onProductSelect(idx, it.productId)">
            <option value="">选择商品</option>
            <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
          <input v-model.number="it.qty" type="number" placeholder="数量" style="width:80px" />
          <input v-model="it.price" type="number" placeholder="单价" style="width:100px" />
          <button type="button" class="admin-btn admin-btn-danger admin-btn-small" @click="removeApplyItem(idx)">删除</button>
        </div>
        <button type="button" class="admin-btn admin-btn-secondary admin-btn-small" @click="addApplyItem">+ 添加明细</button>
      </div>
      <div class="modal-actions">
        <button class="admin-btn admin-btn-secondary" @click="showNewApply = false">取消</button>
        <button class="admin-btn admin-btn-primary" @click="submitNewApply">提交</button>
      </div>
    </div>
  </div>

  <!-- 供应商弹窗 -->
  <div v-if="showSupplierModal" class="modal-overlay" @click.self="showSupplierModal = false">
    <div class="modal">
      <div class="modal-title">{{ supplierForm.id ? '编辑供应商' : '新增供应商' }}</div>
      <div class="form-row">
        <label>名称</label>
        <input v-model="supplierForm.name" placeholder="供应商名称" />
      </div>
      <div class="form-row">
        <label>联系人</label>
        <input v-model="supplierForm.contact" placeholder="联系人" />
      </div>
      <div class="form-row">
        <label>电话</label>
        <input v-model="supplierForm.phone" placeholder="电话" />
      </div>
      <div class="modal-actions">
        <button class="admin-btn admin-btn-secondary" @click="showSupplierModal = false">取消</button>
        <button class="admin-btn admin-btn-primary" @click="saveSupplier">保存</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; color: #111827; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 14px; padding: 20px; min-width: 400px; max-width: 90vw; box-shadow: 0 10px 40px rgba(0,0,0,0.15); }
.modal-title { font-weight: 600; margin-bottom: 16px; color: #111827; }
.form-row { margin-bottom: 12px; }
.form-row label { display: block; font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.form-row input, .form-row select { width: 100%; height: 36px; padding: 0 10px; border: 1px solid #d1d5db; border-radius: 8px; box-sizing: border-box; }
.form-section { margin-bottom: 16px; }
.form-section label { display: block; font-size: 12px; color: #6b7280; margin-bottom: 8px; }
.item-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.item-row select { flex: 1; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 16px; }
</style>

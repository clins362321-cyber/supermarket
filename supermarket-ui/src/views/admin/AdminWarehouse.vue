<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const inventory = ref([])
const alerts = ref([])
const suppliers = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')

const username = () => localStorage.getItem('username') || ''

const loadInventory = async () => {
  try {
    const resp = await fetch('/api/warehouse/inventory')
    inventory.value = await resp.json()
  } catch (e) {
    error.value = '加载库存失败'
  }
}

const loadAlerts = async () => {
  try {
    const resp = await fetch('/api/warehouse/alerts')
    alerts.value = await resp.json()
  } catch (e) {
    error.value = '加载预警失败'
  }
}

const loadSuppliers = async () => {
  try {
    const resp = await fetch('/api/procurement/suppliers')
    suppliers.value = await resp.json()
  } catch (e) {
    suppliers.value = []
  }
}

const loadAll = async () => {
  loading.value = true
  error.value = ''
  await Promise.all([loadInventory(), loadAlerts(), loadSuppliers()])
  loading.value = false
}

const getSafeStock = (p) => p.safeStock != null ? p.safeStock : 50
const getGap = (p) => Math.max(0, getSafeStock(p) - (p.stock ?? 0))

// 发起采购弹窗
const showInitiateModal = ref(false)
const initiateProduct = ref(null)
const initiateForm = ref({ supplierId: '', qty: 50 })

const openInitiate = (p) => {
  initiateProduct.value = p
  initiateForm.value = { supplierId: suppliers.value[0]?.id || '', qty: getGap(p) || 50 }
  showInitiateModal.value = true
}

const submitInitiate = async () => {
  if (!initiateProduct.value || !initiateForm.value.qty || initiateForm.value.qty <= 0) {
    error.value = '请输入采购数量'
    return
  }
  error.value = ''
  success.value = ''
  try {
    const resp = await fetch('/api/warehouse/initiate-procurement', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        productId: initiateProduct.value.id,
        qty: Number(initiateForm.value.qty),
        supplierId: initiateForm.value.supplierId ? Number(initiateForm.value.supplierId) : null,
        username: username(),
      }),
    })
    const data = await resp.json()
    if (!resp.ok) {
      error.value = typeof data === 'string' ? data : '发起采购失败'
      return
    }
    showInitiateModal.value = false
    initiateProduct.value = null
    const applyNo = data?.applyNo || ''
    success.value = applyNo ? `采购申请 ${applyNo} 已提交，请在采购管理-待我审批中处理` : '采购申请已提交，请在采购管理-待我审批中处理'
    await loadAll()
  } catch (e) {
    error.value = '发起采购失败'
  }
}

const goToApproval = () => {
  router.push('/admin/procurement?tab=pending')
}

// 修改安全库存
const editingSafeStock = ref(null)
const safeStockInput = ref('')

const openEditSafeStock = (p) => {
  editingSafeStock.value = p.id
  safeStockInput.value = String(getSafeStock(p))
}

const cancelEditSafeStock = () => {
  editingSafeStock.value = null
  safeStockInput.value = ''
}

const saveSafeStock = async () => {
  const id = editingSafeStock.value
  if (!id) return
  const val = parseInt(safeStockInput.value, 10)
  if (isNaN(val) || val < 0) {
    error.value = '请输入有效的安全库存（≥0）'
    return
  }
  error.value = ''
  try {
    const resp = await fetch(`/api/warehouse/products/${id}/safe-stock`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ safeStock: val }),
    })
    const text = await resp.text()
    let errMsg = '修改失败'
    if (text) {
      try {
        const parsed = JSON.parse(text)
        if (typeof parsed === 'string' && parsed.length < 200) errMsg = parsed
      } catch {
        if (typeof text === 'string' && text.length < 200 && !text.startsWith('<')) errMsg = text
      }
    }
    if (!resp.ok) {
      error.value = errMsg
      return
    }
    editingSafeStock.value = null
    safeStockInput.value = ''
    await loadAll()
  } catch (e) {
    error.value = '修改失败'
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="admin-page">
    <div class="page-title">仓储管理</div>
    <div class="admin-layout-row">
      <div class="admin-main" style="flex:1">
        <div class="admin-card" id="alert">
          <div class="admin-card-title">库存预警（{{ alerts.length }} 条）</div>
          <div class="admin-toolbar">
            <button class="admin-btn admin-btn-secondary" @click="loadAll" :disabled="loading">
              {{ loading ? '加载中...' : '刷新' }}
            </button>
          </div>
          <p v-if="error" class="admin-error">{{ error }}</p>
          <p v-if="success" class="admin-success">
            {{ success }}
            <button class="admin-btn admin-btn-primary admin-btn-small" style="margin-left:12px" @click="goToApproval">去审批</button>
          </p>
          <div v-if="alerts.length === 0" class="admin-hint">暂无库存预警</div>
          <table v-else class="admin-table">
            <thead>
              <tr>
                <th>商品</th>
                <th>当前库存</th>
                <th>安全库存</th>
                <th>缺口</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in alerts" :key="p.id">
                <td>{{ p.name }}</td>
                <td>{{ p.stock ?? 0 }}</td>
                <td>
                  <template v-if="editingSafeStock === p.id">
                    <input v-model="safeStockInput" type="number" min="0" class="safe-stock-input" @keyup.enter="saveSafeStock" />
                  </template>
                  <template v-else>
                    <span class="editable" @click="openEditSafeStock(p)">{{ getSafeStock(p) }}</span>
                  </template>
                </td>
                <td>{{ getGap(p) }}</td>
                <td>
                  <template v-if="editingSafeStock === p.id">
                    <button class="admin-btn admin-btn-primary admin-btn-small" @click="saveSafeStock">保存</button>
                    <button class="admin-btn admin-btn-secondary admin-btn-small" @click="cancelEditSafeStock">取消</button>
                  </template>
                  <template v-else>
                    <button class="admin-btn admin-btn-secondary admin-btn-small" @click="openEditSafeStock(p)">改安全库存</button>
                    <button class="admin-btn admin-btn-primary admin-btn-small" @click="openInitiate(p)">发起采购</button>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="admin-card">
          <div class="admin-card-title">库存查询</div>
          <div v-if="inventory.length === 0" class="admin-hint">暂无库存</div>
          <table v-else class="admin-table">
            <thead>
              <tr>
                <th>商品</th>
                <th>当前库存</th>
                <th>安全库存</th>
                <th>价格</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in inventory" :key="p.id">
                <td>{{ p.name }}</td>
                <td>{{ p.stock ?? 0 }}</td>
                <td>
                  <template v-if="editingSafeStock === p.id">
                    <input v-model="safeStockInput" type="number" min="0" class="safe-stock-input" @keyup.enter="saveSafeStock" />
                  </template>
                  <template v-else>
                    <span class="editable" @click="openEditSafeStock(p)">{{ getSafeStock(p) }}</span>
                  </template>
                </td>
                <td>¥{{ p.price }}</td>
                <td>
                  <template v-if="editingSafeStock === p.id">
                    <button class="admin-btn admin-btn-primary admin-btn-small" @click="saveSafeStock">保存</button>
                    <button class="admin-btn admin-btn-secondary admin-btn-small" @click="cancelEditSafeStock">取消</button>
                  </template>
                  <button v-else class="admin-btn admin-btn-secondary admin-btn-small" @click="openEditSafeStock(p)">修改</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 发起采购弹窗 -->
  <div v-if="showInitiateModal && initiateProduct" class="modal-overlay" @click.self="showInitiateModal = false">
    <div class="modal">
      <div class="modal-title">发起采购 - {{ initiateProduct.name }}</div>
      <div class="form-row">
        <label>供应商</label>
        <select v-model="initiateForm.supplierId">
          <option value="">请选择</option>
          <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>
      <div class="form-row">
        <label>采购数量</label>
        <input v-model.number="initiateForm.qty" type="number" placeholder="数量" min="1" />
      </div>
      <div class="modal-actions">
        <button class="admin-btn admin-btn-secondary" @click="showInitiateModal = false">取消</button>
        <button class="admin-btn admin-btn-primary" @click="submitInitiate">提交采购申请</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; color: #111827; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 14px; padding: 20px; min-width: 360px; box-shadow: 0 10px 40px rgba(0,0,0,0.15); }
.modal-title { font-weight: 600; margin-bottom: 16px; color: #111827; }
.form-row { margin-bottom: 12px; }
.form-row label { display: block; font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.form-row input, .form-row select { width: 100%; height: 36px; padding: 0 10px; border: 1px solid #d1d5db; border-radius: 8px; box-sizing: border-box; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 16px; }
.admin-success { color: #16a34a; font-size: 14px; margin: 8px 0; }
.safe-stock-input { width: 72px; padding: 4px 8px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; }
.editable { cursor: pointer; text-decoration: underline; text-underline-offset: 2px; }
.editable:hover { color: #2563eb; }
</style>

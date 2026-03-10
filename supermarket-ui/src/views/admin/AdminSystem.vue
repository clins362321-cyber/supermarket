<script setup>
import { onMounted, ref } from 'vue'

const admins = ref([])
const users = ref([])
const roles = ref([])
const auditLogs = ref([])
const loading = ref(false)
const error = ref('')
const activeTab = ref('users')

const adminForm = ref({ username: '', password: '', role: 'ADMIN' })
const userForm = ref({ username: '', password: '' })
const editingAdmin = ref(null)

const ROLE_MAP = { ADMIN: '超级管理员', APPROVER: '采购审批员', WAREHOUSE: '仓储管理员' }
const getRoleName = (role) => ROLE_MAP[role] || role || '超级管理员'

const loadAdmins = async () => {
  try {
    const resp = await fetch('/api/admins')
    admins.value = await resp.json()
  } catch (e) {
    error.value = '加载管理员失败'
  }
}

const loadUsers = async () => {
  try {
    const resp = await fetch('/api/users')
    users.value = await resp.json()
  } catch (e) {
    error.value = '加载用户失败'
  }
}

const loadRoles = async () => {
  try {
    const resp = await fetch('/api/system/roles')
    roles.value = await resp.json()
  } catch (e) {
    roles.value = [{ code: 'ADMIN', name: '超级管理员', desc: '全部权限' }, { code: 'APPROVER', name: '采购审批员', desc: '审批采购申请' }, { code: 'WAREHOUSE', name: '仓储管理员', desc: '仓储、发起采购' }]
  }
}

const loadAuditLogs = async () => {
  try {
    const resp = await fetch('/api/system/audit-logs?limit=100')
    auditLogs.value = await resp.json()
  } catch (e) {
    error.value = '加载审计日志失败'
  }
}

const loadAll = async () => {
  loading.value = true
  error.value = ''
  await Promise.all([loadAdmins(), loadUsers(), loadRoles()])
  loading.value = false
}

const createAdmin = async () => {
  if (!adminForm.value.username || !adminForm.value.password) {
    error.value = '请输入管理员账号和密码'
    return
  }
  try {
    const resp = await fetch('/api/admins', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: adminForm.value.username, password: adminForm.value.password, role: adminForm.value.role }),
    })
    if (!resp.ok) { error.value = '新增失败'; return }
    adminForm.value = { username: '', password: '', role: 'ADMIN' }
    await loadAdmins()
  } catch (e) {
    error.value = '新增失败'
  }
}

const createUser = async () => {
  if (!userForm.value.username || !userForm.value.password) {
    error.value = '请输入用户账号和密码'
    return
  }
  try {
    const resp = await fetch('/api/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: userForm.value.username, password: userForm.value.password }),
    })
    if (!resp.ok) { error.value = '新增失败'; return }
    userForm.value = { username: '', password: '' }
    await loadUsers()
  } catch (e) {
    error.value = '新增失败'
  }
}

const openEditAdmin = (a) => {
  editingAdmin.value = { id: a.id, username: a.username, role: a.role || 'ADMIN' }
}

const saveAdminRole = async () => {
  if (!editingAdmin.value) return
  try {
    const resp = await fetch(`/api/admins/${editingAdmin.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: editingAdmin.value.username, role: editingAdmin.value.role }),
    })
    if (!resp.ok) { error.value = '更新失败'; return }
    editingAdmin.value = null
    await loadAdmins()
  } catch (e) {
    error.value = '更新失败'
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="admin-page">
    <div class="page-title">系统管理</div>
    <div class="admin-layout-row">
      <div class="admin-sidebar">
        <div class="admin-card-title">菜单</div>
        <div class="admin-sidebar-item" :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">用户管理</div>
        <div class="admin-sidebar-item" :class="{ active: activeTab === 'roles' }" @click="activeTab = 'roles'">角色权限</div>
        <div class="admin-sidebar-item" :class="{ active: activeTab === 'audit' }" @click="activeTab = 'audit'; loadAuditLogs()">操作审计</div>
      </div>
      <div class="admin-main">
        <div v-show="activeTab === 'users'" class="admin-card" id="users">
          <div class="admin-card-title">用户管理</div>
          <div class="admin-toolbar">
            <button class="admin-btn admin-btn-primary" @click="loadAll" :disabled="loading">刷新</button>
          </div>
          <p v-if="error" class="admin-error">{{ error }}</p>
          <div style="margin-bottom:20px">
            <div class="admin-card-title">管理员</div>
            <div style="display:flex;gap:8px;margin-bottom:8px;flex-wrap:wrap;align-items:center">
              <input v-model="adminForm.username" class="admin-input" placeholder="账号" />
              <input v-model="adminForm.password" class="admin-input" type="password" placeholder="密码" />
              <select v-model="adminForm.role" class="admin-input" style="width:140px">
                <option v-for="r in roles" :key="r.code" :value="r.code">{{ r.name }}</option>
              </select>
              <button class="admin-btn admin-btn-primary" @click="createAdmin">新增</button>
            </div>
          </div>
          <div style="margin-bottom:20px">
            <div class="admin-card-title">普通用户</div>
            <div style="display:flex;gap:8px;margin-bottom:8px;flex-wrap:wrap">
              <input v-model="userForm.username" class="admin-input" placeholder="账号" />
              <input v-model="userForm.password" class="admin-input" type="password" placeholder="密码" />
              <button class="admin-btn admin-btn-primary" @click="createUser">新增</button>
            </div>
          </div>
          <table class="admin-table">
            <thead>
              <tr><th>用户名</th><th>角色</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="a in admins" :key="'a'+a.id">
                <td>{{ a.username }}</td>
                <td>{{ getRoleName(a.role) }}</td>
                <td>
                  <button class="admin-btn admin-btn-secondary admin-btn-small" @click="openEditAdmin(a)">改角色</button>
                </td>
              </tr>
              <tr v-for="u in users" :key="'u'+u.id">
                <td>{{ u.username }}</td>
                <td>普通用户</td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-show="activeTab === 'roles'" class="admin-card">
          <div class="admin-card-title">角色权限配置</div>
          <p class="admin-hint" style="margin-bottom:12px">系统预置角色及权限说明：</p>
          <table class="admin-table">
            <thead>
              <tr><th>角色</th><th>说明</th><th>权限</th></tr>
            </thead>
            <tbody>
              <tr v-for="r in roles" :key="r.code">
                <td>{{ r.name }}</td>
                <td>{{ r.desc }}</td>
                <td>{{ r.code === 'ADMIN' ? '全部' : r.code === 'APPROVER' ? '审批采购申请' : '仓储、发起采购' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-show="activeTab === 'audit'" class="admin-card">
          <div class="admin-card-title">操作审计</div>
          <div class="admin-toolbar">
            <button class="admin-btn admin-btn-secondary" @click="loadAuditLogs">刷新</button>
          </div>
          <p v-if="error" class="admin-error">{{ error }}</p>
          <div v-if="auditLogs.length === 0" class="admin-hint">暂无审计记录</div>
          <table v-else class="admin-table">
            <thead>
              <tr>
                <th>时间</th>
                <th>用户</th>
                <th>角色</th>
                <th>模块</th>
                <th>操作</th>
                <th>详情</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in auditLogs" :key="log.id">
                <td>{{ log.createdAt }}</td>
                <td>{{ log.username || '-' }}</td>
                <td>{{ log.role || '-' }}</td>
                <td>{{ log.module || '-' }}</td>
                <td>{{ log.action || '-' }}</td>
                <td>{{ log.detail || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 编辑管理员角色弹窗 -->
  <div v-if="editingAdmin" class="modal-overlay" @click.self="editingAdmin = null">
    <div class="modal">
      <div class="modal-title">修改角色 - {{ editingAdmin.username }}</div>
      <div class="form-row">
        <label>角色</label>
        <select v-model="editingAdmin.role">
          <option v-for="r in roles" :key="r.code" :value="r.code">{{ r.name }}</option>
        </select>
      </div>
      <div class="modal-actions">
        <button class="admin-btn admin-btn-secondary" @click="editingAdmin = null">取消</button>
        <button class="admin-btn admin-btn-primary" @click="saveAdminRole">保存</button>
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
.form-row select { width: 100%; height: 36px; padding: 0 10px; border: 1px solid #d1d5db; border-radius: 8px; box-sizing: border-box; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 16px; }
</style>

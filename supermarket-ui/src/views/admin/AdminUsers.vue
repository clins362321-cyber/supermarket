<script setup>
import { onMounted, reactive, ref } from 'vue'

const loading = ref(false)
const error = ref('')

const admins = ref([])
const users = ref([])

const adminForm = reactive({
  username: '',
  password: '',
})

const userForm = reactive({
  username: '',
  password: '',
})

const loadAdmins = async () => {
  try {
    const resp = await fetch('/api/admins')
    const data = await resp.json()
    admins.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = '加载管理员失败'
  }
}

const loadUsers = async () => {
  try {
    const resp = await fetch('/api/users')
    const data = await resp.json()
    users.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = '加载用户失败'
  }
}

const loadAll = async () => {
  loading.value = true
  error.value = ''
  await Promise.all([loadAdmins(), loadUsers()])
  loading.value = false
}

const createAdmin = async () => {
  error.value = ''
  if (!adminForm.username || !adminForm.password) {
    error.value = '请输入管理员账号和密码'
    return
  }
  try {
    const resp = await fetch('/api/admins', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: adminForm.username, password: adminForm.password }),
    })
    if (!resp.ok) {
      error.value = '新增管理员失败'
      return
    }
    adminForm.username = ''
    adminForm.password = ''
    await loadAdmins()
  } catch (e) {
    error.value = '新增管理员失败'
  }
}

const createUser = async () => {
  error.value = ''
  if (!userForm.username || !userForm.password) {
    error.value = '请输入用户账号和密码'
    return
  }
  try {
    const resp = await fetch('/api/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: userForm.username, password: userForm.password }),
    })
    if (!resp.ok) {
      error.value = '新增用户失败'
      return
    }
    userForm.username = ''
    userForm.password = ''
    await loadUsers()
  } catch (e) {
    error.value = '新增用户失败'
  }
}

const removeAdmin = async (id) => {
  if (!confirm('确认删除该管理员吗？')) return
  try {
    const resp = await fetch(`/api/admins/${id}`, { method: 'DELETE' })
    if (!resp.ok) {
      error.value = '删除管理员失败'
      return
    }
    await loadAdmins()
  } catch (e) {
    error.value = '删除管理员失败'
  }
}

const removeUser = async (id) => {
  if (!confirm('确认删除该用户吗？')) return
  try {
    const resp = await fetch(`/api/users/${id}`, { method: 'DELETE' })
    if (!resp.ok) {
      error.value = '删除用户失败'
      return
    }
    await loadUsers()
  } catch (e) {
    error.value = '删除用户失败'
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="wrap">
    <div class="panel">
      <div class="panel-title">管理员管理</div>

      <div class="create">
        <input v-model="adminForm.username" placeholder="管理员账号" />
        <input v-model="adminForm.password" placeholder="密码" />
        <button class="btn primary" type="button" @click="createAdmin">新增管理员</button>
        <button class="btn" type="button" @click="loadAdmins" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新' }}
        </button>
      </div>

      <div v-if="admins.length === 0" class="hint small">暂无管理员</div>
      <div v-else class="table">
        <div class="tr head">
          <div>ID</div>
          <div>账号</div>
          <div>操作</div>
        </div>
        <div v-for="a in admins" :key="a.id" class="tr">
          <div>{{ a.id }}</div>
          <div>{{ a.username }}</div>
          <div class="ops">
            <button class="btn small danger" type="button" @click="removeAdmin(a.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-title">用户管理</div>

      <div class="create">
        <input v-model="userForm.username" placeholder="用户账号" />
        <input v-model="userForm.password" placeholder="密码" />
        <button class="btn primary" type="button" @click="createUser">新增用户</button>
        <button class="btn" type="button" @click="loadUsers" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新' }}
        </button>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div v-if="users.length === 0" class="hint small">暂无用户</div>
      <div v-else class="table">
        <div class="tr head">
          <div>ID</div>
          <div>账号</div>
          <div>操作</div>
        </div>
        <div v-for="u in users" :key="u.id" class="tr">
          <div>{{ u.id }}</div>
          <div>{{ u.username }}</div>
          <div class="ops">
            <button class="btn small danger" type="button" @click="removeUser(u.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.panel {
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.92);
  padding: 14px;
}

.panel-title {
  font-weight: 600;
  color: #e5e7eb;
  margin-bottom: 12px;
}

.create {
  display: grid;
  grid-template-columns: 1fr 1fr 120px 90px;
  gap: 10px;
  align-items: center;
}

input {
  width: 100%;
  height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(2, 6, 23, 0.55);
  color: #e5e7eb;
  padding: 0 10px;
  box-sizing: border-box;
  outline: none;
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

.btn.danger {
  border-color: rgba(248, 113, 113, 0.4);
  background: rgba(248, 113, 113, 0.14);
}

.btn.small {
  height: 30px;
  padding: 0 10px;
  border-radius: 9px;
}

.error {
  margin: 10px 0 0;
  color: #fca5a5;
  font-size: 12px;
}

.hint {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 13px;
}

.hint.small {
  font-size: 12px;
}

.table {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tr {
  display: grid;
  grid-template-columns: 80px 1fr 120px;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(2, 6, 23, 0.35);
}

.tr.head {
  color: #94a3b8;
  font-size: 12px;
  background: transparent;
  border-color: transparent;
  padding-top: 0;
}

.ops {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .wrap {
    grid-template-columns: 1fr;
  }

  .create {
    grid-template-columns: 1fr;
  }
}
</style>


<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
// 身份：'admin' 管理员，'user' 普通用户
const role = ref('user')

const router = useRouter()

const handleSubmit = async () => {
  error.value = ''

  if (!username.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }

  loading.value = true

  try {
    const resp = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username.value,
        password: password.value,
        role: role.value,
      }),
    })

    const data = await resp.json()
    loading.value = false

    if (!resp.ok || !data.success) {
      error.value = data.message || '登录失败'
      return
    }

    // 简单保存一下 token 和角色，后续可以扩展
    localStorage.setItem('token', data.token || '')
    localStorage.setItem('role', data.role || role.value)
    localStorage.setItem('adminRole', data.role || '')
    localStorage.setItem('username', username.value)

    if (data.role === 'admin') {
      router.push('/admin')
    } else {
      router.push('/user')
    }
  } catch (e) {
    loading.value = false
    error.value = '无法连接服务器，请检查后端是否启动'
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">生鲜超市物流配送系统</h1>
      <p class="login-subtitle">请选择身份并登录系统</p>

      <div class="role-switch">
        <button
          type="button"
          :class="['role-btn', role === 'user' && 'active']"
          @click="role = 'user'"
        >
          普通用户
        </button>
        <button
          type="button"
          :class="['role-btn', role === 'admin' && 'active']"
          @click="role = 'admin'"
        >
          管理员
        </button>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <div class="form-item">
          <label for="username">账号</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="请输入账号"
            autocomplete="username"
          />
        </div>

        <div class="form-item">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>

        <p v-if="error" class="error-msg">
          {{ error }}
        </p>

        <button class="login-btn" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <p class="login-tip">
        <span v-if="role === 'admin'">管理员示例：账号 admin / 密码 123456</span>
        <span v-else>
          用户示例：账号 user / 密码 123456；
          <RouterLink to="/register">没有账号？点击注册</RouterLink>
        </span>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 24px;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 32px 32px 28px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  box-sizing: border-box;
}

.login-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  text-align: center;
}

.login-subtitle {
  margin: 0 0 24px;
  font-size: 13px;
  color: #6b7280;
  text-align: center;
}

.role-switch {
  display: flex;
  gap: 8px;
  padding: 4px;
  margin-bottom: 20px;
  border-radius: 999px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
}

.role-btn {
  flex: 1;
  height: 32px;
  border-radius: 999px;
  border: none;
  font-size: 13px;
  color: #374151;
  background: transparent;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease, box-shadow 0.15s ease;
}

.role-btn.active {
  background: #2563eb;
  color: #ffffff;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 13px;
  color: #374151;
}

.form-item input {
  height: 38px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  padding: 0 10px;
  font-size: 14px;
  color: #111827;
  background-color: #ffffff;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease;
}

.form-item input::placeholder {
  color: #9ca3af;
}

.form-item input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.35);
  background-color: #ffffff;
}

.error-msg {
  margin: 0;
  font-size: 12px;
  color: #dc2626;
}

.login-btn {
  margin-top: 4px;
  height: 38px;
  border-radius: 8px;
  border: none;
  background: #2563eb;
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.login-btn:disabled {
  cursor: default;
  opacity: 0.7;
}

.login-tip {
  margin-top: 16px;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
}

@media (max-width: 480px) {
  .login-card {
    padding: 24px 20px 20px;
  }

  .login-title {
    font-size: 20px;
  }
}
</style>

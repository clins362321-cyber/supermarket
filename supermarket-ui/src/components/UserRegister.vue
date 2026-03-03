<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const router = useRouter()

const handleSubmit = async () => {
  error.value = ''
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  try {
    const resp = await fetch('/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username.value,
        password: password.value,
      }),
    })

    loading.value = false

    if (!resp.ok) {
      error.value = '注册失败，用户名可能已存在'
      return
    }

    alert('注册成功，请使用新账号登录')
    router.push('/login')
  } catch (e) {
    loading.value = false
    error.value = '无法连接服务器，请稍后重试'
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">用户注册</h1>
      <p class="login-subtitle">创建普通用户账号</p>

      <form class="login-form" @submit.prevent="handleSubmit">
        <div class="form-item">
          <label for="username">用户名</label>
          <input id="username" v-model="username" type="text" placeholder="请输入用户名" />
        </div>

        <div class="form-item">
          <label for="password">密码</label>
          <input id="password" v-model="password" type="password" placeholder="请输入密码" />
        </div>

        <p v-if="error" class="error-msg">
          {{ error }}
        </p>

        <button class="login-btn" type="submit" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>

      <p class="login-tip">
        已有账号？
        <a href="/login">返回登录</a>
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
  background: radial-gradient(circle at top left, #2f80ed22, transparent),
    radial-gradient(circle at bottom right, #22c55e22, transparent),
    #0f172a;
  padding: 24px;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 32px 32px 28px;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.96);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-sizing: border-box;
}

.login-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #e5e7eb;
  text-align: center;
}

.login-subtitle {
  margin: 0 0 24px;
  font-size: 13px;
  color: #9ca3af;
  text-align: center;
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
  color: #e5e7eb;
}

.form-item input {
  height: 38px;
  border-radius: 8px;
  border: 1px solid #4b5563;
  padding: 0 10px;
  font-size: 14px;
  color: #e5e7eb;
  background-color: #020617;
  outline: none;
}

.error-msg {
  margin: 0;
  font-size: 12px;
  color: #f97373;
}

.login-btn {
  margin-top: 4px;
  height: 38px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #f9fafb;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: default;
}

.login-tip {
  margin-top: 16px;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
}

.login-tip a {
  color: #22c55e;
}
</style>


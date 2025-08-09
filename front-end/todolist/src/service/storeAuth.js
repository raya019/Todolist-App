import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '../util/api'

export const useAuth = defineStore('auth', () => {
  const isAuth = ref(localStorage.getItem('accessToken') !== null)

  function login() {
    isAuth.value = true
  }

  function logout() {
    isAuth.value = false
  }

  return { login, logout, isAuth }
})

export const useRegister = defineStore('register', () => {
  const data = ref(null)
  const error = ref(null)

  async function handleRegister(requestUserRegister) {
    try {
      const res = await postApi('auth/register', requestUserRegister)

      if (res.status === 200) {
        data.value = res.data.message
      }
    } catch (e) {
      error.value = e.response.data.errors
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return {
    handleRegister,
    data,
    error,
    $reset,
  }
})

export const useLogin = defineStore('login', () => {
  const data = ref(null)
  const error = ref(null)
  const router = useRouter()
  const auth = useAuth()

  async function handleLogin(requestUserLogin) {
    try {
      const res = await postApi('auth/login', requestUserLogin)

      if (res.status === 200) {
        data.value = res.data.data

        localStorage.setItem('accessToken', data.value.accessToken)

        auth.login()

        await router.replace({ name: 'todolist' })
      }
    } catch (e) {
      error.value = e.response.data.errors
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return {
    handleLogin,
    error,
    $reset,
  }
})

export const useRefreshToken = defineStore('refreshToken', () => {
  const data = ref(null)
  const error = ref(null)

  async function refreshToken() {
    try {
      const res = await postApi('auth/refresh')

      if (res.status === 200) {
        data.value = res.data.data
      }
    } catch (e) {
      localStorage.removeItem('accessToken')

      const logout = useAuth()
      logout.logout()

      await router.replace({ name: 'login' })
    }
  }

  return { data, error, refreshToken }
})

export const useLogout = defineStore('logout', () => {
  const router = useRouter()
  const auth = useAuth()
  const error = ref(null)

  async function onLogout() {
    try {
      const res = await postApi('auth/logout')
      if (res.status === 200) {
        auth.logout()
        localStorage.removeItem('accessToken')
        await router.replace({ name: 'login' })
      }
    } catch (e) {
      error.value = e.response
    }
  }

  return { onLogout }
})

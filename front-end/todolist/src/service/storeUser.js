import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getApi, patchApi, postHeaderApi } from '../util/api'
import { useAuth } from './storeAuth'

export const useGetUser = defineStore('getUser', () => {
  const data = ref(null)
  const error = ref(null)

  async function userCurrent() {
    try {
      const res = await getApi(`user/current`)
      if (res.status === 200) {
        data.value = res.data.data
      }
    } catch (e) {
      error.value = e.response
    }
  }

  return { userCurrent, data }
})

export const useUpdateUser = defineStore('updateUser', () => {
  const data = ref(null)
  const error = ref(null)

  async function handleUserUpdate(values) {
    try {
      const res = await patchApi('user/current', values)
      if (res.status === 200) {
        data.value = res.data
      }
    } catch (e) {
      error.value = e.response
    }
  }

  return { handleUserUpdate, data }
})

export const useChangePassword = defineStore('changePassword', () => {
  const data = ref(null)
  const error = ref(null)

  async function handleChangePassword(values) {
    try {
      const res = await postHeaderApi('user/change-password', values)
      if (res.status === 200) {
        data.value = res.data.message
      }
    } catch (e) {
      error.value = e.response
    }
  }

  return { handleChangePassword, data }
})

// export const useLogout = defineStore('logout', () => {
//   const router = useRouter()
//   const auth = useAuth()
//   const error = ref(null)

//   async function onLogout() {
//     try {
//       const res = await postHeaderApi('user/logout')
//       if (res.status === 200) {
//         auth.logout()
//         localStorage.removeItem('accessToken')
//         await router.replace({ name: 'login' })
//       }
//     } catch (e) {
//       error.value = e.response
//     }
//   }

//   return { onLogout }
// })

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getApi, patchApi, postHeaderApi } from '../util/api'

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
      error.value = e.response.data.errors
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return { handleChangePassword, data, error, $reset }
})

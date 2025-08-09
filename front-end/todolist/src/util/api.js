import { useLogout, useRefreshToken } from '@/service/storeAuth'
import axios from 'axios'

const instance = axios.create({
  baseURL: `${import.meta.env.VITE_BACKEND_URL}/api/`,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

instance.interceptors.request.use(
  (config) => {
    const accesstoken = localStorage.getItem('accessToken')
    if (accesstoken) {
      config.headers.Authorization = `Bearer ${accesstoken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

instance.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        const refreshToken = useRefreshToken()

        await refreshToken.refreshToken()

        originalRequest.headers['Authorization'] = `Bearer ${refreshToken.data.accesstoken}`

        localStorage.setItem('accessToken', refreshToken.data.accessToken)

        return instance(originalRequest)
      } catch (e) {
        const logout = useLogout()
        logout.onLogout()

        localStorage.clear('accessToken')

        return Promise.reject(error)
      }
    }
    return Promise.reject(error)
  },
)

export function postApi(url, reqBody) {
  return instance.post(url, reqBody)
}

export function postHeaderApi(url, reqBody) {
  return instance.post(url, reqBody, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
    },
  })
}

export function getApi(url) {
  return instance.get(url, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
    },
  })
}

export function deleteApi(url) {
  return instance({
    method: 'delete',
    url: url,
    headers: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
    },
  })
}

export function putApi(url, reqBody) {
  return instance.put(url, reqBody, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
    },
  })
}

export function patchApi(url, reqBody) {
  return instance({
    method: 'patch',
    data: reqBody,
    url: url,
    headers: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
    },
  })
}

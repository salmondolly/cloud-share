import axios from 'axios'

export const TOKEN_KEY = 'cloudshare_token'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 30000,
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY)) {
      localStorage.removeItem(TOKEN_KEY)
      window.dispatchEvent(new Event('cloudshare:unauthorized'))
    }
    return Promise.reject(error)
  },
)

export function apiErrorMessage(error, fallback = 'Something went wrong') {
  const fieldErrors = error.response?.data?.fieldErrors
  if (fieldErrors && Object.keys(fieldErrors).length > 0) {
    return Object.values(fieldErrors).join('. ')
  }
  return error.response?.data?.message || error.message || fallback
}

export default apiClient

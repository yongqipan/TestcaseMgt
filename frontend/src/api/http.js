import axios from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'tcm_token'
const USER_KEY = 'tcm_user'

const http = axios.create({
  baseURL: '/api',
  timeout: 20000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code !== 'undefined' && body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body ? body.data : body
  },
  (error) => {
    const status = error.response ? error.response.status : 0
    const message = error.response?.data?.message || error.message || '网络异常，请稍后重试'
    const isLoginRequest = (error.config?.url || '').includes('/auth/login')
    if (status === 401 && !isLoginRequest) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
      ElMessage.error('登录已失效，请重新登录')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export { TOKEN_KEY, USER_KEY }
export default http

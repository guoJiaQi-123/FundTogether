/**
 * @file Axios 请求封装模块
 * @description 基于 axios 创建统一的 HTTP 请求实例，配置了基础 URL、超时时间，
 *              并通过请求拦截器自动注入 JWT Token，通过响应拦截器统一处理
 *              业务错误码和网络错误，使用 Element Plus 的 ElMessage 进行错误提示。
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'

/** Axios 请求实例，配置了后端基础地址和超时时间 */
const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端 API 基础地址
  timeout: 5000 // 请求超时时间：5秒
})

/**
 * 请求拦截器
 * @description 在每个请求发送前，从 localStorage 中读取 JWT Token，
 *              若存在则自动添加到请求头 Authorization 字段中，实现用户身份鉴权。
 */
request.interceptors.request.use(
  config => {
    // 从本地存储中获取登录令牌
    const token = localStorage.getItem('token')
    if (token) {
      // 若令牌存在，以 Bearer 方式添加到请求头
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * @description 统一处理响应数据：
 *   - 业务状态码为 200 时，直接返回响应数据体；
 *   - 其他状态码视为业务错误，使用 ElMessage 弹出错误提示并拒绝 Promise；
 *   - 网络错误同样使用 ElMessage 提示并拒绝 Promise。
 */
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      // 业务成功，返回数据体
      return res
    } else {
      // 业务失败，弹出后端返回的错误消息
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  error => {
    // 网络或服务端错误，弹出错误提示
    ElMessage.error(error.message || 'Request Error')
    return Promise.reject(error)
  }
)

export default request

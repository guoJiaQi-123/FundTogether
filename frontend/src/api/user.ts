/**
 * @file 用户相关 API 接口模块
 * @description 封装了用户账户相关的所有 HTTP 请求，包括注册、登录、密码管理、
 *              个人信息管理、支付方式、账户余额、提现、收藏、举报和消息等功能。
 */

import request from '../utils/request'

/**
 * 用户注册
 * @param data - 注册数据，包含用户名、密码、邮箱等
 * @returns 注册结果
 */
export const registerUser = (data: any) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param data - 登录数据，包含用户名和密码
 * @returns 登录结果，包含 token 和用户信息
 */
export const loginUser = (data: any) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 修改密码（已登录用户）
 * @param data - 包含旧密码和新密码的数据对象
 * @returns 修改结果
 */
export const updatePassword = (data: any) => {
  return request.put('/user/password', data)
}

/**
 * 重置密码（忘记密码场景）
 * @param data - 包含身份验证信息和新密码的数据对象
 * @returns 重置结果
 */
export const resetPassword = (data: any) => {
  return request.post('/user/reset-password', data)
}

/**
 * 更新用户个人资料
 * @param data - 待更新的个人资料字段，如昵称、头像、简介等
 * @returns 更新结果
 */
export const updateProfile = (data: any) => {
  return request.put('/user/profile', data)
}

/**
 * 获取当前登录用户信息
 * @returns 当前用户详细信息
 */
export const getUserInfo = () => {
  return request.get('/user/info')
}

/**
 * 获取指定用户的公开资料（无需登录）
 * @param id - 目标用户ID
 * @returns 用户公开资料数据
 */
export const getPublicUserProfile = (id: number) => {
  return request.get(`/public/user/${id}`)
}

/**
 * 绑定支付方式
 * @param data - 支付方式数据，包含支付类型、账户信息等
 * @returns 绑定结果
 */
export const bindPaymentMethod = (data: any) => {
  return request.post('/user/payment-method', data)
}

/**
 * 获取当前用户已绑定的支付方式列表
 * @returns 支付方式列表数据
 */
export const getPaymentMethods = () => {
  return request.get('/user/payment-methods')
}

/**
 * 获取当前用户账户余额
 * @returns 账户余额信息
 */
export const getBalance = () => {
  return request.get('/user/account')
}

/**
 * 模拟充值（开发/测试环境使用）
 * @param amount - 充值金额
 * @returns 充值结果
 */
export const mockRecharge = (amount: number) => {
  return request.post('/user/account/recharge/mock', { amount })
}

/**
 * 申请提现
 * @param data - 提现申请数据，包含提现金额、支付方式等
 * @returns 提现申请结果
 */
export const applyWithdrawal = (data: any) => {
  return request.post('/withdrawal/apply', data)
}

/**
 * 获取当前用户的提现记录列表
 * @param params - 查询参数，如分页信息等
 * @returns 提现记录列表数据
 */
export const getMyWithdrawals = (params: any) => {
  return request.get('/withdrawal/my-list', { params })
}

export const getMyRechargeOrders = (params: any) => {
  return request.get('/user/account/recharge/my-list', { params })
}

/**
 * 收藏项目
 * @param projectId - 待收藏的项目ID
 * @returns 收藏操作结果
 */
export const addFavorite = (projectId: number) => {
  return request.post(`/favorite/${projectId}`)
}

/**
 * 取消收藏项目
 * @param projectId - 待取消收藏的项目ID
 * @returns 取消收藏操作结果
 */
export const removeFavorite = (projectId: number) => {
  return request.delete(`/favorite/${projectId}`)
}

/**
 * 检查当前用户是否已收藏指定项目
 * @param projectId - 项目ID
 * @returns 收藏状态信息
 */
export const checkFavorite = (projectId: number) => {
  return request.get(`/favorite/status/${projectId}`)
}

/**
 * 获取当前用户的收藏项目列表
 * @param params - 查询参数，如分页信息等
 * @returns 收藏项目列表数据
 */
export const getMyFavorites = (params: any) => {
  return request.get('/favorite/my-list', { params })
}

/**
 * 举报项目
 * @param projectId - 被举报的项目ID
 * @param reason - 举报原因
 * @returns 举报提交结果
 */
export const submitReport = (projectId: number, reason: string) => {
  return request.post(`/report/${projectId}`, { reason })
}

/**
 * 获取当前用户的消息列表
 * @param params - 查询参数，如分页信息等
 * @returns 消息列表数据
 */
export const getMyMessages = (params: any) => {
  return request.get('/message/my-list', { params })
}

/**
 * 标记消息为已读
 * @param id - 消息ID
 * @returns 标记结果
 */
export const markMessageRead = (id: number) => {
  return request.put(`/message/${id}/read`)
}

/**
 * 获取当前用户未读消息数量
 * @returns 未读消息数量
 */
export const getUnreadCount = () => {
  return request.get('/message/unread-count')
}

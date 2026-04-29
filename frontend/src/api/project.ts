/**
 * @file 项目相关 API 接口模块
 * @description 封装了众筹项目相关的所有 HTTP 请求，包括项目的公开查询、创建、更新、
 *              取消，以及项目动态、订单、分类等功能。
 */

import request from '../utils/request'

/**
 * 获取已上线项目列表（公开接口）
 * @param params - 查询参数，如分页信息、分类筛选、搜索关键词等
 * @returns 上线项目列表数据
 */
export const getOnlineProjects = (params: any) => {
  return request.get('/public/project/list', { params })
}

/**
 * 获取项目详情（公开接口）
 * @param id - 项目ID
 * @returns 项目详情数据
 */
export const getProjectDetail = (id: number) => {
  return request.get(`/public/project/${id}`)
}

/**
 * 创建众筹项目（发起人）
 * @param data - 项目创建数据，包含标题、描述、目标金额、分类等
 * @returns 创建结果
 */
export const createProject = (data: any) => {
  return request.post('/sponsor/project/create', data)
}

/**
 * 更新众筹项目信息（发起人）
 * @param data - 项目更新数据，包含项目ID及待修改字段
 * @returns 更新结果
 */
export const updateProject = (data: any) => {
  return request.put('/sponsor/project/update', data)
}

/**
 * 取消众筹项目（发起人）
 * @param id - 待取消的项目ID
 * @returns 取消结果
 */
export const cancelProject = (id: number) => {
  return request.put(`/sponsor/project/cancel/${id}`)
}

/**
 * 获取当前用户发起的项目列表
 * @param params - 查询参数，如分页信息等
 * @returns 我发起的项目列表数据
 */
export const getMyProjects = (params: any) => {
  return request.get('/sponsor/project/my-list', { params })
}

/**
 * 获取项目的支持者列表
 * @param id - 项目ID
 * @param params - 查询参数，如分页信息等
 * @returns 支持者列表数据
 */
export const getProjectSupporters = (id: number, params: any) => {
  return request.get(`/sponsor/project/${id}/supporters`, { params })
}

/**
 * 获取项目分类列表（公开接口）
 * @returns 分类列表数据
 */
export const getCategories = () => {
  return request.get('/public/categories')
}

/**
 * 获取项目动态/更新列表
 * @param projectId - 项目ID
 * @returns 项目动态列表数据
 */
export const getProjectUpdates = (projectId: number) => {
  return request.get(`/public/project/${projectId}/updates`)
}

/**
 * 发布项目动态/更新（发起人）
 * @param data - 动态数据，包含项目ID、更新内容等
 * @returns 发布结果
 */
export const publishProjectUpdate = (data: any) => {
  return request.post('/sponsor/update/publish', data)
}

/**
 * 创建支持订单（支持者下单）
 * @param data - 订单数据，包含项目ID、支持金额、支付方式等
 * @returns 订单创建结果
 */
export const createOrder = (data: any) => {
  return request.post('/order/create', data)
}

/**
 * 获取当前用户的订单列表
 * @param params - 查询参数，如分页信息等
 * @returns 订单列表数据
 */
export const getMyOrders = (params: any) => {
  return request.get('/order/my-list', { params })
}

/**
 * 获取当前用户的订单统计信息
 * @returns 订单统计数据，如总支持金额、订单数量等
 */
export const getMyOrderStats = () => {
  return request.get('/order/my-stats')
}

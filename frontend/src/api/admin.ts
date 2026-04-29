/**
 * @file 管理员相关 API 接口模块
 * @description 封装了管理员后台管理相关的所有 HTTP 请求，包括用户管理、项目审核、
 *              提现审批、举报处理、分类管理以及推荐项目管理等功能。
 */

import request from '../utils/request'

/**
 * 获取用户列表（管理员）
 * @param params - 查询参数，如分页信息、筛选条件等
 * @returns 用户列表数据
 */
export const getAdminUsers = (params: any) => {
  return request.get('/admin/users', { params })
}

/**
 * 更新用户角色
 * @param data - 包含用户ID和目标角色信息的数据对象
 * @returns 更新结果
 */
export const updateUserRole = (data: any) => {
  return request.post('/admin/user/role', data)
}

/**
 * 获取待审核项目列表
 * @param params - 查询参数，如分页信息等
 * @returns 待审核项目列表数据
 */
export const getPendingProjects = (params: any) => {
  return request.get('/admin/projects/pending', { params })
}

/**
 * 审批通过项目
 * @param id - 待审批项目的ID
 * @returns 审批结果
 */
export const approveProject = (id: number) => {
  return request.put(`/admin/projects/approve/${id}`)
}

/**
 * 驳回项目
 * @param id - 待驳回项目的ID
 * @param reason - 驳回原因
 * @returns 驳回结果
 */
export const rejectProject = (id: number, reason: string) => {
  return request.put(`/admin/projects/reject/${id}`, { reason })
}

/**
 * 下架项目
 * @param id - 待下架项目的ID
 * @param reason - 下架原因
 * @returns 下架结果
 */
export const takedownProject = (id: number, reason: string) => {
  return request.put(`/admin/projects/takedown/${id}`, { reason })
}

/**
 * 管理员修改项目内容
 * @param data - 包含项目ID及待修改字段的数据对象
 * @returns 修改结果
 */
export const updateProjectContentByAdmin = (data: any) => {
  return request.put('/admin/projects/update-content', data)
}

/**
 * 获取提现申请列表（管理员）
 * @param params - 查询参数，如分页信息、筛选条件等
 * @returns 提现申请列表数据
 */
export const getWithdrawalList = (params: any) => {
  return request.get('/withdrawal/admin/list', { params })
}

/**
 * 审批通过提现申请
 * @param id - 提现申请的ID
 * @returns 审批结果
 */
export const approveWithdrawal = (id: number) => {
  return request.put(`/withdrawal/admin/approve/${id}`)
}

/**
 * 驳回提现申请
 * @param id - 提现申请的ID
 * @param reason - 驳回原因
 * @returns 驳回结果
 */
export const rejectWithdrawal = (id: number, reason: string) => {
  return request.put(`/withdrawal/admin/reject/${id}`, { reason })
}

/**
 * 获取举报列表（管理员）
 * @param params - 查询参数，如分页信息、筛选条件等
 * @returns 举报列表数据
 */
export const getReportList = (params: any) => {
  return request.get('/report/admin/list', { params })
}

/**
 * 处理举报
 * @param id - 举报记录的ID
 * @param handleResult - 处理结果说明
 * @returns 处理结果
 */
export const handleReport = (id: number, handleResult: string) => {
  return request.put(`/report/admin/handle/${id}`, { handleResult })
}

/**
 * 创建项目分类
 * @param data - 分类数据，包含分类名称等信息
 * @returns 创建结果
 */
export const createCategory = (data: any) => {
  return request.post('/admin/categories', data)
}

/**
 * 更新项目分类
 * @param id - 待更新的分类ID
 * @param data - 更新后的分类数据
 * @returns 更新结果
 */
export const updateCategory = (id: number, data: any) => {
  return request.put(`/admin/categories/${id}`, data)
}

/**
 * 删除项目分类
 * @param id - 待删除的分类ID
 * @returns 删除结果
 */
export const deleteCategory = (id: number) => {
  return request.delete(`/admin/categories/${id}`)
}

/**
 * 获取推荐项目列表
 * @returns 推荐项目列表数据
 */
export const getRecommendList = () => {
  return request.get('/admin/recommends')
}

/**
 * 添加推荐项目
 * @param projectId - 待推荐的项目ID
 * @param sortOrder - 可选，推荐排序权重，数值越小越靠前
 * @returns 添加结果
 */
export const addRecommend = (projectId: number, sortOrder?: number) => {
  return request.post('/admin/recommends', { projectId, sortOrder })
}

/**
 * 移除推荐项目
 * @param id - 推荐记录的ID
 * @returns 移除结果
 */
export const removeRecommend = (id: number) => {
  return request.delete(`/admin/recommends/${id}`)
}

/**
 * 批量更新推荐项目的排序
 * @param orderedIds - 按新排序排列的推荐记录ID数组
 * @returns 更新结果
 */
export const batchUpdateRecommendSort = (orderedIds: number[]) => {
  return request.put('/admin/recommends/sort', orderedIds)
}

/**
 * 获取可推荐的项目列表（未被添加为推荐的项目）
 * @param params - 查询参数，如分页信息、搜索关键词等
 * @returns 可用项目列表数据
 */
export const getAvailableProjects = (params: any) => {
  return request.get('/admin/recommends/available-projects', { params })
}

export const getHeatRules = () => {
  return request.get('/admin/heat-rules')
}

export const updateHeatRuleWeight = (id: number, weight: number) => {
  return request.put(`/admin/heat-rules/${id}/weight`, { weight })
}

export const toggleHeatRule = (id: number, enabled: boolean) => {
  return request.put(`/admin/heat-rules/${id}/toggle`, { enabled })
}

export const resetHeatRules = () => {
  return request.post('/admin/heat-rules/reset')
}

export const recalculateHeat = () => {
  return request.post('/admin/heat-rules/recalculate')
}

/* ========== 数据大盘相关 ========== */

/** 获取数据大盘概览 KPI */
export const getOverviewStats = () => {
  return request.get('/admin/stats/overview')
}

/** 近 N 天每日筹款金额/订单趋势 */
export const getDailyFunding = (days: number = 30) => {
  return request.get('/admin/stats/daily-funding', { params: { days } })
}

/** 近 N 天支付时段分布 */
export const getHourlyDistribution = (days: number = 30) => {
  return request.get('/admin/stats/hourly-distribution', { params: { days } })
}

/** 获取 Top N 筹款项目 */
export const getTopProjects = (limit: number = 10) => {
  return request.get('/admin/stats/top-projects', { params: { limit } })
}

/** 获取 Top N 支持用户 */
export const getTopSupporters = (limit: number = 10) => {
  return request.get('/admin/stats/top-supporters', { params: { limit } })
}

/** 项目状态分布 */
export const getProjectStatusDistribution = () => {
  return request.get('/admin/stats/project-status')
}

/** 近 N 天新增项目趋势 */
export const getDailyProjects = (days: number = 30) => {
  return request.get('/admin/stats/daily-projects', { params: { days } })
}

/** 近 N 天新注册用户趋势 */
export const getDailyUsers = (days: number = 30) => {
  return request.get('/admin/stats/daily-users', { params: { days } })
}

/** 用户角色分布 */
export const getRoleDistribution = () => {
  return request.get('/admin/stats/role-distribution')
}

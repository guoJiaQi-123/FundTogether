/**
 * @file 用户关注关系 API 接口模块
 * @description 封装了用户之间关注/取关相关的所有 HTTP 请求，包括关注用户、
 *              取消关注、查询关注状态、获取关注列表和粉丝列表等功能。
 */

import request from '../utils/request'

/**
 * 关注指定用户
 * @param userId - 待关注的目标用户ID
 * @returns 关注操作结果
 */
export const followUser = (userId: number) => {
  return request.post(`/follow/${userId}`)
}

/**
 * 取消关注指定用户
 * @param userId - 待取消关注的目标用户ID
 * @returns 取消关注操作结果
 */
export const unfollowUser = (userId: number) => {
  return request.delete(`/follow/${userId}`)
}

/**
 * 查询当前用户对指定用户的关注状态
 * @param userId - 目标用户ID
 * @returns 关注状态信息（是否已关注）
 */
export const getFollowStatus = (userId: number) => {
  return request.get(`/follow/status/${userId}`)
}

/**
 * 获取指定用户的关注列表
 * @param userId - 目标用户ID
 * @param page - 页码，默认为1
 * @param size - 每页数量，默认为20
 * @returns 该用户关注的人的列表数据
 */
export const getFollowingList = (userId: number, page: number = 1, size: number = 20) => {
  return request.get(`/follow/following/${userId}`, { params: { page, size } })
}

/**
 * 获取指定用户的粉丝列表
 * @param userId - 目标用户ID
 * @param page - 页码，默认为1
 * @param size - 每页数量，默认为20
 * @returns 该用户的粉丝列表数据
 */
export const getFollowerList = (userId: number, page: number = 1, size: number = 20) => {
  return request.get(`/follow/followers/${userId}`, { params: { page, size } })
}

/**
 * @file 用户状态管理模块（Pinia Store）
 * @description 使用 Pinia 定义全局用户状态仓库，管理用户登录态、角色权限等信息。
 *              同时导出角色常量定义和角色判断工具函数，供全局使用。
 */

import { defineStore } from 'pinia'

/**
 * 用户角色常量（位掩码）
 * @description 使用位掩码方式定义角色，支持一个用户同时拥有多个角色。
 *   - USER(1):  普通用户，二进制 001
 *   - SPONSOR(2): 项目发起人，二进制 010
 *   - ADMIN(4):  管理员，二进制 100
 */
export const UserRole = {
  USER: 1,
  SPONSOR: 2,
  ADMIN: 4
} as const

/**
 * 判断用户是否拥有指定角色（位运算）
 * @param roleMask - 用户的角色位掩码值，可能为 undefined（未登录时）
 * @param role - 待判断的目标角色值
 * @returns 是否拥有该角色，true 表示拥有
 */
export const hasRole = (roleMask: number | undefined, role: number): boolean => {
  if (!roleMask) return false
  return (roleMask & role) !== 0 // 按位与运算，结果非零则表示拥有该角色
}

/**
 * 判断用户是否为管理员
 * @param roleMask - 用户的角色位掩码值
 * @returns 是否为管理员
 */
export const isAdmin = (roleMask: number | undefined): boolean => hasRole(roleMask, UserRole.ADMIN)

/**
 * 判断用户是否为项目发起人
 * @param roleMask - 用户的角色位掩码值
 * @returns 是否为项目发起人
 */
export const isSponsor = (roleMask: number | undefined): boolean => hasRole(roleMask, UserRole.SPONSOR)

/**
 * 用户状态仓库
 * @description 管理用户登录态和角色信息，自动与 localStorage 同步，
 *              页面刷新后可恢复登录状态。
 */
export const useUserStore = defineStore('user', {
  /** 仓库状态 */
  state: () => ({
    /** JWT 令牌，从 localStorage 恢复，用于接口鉴权 */
    token: localStorage.getItem('token') || '',
    /** 用户信息对象，从 localStorage 恢复，包含角色、昵称等 */
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  /** 计算属性 */
  getters: {
    /** 获取用户的角色位掩码值 */
    roleMask: (state) => state.userInfo?.role ?? 0,
    /** 判断当前用户是否为管理员 */
    isAdmin: (state) => hasRole(state.userInfo?.role, UserRole.ADMIN),
    /** 判断当前用户是否为项目发起人 */
    isSponsor: (state) => hasRole(state.userInfo?.role, UserRole.SPONSOR)
  },
  /** 同步操作 */
  actions: {
    /**
     * 设置用户信息（登录成功后调用）
     * @param data - 用户数据，包含 token 和用户详细信息
     */
    setUserInfo(data: any) {
      this.token = data.token
      this.userInfo = data
      // 持久化到 localStorage，确保刷新页面后登录态不丢失
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    /**
     * 退出登录
     * @description 清除内存中的用户状态和 localStorage 中的持久化数据
     */
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})

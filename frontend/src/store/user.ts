import { defineStore } from 'pinia'

export const UserRole = {
  USER: 1,
  SPONSOR: 2,
  ADMIN: 4
} as const

export const hasRole = (roleMask: number | undefined, role: number): boolean => {
  if (!roleMask) return false
  return (roleMask & role) !== 0
}

export const isAdmin = (roleMask: number | undefined): boolean => hasRole(roleMask, UserRole.ADMIN)
export const isSponsor = (roleMask: number | undefined): boolean => hasRole(roleMask, UserRole.SPONSOR)

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  getters: {
    roleMask: (state) => state.userInfo?.role ?? 0,
    isAdmin: (state) => hasRole(state.userInfo?.role, UserRole.ADMIN),
    isSponsor: (state) => hasRole(state.userInfo?.role, UserRole.SPONSOR)
  },
  actions: {
    setUserInfo(data: any) {
      this.token = data.token
      this.userInfo = data
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})

import request from '../utils/request'

export const registerUser = (data: any) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export const loginUser = (data: any) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export const updatePassword = (data: any) => {
  return request.put('/user/password', data)
}

export const resetPassword = (data: any) => {
  return request.post('/user/reset-password', data)
}

export const updateProfile = (data: any) => {
  return request.put('/user/profile', data)
}

export const getUserInfo = () => {
  return request.get('/user/info')
}

export const getPublicUserProfile = (id: number) => {
  return request.get(`/public/user/${id}`)
}

export const bindPaymentMethod = (data: any) => {
  return request.post('/user/payment-method', data)
}

export const getPaymentMethods = () => {
  return request.get('/user/payment-methods')
}

export const getBalance = () => {
  return request.get('/user/account')
}

export const mockRecharge = (amount: number) => {
  return request.post('/user/account/recharge/mock', { amount })
}

export const applyWithdrawal = (data: any) => {
  return request.post('/withdrawal/apply', data)
}

export const getMyWithdrawals = (params: any) => {
  return request.get('/withdrawal/my-list', { params })
}

export const addFavorite = (projectId: number) => {
  return request.post(`/favorite/${projectId}`)
}

export const removeFavorite = (projectId: number) => {
  return request.delete(`/favorite/${projectId}`)
}

export const checkFavorite = (projectId: number) => {
  return request.get(`/favorite/status/${projectId}`)
}

export const getMyFavorites = (params: any) => {
  return request.get('/favorite/my-list', { params })
}

export const submitReport = (projectId: number, reason: string) => {
  return request.post(`/report/${projectId}`, { reason })
}

export const getMyMessages = (params: any) => {
  return request.get('/message/my-list', { params })
}

export const markMessageRead = (id: number) => {
  return request.put(`/message/${id}/read`)
}

export const getUnreadCount = () => {
  return request.get('/message/unread-count')
}

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

// Public user profile (non-sensitive fields only)
export const getPublicUserProfile = (id: number) => {
  return request.get(`/public/user/${id}`)
}

export const bindPaymentMethod = (data: any) => {
  return request.post('/user/payment-method', data)
}

export const getPaymentMethods = () => {
  return request.get('/user/payment-methods')
}

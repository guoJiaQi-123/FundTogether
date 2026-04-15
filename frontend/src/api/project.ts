import request from '../utils/request'

export const getOnlineProjects = (params: any) => {
  return request.get('/public/project/list', { params })
}

export const getProjectDetail = (id: number) => {
  return request.get(`/public/project/${id}`)
}

export const createProject = (data: any) => {
  return request.post('/sponsor/project/create', data)
}

export const updateProject = (data: any) => {
  return request.put('/sponsor/project/update', data)
}

export const cancelProject = (id: number) => {
  return request.put(`/sponsor/project/cancel/${id}`)
}

export const getMyProjects = (params: any) => {
  return request.get('/sponsor/project/my-list', { params })
}

export const getProjectSupporters = (id: number, params: any) => {
  return request.get(`/sponsor/project/${id}/supporters`, { params })
}

export const getCategories = () => {
  return request.get('/public/categories')
}

export const getProjectUpdates = (projectId: number) => {
  return request.get(`/public/project/${projectId}/updates`)
}

export const publishProjectUpdate = (data: any) => {
  return request.post('/sponsor/update/publish', data)
}

export const createOrder = (data: any) => {
  return request.post('/order/create', data)
}

export const getMyOrders = (params: any) => {
  return request.get('/order/my-list', { params })
}

export const getMyOrderStats = () => {
  return request.get('/order/my-stats')
}

import request from '../utils/request'

export const getAdminUsers = (params: any) => {
  return request.get('/admin/users', { params })
}

export const updateUserRole = (data: any) => {
  return request.post('/admin/user/role', data)
}

export const getPendingProjects = (params: any) => {
  return request.get('/admin/projects/pending', { params })
}

export const approveProject = (id: number) => {
  return request.put(`/admin/projects/approve/${id}`)
}

export const rejectProject = (id: number, reason: string) => {
  return request.put(`/admin/projects/reject/${id}`, { reason })
}

export const takedownProject = (id: number, reason: string) => {
  return request.put(`/admin/projects/takedown/${id}`, { reason })
}

export const updateProjectContentByAdmin = (data: any) => {
  return request.put('/admin/projects/update-content', data)
}

export const getWithdrawalList = (params: any) => {
  return request.get('/withdrawal/admin/list', { params })
}

export const approveWithdrawal = (id: number) => {
  return request.put(`/withdrawal/admin/approve/${id}`)
}

export const rejectWithdrawal = (id: number, reason: string) => {
  return request.put(`/withdrawal/admin/reject/${id}`, { reason })
}

export const getReportList = (params: any) => {
  return request.get('/report/admin/list', { params })
}

export const handleReport = (id: number, handleResult: string) => {
  return request.put(`/report/admin/handle/${id}`, { handleResult })
}

export const createCategory = (data: any) => {
  return request.post('/admin/categories', data)
}

export const updateCategory = (id: number, data: any) => {
  return request.put(`/admin/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return request.delete(`/admin/categories/${id}`)
}

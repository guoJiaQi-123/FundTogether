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

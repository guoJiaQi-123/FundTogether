import request from '../utils/request'

export const followUser = (userId: number) => {
  return request.post(`/follow/${userId}`)
}

export const unfollowUser = (userId: number) => {
  return request.delete(`/follow/${userId}`)
}

export const getFollowStatus = (userId: number) => {
  return request.get(`/follow/status/${userId}`)
}

export const getFollowingList = (userId: number, page: number = 1, size: number = 20) => {
  return request.get(`/follow/following/${userId}`, { params: { page, size } })
}

export const getFollowerList = (userId: number, page: number = 1, size: number = 20) => {
  return request.get(`/follow/followers/${userId}`, { params: { page, size } })
}

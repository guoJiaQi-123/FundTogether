import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('../views/ProjectDetail.vue')
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('../views/Projects.vue')
      },
      {
        path: 'admin',
        component: () => import('../views/AdminDashboard.vue'),
        redirect: '/admin/dashboard',
        children: [
          {
            path: 'dashboard',
            name: 'AdminDashboard',
            component: () => import('../views/admin/StatsDashboard.vue')
          },
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('../views/admin/UserManage.vue')
          },
          {
            path: 'projects',
            name: 'AdminProjects',
            component: () => import('../views/admin/ProjectAudit.vue')
          },
          {
            path: 'notices',
            name: 'AdminNotices',
            component: () => import('../views/admin/NoticeManage.vue')
          },
          {
            path: 'funding',
            name: 'AdminFunding',
            component: () => import('../views/admin/FundingManage.vue')
          },
          {
            path: 'user-levels',
            name: 'AdminUserLevels',
            component: () => import('../views/admin/UserLevelManage.vue')
          },
          {
            path: 'withdrawals',
            name: 'AdminWithdrawals',
            component: () => import('../views/admin/WithdrawalManage.vue')
          },
          {
            path: 'reports',
            name: 'AdminReports',
            component: () => import('../views/admin/ReportManage.vue')
          }
        ]
      },
      {
        path: 'sponsor/projects',
        name: 'SponsorProjects',
        component: () => import('../views/SponsorProjects.vue')
      },
      {
        path: 'user/orders',
        name: 'UserOrders',
        component: () => import('../views/UserOrders.vue')
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue')
      },
      {
        path: 'user/messages',
        name: 'UserMessages',
        component: () => import('../views/UserMessages.vue')
      },
      {
        path: 'user/favorites',
        name: 'UserFavorites',
        component: () => import('../views/UserFavorites.vue')
      },
      {
        path: 'user/withdrawal',
        redirect: '/user/profile'
      },
      {
        path: 'users/:id',
        name: 'UserHome',
        component: () => import('../views/UserHome.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

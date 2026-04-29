/**
 * @file 路由配置模块
 * @description 定义应用的前端路由表，使用 Vue Router 进行页面导航管理。
 *              包含公开页面（登录、注册）、主布局内页面（首页、项目详情、用户中心）
 *              以及管理员后台子路由等。所有子页面组件均采用懒加载方式引入。
 */

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

/** 路由配置表 */
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/home' // 根路径重定向到首页
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue') // 注册页面（懒加载）
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue') // 登录页面（懒加载）
  },
  {
    path: '/',
    component: MainLayout, // 主布局组件，包含顶部导航和侧边栏
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue') // 首页
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('../views/ProjectDetail.vue') // 项目详情页，:id 为动态路由参数
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('../views/Projects.vue') // 项目列表页
      },
      {
        path: 'admin',
        component: () => import('../views/AdminDashboard.vue'), // 管理员后台布局
        redirect: '/admin/dashboard', // 默认跳转到数据看板
        children: [
          {
            path: 'dashboard',
            name: 'AdminDashboard',
            component: () => import('../views/admin/StatsDashboard.vue') // 数据统计看板
          },
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('../views/admin/UserManage.vue') // 用户管理
          },
          {
            path: 'projects',
            name: 'AdminProjects',
            component: () => import('../views/admin/ProjectAudit.vue') // 项目审核
          },
          {
            path: 'notices',
            name: 'AdminNotices',
            component: () => import('../views/admin/NoticeManage.vue') // 公告管理
          },
          {
            path: 'funding',
            name: 'AdminFunding',
            component: () => import('../views/admin/FundingManage.vue') // 资金管理
          },
          {
            path: 'user-levels',
            name: 'AdminUserLevels',
            component: () => import('../views/admin/UserLevelManage.vue') // 用户等级管理
          },
          {
            path: 'withdrawals',
            name: 'AdminWithdrawals',
            component: () => import('../views/admin/WithdrawalManage.vue') // 提现审批管理
          },
          {
            path: 'reports',
            name: 'AdminReports',
            component: () => import('../views/admin/ReportManage.vue') // 举报处理
          },
          {
            path: 'recommends',
            name: 'AdminRecommends',
            component: () => import('../views/admin/RecommendManage.vue')
          },
          {
            path: 'heat-rules',
            name: 'AdminHeatRules',
            component: () => import('../views/admin/HeatRuleManage.vue')
          }
        ]
      },
      {
        path: 'sponsor/projects',
        name: 'SponsorProjects',
        component: () => import('../views/SponsorProjects.vue') // 发起人的项目管理页
      },
      {
        path: 'user/orders',
        name: 'UserOrders',
        component: () => import('../views/UserOrders.vue') // 用户订单列表
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue') // 用户个人资料页
      },
      {
        path: 'user/favorites',
        name: 'UserFavorites',
        component: () => import('../views/UserFavorites.vue') // 用户收藏列表
      },
      {
        path: 'user/withdrawal',
        redirect: '/user/profile' // 提现入口重定向到个人资料页
      },
      {
        path: 'users/:id',
        name: 'UserHome',
        component: () => import('../views/UserHome.vue') // 其他用户的个人主页，:id 为动态路由参数
      }
    ]
  }
]

/** Vue Router 实例，使用 HTML5 History 模式 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

import { AppRouteRecord } from '@/types/router'

/**
 * 数据分析看板路由
 * 包含客户分析、产品分析、销售员业绩、订单跟踪、财务分析、地区热力图
 */
export const analysisRoutes: AppRouteRecord = {
  path: '/analysis',
  name: 'Analysis',
  component: '/index/index',
  meta: {
    title: '数据分析',
    icon: 'ri:bar-chart-box-line',
    roles: ['R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES']
  },
  children: [
    {
      path: 'customer',
      name: 'CustomerAnalysis',
      component: '/analysis/customer',
      meta: {
        title: '客户分析',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN', 'R_SALES']
      }
    },
    {
      path: 'product',
      name: 'ProductAnalysis',
      component: '/analysis/product',
      meta: {
        title: '产品分析',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'salesperson',
      name: 'SalespersonAnalysis',
      component: '/analysis/salesperson',
      meta: {
        title: '销售员业绩',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN', 'R_SALES']
      }
    },
    {
      path: 'order-tracking',
      name: 'OrderTracking',
      component: '/analysis/order-tracking',
      meta: {
        title: '订单跟踪',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'finance',
      name: 'FinanceAnalysis',
      component: '/analysis/finance',
      meta: {
        title: '财务分析',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'region',
      name: 'RegionHeatmap',
      component: '/analysis/region',
      meta: {
        title: '地区热力图',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    }
  ]
}

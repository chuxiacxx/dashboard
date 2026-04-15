import { AppRouteRecord } from '@/types/router'

export const detaildataRoutes: AppRouteRecord = {
  path: '/detail-data',
  name: 'DetailData',
  component: '/index/index', 
  meta: { 
    title: '明细数据', 
    icon: 'ri:database-line', 
    roles: ['R_SUPER', 'R_ADMIN', 'R_USER'] 
  },
  children: [
    {
      path: 'sales-details',
      name: 'SalesDetails',
      component: '/detaildata/salesdetails',
      meta: { 
        title: '销售明细', 
        keepAlive: true, 
        roles: ['R_SUPER', 'R_ADMIN', 'R_USER'] 
      }
    },
    {
      path: 'shipment-details',
      name: 'ShipmentDetails',
      component: '/detaildata/shipmentdetails',
      meta: { 
        title: '出货明细', 
        keepAlive: true, 
        roles: ['R_SUPER', 'R_ADMIN', 'R_USER'] 
      }
    },
    // 新增订单详情
    {
      path: 'order-details',
      name: 'OrderDetails',
      component: '/detaildata/orderdetails',
      meta: { 
        title: '订单明细', 
        keepAlive: true, 
        roles: ['R_SUPER', 'R_ADMIN', 'R_USER'] 
      }
    },
    // 开票金额详情
    {
      path: 'invoice-details',
      name: 'InvoiceDetails',
      component: '/detaildata/invoicedetails',
      meta: { 
        title: '开票明细', 
        keepAlive: true, 
        roles: ['R_SUPER', 'R_ADMIN', 'R_USER'] 
      }
    },
    // 成交额详情
    {
      path: 'deal-details',
      name: 'DealDetails',
      component: '/detaildata/dealdetails',
      meta: { 
        title: '成交明细', 
        keepAlive: true, 
        roles: ['R_SUPER', 'R_ADMIN', 'R_USER'] 
      }
    },
    // 新老客户分析
    {
      path: 'customer-details',
      name: 'CustomerDetails',
      component: '/detaildata/customerdetails',
      meta: { 
        title: '客户分析', 
        keepAlive: true, 
        roles: ['R_SUPER', 'R_ADMIN', 'R_USER'] 
      }
    }
  ]
}
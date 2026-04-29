import api from '@/utils/http'

/**
 * 获取仪表盘汇总
 */
export function fetchDashboardSummary() {
  return api.get({
    url: '/api/dashboard/summary',
    raw: true
  })
}

/**
 * 获取销售数据列表
 */
export function fetchSalesList(params: {
  current?: number
  size?: number
  startDate?: string
  endDate?: string
}) {
  return api.get({
    url: '/api/data/sales',
    params,
    raw: true
  })
}

/**
 * 获取发货数据列表
 */
export function fetchShipmentList(params: {
  current?: number
  size?: number
  startDate?: string
  endDate?: string
}) {
  return api.get({
    url: '/api/data/shipment',
    params,
    raw: true
  })
}

/**
 * 获取订单数据列表
 */
export function fetchOrderList(params: {
  current?: number
  size?: number
  startDate?: string
  endDate?: string
}) {
  return api.get({
    url: '/api/data/order',
    params,
    raw: true
  })
}

/**
 * 获取发票数据列表
 */
export function fetchInvoiceList(params: {
  current?: number
  size?: number
  startDate?: string
  endDate?: string
}) {
  return api.get({
    url: '/api/data/invoice',
    params,
    raw: true
  })
}

/**
 * 获取成交数据列表
 */
export function fetchDealList(params: {
  current?: number
  size?: number
  startDate?: string
  endDate?: string
}) {
  return api.get({
    url: '/api/data/deal',
    params,
    raw: true
  })
}

/**
 * 获取客户数据列表
 */
export function fetchCustomerList(params: {
  current?: number
  size?: number
  channel?: string
  isNew?: number
}) {
  return api.get({
    url: '/api/data/customer',
    params,
    raw: true
  })
}

/**
 * 获取支持导入的数据类型列表
 */
export function fetchImportTypes() {
  return api.get<Array<{ type: string; name: string; extensions: string }>>({
    url: '/api/data/import/types',
    raw: true
  })
}

/**
 * 导入数据文件
 * @param file 文件对象
 * @param dataType 数据类型 (sales/order/shipment/invoice/deal/customer)
 */
export function fetchImportData(file: File, dataType: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', dataType)

  return api.post({
    url: '/api/data/import',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    raw: true
  })
}

/**
 * 下载导入模板
 * @param dataType 数据类型
 */
export function fetchImportTemplate(dataType: string) {
  return api.request({
    url: `/api/data/import/template/${dataType}`,
    method: 'GET',
    responseType: 'blob',
    raw: true
  })
}

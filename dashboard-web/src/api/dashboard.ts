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
 * 获取月度销售数据
 * @param year 年份（与startDate/endDate互斥）
 * @param startDate 开始日期（格式：YYYY-MM-DD）
 * @param endDate 结束日期（格式：YYYY-MM-DD）
 */
export function fetchSalesList(params?: { year?: number; startDate?: string; endDate?: string }) {
  return api.get({
    url: '/api/data/sales',
    params,
    raw: true
  })
}

/**
 * 获取月度发货数据
 * @param year 年份（与startDate/endDate互斥）
 * @param startDate 开始日期（格式：YYYY-MM-DD）
 * @param endDate 结束日期（格式：YYYY-MM-DD）
 */
export function fetchShipmentList(params?: { year?: number; startDate?: string; endDate?: string }) {
  return api.get({
    url: '/api/data/shipment',
    params,
    raw: true
  })
}

/**
 * 获取产品销量排行
 * @param year 年份
 * @param month 月份
 * @param limit 数量限制
 * @param startDate 开始日期（与year/month互斥）
 * @param endDate 结束日期（与year/month互斥）
 */
export function fetchProductRanking(params?: { year?: number; month?: number; limit?: number; startDate?: string; endDate?: string }) {
  return api.get({
    url: '/api/data/products',
    params,
    raw: true
  })
}

/**
 * 获取销售员业绩排行
 * @param year 年份
 * @param month 月份
 * @param startDate 开始日期（与year/month互斥）
 * @param endDate 结束日期（与year/month互斥）
 */
export function fetchSalespersonRanking(params?: { year?: number; month?: number; startDate?: string; endDate?: string }) {
  return api.get({
    url: '/api/data/salespersons',
    params,
    raw: true
  })
}

/**
 * 获取地区销售统计
 * @param year 年份
 * @param month 月份
 * @param startDate 开始日期（与year/month互斥）
 * @param endDate 结束日期（与year/month互斥）
 */
export function fetchRegionStats(params?: { year?: number; month?: number; startDate?: string; endDate?: string }) {
  return api.get({
    url: '/api/data/regions',
    params,
    raw: true
  })
}

/**
 * 获取可用年份列表
 */
export function fetchAvailableYears() {
  return api.get({
    url: '/api/data/years',
    raw: true
  })
}

/**
 * 获取销售员列表
 */
export function fetchSalespersonList() {
  return api.get({
    url: '/api/data/salespersons/list',
    raw: true
  })
}

/**
 * 获取地区列表
 */
export function fetchRegionList() {
  return api.get({
    url: '/api/data/regions/list',
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

// ==================== 销售目标配置 API ====================

/**
 * 获取销售目标列表
 */
export function fetchSalesTargets(params?: { year?: number; month?: number }) {
  return api.get({
    url: '/api/sales-target',
    params,
    raw: true
  })
}

/**
 * 创建销售目标
 */
export function createSalesTarget(data: {
  year: number
  month: number
  salesperson?: string
  region?: string
  targetAmount: number
  collectionTargetAmount?: number
  orderCountTarget?: number
  remark?: string
}) {
  return api.post({
    url: '/api/sales-target',
    data,
    raw: true
  })
}

/**
 * 更新销售目标
 */
export function updateSalesTarget(
  id: number,
  data: {
    targetAmount: number
    collectionTargetAmount?: number
    orderCountTarget?: number
    remark?: string
  }
) {
  return api.put({
    url: `/api/sales-target/${id}`,
    data,
    raw: true
  })
}

/**
 * 删除销售目标
 */
export function deleteSalesTarget(id: number) {
  return api.del({
    url: `/api/sales-target/${id}`,
    raw: true
  })
}

/**
 * 批量创建/更新销售目标
 */
export function batchCreateSalesTargets(
  data: Array<{
    year: number
    month: number
    salesperson?: string
    region?: string
    targetAmount: number
    collectionTargetAmount?: number
    orderCountTarget?: number
    remark?: string
  }>
) {
  return api.post({
    url: '/api/sales-target/batch',
    data,
    raw: true
  })
}

/**
 * 获取销售员业绩排行（含目标对比）
 */
export function fetchSalesTargetRanking(params: { year: number; month: number }) {
  return api.get({
    url: '/api/sales-target/ranking',
    params,
    raw: true
  })
}

/**
 * 获取月度目标达成情况
 */
export function fetchMonthlyAchievement(params: { year: number }) {
  return api.get({
    url: '/api/sales-target/monthly-achievement',
    params,
    raw: true
  })
}

/**
 * 获取最近订单列表
 */
export function fetchRecentOrders(params: { startDate: string; endDate: string }) {
  return api.get({
    url: '/api/data/orders',
    params,
    raw: true
  })
}

/**
 * 获取超期订单列表
 */
export function fetchOverdueOrders(params: { startDate: string; endDate: string }) {
  return api.get({
    url: '/api/data/orders/overdue',
    params,
    raw: true
  })
}

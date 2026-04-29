import { AppRouteRecord } from '@/types/router'
import { dashboardRoutes } from './dashboard'
import { systemRoutes } from './system'
import { detaildataRoutes } from './detail'
import { dataImportRoutes } from './data-import'
import { analysisRoutes } from './analysis'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [
  dashboardRoutes,
  analysisRoutes,
  systemRoutes,
  detaildataRoutes,
  dataImportRoutes
]

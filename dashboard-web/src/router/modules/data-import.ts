import { AppRouteRecord } from '@/types/router'

export const dataImportRoutes: AppRouteRecord = {
  path: '/data-import',
  name: 'DataImport',
  component: '/index/index',
  meta: {
    title: 'menus.dataImport.title',
    icon: 'ri:upload-cloud-line',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'index',
      name: 'DataImportPage',
      component: '/system/data-import',
      meta: {
        title: 'menus.dataImport.title',
        keepAlive: true
      }
    }
  ]
}

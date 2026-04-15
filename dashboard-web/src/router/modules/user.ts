// router/modules/user.ts
import { AppRouteRecord } from '@/types/router'

export const userRoutes: AppRouteRecord = {
  path: '/user-manage',
  name: 'UserManage',
  component: '/index/index',
  meta: { 
    title: '用户管理', 
    icon: 'ri:team-line',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'list',
      name: 'UserList',
      component: '/user/UserList',
      meta: { 
        title: '用户列表', 
        keepAlive: false,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'test',
      name: 'UserTest', // 建议使用 UserTest 保持命名一致性
      component: '/user/test', // 对应 views/user/test.vue
      meta: { 
        title: '测试页面', 
        keepAlive: false,
        roles: ['R_SUPER', 'R_ADMIN'] // 根据实际权限需求调整
      }
    }
  ]
}
import api from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

// 获取用户列表
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return api.get<Api.SystemManage.UserList>({
    url: '/api/user/list',
    params,
    raw: true
  })
}

// 获取角色列表
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return api.get<Api.SystemManage.RoleList>({
    url: '/api/role/list',
    params,
    raw: true
  })
}

// 获取菜单列表
export function fetchGetMenuList() {
  return api.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus/simple',
    raw: true
  })
}

// 添加用户
export function fetchAddUser(data: any) {
  return api.post({
    url: '/api/user',
    data,
    raw: true
  })
}

// 更新用户
export function fetchUpdateUser(data: any) {
  return api.put({
    url: `/api/user/${data.id}`,
    data,
    raw: true
  })
}

// 删除用户
export function fetchDeleteUser(id: number) {
  return api.del({
    url: `/api/user/${id}`,
    raw: true
  })
}

// 添加角色
export function fetchAddRole(data: any) {
  return api.post({
    url: '/api/role',
    data,
    raw: true
  })
}

// 更新角色
export function fetchUpdateRole(data: any) {
  return api.put({
    url: `/api/role/${data.id}`,
    data,
    raw: true
  })
}

// 删除角色
export function fetchDeleteRole(id: number) {
  return api.del({
    url: `/api/role/${id}`,
    raw: true
  })
}

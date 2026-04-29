import api from '@/utils/http'

/**
 * 登录
 * @param params 登录参数
 * @returns 登录响应完整对象 {code, message, data: {token, refreshToken}}
 */
export function fetchLogin(params: Api.Auth.LoginParams) {
  return api.request<Api.Auth.LoginResponse>({
    url: '/api/auth/login',
    method: 'POST',
    data: params,
    raw: true
  })
}

/**
 * 获取用户信息
 * @returns 完整响应对象 {code, message, data: UserInfo}
 */
export function fetchGetUserInfo() {
  return api.get<Api.Auth.UserInfo>({
    url: '/api/user/info',
    raw: true
  })
}

/**
 * 修改密码
 * @param oldPassword 原密码
 * @param newPassword 新密码
 */
export function fetchChangePassword(oldPassword: string, newPassword: string) {
  return api.put({
    url: '/api/auth/password',
    params: { oldPassword, newPassword },
    raw: true
  })
}

/**
 * 更新用户信息
 * @param data 用户信息
 */
export function fetchUpdateUserInfo(data: {
  nickName?: string
  email?: string
  phone?: string
  gender?: string
}) {
  return api.put({
    url: '/api/auth/userinfo',
    data,
    raw: true
  })
}

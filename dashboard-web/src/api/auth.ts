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
 * 注册
 * @param params 注册参数
 * @returns 注册响应完整对象 {code, message, data}
 */
export function fetchRegister(params: { username: string; password: string }) {
  return api.request({
    url: '/api/auth/register',
    method: 'POST',
    data: params,
    raw: true
  })
}

/**
 * 获取用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return api.get<Api.Auth.UserInfo>({
    url: '/api/user/info'
  })
}

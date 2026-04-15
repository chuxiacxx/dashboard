/**
 * 路由全局前置守卫模块
 *
 * 提供完整的路由导航守卫功能
 *
 * ## 主要功能
 *
 * - 登录状态验证和重定向
 * - 动态路由注册和权限控制
 * - 菜单数据获取和处理（前端/后端模式）
 * - 用户信息获取和缓存
 * - 页面标题设置
 * - 工作标签页管理
 * - 进度条和加载动画控制
 * - 静态路由识别和处理
 * - 错误处理和异常跳转
 *
 * ## 使用场景
 *
 * - 路由跳转前的权限验证
 * - 动态菜单加载和路由注册
 * - 用户登录状态管理
 * - 页面访问控制
 * - 路由级别的加载状态管理
 *
 * ## 工作流程
 *
 * 1. 检查登录状态，未登录跳转到登录页
 * 2. 首次访问时获取用户信息和菜单数据
 * 3. 根据权限动态注册路由
 * 4. 设置页面标题和工作标签页
 * 5. 处理根路径重定向到首页
 * 6. 未匹配路由跳转到 404 页面
 *
 * @module router/guards/beforeEach
 * @author Art Design Pro Team
 */
import type { Router, RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { nextTick } from 'vue'
import NProgress from 'nprogress'
import { useSettingStore } from '@/store/modules/setting'
import { useUserStore } from '@/store/modules/user'
import { useMenuStore } from '@/store/modules/menu'
import { setWorktab } from '@/utils/navigation'
import { setPageTitle } from '@/utils/router'
import { RoutesAlias } from '../routesAlias'
import { staticRoutes } from '../routes/staticRoutes'
import { loadingService } from '@/utils/ui'
import { useCommon } from '@/hooks/core/useCommon'
import { useWorktabStore } from '@/store/modules/worktab'
import { fetchGetUserInfo } from '@/api/auth'
import { ApiStatus } from '@/utils/http/status'
import { isHttpError } from '@/utils/http/error'
import { RouteRegistry, MenuProcessor, IframeRouteManager, RoutePermissionValidator } from '../core'

// 路由注册器实例
let routeRegistry: RouteRegistry | null = null

// 菜单处理器实例
const menuProcessor = new MenuProcessor()

// 跟踪是否需要关闭 loading
let pendingLoading = false

/**
 * 获取 pendingLoading 状态
 */
export function getPendingLoading(): boolean {
  return pendingLoading
}

/**
 * 重置 pendingLoading 状态
 */
export function resetPendingLoading(): void {
  pendingLoading = false
}

/**
 * 设置路由全局前置守卫
 */
export function setupBeforeEachGuard(router: Router): void {
  // 初始化路由注册器
  routeRegistry = new RouteRegistry(router)

  router.beforeEach(
    async (
      to: RouteLocationNormalized,
      from: RouteLocationNormalized,
      next: NavigationGuardNext
    ) => {
      try {
        console.log('🚀 ========== 路由守卫开始 ==========')
        console.log('📌 路由跳转信息:')
        console.log('   📍 目标路径:', to.path)
        console.log('   📍 目标全路径:', to.fullPath)
        console.log('   📍 目标参数:', to.params)
        console.log('   📍 目标查询参数:', to.query)
        console.log('   📍 来源路径:', from.path)
        console.log('   📍 匹配的路由:', to.matched.map(m => m.path))
        
        const userStore = useUserStore()
        console.log('🔐 用户状态:')
        console.log('   ✅ 是否已登录:', userStore.isLogin)
        console.log('   ✅ 用户信息:', userStore.getUserInfo)
        console.log('   ✅ 路由是否已注册:', routeRegistry?.isRegistered())
        
        // 打印当前路由器的所有路由
        console.log('🗺️ 当前路由器路由列表:')
        const allRoutes = router.getRoutes()
        console.log(`   总路由数: ${allRoutes.length}`)
        allRoutes.forEach((route, index) => {
          const routeName = route.name ? String(route.name) : '未命名'
          console.log(`   ${index + 1}. ${route.path} [name: ${routeName}] [meta: ${JSON.stringify(route.meta || {})}]`)
        })

        // 调用动态路由逻辑
        await handleRouteGuard(to, from, next, router)
      } catch (error) {
        console.error('❌ [RouteGuard] 路由守卫处理失败:', error)
        closeLoading()
        next({ name: 'Exception500' })
      } finally {
        console.log('🏁 ========== 路由守卫结束 ==========')
      }
    }
  )
}


/**
 * 关闭 loading 效果
 */
function closeLoading(): void {
  if (pendingLoading) {
    nextTick(() => {
      loadingService.hideLoading()
      pendingLoading = false
      console.log('⏳ Loading 已关闭')
    })
  }
}

/**
 * 处理路由守卫逻辑
 */
async function handleRouteGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  const settingStore = useSettingStore()
  const userStore = useUserStore()

  console.log('🔄 开始处理路由守卫逻辑')
  console.log(`📊 NProgress 显示状态: ${settingStore.showNprogress}`)

  // 启动进度条
  if (settingStore.showNprogress) {
    NProgress.start()
    console.log('📈 NProgress 已启动')
  }

  // 1. 检查登录状态
  if (!handleLoginStatus(to, userStore, next)) {
    console.log('🔐 登录状态检查未通过，已处理跳转')
    return
  }
  console.log('✅ 登录状态检查通过')

  // 2. 处理动态路由注册
  if (!routeRegistry?.isRegistered() && userStore.isLogin) {
    console.log('🔄 需要处理动态路由注册')
    console.log(`  路由注册器状态: ${routeRegistry ? '已创建' : '未创建'}`)
    console.log(`  路由是否已注册: ${routeRegistry?.isRegistered()}`)
    
    await handleDynamicRoutes(to, next, router)
    return
  }
  console.log('✅ 动态路由已注册或不需要注册')

  // 3. 处理根路径重定向
  if (handleRootPathRedirect(to, next)) {
    console.log('🔄 已处理根路径重定向')
    return
  }
  console.log('✅ 非根路径，无需重定向')

  // 4. 处理已匹配的路由
  if (to.matched.length > 0) {
    console.log('✅ 路由匹配成功')
    console.log(`   匹配到的路由数: ${to.matched.length}`)
    to.matched.forEach((match, index) => {
      const matchName = match.name ? String(match.name) : '未命名'
      console.log(`   ${index + 1}. ${match.path} [name: ${matchName}]`)
    })
    
    setWorktab(to)
    setPageTitle(to)
    next()
    return
  }

  // 5. 未匹配到路由，跳转到 404
  console.warn(`⚠️ 路由未匹配: ${to.path}`)
  console.log('   尝试匹配的路由列表:')
  const allRoutes = router.getRoutes()
  allRoutes.forEach(route => {
    if (route.path.includes(':') || route.path.includes('*')) {
      console.log(`   - ${route.path} (动态路由)`)
    }
  })
  
  next({ name: 'Exception404' })
}

/**
 * 处理登录状态
 * @returns true 表示可以继续，false 表示已处理跳转
 */
function handleLoginStatus(
  to: RouteLocationNormalized,
  userStore: ReturnType<typeof useUserStore>,
  next: NavigationGuardNext
): boolean {
  console.log('🔐 开始检查登录状态')
  console.log(`   目标路径: ${to.path}`)
  console.log(`   是否已登录: ${userStore.isLogin}`)
  console.log(`   是否是登录页: ${to.path === RoutesAlias.Login}`)
  console.log(`   是否是静态路由: ${isStaticRoute(to.path)}`)

  // 已登录或访问登录页或静态路由，直接放行
  if (userStore.isLogin || to.path === RoutesAlias.Login || isStaticRoute(to.path)) {
    console.log('✅ 登录检查通过，允许访问')
    return true
  }

  // 未登录且访问需要权限的页面，跳转到登录页并携带 redirect 参数
  console.log('❌ 未登录且访问需要权限的页面，跳转到登录页')
  userStore.logOut()
  next({
    name: 'Login',
    query: { redirect: to.fullPath }
  })
  return false
}

/**
 * 检查路由是否为静态路由
 */
function isStaticRoute(path: string): boolean {
  console.log(`🔍 检查是否为静态路由: ${path}`)
  
  const checkRoute = (routes: any[], targetPath: string, depth = 0): boolean => {
    const indent = '  '.repeat(depth)
    
    return routes.some((route, index) => {
      console.log(`${indent}检查路由 ${index + 1}: ${route.path}`)
      
      // 处理动态路由参数匹配
      const routePath = route.path
      const pattern = routePath.replace(/:[^/]+/g, '[^/]+').replace(/\*/g, '.*')
      const regex = new RegExp(`^${pattern}$`)
      
      console.log(`${indent}  正则表达式: ${regex}`)
      console.log(`${indent}  是否匹配: ${regex.test(targetPath)}`)
      
      if (regex.test(targetPath)) {
        console.log(`${indent}  ✅ 匹配成功!`)
        return true
      }
      
      if (route.children && route.children.length > 0) {
        console.log(`${indent}  检查子路由...`)
        return checkRoute(route.children, targetPath, depth + 1)
      }
      
      return false
    })
  }

  const result = checkRoute(staticRoutes, path)
  console.log(`🔍 检查结果: ${result ? '是静态路由' : '不是静态路由'}`)
  return result
}

/**
 * 处理动态路由注册
 */
async function handleDynamicRoutes(
  to: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  console.log('🔄 开始处理动态路由注册')
  console.log(`   目标路径: ${to.path}`)

  // 显示 loading
  pendingLoading = true
  loadingService.showLoading()
  console.log('⏳ Loading 已显示')

  try {
    // 1. 获取用户信息
    console.log('📋 步骤 1: 获取用户信息')
    await fetchUserInfo()

    // 2. 获取菜单数据
    console.log('📋 步骤 2: 获取菜单数据')
    const menuList = await menuProcessor.getMenuList()
    console.log('📊 获取到的菜单数据:')
    console.log(`   菜单数量: ${menuList?.length || 0}`)
    if (menuList) {
      menuList.forEach((menu, index) => {
        const menuName = menu.name ? String(menu.name) : '未命名'
        console.log(`   ${index + 1}. ${menuName} (${menu.path}) - 组件: ${menu.component}`)
        if (menu.children && menu.children.length > 0) {
          menu.children.forEach((child, childIndex) => {
            const childName = child.name ? String(child.name) : '未命名'
            console.log(`       ${childIndex + 1}. ${childName} (${child.path}) - 组件: ${child.component}`)
          })
        }
      })
    }

    // 3. 验证菜单数据
    console.log('📋 步骤 3: 验证菜单数据')
    if (!menuProcessor.validateMenuList(menuList)) {
      throw new Error('获取菜单列表失败，请重新登录')
    }
    console.log('✅ 菜单数据验证通过')

    // 4. 注册动态路由
    console.log('📋 步骤 4: 注册动态路由')
    const removeFns = routeRegistry?.register(menuList) || []
    console.log(`   清理函数数量: ${removeFns.length}`)
    
    // 调试是否注册成功
    console.log('🔍 路由注册调试:')
    console.log(`   UserList 是否注册：${router.hasRoute('UserList')}`)
    
    // 打印当前所有路由
    const currentRoutes = router.getRoutes()
    console.log(`   注册后总路由数: ${currentRoutes.length}`)
    console.log('   路由列表详情:')
    currentRoutes.forEach((route, index) => {
      const routeName = route.name ? String(route.name) : '未命名'
      console.log(`   ${index + 1}. ${route.path} [name: ${routeName}] [meta: ${JSON.stringify(route.meta || {})}]`)
      if (route.children && route.children.length > 0) {
        route.children.forEach((child, childIndex) => {
          const childName = child.name ? String(child.name) : '未命名'
          console.log(`       ${childIndex + 1}. ${child.path} [name: ${childName}]`)
        })
      }
    })

    // 5. 保存菜单数据到 store
    console.log('📋 步骤 5: 保存菜单数据到 store')
    const menuStore = useMenuStore()
    menuStore.setMenuList(menuList)
    menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])
    console.log(`   菜单 store 中的菜单数: ${menuStore.menuList.length}`)

    // 6. 保存 iframe 路由
    console.log('📋 步骤 6: 保存 iframe 路由')
    IframeRouteManager.getInstance().save()

    // 7. 验证工作标签页
    console.log('📋 步骤 7: 验证工作标签页')
    useWorktabStore().validateWorktabs(router)

    // 8. 验证目标路径权限
    console.log('📋 步骤 8: 验证目标路径权限')
    const { homePath } = useCommon()
    console.log(`   首页路径: ${homePath.value}`)
    
    const { path: validatedPath, hasPermission } = RoutePermissionValidator.validatePath(
      to.path,
      menuList,
      homePath.value || '/'
    )
    console.log(`   权限验证结果: ${hasPermission ? '有权限' : '无权限'}`)
    console.log(`   验证后路径: ${validatedPath}`)

    // 9. 重新导航到目标路由
    if (!hasPermission) {
      // 无权限访问，跳转到首页
      console.warn(`⚠️ 用户无权限访问路径: ${to.path}，跳转到首页`)
      closeLoading()
      
      next({
        path: validatedPath,
        replace: true
      })
    } else {
      // 有权限，正常导航
      console.log('✅ 有权限访问，正常导航')
      next({
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true
      })
    }
  } catch (error) {
    console.error('❌ [RouteGuard] 动态路由注册失败:', error)

    // 401 错误：axios 拦截器已处理退出登录，取消当前导航
    if (isUnauthorizedError(error)) {
      console.log('🔐 401 未授权错误，取消导航')
      closeLoading()
      next(false)
      return
    }

    // 404 错误：接口不存在，标记路由已注册避免重复请求
    if (isNotFoundError(error)) {
      console.error('❌ 接口返回 404，请检查后端接口配置')
      routeRegistry?.markAsRegistered()
      closeLoading()
      next({ name: 'Exception404' })
      return
    }

    // 其他错误：跳转到 500 页面
    console.error('❌ 其他错误，跳转到 500 页面')
    next({ name: 'Exception500' })
  }
}

/**
 * 获取用户信息
 *
 * 注意：每次动态路由注册时都会重新获取用户信息，确保数据最新
 * 这样可以避免以下问题：
 * 1. 用户信息过期但仍使用 localStorage 中的旧数据
 * 2. 权限变更后不能及时更新
 * 3. 用户信息在后台被修改后前端不同步
 */
async function fetchUserInfo(): Promise<void> {
  console.log('👤 开始获取用户信息')
  const userStore = useUserStore()
  const data = await fetchGetUserInfo()
  console.log('📋 获取到的用户信息:', data)
  userStore.setUserInfo(data)
  
  // 检查并清理工作台标签页（如果是不同用户登录）
  console.log('🔄 检查并清理工作台标签页')
  userStore.checkAndClearWorktabs()
  console.log('✅ 用户信息获取完成')
}

/**
 * 重置路由相关状态
 */
export function resetRouterState(delay: number): void {
  console.log(`🔄 重置路由状态，延迟: ${delay}ms`)
  setTimeout(() => {
    console.log('🗑️ 开始清理路由状态')
    routeRegistry?.unregister()
    IframeRouteManager.getInstance().clear()

    const menuStore = useMenuStore()
    menuStore.removeAllDynamicRoutes()
    menuStore.setMenuList([])
    console.log('✅ 路由状态重置完成')
  }, delay)
}

/**
 * 处理根路径重定向到首页
 * @returns true 表示已处理跳转，false 表示无需跳转
 */
function handleRootPathRedirect(to: RouteLocationNormalized, next: NavigationGuardNext): boolean {
  console.log(`🔍 检查根路径重定向: ${to.path}`)
  
  if (to.path !== '/') {
    console.log('✅ 非根路径，无需重定向')
    return false
  }

  console.log('📍 当前是根路径，检查是否需要重定向到首页')
  const { homePath } = useCommon()
  console.log(`   配置的首页路径: ${homePath.value}`)
  
  if (homePath.value && homePath.value !== '/') {
    console.log(`🔄 重定向到首页: ${homePath.value}`)
    next({ path: homePath.value, replace: true })
    return true
  }

  console.log('✅ 首页路径未配置或就是根路径，无需重定向')
  return false
}

/**
 * 判断是否为未授权错误（401）
 */
function isUnauthorizedError(error: unknown): boolean {
  const result = isHttpError(error) && error.code === ApiStatus.unauthorized
  if (result) {
    console.log('🔐 检测到 401 未授权错误')
  }
  return result
}

/**
 * 判断是否为 404 错误
 */
function isNotFoundError(error: unknown): boolean {
  const result = isHttpError(error) && error.code === ApiStatus.notFound
  if (result) {
    console.log('🔍 检测到 404 错误')
  }
  return result
}
/**
 * 组件加载器
 *
 * 负责动态加载 Vue 组件
 *
 * @module router/core/ComponentLoader
 * @author FuSemi
 */

import { h } from 'vue'

export class ComponentLoader {
  private modules: Record<string, () => Promise<any>>

  constructor() {
    // 动态导入 views 目录下所有 .vue 组件
    this.modules = import.meta.glob('../../views/**/*.vue')
    
    // 🔍 调试：查看有哪些模块
    console.log('🔍 ComponentLoader 加载的模块:')
    const moduleKeys = Object.keys(this.modules)
    console.log('总模块数:', moduleKeys.length)
    
    // 查找 user 相关的模块
    const userModules = moduleKeys.filter(path => 
      path.toLowerCase().includes('user')
    )
    console.log('User 相关模块:', userModules)
  }

  load(componentPath: string): () => Promise<any> {
    console.log('📦 开始加载组件:', componentPath)
    
    if (!componentPath) {
      return this.createEmptyComponent()
    }

    // 🔍 验证路径
    console.log('🔍 原始路径:', componentPath)
    
    // 构建可能的路径
    const fullPath = `../../views${componentPath}.vue`
    const fullPathWithIndex = `../../views${componentPath}/index.vue`
    
    console.log('🔍 尝试路径1:', fullPath, '存在:', !!this.modules[fullPath])
    console.log('🔍 尝试路径2:', fullPathWithIndex, '存在:', !!this.modules[fullPathWithIndex])

    // 先尝试直接路径，再尝试添加/index的路径
    const module = this.modules[fullPath] || this.modules[fullPathWithIndex]

    if (!module) {
      console.error('❌ 未找到组件，可用的模块有:')
      // 显示所有模块
      Object.keys(this.modules).forEach(key => {
        console.log('   -', key)
      })
      return this.createErrorComponent(componentPath)
    }

    console.log('✅ 成功找到组件')
    return module
  }

  /**
   * 加载布局组件
   */
  loadLayout(): () => Promise<any> {
    return () => import('@/views/index/index.vue')
  }

  /**
   * 加载 iframe 组件
   */
  loadIframe(): () => Promise<any> {
    return () => import('@/views/outside/Iframe.vue')
  }

  /**
   * 创建空组件
   */
  private createEmptyComponent(): () => Promise<any> {
    return () =>
      Promise.resolve({
        render() {
          return h('div', {})
        }
      })
  }

  /**
   * 创建错误提示组件
   */
  private createErrorComponent(componentPath: string): () => Promise<any> {
    return () =>
      Promise.resolve({
        render() {
          return h('div', { class: 'route-error' }, `组件未找到: ${componentPath}`)
        }
      })
  }
}

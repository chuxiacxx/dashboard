package com.fusemi.dashboard.config;

import org.springframework.context.annotation.Configuration;

/**
 * 数据初始化配置
 *
 * 移除所有静态数据初始化，数据完全通过以下方式管理：
 * - 用户、角色、菜单：通过系统管理界面或SQL脚本手动创建
 * - 业务数据：通过数据导入功能从Excel/CSV文件导入
 *
 * 如果需要重新启用自动初始化，可参考之前的实现历史。
 */
@Configuration
public class DataInitializer {
    // 静态数据初始化已移除
    // 数据应通过以下方式管理：
    // 1. 系统管理界面 - 创建用户、角色、菜单
    // 2. 数据导入功能 - 导入业务数据
    // 3. SQL脚本 - 数据库管理员手动执行
}

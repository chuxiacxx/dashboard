<template>
  <div class="art-card p-4 h-105">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">月度发货数据</h3>
      <el-select v-model="yearFilter" size="small" placeholder="选择年份" style="width: 120px">
        <el-option label="2024年" value="2024"></el-option>
        <el-option label="2023年" value="2023"></el-option>
      </el-select>
    </div>
    
    <!-- 使用新的混合图表组件 -->
    <ArtLineBarChart
      height="13.7rem"
      :bar-data="salesData"
      :line-data="ordersData"
      :x-axis-data="months"
      :bar-colors="['#409EFF']"
      :line-colors="['#67C23A']"
      :show-legend="false"
      :show-tooltip="true"
      :show-axis-label="true"
      :show-axis-line="true"
      :show-split-line="false"
      :bar-width="'40%'"
      :line-width="2"
      :show-symbol="true"
      :smooth="false"
      :tooltip-formatter="tooltipFormatter"
    />
    
    <!-- 图例和统计信息 -->
    <div class="mt-4">
      <div class="flex justify-center space-x-6 mb-3">
        <div class="flex items-center">
          <div class="w-3 h-3 bg-blue-400 mr-2"></div>
          <span class="text-sm text-gray-600">销售额</span>
        </div>
        <div class="flex items-center">
          <div class="w-3 h-3 bg-green-400 mr-2 rounded-full"></div>
          <span class="text-sm text-gray-600">订单数量</span>
        </div>
      </div>
      
      <!-- 统计数据 -->
      <div class="grid grid-cols-2 gap-4 text-center">
        <div>
          <div class="text-sm text-gray-600">本月销售额</div>
          <div class="text-lg font-bold text-blue-500">¥{{ currentMonthSales }}万</div>
        </div>
        <div>
          <div class="text-sm text-gray-600">本月订单数</div>
          <div class="text-lg font-bold text-green-500">{{ currentMonthOrders }}单</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import { fetchShipmentList } from '@/api/dashboard'

const yearFilter = ref('2024')

// 月度数据 - 从API加载
const shipmentData = ref<{ month: string; sales: number; orders: number }[]>([])

// 销售额数据（万元）
const salesData = computed(() => {
  return shipmentData.value.map(item => Math.round(item.sales / 10000))
})

// 订单数量数据（作为折线图数据）
const ordersData = computed(() => {
  return shipmentData.value.map(item => item.orders)
})

// 月份数据
const months = computed(() => {
  return shipmentData.value.map(item => item.month)
})

// 本月数据
const currentMonthSales = computed(() => {
  const last = salesData.value[salesData.value.length - 1]
  return last || 0
})

const currentMonthOrders = computed(() => {
  const last = ordersData.value[ordersData.value.length - 1]
  return last || 0
})

// 加载数据
const loadShipmentData = async () => {
  try {
    const res: any = await fetchShipmentList({ current: 1, size: 100 })
    if (res && res.code === 200 && res.data && res.data.records) {
      const records = res.data.records
      // 按月份分组统计
      const monthMap = new Map<string, { sales: number; orders: number }>()
      records.forEach((record: any) => {
        const month = record.shipDate ? record.shipDate.substring(0, 7) : '未知'
        const existing = monthMap.get(month) || { sales: 0, orders: 0 }
        existing.sales += Number(record.amount || 0)
        existing.orders += 1
        monthMap.set(month, existing)
      })
      // 排序并更新数据
      const sortedMonths = Array.from(monthMap.keys()).sort()
      shipmentData.value = sortedMonths.map(m => ({
        month: m.substring(5) + '月',
        sales: monthMap.get(m)?.sales || 0,
        orders: monthMap.get(m)?.orders || 0
      }))
    }
  } catch (error) {
    console.error('Failed to load shipment data:', error)
  }
}

// Tooltip 格式化函数
const tooltipFormatter = {
  formatter: (params: any) => {
    // 处理多个系列的 tooltip
    if (Array.isArray(params)) {
      const barParam = params.find(p => p.seriesType === 'bar')
      const lineParam = params.find(p => p.seriesType === 'line')

      if (barParam && lineParam) {
        const index = barParam.dataIndex
        const salesItem = shipmentData.value[index]

        return `
          <div style="padding: 8px;">
            <div style="font-weight: 600; margin-bottom: 6px;">${salesItem.month}</div>
            <div style="display: flex; align-items: center; margin-bottom: 4px;">
              <div style="width: 8px; height: 8px; background: #409EFF; margin-right: 8px;"></div>
              <span>销售额: </span>
              <span style="font-weight: 600; color: #409EFF; margin-left: 4px;">¥${(salesItem.sales / 10000).toFixed(1)}万</span>
            </div>
            <div style="display: flex; align-items: center;">
              <div style="width: 8px; height: 8px; background: #67C23A; border-radius: 50%; margin-right: 8px;"></div>
              <span>订单数: </span>
              <span style="font-weight: 600; color: #67C23A; margin-left: 4px;">${salesItem.orders}单</span>
            </div>
          </div>
        `
      }
    }

    // 单个参数的情况
    const index = params.dataIndex
    const salesItem = shipmentData.value[index]

    // 判断是柱状图还是折线图
    const isBar = params.seriesType === 'bar'
    const isLine = params.seriesType === 'line'

    if (isBar) {
      return `
        <div style="padding: 8px;">
          <div style="font-weight: 600; margin-bottom: 6px;">${salesItem.month}</div>
          <div style="display: flex; align-items: center;">
            <div style="width: 8px; height: 8px; background: #409EFF; margin-right: 8px;"></div>
            <span>销售额: </span>
            <span style="font-weight: 600; color: #409EFF; margin-left: 4px;">¥${(salesItem.sales / 10000).toFixed(1)}万</span>
          </div>
        </div>
      `
    } else if (isLine) {
      return `
        <div style="padding: 8px;">
          <div style="font-weight: 600; margin-bottom: 6px;">${salesItem.month}</div>
          <div style="display: flex; align-items: center;">
            <div style="width: 8px; height: 8px; background: #67C23A; border-radius: 50%; margin-right: 8px;"></div>
            <span>订单数: </span>
            <span style="font-weight: 600; color: #67C23A; margin-left: 4px;">${salesItem.orders}单</span>
          </div>
        </div>
      `
    }

    return ''
  }
}

onMounted(() => {
  loadShipmentData()
})
</script>

<style scoped>
.art-card {
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
}

.h-105 {
  height: 105%;
}
</style> 
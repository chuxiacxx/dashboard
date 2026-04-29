<template>
  <div class="art-card p-4" style="height: 36rem;">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">月度发货数据</h3>
      <el-select v-model="yearFilter" size="small" placeholder="选择年份" style="width: 120px">
        <el-option
          v-for="year in availableYears"
          :key="year"
          :label="year + '年'"
          :value="year"
        />
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
          <span class="text-sm text-gray-600">发货额</span>
        </div>
        <div class="flex items-center">
          <div class="w-3 h-3 bg-green-400 mr-2 rounded-full"></div>
          <span class="text-sm text-gray-600">订单数量</span>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="grid grid-cols-2 gap-4 text-center">
        <div>
          <div class="text-sm text-gray-600">本月发货额</div>
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
import { ref, computed, onMounted, watch } from 'vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import { fetchShipmentList, fetchAvailableYears } from '@/api/dashboard'

interface ShipmentItem {
  month: string
  shipmentAmount: number
  orderCount: number
  shippedQuantity: number
}

const yearFilter = ref(new Date().getFullYear())
const availableYears = ref<number[]>([])
const shipmentData = ref<ShipmentItem[]>([])

// 销售额数据（万元）
const salesData = computed(() => {
  return shipmentData.value.map(item => Math.round((item.shipmentAmount || 0) / 10000))
})

// 订单数量数据（作为折线图数据）
const ordersData = computed(() => {
  return shipmentData.value.map(item => item.orderCount || 0)
})

// 月份数据
const months = computed(() => {
  return shipmentData.value.map(item => {
    const month = item.month.split('-')[1]
    return parseInt(month) + '月'
  })
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
const loadData = async () => {
  try {
    const startDate = `${yearFilter.value}-01-01`
    const endDate = `${yearFilter.value}-12-31`
    const res: any = await fetchShipmentList({ startDate, endDate })
    if (res && res.code === 200 && res.data) {
      shipmentData.value = res.data
    }
  } catch (error) {
    console.error('Failed to load shipment data:', error)
    shipmentData.value = []
  }
}

// 加载可用年份
const loadYears = async () => {
  try {
    const res: any = await fetchAvailableYears()
    if (res && res.code === 200 && res.data) {
      availableYears.value = res.data
      if (availableYears.value.length > 0 && !availableYears.value.includes(yearFilter.value)) {
        yearFilter.value = availableYears.value[availableYears.value.length - 1]
      }
    }
  } catch (error) {
    console.error('Failed to load years:', error)
    availableYears.value = [new Date().getFullYear()]
  }
}

// Tooltip 格式化函数
const tooltipFormatter = {
  formatter: (params: any) => {
    if (Array.isArray(params)) {
      const barParam = params.find(p => p.seriesType === 'bar')
      const lineParam = params.find(p => p.seriesType === 'line')

      if (barParam && lineParam) {
        const index = barParam.dataIndex
        const item = shipmentData.value[index]

        return `
          <div style="padding: 8px;">
            <div style="font-weight: 600; margin-bottom: 6px;">${item.month}</div>
            <div style="display: flex; align-items: center; margin-bottom: 4px;">
              <div style="width: 8px; height: 8px; background: #409EFF; margin-right: 8px;"></div>
              <span>发货额: </span>
              <span style="font-weight: 600; color: #409EFF; margin-left: 4px;">
                ¥${((item.shipmentAmount || 0) / 10000).toFixed(1)}万
              </span>
            </div>
            <div style="display: flex; align-items: center;">
              <div style="width: 8px; height: 8px; background: #67C23A; border-radius: 50%; margin-right: 8px;"></div>
              <span>订单数: </span>
              <span style="font-weight: 600; color: #67C23A; margin-left: 4px;">
                ${item.orderCount || 0}单
              </span>
            </div>
          </div>
        `
      }
    }
    return ''
  }
}

// 监听年份变化
watch(yearFilter, () => {
  loadData()
})

onMounted(() => {
  loadYears()
  loadData()
})
</script>

<style scoped>
.art-card {
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
}
</style>

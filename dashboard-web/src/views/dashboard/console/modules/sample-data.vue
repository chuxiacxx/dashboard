<template>
  <div class="art-card p-4 flex flex-col" style="height: 36rem;">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">月度订单分析</h3>
      <div class="flex space-x-2">
        <el-select v-model="yearFilter" size="small" placeholder="年份" style="width: 100px">
          <el-option
            v-for="year in availableYears"
            :key="year"
            :label="year + '年'"
            :value="year"
          />
        </el-select>
        <el-select v-model="dataType" size="small" placeholder="数据类型" style="width: 140px">
          <el-option label="订单金额" value="amount"></el-option>
          <el-option label="订单数量" value="count"></el-option>
          <el-option label="发货金额" value="shipment"></el-option>
        </el-select>
      </div>
    </div>

    <!-- 单系列柱状图 -->
    <div style="height: 180px;">
      <ArtBarChart
        height="100%"
        :data="chartData"
        :xAxisData="chartLabels"
        :colors="chartColors"
        :showLegend="false"
        :barWidth="'50%'"
        :showTooltip="true"
        :tooltipFormatter="tooltipFormatter"
        :showAxisLine="true"
        :showSplitLine="false"
        :showAxisLabel="true"
      />
    </div>

    <!-- 月度汇总表格 -->
    <div class="mt-4 overflow-y-auto" style="flex: 1; min-height: 0;">
      <h4 class="text-sm font-medium text-gray-600 mb-2">月度汇总统计</h4>
      <el-table :data="monthlySummary" size="small" border style="width: 100%">
        <el-table-column prop="month" label="月份" width="70" />
        <el-table-column prop="orderAmount" label="订单金额" align="right">
          <template #default="{ row }">
            <span class="font-medium text-blue-500">{{ formatAmount(row.orderAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" align="center">
          <template #default="{ row }">
            <span class="font-medium text-green-500">{{ row.orderCount }}笔</span>
          </template>
        </el-table-column>
        <el-table-column prop="shipmentAmount" label="发货金额" align="right">
          <template #default="{ row }">
            <span class="font-medium text-purple-500">{{ formatAmount(row.shipmentAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgOrderAmount" label="均单金额" align="right">
          <template #default="{ row }">
            <span class="font-medium text-orange-500">{{ formatAmount(row.avgOrderAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryRate" label="发货率" align="center">
          <template #default="{ row }">
            <el-tag :type="getRateTagType(row.deliveryRate)" size="small">
              {{ row.deliveryRate }}%
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 底部统计 -->
    <div class="mt-4 pt-4 border-t border-gray-100 flex-shrink-0">
      <div class="grid grid-cols-4 gap-2 text-center">
        <div>
          <div class="text-sm text-gray-600">订单总金额</div>
          <div class="text-lg font-bold text-blue-500">{{ formatAmount(totalStats.totalAmount) }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-600">订单总笔数</div>
          <div class="text-lg font-bold text-green-500">{{ totalStats.totalCount }}笔</div>
        </div>
        <div>
          <div class="text-sm text-gray-600">平均发货率</div>
          <div class="text-lg font-bold text-purple-500">{{ totalStats.avgDeliveryRate }}%</div>
        </div>
        <div>
          <div class="text-sm text-gray-600">月均金额</div>
          <div class="text-lg font-bold text-orange-500">{{ formatAmount(totalStats.avgMonthlyAmount) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'
import { fetchSalesList, fetchShipmentList, fetchAvailableYears } from '@/api/dashboard'

const yearFilter = ref(new Date().getFullYear())
const dataType = ref('amount')
const availableYears = ref<number[]>([])

interface MonthlyData {
  month: string
  orderAmount: number
  orderCount: number
  shipmentAmount: number
  avgOrderAmount: number
  deliveryRate: number
}

const monthlyData = ref<MonthlyData[]>([])

// 图表数据（根据选择的数据类型）
const chartData = computed(() => {
  if (dataType.value === 'amount') {
    return monthlySummary.value.map(item => Math.round((item.orderAmount || 0) / 10000))
  } else if (dataType.value === 'count') {
    return monthlySummary.value.map(item => item.orderCount || 0)
  } else if (dataType.value === 'shipment') {
    return monthlySummary.value.map(item => Math.round((item.shipmentAmount || 0) / 10000))
  }
  return []
})

// 图表标签（月份）
const chartLabels = computed(() => {
  return monthlySummary.value.map(item => item.month)
})

// 图表颜色
const chartColors = computed(() => {
  if (dataType.value === 'amount') {
    return ['#409EFF']
  } else if (dataType.value === 'count') {
    return ['#67C23A']
  } else if (dataType.value === 'shipment') {
    return ['#9C27B0']
  }
  return ['#409EFF']
})

// 月度汇总数据
const monthlySummary = computed(() => monthlyData.value)

// 总统计数据
const totalStats = computed(() => {
  const totalAmount = monthlySummary.value.reduce((sum, item) => sum + (item.orderAmount || 0), 0)
  const totalCount = monthlySummary.value.reduce((sum, item) => sum + (item.orderCount || 0), 0)
  const totalShipment = monthlySummary.value.reduce((sum, item) => sum + (item.shipmentAmount || 0), 0)
  const avgDeliveryRate = totalAmount > 0 ? Math.round((totalShipment / totalAmount) * 100) : 0
  const avgMonthlyAmount = monthlySummary.value.length > 0 ? Math.round(totalAmount / monthlySummary.value.length) : 0

  return {
    totalAmount,
    totalCount,
    avgDeliveryRate,
    avgMonthlyAmount
  }
})

// 格式化金额
const formatAmount = (amount: number) => {
  if (!amount) return '¥0'
  if (amount >= 10000) {
    return '¥' + (amount / 10000).toFixed(2) + '万'
  }
  return '¥' + amount.toFixed(0)
}

// 加载数据
const loadData = async () => {
  try {
    const startDate = `${yearFilter.value}-01-01`
    const endDate = `${yearFilter.value}-12-31`
    const [salesRes, shipmentRes] = await Promise.all([
      fetchSalesList({ startDate, endDate }),
      fetchShipmentList({ startDate, endDate })
    ])

    const salesData = salesRes && (salesRes as any).code === 200 ? (salesRes as any).data : []
    const shipmentData = shipmentRes && (shipmentRes as any).code === 200 ? (shipmentRes as any).data : []

    // 合并数据
    monthlyData.value = salesData.map((item: any) => {
      const shipment = shipmentData.find((s: any) => s.month === item.month)
      return {
        month: item.month,
        orderAmount: item.salesAmount || 0,
        orderCount: item.orderCount || 0,
        shipmentAmount: shipment ? (shipment.shipmentAmount || 0) : 0
      }
    })

    // 计算均单金额和发货率
    monthlyData.value = monthlyData.value.map((item: any) => ({
      ...item,
      avgOrderAmount: item.orderCount > 0 ? Math.round(item.orderAmount / item.orderCount) : 0,
      deliveryRate: item.orderAmount > 0 ? Math.round((item.shipmentAmount / item.orderAmount) * 100) : 0
    }))
  } catch (error) {
    console.error('Failed to load data:', error)
    monthlyData.value = []
  }
}

// Tooltip 格式化函数
const tooltipFormatter = computed(() => {
  return {
    trigger: 'axis',
    axisPointer: {
      type: 'line'
    },
    formatter: (params: any) => {
      if (Array.isArray(params)) {
        const param = params[0]
        const monthIndex = param.dataIndex
        const monthData = monthlySummary.value[monthIndex]

        let title = ''
        let value = ''
        let unit = ''

        if (dataType.value === 'amount') {
          title = '订单金额'
          value = formatAmount(monthData.orderAmount)
        } else if (dataType.value === 'count') {
          title = '订单数量'
          value = monthData.orderCount.toString()
          unit = '笔'
        } else if (dataType.value === 'shipment') {
          title = '发货金额'
          value = formatAmount(monthData.shipmentAmount)
        }

        return `
          <div style="padding: 8px;">
            <div style="font-weight: 600; margin-bottom: 6px;">${param.name} - ${title}</div>
            <div style="display: flex; align-items: center; margin-bottom: 4px;">
              <div style="width: 8px; height: 8px; background: ${param.color}; margin-right: 8px;"></div>
              <span>${title}: </span>
              <span style="font-weight: 600; margin-left: 4px;">${value}${unit}</span>
            </div>
            <div style="color: #666; font-size: 12px; margin-top: 6px;">
              <div>订单数: <span style="color: #67C23A; font-weight: 500;">${monthData.orderCount}笔</span></div>
              <div>发货率: <span style="color: #E6A23C; font-weight: 500;">${monthData.deliveryRate}%</span></div>
            </div>
          </div>
        `
      }
      return ''
    }
  }
})

// 发货率标签类型
const getRateTagType = (rate: number) => {
  if (rate >= 80) return 'success'
  if (rate >= 60) return 'warning'
  return 'danger'
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

// 监听筛选变化
watch([yearFilter, dataType], () => {
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

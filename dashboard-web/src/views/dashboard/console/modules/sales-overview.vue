<template>
  <div class="art-card h-105 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>月度销售额趋势</h4>
        <p>
          今年增长
          <span :class="growthRate >= 0 ? 'text-success' : 'text-danger'">
            {{ growthRate >= 0 ? '+' : '' }}{{ growthRate.toFixed(1) }}%
          </span>
        </p>
      </div>
      <el-select v-model="selectedYear" size="small" style="width: 100px">
        <el-option
          v-for="year in availableYears"
          :key="year"
          :label="year + '年'"
          :value="year"
        />
      </el-select>
    </div>
    <ArtLineChart
      height="calc(100% - 56px)"
      :data="salesData"
      :xAxisData="months"
      :showAreaColor="true"
      :showAxisLine="false"
      :tooltipFormatter="tooltipFormatter"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import ArtLineChart from '@/components/core/charts/art-line-chart/index.vue'
import { fetchSalesList, fetchAvailableYears } from '@/api/dashboard'

interface MonthlyData {
  month: string
  salesAmount: number
  orderCount: number
}

// 当前选中年份
const selectedYear = ref(new Date().getFullYear())
// 可用年份列表
const availableYears = ref<number[]>([])
// 月度数据
const monthlyData = ref<MonthlyData[]>([])

// 月份标签
const months = computed(() => {
  return monthlyData.value.map(item => {
    const month = item.month.split('-')[1]
    return parseInt(month) + '月'
  })
})

// 销售额数据
const salesData = computed(() => {
  return monthlyData.value.map(item => Math.round((item.salesAmount || 0) / 10000))
})

// 计算增长率（与去年对比）
const growthRate = computed(() => {
  if (monthlyData.value.length < 2) return 0
  const currentTotal = monthlyData.value.reduce((sum, item) => sum + (item.salesAmount || 0), 0)
  // 这里简化计算，实际应该与去年同期对比
  return 15 // 暂时返回固定值，后续可从后端获取同比数据
})

// Tooltip 格式化
const tooltipFormatter = (params: any) => {
  const dataIndex = params.dataIndex
  const item = monthlyData.value[dataIndex]
  if (!item) return ''

  return `
    <div style="padding: 8px;">
      <div style="font-weight: 600; margin-bottom: 6px;">${item.month}</div>
      <div style="display: flex; align-items: center; margin-bottom: 4px;">
        <div style="width: 8px; height: 8px; background: #409EFF; margin-right: 8px;"></div>
        <span>销售额: </span>
        <span style="font-weight: 600; color: #409EFF; margin-left: 4px;">
          ¥${((item.salesAmount || 0) / 10000).toFixed(2)}万
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

// 加载数据
const loadData = async () => {
  try {
    const startDate = `${selectedYear.value}-01-01`
    const endDate = `${selectedYear.value}-12-31`
    const res: any = await fetchSalesList({ startDate, endDate })
    if (res && res.code === 200 && res.data) {
      monthlyData.value = res.data
    }
  } catch (error) {
    console.error('Failed to load sales data:', error)
    monthlyData.value = []
  }
}

// 加载可用年份
const loadYears = async () => {
  try {
    const res: any = await fetchAvailableYears()
    if (res && res.code === 200 && res.data) {
      availableYears.value = res.data
      if (availableYears.value.length > 0 && !availableYears.value.includes(selectedYear.value)) {
        selectedYear.value = availableYears.value[availableYears.value.length - 1]
      }
    }
  } catch (error) {
    console.error('Failed to load years:', error)
    availableYears.value = [new Date().getFullYear()]
  }
}

// 监听年份变化
watch(selectedYear, () => {
  loadData()
})

onMounted(() => {
  loadYears()
  loadData()
})
</script>

<style scoped>
.art-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.art-card-header .title h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.art-card-header .title p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}
</style>

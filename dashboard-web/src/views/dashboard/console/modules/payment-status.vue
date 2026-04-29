<template>
  <div class="art-card p-4" style="height: 36rem;">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">销售员业绩排行</h3>
      <div class="flex space-x-2">
        <el-select v-model="yearFilter" size="small" style="width: 90px">
          <el-option
            v-for="year in availableYears"
            :key="year"
            :label="year + '年'"
            :value="year"
          />
        </el-select>
        <el-select v-model="monthFilter" size="small" style="width: 80px">
          <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </el-select>
      </div>
    </div>

    <!-- 使用堆叠柱状图显示所有销售员 -->
    <ArtBarChart
      height="13.7rem"
      :data="stackedData"
      :xAxisData="salesNames"
      :colors="['#409EFF', '#FFAF20']"
      :showAxisLine="true"
      :showSplitLine="false"
      :showAxisLabel="true"
      :showTooltip="true"
      :stack="true"
      barWidth="26%"
    />

    <!-- 前三名统计信息 -->
    <div class="mt-4">
      <p class="text-sm text-gray-600 mb-3">
        Top 3 销售员，共
        <span class="font-semibold">{{ formatAmount(totalSalesAmount) }}</span>
      </p>
      <div class="grid grid-cols-3 gap-3">
        <div v-for="(item, index) in top3Sales" :key="item.name" class="text-center">
          <div class="w-8 h-8 rounded-full flex items-center justify-center mx-auto mb-2"
               :class="index === 0 ? 'bg-blue-100 text-blue-500' : 'bg-blue-50 text-blue-400'">
            {{ index + 1 }}
          </div>
          <p class="text-sm font-medium truncate">{{ item.name }}</p>
          <p class="text-xs text-gray-500">{{ formatAmount(item.actualAmount) }}</p>
          <p class="text-xs" :class="getCompletionClass(item.completionRate)">
            完成率 {{ item.completionRate }}%
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'
import { fetchSalespersonRanking, fetchAvailableYears } from '@/api/dashboard'

interface SalesPersonData {
  name: string
  actualAmount: number
  targetAmount: number
  shippedAmount: number
  completionRate: number
  rank: number
}

const yearFilter = ref(new Date().getFullYear())
const monthFilter = ref(new Date().getMonth() + 1)
const availableYears = ref<number[]>([])
const salesData = ref<SalesPersonData[]>([])

// 已回款金额（实际销售额）
const actualPayments = computed(() => {
  return salesData.value.map(item => Math.round((item.actualAmount || 0) / 10000))
})

// 目标回款金额
const targetPayments = computed(() => {
  return salesData.value.map(item => Math.round((item.targetAmount || 0) / 10000))
})

// 销售人员姓名（X轴标签）
const salesNames = computed(() => {
  return salesData.value.map(item => item.name)
})

// 总回款金额
const totalSalesAmount = computed(() => {
  return salesData.value.reduce((sum, item) => sum + (item.actualAmount || 0), 0)
})

// 前三名销售人员
const top3Sales = computed(() => {
  return salesData.value.slice(0, 3)
})

// 堆叠数据：已回款金额和目标回款金额
const stackedData = computed(() => {
  return [
    { name: '实际金额', data: actualPayments.value },
    { name: '目标金额', data: targetPayments.value }
  ]
})

// 格式化金额
const formatAmount = (amount: number) => {
  if (!amount) return '¥0'
  if (amount >= 10000) {
    return '¥' + (amount / 10000).toFixed(2) + '万'
  }
  return '¥' + amount.toFixed(0)
}

// 完成率样式
const getCompletionClass = (rate: number) => {
  if (rate >= 100) return 'text-success'
  if (rate >= 80) return 'text-warning'
  return 'text-danger'
}

// 加载销售数据
const loadSalesData = async () => {
  try {
    const res: any = await fetchSalespersonRanking({
      year: yearFilter.value,
      month: monthFilter.value
    })
    if (res && res.code === 200 && res.data) {
      salesData.value = res.data
    }
  } catch (error) {
    console.error('Failed to load sales data:', error)
    salesData.value = []
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

// 监听筛选变化
watch([yearFilter, monthFilter], () => {
  loadSalesData()
})

onMounted(() => {
  loadYears()
  loadSalesData()
})
</script>

<style scoped>
.art-card {
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
}

.text-success {
  color: #67C23A;
}

.text-warning {
  color: #E6A23C;
}

.text-danger {
  color: #F56C6C;
}
</style>

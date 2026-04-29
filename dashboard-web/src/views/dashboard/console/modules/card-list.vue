<template>
  <ElRow :gutter="20" class="flex">
    <!-- 1. 目标销售额与实际销售额 -->
    <ElCol :xs="24" :sm="12" :md="6">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('SalesDetails')">
        <div class="flex justify-between items-center mb-4">
          <div class="flex items-center">
            <span class="text-g-700 text-sm">销售额</span>
            <ElTag size="small" type="primary" class="ml-2">本月</ElTag>
          </div>
          <div class="size-12.5 rounded-xl flex-cc bg-blue-100">
            <ArtSvgIcon icon="ri:money-dollar-circle-line" class="text-xl text-blue-500" />
          </div>
        </div>

        <div class="flex justify-between items-center mb-4">
          <div class="text-center flex-1">
            <div class="text-xs text-g-600 mb-1">目标</div>
            <div class="text-lg font-semibold">¥{{ formatAmount(summary.salesTarget) }}</div>
          </div>
          <div class="text-center flex-1">
            <div class="text-xs text-g-600 mb-1">实际</div>
            <div class="text-lg font-semibold text-success">¥{{ formatAmount(summary.salesAmount) }}</div>
          </div>
        </div>

        <div class="mt-auto">
          <div class="flex justify-between text-xs text-g-600 mb-1">
            <span>完成率</span>
            <span class="text-primary font-semibold">{{ summary.salesCompletionRate }}%</span>
          </div>
          <ElProgress :percentage="Number(summary.salesCompletionRate)" :show-text="false" :stroke-width="6" />
        </div>
      </div>
    </ElCol>

    <!-- 2. 本月出货金额 -->
    <ElCol :xs="24" :sm="12" :md="6">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('ShipmentDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">出货金额</span>
          <div class="size-12.5 rounded-xl flex-cc bg-green-100">
            <ArtSvgIcon icon="ri:ship-line" class="text-xl text-green-500" />
          </div>
        </div>

        <div class="flex items-end mb-3">
          <ArtCountTo class="text-[26px] font-medium mr-2" :target="Number(summary.shipmentAmount) || 0" :duration="1300" prefix="¥" />
          <span class="text-xs text-g-600 mb-1">(本月)</span>
        </div>

        <div class="flex-c mt-auto">
          <span class="text-xs text-g-600">较上月</span>
          <span class="ml-1 text-xs font-semibold" :class="getGrowthClass(summary.shipmentGrowthRate)">
            <ArtSvgIcon :icon="getGrowthIcon(summary.shipmentGrowthRate)" class="mr-1 inline" />
            {{ formatGrowthRate(summary.shipmentGrowthRate) }}
          </span>
        </div>
      </div>
    </ElCol>

    <!-- 3. 总新增订单金额 -->
    <ElCol :xs="24" :sm="12" :md="6">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('OrderDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">新增订单</span>
          <div class="size-12.5 rounded-xl flex-cc bg-purple-100">
            <ArtSvgIcon icon="ri:file-list-3-line" class="text-xl text-purple-500" />
          </div>
        </div>

        <div class="mb-3">
          <div class="flex items-end mb-2">
            <ArtCountTo class="text-[26px] font-medium mr-2" :target="Number(summary.orderAmount) || 0" :duration="1300" prefix="¥" />
            <span class="text-xs text-g-600 mb-1">(总金额)</span>
          </div>
          <div class="text-sm text-g-700">
            订单数量：<span class="font-semibold text-purple-500">{{ summary.orderCount }}</span> 笔
          </div>
        </div>

        <div class="flex-c mt-auto">
          <span class="text-xs text-g-600">较上月</span>
          <span class="ml-1 text-xs font-semibold" :class="getGrowthClass(summary.orderGrowthRate)">
            <ArtSvgIcon :icon="getGrowthIcon(summary.orderGrowthRate)" class="mr-1 inline" />
            {{ formatGrowthRate(summary.orderGrowthRate) }}
          </span>
        </div>
      </div>
    </ElCol>

    <!-- 4. 开票金额 -->
    <ElCol :xs="24" :sm="12" :md="6">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('InvoiceDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">开票金额</span>
          <div class="size-12.5 rounded-xl flex-cc bg-orange-100">
            <ArtSvgIcon icon="ri:receipt-line" class="text-xl text-orange-500" />
          </div>
        </div>

        <div class="flex items-end mb-3">
          <ArtCountTo class="text-[26px] font-medium mr-2" :target="Number(summary.invoiceAmount) || 0" :duration="1300" prefix="¥" />
          <span class="text-xs text-g-600 mb-1">(本月)</span>
        </div>

        <div class="flex-c mt-auto">
          <span class="text-xs text-g-600">较上月</span>
          <span class="ml-1 text-xs font-semibold" :class="getGrowthClass(summary.invoiceGrowthRate)">
            <ArtSvgIcon :icon="getGrowthIcon(summary.invoiceGrowthRate)" class="mr-1 inline" />
            {{ formatGrowthRate(summary.invoiceGrowthRate) }}
          </span>
        </div>
      </div>
    </ElCol>
  </ElRow>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchDashboardSummary } from '@/api/dashboard'

const router = useRouter()

interface DashboardSummary {
  salesAmount: number
  shipmentAmount: number
  orderAmount: number
  orderCount: number
  invoiceAmount: number
  salesTarget: number
  salesCompletionRate: number
  salesGrowthRate: number
  shipmentGrowthRate: number
  orderGrowthRate: number
  invoiceGrowthRate: number
}

const summary = ref<DashboardSummary>({
  salesAmount: 0,
  shipmentAmount: 0,
  orderAmount: 0,
  orderCount: 0,
  invoiceAmount: 0,
  salesTarget: 0,
  salesCompletionRate: 0,
  salesGrowthRate: 0,
  shipmentGrowthRate: 0,
  orderGrowthRate: 0,
  invoiceGrowthRate: 0
})

// 格式化金额显示
const formatAmount = (amount: number) => {
  if (!amount) return '0'
  return (amount / 10000).toFixed(2) + '万'
}

// 格式化增长率
const formatGrowthRate = (rate: number) => {
  if (!rate) return '0%'
  const sign = rate >= 0 ? '+' : ''
  return `${sign}${rate.toFixed(1)}%`
}

// 获取增长样式类
const getGrowthClass = (rate: number) => {
  if (!rate) return 'text-gray-500'
  return rate >= 0 ? 'text-success' : 'text-danger'
}

// 获取增长图标
const getGrowthIcon = (rate: number) => {
  return rate >= 0 ? 'ri:arrow-up-line' : 'ri:arrow-down-line'
}

// 获取仪表盘数据
const loadDashboardData = async () => {
  try {
    const res: any = await fetchDashboardSummary()
    if (res && res.code === 200 && res.data) {
      summary.value = {
        ...summary.value,
        ...res.data
      }
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

onMounted(() => {
  loadDashboardData()
})

const handleCardClick = (routeName: string) => {
  router.push({
    name: routeName,
    query: { from: routeName.toLowerCase() }
  })
}
</script>

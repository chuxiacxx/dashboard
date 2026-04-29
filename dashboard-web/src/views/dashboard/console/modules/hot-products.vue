<template>
  <div class="art-card p-4" style="height: 36rem;">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">热销产品排行</h3>
      <div class="flex space-x-2">
        <el-select v-model="yearFilter" size="small" placeholder="年份" style="width: 90px">
          <el-option
            v-for="year in availableYears"
            :key="year"
            :label="year + '年'"
            :value="year"
          />
        </el-select>
        <el-select v-model="monthFilter" size="small" placeholder="月份" style="width: 80px">
          <el-option label="全年" :value="0" />
          <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </el-select>
      </div>
    </div>

    <!-- 使用垂直柱状图显示所有产品 -->
    <ArtBarChart
      height="13.7rem"
      barWidth="50%"
      :data="chartData"
      :xAxisData="productNames"
      :colors="chartColors"
      :showAxisLine="true"
      :showSplitLine="false"
      :showAxisLabel="true"
      :showTooltip="true"
      :tooltipFormatter="tooltipFormatter"
    />

    <!-- 前三名统计信息 -->
    <div class="mt-4">
      <p class="text-sm text-gray-600 mb-3">
        Top {{ productList.length }} 热销产品，共
        <span class="font-semibold">{{ totalCount }}</span> 件
      </p>
      <div class="grid grid-cols-3 gap-3">
        <div v-for="(item, index) in top3Products" :key="item.productName" class="text-center">
          <div class="w-8 h-8 rounded-full flex items-center justify-center mx-auto mb-2"
               :class="index === 0 ? 'bg-blue-100 text-blue-500' : 'bg-blue-50 text-blue-400'">
            {{ index + 1 }}
          </div>
          <p class="text-sm font-medium truncate" :title="item.productName">{{ item.productName }}</p>
          <p class="text-xs text-gray-500">{{ formatQuantity(item.quantity) }}件</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'
import { fetchProductRanking, fetchAvailableYears } from '@/api/dashboard'

interface ProductItem {
  productName: string
  specModel: string
  quantity: number
  salesAmount: number
  rank: number
}

const yearFilter = ref(new Date().getFullYear())
const monthFilter = ref(0)
const availableYears = ref<number[]>([])
const productList = ref<ProductItem[]>([])

// 图表数据（所有产品）
const chartData = computed(() => {
  return productList.value.map(item => item.salesAmount / 10000)
})

// 产品名称（用于X轴标签）- 使用序号
const productNames = computed(() => {
  return productList.value.map((_, index) => `TOP${index + 1}`)
})

// 总销量
const totalCount = computed(() => {
  return productList.value.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

// 前三名产品
const top3Products = computed(() => {
  return productList.value.slice(0, 3)
})

// 图表颜色 - 使用统一的蓝色
const chartColors = ref(['#409EFF'])

// 格式化数量
const formatQuantity = (qty: number) => {
  if (qty >= 10000) {
    return (qty / 10000).toFixed(1) + '万'
  }
  return qty.toString()
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
        const index = param.dataIndex
        const product = productList.value[index]
        if (product) {
          return `
            <div style="padding: 8px;">
              <div style="font-weight: 600; margin-bottom: 4px;">${product.productName}</div>
              <div style="color: #666; font-size: 12px; margin-bottom: 2px;">
                型号: ${product.specModel || '-'}
              </div>
              <div>
                销量:
                <span style="font-weight: 600; color: #409EFF;">
                  ${formatQuantity(product.quantity)}件
                </span>
              </div>
              <div>
                金额:
                <span style="font-weight: 600; color: #409EFF;">
                  ¥${(product.salesAmount / 10000).toFixed(2)}万
                </span>
              </div>
              <div style="color: #999; font-size: 12px; margin-top: 2px;">
                排名: TOP${index + 1}
              </div>
            </div>
          `
        }
      }
      return ''
    }
  }
})

// 获取产品数据
const loadProductData = async () => {
  try {
    const params: any = { year: yearFilter.value, limit: 10 }
    if (monthFilter.value > 0) {
      params.month = monthFilter.value
    }
    const res: any = await fetchProductRanking(params)
    if (res && res.code === 200 && res.data) {
      productList.value = res.data
    }
  } catch (error) {
    console.error('Failed to load product data:', error)
    productList.value = []
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
  loadProductData()
})

onMounted(() => {
  loadYears()
  loadProductData()
})
</script>

<style scoped>
.art-card {
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
}
</style>

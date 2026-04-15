<template>
  <div class="art-card p-4 h-105">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">本月热销产品排行</h3>
      <el-select v-model="productFilter" size="small" placeholder="选择分类" style="width: 120px">
        <el-option label="全部产品" value="all"></el-option>
        <el-option label="A类产品" value="A"></el-option>
        <el-option label="B类产品" value="B"></el-option>
      </el-select>
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
      <p class="text-sm text-gray-600 mb-3">Top 10热销产品，共 <span class="font-semibold">{{ totalCount }}</span> 件</p>
      <div class="grid grid-cols-3 gap-3">
        <div v-for="(item, index) in top3Products" :key="item.name" class="text-center">
          <div class="w-8 h-8 rounded-full flex items-center justify-center mx-auto mb-2"
               :class="index === 0 ? 'bg-blue-100 text-blue-500' : 'bg-blue-50 text-blue-400'">
            {{ index + 1 }}
          </div>
          <p class="text-sm font-medium truncate">{{ item.name }}</p>
          <p class="text-xs text-gray-500">{{ item.count }}件</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'
import { fetchSalesList } from '@/api/dashboard'

const productFilter = ref('all')

interface ProductItem {
  name: string
  model: string
  count: number
}

// 产品数据
const productList = ref<ProductItem[]>([])

// 图表数据（所有产品）
const chartData = computed(() => {
  return productList.value.map(item => item.count)
})

// 产品名称（用于X轴标签）- 使用序号
const productNames = computed(() => {
  return productList.value.map((_, index) => `TOP${index + 1}`)
})

// 总销量
const totalCount = computed(() => {
  return productList.value.reduce((sum, item) => sum + item.count, 0)
})

// 前三名产品
const top3Products = computed(() => {
  return productList.value.slice(0, 3)
})

// 图表颜色 - 使用统一的蓝色
const chartColors = ref(['#409EFF'])

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
              <div style="font-weight: 600; margin-bottom: 4px;">${product.name}</div>
              <div style="color: #666; font-size: 12px; margin-bottom: 2px;">型号: ${product.model}</div>
              <div>销量: <span style="font-weight: 600; color: #409EFF;">${product.count}件</span></div>
              <div style="color: #999; font-size: 12px; margin-top: 2px;">排名: TOP${index + 1}</div>
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
    const res: any = await fetchSalesList({ current: 1, size: 100 })
    if (res && res.code === 200 && res.data && res.data.records) {
      // 按产品分组统计销量
      const productMap = new Map<string, ProductItem>()
      res.data.records.forEach((record: any) => {
        const name = record.productName || '未知产品'
        if (productMap.has(name)) {
          productMap.get(name)!.count += record.quantity || 0
        } else {
          productMap.set(name, {
            name: name,
            model: name,
            count: record.quantity || 0
          })
        }
      })
      // 排序并取前10
      productList.value = Array.from(productMap.values())
        .sort((a, b) => b.count - a.count)
        .slice(0, 10)
    }
  } catch (error) {
    console.error('Failed to load product data:', error)
  }
}

onMounted(() => {
  loadProductData()
})
</script>
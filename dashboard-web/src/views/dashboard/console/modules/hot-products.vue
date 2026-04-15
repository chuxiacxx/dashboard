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
import { ref, computed } from 'vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'

const productFilter = ref('all')

// 示例数据
const productList = ref([
  { name: 'SC43N12TFI8_C', model: 'SC43N12TFI8_C', count: 1250 },
  { name: 'SF150R12A6H', model: 'SF150R12A6H', count: 980 },
  { name: 'S3L450R12D6L_C20', model: 'S3L450R12D6L_C20', count: 765 },
  { name: 'SC19N07TFI8_B', model: 'SC19N07TFI8_B', count: 654 },
  { name: 'SC28N12TFI8_C', model: 'SC28N12TFI8_C', count: 543 },
  { name: 'SF200R17E6', model: 'SF200R17E6', count: 432 },
  { name: 'S3L150R07G6', model: 'S3L150R07G6', count: 321 },
  { name: 'SD75R07A6U', model: 'SD75R07A6U', count: 210 },
  { name: 'SF100R17A6', model: 'SF100R17A6', count: 198 },
  { name: 'S3L450R12D6S_C20', model: 'S3L450R12D6S_C20', count: 165 }
])

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
        return `
          <div style="padding: 8px;">
            <div style="font-weight: 600; margin-bottom: 4px;">${product.name}</div>
            <div style="color: #666; font-size: 12px; margin-bottom: 2px;">型号: ${product.model}</div>
            <div>销量: <span style="font-weight: 600; color: #409EFF;">${product.count}件</span></div>
            <div style="color: #999; font-size: 12px; margin-top: 2px;">排名: TOP${index + 1}</div>
          </div>
        `
      }
      return ''
    }
  }
})
</script>
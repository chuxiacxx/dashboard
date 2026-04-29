<template>
  <div class="art-card p-4" style="height: 36rem;">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">本月销售回款完成情况</h3>
      <el-select v-model="productFilter" size="small" placeholder="选择销售人员" style="width: 120px">
        <el-option label="全部销售" value="all"></el-option>
        <el-option label="销售A" value="A"></el-option>
        <el-option label="销售B" value="B"></el-option>
      </el-select>
    </div>
    
    <!-- 使用堆叠柱状图显示销售数据 -->
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
      <p class="text-sm text-gray-600 mb-3">Top 3销售人员，共 <span class="font-semibold">{{ totalSalesCount }}</span> 元</p>
      <div class="grid grid-cols-3 gap-3">
        <div v-for="(item, index) in top3Sales" :key="item.name" class="text-center">
          <div class="w-8 h-8 rounded-full flex items-center justify-center mx-auto mb-2"
               :class="index === 0 ? 'bg-blue-100 text-blue-500' : 'bg-blue-50 text-blue-400'">
            {{ index + 1 }}
          </div>
          <p class="text-sm font-medium truncate">{{ item.name }}</p>
          <p class="text-xs text-gray-500">{{ item.actualAmount }}元</p>
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
const salesData = ref([
  { name: '销售A', actualAmount: 12500, targetAmount: 20000 },
  { name: '销售B', actualAmount: 9800, targetAmount: 15000 },
  { name: '销售C', actualAmount: 7650, targetAmount: 12000 },
  { name: '销售D', actualAmount: 6540, targetAmount: 10000 },
  { name: '销售E', actualAmount: 5430, targetAmount: 8000 },
  { name: '销售F', actualAmount: 4320, targetAmount: 7000 },
  { name: '销售G', actualAmount: 3210, targetAmount: 5000 },
  { name: '销售H', actualAmount: 2100, targetAmount: 3000 },
  { name: '销售I', actualAmount: 1980, targetAmount: 2500 },
  { name: '销售J', actualAmount: 1650, targetAmount: 2000 }
])

// 已回款金额（正向数据）
const actualPayments = computed(() => {
  return salesData.value.map(item => item.actualAmount)
})

// 目标回款金额（负向数据）
const targetPayments = computed(() => {
  return salesData.value.map(item => item.targetAmount)
})

// 销售人员姓名（X轴标签）
const salesNames = computed(() => {
  return salesData.value.map(item => item.name)
})

// 总回款金额
const totalSalesCount = computed(() => {
  return salesData.value.reduce((sum, item) => sum + item.actualAmount, 0)
})

// 前三名销售人员
const top3Sales = computed(() => {
  return salesData.value.slice(0, 3)
})

// 堆叠数据：已回款金额和目标回款金额，堆叠到一起
const stackedData = computed(() => {
  return [
    { name: '已回款金额', data: actualPayments.value },
    { name: '目标金额', data: targetPayments.value }
  ]
})
</script>

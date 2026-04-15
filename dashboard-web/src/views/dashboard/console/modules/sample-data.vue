<template>
  <div class="art-card p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">每月送样数据分析</h3>
      <div class="flex space-x-2">
        <el-select v-model="yearFilter" size="small" placeholder="年份" style="width: 100px">
          <el-option label="2024年" value="2024"></el-option>
          <el-option label="2023年" value="2023"></el-option>
        </el-select>
        <el-select v-model="dataType" size="small" placeholder="数据类型" style="width: 140px">
          <el-option label="送样订单数量" value="sampleCount"></el-option>
          <el-option label="型号数量" value="modelCount"></el-option>
          <el-option label="客户数量" value="customerCount"></el-option>
        </el-select>
      </div>
    </div>
    
    <!-- 单系列柱状图：根据选择展示不同类型数据 -->
    <div style="height: 200px;">
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
    <div class="mt-6">
      <h4 class="text-sm font-medium text-gray-600 mb-3">月度汇总统计</h4>
      <el-table :data="monthlySummary" size="small" border style="width: 100%" max-height="200">
        <el-table-column prop="month" label="月份" width="80" fixed />
        <el-table-column prop="sampleCount" label="送样数量" width="100" align="center">
          <template #default="{ row }">
            <span class="font-medium text-blue-500">{{ row.sampleCount }}件</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数量" width="100" align="center">
          <template #default="{ row }">
            <span class="font-medium text-green-500">{{ row.orderCount }}单</span>
          </template>
        </el-table-column>
        <el-table-column prop="modelCount" label="涉及型号" width="100" align="center">
          <template #default="{ row }">
            <span class="font-medium text-purple-500">{{ row.modelCount }}种</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerCount" label="客户数量" width="100" align="center">
          <template #default="{ row }">
            <span class="font-medium text-orange-500">{{ row.customerCount }}家</span>
          </template>
        </el-table-column>
        <el-table-column prop="conversionRate" label="转化率" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRateTagType(row.conversionRate)" size="small">
              {{ row.conversionRate }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryRate" label="交货率" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRateTagType(row.deliveryRate)" size="small">
              {{ row.deliveryRate }}%
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 底部统计 -->
    <div class="mt-4 pt-4 border-t border-gray-100">
      <div class="grid grid-cols-4 gap-4 text-center">
        <div>
          <div class="text-sm text-gray-600">送样总数</div>
          <div class="text-lg font-bold text-blue-500">{{ totalStats.sampleCount }}件</div>
        </div>
        <div>
          <div class="text-sm text-gray-600">订单总数</div>
          <div class="text-lg font-bold text-green-500">{{ totalStats.orderCount }}单</div>
        </div>
        <div>
          <div class="text-sm text-gray-600">平均转化率</div>
          <div class="text-lg font-bold text-green-500">{{ totalStats.avgConversionRate }}%</div>
        </div>
        <div>
          <div class="text-sm text-gray-600">型号总数</div>
          <div class="text-lg font-bold text-purple-500">{{ totalStats.modelCount }}种</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'

const yearFilter = ref('2024')
const dataType = ref('sampleCount') // 默认显示送样订单数量

// 静态数据 - 每月送样数据（与之前相同）
const sampleData = [
  { month: '1月', model: 'SC43N12TFI8_C', sampleCount: 25, orderCount: 8, deliveryStatus: '已交货', deliveryDate: '2024-01-15', customer: 'ABC电子', remarks: '首批样品' },
  { month: '1月', model: 'SF150R12A6H', sampleCount: 18, orderCount: 12, deliveryStatus: '已交货', deliveryDate: '2024-01-20', customer: 'XYZ科技', remarks: '测试样品' },
  { month: '1月', model: 'S3L450R12D6L_C20', sampleCount: 32, orderCount: 15, deliveryStatus: '已交货', deliveryDate: '2024-01-28', customer: 'DEF制造', remarks: '品质验证' },
  
  { month: '2月', model: 'SC43N12TFI8_C', sampleCount: 28, orderCount: 10, deliveryStatus: '已交货', deliveryDate: '2024-02-10', customer: 'GHI半导体', remarks: '批量样品' },
  { month: '2月', model: 'SF150R12A6H', sampleCount: 22, orderCount: 18, deliveryStatus: '已交货', deliveryDate: '2024-02-18', customer: 'JKL电子', remarks: '认证样品' },
  { month: '2月', model: 'SC19N07TFI8_B', sampleCount: 15, orderCount: 6, deliveryStatus: '已交货', deliveryDate: '2024-02-25', customer: 'MNO科技', remarks: '新产品测试' },
  
  { month: '3月', model: 'SC43N12TFI8_C', sampleCount: 35, orderCount: 22, deliveryStatus: '已交货', deliveryDate: '2024-03-05', customer: 'PQR工业', remarks: '大客户样品' },
  { month: '3月', model: 'SF150R12A6H', sampleCount: 30, orderCount: 25, deliveryStatus: '延期', deliveryDate: '2024-03-25', customer: 'STU电子', remarks: '生产延期' },
  { month: '3月', model: 'S3L450R12D6L_C20', sampleCount: 20, orderCount: 12, deliveryStatus: '已交货', deliveryDate: '2024-03-15', customer: 'VWX制造', remarks: '标准样品' },
  { month: '3月', model: 'SC28N12TFI8_C', sampleCount: 12, orderCount: 5, deliveryStatus: '已交货', deliveryDate: '2024-03-20', customer: 'YZA科技', remarks: '小批量测试' },
  
  { month: '4月', model: 'SC43N12TFI8_C', sampleCount: 40, orderCount: 28, deliveryStatus: '已交货', deliveryDate: '2024-04-08', customer: 'BCD电子', remarks: '战略客户' },
  { month: '4月', model: 'SF200R17E6', sampleCount: 18, orderCount: 10, deliveryStatus: '在途', deliveryDate: '2024-04-25', customer: 'EFG科技', remarks: '新产品推广' },
  { month: '4月', model: 'S3L150R07G6', sampleCount: 25, orderCount: 15, deliveryStatus: '已交货', deliveryDate: '2024-04-18', customer: 'HIJ制造', remarks: '常规样品' },
  
  { month: '5月', model: 'SC43N12TFI8_C', sampleCount: 45, orderCount: 35, deliveryStatus: '已交货', deliveryDate: '2024-05-10', customer: 'KLM电子', remarks: '主力产品' },
  { month: '5月', model: 'SD75R07A6U', sampleCount: 20, orderCount: 12, deliveryStatus: '已交货', deliveryDate: '2024-05-15', customer: 'NOP科技', remarks: '新客户开发' },
  { month: '5月', model: 'SF100R17A6', sampleCount: 15, orderCount: 8, deliveryStatus: '已交货', deliveryDate: '2024-05-22', customer: 'QRS制造', remarks: '紧急样品' },
  
  { month: '6月', model: 'SC43N12TFI8_C', sampleCount: 50, orderCount: 40, deliveryStatus: '已交货', deliveryDate: '2024-06-05', customer: 'TUV电子', remarks: '旺季备货' },
  { month: '6月', model: 'S3L450R12D6S_C20', sampleCount: 22, orderCount: 15, deliveryStatus: '待安排', deliveryDate: '2024-06-30', customer: 'WXY科技', remarks: '排期待定' },
  { month: '6月', model: 'SF150R12A6H', sampleCount: 30, orderCount: 25, deliveryStatus: '已交货', deliveryDate: '2024-06-12', customer: 'ZAB制造', remarks: '批量订单样品' }
]

// 月份列表（排序）
const months = computed(() => {
  return Array.from(new Set(sampleData.map(item => item.month))).sort((a, b) => {
    const monthOrder = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    return monthOrder.indexOf(a) - monthOrder.indexOf(b)
  })
})

// 月度汇总数据
const monthlySummary = computed(() => {
  return months.value.map(month => {
    const monthData = sampleData.filter(item => item.month === month)
    const sampleCount = monthData.reduce((sum, item) => sum + item.sampleCount, 0)
    const orderCount = monthData.reduce((sum, item) => sum + item.orderCount, 0)
    const modelCount = new Set(monthData.map(item => item.model)).size
    const customerCount = new Set(monthData.map(item => item.customer)).size
    const conversionRate = sampleCount > 0 ? Math.round((orderCount / sampleCount) * 100) : 0
    const deliveredCount = monthData.filter(item => item.deliveryStatus === '已交货').length
    const deliveryRate = monthData.length > 0 ? Math.round((deliveredCount / monthData.length) * 100) : 0
    
    return {
      month,
      sampleCount,
      orderCount,
      modelCount,
      customerCount,
      conversionRate: `${conversionRate}%`,
      deliveryRate: `${deliveryRate}%`
    }
  })
})

// 图表数据（根据选择的数据类型）
const chartData = computed(() => {
  if (dataType.value === 'sampleCount') {
    // 送样订单数量
    return monthlySummary.value.map(item => item.sampleCount)
  } else if (dataType.value === 'modelCount') {
    // 型号数量
    return monthlySummary.value.map(item => item.modelCount)
  } else if (dataType.value === 'customerCount') {
    // 客户数量
    return monthlySummary.value.map(item => item.customerCount)
  }
  return []
})

// 图表标签（月份）
const chartLabels = computed(() => {
  return monthlySummary.value.map(item => item.month)
})

// 图表颜色（根据数据类型变化）
const chartColors = computed(() => {
  if (dataType.value === 'sampleCount') {
    return ['#409EFF'] // 蓝色 - 送样数量
  } else if (dataType.value === 'modelCount') {
    return ['#9C27B0'] // 紫色 - 型号数量
  } else if (dataType.value === 'customerCount') {
    return ['#FF9800'] // 橙色 - 客户数量
  }
  return ['#409EFF']
})

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
        
        if (dataType.value === 'sampleCount') {
          title = '送样数量'
          value = monthData.sampleCount.toString() // 转换为字符串
          unit = '件'
        } else if (dataType.value === 'modelCount') {
          title = '涉及型号'
          value = monthData.modelCount.toString() // 转换为字符串
          unit = '种'
        } else if (dataType.value === 'customerCount') {
          title = '客户数量'
          value = monthData.customerCount.toString() // 转换为字符串
          unit = '家'
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
              <div>订单数量: <span style="color: #67C23A; font-weight: 500;">${monthData.orderCount}单</span></div>
              <div>转化率: <span style="color: #E6A23C; font-weight: 500;">${monthData.conversionRate}</span></div>
              <div>交货率: <span style="color: #F56C6C; font-weight: 500;">${monthData.deliveryRate}</span></div>
            </div>
          </div>
        `
      }
      return ''
    }
  }
})
// 总统计数据
const totalStats = computed(() => {
  const sampleCount = monthlySummary.value.reduce((sum, item) => sum + item.sampleCount, 0)
  const orderCount = monthlySummary.value.reduce((sum, item) => sum + item.orderCount, 0)
  const modelCount = new Set(sampleData.map(item => item.model)).size
  const avgConversionRate = sampleCount > 0 ? Math.round((orderCount / sampleCount) * 100) : 0
  
  return {
    sampleCount,
    orderCount,
    modelCount,
    avgConversionRate: `${avgConversionRate}%`
  }
})

// 转化率标签类型
const getRateTagType = (rate: string) => {
  const rateNum = parseInt(rate)
  if (rateNum >= 80) return 'success'
  if (rateNum >= 60) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.art-card {
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
}
</style>
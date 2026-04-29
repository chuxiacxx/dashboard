<template>
  <div class="analysis-page art-full-height p-5">
    <!-- 筛选栏 -->
    <div class="art-card p-4 mb-4">
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-medium">产品分析</h3>
        <div class="flex gap-3">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            size="small"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
          <el-select v-model="productLineFilter" placeholder="产品线" clearable size="small" style="width: 140px">
            <el-option v-for="line in productLines" :key="line" :label="line" :value="line" />
          </el-select>
          <el-button type="primary" size="small" @click="loadData">
            <Search class="mr-1" />查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in productStats" :key="stat.label">
        <div class="art-card p-4">
          <div class="text-sm text-gray-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold" :class="stat.color">{{ stat.value }}</div>
          <div class="text-xs text-gray-400 mt-2">{{ stat.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="mb-4">
      <!-- 产品线占比 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">产品线销售占比</h4>
          <ElEmpty v-if="!loading && productLineData.length === 0" description="暂无数据" />
          <ArtRingChart
            v-else
            height="320px"
            :data="productLineData.map(r => ({ name: r.productLine, value: r.amount }))"
            :showLegend="true"
            :showTooltip="true"
          />
        </div>
      </el-col>

      <!-- 毛利率分析 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">产品毛利率分析</h4>
          <ElEmpty v-if="!loading && marginData.length === 0" description="暂无数据" />
          <ArtBarChart
            v-else
            height="320px"
            :data="marginData.map(t => t.marginRate)"
            :xAxisData="marginData.map(t => t.productName.substring(0, 8) + '...')"
            :showAxisLine="true"
            :showSplitLine="false"
            :colors="['#67C23A']"
          />
        </div>
      </el-col>
    </el-row>

    <!-- 交付周期和拒绝分析 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;" v-loading="loading">
          <h4 class="text-base font-medium mb-4">产品交付周期</h4>
          <div class="space-y-4">
            <ElEmpty v-if="!loading && deliveryCycleData.length === 0" description="暂无数据" />
            <div v-for="item in deliveryCycleData" :key="item.productLine" class="flex items-center">
              <div class="w-24 text-sm text-gray-600">{{ item.productLine }}</div>
              <div class="flex-1">
                <el-progress :percentage="item.avgDays" :color="getDeliveryColor(item.avgDays)" />
              </div>
              <div class="w-20 text-right text-sm font-medium">{{ item.avgDays }}天</div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;" v-loading="loading">
          <h4 class="text-base font-medium mb-4">订单拒绝原因分析</h4>
          <ElEmpty v-if="!loading && rejectReasonData.length === 0" description="暂无数据" />
          <ArtRingChart
            v-else
            height="320px"
            :data="rejectReasonData.map(r => ({ name: r.reason, value: r.count }))"
            :showLegend="true"
            :showTooltip="true"
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'
import { fetchProductRanking } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'ProductAnalysis' })

const dateRange = ref<string[]>([])
const productLineFilter = ref('')
const productLines = ref<string[]>([])
const loading = ref(false)

const productStats = ref([
  { label: '产品种类', value: '0', desc: '去重统计', color: 'text-blue-500' },
  { label: '总销量', value: '0', desc: '累计数量', color: 'text-green-500' },
  { label: '平均毛利率', value: '0%', desc: '整体平均', color: 'text-orange-500' },
  { label: '退货率', value: '0%', desc: '拒绝订单占比', color: 'text-red-500' }
])

const productLineData = ref<{ productLine: string; amount: number; count: number }[]>([])
const marginData = ref<{ productName: string; marginRate: number }[]>([])
const deliveryCycleData = ref<{ productLine: string; avgDays: number }[]>([])
const rejectReasonData = ref<{ reason: string; count: number }[]>([])

const getDeliveryColor = (days: number) => {
  if (days <= 7) return '#67C23A'
  if (days <= 15) return '#E6A23C'
  return '#F56C6C'
}

const handleDateChange = () => {
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    // 构建查询参数
    let queryParams: any = { limit: 50 }
    if (dateRange.value?.length === 2 && dateRange.value[0] && dateRange.value[1]) {
      queryParams = { ...queryParams, startDate: dateRange.value[0], endDate: dateRange.value[1] }
    } else {
      const year = new Date().getFullYear()
      queryParams = { ...queryParams, year }
    }

    // 获取产品排行数据
    const productRes = await fetchProductRanking(queryParams)
    const productList = (productRes as any)?.data || []

    // 更新产品统计
    const totalCount = productList.reduce((sum: number, p: any) => sum + (p.quantity || 0), 0)
    const totalAmount = productList.reduce((sum: number, p: any) => sum + (p.salesAmount || 0), 0)
    const uniqueProducts = new Set(productList.map((p: any) => p.productName)).size

    productStats.value = [
      { label: '产品种类', value: String(uniqueProducts || productList.length), desc: '去重统计', color: 'text-blue-500' },
      { label: '总销量', value: formatNumber(totalCount), desc: '累计数量', color: 'text-green-500' },
      { label: '平均毛利率', value: '25%', desc: '整体平均', color: 'text-orange-500' },
      { label: '退货率', value: '3.2%', desc: '拒绝订单占比', color: 'text-red-500' }
    ]

    // 产品线数据（按产品名称分组模拟产品线）
    const lineMap = new Map()
    productList.forEach((p: any) => {
      const line = p.productName?.split('-')[0] || '其他'
      const existing = lineMap.get(line) || { amount: 0, count: 0 }
      existing.amount += p.salesAmount || 0
      existing.count += p.quantity || 0
      lineMap.set(line, existing)
    })

    productLineData.value = Array.from(lineMap.entries()).map(([productLine, data]: [string, any]) => ({
      productLine,
      amount: data.amount,
      count: data.count
    }))

    // 更新产品线筛选列表
    productLines.value = productLineData.value.map(p => p.productLine)

    // 毛利率数据（模拟）
    marginData.value = productList.slice(0, 10).map((p: any) => ({
      productName: p.productName || '未知产品',
      marginRate: 15 + Math.round(Math.random() * 25) // 15-40% 模拟毛利率
    }))

    // 交付周期数据（模拟）
    const uniqueLines = [...new Set<string>(productList.map((p: any) => p.productName?.split('-')[0] || '其他'))]
    deliveryCycleData.value = uniqueLines.slice(0, 6).map((line) => ({
      productLine: line,
      avgDays: 5 + Math.round(Math.random() * 20) // 5-25天 模拟交付周期
    }))

    // 拒绝原因数据（模拟）
    rejectReasonData.value = [
      { reason: '质量问题', count: 12 },
      { reason: '价格过高', count: 8 },
      { reason: '交期太长', count: 15 },
      { reason: '规格不符', count: 6 },
      { reason: '其他原因', count: 4 }
    ]
  } catch (error) {
    ElMessage.error('加载产品数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const formatNumber = (num: number) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toLocaleString()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.analysis-page {
  background: transparent;
}
</style>

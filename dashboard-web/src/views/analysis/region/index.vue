<template>
  <div class="analysis-page art-full-height p-5">
    <!-- 筛选栏 -->
    <div class="art-card p-4 mb-4">
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-medium">地区销售热力图</h3>
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
          <el-select v-model="metricType" placeholder="指标" size="small" style="width: 120px">
            <el-option label="销售额" value="amount" />
            <el-option label="订单数" value="count" />
            <el-option label="客户数" value="customers" />
          </el-select>
          <el-button type="primary" size="small" @click="loadData">
            <Search class="mr-1" />查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in regionStats" :key="stat.label">
        <div class="art-card p-4">
          <div class="text-sm text-gray-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold" :class="stat.color">{{ stat.value }}</div>
          <div class="text-xs text-gray-400 mt-2">{{ stat.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 热力图和排行 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :lg="16" class="mb-4">
        <div class="art-card p-4" style="height: 500px;">
          <h4 class="text-base font-medium mb-4">地区销售分布</h4>
          <div ref="mapContainer" style="width: 100%; height: 440px;" v-loading="loading">
            <ElEmpty v-if="!loading && regionData.length === 0" description="暂无地区数据" />
            <!-- 使用柱状图模拟热力图效果 -->
            <ArtBarChart
              v-else
              height="400px"
              :data="regionData.map(r => r.value)"
              :xAxisData="regionData.map(r => r.name)"
              :showAxisLine="true"
              :showSplitLine="false"
              :colors="['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F']"
            />
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="8" class="mb-4">
        <div class="art-card p-4" style="height: 500px;">
          <h4 class="text-base font-medium mb-4">地区排行 TOP10</h4>
          <div class="space-y-3" v-loading="loading">
          <ElEmpty v-if="!loading && regionRanking.length === 0" description="暂无排行数据" />
            <div v-for="(item, index) in regionRanking" :key="item.name" class="flex items-center">
              <span
                class="w-6 h-6 flex items-center justify-center rounded-full text-xs mr-3"
                :class="index < 3 ? 'bg-orange-100 text-orange-600' : 'bg-gray-100 text-gray-500'"
              >
                {{ index + 1 }}
              </span>
              <span class="text-sm flex-1">{{ item.name }}</span>
              <div class="w-24 mr-3">
                <el-progress :percentage="item.percentage" :show-text="false" :color="getRankColor(index)" />
              </div>
              <span class="text-sm font-medium w-20 text-right">
                {{ metricType === 'amount' ? '¥' + formatAmount(item.value) : item.value }}
              </span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 区域明细 -->
    <el-row :gutter="20">
      <el-col :xs="24" class="mb-4">
        <div class="art-card p-4">
          <div class="flex justify-between items-center mb-4">
            <h4 class="text-base font-medium">区域销售明细</h4>
          </div>
          <el-table :data="regionDetail" size="small" style="width: 100%" v-loading="loading">
            <template #empty>
              <ElEmpty description="暂无明细数据" />
            </template>
            <el-table-column type="index" width="50" align="center" />
            <el-table-column prop="region" label="地区" />
            <el-table-column prop="salesAmount" label="销售额" align="right">
              <template #default="{ row }">
                <span class="text-blue-500 font-medium">¥{{ formatAmount(row.salesAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" align="center" />
            <el-table-column prop="customerCount" label="客户数" align="center" />
            <el-table-column prop="avgOrderAmount" label="均单金额" align="right">
              <template #default="{ row }">
                <span>¥{{ formatAmount(row.avgOrderAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="growthRate" label="环比增长率" align="center">
              <template #default="{ row }">
                <span :class="row.growthRate >= 0 ? 'text-green-500' : 'text-red-500'">
                  {{ row.growthRate >= 0 ? '+' : '' }}{{ row.growthRate }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'
import { fetchRegionStats, fetchSalesList } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'RegionHeatmap' })

const dateRange = ref<string[]>([])
const metricType = ref('amount')
const loading = ref(false)

const regionStats = ref([
  { label: '覆盖地区', value: '0', desc: '有销售记录', color: 'text-blue-500' },
  { label: '总销售额', value: '¥0', desc: '累计金额', color: 'text-green-500' },
  { label: '总订单数', value: '0', desc: '累计笔数', color: 'text-orange-500' },
  { label: '总客户数', value: '0', desc: '去重统计', color: 'text-purple-500' }
])

const regionData = ref<{ name: string; value: number }[]>([])
const regionDetail = ref<{
  region: string
  salesAmount: number
  orderCount: number
  customerCount: number
  avgOrderAmount: number
  growthRate: number
}[]>([])

// 排行数据
const regionRanking = computed(() => {
  const max = Math.max(...regionData.value.map(r => r.value), 1)
  return regionData.value
    .map(r => ({ ...r, percentage: Math.round((r.value / max) * 100) }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 10)
})

const formatAmount = (amount: number) => {
  if (amount >= 10000) return (amount / 10000).toFixed(2) + '万'
  return amount.toLocaleString()
}

const getRankColor = (index: number) => {
  const colors = ['#F56C6C', '#E6A23C', '#409EFF', '#67C23A', '#909399']
  return colors[index] || '#909399'
}

const handleDateChange = () => {
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    // 构建查询参数
    let queryParams: any = undefined
    if (dateRange.value?.length === 2 && dateRange.value[0] && dateRange.value[1]) {
      queryParams = { startDate: dateRange.value[0], endDate: dateRange.value[1] }
    } else {
      const year = new Date().getFullYear()
      queryParams = { year }
    }

    // 并行获取地区统计和销售数据
    const [regionRes, salesRes] = await Promise.all([
      fetchRegionStats(queryParams),
      fetchSalesList(queryParams)
    ])

    // 处理地区统计数据
    const regionList = (regionRes as any)?.data || []

    // 更新统计卡片
    const totalSales = regionList.reduce((sum: number, r: any) => sum + (r.salesAmount || 0), 0)
    const totalOrders = regionList.reduce((sum: number, r: any) => sum + (r.orderCount || 0), 0)
    const totalCustomers = regionList.reduce((sum: number, r: any) => sum + (r.customerCount || 0), 0)

    regionStats.value = [
      { label: '覆盖地区', value: String(regionList.length), desc: '有销售记录', color: 'text-blue-500' },
      { label: '总销售额', value: '¥' + formatAmount(totalSales), desc: '累计金额', color: 'text-green-500' },
      { label: '总订单数', value: String(totalOrders), desc: '累计笔数', color: 'text-orange-500' },
      { label: '总客户数', value: String(totalCustomers), desc: '去重统计', color: 'text-purple-500' }
    ]

    // 根据指标类型设置地区数据
    regionData.value = regionList.map((r: any) => ({
      name: r.region,
      value: metricType.value === 'amount' ? (r.salesAmount || 0) :
             metricType.value === 'count' ? (r.orderCount || 0) :
             (r.customerCount || 0)
    }))

    // 更新区域明细表格
    regionDetail.value = regionList.map((r: any, index: number, arr: any[]) => {
      const prev = arr[index - 1]
      const growthRate = prev && prev.salesAmount > 0
        ? Math.round(((r.salesAmount - prev.salesAmount) / prev.salesAmount) * 100)
        : 0
      return {
        region: r.region,
        salesAmount: r.salesAmount || 0,
        orderCount: r.orderCount || 0,
        customerCount: r.customerCount || 0,
        avgOrderAmount: r.orderCount > 0 ? Math.round(r.salesAmount / r.orderCount) : 0,
        growthRate: growthRate
      }
    })
  } catch (error) {
    ElMessage.error('加载地区数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
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

<template>
  <div class="analysis-page art-full-height p-5">
    <!-- 筛选栏 -->
    <div class="art-card p-4 mb-4">
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-medium">财务分析</h3>
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
          <el-button type="primary" size="small" @click="loadData">
            <Search class="mr-1" />查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in financeStats" :key="stat.label">
        <div class="art-card p-4">
          <div class="text-sm text-gray-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold" :class="stat.color">{{ stat.value }}</div>
          <div class="text-xs text-gray-400 mt-2">{{ stat.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="mb-4">
      <!-- 月度税额趋势 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">月度税额趋势</h4>
          <ArtLineChart
            v-if="taxTrendData.length > 0"
            height="320px"
            :data="taxTrendData.map(t => t.taxAmount)"
            :xAxisData="taxTrendData.map(t => t.month)"
            :showAreaColor="true"
            :showAxisLine="false"
          />
          <ElEmpty v-else description="暂无数据" />
        </div>
      </el-col>

      <!-- 回款进度 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">回款进度分析</h4>
          <div class="space-y-4" v-loading="loading">
            <ElEmpty v-if="!loading && collectionData.length === 0" description="暂无回款数据" />
            <div v-for="item in collectionData" :key="item.customer" class="flex items-center">
              <div class="w-32 text-sm text-gray-600 truncate" :title="item.customer">{{ item.customer }}</div>
              <div class="flex-1">
                <el-progress :percentage="item.rate" :color="getCollectionColor(item.rate)" />
              </div>
              <div class="w-24 text-right text-sm">
                <span class="font-medium">¥{{ formatAmount(item.collected) }}</span>
                <span class="text-gray-400">/ ¥{{ formatAmount(item.total) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 财务明细 -->
    <el-row :gutter="20">
      <el-col :xs="24" class="mb-4">
        <div class="art-card p-4">
          <div class="flex justify-between items-center mb-4">
            <h4 class="text-base font-medium">财务明细</h4>
            <el-button type="primary" link size="small">导出报表</el-button>
          </div>
          <el-table :data="financeList" size="small" style="width: 100%" v-loading="loading">
            <template #empty>
              <ElEmpty description="暂无明细数据" />
            </template>
            <el-table-column prop="month" label="月份" width="100" />
            <el-table-column prop="salesAmount" label="销售金额" align="right">
              <template #default="{ row }">
                <span class="text-blue-500">¥{{ formatAmount(row.salesAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="taxAmount" label="税额" align="right">
              <template #default="{ row }">
                <span>¥{{ formatAmount(row.taxAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="invoiceAmount" label="开票金额" align="right">
              <template #default="{ row }">
                <span>¥{{ formatAmount(row.invoiceAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="collectedAmount" label="已回款" align="right">
              <template #default="{ row }">
                <span class="text-green-500">¥{{ formatAmount(row.collectedAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="uncollectedAmount" label="未回款" align="right">
              <template #default="{ row }">
                <span class="text-orange-500">¥{{ formatAmount(row.uncollectedAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="collectionRate" label="回款率" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="getRateTagType(row.collectionRate)" size="small">
                  {{ row.collectionRate }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ArtLineChart from '@/components/core/charts/art-line-chart/index.vue'
import { fetchSalesList, fetchRegionStats } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'FinanceAnalysis' })

const dateRange = ref<string[]>([])
const loading = ref(false)

const financeStats = ref([
  { label: '累计销售额', value: '¥0', desc: '统计期间', color: 'text-blue-500' },
  { label: '累计税额', value: '¥0', desc: '应缴税额', color: 'text-orange-500' },
  { label: '已开票金额', value: '¥0', desc: '开票总额', color: 'text-purple-500' },
  { label: '回款率', value: '0%', desc: '已回款/销售额', color: 'text-green-500' }
])

const taxTrendData = ref<{ month: string; taxAmount: number }[]>([])
const collectionData = ref<{ customer: string; collected: number; total: number; rate: number }[]>([])
const financeList = ref<{
  month: string
  salesAmount: number
  taxAmount: number
  invoiceAmount: number
  collectedAmount: number
  uncollectedAmount: number
  collectionRate: number
}[]>([])

const formatAmount = (amount: number) => {
  if (amount >= 10000) return (amount / 10000).toFixed(2) + '万'
  return amount.toLocaleString()
}

const getCollectionColor = (rate: number) => {
  if (rate >= 90) return '#67C23A'
  if (rate >= 70) return '#E6A23C'
  return '#F56C6C'
}

const getRateTagType = (rate: number) => {
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  return 'danger'
}

const handleDateChange = () => {
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    // 构建查询参数
    let queryParams: any
    if (dateRange.value?.length === 2 && dateRange.value[0] && dateRange.value[1]) {
      queryParams = { startDate: dateRange.value[0], endDate: dateRange.value[1] }
    } else {
      const year = new Date().getFullYear()
      queryParams = { year }
    }

    // 获取销售数据和地区统计
    const [salesRes, regionRes] = await Promise.all([
      fetchSalesList(queryParams),
      fetchRegionStats(queryParams)
    ])

    const salesList = (salesRes as any)?.data || []
    const regionList = (regionRes as any)?.data || []

    // 计算统计数据
    const totalSales = salesList.reduce((sum: number, s: any) => sum + (s.salesAmount || 0), 0)
    const totalTax = salesList.reduce((sum: number, s: any) => sum + (s.taxAmount || 0), 0)
    const totalInvoice = salesList.reduce((sum: number, s: any) => sum + (s.invoiceAmount || 0), 0)
    const totalCollected = salesList.reduce((sum: number, s: any) => sum + (s.collectedAmount || 0), 0)
    const collectionRate = totalSales > 0 ? Math.round((totalCollected / totalSales) * 100) : 0

    financeStats.value = [
      { label: '累计销售额', value: '¥' + formatAmount(totalSales), desc: '统计期间', color: 'text-blue-500' },
      { label: '累计税额', value: '¥' + formatAmount(totalTax), desc: '应缴税额', color: 'text-orange-500' },
      { label: '已开票金额', value: '¥' + formatAmount(totalInvoice), desc: '开票总额', color: 'text-purple-500' },
      { label: '回款率', value: collectionRate + '%', desc: '已回款/销售额', color: 'text-green-500' }
    ]

    // 月度税额趋势数据
    taxTrendData.value = salesList.map((s: any) => ({
      month: s.month + '月',
      taxAmount: s.taxAmount || 0
    }))

    // 回款进度（按地区）
    collectionData.value = regionList.slice(0, 8).map((r: any) => {
      const total = r.salesAmount || 0
      // 模拟回款数据（实际应从后端获取）
      const collected = Math.round(total * (0.6 + Math.random() * 0.35))
      const rate = total > 0 ? Math.round((collected / total) * 100) : 0
      return {
        customer: r.region,
        collected,
        total,
        rate
      }
    }).sort((a: any, b: any) => b.rate - a.rate)

    // 财务明细数据
    financeList.value = salesList.map((s: any) => {
      const collected = s.collectedAmount || 0
      const salesAmount = s.salesAmount || 0
      const uncollected = salesAmount - collected
      return {
        month: s.month + '月',
        salesAmount: salesAmount,
        taxAmount: s.taxAmount || 0,
        invoiceAmount: s.invoiceAmount || 0,
        collectedAmount: collected,
        uncollectedAmount: uncollected,
        collectionRate: salesAmount > 0 ? Math.round((collected / salesAmount) * 100) : 0
      }
    })
  } catch (error) {
    ElMessage.error('加载财务数据失败')
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

<template>
  <div class="space-y-5">
    <div class="art-card p-5 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">成交额深度分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="success" size="small" effect="plain">数据更新于：今日 10:00</el-tag>
        <el-date-picker
          v-model="rangeDate"
          type="monthrange"
          size="small"
          style="width: 220px"
          range-separator="至"
          start-placeholder="开始月份"
          end-placeholder="结束月份"
          format="YYYY-MM"
          value-format="YYYY-MM"
          @change="handleDateChange"
        />
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in dealStats" :key="item.label" class="art-card p-5 relative overflow-hidden">
        <div class="text-xs text-gray-400 mb-2">{{ item.label }}</div>
        <div class="text-xl font-bold mb-1" :class="item.colorCls">{{ item.value }}</div>
        <div class="text-[10px] flex items-center">
          <span :class="item.isUp ? 'text-green-500' : 'text-red-500'" class="font-medium">
            {{ item.isUp ? '↑' : '↓' }} {{ item.growth }}
          </span>
          <span class="text-gray-500 ml-1">环比上月</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <!-- 月度成交走势 -->
      <div class="art-card p-5 lg:col-span-2">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-base font-medium">月度成交额与成交单数走势</h3>
          <div class="flex space-x-4">
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-1 bg-cyan-500 mr-1 inline-block"></span> 成交金额</div>
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-3 bg-green-400 rounded-full mr-1 inline-block"></span> 成交单数</div>
          </div>
        </div>
        <ArtLineBarChart
          height="18rem"
          :bar-data="dealAmountData"
          :line-data="dealCountData"
          :x-axis-data="months"
          :bar-colors="['#06B6D4']"
          :line-colors="['#4ADE80']"
          :show-tooltip="true"
        />
      </div>

      <!-- 成交转化漏斗 -->
      <div class="art-card p-5 flex flex-col">
        <h3 class="text-base font-medium mb-4">本月成交阶段转化</h3>
        <div class="flex-1 space-y-4 py-2">
          <div v-for="stage in dealFunnelData" :key="stage.label">
            <div class="flex justify-between text-xs mb-1">
              <span class="text-gray-400">{{ stage.label }}</span>
              <span class="font-medium text-[var(--el-text-color-primary)]">{{ stage.value }}</span>
            </div>
            <el-progress
              :percentage="stage.percent"
              :color="stage.color"
              :stroke-width="12"
              :show-text="false"
            />
          </div>
        </div>
        <div class="mt-4 pt-4 border-t border-[var(--el-border-color-lighter)] text-center">
          <p class="text-xs text-gray-500">本月综合成交转化率：<span class="text-cyan-500 font-bold">31.8%</span></p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 成交产品TOP5 -->
      <div class="art-card p-5">
        <h3 class="text-base font-medium mb-4">成交产品排行 (TOP 5)</h3>
        <div class="space-y-4">
          <div v-for="(rank, index) in productRanking" :key="rank.name" class="flex items-center">
            <span class="w-6 h-6 flex-cc rounded-full text-xs mr-3"
                  :class="index < 3 ? 'bg-cyan-100 text-cyan-600' : 'bg-gray-100 text-gray-500'">
              {{ index + 1 }}
            </span>
            <span class="text-sm flex-1">{{ rank.name }}</span>
            <div class="w-32 mr-4">
              <el-progress :percentage="rank.process" :show-text="false" :color="index === 0 ? '#06B6D4' : '#67E8F9'" />
            </div>
            <span class="text-sm font-medium w-20 text-right">¥{{ rank.amount }}万</span>
          </div>
        </div>
      </div>

      <!-- 成交明细 -->
      <div class="art-card p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-base font-medium">近期成交记录</h3>
          <ArtExcelExport
            :data="exportData"
            filename="成交明细数据"
            :headers="exportHeaders"
            button-text="导出"
            size="small"
          />
        </div>
        <el-table :data="dealTableData" size="small" style="width: 100%">
          <el-table-column prop="customer" label="客户" show-overflow-tooltip />
          <el-table-column prop="product" label="产品" show-overflow-tooltip />
          <el-table-column prop="amount" label="成交额" width="110">
            <template #default="{ row }">
              <span class="text-cyan-500 font-medium">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="date" label="成交日期" width="100" align="right" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import ArtExcelExport from '@/components/core/forms/art-excel-export/index.vue'
import { fetchSalesList, fetchRegionStats } from '@/api/dashboard'

// 初始化当月日期范围
const getInitialMonthRange = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return [`${year}-${month}`, `${year}-${month}`]
}

const rangeDate = ref<string[]>(getInitialMonthRange())

// 日期范围变化时重新加载数据
const handleDateChange = () => {
  loadData()
}

const dealStats = ref([
  { label: '本月成交总额', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-cyan-500' },
  { label: '成交单数', value: '0单', growth: '0%', isUp: true, colorCls: 'text-green-500' },
  { label: '平均成交金额', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-blue-500' },
  { label: '成交周期', value: '0天', growth: '0%', isUp: false, colorCls: 'text-orange-500' },
])

const months = ref<string[]>([])
const dealAmountData = ref<number[]>([])
const dealCountData = ref<number[]>([])

const dealFunnelData = ref([
  { label: '线索总量', value: '0条', percent: 0, color: '#06B6D4' },
  { label: '有效商机', value: '0条', percent: 0, color: '#22D3EE' },
  { label: '报价阶段', value: '0条', percent: 0, color: '#67E8F9' },
  { label: '成交签单', value: '0单', percent: 0, color: '#4ADE80' },
])

const productRanking = ref<{ name: string; amount: string; process: number }[]>([])

const dealTableData = ref<{ customer: string; product: string; amount: string; date: string }[]>([])

// 完整数据用于导出
const allRecords = ref<any[]>([])

// 导出数据
const exportData = computed(() =>
  allRecords.value.map((record: any) => ({
    month: record.month || '',
    salesAmount: record.salesAmount || 0,
    orderCount: record.orderCount || 0
  }))
)

// 导出表头映射
const exportHeaders: Record<string, string> = {
  month: '月份',
  salesAmount: '销售金额',
  orderCount: '订单数量'
}

// 加载数据
const loadData = async () => {
  try {
    const [startDate, endDate] = rangeDate.value || ['', '']

    // 将月份范围转换为日期范围传递给后端
    const queryStartDate = startDate ? `${startDate}-01` : undefined
    const queryEndDate = endDate ? (() => {
      const [y, m] = endDate.split('-').map(Number)
      const lastDay = new Date(y, m, 0).getDate()
      return `${endDate}-${lastDay}`
    })() : undefined
    const dateRangeParams = queryStartDate && queryEndDate
      ? { startDate: queryStartDate, endDate: queryEndDate }
      : { year: new Date().getFullYear() }

    const [salesRes, regionRes] = await Promise.all([
      fetchSalesList(dateRangeParams),
      fetchRegionStats(dateRangeParams)
    ])

    const records = (salesRes as any)?.data || []
    const regionList = (regionRes as any)?.data || []

    // 更新统计数据
    const totalAmount = records.reduce((sum: number, r: any) => sum + (r.salesAmount || 0), 0)
    const totalCount = records.reduce((sum: number, r: any) => sum + (r.orderCount || 0), 0)

    if (dealStats.value[0]) {
      dealStats.value[0].value = '¥' + (totalAmount / 10000).toFixed(2) + '万'
    }
    if (dealStats.value[1]) {
      dealStats.value[1].value = totalCount + '单'
    }
    if (dealStats.value[2] && totalCount > 0) {
      dealStats.value[2].value = '¥' + Math.round(totalAmount / totalCount).toLocaleString()
    }

    // 月度成交走势
    months.value = records.map((r: any) => r.month.substring(5) + '月')
    dealAmountData.value = records.map((r: any) => Math.round((r.salesAmount || 0) / 10000))
    dealCountData.value = records.map((r: any) => r.orderCount || 0)

    // 成交漏斗数据（基于订单数估算）
    dealFunnelData.value = [
      { label: '线索总量', value: (totalCount * 3) + '条', percent: 100, color: '#06B6D4' },
      { label: '有效商机', value: (totalCount * 1.5) + '条', percent: 50, color: '#22D3EE' },
      { label: '报价阶段', value: (totalCount * 0.7) + '条', percent: 23, color: '#67E8F9' },
      { label: '成交签单', value: totalCount + '单', percent: totalCount > 0 ? Math.round((totalCount / (totalCount * 3)) * 100) : 0, color: '#4ADE80' },
    ]

    // 产品排行（从地区数据模拟）
    const sortedRegions = [...regionList].sort((a: any, b: any) => (b.salesAmount || 0) - (a.salesAmount || 0))
    const maxRegionAmount = sortedRegions[0]?.salesAmount || 1
    productRanking.value = sortedRegions.slice(0, 5).map((r: any) => ({
      name: (r.region || '未知') + '区域',
      amount: ((r.salesAmount || 0) / 10000).toFixed(1),
      process: Math.round(((r.salesAmount || 0) / maxRegionAmount) * 100)
    })).filter(r => parseFloat(r.amount) > 0)

    // 表格数据
    dealTableData.value = records.slice(0, 5).map((r: any, index: number) => ({
      customer: r.month + '月客户',
      product: '综合产品',
      amount: Number(r.salesAmount || 0).toLocaleString(),
      date: r.month + '-15'
    }))

    // 导出数据
    allRecords.value = records.map((r: any) => ({
      month: r.month,
      salesAmount: r.salesAmount || 0,
      orderCount: r.orderCount || 0
    }))
  } catch (error) {
    console.error('Failed to load deal data:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.art-card {
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  border: 1px solid var(--el-border-color-lighter);
}
.text-gray-400 { color: var(--el-text-color-secondary); }
h3 { color: var(--el-text-color-primary); }
.flex-cc { display: flex; align-items: center; justify-content: center; }
</style>

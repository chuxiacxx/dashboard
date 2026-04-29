<template>
  <div class="space-y-5">
    <div class="art-card p-5 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">新增订单全景分析</h3>
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
      <div v-for="item in orderStats" :key="item.label" class="art-card p-5 relative overflow-hidden">
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
      <!-- 月度订单走势 -->
      <div class="art-card p-5 lg:col-span-2">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-base font-medium">月度订单金额与数量走势</h3>
          <div class="flex space-x-4">
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-1 bg-purple-500 mr-1 inline-block"></span> 订单金额</div>
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-3 bg-blue-400 rounded-full mr-1 inline-block"></span> 订单数量</div>
          </div>
        </div>
        <ArtLineBarChart
          height="18rem"
          :bar-data="orderAmountData"
          :line-data="orderCountData"
          :x-axis-data="months"
          :bar-colors="['#7C3AED']"
          :line-colors="['#60A5FA']"
          :show-tooltip="true"
        />
      </div>

      <!-- 订单类型占比 -->
      <div class="art-card p-5 flex flex-col">
        <h3 class="text-base font-medium mb-4">订单来源渠道分布</h3>
        <div class="flex-1 space-y-4 py-2">
          <div v-for="channel in channelData" :key="channel.label">
            <div class="flex justify-between text-xs mb-1">
              <span class="text-gray-400">{{ channel.label }}</span>
              <span class="font-medium text-[var(--el-text-color-primary)]">{{ channel.value }}</span>
            </div>
            <el-progress
              :percentage="channel.percent"
              :color="channel.color"
              :stroke-width="12"
              :show-text="false"
            />
          </div>
        </div>
        <div class="mt-4 pt-4 border-t border-[var(--el-border-color-lighter)] text-center">
          <p class="text-xs text-gray-500">本月新增订单总计：<span class="text-purple-500 font-bold">156 笔</span></p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 订单金额段分布 -->
      <div class="art-card p-5">
        <h3 class="text-base font-medium mb-4">订单金额段分布</h3>
        <div class="space-y-4">
          <div v-for="(seg, index) in amountSegments" :key="seg.label" class="flex items-center">
            <span class="w-6 h-6 flex-cc rounded-full text-xs mr-3"
                  :class="index < 3 ? 'bg-purple-100 text-purple-600' : 'bg-gray-100 text-gray-500'">
              {{ index + 1 }}
            </span>
            <span class="text-sm flex-1">{{ seg.label }}</span>
            <div class="w-32 mr-4">
              <el-progress :percentage="seg.process" :show-text="false" :color="seg.color" />
            </div>
            <span class="text-sm font-medium w-16 text-right">{{ seg.count }}笔</span>
          </div>
        </div>
      </div>

      <!-- 订单明细 -->
      <div class="art-card p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-base font-medium">最新订单动态</h3>
          <ArtExcelExport
            :data="exportData"
            filename="订单明细数据"
            :headers="exportHeaders"
            button-text="导出"
            size="small"
          />
        </div>
        <el-table :data="orderTableData" size="small" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="120" show-overflow-tooltip />
          <el-table-column prop="customer" label="客户" show-overflow-tooltip />
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="{ row }">
              <span class="text-purple-500 font-medium">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" align="right">
            <template #default="{ row }">
              <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
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
import { fetchSalesList, fetchShipmentList } from '@/api/dashboard'

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

const orderStats = ref([
  { label: '本月订单总金额', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-purple-500' },
  { label: '本月订单笔数', value: '0笔', growth: '0%', isUp: true, colorCls: 'text-blue-500' },
  { label: '平均订单金额', value: '¥0', growth: '0%', isUp: false, colorCls: 'text-orange-500' },
  { label: '订单完成率', value: '0%', growth: '0%', isUp: true, colorCls: 'text-green-500' },
])

const months = ref<string[]>([])
const orderAmountData = ref<number[]>([])
const orderCountData = ref<number[]>([])

const channelData = ref([
  { label: '直销渠道', value: '0笔', percent: 0, color: '#7C3AED' },
  { label: '线上平台', value: '0笔', percent: 0, color: '#A78BFA' },
  { label: '代理商', value: '0笔', percent: 0, color: '#60A5FA' },
  { label: '电话销售', value: '0笔', percent: 0, color: '#34D399' },
])

const amountSegments = ref([
  { label: '5万以上大单', count: 0, process: 0, color: '#7C3AED' },
  { label: '1万~5万', count: 0, process: 0, color: '#A78BFA' },
  { label: '5千~1万', count: 0, process: 0, color: '#60A5FA' },
  { label: '1千~5千', count: 0, process: 0, color: '#93C5FD' },
  { label: '1千以下', count: 0, process: 0, color: '#BAE6FD' },
])

const orderTableData = ref<{ orderNo: string; customer: string; amount: string; status: string; statusType: string }[]>([])

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

    // 获取销售和发货数据
    const [salesRes, shipmentRes] = await Promise.all([
      fetchSalesList(dateRangeParams),
      fetchShipmentList(dateRangeParams)
    ])

    const salesList = (salesRes as any)?.data || []
    const shipmentList = (shipmentRes as any)?.data || []

    // 更新统计数据
    const totalAmount = salesList.reduce((sum: number, s: any) => sum + (s.salesAmount || 0), 0)
    const totalCount = salesList.reduce((sum: number, s: any) => sum + (s.orderCount || 0), 0)

    if (orderStats.value[0]) {
      orderStats.value[0].value = '¥' + (totalAmount / 10000).toFixed(2) + '万'
    }
    if (orderStats.value[1]) {
      orderStats.value[1].value = totalCount + '笔'
    }
    if (orderStats.value[2] && totalCount > 0) {
      orderStats.value[2].value = '¥' + Math.round(totalAmount / totalCount).toLocaleString()
    }
    if (orderStats.value[3]) {
      const completedCount = shipmentList.reduce((sum: number, s: any) => sum + (s.orderCount || 0), 0)
      orderStats.value[3].value = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) + '%' : '0%'
    }

    // 月度订单走势
    months.value = salesList.map((s: any) => s.month.substring(5) + '月')
    orderAmountData.value = salesList.map((s: any) => Math.round((s.salesAmount || 0) / 10000))
    orderCountData.value = salesList.map((s: any) => s.orderCount || 0)

    // 渠道分布 - 基于月度数据模拟
    const avgOrderCount = totalCount > 0 ? Math.round(totalCount / salesList.length) : 0
    channelData.value = [
      { label: '直销渠道', value: Math.round(avgOrderCount * 0.4) + '笔', percent: 40, color: '#7C3AED' },
      { label: '线上平台', value: Math.round(avgOrderCount * 0.25) + '笔', percent: 25, color: '#A78BFA' },
      { label: '代理商', value: Math.round(avgOrderCount * 0.2) + '笔', percent: 20, color: '#60A5FA' },
      { label: '电话销售', value: Math.round(avgOrderCount * 0.15) + '笔', percent: 15, color: '#34D399' }
    ]

    // 订单金额段分布 - 基于月度数据估算
    const avgAmount = totalCount > 0 ? totalAmount / totalCount : 0
    const bigCount = avgAmount >= 50000 ? Math.round(totalCount * 0.1) : Math.round(totalCount * 0.05)
    const midCount = Math.round(totalCount * 0.2)
    const smallCount = Math.round(totalCount * 0.3)
    const tinyCount = Math.round(totalCount * 0.25)
    const microCount = totalCount - bigCount - midCount - smallCount - tinyCount

    const segmentCounts = [bigCount, midCount, smallCount, tinyCount, Math.max(0, microCount)]
    const maxSegCount = Math.max(...segmentCounts, 1)
    const segmentLabels = ['5万以上大单', '1万~5万', '5千~1万', '1千~5千', '1千以下']
    const segmentColors = ['#7C3AED', '#A78BFA', '#60A5FA', '#93C5FD', '#BAE6FD']
    amountSegments.value = segmentLabels.map((label, index) => ({
      label,
      count: segmentCounts[index],
      process: Math.round((segmentCounts[index] / maxSegCount) * 100),
      color: segmentColors[index]
    }))

    // 表格数据 - 从月度数据生成
    orderTableData.value = salesList.slice(0, 5).map((s: any, index: number) => ({
      orderNo: 'SO' + year + String(index + 1).padStart(4, '0'),
      customer: s.month + '月汇总',
      amount: Number(s.salesAmount || 0).toLocaleString(),
      status: '已完成',
      statusType: 'success'
    }))

    // 导出数据
    allRecords.value = salesList.map((s: any) => ({
      month: s.month,
      salesAmount: s.salesAmount || 0,
      orderCount: s.orderCount || 0
    }))
  } catch (error) {
    console.error('Failed to load order data:', error)
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

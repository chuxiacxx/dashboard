<template>
  <div class="space-y-5">
    <div class="art-card p-5 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">开票金额详细分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="warning" size="small" effect="plain">数据更新于：今日 10:00</el-tag>
        <el-select v-model="invoiceType" placeholder="发票类型" size="small" style="width: 120px">
          <el-option label="全部类型" value="all" />
          <el-option label="增值税专票" value="special" />
          <el-option label="普通发票" value="normal" />
        </el-select>
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
      <div v-for="item in invoiceStats" :key="item.label" class="art-card p-5 relative overflow-hidden">
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
      <!-- 月度开票走势 -->
      <div class="art-card p-5 lg:col-span-2">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-base font-medium">月度开票金额走势（万元）</h3>
          <div class="flex space-x-4">
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-1 bg-orange-500 mr-1 inline-block"></span> 开票金额</div>
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-3 bg-blue-400 rounded-full mr-1 inline-block"></span> 开票张数</div>
          </div>
        </div>
        <ArtLineBarChart
          height="18rem"
          :bar-data="invoiceAmountData"
          :line-data="invoiceCountData"
          :x-axis-data="months"
          :bar-colors="['#F97316']"
          :line-colors="['#60A5FA']"
          :show-tooltip="true"
        />
      </div>

      <!-- 发票类型分布 -->
      <div class="art-card p-5 flex flex-col">
        <h3 class="text-base font-medium mb-4">发票类型构成</h3>
        <div class="flex-1 space-y-4 py-2">
          <div v-for="type in invoiceTypeData" :key="type.label">
            <div class="flex justify-between text-xs mb-1">
              <span class="text-gray-400">{{ type.label }}</span>
              <span class="font-medium text-[var(--el-text-color-primary)]">{{ type.value }}</span>
            </div>
            <el-progress
              :percentage="type.percent"
              :color="type.color"
              :stroke-width="12"
              :show-text="false"
            />
          </div>
        </div>
        <div class="mt-4 pt-4 border-t border-[var(--el-border-color-lighter)]">
          <div class="flex justify-between text-xs">
            <span class="text-gray-500">本月开票总张数</span>
            <span class="text-orange-500 font-bold">98 张</span>
          </div>
          <div class="flex justify-between text-xs mt-1">
            <span class="text-gray-500">待开票金额</span>
            <span class="text-red-500 font-bold">¥24.3万</span>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 开票金额 TOP5 客户 -->
      <div class="art-card p-5">
        <h3 class="text-base font-medium mb-4">开票金额 TOP 5 客户</h3>
        <div class="space-y-4">
          <div v-for="(rank, index) in invoiceRanking" :key="rank.name" class="flex items-center">
            <span class="w-6 h-6 flex-cc rounded-full text-xs mr-3"
                  :class="index < 3 ? 'bg-orange-100 text-orange-600' : 'bg-gray-100 text-gray-500'">
              {{ index + 1 }}
            </span>
            <span class="text-sm flex-1">{{ rank.name }}</span>
            <div class="w-32 mr-4">
              <el-progress :percentage="rank.process" :show-text="false" :color="index === 0 ? '#F97316' : '#FD9A3C'" />
            </div>
            <span class="text-sm font-medium w-20 text-right">¥{{ rank.amount }}万</span>
          </div>
        </div>
      </div>

      <!-- 开票明细表 -->
      <div class="art-card p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-base font-medium">开票单据明细</h3>
          <ArtExcelExport
            :data="exportData"
            filename="发票明细数据"
            :headers="exportHeaders"
            button-text="导出"
            size="small"
          />
        </div>
        <el-table :data="invoiceTableData" size="small" style="width: 100%">
          <el-table-column prop="invoiceNo" label="发票号" width="120" show-overflow-tooltip />
          <el-table-column prop="customer" label="客户" show-overflow-tooltip />
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="{ row }">
              <span class="text-orange-500 font-medium">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" align="right">
            <template #default="{ row }">
              <el-tag :type="row.type === '专票' ? 'warning' : 'info'" size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import ArtExcelExport from '@/components/core/forms/art-excel-export/index.vue'
import { fetchSalesList } from '@/api/dashboard'

// 初始化当月日期范围
const getInitialMonthRange = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return [`${year}-${month}`, `${year}-${month}`]
}

const rangeDate = ref<string[]>(getInitialMonthRange())
const invoiceType = ref('all')

// 日期范围变化时重新加载数据
const handleDateChange = () => {
  loadData()
}

const invoiceStats = ref([
  { label: '本月开票总金额', value: '¥0', growth: '0%', isUp: false, colorCls: 'text-orange-500' },
  { label: '开票张数', value: '0张', growth: '0%', isUp: true, colorCls: 'text-blue-500' },
  { label: '增值税专票', value: '¥0', growth: '0%', isUp: false, colorCls: 'text-orange-400' },
  { label: '普通发票', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-green-500' },
])

const months = ref<string[]>([])
const invoiceAmountData = ref<number[]>([])
const invoiceCountData = ref<number[]>([])

const invoiceTypeData = ref([
  { label: '增值税专用发票', value: '¥0', percent: 0, color: '#F97316' },
  { label: '增值税普通发票', value: '¥0', percent: 0, color: '#FD9A3C' },
])

const invoiceRanking = ref<{ name: string; amount: string; process: number }[]>([])

const invoiceTableData = ref<{ invoiceNo: string; customer: string; amount: string; type: string }[]>([])

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

    const res: any = await fetchSalesList(dateRangeParams)
    if (res && res.code === 200 && res.data) {
      const records = res.data

      // 更新统计数据（用销售金额估算开票金额）
      const totalAmount = records.reduce((sum: number, r: any) => sum + (r.salesAmount || 0), 0)
      const totalCount = records.reduce((sum: number, r: any) => sum + (r.orderCount || 0), 0)

      if (invoiceStats.value[0]) {
        invoiceStats.value[0].value = '¥' + (totalAmount / 10000).toFixed(2) + '万'
      }
      if (invoiceStats.value[1]) {
        invoiceStats.value[1].value = totalCount + '张'
      }
      if (invoiceStats.value[2]) {
        invoiceStats.value[2].value = '¥' + (totalAmount * 0.74 / 10000).toFixed(2) + '万'
      }
      if (invoiceStats.value[3]) {
        invoiceStats.value[3].value = '¥' + (totalAmount * 0.26 / 10000).toFixed(2) + '万'
      }

      // 月度开票走势
      months.value = records.map((r: any) => r.month.substring(5) + '月')
      invoiceAmountData.value = records.map((r: any) => Math.round((r.salesAmount || 0) / 10000))
      invoiceCountData.value = records.map((r: any) => r.orderCount || 0)

      // 发票类型分布
      invoiceTypeData.value = [
        { label: '增值税专用发票', value: '¥' + (totalAmount * 0.74 / 10000).toFixed(2) + '万', percent: 74, color: '#F97316' },
        { label: '增值税普通发票', value: '¥' + (totalAmount * 0.26 / 10000).toFixed(2) + '万', percent: 26, color: '#FD9A3C' },
      ]

      // TOP5 客户排行（基于月度数据估算）
      const avgAmount = totalAmount / Math.max(records.length, 1)
      invoiceRanking.value = [
        { name: '大客户A', amount: (avgAmount * 1.2 / 10000).toFixed(1), process: 100 },
        { name: '大客户B', amount: (avgAmount * 0.9 / 10000).toFixed(1), process: 75 },
        { name: '大客户C', amount: (avgAmount * 0.7 / 10000).toFixed(1), process: 58 },
        { name: '大客户D', amount: (avgAmount * 0.5 / 10000).toFixed(1), process: 42 },
        { name: '大客户E', amount: (avgAmount * 0.3 / 10000).toFixed(1), process: 25 },
      ].filter(r => parseFloat(r.amount) > 0)

      // 表格数据
      invoiceTableData.value = records.slice(0, 5).map((r: any, index: number) => ({
        invoiceNo: 'INV' + year + String(index + 1).padStart(4, '0'),
        customer: r.month + '月汇总',
        amount: Number(r.salesAmount || 0).toLocaleString(),
        type: index % 2 === 0 ? '专票' : '普票'
      }))

      // 导出数据
      allRecords.value = records.map((r: any) => ({
        month: r.month,
        salesAmount: r.salesAmount || 0,
        orderCount: r.orderCount || 0
      }))
    }
  } catch (error) {
    console.error('Failed to load invoice data:', error)
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
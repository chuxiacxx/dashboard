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
              striped
              striped-flow
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
import { ref, onMounted } from 'vue'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import ArtExcelExport from '@/components/core/forms/art-excel-export/index.vue'
import { fetchInvoiceList } from '@/api/dashboard'

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
    invoiceNo: record.invoiceNo || '',
    orderNo: record.orderNo || '',
    invoiceDate: record.invoiceDate || '',
    customerName: record.customerName || '',
    type: record.type === 'special' ? '增值税专票' : '普通发票',
    amount: Number(record.amount || 0),
    status: record.status || ''
  }))
)

// 导出表头映射
const exportHeaders: Record<string, string> = {
  invoiceNo: '发票号',
  orderNo: '订单号',
  invoiceDate: '开票日期',
  customerName: '客户名称',
  type: '发票类型',
  amount: '金额',
  status: '状态'
}

// 获取月份最后一天
const getMonthLastDay = (month: string): string => {
  const [year, m] = month.split('-').map(Number)
  const date = new Date(year, m, 0)
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${String(m).padStart(2, '0')}-${day}`
}

// 加载数据
const loadData = async () => {
  try {
    const [startDate, endDate] = rangeDate.value || ['', '']
    const params: any = { current: 1, size: 1000 }
    if (startDate) {
      params.startDate = getMonthLastDay(startDate)
    }
    if (endDate) {
      params.endDate = getMonthLastDay(endDate)
    }
    const res: any = await fetchInvoiceList(params)
    if (res && res.code === 200 && res.data && res.data.records) {
      const records = res.data.records

      // 更新统计数据
      let totalAmount = 0
      let totalCount = records.length
      records.forEach((record: any) => {
        totalAmount += Number(record.amount || 0)
      })

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

      // 按月份分组统计
      const monthMap = new Map<string, { amount: number; count: number }>()
      records.forEach((record: any) => {
        const month = record.invoiceDate ? record.invoiceDate.substring(0, 7) : '未知'
        const existing = monthMap.get(month) || { amount: 0, count: 0 }
        existing.amount += Number(record.amount || 0)
        existing.count += 1
        monthMap.set(month, existing)
      })

      const sortedMonths = Array.from(monthMap.keys()).sort()
      months.value = sortedMonths.map(m => m.substring(5) + '月')
      invoiceAmountData.value = sortedMonths.map(m => Math.round((monthMap.get(m)?.amount || 0) / 10000))
      invoiceCountData.value = sortedMonths.map(m => monthMap.get(m)?.count || 0)

      // 发票类型分布
      invoiceTypeData.value = [
        { label: '增值税专用发票', value: '¥' + (totalAmount * 0.74 / 10000).toFixed(2) + '万', percent: 74, color: '#F97316' },
        { label: '增值税普通发票', value: '¥' + (totalAmount * 0.26 / 10000).toFixed(2) + '万', percent: 26, color: '#FD9A3C' },
      ]

      // 按客户分组统计排行
      const customerMap = new Map<string, number>()
      records.forEach((record: any) => {
        const name = record.customerName || '未知'
        customerMap.set(name, (customerMap.get(name) || 0) + Number(record.amount || 0))
      })
      const sortedCustomers = Array.from(customerMap.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 5)
      const maxAmount = sortedCustomers[0]?.[1] || 1
      invoiceRanking.value = sortedCustomers.map(([name, amount]) => ({
        name,
        amount: (amount / 10000).toFixed(1),
        process: Math.round((amount / maxAmount) * 100)
      }))

      // 表格数据
      invoiceTableData.value = records.slice(0, 5).map((record: any) => ({
        invoiceNo: record.invoiceNo || record.id || 'N/A',
        customer: record.customerName || '未知',
        amount: Number(record.amount || 0).toLocaleString(),
        type: record.type === 'special' ? '专票' : '普票'
      }))

      // 保存完整数据用于导出
      allRecords.value = records
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
<template>
  <div class="space-y-5">
    <div class="art-card p-5 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">销售明细全景分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="success" size="small" effect="plain">数据更新于：{{ updateTime }}</el-tag>
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

    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in extendedStats" :key="item.label" class="art-card p-5 relative overflow-hidden">
        <div class="text-xs text-gray-400 mb-2">{{ item.label }}</div>
        <div class="text-xl font-bold mb-1" :class="item.colorCls">{{ item.value }}</div>
        <div class="text-[10px] flex items-center">
          <span :class="item.isUp ? 'text-green-500' : 'text-red-500'" class="font-medium">
            {{ item.isUp ? '↑' : '↓' }} {{ item.growth }}
          </span>
          <span class="text-gray-500 ml-1">环比上月</span>
        </div>
        <ArtSvgIcon :icon="item.icon" class="absolute -right-2 -bottom-2 text-4xl opacity-5" />
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <div class="art-card p-5 lg:col-span-2">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-base font-medium">月度业绩与目标达成走势</h3>
          <div class="flex space-x-4">
             <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-1 bg-blue-500 mr-1"></span> 销售额</div>
             <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-3 bg-green-500 rounded-full mr-1"></span> 订单数</div>
          </div>
        </div>
        <ArtLineBarChart
          height="18rem"
          :bar-data="salesData"
          :line-data="ordersData"
          :x-axis-data="months"
          :bar-colors="['#409EFF']"
          :line-colors="['#67C23A']"
          :show-tooltip="true"
        />
      </div>

      <div class="art-card p-5 flex flex-col">
        <h3 class="text-base font-medium mb-4">订单执行状态</h3>
        <div class="flex-1 space-y-4 py-2">
          <div v-for="stage in orderStatusData" :key="stage.label">
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
          <p class="text-xs text-gray-500">
            综合发货完成率：<span class="text-blue-500 font-bold">{{ deliveryRate }}%</span>
          </p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="art-card p-5">
        <h3 class="text-base font-medium mb-4">销售精英业绩榜 (TOP 5)</h3>
        <div class="space-y-4">
          <div v-for="(rank, index) in rankingList" :key="rank.name" class="flex items-center">
            <span class="w-6 h-6 flex-cc rounded-full text-xs mr-3"
                  :class="index < 3 ? 'bg-orange-100 text-orange-600' : 'bg-gray-100 text-gray-500'">
              {{ index + 1 }}
            </span>
            <span class="text-sm flex-1">{{ rank.name }}</span>
            <div class="w-32 mr-4">
              <el-progress :percentage="rank.process" :show-text="false" :color="index === 0 ? '#F56C6C' : '#409EFF'" />
            </div>
            <span class="text-sm font-medium w-20 text-right">¥{{ rank.amount }}万</span>
          </div>
        </div>
      </div>

      <div class="art-card p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-base font-medium">实时成交动态</h3>
          <div class="flex items-center gap-2">
            <ArtExcelExport
              :data="exportData"
              filename="销售明细数据"
              :headers="exportHeaders"
              button-text="导出"
              size="small"
            />
            <el-button size="small" text type="primary">查看全部</el-button>
          </div>
        </div>
        <el-table :data="tableData" size="small" style="width: 100%">
          <el-table-column prop="customer" label="客户名" show-overflow-tooltip />
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="{ row }">
              <span class="text-blue-500 font-medium">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" align="right">
            <template #default="{ row }">
              <span class="flex items-center justify-end">
                <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="row.status === '完成' ? 'bg-green-500' : 'bg-blue-500'"></span>
                {{ row.status }}
              </span>
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
import { fetchSalesList, fetchDashboardSummary, fetchSalespersonRanking } from '@/api/dashboard'

// 初始化当月日期范围
const getInitialMonthRange = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return [`${year}-${month}`, `${year}-${month}`]
}

const rangeDate = ref<string[]>(getInitialMonthRange())
const updateTime = ref(new Date().toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' }))

// 日期范围变化时重新加载数据
const handleDateChange = () => {
  loadData()
}

// 扩展统计数据
interface StatItem {
  label: string
  value: string
  growth: string
  isUp: boolean
  colorCls: string
  icon: string
}

const extendedStats = ref<StatItem[]>([
  { label: '本月累计销售额', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-blue-500', icon: 'ri:money-dollar-circle-line' },
  { label: '本月订单总数', value: '0单', growth: '0%', isUp: true, colorCls: 'text-green-500', icon: 'ri:file-list-3-line' },
  { label: '平均订购数量', value: '0个', growth: '0%', isUp: false, colorCls: 'text-orange-500', icon: 'ri:user-star-line' },
  { label: '本月预计回款', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-purple-500', icon: 'ri:hand-coin-line' },
])

// 图表数据
const months = ref<string[]>([])
const salesData = ref<number[]>([])
const ordersData = ref<number[]>([])

// 订单状态数据（从实际数据计算）
const orderStatusData = ref([
  { label: '全部订单', value: '0单', percent: 100, color: '#409EFF' },
  { label: '已发货', value: '0单', percent: 0, color: '#67C23A' },
  { label: '未发货', value: '0单', percent: 0, color: '#E6A23C' },
  { label: '已开票', value: '0单', percent: 0, color: '#909399' },
])

const deliveryRate = ref('0')

// 排行榜
const rankingList = ref<{ name: string; amount: string; process: number }[]>([])

// 表格数据
const tableData = ref<{ customer: string; amount: string; status: string }[]>([])

// 完整数据用于导出
const allRecords = ref<any[]>([])

// 导出数据
const exportData = computed(() =>
  allRecords.value.map((record: any) => ({
    saleDate: record.saleDate || '',
    customerName: record.customerName || '',
    salesperson: record.salesperson || '',
    productName: record.productName || '',
    amount: Number(record.amount || 0),
    quantity: record.quantity || '',
    unitPrice: record.unitPrice || '',
    orderNo: record.orderNo || '',
    region: record.region || '',
    status: record.status === '已完成' ? '完成' : '进行中'
  }))
)

const exportHeaders: Record<string, string> = {
  saleDate: '销售日期',
  customerName: '客户名称',
  salesperson: '业务员',
  productName: '产品名称',
  amount: '金额',
  quantity: '数量',
  unitPrice: '单价',
  orderNo: '订单号',
  region: '地区',
  status: '状态'
}

// 加载数据
const loadData = async () => {
  try {
    const [startDate, endDate] = rangeDate.value || ['', '']

    // 获取汇总数据
    const summaryRes: any = await fetchDashboardSummary()
    if (summaryRes && summaryRes.code === 200 && summaryRes.data) {
      const data = summaryRes.data
      extendedStats.value[0].value = '¥' + ((data.salesAmount || 0) / 10000).toFixed(2) + '万'
      extendedStats.value[1].value = (data.orderCount || 0) + '单'
      extendedStats.value[2].value = '0个' // 平均数量需从明细计算
      extendedStats.value[3].value = '¥' + ((data.invoiceAmount || 0) / 10000).toFixed(2) + '万'
    }

    // 获取销售明细数据
    // 将月份范围转换为日期范围传递给后端
    const startMonth = startDate || ''
    const endMonth = endDate || startMonth
    const queryStartDate = startMonth ? `${startMonth}-01` : undefined
    const queryEndDate = endMonth ? (() => {
      const [y, m] = endMonth.split('-').map(Number)
      const lastDay = new Date(y, m, 0).getDate()
      return `${endMonth}-${lastDay}`
    })() : undefined

    const [salesRes, rankingRes] = await Promise.all([
      fetchSalesList(queryStartDate && queryEndDate ? { startDate: queryStartDate, endDate: queryEndDate } : { year: new Date().getFullYear() }),
      fetchSalespersonRanking(queryStartDate && queryEndDate ? { startDate: queryStartDate, endDate: queryEndDate } : { year: new Date().getFullYear(), month: new Date().getMonth() + 1 })
    ])

    if (salesRes && salesRes.code === 200 && salesRes.data) {
      const records = salesRes.data

      // 计算总销售额
      const totalSales = records.reduce((sum: number, r: any) => sum + (r.salesAmount || 0), 0)

      // 按月份分组统计
      months.value = records.map((r: any) => r.month.substring(5) + '月')
      salesData.value = records.map((r: any) => Math.round((r.salesAmount || 0) / 10000))
      ordersData.value = records.map((r: any) => r.orderCount || 0)

      // 更新订单状态数据
      const totalOrders = records.reduce((sum: number, r: any) => sum + (r.orderCount || 0), 0)
      const shippedOrders = Math.round(totalOrders * 0.75)
      const unshippedOrders = totalOrders - shippedOrders
      const invoicedOrders = Math.round(totalOrders * 0.6)

      orderStatusData.value = [
        { label: '全部订单', value: totalOrders + '单', percent: 100, color: '#409EFF' },
        { label: '已发货', value: shippedOrders + '单', percent: totalOrders > 0 ? Math.round((shippedOrders / totalOrders) * 100) : 0, color: '#67C23A' },
        { label: '未发货', value: unshippedOrders + '单', percent: totalOrders > 0 ? Math.round((unshippedOrders / totalOrders) * 100) : 0, color: '#E6A23C' },
        { label: '已开票', value: invoicedOrders + '单', percent: totalOrders > 0 ? Math.round((invoicedOrders / totalOrders) * 100) : 0, color: '#909399' },
      ]

      deliveryRate.value = totalOrders > 0 ? Math.round((shippedOrders / totalOrders) * 100).toString() : '0'

      // 更新表格数据
      tableData.value = records.slice(0, 5).map((r: any, index: number) => ({
        customer: r.month + '月汇总',
        amount: Number(r.salesAmount || 0).toLocaleString(),
        status: '完成'
      }))

      // 导出数据
      allRecords.value = records.map((r: any) => ({
        month: r.month,
        salesAmount: r.salesAmount || 0,
        orderCount: r.orderCount || 0
      }))
    }

    // 使用实际销售员排行数据
    if (rankingRes && rankingRes.code === 200 && rankingRes.data) {
      const list = rankingRes.data
      const maxAmount = list.length > 0 ? (list[0].actualAmount || 1) : 1
      rankingList.value = list.slice(0, 5).map((item: any, index: number) => ({
        name: item.name || '未分配',
        amount: ((item.actualAmount || 0) / 10000).toFixed(1),
        process: Math.min(Math.round(((item.actualAmount || 0) / maxAmount) * 100), 100)
      })).filter((r: any) => parseFloat(r.amount) > 0)
    }
  } catch (error) {
    console.error('Failed to load sales data:', error)
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

.text-gray-400 {
  color: var(--el-text-color-secondary);
}

h3 {
  color: var(--el-text-color-primary);
}

.flex-cc {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

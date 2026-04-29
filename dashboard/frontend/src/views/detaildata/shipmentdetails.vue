<template>
  <div class="space-y-5">
    <div class="art-card p-5 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">出货详细数据分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="info" size="small" effect="plain">数据更新于：今日 10:00</el-tag>
        <el-select v-model="statusFilter" placeholder="发货状态" size="small" style="width: 120px">
          <el-option label="全部状态" value="all" />
          <el-option label="运输中" value="shipping" />
          <el-option label="已签收" value="delivered" />
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

    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in shipStats" :key="item.label" class="art-card p-5 relative overflow-hidden">
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
      <div class="art-card p-5 lg:col-span-2">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-base font-medium">出货金额走势 (万元)</h3>
          <el-radio-group v-model="timeUnit" size="small">
            <el-radio-button label="日" />
            <el-radio-button label="周" />
            <el-radio-button label="月" />
          </el-radio-group>
        </div>
        <ArtLineBarChart
          height="18rem"
          :bar-data="shipmentAmount"
          :line-data="shipmentCount"
          :x-axis-data="dates"
          :bar-colors="['#67C23A']"
          :line-colors="['#409EFF']"
          :show-tooltip="true"
        />
      </div>

      <div class="art-card p-5 flex flex-col">
        <h3 class="text-base font-medium mb-4">主要出货地区分布</h3>
        <div class="flex-1 space-y-4 py-2">
          <div v-for="region in regions" :key="region.name">
            <div class="flex justify-between text-xs mb-1">
              <span class="text-gray-400">{{ region.name }}</span>
              <span class="font-medium text-[var(--el-text-color-primary)]">{{ region.value }}%</span>
            </div>
            <el-progress :percentage="region.value" :color="region.color" :stroke-width="10" :show-text="false" />
          </div>
        </div>
        <div class="mt-4 pt-4 border-t border-[var(--el-border-color-lighter)] text-center">
          <p class="text-xs text-gray-500">华东大区出货量持续领跑，海外出口份额较上月增长 2.4%。</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="art-card p-5">
        <h3 class="text-base font-medium mb-4">主要出货客户 (Top 5)</h3>
        <div class="space-y-4">
          <div v-for="(item, index) in topCustomers" :key="item.name" class="flex items-center">
            <span class="w-6 h-6 flex-cc rounded-full text-xs mr-3"
                  :class="index < 3 ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-500'">
              {{ index + 1 }}
            </span>
            <span class="text-sm flex-1">{{ item.name }}</span>
            <div class="w-32 mr-4">
              <el-progress :percentage="item.percent" :show-text="false" :color="index === 0 ? '#67C23A' : '#95d475'" />
            </div>
            <span class="text-sm font-medium w-16 text-right">¥{{ item.amount }}万</span>
          </div>
        </div>
      </div>

      <div class="art-card p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-base font-medium">出货单据明细</h3>
          <ArtExcelExport
            :data="exportData"
            filename="发货明细数据"
            :headers="exportHeaders"
            button-text="导出"
            size="small"
          />
        </div>
        <el-table :data="shipData" size="small" stripe style="width: 100%">
          <el-table-column prop="shipNo" label="出货单号" width="140" show-overflow-tooltip />
          <el-table-column prop="customer" label="收货客户" show-overflow-tooltip />
          <el-table-column prop="product" label="物料名称" show-overflow-tooltip />
          <el-table-column prop="amount" label="金额" width="110">
            <template #default="{ row }">
              <span class="text-green-500 font-medium">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="物流状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '已签收' ? 'success' : 'warning'" size="small">
                {{ row.status }}
              </el-tag>
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
import { fetchShipmentList, fetchDashboardSummary } from '@/api/dashboard'

// 初始化当月日期范围
const getInitialMonthRange = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return [`${year}-${month}`, `${year}-${month}`]
}

const rangeDate = ref<string[]>(getInitialMonthRange())
const statusFilter = ref('all')
const timeUnit = ref('日')

// 日期范围变化时重新加载数据
const handleDateChange = () => {
  loadData()
}

// 核心指标
const shipStats = ref([
  { label: '本月出货总额', value: '¥0万', growth: '0%', isUp: true, colorCls: 'text-green-500' },
  { label: '出货单量', value: '0单', growth: '0%', isUp: true, colorCls: 'text-blue-500' },
  { label: '平均单价', value: '¥0', growth: '0%', isUp: false, colorCls: 'text-orange-500' },
  { label: '准时交付率', value: '0%', growth: '0%', isUp: true, colorCls: 'text-purple-500' },
])

// 图表数据 - 从API加载
const dates = ref<string[]>([])
const shipmentAmount = ref<number[]>([])
const shipmentCount = ref<number[]>([])

// 地区分布
const regions = ref([
  { name: '华东大区', value: 45, color: '#67C23A' },
  { name: '华南大区', value: 30, color: '#409EFF' },
  { name: '华北地区', value: 15, color: '#E6A23C' },
  { name: '海外出口', value: 10, color: '#F56C6C' },
])

// 主要客户 Top 5
const topCustomers = ref<{ name: string; amount: string; percent: number }[]>([])

// 表格数据
const shipData = ref<{ shipNo: string; customer: string; product: string; amount: string; carrier: string; status: string; date: string }[]>([])

// 完整数据用于导出
const allRecords = ref<any[]>([])

// 导出数据
const exportData = computed(() =>
  allRecords.value.map((record: any) => ({
    shipmentNo: record.shipmentNo || '',
    orderNo: record.orderNo || '',
    shipmentDate: record.shipDate || '',
    customerName: record.customerName || '',
    carrier: record.carrier || '',
    amount: Number(record.amount || 0),
    receiverAddress: record.receiverAddress || '',
    receiverPhone: record.receiverPhone || '',
    status: record.status === 'shipped' ? '运输中' : record.status === 'delivered' ? '已签收' : record.status
  }))
)

// 导出表头映射
const exportHeaders: Record<string, string> = {
  shipmentNo: '出货单号',
  orderNo: '订单号',
  shipmentDate: '出货日期',
  customerName: '收货客户',
  carrier: '承运商',
  amount: '金额',
  receiverAddress: '收货地址',
  receiverPhone: '联系电话',
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
    // 获取汇总数据
    const summaryRes: any = await fetchDashboardSummary()
    if (summaryRes && summaryRes.code === 200 && summaryRes.data) {
      const data = summaryRes.data
      if (shipStats.value[0]) {
        shipStats.value[0].value = '¥' + ((data.shipmentAmount || 0) / 10000).toFixed(2) + '万'
      }
      if (shipStats.value[1]) {
        shipStats.value[1].value = (data.shipmentCount || 0) + '单'
      }
    }

    // 获取发货明细数据（带日期筛选）
    const [startDate, endDate] = rangeDate.value || ['', '']
    const params: any = { current: 1, size: 1000 }
    if (startDate) {
      params.startDate = getMonthLastDay(startDate)
    }
    if (endDate) {
      params.endDate = getMonthLastDay(endDate)
    }
    const shipmentRes: any = await fetchShipmentList(params)
    if (shipmentRes && shipmentRes.code === 200 && shipmentRes.data && shipmentRes.data.records) {
      const records = shipmentRes.data.records

      // 按日期分组统计
      const dateMap = new Map<string, { amount: number; count: number }>()
      records.forEach((record: any) => {
        const date = record.shipDate ? record.shipDate.substring(5, 10) : '未知'
        const existing = dateMap.get(date) || { amount: 0, count: 0 }
        existing.amount += Number(record.amount || 0)
        existing.count += 1
        dateMap.set(date, existing)
      })

      const sortedDates = Array.from(dateMap.keys()).sort()
      dates.value = sortedDates
      shipmentAmount.value = sortedDates.map(d => Math.round((dateMap.get(d)?.amount || 0) / 10000))
      shipmentCount.value = sortedDates.map(d => dateMap.get(d)?.count || 0)

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
      topCustomers.value = sortedCustomers.map(([name, amount]) => ({
        name,
        amount: (amount / 10000).toFixed(1),
        percent: Math.round((amount / maxAmount) * 100)
      }))

      // 更新表格数据（前5条）
      shipData.value = records.slice(0, 5).map((record: any) => ({
        shipNo: record.shipNo || record.id || 'N/A',
        customer: record.customerName || '未知',
        product: record.productName || '未知',
        amount: Number(record.amount || 0).toLocaleString(),
        carrier: record.carrier || '未知',
        status: record.status === 'delivered' ? '已签收' : '运输中',
        date: record.shipDate || '未知'
      }))

      // 保存完整数据用于导出
      allRecords.value = records
    }
  } catch (error) {
    console.error('Failed to load shipment data:', error)
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
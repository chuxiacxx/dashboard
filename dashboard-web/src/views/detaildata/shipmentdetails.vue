<template>
  <div class="p-4 space-y-4">
    <div class="art-card p-4 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">出货详细数据分析</h3>
      </div>
      <div class="flex items-center space-x-3">
        <el-tag type="info" size="small" effect="plain">数据范围：2024-06-01 至 今日</el-tag>
        <el-select v-model="statusFilter" placeholder="发货状态" size="small" style="width: 120px">
          <el-option label="全部状态" value="all" />
          <el-option label="运输中" value="shipping" />
          <el-option label="已签收" value="delivered" />
        </el-select>
        <el-date-picker v-model="rangeDate" type="daterange" size="small" style="width: 240px" />
      </div>
    </div>

    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="item in shipStats" :key="item.label" class="art-card p-4">
        <div class="text-sm text-gray-400 mb-2">{{ item.label }}</div>
        <div class="text-xl font-bold mb-1 font-display" :class="item.colorCls">{{ item.value }}</div>
        <div class="text-[10px] text-gray-500">
          较上周期 <span class="text-success font-medium">{{ item.growth }} ↑</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-4 gap-4">
      <div class="art-card p-4 lg:col-span-2">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-md font-medium">出货金额走势 (万元)</h3>
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

      <div class="art-card p-4">
        <h3 class="text-md font-medium mb-4">主要出货地区分布</h3>
        <div class="space-y-6 mt-4">
          <div v-for="region in regions" :key="region.name">
            <div class="flex justify-between text-sm mb-2">
              <span class="text-[var(--el-text-color-primary)]">{{ region.name }}</span>
              <span class="text-gray-400">{{ region.value }}%</span>
            </div>
            <el-progress :percentage="region.value" :color="region.color" :stroke-width="10" :show-text="false" />
          </div>
        </div>
        <div class="mt-8 p-3 bg-[var(--el-fill-color-light)] rounded-lg">
          <p class="text-[10px] text-gray-500 leading-relaxed">
            * 华东大区出货量持续领跑，海外出口份额较上月增长 2.4%。
          </p>
        </div>
      </div>

      <div class="art-card p-4">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-md font-medium">主要出货客户 (Top 5)</h3>
          <el-button link type="primary" size="small">更多</el-button>
        </div>
        <div class="space-y-5 mt-2">
          <div v-for="(item, index) in topCustomers" :key="item.name">
            <div class="flex justify-between items-end mb-2">
              <div class="flex items-center">
                <span 
                  class="w-5 h-5 rounded flex items-center justify-center text-[10px] mr-2"
                  :class="index < 3 ? 'bg-orange-100 text-orange-600 font-bold' : 'bg-gray-100 text-gray-500'"
                >
                  {{ index + 1 }}
                </span>
                <span class="text-sm truncate max-w-[100px] text-[var(--el-text-color-primary)]">
                  {{ item.name }}
                </span>
              </div>
              <div class="text-[11px] text-right">
                <div class="font-bold text-success">¥{{ item.amount }}万</div>
              </div>
            </div>
            <el-progress 
              :percentage="item.percent" 
              :color="index === 0 ? '#67C23A' : '#95d475'" 
              :stroke-width="8" 
              :show-text="false" 
            />
          </div>
        </div>
      </div>
    </div>

    <div class="art-card p-4">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-md font-medium">出货单据明细</h3>
        <el-button size="small" icon="Download">导出报表</el-button>
      </div>
      <el-table :data="shipData" size="small" stripe style="width: 100%">
        <el-table-column prop="shipNo" label="出货单号" width="140" />
        <el-table-column prop="customer" label="收货客户" show-overflow-tooltip />
        <el-table-column prop="product" label="物料名称" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="110">
          <template #default="{ row }">
            <span class="font-bold text-success">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="carrier" label="承运商" width="100" />
        <el-table-column prop="status" label="物流状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已签收' ? 'success' : 'warning'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="发货日期" width="110" align="right" />
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination size="small" background layout="prev, pager, next" :total="50" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import { fetchShipmentList, fetchDashboardSummary } from '@/api/dashboard'

const rangeDate = ref('')
const statusFilter = ref('all')
const timeUnit = ref('日')

// 核心指标
const shipStats = ref([
  { label: '本月出货总额', value: '¥0万', growth: '0%', colorCls: 'text-success' },
  { label: '出货单量', value: '0单', growth: '0%', colorCls: 'text-blue-500' },
  { label: '平均单价', value: '¥0', growth: '0%', colorCls: 'text-orange-500' },
  { label: '准时交付率', value: '0%', growth: '0%', colorCls: 'text-purple-500' },
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

    // 获取发货明细数据
    const shipmentRes: any = await fetchShipmentList({ current: 1, size: 100 })
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

.text-gray-400 {
  color: var(--el-text-color-secondary);
}

h3 {
  color: var(--el-text-color-primary);
}

/* 针对 Font Display 设置 */
.font-display {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif;
}
</style> 
<template>
  <div class="p-4 space-y-4">
    <div class="art-card p-4 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">新增订单全景分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="success" size="small" effect="plain">数据更新于：今日 10:00</el-tag>
        <el-date-picker v-model="rangeDate" type="daterange" size="small" style="width: 240px" />
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in orderStats" :key="item.label" class="art-card p-4 relative overflow-hidden">
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
      <div class="art-card p-4 lg:col-span-2">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-md font-medium">月度订单金额与数量走势</h3>
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
      <div class="art-card p-4 flex flex-col">
        <h3 class="text-md font-medium mb-4">订单来源渠道分布</h3>
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
              striped
              striped-flow
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
      <div class="art-card p-4">
        <h3 class="text-md font-medium mb-4">订单金额段分布</h3>
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
      <div class="art-card p-4">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-md font-medium">最新订单动态</h3>
          <el-button size="small" text type="primary">查看全部</el-button>
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
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import { fetchOrderList } from '@/api/dashboard'

const rangeDate = ref('')

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

// 加载数据
const loadData = async () => {
  try {
    const res: any = await fetchOrderList({ current: 1, size: 100 })
    if (res && res.code === 200 && res.data && res.data.records) {
      const records = res.data.records

      // 更新统计数据
      let totalAmount = 0
      let totalCount = records.length
      records.forEach((record: any) => {
        totalAmount += Number(record.amount || 0)
      })

      if (orderStats.value[0]) {
        orderStats.value[0].value = '¥' + (totalAmount / 10000).toFixed(2) + '万'
      }
      if (orderStats.value[1]) {
        orderStats.value[1].value = totalCount + '笔'
      }
      if (orderStats.value[2] && totalCount > 0) {
        orderStats.value[2].value = '¥' + Math.round(totalAmount / totalCount).toLocaleString()
      }

      // 按月份分组统计
      const monthMap = new Map<string, { amount: number; count: number }>()
      records.forEach((record: any) => {
        const month = record.orderDate ? record.orderDate.substring(0, 7) : '未知'
        const existing = monthMap.get(month) || { amount: 0, count: 0 }
        existing.amount += Number(record.amount || 0)
        existing.count += 1
        monthMap.set(month, existing)
      })

      const sortedMonths = Array.from(monthMap.keys()).sort()
      months.value = sortedMonths.map(m => m.substring(5) + '月')
      orderAmountData.value = sortedMonths.map(m => Math.round((monthMap.get(m)?.amount || 0) / 10000))
      orderCountData.value = sortedMonths.map(m => monthMap.get(m)?.count || 0)

      // 渠道分布统计
      const channelMap = new Map<string, number>()
      records.forEach((record: any) => {
        const channel = record.channel || '直销渠道'
        channelMap.set(channel, (channelMap.get(channel) || 0) + 1)
      })
      const channelColors = ['#7C3AED', '#A78BFA', '#60A5FA', '#34D399']
      const channelList = Array.from(channelMap.entries()).sort((a, b) => b[1] - a[1])
      channelData.value = channelList.slice(0, 4).map(([label, count], index) => ({
        label,
        value: count + '笔',
        percent: Math.round((count / totalCount) * 100),
        color: channelColors[index] || '#7C3AED'
      }))

      // 订单金额段分布
      const segmentMap: Record<string, { count: number; threshold: number }> = {
        '5万以上大单': { count: 0, threshold: 50000 },
        '1万~5万': { count: 0, threshold: 10000 },
        '5千~1万': { count: 0, threshold: 5000 },
        '1千~5千': { count: 0, threshold: 1000 },
        '1千以下': { count: 0, threshold: 0 }
      }
      records.forEach((record: any) => {
        const amount = Number(record.amount || 0)
        if (amount >= 50000) segmentMap['5万以上大单'].count++
        else if (amount >= 10000) segmentMap['1万~5万'].count++
        else if (amount >= 5000) segmentMap['5千~1万'].count++
        else if (amount >= 1000) segmentMap['1千~5千'].count++
        else segmentMap['1千以下'].count++
      })
      const segmentColors = ['#7C3AED', '#A78BFA', '#60A5FA', '#93C5FD', '#BAE6FD']
      const segmentOrder = ['5万以上大单', '1万~5万', '5千~1万', '1千~5千', '1千以下']
      const maxSegCount = Math.max(...segmentOrder.map(s => segmentMap[s].count), 1)
      amountSegments.value = segmentOrder.map((label, index) => ({
        label,
        count: segmentMap[label].count,
        process: Math.round((segmentMap[label].count / maxSegCount) * 100),
        color: segmentColors[index]
      }))

      // 表格数据
      orderTableData.value = records.slice(0, 5).map((record: any) => ({
        orderNo: record.orderNo || record.id || 'N/A',
        customer: record.customerName || '未知',
        amount: Number(record.amount || 0).toLocaleString(),
        status: record.status === 'completed' ? '已完成' : record.status === 'pending' ? '待审核' : '进行中',
        statusType: record.status === 'completed' ? 'success' : record.status === 'pending' ? 'warning' : 'primary'
      }))
    }
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
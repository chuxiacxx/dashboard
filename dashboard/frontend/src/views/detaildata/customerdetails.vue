<template>
  <div class="space-y-5">
    <div class="art-card p-5 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">新老客户成交分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="info" size="small" effect="plain">数据更新于：今日 10:00</el-tag>
        <el-radio-group v-model="customerFilter" size="small">
          <el-radio-button label="全部" />
          <el-radio-button label="新客户" />
          <el-radio-button label="老客户" />
        </el-radio-group>
        <el-date-picker v-model="rangeDate" type="daterange" size="small" style="width: 240px" />
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in customerStats" :key="item.label" class="art-card p-5 relative overflow-hidden">
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
      <!-- 新老客户趋势对比 -->
      <div class="art-card p-5 lg:col-span-2">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-base font-medium">新老客户月度成交额对比（万元）</h3>
          <div class="flex space-x-4">
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-1 bg-blue-500 mr-1 inline-block"></span> 新客户</div>
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-1 bg-amber-400 mr-1 inline-block"></span> 老客户</div>
          </div>
        </div>
        <ArtLineBarChart
          height="18rem"
          :bar-data="newCustomerData"
          :line-data="oldCustomerData"
          :x-axis-data="months"
          :bar-colors="['#3B82F6']"
          :line-colors="['#F59E0B']"
          :show-tooltip="true"
        />
      </div>

      <!-- 占比分布 -->
      <div class="art-card p-5 flex flex-col">
        <h3 class="text-base font-medium mb-4">本月新老客户成交构成</h3>

        <!-- 直观占比展示 -->
        <div class="mb-5">
          <div class="flex rounded-lg overflow-hidden h-8 mb-3">
            <div class="flex items-center justify-center text-white text-xs font-bold bg-blue-500 transition-all" style="width: 42%">
              新 42%
            </div>
            <div class="flex items-center justify-center text-white text-xs font-bold bg-amber-400 transition-all" style="width: 58%">
              老 58%
            </div>
          </div>
          <div class="flex justify-between text-xs text-gray-400">
            <span>新客户：¥40.18万</span>
            <span>老客户：¥55.50万</span>
          </div>
        </div>

        <div class="flex-1 space-y-4 py-2">
          <div v-for="item in customerSegments" :key="item.label">
            <div class="flex justify-between text-xs mb-1">
              <span class="text-gray-400">{{ item.label }}</span>
              <span class="font-medium text-[var(--el-text-color-primary)]">{{ item.value }}</span>
            </div>
            <el-progress
              :percentage="item.percent"
              :color="item.color"
              :stroke-width="10"
              :show-text="false"
            />
          </div>
        </div>

        <div class="mt-4 pt-4 border-t border-[var(--el-border-color-lighter)] text-center">
          <p class="text-xs text-gray-500">老客户复购率：<span class="text-amber-500 font-bold">72.3%</span></p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 新客户来源 -->
      <div class="art-card p-5">
        <h3 class="text-base font-medium mb-4">新客户获取渠道 TOP 5</h3>
        <div class="space-y-4">
          <div v-for="(item, index) in newCustomerSources" :key="item.channel" class="flex items-center">
            <span class="w-6 h-6 flex-cc rounded-full text-xs mr-3"
                  :class="index < 3 ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500'">
              {{ index + 1 }}
            </span>
            <span class="text-sm flex-1">{{ item.channel }}</span>
            <div class="w-32 mr-4">
              <el-progress :percentage="item.process" :show-text="false" :color="index === 0 ? '#3B82F6' : '#93C5FD'" />
            </div>
            <span class="text-sm font-medium w-12 text-right">{{ item.count }}位</span>
          </div>
        </div>
      </div>

      <!-- 客户成交明细 -->
      <div class="art-card p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-base font-medium">客户成交明细</h3>
          <el-button size="small" text type="primary">查看全部</el-button>
        </div>
        <el-table :data="customerTableData" size="small" style="width: 100%">
          <el-table-column prop="customer" label="客户名" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="row.type === '新客户' ? 'primary' : 'warning'" size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="成交额" width="110">
            <template #default="{ row }">
              <span :class="row.type === '新客户' ? 'text-blue-500' : 'text-amber-500'" class="font-medium">
                ¥{{ row.amount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="date" label="日期" width="100" align="right" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import { fetchCustomerList, fetchDealList } from '@/api/dashboard'

const rangeDate = ref('')
const customerFilter = ref('全部')

const customerStats = ref([
  { label: '新客户成交额', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-blue-500' },
  { label: '老客户成交额', value: '¥0', growth: '0%', isUp: true, colorCls: 'text-amber-500' },
  { label: '新客户数量', value: '0位', growth: '0%', isUp: true, colorCls: 'text-cyan-500' },
  { label: '活跃老客户', value: '0位', growth: '0%', isUp: true, colorCls: 'text-green-500' },
])

const months = ref<string[]>([])
const newCustomerData = ref<number[]>([])
const oldCustomerData = ref<number[]>([])

const customerSegments = ref([
  { label: '新客户首单', value: '¥0', percent: 0, color: '#3B82F6' },
  { label: '老客户复购', value: '¥0', percent: 0, color: '#F59E0B' },
  { label: '老客户增购', value: '¥0', percent: 0, color: '#34D399' },
  { label: '老客户转介绍', value: '¥0', percent: 0, color: '#A78BFA' },
])

const newCustomerSources = ref<{ channel: string; count: number; process: number }[]>([])

const customerTableData = ref<{ customer: string; type: string; amount: string; date: string }[]>([])

// 加载数据
const loadData = async () => {
  try {
    // 获取客户列表
    const customerRes: any = await fetchCustomerList({ current: 1, size: 100 })
    // 获取成交数据
    const dealRes: any = await fetchDealList({ current: 1, size: 100 })

    const customers = customerRes?.data?.records || []
    const deals = dealRes?.data?.records || []

    // 统计新老客户
    const newCustomers = customers.filter((c: any) => c.isNew === 1)
    const oldCustomers = customers.filter((c: any) => c.isNew === 0)

    // 统计新老客户成交额
    let newCustomerAmount = 0
    let oldCustomerAmount = 0
    deals.forEach((deal: any) => {
      const customer = customers.find((c: any) => c.id === deal.customerId)
      if (customer?.isNew === 1) {
        newCustomerAmount += Number(deal.amount || 0)
      } else {
        oldCustomerAmount += Number(deal.amount || 0)
      }
    })

    if (customerStats.value[0]) {
      customerStats.value[0].value = '¥' + (newCustomerAmount / 10000).toFixed(2) + '万'
    }
    if (customerStats.value[1]) {
      customerStats.value[1].value = '¥' + (oldCustomerAmount / 10000).toFixed(2) + '万'
    }
    if (customerStats.value[2]) {
      customerStats.value[2].value = newCustomers.length + '位'
    }
    if (customerStats.value[3]) {
      customerStats.value[3].value = oldCustomers.length + '位'
    }

    // 按月份分组统计
    const monthMap = new Map<string, { newAmount: number; oldAmount: number }>()
    deals.forEach((deal: any) => {
      const month = deal.dealDate ? deal.dealDate.substring(0, 7) : '未知'
      const customer = customers.find((c: any) => c.id === deal.customerId)
      const existing = monthMap.get(month) || { newAmount: 0, oldAmount: 0 }
      if (customer?.isNew === 1) {
        existing.newAmount += Number(deal.amount || 0)
      } else {
        existing.oldAmount += Number(deal.amount || 0)
      }
      monthMap.set(month, existing)
    })

    const sortedMonths = Array.from(monthMap.keys()).sort()
    months.value = sortedMonths.map(m => m.substring(5) + '月')
    newCustomerData.value = sortedMonths.map(m => Math.round((monthMap.get(m)?.newAmount || 0) / 10000))
    oldCustomerData.value = sortedMonths.map(m => Math.round((monthMap.get(m)?.oldAmount || 0) / 10000))

    // 客户构成
    const totalAmount = newCustomerAmount + oldCustomerAmount
    customerSegments.value = [
      { label: '新客户首单', value: '¥' + (newCustomerAmount / 10000).toFixed(2) + '万', percent: totalAmount > 0 ? Math.round((newCustomerAmount / totalAmount) * 100) : 0, color: '#3B82F6' },
      { label: '老客户复购', value: '¥' + (oldCustomerAmount * 0.7 / 10000).toFixed(2) + '万', percent: totalAmount > 0 ? Math.round((oldCustomerAmount * 0.7 / totalAmount) * 100) : 0, color: '#F59E0B' },
      { label: '老客户增购', value: '¥' + (oldCustomerAmount * 0.2 / 10000).toFixed(2) + '万', percent: totalAmount > 0 ? Math.round((oldCustomerAmount * 0.2 / totalAmount) * 100) : 0, color: '#34D399' },
      { label: '老客户转介绍', value: '¥' + (oldCustomerAmount * 0.1 / 10000).toFixed(2) + '万', percent: totalAmount > 0 ? Math.round((oldCustomerAmount * 0.1 / totalAmount) * 100) : 0, color: '#A78BFA' },
    ]

    // 新客户来源
    const channelMap = new Map<string, number>()
    newCustomers.forEach((c: any) => {
      const channel = c.channel || '其他渠道'
      channelMap.set(channel, (channelMap.get(channel) || 0) + 1)
    })
    const channelList = Array.from(channelMap.entries()).sort((a, b) => b[1] - a[1]).slice(0, 5)
    const maxChannel = channelList[0]?.[1] || 1
    newCustomerSources.value = channelList.map(([channel, count]) => ({
      channel,
      count,
      process: Math.round((count / maxChannel) * 100)
    }))

    // 表格数据
    customerTableData.value = deals.slice(0, 5).map((deal: any) => {
      const customer = customers.find((c: any) => c.id === deal.customerId)
      return {
        customer: customer?.name || deal.customerName || '未知',
        type: customer?.isNew === 1 ? '新客户' : '老客户',
        amount: Number(deal.amount || 0).toLocaleString(),
        date: deal.dealDate || '未知'
      }
    })
  } catch (error) {
    console.error('Failed to load customer data:', error)
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
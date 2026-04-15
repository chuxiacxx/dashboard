<template>
  <div class="p-4 space-y-4">
    <div class="art-card p-4 flex justify-between items-center">
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
      <div v-for="item in customerStats" :key="item.label" class="art-card p-4 relative overflow-hidden">
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
      <div class="art-card p-4 lg:col-span-2">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-md font-medium">新老客户月度成交额对比（万元）</h3>
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
      <div class="art-card p-4 flex flex-col">
        <h3 class="text-md font-medium mb-4">本月新老客户成交构成</h3>

        <!-- 直观占比展示 -->
        <div class="mb-6">
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
      <div class="art-card p-4">
        <h3 class="text-md font-medium mb-4">新客户获取渠道 TOP 5</h3>
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
      <div class="art-card p-4">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-md font-medium">客户成交明细</h3>
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
import { ref } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'

const rangeDate = ref('')
const customerFilter = ref('全部')

const customerStats = ref([
  { label: '新客户成交额', value: '¥40.18万', growth: '22.4%', isUp: true, colorCls: 'text-blue-500' },
  { label: '老客户成交额', value: '¥55.50万', growth: '11.2%', isUp: true, colorCls: 'text-amber-500' },
  { label: '新客户数量', value: '42位', growth: '15.3%', isUp: true, colorCls: 'text-cyan-500' },
  { label: '活跃老客户', value: '90位', growth: '5.6%', isUp: true, colorCls: 'text-green-500' },
  { label: '新客户平均单价', value: '¥9,567', growth: '6.2%', isUp: true, colorCls: 'text-indigo-500' },
  { label: '老客户复购率', value: '72.3%', growth: '3.1%', isUp: true, colorCls: 'text-orange-500' },
  { label: '客户流失数', value: '2位', growth: '0.5%', isUp: false, colorCls: 'text-red-400' },
  { label: '客户满意度', value: '4.8分', growth: '0.2%', isUp: true, colorCls: 'text-purple-500' },
])

const months = ['1月', '2月', '3月', '4月', '5月', '6月']
const newCustomerData = [28, 32, 38, 35, 40, 40]
const oldCustomerData = [45, 48, 52, 50, 54, 56]

const customerSegments = [
  { label: '新客户首单', value: '¥40.18万', percent: 42, color: '#3B82F6' },
  { label: '老客户复购', value: '¥40.06万', percent: 41, color: '#F59E0B' },
  { label: '老客户增购', value: '¥15.44万', percent: 16, color: '#34D399' },
  { label: '老客户转介绍', value: '¥11.2万', percent: 12, color: '#A78BFA' },
]

const newCustomerSources = [
  { channel: '线上广告投放', count: 15, process: 85 },
  { channel: '老客户转介绍', count: 12, process: 68 },
  { channel: '展会/活动', count: 8, process: 45 },
  { channel: '内容营销', count: 5, process: 28 },
  { channel: '其他渠道', count: 2, process: 11 },
]

const customerTableData = [
  { customer: 'AAA公司', type: '新客户', amount: '125,000', date: '2024-06-27' },
  { customer: 'BBB公司', type: '老客户', amount: '88,000', date: '2024-06-26' },
  { customer: 'CCC公司', type: '老客户', amount: '45,200', date: '2024-06-26' },
  { customer: 'DDD公司', type: '新客户', amount: '32,000', date: '2024-06-25' },
  { customer: 'EEE公司', type: '老客户', amount: '65,400', date: '2024-06-24' },
]
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
<template>
  <div class="p-4 space-y-4">
    <div class="art-card p-4 flex justify-between items-center">
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
        <el-date-picker v-model="rangeDate" type="daterange" size="small" style="width: 240px" />
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in invoiceStats" :key="item.label" class="art-card p-4 relative overflow-hidden">
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
      <div class="art-card p-4 lg:col-span-2">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-md font-medium">月度开票金额走势（万元）</h3>
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
      <div class="art-card p-4 flex flex-col">
        <h3 class="text-md font-medium mb-4">发票类型构成</h3>
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
      <div class="art-card p-4">
        <h3 class="text-md font-medium mb-4">开票金额 TOP 5 客户</h3>
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
      <div class="art-card p-4">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-md font-medium">开票单据明细</h3>
          <el-button size="small" icon="Download">导出</el-button>
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
import { ref } from 'vue'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'

const rangeDate = ref('')
const invoiceType = ref('all')

const invoiceStats = ref([
  { label: '本月开票总金额', value: '¥71.24万', growth: '3.2%', isUp: false, colorCls: 'text-orange-500' },
  { label: '开票张数', value: '98张', growth: '5.6%', isUp: true, colorCls: 'text-blue-500' },
  { label: '增值税专票', value: '¥52.8万', growth: '2.1%', isUp: false, colorCls: 'text-orange-400' },
  { label: '普通发票', value: '¥18.44万', growth: '1.0%', isUp: true, colorCls: 'text-green-500' },
  { label: '待开票金额', value: '¥24.3万', growth: '8.4%', isUp: false, colorCls: 'text-red-400' },
  { label: '平均开票金额', value: '¥7,269', growth: '1.8%', isUp: false, colorCls: 'text-gray-500' },
  { label: '开票及时率', value: '91.2%', growth: '2.0%', isUp: true, colorCls: 'text-cyan-500' },
  { label: '本月冲红张数', value: '3张', growth: '0.5%', isUp: false, colorCls: 'text-red-400' },
])

const months = ['1月', '2月', '3月', '4月', '5月', '6月']
const invoiceAmountData = [65, 72, 80, 74, 73, 71]
const invoiceCountData = [85, 92, 106, 98, 102, 98]

const invoiceTypeData = [
  { label: '增值税专用发票', value: '¥52.8万', percent: 74, color: '#F97316' },
  { label: '增值税普通发票', value: '¥18.44万', percent: 26, color: '#FD9A3C' },
  { label: '电子发票', value: '28张', percent: 45, color: '#60A5FA' },
  { label: '纸质发票', value: '70张', percent: 55, color: '#A78BFA' },
]

const invoiceRanking = [
  { name: 'AAA公司', amount: '22.5', process: 90 },
  { name: 'BBB公司', amount: '16.8', process: 67 },
  { name: 'CCC公司', amount: '12.4', process: 50 },
  { name: 'DDD公司', amount: '9.6', process: 38 },
  { name: 'EEE公司', amount: '5.2', process: 21 },
]

const invoiceTableData = [
  { invoiceNo: 'INV-202406001', customer: 'AAA公司', amount: '85,000', type: '专票' },
  { invoiceNo: 'INV-202406002', customer: 'BBB公司', amount: '42,000', type: '普票' },
  { invoiceNo: 'INV-202406003', customer: 'CCC公司', amount: '68,000', type: '专票' },
  { invoiceNo: 'INV-202406004', customer: 'DDD公司', amount: '15,400', type: '普票' },
  { invoiceNo: 'INV-202406005', customer: 'EEE公司', amount: '32,000', type: '专票' },
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
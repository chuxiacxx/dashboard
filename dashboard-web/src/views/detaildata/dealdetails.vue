<template>
  <div class="p-4 space-y-4">
    <div class="art-card p-4 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">成交额深度分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="success" size="small" effect="plain">数据更新于：今日 10:00</el-tag>
        <el-date-picker v-model="rangeDate" type="daterange" size="small" style="width: 240px" />
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in dealStats" :key="item.label" class="art-card p-4 relative overflow-hidden">
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
      <!-- 月度成交走势 -->
      <div class="art-card p-4 lg:col-span-2">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-md font-medium">月度成交额与成交单数走势</h3>
          <div class="flex space-x-4">
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-1 bg-cyan-500 mr-1 inline-block"></span> 成交金额</div>
            <div class="flex items-center text-xs text-gray-400"><span class="w-3 h-3 bg-green-400 rounded-full mr-1 inline-block"></span> 成交单数</div>
          </div>
        </div>
        <ArtLineBarChart
          height="18rem"
          :bar-data="dealAmountData"
          :line-data="dealCountData"
          :x-axis-data="months"
          :bar-colors="['#06B6D4']"
          :line-colors="['#4ADE80']"
          :show-tooltip="true"
        />
      </div>

      <!-- 成交转化漏斗 -->
      <div class="art-card p-4 flex flex-col">
        <h3 class="text-md font-medium mb-4">本月成交阶段转化</h3>
        <div class="flex-1 space-y-4 py-2">
          <div v-for="stage in dealFunnelData" :key="stage.label">
            <div class="flex justify-between text-xs mb-1">
              <span class="text-gray-400">{{ stage.label }}</span>
              <span class="font-medium text-[var(--el-text-color-primary)]">{{ stage.value }}</span>
            </div>
            <el-progress
              :percentage="stage.percent"
              :color="stage.color"
              :stroke-width="12"
              :show-text="false"
              striped
              striped-flow
            />
          </div>
        </div>
        <div class="mt-4 pt-4 border-t border-[var(--el-border-color-lighter)] text-center">
          <p class="text-xs text-gray-500">本月综合成交转化率：<span class="text-cyan-500 font-bold">31.8%</span></p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 成交产品TOP5 -->
      <div class="art-card p-4">
        <h3 class="text-md font-medium mb-4">成交产品排行 (TOP 5)</h3>
        <div class="space-y-4">
          <div v-for="(rank, index) in productRanking" :key="rank.name" class="flex items-center">
            <span class="w-6 h-6 flex-cc rounded-full text-xs mr-3"
                  :class="index < 3 ? 'bg-cyan-100 text-cyan-600' : 'bg-gray-100 text-gray-500'">
              {{ index + 1 }}
            </span>
            <span class="text-sm flex-1">{{ rank.name }}</span>
            <div class="w-32 mr-4">
              <el-progress :percentage="rank.process" :show-text="false" :color="index === 0 ? '#06B6D4' : '#67E8F9'" />
            </div>
            <span class="text-sm font-medium w-20 text-right">¥{{ rank.amount }}万</span>
          </div>
        </div>
      </div>

      <!-- 成交明细 -->
      <div class="art-card p-4">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-md font-medium">近期成交记录</h3>
          <el-button size="small" text type="primary">查看全部</el-button>
        </div>
        <el-table :data="dealTableData" size="small" style="width: 100%">
          <el-table-column prop="customer" label="客户" show-overflow-tooltip />
          <el-table-column prop="product" label="产品" show-overflow-tooltip />
          <el-table-column prop="amount" label="成交额" width="110">
            <template #default="{ row }">
              <span class="text-cyan-500 font-medium">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="date" label="成交日期" width="100" align="right" />
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

const dealStats = ref([
  { label: '本月成交总额', value: '¥95.68万', growth: '15.7%', isUp: true, colorCls: 'text-cyan-500' },
  { label: '成交单数', value: '132单', growth: '10.2%', isUp: true, colorCls: 'text-green-500' },
  { label: '平均成交金额', value: '¥7,248', growth: '4.8%', isUp: true, colorCls: 'text-blue-500' },
  { label: '成交周期', value: '8.5天', growth: '1.2%', isUp: false, colorCls: 'text-orange-500' },
  { label: '大客户成交额', value: '¥52.4万', growth: '18.3%', isUp: true, colorCls: 'text-purple-500' },
  { label: '散客成交额', value: '¥43.28万', growth: '9.5%', isUp: true, colorCls: 'text-indigo-500' },
  { label: '成交转化率', value: '31.8%', growth: '3.4%', isUp: true, colorCls: 'text-teal-500' },
  { label: '退单率', value: '1.5%', growth: '0.3%', isUp: false, colorCls: 'text-red-400' },
])

const months = ['1月', '2月', '3月', '4月', '5月', '6月']
const dealAmountData = [68, 75, 88, 82, 90, 96]
const dealCountData = [98, 108, 124, 118, 128, 132]

const dealFunnelData = [
  { label: '线索总量', value: '415条', percent: 100, color: '#06B6D4' },
  { label: '有效商机', value: '210条', percent: 51, color: '#22D3EE' },
  { label: '报价阶段', value: '98条', percent: 24, color: '#67E8F9' },
  { label: '成交签单', value: '132单', percent: 32, color: '#4ADE80' },
]

const productRanking = [
  { name: '产品A - 旗舰款', amount: '28.5', process: 90 },
  { name: '产品B - 标准款', amount: '22.3', process: 71 },
  { name: '产品C - 定制款', amount: '18.6', process: 59 },
  { name: '产品D - 入门款', amount: '14.2', process: 45 },
  { name: '服务套餐', amount: '8.4', process: 27 },
]

const dealTableData = [
  { customer: 'AAA公司', product: '产品A旗舰款', amount: '125,000', date: '2024-06-27' },
  { customer: 'BBB公司', product: '产品B标准款', amount: '48,200', date: '2024-06-26' },
  { customer: 'CCC公司', product: '服务套餐', amount: '89,000', date: '2024-06-25' },
  { customer: 'DDD公司', product: '产品C定制款', amount: '32,000', date: '2024-06-25' },
  { customer: 'EEE公司', product: '产品D入门款', amount: '15,400', date: '2024-06-24' },
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
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
import { ref } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'

const rangeDate = ref('')

const orderStats = ref([
  { label: '本月订单总金额', value: '¥104.56万', growth: '8.3%', isUp: true, colorCls: 'text-purple-500' },
  { label: '本月订单笔数', value: '156笔', growth: '12.1%', isUp: true, colorCls: 'text-blue-500' },
  { label: '平均订单金额', value: '¥6,702', growth: '3.5%', isUp: false, colorCls: 'text-orange-500' },
  { label: '订单完成率', value: '86.5%', growth: '4.2%', isUp: true, colorCls: 'text-green-500' },
  { label: '待审核订单', value: '12笔', growth: '2.1%', isUp: false, colorCls: 'text-red-400' },
  { label: '已取消订单', value: '3笔', growth: '1.0%', isUp: false, colorCls: 'text-gray-400' },
  { label: '新客户订单占比', value: '42%', growth: '5.0%', isUp: true, colorCls: 'text-cyan-500' },
  { label: '复购率', value: '58%', growth: '2.3%', isUp: true, colorCls: 'text-indigo-500' },
])

const months = ['1月', '2月', '3月', '4月', '5月', '6月']
const orderAmountData = [78, 88, 102, 96, 115, 125]
const orderCountData = [110, 125, 148, 138, 162, 156]

const channelData = [
  { label: '直销渠道', value: '68笔', percent: 44, color: '#7C3AED' },
  { label: '线上平台', value: '42笔', percent: 27, color: '#A78BFA' },
  { label: '代理商', value: '28笔', percent: 18, color: '#60A5FA' },
  { label: '电话销售', value: '18笔', percent: 11, color: '#34D399' },
]

const amountSegments = [
  { label: '5万以上大单', count: 18, process: 85, color: '#7C3AED' },
  { label: '1万~5万', count: 56, process: 65, color: '#A78BFA' },
  { label: '5千~1万', count: 48, process: 55, color: '#60A5FA' },
  { label: '1千~5千', count: 24, process: 30, color: '#93C5FD' },
  { label: '1千以下', count: 10, process: 15, color: '#BAE6FD' },
]

const orderTableData = [
  { orderNo: 'PO-20240601', customer: 'AAA公司', amount: '125,000', status: '已完成', statusType: 'success' },
  { orderNo: 'PO-20240602', customer: 'BBB公司', amount: '48,200', status: '待审核', statusType: 'warning' },
  { orderNo: 'PO-20240603', customer: 'CCC公司', amount: '89,000', status: '已完成', statusType: 'success' },
  { orderNo: 'PO-20240604', customer: 'DDD公司', amount: '12,000', status: '进行中', statusType: 'primary' },
  { orderNo: 'PO-20240605', customer: 'EEE公司', amount: '65,400', status: '已完成', statusType: 'success' },
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
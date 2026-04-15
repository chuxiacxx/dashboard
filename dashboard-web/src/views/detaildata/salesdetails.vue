<template>
  <div class="p-4 space-y-4">
    <div class="art-card p-4 flex justify-between items-center">
      <div class="flex items-center">
        <el-button link icon="ArrowLeft" @click="$router.back()" class="mr-2">返回看板</el-button>
        <h3 class="text-lg font-medium">销售明细全景分析</h3>
      </div>
      <div class="flex items-center space-x-2">
        <el-tag type="success" size="small" effect="plain">数据更新于：今日 10:00</el-tag>
        <el-date-picker v-model="rangeDate" type="daterange" size="small" style="width: 240px" />
      </div>
    </div>

    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="item in extendedStats" :key="item.label" class="art-card p-4 relative overflow-hidden">
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
      <div class="art-card p-4 lg:col-span-2">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-md font-medium">月度业绩与目标达成走势</h3>
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

      <div class="art-card p-4 flex flex-col">
        <h3 class="text-md font-medium mb-4">销售转化转化情况</h3>
        <div class="flex-1 space-y-4 py-2">
          <div v-for="stage in funnelData" :key="stage.label">
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
          <p class="text-xs text-gray-500">综合成交转化率：<span class="text-blue-500 font-bold">24.5%</span></p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="art-card p-4">
        <h3 class="text-md font-medium mb-4">销售精英业绩榜 (TOP 5)</h3>
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

      <div class="art-card p-4">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-md font-medium">实时成交动态</h3>
          <el-button size="small" text type="primary">查看全部</el-button>
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
import { ref } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'

const rangeDate = ref('')

// 1. 扩展统计数据
const extendedStats = ref([
  { label: '本月累计销售额', value: '¥95.68万', growth: '12.5%', isUp: true, colorCls: 'text-blue-500', icon: 'ri:money-dollar-circle-line' },
  { label: '本月订单总数', value: '156单', growth: '8.2%', isUp: true, colorCls: 'text-green-500', icon: 'ri:file-list-3-line' },
  { label: '平均订购数量', value: '500个', growth: '2.4%', isUp: false, colorCls: 'text-orange-500', icon: 'ri:user-star-line' },
  { label: '本月预计回款', value: '¥24.5万', growth: '5.1%', isUp: true, colorCls: 'text-purple-500', icon: 'ri:hand-coin-line' },
  { label: '本月新增潜客', value: '42位', growth: '15.3%', isUp: true, colorCls: 'text-cyan-500', icon: 'ri:user-add-line' },
  { label: '被虐流失客户', value: '2位', growth: '0.5%', isUp: false, colorCls: 'text-red-400', icon: 'ri:user-unfollow-line' },
  { label: '销售目标达成率', value: '79.7%', growth: '3.2%', isUp: true, colorCls: 'text-indigo-500', icon: 'ri:flag-line' },
  { label: '客诉率', value: '12.4%', growth: '1.2%', isUp: true, colorCls: 'text-blue-400', icon: 'ri:pie-chart-line' },
])

// 2. 图表数据
const months = ['1月', '2月', '3月', '4月', '5月', '6月']
const salesData = [85, 92, 110, 98, 120, 135]
const ordersData = [125, 142, 168, 156, 189, 210]

// 3. 漏斗数据
const funnelData = [
  { label: '初步接洽', value: '500人', percent: 100, color: '#409EFF' },
  { label: '需求确认', value: '180人', percent: 36, color: '#79bbff' },
  { label: '方案报价', value: '86人', percent: 17, color: '#a0cfff' },
  { label: '成交签单', value: '42人', percent: 8, color: '#67C23A' },
]

// 4. 排行榜
const rankingList = [
  { name: '销售A组 - 王经理', amount: '45.2', process: 90 },
  { name: '销售B组 - 李主管', amount: '38.6', process: 77 },
  { name: '华东区 - 张销售', amount: '32.1', process: 64 },
  { name: '电商渠道', amount: '28.4', process: 56 },
  { name: '个人代理 - 陈某', amount: '12.5', process: 25 },
]

const tableData = [
  { customer: 'AAA公司', amount: '125,000', status: '完成' },
  { customer: 'BBB公司', amount: '48,200', status: '待审核' },
  { customer: 'CCC公司', amount: '89,000', status: '完成' },
  { customer: 'DDD公司', amount: '12,000', status: '进行中' },
  { customer: 'EEE公司', amount: '65,400', status: '完成' },
]
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

/* 布局辅助 */
.flex-cc {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
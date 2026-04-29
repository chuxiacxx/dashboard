<template>
  <div class="analysis-page art-full-height p-5">
    <!-- 筛选栏 -->
    <div class="art-card p-4 mb-4">
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-medium">销售员业绩分析</h3>
        <div class="flex gap-3">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            size="small"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
          <el-select v-model="regionFilter" placeholder="销售地区" clearable size="small" style="width: 140px">
            <el-option v-for="region in regionList" :key="region" :label="region" :value="region" />
          </el-select>
          <el-button type="primary" size="small" @click="loadData">
            <Search class="mr-1" />查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in salespersonStats" :key="stat.label">
        <div class="art-card p-4">
          <div class="text-sm text-gray-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold" :class="stat.color">{{ stat.value }}</div>
          <div class="text-xs text-gray-400 mt-2">{{ stat.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="mb-4">
      <!-- 业绩排行 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">销售员业绩排行 TOP10</h4>
          <ArtBarChart
            v-if="rankingData.length > 0"
            height="320px"
            :data="rankingData.map(r => r.amount)"
            :xAxisData="rankingData.map(r => r.name.substring(0, 6) + (r.name.length > 6 ? '...' : ''))"
            :showAxisLine="true"
            :showSplitLine="false"
            :colors="['#409EFF']"
          />
          <ElEmpty v-else description="暂无数据" />
        </div>
      </el-col>

      <!-- 目标达成率 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">目标达成率</h4>
          <div v-if="completionData.length > 0" class="space-y-4">
            <div v-for="item in completionData" :key="item.name" class="flex items-center">
              <div class="w-24 text-sm text-gray-600">{{ item.name }}</div>
              <div class="flex-1">
                <el-progress :percentage="item.rate" :color="getCompletionColor(item.rate)" />
              </div>
              <div class="w-20 text-right text-sm font-medium">{{ item.rate }}%</div>
            </div>
          </div>
          <ElEmpty v-else description="暂无数据" />
        </div>
      </el-col>
    </el-row>

    <!-- 业绩趋势和区域分布 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">月度业绩趋势</h4>
          <ArtLineChart
            v-if="trendData.length > 0"
            height="320px"
            :data="trendData.map(t => Math.round(t.amount / 10000))"
            :xAxisData="trendData.map(t => t.month.substring(5) + '月')"
            :showAreaColor="true"
            :showAxisLine="false"
          />
          <ElEmpty v-else description="暂无数据" />
        </div>
      </el-col>

      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">区域业绩分布</h4>
          <ArtRingChart
            v-if="regionData.length > 0"
            height="320px"
            :data="regionData.map(r => ({ name: r.region, value: r.amount }))"
            :showLegend="true"
            :showTooltip="true"
          />
          <ElEmpty v-else description="暂无数据" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ArtBarChart from '@/components/core/charts/art-bar-chart/index.vue'
import ArtLineChart from '@/components/core/charts/art-line-chart/index.vue'
import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'
import { fetchSalespersonRanking, fetchRegionStats, fetchSalesList } from '@/api/dashboard'

defineOptions({ name: 'SalespersonAnalysis' })

const dateRange = ref<string[]>([])
const regionFilter = ref('')
const regionList = ref<string[]>([])
const loading = ref(false)

const salespersonStats = ref([
  { label: '销售员总数', value: '0', desc: '在职人员', color: 'text-blue-500' },
  { label: '总业绩', value: '¥0', desc: '累计销售额', color: 'text-green-500' },
  { label: '平均业绩', value: '¥0', desc: '人均销售额', color: 'text-orange-500' },
  { label: '平均达成率', value: '0%', desc: '完成目标占比', color: 'text-purple-500' },
  { label: '销售冠军', value: '-', desc: '本月冠军', color: 'text-red-500' }
])

const rankingData = ref<{ name: string; amount: number; target: number }[]>([])
const completionData = ref<{ name: string; rate: number }[]>([])
const trendData = ref<{ month: string; amount: number }[]>([])
const regionData = ref<{ region: string; amount: number }[]>([])

const formatAmount = (amount: number) => {
  if (amount >= 10000) return (amount / 10000).toFixed(2) + '万'
  return amount.toLocaleString()
}

const getCompletionColor = (rate: number) => {
  if (rate >= 100) return '#67C23A'
  if (rate >= 80) return '#E6A23C'
  return '#F56C6C'
}

const handleDateChange = () => {
  loadData()
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 获取销售员排行数据
    const now = new Date()
    const year = dateRange.value?.[0] ? new Date(dateRange.value[0]).getFullYear() : now.getFullYear()

    // 将daterange转换为日期字符串
    let dateRangeParams: any = { year }
    if (dateRange.value?.length === 2) {
      const startDate = dateRange.value[0]
      const endDate = dateRange.value[1]
      if (startDate && endDate) {
        dateRangeParams = { startDate, endDate }
      }
    }

    const res: any = await fetchSalespersonRanking(dateRangeParams)

    if (res?.code === 200 && res.data) {
      const list = res.data

      // 统计卡片数据
      const totalSales = list.reduce((sum: number, item: any) => sum + (item.actualAmount || 0), 0)
      const avgCompletion = list.length > 0
        ? list.reduce((sum: number, item: any) => sum + (item.completionRate || 0), 0) / list.length
        : 0
      const topSalesperson = list.length > 0 ? (list[0].name || '未分配') : '-'
      const topAmount = list.length > 0 ? list[0].actualAmount : 0

      salespersonStats.value = [
        { label: '销售员总数', value: list.length.toString(), desc: '在职人员', color: 'text-blue-500' },
        { label: '总业绩', value: '¥' + formatAmount(totalSales), desc: '累计销售额', color: 'text-green-500' },
        { label: '平均业绩', value: '¥' + formatAmount(list.length > 0 ? totalSales / list.length : 0), desc: '人均销售额', color: 'text-orange-500' },
        { label: '平均达成率', value: avgCompletion.toFixed(1) + '%', desc: '完成目标占比', color: 'text-purple-500' },
        { label: '销售冠军', value: topSalesperson, desc: '¥' + formatAmount(topAmount), color: 'text-red-500' }
      ]

      // 业绩排行 TOP10
      rankingData.value = list.slice(0, 10).map((item: any) => ({
        name: item.name || '未分配',
        amount: item.actualAmount || 0
      }))

      // 目标达成率
      completionData.value = list.slice(0, 8).map((item: any) => ({
        name: item.name || '未分配',
        rate: Math.min(item.completionRate || 0, 100)
      }))
    }

    // 加载月度趋势数据
    const salesRes: any = await fetchSalesList(dateRangeParams)
    if (salesRes?.code === 200 && salesRes.data) {
      trendData.value = salesRes.data.map((item: any) => ({
        month: item.month,
        amount: item.salesAmount || 0
      }))
    }

    // 加载地区分布数据
    const regionRes: any = await fetchRegionStats(dateRangeParams)
    if (regionRes?.code === 200 && regionRes.data) {
      regionData.value = regionRes.data
        .filter((item: any) => item.region && item.region !== '未分类')
        .map((item: any) => ({
          region: item.region,
          amount: item.salesAmount || 0
        }))
    }
  } catch (error) {
    console.error('Failed to load salesperson data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.analysis-page {
  background: transparent;
}
</style>

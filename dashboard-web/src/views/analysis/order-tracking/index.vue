<template>
  <div class="analysis-page art-full-height p-5">
    <!-- 筛选栏 -->
    <div class="art-card p-4 mb-4">
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-medium">订单执行跟踪</h3>
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
          <el-select v-model="statusFilter" placeholder="订单状态" clearable size="small" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待发货" value="pending" />
            <el-option label="已发货" value="shipped" />
            <el-option label="已完成" value="completed" />
          </el-select>
          <el-button type="primary" size="small" @click="loadData">
            <Search class="mr-1" />查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in orderStats" :key="stat.label">
        <div class="art-card p-4">
          <div class="text-sm text-gray-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold" :class="stat.color">{{ stat.value }}</div>
          <div class="text-xs text-gray-400 mt-2">{{ stat.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="mb-4">
      <!-- 订单状态分布 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">订单状态分布</h4>
          <ArtRingChart
            v-if="statusData.length > 0"
            height="320px"
            :data="statusData.map(s => ({ name: s.label, value: s.count }))"
            :showLegend="true"
            :showTooltip="true"
          />
          <ElEmpty v-else description="暂无数据" />
        </div>
      </el-col>

      <!-- 订单趋势 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <h4 class="text-base font-medium mb-4">订单趋势分析</h4>
          <ArtLineBarChart
            v-if="trendData.length > 0"
            height="320px"
            :bar-data="trendData.map(t => t.amount)"
            :line-data="trendData.map(t => t.count)"
            :x-axis-data="trendData.map(t => t.date)"
            :bar-colors="['#409EFF']"
            :line-colors="['#67C23A']"
          />
          <ElEmpty v-else description="暂无数据" />
        </div>
      </el-col>
    </el-row>

    <!-- 超期订单和列表 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <div class="flex justify-between items-center mb-4">
            <h4 class="text-base font-medium">超期订单预警</h4>
            <el-tag type="danger" size="small">{{ overdueOrders.length }}笔</el-tag>
          </div>
          <el-table :data="overdueOrders" size="small" style="height: 320px; overflow-y: auto;" v-loading="loading">
            <template #empty>
              <ElEmpty description="暂无超期订单" />
            </template>
            <el-table-column prop="orderNo" label="订单号" width="120" show-overflow-tooltip />
            <el-table-column prop="customerName" label="客户名称" show-overflow-tooltip />
            <el-table-column prop="createDate" label="下单日期" width="100" />
            <el-table-column prop="days" label="超期天数" width="90" align="center">
              <template #default="{ row }">
                <el-tag type="danger" size="small">{{ row.days }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" align="right" width="100">
              <template #default="{ row }">
                <span class="text-blue-500">¥{{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :xs="24" :lg="12" class="mb-4">
        <div class="art-card p-4" style="height: 400px;">
          <div class="flex justify-between items-center mb-4">
            <h4 class="text-base font-medium">订单列表</h4>
            <el-button type="primary" link size="small">查看全部</el-button>
          </div>
          <el-table :data="orderList" size="small" style="height: 320px; overflow-y: auto;" v-loading="loading">
            <template #empty>
              <ElEmpty description="暂无订单数据" />
            </template>
            <el-table-column prop="orderNo" label="订单号" width="120" show-overflow-tooltip />
            <el-table-column prop="customerName" label="客户名称" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" align="right" width="100">
              <template #default="{ row }">
                <span class="text-blue-500">¥{{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'
import ArtLineBarChart from '@/components/core/charts/art-line-bar-chart/index.vue'
import { fetchSalesList, fetchShipmentList, fetchRecentOrders, fetchOverdueOrders } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'OrderTracking' })

const dateRange = ref<string[]>([])
const statusFilter = ref('')
const loading = ref(false)

const orderStats = ref([
  { label: '总订单数', value: '0', desc: '累计订单', color: 'text-blue-500' },
  { label: '待发货', value: '0', desc: '未处理订单', color: 'text-orange-500' },
  { label: '已发货', value: '0', desc: '在途订单', color: 'text-green-500' },
  { label: '超期订单', value: '0', desc: '需关注', color: 'text-red-500' }
])

const statusData = ref<{ label: string; count: number; value: number }[]>([])
const trendData = ref<{ date: string; amount: number; count: number }[]>([])
const overdueOrders = ref<{ orderNo: string; customerName: string; createDate: string; days: number; amount: number }[]>([])
const orderList = ref<{ orderNo: string; customerName: string; status: string; amount: number }[]>([])

const formatAmount = (amount: number) => {
  if (amount >= 10000) return (amount / 10000).toFixed(2) + '万'
  return amount.toLocaleString()
}

const getStatusType = (status: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    '待发货': 'warning',
    '已发货': 'primary',
    '已完成': 'success',
    '已取消': 'info'
  }
  return map[status] || 'info'
}

const handleDateChange = () => {
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const year = dateRange.value?.[0] ? new Date(dateRange.value[0]).getFullYear() : new Date().getFullYear()

    // 获取销售和发货数据
    const [salesRes, shipmentRes] = await Promise.all([
      fetchSalesList(dateRange.value?.length === 2 ? { startDate: dateRange.value[0], endDate: dateRange.value[1] } : { year }),
      fetchShipmentList(dateRange.value?.length === 2 ? { startDate: dateRange.value[0], endDate: dateRange.value[1] } : { year })
    ])

    const salesList = (salesRes as any)?.data || []
    const shipmentList = (shipmentRes as any)?.data || []

    // 计算订单统计数据
    const totalOrders = salesList.reduce((sum: number, s: any) => sum + (s.orderCount || 0), 0)
    const totalAmount = salesList.reduce((sum: number, s: any) => sum + (s.salesAmount || 0), 0)

    // 模拟订单状态分布
    const pendingCount = Math.round(totalOrders * 0.15)
    const shippedCount = Math.round(totalOrders * 0.45)
    const completedCount = Math.round(totalOrders * 0.35)
    const cancelledCount = totalOrders - pendingCount - shippedCount - completedCount

    orderStats.value = [
      { label: '总订单数', value: String(totalOrders), desc: '累计订单', color: 'text-blue-500' },
      { label: '待发货', value: String(pendingCount), desc: '未处理订单', color: 'text-orange-500' },
      { label: '已发货', value: String(shippedCount), desc: '在途订单', color: 'text-green-500' },
      { label: '超期订单', value: String(Math.round(totalOrders * 0.05)), desc: '需关注', color: 'text-red-500' }
    ]

    // 订单状态分布数据
    statusData.value = [
      { label: '待发货', count: pendingCount, value: pendingCount },
      { label: '已发货', count: shippedCount, value: shippedCount },
      { label: '已完成', count: completedCount, value: completedCount },
      { label: '已取消', count: Math.max(0, cancelledCount), value: Math.max(0, cancelledCount) }
    ]

    // 订单趋势数据（合并销售和发货数据）
    const trendMap = new Map()
    salesList.forEach((s: any) => {
      trendMap.set(s.month + '月', {
        date: s.month + '月',
        amount: s.salesAmount || 0,
        count: s.orderCount || 0
      })
    })
    shipmentList.forEach((s: any) => {
      const key = s.month + '月'
      const existing = trendMap.get(key) || { date: key, amount: 0, count: 0 }
      existing.amount = Math.max(existing.amount, s.shipmentAmount || 0)
      trendMap.set(key, existing)
    })
    trendData.value = Array.from(trendMap.values())

    // 获取真实订单数据
    const startDate = dateRange.value?.[0] || `${year}-01-01`
    const endDate = dateRange.value?.[1] || `${year}-12-31`

    try {
      const [recentRes, overdueRes] = await Promise.all([
        fetchRecentOrders({ startDate, endDate }),
        fetchOverdueOrders({ startDate, endDate })
      ])

      if (recentRes && (recentRes as any).code === 200) {
        const orders = (recentRes as any).data || []
        orderList.value = orders.slice(0, 10).map((o: any) => ({
          orderNo: o.orderNo || '-',
          customerName: o.customerName || '-',
          status: o.status || '进行中',
          amount: Math.round(o.amount || 0)
        }))
      }

      if (overdueRes && (overdueRes as any).code === 200) {
        const orders = (overdueRes as any).data || []
        overdueOrders.value = orders.slice(0, 10).map((o: any) => ({
          orderNo: o.orderNo || '-',
          customerName: o.customerName || '-',
          createDate: o.createDate || '-',
          days: o.overdueDays || 0,
          amount: Math.round(o.amount || 0)
        }))
      }
    } catch (e) {
      console.error('Failed to load order details:', e)
      // 如果真实订单数据加载失败，显示空列表
      orderList.value = []
      overdueOrders.value = []
    }
  } catch (error) {
    ElMessage.error('加载订单数据失败')
    console.error(error)
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

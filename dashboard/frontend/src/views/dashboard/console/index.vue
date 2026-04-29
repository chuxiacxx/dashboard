<template>
  <div class="dashboard-container p-5">
    <!-- 顶部数据卡片区 -->
    <div class="card-section mb-5">
      <CardList />
    </div>

    <!-- 图表区 -->
    <div class="chart-section">
      <!-- 第一行：两个图表并排 -->
      <el-row :gutter="20" class="chart-row mb-5">
        <el-col :xs="24" :md="12" class="chart-col">
          <HotProducts />
        </el-col>
        <el-col :xs="24" :md="12" class="chart-col">
          <MonthlyShipment />
        </el-col>
      </el-row>

      <!-- 第二行：两个图表并排 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :md="12" class="chart-col">
           <PaymentStatus />
        </el-col>
        <el-col :xs="24" :md="12" class="chart-col">
           <SampleData />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
  import CardList from './modules/card-list.vue'
  import HotProducts from './modules/hot-products.vue'
  import MonthlyShipment from './modules/monthly-shipment.vue'
  import SampleData from './modules/sample-data.vue'
  import PaymentStatus from './modules/payment-status.vue'

  defineOptions({ name: 'Console' })
</script>
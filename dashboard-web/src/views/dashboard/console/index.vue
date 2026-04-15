<template>
  <div class="dashboard-container">
    <!-- 顶部数据卡片区 -->
    <div class="card-section">
      <CardList />
    </div>

    <!-- 图表区 -->
    <div class="chart-section">
      <!-- 第一行：两个图表并排 -->
      <el-row :gutter="20" class="chart-row mb-15">  <!-- 添加 mb-6 (margin-bottom: 1.5rem) -->
        <el-col :xs="24" :md="12" class="chart-col">
          <HotProducts />
        </el-col>
        <el-col :xs="24" :md="12" class="chart-col">
          <MonthlyShipment />
        </el-col>
      </el-row>

      <!-- 第二行：两个图表并排 -->
      <el-row :gutter="20" class="chart-row">  <!-- 第二行不需要底部间距 -->
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
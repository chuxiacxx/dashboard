<template>
  <ElRow :gutter="20" class="flex">
    <!-- 1. 目标销售额与实际销售额 -->
    <ElCol :sm="12" :md="8" :lg="4">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('SalesDetails')">
        <div class="flex justify-between items-center mb-4">
          <div class="flex items-center">
            <span class="text-g-700 text-sm">销售额</span>
            <ElTag size="small" type="primary" class="ml-2">本月</ElTag>
          </div>
          <div class="size-12.5 rounded-xl flex-cc bg-blue-100">
            <ArtSvgIcon icon="ri:money-dollar-circle-line" class="text-xl text-blue-500" />
          </div>
        </div>
        
        <div class="flex justify-between items-center mb-4">
          <div class="text-center flex-1">
            <div class="text-xs text-g-600 mb-1">目标</div>
            <div class="text-lg font-semibold">¥120万</div>
          </div>
          <div class="text-center flex-1">
            <div class="text-xs text-g-600 mb-1">实际</div>
            <div class="text-lg font-semibold text-success">¥95.68万</div>
          </div>
        </div>
        
        <div class="mt-auto">
          <div class="flex justify-between text-xs text-g-600 mb-1">
            <span>完成率</span>
            <span class="text-primary font-semibold">79.73%</span>
          </div>
          <ElProgress :percentage="79.73" :show-text="false" :stroke-width="6" />
        </div>
      </div>
    </ElCol>

    <!-- 2. 本月出货金额 -->
    <ElCol :sm="12" :md="8" :lg="4">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('ShipmentDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">出货金额</span>
          <div class="size-12.5 rounded-xl flex-cc bg-green-100">
            <ArtSvgIcon icon="ri:ship-line" class="text-xl text-green-500" />
          </div>
        </div>
        
        <div class="flex items-end mb-3">
          <ArtCountTo class="text-[26px] font-medium mr-2" :target="873200" :duration="1300" prefix="¥" />
          <span class="text-xs text-g-600 mb-1">(本月)</span>
        </div>
        
        <div class="flex-c mt-auto">
          <span class="text-xs text-g-600">较上周</span>
          <span class="ml-1 text-xs font-semibold text-success">
            <ArtSvgIcon icon="ri:arrow-up-line" class="mr-1 inline" />+12.5%
          </span>
        </div>
      </div>
    </ElCol>
    
    <!-- 3. 总新增订单金额 -->
    <ElCol :sm="12" :md="8" :lg="4">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('OrderDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">新增订单</span>
          <div class="size-12.5 rounded-xl flex-cc bg-purple-100">
            <ArtSvgIcon icon="ri:file-list-3-line" class="text-xl text-purple-500" />
          </div>
        </div>
        
        <div class="mb-3">
          <div class="flex items-end mb-2">
            <ArtCountTo class="text-[26px] font-medium mr-2" :target="1045600" :duration="1300" prefix="¥" />
            <span class="text-xs text-g-600 mb-1">(总金额)</span>
          </div>
          <div class="text-sm text-g-700">
            订单数量：<span class="font-semibold text-purple-500">156</span> 笔
          </div>
        </div>
        
        <div class="flex-c mt-auto">
          <span class="text-xs text-g-600">较上周</span>
          <span class="ml-1 text-xs font-semibold text-success">
            <ArtSvgIcon icon="ri:arrow-up-line" class="mr-1 inline" />+8.3%
          </span>
        </div>
      </div>
    </ElCol>

    <!-- 4. 开票金额 -->
    <ElCol :sm="12" :md="8" :lg="4">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('InvoiceDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">开票金额</span>
          <div class="size-12.5 rounded-xl flex-cc bg-orange-100">
            <ArtSvgIcon icon="ri:receipt-line" class="text-xl text-orange-500" />
          </div>
        </div>
        
        <div class="flex items-end mb-3">
          <ArtCountTo class="text-[26px] font-medium mr-2" :target="712400" :duration="1300" prefix="¥" />
          <span class="text-xs text-g-600 mb-1">(本月)</span>
        </div>
        
        <div class="flex-c mt-auto">
          <span class="text-xs text-g-600">较上周</span>
          <span class="ml-1 text-xs font-semibold text-danger">
            <ArtSvgIcon icon="ri:arrow-down-line" class="mr-1 inline" />-3.2%
          </span>
        </div>
      </div>
    </ElCol>

    <!-- 5. 本月成交额 -->
    <ElCol :sm="12" :md="8" :lg="4">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('DealDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">本月成交额</span>
          <div class="size-12.5 rounded-xl flex-cc bg-cyan-100">
            <ArtSvgIcon icon="ri:exchange-dollar-line" class="text-xl text-cyan-500" />
          </div>
        </div>
        
        <div class="flex items-end mb-3">
          <ArtCountTo class="text-[26px] font-medium" :target="956800" :duration="1300" prefix="¥" />
        </div>
        
        <div class="flex-c mt-auto">
          <span class="text-xs text-g-600">较上周</span>
          <span class="ml-1 text-xs font-semibold text-success">
            <ArtSvgIcon icon="ri:arrow-up-line" class="mr-1 inline" />+15.7%
          </span>
        </div>
      </div>
    </ElCol>

    <!-- 6. 新老客户成交占比 -->
    <ElCol :sm="12" :md="8" :lg="4">
      <div class="art-card relative flex flex-col h-60 px-5 py-6 mb-5 max-sm:mb-4" @click="handleCardClick('CustomerDetails')">
        <div class="flex justify-between items-center mb-4">
          <span class="text-g-700 text-sm">新老客户成交占比</span>
          <div class="size-12.5 rounded-xl flex-cc bg-indigo-100">
            <ArtSvgIcon icon="ri:user-heart-line" class="text-xl text-indigo-500" />
          </div>
        </div>
        
        <div class="flex justify-between mb-4">
          <div class="text-center flex-1">
            <div class="text-xs text-g-600 mb-1">新客户</div>
            <div class="text-lg font-semibold text-blue-500">42%</div>
          </div>
          <div class="text-center flex-1">
            <div class="text-xs text-g-600 mb-1">老客户</div>
            <div class="text-lg font-semibold text-amber-500">58%</div>
          </div>
        </div>
        
        <div class="mt-auto">
          <div class="h-2 w-full bg-g-200 rounded-full overflow-hidden flex">
            <div class="h-full bg-blue-400" style="width: 42%"></div>
            <div class="h-full bg-amber-400" style="width: 58%"></div>
          </div>
        </div>
      </div>
    </ElCol>
  </ElRow>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * 此时的 type 直接对应路由配置中的 name
 */
const handleCardClick = (routeName: string) => {
  router.push({
    name: routeName, // 直接匹配 router/modules/detail.ts 里的 name
    // 如果需要区分是哪个卡片点进去的，依然可以带参数
    query: { from: routeName.toLowerCase() } 
  })
}
</script>
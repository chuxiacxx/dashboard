<template>
  <div class="sales-target-page art-full-height p-5">
    <ElCard class="art-table-card" shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-medium">销售目标配置</h3>
          <ElSpace>
            <ElButton type="primary" @click="showDialog('add')">
              <Plus class="mr-1" />新增目标
            </ElButton>
            <ElButton @click="showBatchDialog">
              <Upload class="mr-1" />批量设置
            </ElButton>
          </ElSpace>
        </div>
      </template>

      <!-- 筛选 -->
      <div class="mb-4 flex gap-3">
        <ElSelect v-model="filterYear" placeholder="选择年份" clearable style="width: 120px">
          <ElOption v-for="year in availableYears" :key="year" :label="year + '年'" :value="year" />
        </ElSelect>
        <ElSelect v-model="filterMonth" placeholder="选择月份" clearable style="width: 120px">
          <ElOption v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </ElSelect>
        <ElButton @click="loadData">查询</ElButton>
        <ElButton @click="resetFilter">重置</ElButton>
      </div>

      <!-- 数据表格 -->
      <ElTable :data="targetList" v-loading="loading" border stripe>
        <ElTableColumn prop="year" label="年份" width="80" align="center" />
        <ElTableColumn prop="month" label="月份" width="70" align="center">
          <template #default="{ row }">{{ row.month }}月</template>
        </ElTableColumn>
        <ElTableColumn prop="salesperson" label="销售员" width="100">
          <template #default="{ row }">{{ row.salesperson || '全部' }}</template>
        </ElTableColumn>
        <ElTableColumn prop="region" label="地区" width="120">
          <template #default="{ row }">{{ row.region || '全部' }}</template>
        </ElTableColumn>
        <ElTableColumn prop="targetAmount" label="销售目标" align="right">
          <template #default="{ row }">
            <span class="font-medium text-blue-500">¥{{ formatAmount(row.targetAmount) }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="collectionTargetAmount" label="回款目标" align="right">
          <template #default="{ row }">
            <span>{{ row.collectionTargetAmount ? '¥' + formatAmount(row.collectionTargetAmount) : '-' }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="orderCountTarget" label="订单数目标" align="center" width="110">
          <template #default="{ row }">{{ row.orderCountTarget || '-' }}</template>
        </ElTableColumn>
        <ElTableColumn prop="remark" label="备注" show-overflow-tooltip />
        <ElTableColumn label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link @click="showDialog('edit', row)">编辑</ElButton>
            <ElButton type="danger" link @click="handleDelete(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <!-- 新增/编辑弹窗 -->
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="100px">
        <ElFormItem label="年份" prop="year">
          <ElSelect v-model="form.year" style="width: 100%">
            <ElOption v-for="year in availableYears" :key="year" :label="year + '年'" :value="year" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="月份" prop="month">
          <ElSelect v-model="form.month" style="width: 100%">
            <ElOption v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="销售员">
          <ElSelect v-model="form.salesperson" clearable placeholder="不选表示全部" style="width: 100%">
            <ElOption v-for="name in salespersonList" :key="name" :label="name" :value="name" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="地区">
          <ElSelect v-model="form.region" clearable placeholder="不选表示全部" style="width: 100%">
            <ElOption v-for="region in regionList" :key="region" :label="region" :value="region" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="销售目标" prop="targetAmount">
          <ElInputNumber v-model="form.targetAmount" :min="0" :precision="2" style="width: 100%">
            <template #append>元</template>
          </ElInputNumber>
        </ElFormItem>
        <ElFormItem label="回款目标">
          <ElInputNumber v-model="form.collectionTargetAmount" :min="0" :precision="2" style="width: 100%">
            <template #append>元</template>
          </ElInputNumber>
        </ElFormItem>
        <ElFormItem label="订单数目标">
          <ElInputNumber v-model="form.orderCountTarget" :min="0" :precision="0" style="width: 100%">
            <template #append>笔</template>
          </ElInputNumber>
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="form.remark" type="textarea" :rows="2" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>

    <!-- 批量设置弹窗 -->
    <ElDialog v-model="batchDialogVisible" title="批量设置销售目标" width="700px">
      <div class="mb-4">
        <ElAlert type="info" :closable="false">
          为选定的销售员批量设置多个月份的目标金额
        </ElAlert>
      </div>
      <ElForm :model="batchForm" ref="batchFormRef" label-width="100px">
        <ElFormItem label="年份" required>
          <ElSelect v-model="batchForm.year" style="width: 100%">
            <ElOption v-for="year in availableYears" :key="year" :label="year + '年'" :value="year" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="销售员">
          <ElSelect v-model="batchForm.salesperson" clearable placeholder="不选表示全部" style="width: 100%">
            <ElOption v-for="name in salespersonList" :key="name" :label="name" :value="name" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="目标设置">
          <ElTable :data="batchForm.months" border size="small">
            <ElTableColumn prop="month" label="月份" width="80" align="center">
              <template #default="{ row }">{{ row.month }}月</template>
            </ElTableColumn>
            <ElTableColumn label="销售目标" align="center">
              <template #default="{ row }">
                <ElInputNumber v-model="row.targetAmount" :min="0" :precision="2" size="small" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="回款目标" align="center">
              <template #default="{ row }">
                <ElInputNumber v-model="row.collectionTargetAmount" :min="0" :precision="2" size="small" />
              </template>
            </ElTableColumn>
          </ElTable>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="batchDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleBatchSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import {
  fetchSalesTargets,
  createSalesTarget,
  updateSalesTarget,
  deleteSalesTarget,
  batchCreateSalesTargets,
  fetchSalespersonList,
  fetchRegionList,
  fetchAvailableYears
} from '@/api/dashboard'

defineOptions({ name: 'SalesTarget' })

interface TargetItem {
  id?: number
  year: number
  month: number
  salesperson?: string
  region?: string
  targetAmount: number
  collectionTargetAmount?: number
  orderCountTarget?: number
  remark?: string
}

const loading = ref(false)
const targetList = ref<TargetItem[]>([])
const availableYears = ref<number[]>([])
const salespersonList = ref<string[]>([])
const regionList = ref<string[]>([])

// 筛选
const filterYear = ref<number | undefined>(undefined)
const filterMonth = ref<number | undefined>(undefined)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增目标')
const dialogType = ref<'add' | 'edit'>('add')
const currentId = ref<number | undefined>(undefined)

const formRef = ref()
const form = ref<Partial<TargetItem> & { orderCountTarget?: number | string }>({
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1,
  targetAmount: 0,
  orderCountTarget: 0
})

const rules = {
  year: [{ required: true, message: '请选择年份', trigger: 'change' }],
  month: [{ required: true, message: '请选择月份', trigger: 'change' }],
  targetAmount: [{ required: true, message: '请输入销售目标', trigger: 'blur' }]
}

// 批量弹窗
const batchDialogVisible = ref(false)
const batchFormRef = ref()
const batchForm = ref({
  year: new Date().getFullYear(),
  salesperson: undefined as string | undefined,
  months: Array.from({ length: 12 }, (_, i) => ({
    month: i + 1,
    targetAmount: 0,
    collectionTargetAmount: 0
  }))
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res: any = await fetchSalesTargets({
      year: filterYear.value,
      month: filterMonth.value
    })
    if (res && res.code === 200 && res.data) {
      targetList.value = res.data
    }
  } catch (error) {
    console.error('Failed to load targets:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载辅助数据
const loadAuxData = async () => {
  try {
    const [yearsRes, personsRes, regionsRes] = await Promise.all([
      fetchAvailableYears(),
      fetchSalespersonList(),
      fetchRegionList()
    ])

    if (yearsRes && (yearsRes as any).code === 200) {
      availableYears.value = (yearsRes as any).data || []
    }
    if (personsRes && (personsRes as any).code === 200) {
      salespersonList.value = (personsRes as any).data || []
    }
    if (regionsRes && (regionsRes as any).code === 200) {
      regionList.value = (regionsRes as any).data || []
    }
  } catch (error) {
    console.error('Failed to load aux data:', error)
  }
}

// 显示弹窗
const showDialog = (type: 'add' | 'edit', row?: TargetItem) => {
  dialogType.value = type
  dialogTitle.value = type === 'add' ? '新增目标' : '编辑目标'

  if (type === 'edit' && row) {
    currentId.value = row.id
    form.value = { ...row }
  } else {
    currentId.value = undefined
    form.value = {
      year: new Date().getFullYear(),
      month: new Date().getMonth() + 1,
      targetAmount: 0
    }
  }

  dialogVisible.value = true
}

// 显示批量弹窗
const showBatchDialog = () => {
  batchForm.value = {
    year: new Date().getFullYear(),
    salesperson: undefined,
    months: Array.from({ length: 12 }, (_, i) => ({
      month: i + 1,
      targetAmount: 0,
      collectionTargetAmount: 0
    }))
  }
  batchDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (dialogType.value === 'add') {
      await createSalesTarget({
        ...form.value,
        orderCountTarget: Number(form.value.orderCountTarget) || 0
      } as TargetItem)
      ElMessage.success('创建成功')
    } else {
      await updateSalesTarget(currentId.value!, {
        targetAmount: form.value.targetAmount!,
        collectionTargetAmount: form.value.collectionTargetAmount,
        orderCountTarget: Number(form.value.orderCountTarget) || 0,
        remark: form.value.remark
      })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  }
}

// 批量提交
const handleBatchSubmit = async () => {
  const targets = batchForm.value.months
    .filter(m => m.targetAmount > 0)
    .map(m => ({
      year: batchForm.value.year,
      month: m.month,
      salesperson: batchForm.value.salesperson,
      targetAmount: m.targetAmount,
      collectionTargetAmount: m.collectionTargetAmount
    }))

  if (targets.length === 0) {
    ElMessage.warning('请至少设置一个月的目标')
    return
  }

  try {
    await batchCreateSalesTargets(targets)
    ElMessage.success('批量设置成功')
    batchDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error?.message || '批量设置失败')
  }
}

// 删除
const handleDelete = async (row: TargetItem) => {
  try {
    await ElMessageBox.confirm('确定要删除该目标配置吗？', '确认删除', {
      type: 'warning'
    })
    await deleteSalesTarget(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

// 重置筛选
const resetFilter = () => {
  filterYear.value = undefined
  filterMonth.value = undefined
  loadData()
}

// 格式化金额
const formatAmount = (amount: number) => {
  if (!amount) return '0'
  if (amount >= 10000) {
    return (amount / 10000).toFixed(2) + '万'
  }
  return amount.toFixed(0)
}

onMounted(() => {
  loadAuxData()
  loadData()
})
</script>

<style scoped>
.sales-target-page {
  background: transparent;
}
</style>

<!-- 数据导入页面 -->
<template>
  <div class="data-import-page art-full-height p-5">
    <!-- 文件上传 -->
    <ElCard class="art-table-card mb-5" shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-medium">销售订单数据导入</h3>
        </div>
      </template>

      <div>
        <!-- 说明 -->
        <div class="mb-4">
          <ElAlert
            type="info"
            :closable="false"
            show-icon
          >
            <template #title>
              <span class="font-medium">支持导入 Excel 文件（.xlsx / .xls）</span>
            </template>
            <div class="text-sm mt-2 space-y-1">
              <p>1. 上传包含 Sheet1 + Sheet2 的销售订单 Excel 文件</p>
              <p>2. 系统会自动解析所有 Sheet 中的数据</p>
              <p>3. 基于 订单编号 + 行号 自动去重，重复数据会更新而非追加</p>
              <p>4. 支持 Excel 数字日期格式自动转换</p>
            </div>
          </ElAlert>
        </div>

        <!-- 文件上传区域 -->
        <div class="mb-4">
          <ElUpload
            ref="uploadRef"
            class="upload-area"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .xlsx, .xls 格式，文件大小不超过 10MB
              </div>
            </template>
          </ElUpload>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-3 mb-4">
          <ElButton
            type="primary"
            :loading="uploading"
            :disabled="!selectedFile"
            @click="handleImport"
          >
            <Upload class="mr-1" />
            开始导入
          </ElButton>
          <ElButton @click="handleReset">
            <Refresh class="mr-1" />
            重置
          </ElButton>
        </div>

        <!-- 导入结果 -->
        <div v-if="importResult" class="mb-4">
          <ElAlert
            :type="importResult.code === 200 ? 'success' : 'error'"
            :title="importResult.code === 200 ? '导入完成' : '导入失败'"
            :description="importResult.message"
            show-icon
          />
          <div v-if="importResult.data" class="mt-3 import-result-box">
            <div class="flex gap-6 text-sm flex-wrap">
              <span class="text-gray-800 dark:text-gray-200">总记录: <strong>{{ importResult.data.total }}</strong></span>
              <span class="text-green-600">成功: <strong>{{ importResult.data.success }}</strong></span>
              <span class="text-blue-600">新建: <strong>{{ importResult.data.created || 0 }}</strong></span>
              <span class="text-orange-600">更新: <strong>{{ importResult.data.updated || 0 }}</strong></span>
              <span class="text-red-600">失败: <strong>{{ importResult.data.failed }}</strong></span>
            </div>
            <div v-if="importResult.data.errors && importResult.data.errors.length > 0" class="mt-3">
              <p class="text-sm font-medium text-red-600 mb-1">错误详情:</p>
              <ul class="text-sm text-red-500 space-y-1 max-h-40 overflow-y-auto">
                <li v-for="(err, idx) in importResult.data.errors" :key="idx">{{ err }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </ElCard>

    <!-- 导入记录 -->
    <ElCard class="art-table-card mt-4" shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-medium">导入记录</h3>
          <ElButton type="primary" link size="small" @click="loadImportHistory">
            <Refresh class="mr-1" />刷新
          </ElButton>
        </div>
      </template>

      <ElTable v-if="importHistory.length > 0" :data="importHistory" size="small" style="width: 100%">
        <ElTableColumn type="index" width="50" align="center" />
        <ElTableColumn prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <ElTableColumn prop="importTime" label="导入时间" width="160" />
        <ElTableColumn prop="total" label="总记录" width="80" align="center" />
        <ElTableColumn prop="success" label="成功" width="80" align="center">
          <template #default="{ row }">
            <span class="text-green-600">{{ row.success }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="created" label="新建" width="80" align="center">
          <template #default="{ row }">
            <span class="text-blue-600">{{ row.created || 0 }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="updated" label="更新" width="80" align="center">
          <template #default="{ row }">
            <span class="text-orange-600">{{ row.updated || 0 }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="failed" label="失败" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.failed > 0 ? 'text-red-600' : ''">{{ row.failed }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 'success' ? 'success' : row.status === 'partial' ? 'warning' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : row.status === 'partial' ? '部分成功' : '失败' }}
            </ElTag>
          </template>
        </ElTableColumn>
      </ElTable>
      <ElEmpty v-else description="暂无导入记录" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Upload, Refresh } from '@element-plus/icons-vue'
import { fetchImportData } from '@/api/dashboard'

defineOptions({ name: 'DataImport' })

const uploadRef = ref()
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const importResult = ref<{
  code: number
  message: string
  data?: {
    total: number
    success: number
    failed: number
    created?: number
    updated?: number
    errors: string[]
  }
} | null>(null)

// 导入记录（使用 localStorage 存储）
interface ImportRecord {
  fileName: string
  importTime: string
  total: number
  success: number
  failed: number
  created?: number
  updated?: number
  status: 'success' | 'partial' | 'failed'
}

const importHistory = ref<ImportRecord[]>([])

// 加载导入记录
const loadImportHistory = () => {
  try {
    const stored = localStorage.getItem('import_history')
    if (stored) {
      importHistory.value = JSON.parse(stored)
    }
  } catch {
    importHistory.value = []
  }
}

// 保存导入记录
const saveImportRecord = (fileName: string, data: any) => {
  const record: ImportRecord = {
    fileName,
    importTime: new Date().toLocaleString('zh-CN'),
    total: data.total || 0,
    success: data.success || 0,
    failed: data.failed || 0,
    created: data.created || 0,
    updated: data.updated || 0,
    status: data.failed === 0 ? 'success' : data.success > 0 ? 'partial' : 'failed'
  }
  importHistory.value.unshift(record)
  // 最多保留 50 条
  if (importHistory.value.length > 50) {
    importHistory.value = importHistory.value.slice(0, 50)
  }
  localStorage.setItem('import_history', JSON.stringify(importHistory.value))
}

// 文件变化
const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
  importResult.value = null
}

// 文件删除
const handleFileRemove = () => {
  selectedFile.value = null
  importResult.value = null
}

// 开始导入
const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  uploading.value = true
  importResult.value = null

  try {
    const res: any = await fetchImportData(selectedFile.value, 'order')
    importResult.value = {
      code: res.code,
      message: res.message || (res.code === 200 ? '导入成功' : '导入失败'),
      data: res.data
    }

    // 保存到导入记录
    if (res.data || res.code === 200) {
      const recordData = res.data || { total: 0, success: 0, failed: 0, created: 0, updated: 0 }
      saveImportRecord(selectedFile.value.name, recordData)
    }

    if (res.code === 200) {
      ElMessage.success(res.message || '导入成功')
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error: any) {
    importResult.value = {
      code: 500,
      message: error?.message || '导入失败'
    }
    ElMessage.error('导入失败')
  } finally {
    uploading.value = false
  }
}

// 重置
const handleReset = () => {
  selectedFile.value = null
  importResult.value = null
  uploadRef.value?.clearFiles()
}

onMounted(() => {
  loadImportHistory()
})
</script>

<style scoped>
.data-import-page {
  background: transparent;
}

.upload-area {
  width: 100%;
}

:deep(.el-upload-dragger) {
  padding: 40px 20px;
}

:deep(.el-radio-button__inner) {
  padding: 8px 16px;
}

/* 导入结果区域 */
.import-result-box {
  padding: 1rem;
  border-radius: 0.5rem;
  background-color: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
}

/* 确保文字在暗色主题下可见 */
:deep(.el-card) {
  --el-card-bg-color: var(--el-bg-color);
}

/* 上传区域文字颜色 */
:deep(.el-upload__text) {
  color: var(--el-text-color-regular);
}

:deep(.el-upload__text em) {
  color: var(--el-color-primary);
}

:deep(.el-upload__tip) {
  color: var(--el-text-color-secondary);
}
</style>

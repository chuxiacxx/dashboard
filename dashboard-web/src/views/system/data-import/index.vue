<!-- 数据导入页面 -->
<template>
  <div class="data-import-page art-full-height p-5">
    <ElCard class="art-table-card" shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-medium">数据导入</h3>
          <el-button size="small" text @click="loadImportTypes">
            <Refresh class="mr-1" />
            刷新
          </el-button>
        </div>
      </template>

      <div class="max-w-4xl">
        <!-- 数据类型选择 -->
        <div class="mb-6">
          <h4 class="text-base font-medium mb-3 text-gray-800 dark:text-gray-200">选择数据类型</h4>
          <ElRadioGroup v-model="selectedType" size="large">
            <ElRadioButton
              v-for="item in importTypes"
              :key="item.type"
              :value="item.type"
            >
              {{ item.name }}
            </ElRadioButton>
          </ElRadioGroup>
        </div>

        <!-- 文件上传和导入说明 - 水平布局 -->
        <div class="flex gap-6 mb-6">
          <!-- 文件上传区域 -->
          <div class="flex-1">
            <h4 class="text-base font-medium mb-3 text-gray-800 dark:text-gray-200">上传文件</h4>
            <ElUpload
              ref="uploadRef"
              class="upload-area"
              drag
              :auto-upload="false"
              :limit="1"
              :accept="acceptTypes"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 {{ selectedTypeInfo?.extensions || '.xlsx, .xls, .csv' }} 格式，文件大小不超过 10MB
                </div>
              </template>
            </ElUpload>
          </div>

          <!-- 导入说明 -->
          <div class="w-80 import-info-box">
            <h4 class="text-base font-medium mb-3 text-gray-800 dark:text-gray-200">导入说明</h4>
            <ul class="text-sm text-gray-600 dark:text-gray-300 space-y-1">
              <li>1. 请先下载导入模板，确保数据格式正确</li>
              <li>2. Excel 文件需符合模板格式，第一行为表头</li>
              <li>3. CSV 文件请使用 UTF-8 编码，避免中文乱码</li>
              <li>4. 导入过程中请勿关闭页面</li>
              <li>5. 导入完成后可在数据列表中查看导入结果</li>
            </ul>
          </div>
        </div>

        <!-- 下载模板 -->
        <div class="mb-6">
          <h4 class="text-base font-medium mb-3 text-gray-800 dark:text-gray-200">导入模板</h4>
          <div class="flex flex-wrap gap-2">
            <el-button
              v-for="item in importTypes"
              :key="item.type"
              size="small"
              @click="downloadTemplate(item.type)"
            >
              <Download class="mr-1" />
              下载 {{ item.name }} 模板
            </el-button>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-3">
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
        <div v-if="importResult" class="mt-6">
          <ElAlert
            :type="importResult.code === 200 ? 'success' : 'error'"
            :title="importResult.code === 200 ? '导入完成' : '导入失败'"
            :description="importResult.message"
            show-icon
          />
          <div v-if="importResult.data" class="mt-3 import-result-box">
            <div class="flex gap-6 text-sm">
              <span class="text-gray-800 dark:text-gray-200">总记录: <strong>{{ importResult.data.total }}</strong></span>
              <span class="text-green-600">成功: <strong>{{ importResult.data.success }}</strong></span>
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

    <!-- 历史导入记录 -->
    <ElCard class="art-table-card mt-5" shadow="never">
      <template #header>
        <h3 class="text-lg font-medium">导入记录</h3>
      </template>
      <ElEmpty description="暂无导入记录" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Upload, Download, Refresh } from '@element-plus/icons-vue'
import { fetchImportTypes, fetchImportData, fetchImportTemplate } from '@/api/dashboard'

defineOptions({ name: 'DataImport' })

const uploadRef = ref()
const selectedType = ref('sales')
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const importResult = ref<{
  code: number
  message: string
  data?: {
    total: number
    success: number
    failed: number
    errors: string[]
  }
} | null>(null)

interface ImportType {
  type: string
  name: string
  extensions: string
}

const importTypes = ref<ImportType[]>([])

const selectedTypeInfo = computed(() =>
  importTypes.value.find(t => t.type === selectedType.value)
)

const acceptTypes = computed(() => {
  const ext = selectedTypeInfo.value?.extensions || '.xlsx,.xls,.csv'
  return ext.replace(/\s/g, '')
})

// 加载导入类型
const loadImportTypes = async () => {
  try {
    const res: any = await fetchImportTypes()
    if (res && res.code === 200 && res.data) {
      importTypes.value = res.data
    }
  } catch (error) {
    console.error('获取导入类型失败:', error)
    ElMessage.error('获取导入类型失败')
  }
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

// 下载模板
const downloadTemplate = async (type: string) => {
  try {
    const res: any = await fetchImportTemplate(type)
    const blob = new Blob([res], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${type}_template.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
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
    const res: any = await fetchImportData(selectedFile.value, selectedType.value)
    importResult.value = {
      code: res.code,
      message: res.message || (res.code === 200 ? '导入成功' : '导入失败'),
      data: res.data
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

// 初始化
loadImportTypes()
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

/* 导入说明区域 - 使用 Element Plus 变量适配主题 */
.import-info-box {
  padding: 1rem;
  border-radius: 0.5rem;
  background-color: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
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

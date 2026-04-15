<template>
  <div class="p-4">
    <h2 style="margin-bottom: 20px;">222用户列表（测试页面）</h2>

    <a-table :columns="columns" :data="data" row-key="id" />

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id'
  },
  {
    title: '用户名',
    dataIndex: 'name'
  },
  {
    title: '邮箱',
    dataIndex: 'email'
  }
]

const data = ref([
  { id: 1, name: '张三', email: 'zhangsan@example.com' },
  { id: 2, name: '李四', email: 'lisi@example.com' }
])
</script>

<style scoped>
.p-4 {
  padding: 20px;
}
</style>

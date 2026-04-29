<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
    width="30%"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="80px">
      <ElFormItem label="用户名" prop="userName">
        <ElInput v-model="formData.userName" placeholder="请输入用户名" :disabled="isEdit" />
      </ElFormItem>
      <ElFormItem v-if="isEdit" label="昵称" prop="nickName">
        <ElInput v-model="formData.nickName" placeholder="请输入昵称" />
      </ElFormItem>
      <ElFormItem label="邮箱" prop="userEmail">
        <ElInput v-model="formData.userEmail" placeholder="请输入邮箱" />
      </ElFormItem>
      <ElFormItem label="手机号" prop="userPhone">
        <ElInput v-model="formData.userPhone" placeholder="请输入手机号" />
      </ElFormItem>
      <ElFormItem label="性别" prop="userGender">
        <ElSelect v-model="formData.userGender" placeholder="请选择性别">
          <ElOption label="男" value="1" />
          <ElOption label="女" value="2" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem v-if="isEdit" label="状态" prop="status">
        <ElSelect v-model="formData.status" placeholder="请选择状态">
          <ElOption label="正常" value="0" />
          <ElOption label="禁用" value="1" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="角色" prop="roleIds">
        <ElSelect v-model="formData.roleIds" multiple placeholder="请选择角色">
          <ElOption
            v-for="role in roleList"
            :key="role.roleId"
            :value="role.roleId"
            :label="role.roleName"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem v-if="!isEdit" label="密码" prop="password">
        <ElInput v-model="formData.password" type="password" placeholder="请输入密码" show-password />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchAddUser, fetchUpdateUser, fetchGetRoleList } from '@/api/system-manage'
  import { ElMessage } from 'element-plus'

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  interface RoleItem {
    roleId: number
    roleName: string
    roleCode: string
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 角色列表数据
  const roleList = ref<RoleItem[]>([])

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const isEdit = computed(() => props.type === 'edit')
  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 表单数据 - 匹配后端 UserDTO 格式
  const formData = reactive({
    userName: '',
    nickName: '',
    userEmail: '',
    userPhone: '',
    userGender: '',
    status: '0',
    roleIds: [] as number[],
    password: ''
  })

  // 加载角色列表
  const loadRoleList = async () => {
    try {
      const res: any = await fetchGetRoleList({ current: 1, size: 100 })
      if (res && res.code === 200 && res.data) {
        roleList.value = res.data.records || []
      }
    } catch (error) {
      console.error('获取角色列表失败:', error)
    }
  }

  // 表单验证规则
  const rules: FormRules = {
    userName: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    userPhone: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    userEmail: [
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    roleIds: [
      { type: 'array', required: true, message: '请选择至少一个角色', trigger: 'change' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ]
  }

  // 根据角色code获取roleId
  const getRoleIdByCode = (code: string): number | undefined => {
    const role = roleList.value.find(r => r.roleCode === code)
    return role?.roleId
  }

  // 根据角色Id获取roleCode
  const getRoleCodeById = (id: number): string | undefined => {
    const role = roleList.value.find(r => r.roleId === id)
    return role?.roleCode
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = async () => {
    await loadRoleList()

    if (isEdit.value && props.userData) {
      const row = props.userData
      // 将角色code转换为roleId
      const roleIds = (row.userRoles || [])
        .map((code: string) => getRoleIdByCode(code))
        .filter((id: number | undefined): id is number => id !== undefined)

      Object.assign(formData, {
        userName: row.userName || '',
        nickName: row.nickName || '',
        userEmail: row.userEmail || '',
        userPhone: row.userPhone || '',
        userGender: row.userGender || '',
        status: row.status || '0',
        roleIds: roleIds,
        password: ''
      })
    } else {
      Object.assign(formData, {
        userName: '',
        nickName: '',
        userEmail: '',
        userPhone: '',
        userGender: '',
        status: '0',
        roleIds: [],
        password: ''
      })
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.userData],
    ([visible]) => {
      if (visible) {
        initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   * 验证通过后调用API提交
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 构建符合后端 UserDTO 格式的数据
          const submitData: any = {
            userName: formData.userName,
            nickName: formData.nickName,
            userEmail: formData.userEmail,
            userPhone: formData.userPhone,
            userGender: formData.userGender,
            status: formData.status,
            roleIds: formData.roleIds
          }

          // 编辑时不需要密码
          if (!isEdit.value) {
            submitData.password = formData.password
          }

          if (isEdit.value && props.userData?.id) {
            submitData.id = props.userData.id
            await fetchUpdateUser(submitData)
            ElMessage.success('更新成功')
          } else {
            await fetchAddUser(submitData)
            ElMessage.success('添加成功')
          }
          dialogVisible.value = false
          emit('submit')
        } catch (error: any) {
          console.error('提交失败:', error)
          ElMessage.error(error?.message || '操作失败')
        }
      }
    })
  }
</script>

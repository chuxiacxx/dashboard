<!-- 个人中心页面 -->
<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="art-card-sm relative p-9 pb-6 overflow-hidden text-center">
          <img class="absolute top-0 left-0 w-full h-50 object-cover" src="@imgs/user/bg.webp" />
          <img
            class="relative z-10 w-20 h-20 mt-30 mx-auto object-cover border-2 border-white rounded-full"
            src="@imgs/user/avatar.webp"
          />
          <h2 class="mt-5 text-xl font-normal">{{ userInfo.nickName || userInfo.userName }}</h2>
          <p class="mt-5 text-sm">{{ greeting }}</p>

          <div class="w-75 mx-auto mt-7.5 text-left">
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:mail-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userInfo.email || '未设置' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:phone-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userInfo.phone || '未设置' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:shield-user-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userRoleLabel }}</span>
            </div>
          </div>

          <div class="mt-10">
            <h3 class="text-sm font-medium">角色</h3>
            <div class="flex flex-wrap justify-center mt-3.5">
              <div
                v-for="role in userInfo.roles"
                :key="role"
                class="py-1 px-1.5 mr-2.5 mb-2.5 text-xs border border-g-300 rounded"
              >
                {{ roleNameMap[role] || role }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <div class="art-card-sm">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">基本设置</h1>

          <ElForm
            :model="form"
            class="box-border p-5 [&>.el-row_.el-form-item]:w-[calc(50%-10px)] [&>.el-row_.el-input]:w-full [&>.el-row_.el-select]:w-full"
            ref="ruleFormRef"
            :rules="rules"
            label-width="86px"
            label-position="top"
          >
            <ElRow>
              <ElFormItem label="用户名" prop="userName">
                <ElInput v-model="form.userName" disabled />
              </ElFormItem>
              <ElFormItem label="昵称" prop="nickName" class="ml-5">
                <ElInput v-model="form.nickName" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="性别" prop="gender">
                <ElSelect v-model="form.gender" placeholder="选择性别" :disabled="!isEdit">
                  <ElOption
                    v-for="item in genderOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
              <ElFormItem label="邮箱" prop="email" class="ml-5">
                <ElInput v-model="form.email" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="手机" prop="phone">
                <ElInput v-model="form.phone" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="handleEditProfile">
                {{ isEdit ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>

        <div class="art-card-sm my-5">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">修改密码</h1>

          <ElForm :model="pwdForm" class="box-border p-5" ref="pwdFormRef" :rules="pwdRules" label-width="86px" label-position="top">
            <ElFormItem label="当前密码" prop="oldPassword">
              <ElInput
                v-model="pwdForm.oldPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请输入当前密码"
              />
            </ElFormItem>

            <ElFormItem label="新密码" prop="newPassword">
              <ElInput
                v-model="pwdForm.newPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请输入新密码"
              />
            </ElFormItem>

            <ElFormItem label="确认新密码" prop="confirmPassword">
              <ElInput
                v-model="pwdForm.confirmPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请再次输入新密码"
              />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="handleChangePassword">
                {{ isEditPwd ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { fetchGetUserInfo, fetchUpdateUserInfo, fetchChangePassword } from '@/api/auth'
  import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
  import { computed } from 'vue'

  defineOptions({ name: 'UserCenter' })

  const userStore = useUserStore()
  const userInfo = computed(() => userStore.getUserInfo as Api.Auth.UserInfo)

  const ruleFormRef = ref<FormInstance>()
  const pwdFormRef = ref<FormInstance>()

  const isEdit = ref(false)
  const isEditPwd = ref(false)

  const greeting = ref('')

  const roleNameMap: Record<string, string> = {
    R_SUPER: '超级管理员',
    R_ADMIN: '管理员',
    R_USER: '普通用户'
  }

  const userRoleLabel = computed(() => {
    if (!userInfo.value?.roles?.length) return '未分配角色'
    const roleNames = userInfo.value.roles.map(r => roleNameMap[r] || r)
    return roleNames.join(', ')
  })

  const genderOptions = [
    { value: '1', label: '男' },
    { value: '2', label: '女' }
  ]

  /**
   * 用户信息表单
   */
  const form = reactive({
    userName: '',
    nickName: '',
    email: '',
    phone: '',
    gender: ''
  })

  /**
   * 密码修改表单
   */
  const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    email: [
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    phone: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ]
  })

  const validateConfirmPassword = (rule: any, value: string, callback: any) => {
    if (value !== pwdForm.newPassword) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  }

  const pwdRules = reactive<FormRules>({
    oldPassword: [
      { required: true, message: '请输入当前密码', trigger: 'blur' }
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      { validator: validateConfirmPassword, trigger: 'blur' }
    ]
  })

  /**
   * 获取问候语
   */
  const updateGreeting = () => {
    const h = new Date().getHours()
    if (h >= 6 && h < 9) greeting.value = '早上好'
    else if (h >= 9 && h < 11) greeting.value = '上午好'
    else if (h >= 11 && h < 13) greeting.value = '中午好'
    else if (h >= 13 && h < 18) greeting.value = '下午好'
    else if (h >= 18 && h < 24) greeting.value = '晚上好'
    else greeting.value = '夜深了，早点休息'
  }

  /**
   * 加载用户信息
   */
  const loadUserInfo = async () => {
    try {
      const res: any = await fetchGetUserInfo()
      if (res && res.code === 200 && res.data) {
        const data = res.data
        userStore.setUserInfo(data)
        form.userName = data.userName || ''
        form.nickName = data.nickName || ''
        form.email = data.email || ''
        form.phone = data.phone || ''
        form.gender = data.gender || ''
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  /**
   * 切换用户信息编辑状态 / 保存
   */
  const handleEditProfile = async () => {
    if (isEdit.value) {
      // 保存
      if (!ruleFormRef.value) return
      try {
        await ruleFormRef.value.validate()
      } catch {
        return
      }

      try {
        const res: any = await fetchUpdateUserInfo({
          nickName: form.nickName,
          email: form.email,
          phone: form.phone,
          gender: form.gender
        })
        if (res && res.code === 200) {
          ElMessage.success('保存成功')
          isEdit.value = false
        } else {
          ElMessage.error(res?.message || '保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    } else {
      isEdit.value = true
    }
  }

  /**
   * 切换密码编辑状态 / 保存
   */
  const handleChangePassword = async () => {
    if (isEditPwd.value) {
      // 保存
      if (!pwdFormRef.value) return
      try {
        await pwdFormRef.value.validate()
      } catch {
        return
      }

      try {
        const res: any = await fetchChangePassword(pwdForm.oldPassword, pwdForm.newPassword)
        if (res && res.code === 200) {
          ElMessage.success('密码修改成功')
          isEditPwd.value = false
          pwdForm.oldPassword = ''
          pwdForm.newPassword = ''
          pwdForm.confirmPassword = ''
        } else {
          ElMessage.error(res?.message || '修改失败')
        }
      } catch (error) {
        ElMessage.error('修改失败')
      }
    } else {
      isEditPwd.value = true
    }
  }

  onMounted(() => {
    updateGreeting()
    loadUserInfo()
  })
</script>

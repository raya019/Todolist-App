<script setup>
import ContainerUser from '@/components/ContainerUser.vue'
import UpdatePassword from '@/components/UpdatePassword.vue'
import UpdateUser from '@/components/UpdateUser.vue'
import { useChangePassword, useGetUser, useUpdateUser } from '@/service/storeUser'
import { toTypedSchema } from '@vee-validate/yup'
import { useForm } from 'vee-validate'
import { ref, nextTick } from 'vue'
import { object, string } from 'yup'

const getUser = useGetUser()
const updatePassword = useChangePassword()
const updateUser = useUpdateUser()
const isDisabled = ref(false)
const isShow = ref(updateUser.data || updatePassword.data)
const username = ref(getUser.data.name)
const password = ref(null)
const newPassword = ref(null)
const confirmPassword = ref(null)
const disabledButton = [
  `disabled:cursor-not-allowed disabled:bg-yellow-300/50 disabled:hover:text-yellow-700`,
]
const disabledInput = [`disabled:cursor-not-allowed disabled:bg-black/30`]

const schemaUpdateUser = object({
  nameInput: string()
    .required('Name is required')
    .min(5, 'Name must be more than 5 characters')
    .max(30),
})

const schemaUpdatePassword = object({
  oldPassword: string().required().min(8, 'Password must be less than 8 characters'),
  newPassword: string().required().min(8, 'Password must be less than 8 characters'),
  confirmPassword: string()
    .required()
    .min(8, 'Password must be less than 8 characters')
    .oneOf([ref('password')], 'password not match'),
})

const schema = object({
  ...schemaUpdateUser.fields,
  ...schemaUpdatePassword.fields,
})

const {
  errors: errorField,
  values,
  setErrors,
  handleSubmit,
  validateField,
} = useForm({
  validationSchema: toTypedSchema(schema),
})

const onChangePassword = handleSubmit(
  async (values) => {
    await updatePassword.handleChangePassword({
      oldPassword: values.oldPassword,
      newPassword: values.newPassword,
      confirmPassword: values.confirmPassword,
    })
    password.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
    isDisabled.value = !isDisabled.value
  },
  async ({ errors }) => {
    await nextTick()
    const firstError = Object.keys(errors)[0]

    const el = document.querySelector(`[name="${firstError}"]`)

    el?.focus()
  },
)

const onUpdateUser = async () => {
  const { valid, errors } = await validateField('nameInput')
  if (valid && !errors) {
    await updateUser.handleUserUpdate({ name: values.nameInput })
  } else {
    await nextTick()
    const firstError = Object.keys(errorField.value)[0]

    const el = document.querySelector(`[name="${firstError}"]`)

    el?.focus()
  }
}

const onActiveChangePassword = () => {
  isDisabled.value = !isDisabled.value
  username.value = getUser.data.name
}

function onCancelChangePassword() {
  password.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  isDisabled.value = !isDisabled.value
  setErrors({
    oldPassword: undefined,
    newPassword: undefined,
    confirmPassword: undefined,
  })
}
</script>

<template>
  <ContainerUser :is-show="isShow">
    <UpdateUser
      :email="getUser.data.email"
      :is-disabled="isDisabled"
      :disabled-input="disabledInput"
      :disabled-button="disabledButton"
      :error="errorField.nameInput"
      v-model="username"
      @handle-active-change-password="onActiveChangePassword"
      @handle-update-user="onUpdateUser"
    />

    <UpdatePassword
      :is-disabled="!isDisabled"
      :disabled-input="disabledInput"
      :disabled-button="disabledButton"
      :error="errorField"
      v-model:password="password"
      v-model:new-password="newPassword"
      v-model:confirm-password="confirmPassword"
      @handle-change-password="onChangePassword"
      @handle-cancel="onCancelChangePassword"
    />
  </ContainerUser>
</template>

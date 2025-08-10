<script setup>
import BaseInput from '@/components/Input/BaseInput.vue'
import FadeTransitionGroup from '@/components/Transition/FadeTransitionGroup.vue'
import { useRegister } from '@/service/storeAuth'
import { toTypedSchema } from '@vee-validate/yup'
import { useForm } from 'vee-validate'
import { nextTick, ref } from 'vue'
import { object, string } from 'yup'

const register = useRegister()
const isShow = ref(false)
const name = ref(null)
const email = ref(null)
const password = ref(null)

const schema = object({
  name: string().required().min(5, 'Name must be more than 5 characters').max(30),
  email: string().required('Email is required').email('format email not valid'),
  password: string()
    .min(8, 'Password must be less than 8 characters')
    .required('Password is required'),
})

const { errors, handleSubmit } = useForm({
  validationSchema: toTypedSchema(schema),
})

const onSubmitRegister = handleSubmit(
  async (values, { resetForm }) => {
    await register.handleRegister(values)

    isShow.value = true
    setTimeout(() => {
      isShow.value = false
      register.$reset()
    }, 5000)
    resetForm()
  },
  async ({ errors }) => {
    await nextTick()
    const firstError = Object.keys(errors)[0]

    const el = document.querySelector(`[name="${firstError}"]`)

    el?.focus()
  },
)
</script>

<template>
  <main class="flex flex-col justify-center items-center w-full h-screen gap-y-3">
    <FadeTransitionGroup tag="div">
      <div
        v-if="register.data && isShow"
        class="bg-green-100 py-1 px-5 rounded-full border-2 border-solid border-green-400"
      >
        <p class="text-green-600">{{ register.data }}</p>
      </div>

      <div
        v-else-if="register.error && isShow"
        class="bg-red-100 py-1 px-5 rounded-full border-2 border-solid border-red-400"
      >
        <p class="text-red-600">{{ register.error }}</p>
      </div>
    </FadeTransitionGroup>

    <div
      class="border-2 rounded-md border-solid border-orange-300 p-4 w-72 h-[75%] md:h-[60%] shadow-style"
    >
      <div class="flex flex-col gap-y-5">
        <h1 class="text-3xl">Registrasi</h1>

        <form class="flex flex-col gap-y-3" @submit.prevent="onSubmitRegister">
          <div class="flex flex-col gap-y-1">
            <BaseInput
              title="Name :"
              class-error="text-xs font-bold text-red-900"
              class-label="text-sm"
              :class-input="[
                `rounded-md px-2 text-sm text-black focus:outline-red-500`,
                errors.name ? 'outline outline-2 outline-red-500' : '',
              ]"
              name="name"
              v-model="name"
            />
          </div>

          <div class="flex flex-col gap-y-1">
            <BaseInput
              type="email"
              title="Email :"
              class-error="text-xs font-bold text-red-900"
              class-label="text-sm"
              :class-input="[
                `rounded-md px-2 text-sm text-black focus:outline-red-500`,
                errors.email ? 'outline outline-2 outline-red-500' : '',
              ]"
              name="email"
              v-model="email"
            />
          </div>

          <div class="flex flex-col gap-y-1">
            <BaseInput
              type="password"
              title="Password :"
              class-error="text-xs font-bold text-red-900"
              class-label="text-sm"
              :class-input="[
                `rounded-md px-2 text-sm text-black focus:outline-red-500`,
                errors.password ? 'outline outline-2 outline-red-500' : '',
              ]"
              name="password"
              v-model="password"
            />
          </div>

          <button
            type="submit"
            class="bg-yellow-300 text-yellow-700 text-sm hover:text-white w-20 p-2 rounded-md"
          >
            Register
          </button>
        </form>

        <router-link
          :to="{ name: 'login' }"
          class="text-blue-300 hover:text-white self-center underline text-sm"
          >Login</router-link
        >
      </div>
    </div>
  </main>
</template>

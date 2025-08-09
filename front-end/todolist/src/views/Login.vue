<script setup>
import BaseInput from '@/components/Input/BaseInput.vue'
import FadeTransitionGroup from '@/components/Transition/FadeTransitionGroup.vue'
import { useLogin } from '@/service/storeAuth'
import { toTypedSchema } from '@vee-validate/yup'
import { useForm } from 'vee-validate'
import { nextTick, onUnmounted, ref } from 'vue'
import { object, string } from 'yup'

const login = useLogin()
const email = ref(null)
const password = ref(null)

const schema = object({
  email: string().required('Email is required').email('format email not valid'),
  password: string()
    .min(8, 'Password must be less than 8 characters')
    .required('Password is required'),
})

const { handleSubmit, errors } = useForm({
  validationSchema: toTypedSchema(schema),
})

const onSubmit = handleSubmit(
  async (values, { resetForm }) => {
    await login.handleLogin(values)
    resetForm()
  },
  async ({ errors }) => {
    await nextTick()
    const firstError = Object.keys(errors)[0]
    console.log(firstError)

    const el = document.querySelector(`[name="${firstError}"]`)
    console.log(el)

    el?.focus()
  },
)

onUnmounted(() => {
  login.$reset()
})
</script>

<template>
  <main class="flex justify-center items-center flex-col gap-y-3 w-full h-screen">
    <FadeTransitionGroup>
      <div
        v-if="login.error"
        class="bg-red-100 py-1 px-5 rounded-full border-2 border-solid border-red-400"
      >
        <p class="text-red-600">{{ login.error }}</p>
      </div>
    </FadeTransitionGroup>

    <div
      class="border-2 rounded-md border-solid border-orange-300 p-4 w-72 h-2/3 md:h-1/2 shadow-style"
    >
      <div class="flex flex-col gap-y-5 lg:gap-y-3">
        <h1 class="text-3xl">Login</h1>

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
          @click="onSubmit"
          class="bg-yellow-300 text-yellow-700 font-medium hover:text-white w-20 px-1 py-2 text-sm rounded-md"
        >
          Login
        </button>

        <router-link
          :to="{ name: 'register' }"
          class="text-blue-300 hover:text-white self-center underline text-sm"
          >Register</router-link
        >
      </div>
    </div>
  </main>
</template>

<script setup>
import { useChangePassword, useUpdateUser } from '@/service/storeUser'
import FadeTransitionGroup from './Transition/FadeTransitionGroup.vue'

const updateUser = useUpdateUser()
const changePasswordUser = useChangePassword()

defineProps(['isShow'])
</script>

<template>
  <div class="flex flex-col justify-center items-center lg:h-screen gap-y-5 pt-20 lg:pt-0">
    <h1 class="text-2xl text-white font-bold">User Profile</h1>
    <FadeTransitionGroup tag="div">
      <div
        v-if="(updateUser.data || changePasswordUser.data) && isShow"
        class="bg-green-100 py-1 px-5 rounded-full border-2 border-solid border-green-400"
      >
        <p class="text-green-600">
          {{ updateUser.data?.message || changePasswordUser.data }}
        </p>
      </div>
    </FadeTransitionGroup>

    <div
      class="flex flex-col justify-around md:flex-row gap-x-28 p-5 w-10/12 md:w-1/2 rounded-lg border-2 bg-white/20 backdrop-blur-lg"
    >
      <slot />
    </div>
  </div>
</template>

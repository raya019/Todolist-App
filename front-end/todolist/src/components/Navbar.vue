<script setup>
import { useLogout } from '@/service/storeAuth'
import { useGetUser } from '@/service/storeUser'

const getUser = useGetUser()
const logout = useLogout()
</script>

<template>
  <header
    class="fixed flex z-50 items-center px-10 py-3 border-b-2 w-full justify-between md:justify-around"
  >
    <router-link :to="{ name: 'todolist' }" class="text-sm md:text-base">Home</router-link>
    <router-link
      :to="{ name: 'user', params: { user: getUser.data.name } }"
      class="text-sm md:text-base"
      >Profile</router-link
    >
    <router-link
      :replace="true"
      :to="{ name: 'login' }"
      class="text-sm md:text-base"
      @click="logout.onLogout()"
      >Logout</router-link
    >
  </header>
</template>

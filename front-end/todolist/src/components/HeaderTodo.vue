<script setup>
import BaseInput from './Input/BaseInput.vue'

const model = defineModel()
defineEmits(['handleSubmit'])
const props = defineProps(['errors'])
</script>

<template>
  <h1 class="text-4xl text-center py-5 lg:text-5xl">My Todo List 📝</h1>
  <div class="flex justify-center items-center flex-wrap gap-3 lg:mt-5 px-5">
    <BaseInput
      title="Masukkan Todo :"
      class-label="text-lg text-white font-sans font-bold order-1 lg:order-2 text-center lg:text-2xl"
      :class-input="[
        `border-2 border-black rounded-full text-black order-2  font-sans text-center text-lg lg:order-3`,
        props.errors ? 'outline outline-2 outline-red-500' : '',
      ]"
      class-error="shrink lg:shrink-0 w-full text-center text-xs order-3 lg:order-1 text-red-500 lg:inline-block"
      name="todo"
      v-model="model"
    />

    <button
      @click="$emit('handleSubmit')"
      class="bg-white text-red-900 px-5 py-1 rounded-full hover:text-black order-4 lg:order"
    >
      Tambah
    </button>
  </div>
</template>

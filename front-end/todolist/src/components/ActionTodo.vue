<script setup>
const value = defineModel()
defineEmits(['click'])
</script>

<template>
  <div class="py-3 flex justify-center items-center flex-wrap gap-5">
    <select
      v-model="value"
      class="text-black text-sm lg:text-base font-sans p-2 lg:p-3 rounded-full order-2 lg:order-1"
    >
      <option value="input">Urutkan berdasarkan Urutan Input</option>
      <option value="name">Urutkan berdasarkan Urutan Name</option>
      <option value="checked">Urutkan berdasarkan Urutan Checklist</option>
    </select>
    <button
      class="py-2 px-4 lg:px-7 rounded-full bg-white text-red-900 text-sm lg:text-base font-sans font-medium order-1 hover:text-black Lg:order-2"
      @click="$emit('click')"
    >
      Hapus semua
    </button>
  </div>
</template>

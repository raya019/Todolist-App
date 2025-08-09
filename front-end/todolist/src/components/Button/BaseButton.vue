<script setup>
defineEmits(['handleButton'])
defineProps(['isDisabled', 'classButton', 'title'])
</script>

<template>
  <button :class="classButton" @click="$emit('handleButton')" :disabled="isDisabled">
    {{ title }}
  </button>
</template>

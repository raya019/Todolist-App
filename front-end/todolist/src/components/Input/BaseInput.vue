<script setup>
import { useField } from 'vee-validate'

const props = defineProps([
  'classLabel',
  'classInput',
  'classError',
  'name',
  'title',
  'modelValue',
  'type',
  'isDisabled',
])

const { value, errorMessage } = useField(() => props.name, undefined, {
  validateOnValueUpdate: false,
  syncVModel: true,
  validateOnMount: false,
})
</script>

<template>
  <label :class="props.classLabel" :for="props.name">{{ title }}</label>
  <input
    :id="props.name"
    :name="props.name"
    :type="type || 'text'"
    :class="props.classInput"
    v-model="value"
    :disabled="props.isDisabled"
  />
  <span :class="props.classError">{{ errorMessage }}</span>
</template>

<script setup>
import BaseButton from './Button/BaseButton.vue'
import BaseInput from './Input/BaseInput.vue'

const password = defineModel('password')
const newPassword = defineModel('newPassword')
const confirmPassword = defineModel('confirmPassword')
const props = defineProps(['isDisabled', 'disabledInput', 'disabledButton', 'error'])
defineEmits(['handleChangePassword', 'handleCancel'])
</script>

<template>
  <div class="flex flex-col gap-y-3 lg:gap-y-2 mt-6 md:mt-0 lg:w-60">
    <BaseInput
      type="password"
      title="Old Password :"
      class-label=""
      :class-input="[
        'border-none text-sm bg-black rounded-md py-1 md:py-2 text-slate-200 caret-orange-500 pl-2 outline-offset-[3px] focus:outline-offset-4 focus:outline-offset-4',
        props.isDisabled ? props.disabledInput : '',
        props.error.oldPassword
          ? 'outline outline-2 outline-red-500'
          : 'outline outline-yellow-300 outline-2',
      ]"
      class-error="text-xs font-bold text-red-500"
      name="oldPassword"
      :is-disabled="props.isDisabled"
      v-model="password"
    />

    <BaseInput
      type="password"
      title="New Password :"
      class-label="text-slate-200"
      :class-input="[
        'border-none text-sm bg-black rounded-md py-1 md:py-2 text-slate-200 caret-orange-500 pl-2 outline-offset-[3px]focus:outline-offset-4 ',
        props.isDisabled ? props.disabledInput : '',
        props.error.newPassword
          ? 'outline outline-2 outline-red-500'
          : 'outline outline-yellow-300 outline-2',
      ]"
      class-error="text-xs font-bold text-red-500"
      name="newPassword"
      :is-disabled="props.isDisabled"
      v-model="newPassword"
    />

    <BaseInput
      type="password"
      title="Confirm Password :"
      class-label=""
      :class-input="[
        'border-none text-sm bg-black rounded-md py-1 md:py-2 text-slate-200 caret-orange-500 pl-2 outline-offset-[3px] focus:outline-offset-4',
        props.isDisabled ? props.disabledInput : '',
        props.error.confirmPassword
          ? 'outline outline-2 outline-red-500'
          : 'outline outline-2 outline-yellow-300',
      ]"
      class-error="text-xs font-bold text-red-500"
      name="confirmPassword"
      :is-disabled="props.isDisabled"
      v-model="confirmPassword"
    />

    <BaseButton
      title="Update Password"
      @handle-button="$emit('handleChangePassword')"
      :is-disabled="props.isDisabled"
      :class="[
        'mt-5 bg-yellow-300 text-yellow-700 font-bold hover:text-white px-1 py-2 text-sm rounded-md ',
        props.isDisabled ? props.disabledButton : '',
      ]"
    />

    <BaseButton
      title="Cancel"
      @handle-button="$emit('handleCancel')"
      :is-disabled="props.isDisabled"
      :class="[
        'bg-yellow-300 text-yellow-700 font-bold hover:text-white px-1 py-2 text-sm rounded-md  ',
        props.isDisabled ? props.disabledButton : '',
      ]"
    />
  </div>
</template>

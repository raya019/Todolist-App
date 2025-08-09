<script setup>
import BaseButton from './Button/BaseButton.vue'
import BaseInput from './Input/BaseInput.vue'

const nameModel = defineModel()
const props = defineProps(['isDisabled', 'disabledInput', 'disabledButton', 'error', 'email'])
defineEmits(['handleUpdateUser', 'handleActiveChangePassword'])
</script>

<template>
  <div class="flex flex-col gap-y-3 lg:gap-y-1 lg:w-60">
    <BaseInput
      title="Name :"
      class-error="text-xs font-bold text-red-500"
      class-label="lg:text-lg text-slate-200"
      :class-input="[
        `border-none text-sm bg-black hover:outline-yellow-400 focus:outline-offset-4 hover:outline-offset-4 rounded-md py-1 md:py-2 text-slate-200  caret-orange-500 pl-2 outline-offset-[3px]`,
        props.isDisabled ? props.disabledInput : '',
        props.error ? 'outline outline-2 outline-red-500' : 'outline outline-yellow-300 outline-2 ',
      ]"
      name="nameInput"
      v-model="nameModel"
      :is-disabled="props.isDisabled"
    />

    <label for="email" class="lg:text-lg text-slate-200">Email :</label>
    <input
      type="text"
      name="email"
      id="email"
      class="disabled:cursor-not-allowed border-none outline-none text-sm rounded-md py-1 md:py-2 text-white caret-orange-500 pl-2 outline outline-offset-[3px] outline-2 outline-yellow-300 disabled:bg-black/30"
      disabled
      :value="props.email"
    />

    <BaseButton
      title="Update User"
      @handle-button="$emit('handleUpdateUser')"
      :is-disabled="props.isDisabled"
      :class-button="[
        'mt-5 bg-yellow-300 text-yellow-700 font-bold hover:text-white px-1 py-2 text-sm rounded-md',
        props.isDisabled ? props.disabledButton : '',
      ]"
    />

    <BaseButton
      title="Change Password"
      @handle-button="$emit('handleActiveChangePassword')"
      :is-disabled="props.isDisabled"
      :class-button="[
        'mt-2 bg-yellow-300 text-yellow-700 font-bold hover:text-white px-1 py-2 text-sm rounded-md',
        props.isDisabled ? props.disabledButton : '',
      ]"
    />
  </div>
</template>

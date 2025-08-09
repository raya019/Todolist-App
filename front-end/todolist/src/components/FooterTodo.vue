<script setup>
import { computed } from 'vue'

const { total } = defineProps(['total'])

const com = computed(() => {
  const result = total.length
  const done = total.filter((todo) => todo.isDone).length
  const unDone = total.filter((todo) => !todo.isDone).length
  const percentage = Math.round((done / result) * 100)

  return { result, done, unDone, percentage }
})
</script>

<template>
  <footer className="p-5 font-sans text-center md:text-lg lg:text-2xl">
    {{
      com.result === 0
        ? 'Todolist Kosong ☹️'
        : `Total Todo-List ada ${com.result}. List Yang belum Selesai ${com.unDone} Dan yang Selesai ${com.done} (${com.percentage}%)`
    }}
  </footer>
</template>

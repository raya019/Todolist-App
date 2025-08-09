<script setup>
const props = defineProps(['todos'])
defineEmits(['updateTodo', 'deleteTodo'])
</script>

<template>
  <div
    className="mt-5 bg-white/20 backdrop-blur-lg w-10/12 h-80 p-2 rounded lg:h-72 overflow-auto lg:w-1/2"
  >
    <div v-if="props.todos.length === 0" class="flex justify-center items-center h-full">
      <h1 class="text-black text-2xl">Todolist Kosong ☹️</h1>
    </div>

    <ul v-else className="list-decimal pl-8 text-black text-lg font-mono">
      <li class="w-full" v-for="todo in props.todos" :key="todo.id">
        <div class="flex gap-x-6 mb-2">
          <span :class="['line-clamp-1 w-96 ', { 'line-through': todo.isDone }]">
            {{ todo.todo }}
          </span>
          <div class="flex gap-2">
            <button
              class="text-xs py-1 px-5 text-red-900 rounded-full bg-white hover:text-black whitespace-nowrap lg:text-base"
              @click="$emit('updateTodo', todo)"
            >
              {{ todo.isDone ? 'Undone' : 'Done' }}
            </button>
            <button
              class="text-xs py-1 px-5 text-red-900 rounded-full bg-white hover:text-black whitespace-nowrap lg:text-base"
              @click="$emit('deleteTodo', todo)"
            >
              Hapus
            </button>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>

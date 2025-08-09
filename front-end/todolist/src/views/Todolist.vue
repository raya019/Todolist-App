<script setup>
import Action from '@/components/ActionTodo.vue'
import Container from '@/components/ContainerTodo.vue'
import ContentTodo from '@/components/ContentTodo.vue'
import Footer from '@/components/FooterTodo.vue'
import HeaderTodo from '@/components/HeaderTodo.vue'
import {
  useAddTodo,
  useDeleteAllTodo,
  useDeleteTodo,
  useGetTodo,
  useGetTodoSortByIsDone,
  useGetTodoSortByName,
  useUpdateTodo,
} from '@/service/storeTodolist'
import { toTypedSchema } from '@vee-validate/yup'
import { useForm } from 'vee-validate'
import { onUnmounted, ref, watchEffect, nextTick } from 'vue'
import { object, string } from 'yup'

const getTodo = useGetTodo()
const addTodo = useAddTodo()
const todoDelete = useDeleteTodo()
const todoUpdate = useUpdateTodo()
const todoDeleteAll = useDeleteAllTodo()
const todoSortByIsDone = useGetTodoSortByIsDone()
const todoSortByName = useGetTodoSortByName()
const todo = ref(null)
const sortby = ref('input')

const schema = object({
  todo: string()
    .min(5, 'Todolist must be more than 5 characters')
    .max(30, 'Todolist must be less than 30 characters'),
})

const { handleSubmit, errors } = useForm({
  validationSchema: toTypedSchema(schema),
})

const onSubmit = handleSubmit(
  async (values, { resetForm }) => {
    await addTodo.handleTodo(values)
    resetForm()
  },
  async ({ errors }) => {
    await nextTick()
    const firstError = Object.keys(errors)[0]

    const el = document.querySelector(`[name="${firstError}"]`)

    el?.focus()
  },
)

watchEffect(() => {
  if (sortby.value === 'name') {
    todoSortByName.sortName()
  } else if (sortby.value === 'checked') {
    todoSortByIsDone.sortIsDone()
  } else {
    getTodo.get()
  }
})

onUnmounted(() => {
  addTodo.$reset()
  todoUpdate.$reset()
  todoDelete.$reset()
  todoDeleteAll.$reset()
  todoSortByIsDone.$reset()
  todoSortByName.$reset()
})
</script>

<template>
  <Container>
    <HeaderTodo v-model="todo" :errors="errors.todo" @handle-submit="onSubmit" />
    <ContentTodo
      :todos="getTodo.data"
      @update-todo="todoUpdate.onTodoUpdate"
      @delete-todo="todoDelete.onTodoDelete"
    />
    <Action v-model="sortby" @click="todoDeleteAll.onTodoDeleteAll" />
    <Footer :total="getTodo.data" />
  </Container>
</template>

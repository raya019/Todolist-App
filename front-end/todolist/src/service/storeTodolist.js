import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { deleteApi, getApi, postHeaderApi, putApi } from '../util/api'

export const useGetTodo = defineStore('getTodo', () => {
  const data = ref([])
  const error = ref(null)

  async function todoGet() {
    try {
      const res = await getApi('todolist/get')
      if (res.status === 200) {
        data.value = res.data.data
      }
    } catch (e) {
      error.value = e.response
    }
  }

  const $reset = () => {
    data.value = []
    error.value = null
  }

  return { data, get: todoGet, $reset }
})

export const useAddTodo = defineStore('addTodo', () => {
  const data = ref(null)
  const error = ref(null)
  const getTodo = useGetTodo()

  async function handleTodo(values) {
    try {
      const res = await postHeaderApi('todolist/add', values)

      if (res.status === 200) {
        data.value = res.data
        getTodo.data = getTodo.data.concat(data.value.data)
      }
    } catch (e) {
      error.value = e.response.data
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return { data, error, handleTodo, $reset }
})

export const useUpdateTodo = defineStore('updateTodo', () => {
  const data = ref(null)
  const error = ref(null)
  const getTodo = useGetTodo()
  const updateDataTodo = reactive({
    id: null,
    todo: null,
    isDone: null,
  })

  async function onTodoUpdate(value) {
    updateDataTodo.id = value.id
    updateDataTodo.todo = value.todo
    updateDataTodo.isDone = !value.isDone
    try {
      const res = await putApi(`todolist/update/${updateDataTodo.id}`, updateDataTodo)
      if (res.status === 200) {
        data.value = res.data.data
        getTodo.data = getTodo.data.map((item) => {
          if (item.id === data.value.id) {
            item.todo = data.value.todo
            item.isDone = data.value.isDone
          }
          return item
        })
      }
    } catch (e) {
      error.value = e.response
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return { data, $reset, onTodoUpdate }
})

export const useDeleteTodo = defineStore('deleteTodo', () => {
  const data = ref(null)
  const error = ref(null)
  const getTodo = useGetTodo()

  async function onTodoDelete(value) {
    try {
      const res = await deleteApi(`todolist/delete/${value.id}`)
      if (res.status === 200) {
        data.value = res.data.message
        getTodo.data = getTodo.data.filter((item) => item.id !== value.id)
      }
    } catch (e) {
      error.value = e.response
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return { data, onTodoDelete, $reset }
})

export const useDeleteAllTodo = defineStore('deleteAllTodo', () => {
  const data = ref(null)
  const error = ref(null)
  const getTodo = useGetTodo()

  async function onTodoDeleteAll() {
    try {
      const res = await deleteApi(`todolist/delete-all`)
      if (res.status === 200) {
        data.value = res.data.message
        getTodo.data = []
      }
    } catch (e) {
      error.value = e.response
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return { data, onTodoDeleteAll, $reset }
})

export const useGetTodoSortByName = defineStore('getTodoSortByName', () => {
  const data = ref(null)
  const error = ref(null)
  const getTodo = useGetTodo()

  async function sortName() {
    try {
      const res = await getApi(`todolist/get-order-by-name`)
      if (res.status === 200) {
        data.value = res.data.data
        getTodo.data = [...data.value]
      }
    } catch (e) {
      error.value = e.response
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return { data, sortName, $reset }
})

export const useGetTodoSortByIsDone = defineStore('getTodoSortByIsDone', () => {
  const data = ref(null)
  const error = ref(null)
  const getTodo = useGetTodo()

  async function sortIsDone() {
    try {
      const res = await getApi(`todolist/get-order-by-done`)
      if (res.status === 200) {
        data.value = res.data.data
        getTodo.data = [...data.value]
      }
    } catch (e) {
      error.value = e.response
    }
  }

  function $reset() {
    data.value = null
    error.value = null
  }

  return { data, sortIsDone, $reset }
})

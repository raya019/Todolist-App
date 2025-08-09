import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import router from './service/router'
import './style.css'

const pinia = createPinia()

createApp(App).use(router).use(pinia).mount('#app')

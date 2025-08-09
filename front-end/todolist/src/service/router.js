import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from './storeAuth'
import { useGetUser } from './storeUser'

const getCurrentUser = async (to, from, next) => {
  await useGetUser().userCurrent()
  next()
}

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: {
      guest: true,
    },
  },

  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue'),
    meta: {
      guest: true,
    },
  },

  {
    path: '/',
    name: 'home',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'todolist',
        components: {
          navbar: () => import('@/components/Navbar.vue'),
          default: () => import('@/views/Todolist.vue'),
        },
        beforeEnter: getCurrentUser,
      },

      {
        path: '/profile/:user',
        name: 'user',
        components: {
          navbar: () => import('@/components/Navbar.vue'),
          default: () => import('@/views/User.vue'),
        },
        beforeEnter: getCurrentUser,
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const auth = useAuth()

  if (to.meta.guest && auth.isAuth) {
    next({ name: 'todolist' })
  } else if (to.meta.requiresAuth && !auth.isAuth) {
    next({ name: 'login' })
  } else {
    next()
  }
})

export default router

import { toTypedSchema } from '@vee-validate/yup'
import { defineStore } from 'pinia'
import { useField } from 'vee-validate'
import { object, string } from 'yup'

export const useValidationLogin = defineStore('storeValidationLogin', () => {
  const schema = object({
    email: string().required().email().typeError('Email is required'),
    password: string().max(5).required().typeError('Password is required'),
  })

  const { errors, value } = useField({
    validationSchema: toTypedSchema(schema),
  })

  return { value, errors }
})

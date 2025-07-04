import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // Initialize state from localStorage if available
  const isLoggedIn = ref(localStorage.getItem('auth_isLoggedIn') === 'true')
  const userType = ref(localStorage.getItem('auth_userType') || null)
  const userInfo = ref(JSON.parse(localStorage.getItem('auth_userInfo') || 'null'))

  const login = (userData) => {
    isLoggedIn.value = true
    userType.value = userData.type
    userInfo.value = {
      name: userData.name,
      email: userData.email,
    }

    // Persist to localStorage
    localStorage.setItem('auth_isLoggedIn', 'true')
    localStorage.setItem('auth_userType', userData.type)
    localStorage.setItem('auth_userInfo', JSON.stringify(userInfo.value))
  }

  const logout = () => {
    isLoggedIn.value = false
    userType.value = null
    userInfo.value = null

    // Clear localStorage
    localStorage.removeItem('auth_isLoggedIn')
    localStorage.removeItem('auth_userType')
    localStorage.removeItem('auth_userInfo')
  }

  const isUser = () => {
    return isLoggedIn.value && userType.value === 'user'
  }

  const isLawyer = () => {
    return isLoggedIn.value && userType.value === 'lawyer'
  }

  return {
    isLoggedIn,
    userType,
    userInfo,
    login,
    logout,
    isUser,
    isLawyer,
  }
})

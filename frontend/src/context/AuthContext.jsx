import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi } from '../services/api'
import toast from 'react-hot-toast'

const AuthContext = createContext(null)

const STORAGE_KEY = 'user'

function readStored() {
  let raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) raw = sessionStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    const user = JSON.parse(raw)
    // Reject corrupt / incomplete sessions (missing token or username) so a
    // stale "user" object can never make the app appear logged in and trap the
    // user in a redirect loop instead of showing the login form.
    if (!user || typeof user !== 'object' || !user.token || !user.username) {
      localStorage.removeItem(STORAGE_KEY)
      sessionStorage.removeItem(STORAGE_KEY)
      return null
    }
    return { ...user, persist: Boolean(localStorage.getItem(STORAGE_KEY)) }
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    sessionStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setUser(readStored())
    setLoading(false)
  }, [])

  const persistUser = (data, persist) => {
    const payload = { ...data, persist }
    const storage = persist ? localStorage : sessionStorage
    storage.setItem(STORAGE_KEY, JSON.stringify(payload))
    if (persist) sessionStorage.removeItem(STORAGE_KEY)
    else localStorage.removeItem(STORAGE_KEY)
    setUser(payload)
    return payload
  }

  const login = async (credentials) => {
    // "rememberMe" is UI-only state; the backend LoginRequest only accepts
    // username/password. Never send UI flags to the API.
    const { rememberMe, ...apiCredentials } = credentials
    const res = await authApi.login(apiCredentials)
    const data = res.data
    const persist = Boolean(rememberMe)
    const payload = persistUser(data, persist)
    toast.success(`Welcome, ${payload.username}`)
    return payload
  }

  const demoLogin = async (role = 'user') => {
    const creds = role === 'admin'
      ? { username: 'admin', password: 'admin123' }
      : { username: 'user', password: 'user123' }
    return login({ ...creds, rememberMe: false })
  }

  const register = async (data) => {
    await authApi.register(data)
    toast.success('Registration successful. Please login.')
  }

  const logout = async () => {
    const refreshToken = user?.refreshToken
    if (refreshToken) {
      try { await authApi.logout(refreshToken) } catch { /* ignore */ }
    }
    localStorage.removeItem(STORAGE_KEY)
    sessionStorage.removeItem(STORAGE_KEY)
    setUser(null)
    toast.success('Logged out')
  }

  const hasPermission = useCallback((permission) => {
    if (!user) return false
    if (Array.isArray(user.permissions)) return user.permissions.includes(permission)
    return false
  }, [user])

  const hasAnyPermission = useCallback((...permissions) => {
    if (!user) return false
    if (Array.isArray(user.permissions)) {
      return permissions.some(p => user.permissions.includes(p))
    }
    return false
  }, [user])

  return (
    <AuthContext.Provider value={{
      user, loading, login, register, logout, demoLogin,
      hasPermission, hasAnyPermission
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)

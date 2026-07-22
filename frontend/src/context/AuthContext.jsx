import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import apiClient, { TOKEN_KEY } from '../api/axios.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    setUser(null)
  }, [])

  const refreshUser = useCallback(async () => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) {
      setUser(null)
      return null
    }
    const response = await apiClient.get('/users/me')
    setUser(response.data)
    return response.data
  }, [])

  useEffect(() => {
    const initialize = async () => {
      try {
        await refreshUser()
      } catch {
        logout()
      } finally {
        setLoading(false)
      }
    }
    initialize()
  }, [logout, refreshUser])

  useEffect(() => {
    window.addEventListener('cloudshare:unauthorized', logout)
    return () => window.removeEventListener('cloudshare:unauthorized', logout)
  }, [logout])

  const authenticate = useCallback((authResponse) => {
    localStorage.setItem(TOKEN_KEY, authResponse.token)
    setUser(authResponse.user)
  }, [])

  const value = useMemo(() => ({
    user,
    loading,
    authenticate,
    logout,
    refreshUser,
  }), [user, loading, authenticate, logout, refreshUser])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return context
}

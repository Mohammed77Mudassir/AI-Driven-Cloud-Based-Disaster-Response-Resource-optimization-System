import { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react'
import { ThemeProvider } from '@mui/material/styles'
import { lightTheme, darkTheme } from '../theme'

const ThemeContext = createContext(null)

const STORAGE_KEY = 'dms-theme-mode'

export function ThemeModeProvider({ children }) {
  const [mode, setMode] = useState(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'light' || stored === 'dark') return stored
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) return 'dark'
    return 'light'
  })

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, mode)
    document.documentElement.dataset.theme = mode
  }, [mode])

  const toggleMode = useCallback(() => {
    setMode(prev => (prev === 'light' ? 'dark' : 'light'))
  }, [])

  const theme = useMemo(() => (mode === 'dark' ? darkTheme : lightTheme), [mode])

  const value = useMemo(() => ({ mode, toggleMode, isDark: mode === 'dark' }), [mode, toggleMode])

  return (
    <ThemeContext.Provider value={value}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </ThemeContext.Provider>
  )
}

export const useThemeMode = () => useContext(ThemeContext)

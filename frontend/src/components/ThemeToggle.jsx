import { IconButton, Tooltip } from '@mui/material'
import LightModeIcon from '@mui/icons-material/LightMode'
import DarkModeIcon from '@mui/icons-material/DarkMode'
import { useThemeMode } from '../context/ThemeContext'

export default function ThemeToggle({ size = 'small' }) {
  const { mode, toggleMode } = useThemeMode()
  const isDark = mode === 'dark'
  return (
    <Tooltip title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}>
      <IconButton
        onClick={toggleMode}
        size={size}
        aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
        sx={{
          border: '1px solid',
          borderColor: 'divider',
          color: 'text.secondary',
          transition: 'color 200ms ease, background-color 200ms ease',
          '&:hover': { color: 'primary.main', bgcolor: 'action.hover' },
        }}
      >
        {isDark ? <LightModeIcon sx={{ fontSize: 20 }} /> : <DarkModeIcon sx={{ fontSize: 20 }} />}
      </IconButton>
    </Tooltip>
  )
}

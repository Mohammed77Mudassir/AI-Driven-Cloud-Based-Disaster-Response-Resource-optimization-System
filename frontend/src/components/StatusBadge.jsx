import { Chip, useTheme } from '@mui/material'
import CircleIcon from '@mui/icons-material/Circle'

const STATUS_COLORS = {
  ACTIVE: 'success',
  AVAILABLE: 'success',
  CONNECTED: 'success',
  LIVE: 'success',
  RESOLVED: 'success',
  COMPLETED: 'success',
  READY: 'success',
  OPERATIONAL: 'success',
  OPEN: 'success',
  PENDING: 'warning',
  SCHEDULED: 'info',
  ASSIGNED: 'info',
  IN_PROGRESS: 'warning',
  PROGRESS: 'info',
  High: 'warning',
  Medium: 'info',
  Warning: 'warning',
  FAILED: 'error',
  ERROR: 'error',
  OFFLINE: 'error',
  DISCONNECTED: 'error',
  CANCELLED: 'error',
  CRITICAL: 'error',
  LOW: 'error',
  IN_MAINTENANCE: 'warning',
  MAINTENANCE: 'warning',
  DEPLOYED: 'info',
  ON_MISSION: 'info',
  RETURNED: 'success',
  RESTOCKING: 'warning',
  SHIFT_ACTIVE: 'success',
  SHIFT_COMPLETED: 'default',
  ARCHIVED: 'default',
  Unknown: 'default',
}

export default function StatusBadge({ status, showDot = true, size = 'small', sx = {} }) {
  const theme = useTheme()
  const key = String(status || 'Unknown').toUpperCase()
  const color = STATUS_COLORS[key] || 'default'

  if (color === 'default') {
    return (
      <Chip
        size={size}
        label={status}
        sx={{
          fontWeight: 700,
          fontSize: '0.72rem',
          bgcolor: theme.palette.action.hover,
          color: theme.palette.text.secondary,
          ...sx,
        }}
      />
    )
  }

  return (
    <Chip
      size={size}
      icon={showDot ? <CircleIcon sx={{ fontSize: 10, color: `${color}.main` }} /> : undefined}
      label={status}
      color={color}
      variant="soft"
      sx={{ fontWeight: 700, fontSize: '0.72rem', ...sx }}
    />
  )
}

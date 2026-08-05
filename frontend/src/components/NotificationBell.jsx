import { useState, useEffect, useRef } from 'react'
import {
  Box, Badge, IconButton, Popover, List, ListItem, ListItemText, Typography,
  Button, Divider, Chip, Tooltip, useTheme
} from '@mui/material'
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone'
import MarkEmailReadIcon from '@mui/icons-material/MarkEmailRead'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import InfoIcon from '@mui/icons-material/Info'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import { notificationApi } from '../services/api'

const TYPE_ICON = {
  WARNING: <WarningAmberIcon sx={{ fontSize: 20, color: 'warning.main' }} />,
  ALERT: <WarningAmberIcon sx={{ fontSize: 20, color: 'error.main' }} />,
  INFO: <InfoIcon sx={{ fontSize: 20, color: 'info.main' }} />,
  SUCCESS: <CheckCircleIcon sx={{ fontSize: 20, color: 'success.main' }} />,
}

export default function NotificationBell() {
  const [unreadCount, setUnreadCount] = useState(0)
  const [notifications, setNotifications] = useState([])
  const [anchorEl, setAnchorEl] = useState(null)
  const theme = useTheme()
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true
    loadUnreadCount()
    const interval = setInterval(loadUnreadCount, 30000)
    return () => {
      mountedRef.current = false
      clearInterval(interval)
    }
  }, [])

  const loadUnreadCount = async () => {
    try {
      const res = await notificationApi.getUnreadCount()
      if (mountedRef.current) setUnreadCount(res.data.count)
    } catch { /* ignore */ }
  }

  const toggleDropdown = async (e) => {
    setAnchorEl(e.currentTarget)
    if (!anchorEl) {
      try {
        const res = await notificationApi.getUnread()
        if (mountedRef.current) setNotifications(res.data)
      } catch { /* ignore */ }
    }
  }

  const handleMarkAsRead = async (id) => {
    try {
      await notificationApi.markAsRead(id)
      if (!mountedRef.current) return
      loadUnreadCount()
      setNotifications(prev => prev.filter(n => n.id !== id))
    } catch { /* ignore */ }
  }

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllAsRead()
      setUnreadCount(0)
      setNotifications([])
    } catch { /* ignore */ }
  }

  return (
    <>
      <Tooltip title={`Notifications${unreadCount ? ` (${unreadCount} unread)` : ''}`}>
        <IconButton
          onClick={toggleDropdown}
          size="small"
          aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}
          sx={{ border: '1px solid', borderColor: 'divider', color: 'text.secondary', '&:hover': { color: 'primary.main', bgcolor: 'action.hover' } }}
        >
          <Badge badgeContent={unreadCount} color="error" max={99} overlap="circular">
            <NotificationsNoneIcon sx={{ fontSize: 20 }} />
          </Badge>
        </IconButton>
      </Tooltip>

      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        slotProps={{ paper: { sx: { width: 340, maxHeight: 440, mt: 1, overflow: 'hidden', borderRadius: 3 } } }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', px: 2, py: 1.5, bgcolor: 'action.hover' }}>
          <Typography variant="h6" sx={{ fontSize: '0.9rem' }}>Notifications</Typography>
          {unreadCount > 0 && (
            <Button size="small" startIcon={<MarkEmailReadIcon />} onClick={handleMarkAllRead} sx={{ textTransform: 'none' }}>
              Mark all read
            </Button>
          )}
        </Box>
        <Divider />
        <List dense sx={{ maxHeight: 360, overflowY: 'auto', p: 0.5 }}>
          {notifications.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 5, px: 2 }}>
              <NotificationsNoneIcon sx={{ fontSize: 40, color: 'text.disabled', mb: 1 }} />
              <Typography variant="body2" color="text.secondary">You're all caught up</Typography>
            </Box>
          ) : notifications.map(n => (
            <ListItem
              key={n.id}
              button
              alignItems="flex-start"
              onClick={() => handleMarkAsRead(n.id)}
              sx={{ borderRadius: 2, mx: 0.5 }}
            >
              <Box sx={{ mr: 1.25, mt: 0.5 }}>{TYPE_ICON[n.type?.toUpperCase()] || <InfoIcon sx={{ fontSize: 20, color: 'info.main' }} />}</Box>
              <ListItemText
                primary={n.title}
                secondary={
                  <>
                    {n.message}
                    <Box component="span" sx={{ display: 'block', mt: 0.5 }}>
                      <Chip label={n.type || 'INFO'} size="small" variant="soft" color="default" sx={{ fontSize: '0.62rem' }} />
                      <Typography component="span" variant="caption" color="text.disabled" sx={{ ml: 1 }}>
                        {new Date(n.createdAt).toLocaleString()}
                      </Typography>
                    </Box>
                  </>
                }
                slotProps={{ primary: { sx: { fontWeight: 700, fontSize: '0.84rem' } }, secondary: { sx: { fontSize: '0.76rem', mt: 0.25 } } }}
              />
            </ListItem>
          ))}
        </List>
      </Popover>
    </>
  )
}

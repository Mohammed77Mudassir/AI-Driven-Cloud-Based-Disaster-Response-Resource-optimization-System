import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import SectionCard from '../components/SectionCard'
import { TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { notificationApi } from '../services/api'
import {
  Box, Card, CardContent, Typography, Chip, Button, Grid, IconButton, Tooltip, useTheme
} from '@mui/material'
import NotificationsIcon from '@mui/icons-material/Notifications'
import MailIcon from '@mui/icons-material/Mail'
import InfoIcon from '@mui/icons-material/Info'
import WarningIcon from '@mui/icons-material/Warning'
import ErrorIcon from '@mui/icons-material/Error'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import MarkEmailReadIcon from '@mui/icons-material/MarkEmailRead'
import DoneAllIcon from '@mui/icons-material/DoneAll'

const typeConfig = (theme) => ({
  INFO: { icon: <InfoIcon />, color: theme.palette.primary.main, bg: `${theme.palette.primary.main}1A` },
  WARNING: { icon: <WarningIcon />, color: theme.palette.warning.main, bg: `${theme.palette.warning.main}1A` },
  ALERT: { icon: <ErrorIcon />, color: theme.palette.error.main, bg: `${theme.palette.error.main}1A` },
  SUCCESS: { icon: <CheckCircleIcon />, color: theme.palette.success.main, bg: `${theme.palette.success.main}1A` },
})

export default function NotificationCenter() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const theme = useTheme()

  useEffect(() => { loadNotifications() }, [])

  const loadNotifications = async () => {
    try { const res = await notificationApi.getAll(); setNotifications(res.data) }
    catch {} finally { setLoading(false) }
  }

  const handleMarkRead = async (id) => {
    try { await notificationApi.markAsRead(id); loadNotifications() } catch {}
  }

  const handleMarkAllRead = async () => {
    try { await notificationApi.markAllAsRead(); loadNotifications() } catch {}
  }

  const unreadCount = notifications.filter(n => !n.read).length

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <TableSkeleton />
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Notification Center"
          subtitle="View all system notifications"
          actions={
            unreadCount > 0 && (
              <Button size="small" variant="outlined" startIcon={<DoneAllIcon />} onClick={handleMarkAllRead}>
                Mark All Read
              </Button>
            )
          }
        />

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, mb: 3 }}>
          <StatCard icon={<NotificationsIcon />} value={notifications.length} label="Total" color="primary.main" />
          <StatCard icon={<MailIcon />} value={unreadCount} label="Unread" color="warning.main" />
        </Box>

        <SectionCard
          title="Notifications"
          subtitle={`${notifications.length} total · ${unreadCount} unread`}
        >
          {notifications.length === 0 ? (
            <EmptyState title="No notifications" message="You're all caught up!" icon={<NotificationsIcon sx={{ fontSize: 64, color: 'text.disabled' }} />} />
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              {notifications.map(n => {
                const cfg = typeConfig(theme)[n.type] || typeConfig(theme).INFO
                return (
                  <Card key={n.id} sx={{
                    opacity: n.read ? 0.7 : 1,
                    borderLeft: `4px solid ${cfg.color}`,
                    bgcolor: n.read ? 'transparent' : cfg.bg
                  }}>
                    <CardContent sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, py: 2, '&:last-child': { pb: 2 } }}>
                      <Box sx={{ color: cfg.color, mt: 0.3 }}>{cfg.icon}</Box>
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 0.5 }}>
                          <Typography variant="subtitle2" sx={{ fontWeight: n.read ? 500 : 700 }}>
                            {n.title}
                          </Typography>
                          <Chip label={n.type} size="small" sx={{
                            fontWeight: 600, fontSize: '0.65rem',
                            backgroundColor: cfg.bg, color: cfg.color
                          }} />
                        </Box>
                        <Typography variant="body2" color="text.secondary" sx={{
                          mb: 0.5, overflow: 'hidden', textOverflow: 'ellipsis',
                          display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical'
                        }}>
                          {n.message}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(n.createdAt).toLocaleString()}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 0.5, minWidth: 60 }}>
                        <Typography variant="caption" sx={{ color: n.read ? theme.palette.success.main : theme.palette.warning.main, fontWeight: 600 }}>
                          {n.read ? 'Read' : 'Unread'}
                        </Typography>
                        {!n.read && (
                          <Tooltip title="Mark as read">
                            <IconButton size="small" onClick={() => handleMarkRead(n.id)} sx={{ color: theme.palette.primary.main }}>
                              <MarkEmailReadIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </CardContent>
                  </Card>
                )
              })}
            </Box>
          )}
        </SectionCard>
        <Footer />
      </Box>
    </Box>
  )
}

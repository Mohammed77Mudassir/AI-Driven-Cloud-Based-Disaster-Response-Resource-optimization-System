import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Card, CardContent, Grid, Button, TextField, Chip, Alert,
  List, ListItem, ListItemText, ListItemIcon, IconButton, Tooltip,
  Paper, Stack, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow
} from '@mui/material'
import {
  Wifi as WifiIcon, RocketLaunch as RocketLaunchIcon,
  WarningAmber as WarningAmberIcon, Inventory2 as Inventory2Icon,
  History as HistoryIcon,
  Refresh as RefreshIcon, Download as DownloadIcon, Security as SecurityIcon,
  Settings as SettingsIcon, SmartToy as SmartToyIcon, Analytics as AnalyticsIcon,
  Notifications as NotificationsIcon, Edit as EditIcon, Check as CheckIcon,
  Close as CloseIcon, People as PeopleIcon
} from '@mui/icons-material'
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip as ChartTooltip, Legend,
  PieChart, Pie, Cell, AreaChart, Area
} from 'recharts'
import { adminApi, exportApi } from '../services/api'
import Sidebar from '../components/Sidebar'
import Breadcrumbs from '../components/Breadcrumbs'
import Footer from '../components/Footer'
import EmptyState from '../components/EmptyState'
import WebSocketStatus from '../components/WebSocketStatus'
import AnimatedCounter from '../components/AnimatedCounter'
import { StatsSkeleton, ChartSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import { useWebSocket } from '../context/WebSocketContext'
import toast from 'react-hot-toast'

const CHART_COLORS = ['#0F4C81', '#00897B', '#F57C00', '#4d96ff', '#6bcb77', '#ff6b6b', '#ffd93d', '#ce93d8']
const STATUS_COLORS = {
  PENDING: '#ffd93d', VERIFIED: '#4d96ff', ASSIGNED: '#4d96ff',
  RESOURCES_DISPATCHED: '#F57C00', IN_PROGRESS: '#F57C00', RESOLVED: '#6bcb77'
}

const SECTIONS = [
  { id: 'overview', label: 'Overview' },
  { id: 'statistics', label: 'Statistics' },
  { id: 'activity', label: 'Activity' },
  { id: 'notifications', label: 'Notifications' },
  { id: 'settings', label: 'Settings' },
]

const toChartEntries = (map) =>
  map ? Object.entries(map).map(([name, value]) => ({ name, value })) : []

const ChartCard = ({ title, children }) => (
  <Card sx={{ height: '100%' }}>
    <CardContent>
      <Typography variant="h6" gutterBottom>{title}</Typography>
      {children}
    </CardContent>
  </Card>
)

export default function AdminControlCenter() {
  const navigate = useNavigate()
  const { connected } = useWebSocket()

  const [dashboard, setDashboard] = useState(null)
  const [notificationsData, setNotificationsData] = useState({ unread: 0, notifications: [] })
  const [settings, setSettings] = useState([])
  const [loading, setLoading] = useState(true)
  const [lastRefresh, setLastRefresh] = useState(new Date())
  const [editingSetting, setEditingSetting] = useState(null)
  const [editValue, setEditValue] = useState('')
  const [activeSection, setActiveSection] = useState('overview')

  const loadAll = useCallback(async () => {
    setLoading(true)
    try {
      const [dashRes, notifRes, settingsRes] = await Promise.all([
        adminApi.getDashboard().catch(() => null),
        adminApi.getNotifications().catch(() => null),
        adminApi.getSettings().catch(() => null)
      ])
      setDashboard(dashRes?.data || null)
      setNotificationsData(notifRes?.data || { unread: 0, notifications: [] })
      if (Array.isArray(settingsRes?.data)) setSettings(settingsRes.data)
      setLastRefresh(new Date())
    } catch {
      toast.error('Failed to load admin data')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadAll() }, [loadAll])

  useEffect(() => {
    const onScroll = () => {
      let current = SECTIONS[0].id
      for (const section of SECTIONS) {
        const el = document.getElementById(section.id)
        if (el && el.getBoundingClientRect().top <= 140) current = section.id
      }
      setActiveSection(current)
    }
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  const scrollToSection = (id) => {
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  const handleExport = async (format) => {
    try {
      const res = await exportApi[format]()
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `export.${format}`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
      toast.success(`${format.toUpperCase()} exported`)
    } catch {
      toast.error('Export failed')
    }
  }

  const handleSaveSetting = async (key) => {
    try {
      await adminApi.updateSetting(key, editValue)
      setSettings(prev => prev.map(s => (s.settingKey === key ? { ...s, settingValue: editValue } : s)))
      setEditingSetting(null)
      toast.success('Setting updated')
    } catch {
      toast.error('Failed to update setting')
    }
  }

  const handleStartEdit = (key, value) => {
    setEditingSetting(key)
    setEditValue(value || '')
  }

  const kpis = [
    { label: 'Connected Users', icon: <WifiIcon />, color: '#4d96ff', value: dashboard?.connectedUsers ?? 0, sub: 'Live WebSocket sessions' },
    { label: 'Active Missions', icon: <RocketLaunchIcon />, color: '#F57C00', value: dashboard?.activeMissions ?? 0, sub: 'Deployed & in progress' },
    { label: 'Pending Disasters', icon: <WarningAmberIcon />, color: '#ffd93d', value: dashboard?.pendingDisasters ?? 0, sub: 'Awaiting triage' },
    { label: 'Resources Available', icon: <Inventory2Icon />, color: '#00897B', value: dashboard?.resourcesAvailable ?? 0, sub: `${dashboard?.resourceUtilizationPercent ?? 0}% utilized` },
  ]

  const statusData = toChartEntries(dashboard?.disastersByStatus)
  const severityData = toChartEntries(dashboard?.disastersBySeverity)
  const monthData = toChartEntries(dashboard?.disastersByMonth)
  const roleData = toChartEntries(dashboard?.usersByRole)
  const resourceDonut = [
    { name: 'Available', value: dashboard?.resourcesAvailable ?? 0 },
    { name: 'Deployed', value: dashboard?.resourcesDeployed ?? 0 },
    { name: 'In Maintenance', value: dashboard?.resourcesInMaintenance ?? 0 },
  ].filter(d => d.value > 0)

  const recentActivities = dashboard?.recentActivities || []
  const notifications = notificationsData.notifications || []
  const unreadCount = notificationsData.unread || 0

  const quickActions = [
    { label: 'Export CSV', icon: <DownloadIcon />, onClick: () => handleExport('csv') },
    { label: 'Export PDF', icon: <DownloadIcon />, onClick: () => handleExport('pdf') },
    { label: 'Export Excel', icon: <DownloadIcon />, onClick: () => handleExport('excel') },
    { label: 'Audit Logs', icon: <SecurityIcon />, onClick: () => navigate('/audit-logs') },
    { label: 'User Management', icon: <PeopleIcon />, onClick: () => navigate('/users') },
    { label: 'System Settings', icon: <SettingsIcon />, onClick: () => scrollToSection('settings') },
    { label: 'AI Insights', icon: <SmartToyIcon />, onClick: () => navigate('/ai-insights') },
    { label: 'Analytics', icon: <AnalyticsIcon />, onClick: () => navigate('/analytics') },
  ]

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flexGrow: 1, p: 3, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <StatsSkeleton />
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mt: 2 }}>
          <ChartSkeleton /><ChartSkeleton /><ChartSkeleton /><ChartSkeleton />
        </Box>
        <TableSkeleton rows={4} />
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flexGrow: 1, p: 3, minHeight: '100vh', backgroundColor: 'background.default' }}>
        <Breadcrumbs />

        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2, flexWrap: 'wrap', gap: 2 }}>
          <Box>
            <Typography variant="h4" fontWeight={700}>Emergency Operations Center</Typography>
            <Typography variant="body2" color="text.secondary">System administration and configuration hub</Typography>
          </Box>
          <Stack direction="row" spacing={1.5} alignItems="center" sx={{ flexWrap: 'wrap' }}>
            <WebSocketStatus connected={connected} />
            <Typography variant="caption" color="text.secondary">
              Last refresh: {lastRefresh.toLocaleTimeString()}
            </Typography>
            <Button size="small" variant="contained" startIcon={<RefreshIcon />} onClick={loadAll} disabled={loading}>
              Refresh
            </Button>
          </Stack>
        </Box>

        <Stack direction="row" spacing={1} sx={{ mb: 3, flexWrap: 'wrap', gap: 1 }}>
          {SECTIONS.map(section => (
            <Chip
              key={section.id} label={section.label} clickable
              onClick={() => scrollToSection(section.id)}
              color={activeSection === section.id ? 'primary' : 'default'}
              variant={activeSection === section.id ? 'filled' : 'outlined'}
            />
          ))}
        </Stack>

        {/* Section A - KPI stat cards */}
        <Grid container spacing={2} id="overview" sx={{ scrollMarginTop: 16, mb: 3 }}>
          {kpis.map((k, i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4, lg: 4 }} key={i}>
              <Card>
                <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 2.5 }}>
                  <Box sx={{
                    width: 52, height: 52, borderRadius: 2, display: 'flex', alignItems: 'center',
                    justifyContent: 'center', backgroundColor: `${k.color}15`, color: k.color, flexShrink: 0
                  }}>
                    {k.icon}
                  </Box>
                  <Box sx={{ minWidth: 0 }}>
                    {k.value !== undefined ? (
                      <AnimatedCounter value={k.value} sx={{ fontWeight: 700, lineHeight: 1.2 }} />
                    ) : (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Box sx={{
                          width: 12, height: 12, borderRadius: '50%',
                          backgroundColor: k.color, boxShadow: `0 0 6px ${k.color}`
                        }} />
                        <Typography variant="h6" sx={{ fontWeight: 700, lineHeight: 1.2 }}>{k.valueText}</Typography>
                      </Box>
                    )}
                    <Typography variant="body2" color="text.secondary">{k.label}</Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{k.sub}</Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Section C - Charts */}
        <Box id="statistics" sx={{ scrollMarginTop: 16, mb: 3 }}>
          <Typography variant="h5" sx={{ mb: 2 }}>Statistics</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6, lg: 4 }}>
              <ChartCard title="Disasters by Status">
                <ResponsiveContainer width="100%" height={260}>
                  <PieChart>
                    <Pie data={statusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={85} label>
                      {statusData.map((_, i) => (
                        <Cell key={i} fill={STATUS_COLORS[statusData[i].name] || CHART_COLORS[i % CHART_COLORS.length]} />
                      ))}
                    </Pie>
                    <ChartTooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </ChartCard>
            </Grid>
            <Grid size={{ xs: 12, md: 6, lg: 4 }}>
              <ChartCard title="Disasters by Severity">
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={severityData}>
                    <XAxis dataKey="name" />
                    <YAxis allowDecimals={false} />
                    <ChartTooltip />
                    <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                      {severityData.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </ChartCard>
            </Grid>
            <Grid size={{ xs: 12, md: 6, lg: 4 }}>
              <ChartCard title="Disasters by Month">
                <ResponsiveContainer width="100%" height={260}>
                  <AreaChart data={monthData}>
                    <defs>
                      <linearGradient id="monthGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#0F4C81" stopOpacity={0.8} />
                        <stop offset="95%" stopColor="#0F4C81" stopOpacity={0.05} />
                      </linearGradient>
                    </defs>
                    <XAxis dataKey="name" />
                    <YAxis allowDecimals={false} />
                    <ChartTooltip />
                    <Area type="monotone" dataKey="value" stroke="#0F4C81" fill="url(#monthGrad)" strokeWidth={2} />
                  </AreaChart>
                </ResponsiveContainer>
              </ChartCard>
            </Grid>
            <Grid size={{ xs: 12, md: 6, lg: 6 }}>
              <ChartCard title="Users by Role">
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={roleData}>
                    <XAxis dataKey="name" />
                    <YAxis allowDecimals={false} />
                    <ChartTooltip />
                    <Legend />
                    <Bar dataKey="value" fill="#00897B" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </ChartCard>
            </Grid>
            <Grid size={{ xs: 12, md: 6, lg: 6 }}>
              <ChartCard title="Resource Utilization">
                <ResponsiveContainer width="100%" height={260}>
                  <PieChart>
                    <Pie
                      data={resourceDonut} dataKey="value" nameKey="name" cx="50%" cy="50%"
                      innerRadius={50} outerRadius={85} paddingAngle={3}
                    >
                      {resourceDonut.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                    </Pie>
                    <ChartTooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </ChartCard>
            </Grid>
          </Grid>
        </Box>

        {/* Section D - Recent activities + notifications */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid size={{ xs: 12, lg: 8 }}>
            <Card id="activity" sx={{ scrollMarginTop: 16, height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <HistoryIcon color="primary" />
                  <Typography variant="h6">Recent Activities</Typography>
                </Box>
                <Box sx={{ overflowX: 'auto' }}>
                  <TableContainer component={Paper} variant="outlined" sx={{ boxShadow: 'none' }}>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Action</TableCell>
                          <TableCell>Entity</TableCell>
                          <TableCell>User</TableCell>
                          <TableCell>Time</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {recentActivities.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={4} sx={{ py: 3 }}>
                              <EmptyState title="No recent activity" message="Audit events will appear here." />
                            </TableCell>
                          </TableRow>
                        ) : recentActivities.map(a => (
                          <TableRow key={a.id} hover>
                            <TableCell>
                              <Chip label={a.action} size="small" sx={{ fontWeight: 600, fontSize: '0.7rem' }} />
                            </TableCell>
                            <TableCell>
                              {a.entityType}{a.entityId ? ` #${a.entityId}` : ''}
                            </TableCell>
                            <TableCell>{a.performedBy || '—'}</TableCell>
                            <TableCell>{a.timestamp ? new Date(a.timestamp).toLocaleString() : '—'}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, lg: 4 }}>
            <Card id="notifications" sx={{ scrollMarginTop: 16, height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <NotificationsIcon color="primary" />
                  <Typography variant="h6">Notifications</Typography>
                  <Chip
                    label={`${unreadCount} unread`} size="small"
                    color={unreadCount > 0 ? 'error' : 'default'}
                    sx={{ ml: 'auto', fontWeight: 700, fontSize: '0.7rem' }}
                  />
                </Box>
                {notifications.length === 0 ? (
                  <Alert severity="info">No notifications</Alert>
                ) : (
                  <List dense disablePadding>
                    {notifications.map(n => (
                      <ListItem
                        key={n.id} alignItems="flex-start" disablePadding
                        sx={{
                          py: 1, opacity: n.read ? 0.7 : 1,
                          borderBottom: '1px solid', borderColor: 'divider'
                        }}
                      >
                        <ListItemIcon sx={{ minWidth: 30, color: n.read ? 'text.secondary' : '#F57C00' }}>
                          <NotificationsIcon fontSize="small" />
                        </ListItemIcon>
                        <ListItemText
                          primary={n.title}
                          secondary={
                            <Box component="span">
                              {n.message}
                              <Box component="span" sx={{ display: 'block', mt: 0.3 }}>
                                {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                              </Box>
                            </Box>
                          }
                          primaryTypographyProps={{ fontWeight: n.read ? 400 : 700, fontSize: '0.85rem' }}
                          secondaryTypographyProps={{ fontSize: '0.75rem' }}
                        />
                        {!n.read && <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#F57C00', mt: 1.2, flexShrink: 0 }} />}
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Section E - Quick actions */}
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>Quick Actions</Typography>
            <Grid container spacing={2}>
              {quickActions.map((action, i) => (
                <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={i}>
                  <Button
                    variant="outlined" fullWidth startIcon={action.icon}
                    onClick={action.onClick} sx={{ py: 1.5, justifyContent: 'flex-start' }}
                  >
                    {action.label}
                  </Button>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </Card>

        {/* Section F - System settings editor */}
        <Card id="settings" sx={{ scrollMarginTop: 16 }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
              <SettingsIcon /> System Settings
            </Typography>
            {settings.length === 0 ? (
              <Alert severity="info">No settings available</Alert>
            ) : (
              <Grid container spacing={1}>
                {settings.map(s => (
                  <Grid size={{ xs: 12, sm: 6, md: 4 }} key={s.id ?? s.settingKey}>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 1, border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'uppercase', fontWeight: 600 }}>
                          {String(s.settingKey).replace(/([A-Z])/g, ' $1').trim()}
                        </Typography>
                        {s.description && (
                          <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                            {s.description}
                          </Typography>
                        )}
                        {editingSetting === s.settingKey ? (
                          <TextField
                            size="small" fullWidth value={editValue}
                            onChange={e => setEditValue(e.target.value)}
                            sx={{ mt: 0.5 }}
                          />
                        ) : (
                          <Typography variant="body2" sx={{ fontWeight: 500, wordBreak: 'break-word' }}>
                            {String(s.settingValue ?? '')}
                          </Typography>
                        )}
                      </Box>
                      <Box sx={{ ml: 1 }}>
                        {editingSetting === s.settingKey ? (
                          <Box sx={{ display: 'flex', gap: 0.5 }}>
                            <Tooltip title="Save">
                              <IconButton size="small" color="primary" onClick={() => handleSaveSetting(s.settingKey)}>
                                <CheckIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Cancel">
                              <IconButton size="small" onClick={() => setEditingSetting(null)}>
                                <CloseIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </Box>
                        ) : (
                          <Tooltip title="Edit">
                            <IconButton size="small" onClick={() => handleStartEdit(s.settingKey, s.settingValue)}>
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            )}
          </CardContent>
        </Card>

        <Footer />
      </Box>
    </Box>
  )
}

import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Card, CardContent, Typography, Grid, Chip, LinearProgress, Button, IconButton,
  Tooltip, TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Alert, Divider, Avatar, List, ListItem, ListItemAvatar, ListItemText,
} from '@mui/material'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ChartTooltip, ResponsiveContainer,
  PieChart, Pie, Cell, AreaChart, Area, Legend,
} from 'recharts'
import toast from 'react-hot-toast'
import Sidebar from '../components/Sidebar'
import Breadcrumbs from '../components/Breadcrumbs'
import Footer from '../components/Footer'
import AnimatedCounter from '../components/AnimatedCounter'
import WebSocketStatus from '../components/WebSocketStatus'
import { StatsSkeleton, ChartSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { useWebSocket } from '../context/WebSocketContext'
import {
  adminApi, analyticsApi, monitoringApi, missionApi, auditLogApi, exportApi,
} from '../services/api'
import RefreshIcon from '@mui/icons-material/Refresh'
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch'
import WifiIcon from '@mui/icons-material/Wifi'
import WavesIcon from '@mui/icons-material/Waves'
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import InventoryIcon from '@mui/icons-material/Inventory'
import GroupsIcon from '@mui/icons-material/Groups'
import NotificationsIcon from '@mui/icons-material/Notifications'
import ReportIcon from '@mui/icons-material/Report'
import MapIcon from '@mui/icons-material/Map'
import DescriptionIcon from '@mui/icons-material/Description'
import DownloadIcon from '@mui/icons-material/Download'
import SettingsIcon from '@mui/icons-material/Settings'
import WifiTetheringIcon from '@mui/icons-material/WifiTethering'
import EditIcon from '@mui/icons-material/Edit'
import CheckIcon from '@mui/icons-material/Check'
import CloseIcon from '@mui/icons-material/Close'
import HistoryIcon from '@mui/icons-material/History'
import FlightIcon from '@mui/icons-material/Flight'
import SecurityIcon from '@mui/icons-material/Security'
import BarChartIcon from '@mui/icons-material/BarChart'

const CHART_COLORS = ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#ff8a65', '#ce93d8', '#26a69a', '#ff8800']
const RESOURCE_COLORS = { Available: '#6bcb77', Deployed: '#4d96ff', Maintenance: '#F57C00' }
const MISSION_COLORS = {
  PENDING: '#F9A825', ASSIGNED: '#4d96ff', IN_PROGRESS: '#ff8800',
  COMPLETED: '#2E7D32', CANCELLED: '#C62828',
}
const ACTIVE_MISSION_STATUSES = ['PENDING', 'ASSIGNED', 'IN_PROGRESS']

const mapToArray = (obj) => (obj ? Object.entries(obj).map(([name, value]) => ({ name, value })) : [])

function LiveClock() {
  const [now, setNow] = useState(new Date())
  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(t)
  }, [])
  return (
    <Typography variant="body2" sx={{ fontFamily: 'monospace', fontWeight: 600, color: 'text.secondary' }}>
      {now.toLocaleString()}
    </Typography>
  )
}

function KpiCard({ icon, value, label, color }) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 2 }}>
        <Box sx={{
          width: 48, height: 48, borderRadius: 2, display: 'flex', alignItems: 'center',
          justifyContent: 'center', backgroundColor: `${color}15`, color,
        }}>
          {icon}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <AnimatedCounter value={value} variant="h4" sx={{ fontWeight: 700, lineHeight: 1.2 }} />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.78rem' }}>{label}</Typography>
        </Box>
      </CardContent>
    </Card>
  )
}

function ChartCard({ title, subtitle, children }) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Typography variant="h6" sx={{ fontSize: '0.95rem' }}>{title}</Typography>
        {subtitle && <Typography variant="caption" color="text.secondary">{subtitle}</Typography>}
        <Box sx={{ mt: 1.5 }}>{children}</Box>
      </CardContent>
    </Card>
  )
}

export default function EmergencyOperationsCenter() {
  const navigate = useNavigate()
  const { connected, status, reconnectAttempt, subscribe } = useWebSocket()

  const [dash, setDash] = useState(null)
  const [analytics, setAnalytics] = useState(null)
  const [overview, setOverview] = useState(null)
  const [missions, setMissions] = useState([])
  const [auditLogs, setAuditLogs] = useState([])
  const [notifData, setNotifData] = useState({ unread: 0, notifications: [] })
  const [settings, setSettings] = useState([])
  const [loading, setLoading] = useState(true)
  const [lastLiveUpdate, setLastLiveUpdate] = useState(null)
  const [lastRefreshed, setLastRefreshed] = useState(null)

  const [editingSetting, setEditingSetting] = useState(null)
  const [editValue, setEditValue] = useState('')
  const [auditFilter, setAuditFilter] = useState('')
  const [missionFilter, setMissionFilter] = useState('ALL')
  const pollingRef = useRef(null)

  const loadAll = useCallback(async (opts = {}) => {
    if (!opts.silent) setLoading(true)
    try {
      const [dashRes, analyticsRes, overviewRes, missionsRes, auditRes, notifRes, settingsRes] =
        await Promise.all([
          adminApi.getDashboard().catch(() => null),
          analyticsApi.get().catch(() => null),
          monitoringApi.overview().catch(() => null),
          missionApi.getAll().catch(() => null),
          auditLogApi.getAll().catch(() => null),
          adminApi.getNotifications().catch(() => null),
          adminApi.getSettings().catch(() => null),
        ])
      if (dashRes?.data) setDash(dashRes.data)
      if (analyticsRes?.data) setAnalytics(analyticsRes.data)
      if (overviewRes?.data) setOverview(overviewRes.data)
      if (missionsRes?.data) setMissions(missionsRes.data)
      if (auditRes?.data) setAuditLogs(auditRes.data)
      if (notifRes?.data) setNotifData(notifRes.data)
      if (settingsRes?.data) setSettings(Array.isArray(settingsRes.data) ? settingsRes.data : [])
      setLastRefreshed(new Date())
    } catch {
      toast.error('Failed to load EOC data')
    } finally {
      setLoading(false)
    }
  }, [])

  // Lighter reload path for mission lifecycle events — avoids re-fetching the
  // full 8-endpoint snapshot when only mission state changed.
  const loadMissions = useCallback(async () => {
    try {
      const res = await missionApi.getAll()
      if (res?.data) setMissions(res.data)
    } catch {
      // silent — polling will retry
    }
  }, [])

  useEffect(() => {
    loadAll()
    pollingRef.current = setInterval(() => loadAll({ silent: true }), 60000)
    return () => { if (pollingRef.current) clearInterval(pollingRef.current) }
  }, [loadAll])

  // Live WebSocket updates.
  useEffect(() => {
    if (!subscribe) return
    const unsubs = [
      subscribe('DASHBOARD', (data) => {
        if (data?.analytics) setAnalytics(data.analytics)
        setLastLiveUpdate(new Date())
      }),
      subscribe('LOCATION_SNAPSHOT', () => setLastLiveUpdate(new Date())),
      subscribe('TEAM_LOCATION', () => setLastLiveUpdate(new Date())),
      subscribe('DRONE_LOCATION', () => setLastLiveUpdate(new Date())),
      subscribe('NOTIFICATION', (data) => {
        setNotifData(prev => ({
          unread: (prev?.unread || 0) + 1,
          notifications: [data, ...(prev?.notifications || [])].slice(0, 10),
        }))
      }),
      subscribe('MISSION_CREATED', () => loadMissions()),
      subscribe('MISSION_STATUS', () => loadMissions()),
    ]
    return () => unsubs.forEach(u => u())
  }, [subscribe, loadMissions])

  const handleExport = async (format) => {
    try {
      const res = await exportApi[format]()
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `eoc-export.${format}`)
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

  const severityData = mapToArray(dash?.disastersBySeverity)
  const statusData = mapToArray(dash?.disastersByStatus)
  const typeData = mapToArray(dash?.disastersByType)
  const monthData = mapToArray(dash?.disastersByMonth)
  const roleData = mapToArray(dash?.usersByRole)
  const resourceDonut = [
    { name: 'Available', value: dash?.resourcesAvailable || 0 },
    { name: 'Deployed', value: dash?.resourcesDeployed || 0 },
    { name: 'Maintenance', value: dash?.resourcesInMaintenance || 0 },
  ].filter(d => d.value > 0)

  const activeMissions = missions.filter(m => ACTIVE_MISSION_STATUSES.includes(m.status))
  const filteredMissions = missionFilter === 'ALL'
    ? activeMissions
    : activeMissions.filter(m => m.status === missionFilter)

  const filteredAudits = auditLogs.filter(a =>
    !auditFilter ||
    a.action?.toLowerCase().includes(auditFilter.toLowerCase()) ||
    a.performedBy?.toLowerCase().includes(auditFilter.toLowerCase()) ||
    a.entityType?.toLowerCase().includes(auditFilter.toLowerCase())
  ).slice(0, 8)

  const occupancy = [
    { label: 'Resource Utilization', value: analytics?.resourceUtilizationPercent || 0, color: '#4d96ff' },
    { label: 'Volunteer Activity', value: analytics?.volunteerActivityPercent || 0, color: '#6bcb77' },
    { label: 'Hospital Occupancy', value: analytics?.hospitalOccupancyPercent || 0, color: '#ff8800' },
    { label: 'Shelter Occupancy', value: analytics?.shelterOccupancyPercent || 0, color: '#ff6b6b' },
  ]

  const kpis = [
    { icon: <RocketLaunchIcon />, value: dash?.activeMissions || 0, label: 'Active Missions', color: '#4d96ff' },
    { icon: <WifiIcon />, value: dash?.connectedUsers || 0, label: 'Connected Users', color: '#26a69a' },
    { icon: <WavesIcon />, value: dash?.totalDisasters || 0, label: 'Total Disasters', color: '#ff8a65' },
    { icon: <HourglassEmptyIcon />, value: dash?.pendingDisasters || 0, label: 'Pending', color: '#F9A825' },
    { icon: <CheckCircleIcon />, value: dash?.resolvedDisasters || 0, label: 'Resolved', color: '#2E7D32' },
    { icon: <InventoryIcon />, value: dash?.resourcesAvailable || 0, label: 'Resources Available', color: '#ce93d8' },
    { icon: <GroupsIcon />, value: dash?.totalRescueTeams || 0, label: 'Rescue Teams', color: '#0F4C81' },
    { icon: <NotificationsIcon />, value: notifData.unread || 0, label: 'Unread Notifications', color: '#ff6b6b' },
  ]

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: 3, backgroundColor: 'background.default' }}>
        <StatsSkeleton />
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mt: 2 }}>
          <ChartSkeleton /><ChartSkeleton /><ChartSkeleton /><ChartSkeleton />
        </Box>
        <TableSkeleton rows={5} />
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 1.5, sm: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <Breadcrumbs />

        {/* Header */}
        <Box sx={{
          display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between',
          gap: 1.5, mb: 2, p: 2.5, borderRadius: 3,
          background: 'linear-gradient(135deg, #0F4C81 0%, #1A6BB5 100%)',
          color: '#fff',
        }}>
          <Box>
            <Typography variant="h5" fontWeight={700} sx={{ letterSpacing: '0.3px', fontSize: { xs: '1.1rem', md: '1.4rem' } }}>
              Emergency Operations Center
            </Typography>
            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.75)', mt: 0.3 }}>
              Command-level situational awareness and live operations
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
            <LiveClock />
            <WebSocketStatus status={status} reconnectAttempt={reconnectAttempt} />
            <Tooltip title="Refresh dashboard">
              <IconButton
                size="small" onClick={() => loadAll()}
                sx={{ color: '#fff', backgroundColor: 'rgba(255,255,255,0.12)', '&:hover': { backgroundColor: 'rgba(255,255,255,0.22)' } }}
              >
                <RefreshIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>

        {/* KPI stats */}
        <Grid container spacing={1.5} sx={{ mb: 3 }}>
          {kpis.map((k, i) => (
            <Grid size={{ xs: 6, sm: 4, md: 3, lg: 3 }} key={i}>
              <KpiCard {...k} />
            </Grid>
          ))}
        </Grid>

        {/* Charts row 1 */}
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid size={{ xs: 12, md: 6, lg: 4 }}>
            <ChartCard title="Disasters by Severity">
              {severityData.length ? (
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={severityData}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                    <ChartTooltip />
                    <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                      {severityData.map((_, idx) => <Cell key={idx} fill={CHART_COLORS[idx % CHART_COLORS.length]} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : <EmptyState title="No data" message="No disaster severity data." />}
            </ChartCard>
          </Grid>
          <Grid size={{ xs: 12, md: 6, lg: 4 }}>
            <ChartCard title="Status Distribution">
              {statusData.length ? (
                <ResponsiveContainer width="100%" height={220}>
                  <PieChart>
                    <Pie data={statusData} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={48} outerRadius={80} paddingAngle={2}>
                      {statusData.map((_, idx) => <Cell key={idx} fill={CHART_COLORS[idx % CHART_COLORS.length]} />)}
                    </Pie>
                    <ChartTooltip />
                    <Legend wrapperStyle={{ fontSize: 11 }} />
                  </PieChart>
                </ResponsiveContainer>
              ) : <EmptyState title="No data" message="No disaster status data." />}
            </ChartCard>
          </Grid>
          <Grid size={{ xs: 12, md: 6, lg: 4 }}>
            <ChartCard title="Disasters by Type">
              {typeData.length ? (
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={typeData} layout="vertical" margin={{ left: 24 }}>
                    <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                    <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11 }} />
                    <YAxis type="category" dataKey="name" width={78} tick={{ fontSize: 10 }} />
                    <ChartTooltip />
                    <Bar dataKey="value" fill="#ff8a65" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : <EmptyState title="No data" message="No disaster type data." />}
            </ChartCard>
          </Grid>
          <Grid size={{ xs: 12, md: 6, lg: 4 }}>
            <ChartCard title="Disasters by Month" subtitle="Reporting trend">
              {monthData.length ? (
                <ResponsiveContainer width="100%" height={220}>
                  <AreaChart data={monthData}>
                    <defs>
                      <linearGradient id="monthGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#4d96ff" stopOpacity={0.7} />
                        <stop offset="95%" stopColor="#4d96ff" stopOpacity={0.05} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="name" tick={{ fontSize: 10 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                    <ChartTooltip />
                    <Area type="monotone" dataKey="value" stroke="#4d96ff" strokeWidth={2} fill="url(#monthGrad)" />
                  </AreaChart>
                </ResponsiveContainer>
              ) : <EmptyState title="No data" message="No monthly trend data." />}
            </ChartCard>
          </Grid>
          <Grid size={{ xs: 12, md: 6, lg: 4 }}>
            <ChartCard title="Users by Role">
              {roleData.length ? (
                <ResponsiveContainer width="100%" height={220}>
                  <PieChart>
                    <Pie data={roleData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} paddingAngle={2}>
                      {roleData.map((_, idx) => <Cell key={idx} fill={CHART_COLORS[idx % CHART_COLORS.length]} />)}
                    </Pie>
                    <ChartTooltip />
                    <Legend wrapperStyle={{ fontSize: 11 }} />
                  </PieChart>
                </ResponsiveContainer>
              ) : <EmptyState title="No data" message="No user role data." />}
            </ChartCard>
          </Grid>
          <Grid size={{ xs: 12, md: 6, lg: 4 }}>
            <ChartCard title="Resource Utilization" subtitle={`${dash?.resourceUtilizationPercent || 0}% of inventory in use`}>
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                {resourceDonut.length ? (
                  <ResponsiveContainer width="100%" height={170}>
                    <PieChart>
                      <Pie data={resourceDonut} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={48} outerRadius={70} paddingAngle={2}>
                        {resourceDonut.map((d) => <Cell key={d.name} fill={RESOURCE_COLORS[d.name] || '#ccc'} />)}
                      </Pie>
                      <ChartTooltip />
                    </PieChart>
                  </ResponsiveContainer>
                ) : <EmptyState title="No inventory" message="No resource inventory data." />}
                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', justifyContent: 'center' }}>
                  {resourceDonut.map(d => (
                    <Box key={d.name} sx={{ textAlign: 'center' }}>
                      <Typography variant="h6" fontWeight={700} sx={{ color: RESOURCE_COLORS[d.name] || '#666' }}>{d.value}</Typography>
                      <Typography variant="caption" color="text.secondary">{d.name}</Typography>
                    </Box>
                  ))}
                </Box>
              </Box>
            </ChartCard>
          </Grid>
        </Grid>

        {/* Occupancy + performance */}
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
              <Typography variant="h6" sx={{ fontSize: '0.95rem' }}>Operational Capacity</Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Typography variant="caption" color="text.secondary">Avg Response: <b>{(analytics?.averageResponseTimeHours || 0).toFixed(1)}h</b></Typography>
                <Typography variant="caption" color="text.secondary">Avg Resolution: <b>{(analytics?.averageResolutionTimeHours || 0).toFixed(1)}h</b></Typography>
              </Box>
            </Box>
            <Grid container spacing={2}>
              {occupancy.map((o, i) => (
                <Grid size={{ xs: 12, sm: 6, md: 3 }} key={i}>
                  <Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="body2">{o.label}</Typography>
                      <Typography variant="body2" fontWeight={700}>{o.value}%</Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate" value={o.value}
                      sx={{ height: 10, borderRadius: 5, backgroundColor: '#eee', '& .MuiLinearProgress-bar': { backgroundColor: o.color, borderRadius: 5 } }}
                    />
                  </Box>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </Card>

        {/* Active missions table */}
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
              <Typography variant="h6" sx={{ fontSize: '0.95rem' }}>Active Missions</Typography>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
                <Chip icon={<WifiIcon sx={{ fontSize: 13 }} />} label={`${activeMissions.length} active`} size="small" sx={{ backgroundColor: 'rgba(77,150,255,0.1)', color: '#0F4C81' }} />
                <select
                  value={missionFilter}
                  onChange={e => setMissionFilter(e.target.value)}
                  style={{ fontSize: '0.8rem', padding: '4px 8px', borderRadius: 6, border: '1px solid #ccc' }}
                >
                  <option value="ALL">All Active</option>
                  {ACTIVE_MISSION_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
                <Button size="small" variant="outlined" onClick={() => navigate('/rescue-teams')}>Manage</Button>
              </Box>
            </Box>
            <TableContainer component={Paper} variant="outlined" sx={{ boxShadow: 'none' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Mission</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Team</TableCell>
                    <TableCell>Disaster</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Started</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredMissions.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} sx={{ py: 3 }}>
                        <EmptyState title="No active missions" message="Active rescue missions will appear here in real time." />
                      </TableCell>
                    </TableRow>
                  ) : filteredMissions.map(m => (
                    <TableRow key={m.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight={600}>{m.missionCode}</Typography>
                        <Typography variant="caption" color="text.secondary">{m.title}</Typography>
                      </TableCell>
                      <TableCell><Chip label={m.missionType || '—'} size="small" sx={{ fontSize: '0.7rem' }} /></TableCell>
                      <TableCell>{m.teamName || '—'}</TableCell>
                      <TableCell>{m.disasterName || '—'}</TableCell>
                      <TableCell>
                        <Chip
                          label={m.status}
                          size="small"
                          sx={{ fontWeight: 700, fontSize: '0.68rem', backgroundColor: `${MISSION_COLORS[m.status]}18`, color: MISSION_COLORS[m.status] }}
                        />
                      </TableCell>
                      <TableCell>{m.startTime ? new Date(m.startTime).toLocaleString() : '—'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>

        {/* Activities + notifications */}
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Typography variant="h6" sx={{ fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: 1 }}>
                    <HistoryIcon color="primary" /> Recent Activities
                  </Typography>
                  <Button size="small" onClick={() => navigate('/audit-logs')}>View all</Button>
                </Box>
                {!dash?.recentActivities?.length ? (
                  <EmptyState title="No activity" message="System activity will appear here." />
                ) : (
                  <List dense disablePadding>
                    {dash.recentActivities.slice(0, 7).map((a, i) => (
                      <ListItem key={i} disablePadding sx={{ py: 0.6 }}>
                        <ListItemAvatar sx={{ minWidth: 36 }}>
                          <Avatar sx={{ width: 26, height: 26, fontSize: '0.7rem', bgcolor: 'rgba(15,76,129,0.1)', color: '#0F4C81' }}>
                            {a.action?.[0]?.toUpperCase() || '•'}
                          </Avatar>
                        </ListItemAvatar>
                        <ListItemText
                          primary={
                            <Typography variant="body2">
                              <b>{a.action}</b>
                              {a.entityType ? ` · ${a.entityType}` : ''}
                            </Typography>
                          }
                          secondary={
                            <Typography variant="caption" color="text.secondary">
                              {a.performedBy || 'system'} · {a.timestamp ? new Date(a.timestamp).toLocaleString() : ''}
                            </Typography>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Typography variant="h6" sx={{ fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: 1 }}>
                    <NotificationsIcon color="primary" /> Notifications
                  </Typography>
                  <Button size="small" onClick={() => navigate('/notifications')}>View all</Button>
                </Box>
                {!notifData.notifications?.length ? (
                  <EmptyState title="No notifications" message="System notifications will appear here." />
                ) : (
                  <List dense disablePadding>
                    {notifData.notifications.slice(0, 7).map(n => (
                      <ListItem key={n.id} disablePadding sx={{ py: 0.6 }}>
                        <ListItemAvatar sx={{ minWidth: 36 }}>
                          <Avatar
                            sx={{ width: 26, height: 26, fontSize: '0.7rem', bgcolor: n.read ? 'rgba(0,0,0,0.06)' : 'rgba(255,107,107,0.12)', color: n.read ? '#999' : '#C62828' }}
                          >
                            {n.type?.[0]?.toUpperCase() || '•'}
                          </Avatar>
                        </ListItemAvatar>
                        <ListItemText
                          primary={<Typography variant="body2" sx={{ fontWeight: n.read ? 400 : 700 }}>{n.title}</Typography>}
                          secondary={
                            <Typography variant="caption" color="text.secondary">
                              {n.message} · {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                            </Typography>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Audit logs table */}
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
              <Typography variant="h6" sx={{ fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: 1 }}>
                <DescriptionIcon color="primary" /> Audit Logs
              </Typography>
              <TextField
                size="small" placeholder="Filter action / user / entity…"
                value={auditFilter} onChange={e => setAuditFilter(e.target.value)}
                sx={{ width: 260 }}
              />
            </Box>
            <TableContainer component={Paper} variant="outlined" sx={{ boxShadow: 'none' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Action</TableCell>
                    <TableCell>Entity</TableCell>
                    <TableCell>Performed By</TableCell>
                    <TableCell>Details</TableCell>
                    <TableCell>Timestamp</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredAudits.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} sx={{ py: 3 }}>
                        <EmptyState title="No audit entries" message="Audit log entries will appear here." />
                      </TableCell>
                    </TableRow>
                  ) : filteredAudits.map(a => (
                    <TableRow key={a.id} hover>
                      <TableCell><Chip label={a.action} size="small" sx={{ fontSize: '0.68rem', backgroundColor: 'rgba(15,76,129,0.08)', color: '#0F4C81' }} /></TableCell>
                      <TableCell>
                        <Typography variant="body2">{a.entityType}</Typography>
                        {a.entityId && <Typography variant="caption" color="text.secondary">#{a.entityId}</Typography>}
                      </TableCell>
                      <TableCell>{a.performedBy}</TableCell>
                      <TableCell sx={{ maxWidth: 260 }}>
                        <Typography variant="body2" sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{a.details || '—'}</Typography>
                      </TableCell>
                      <TableCell>{a.timestamp ? new Date(a.timestamp).toLocaleString() : '—'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>

        {/* Quick actions + settings */}
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid size={{ xs: 12, md: 5 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Typography variant="h6" sx={{ fontSize: '0.95rem', mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <SecurityIcon color="primary" /> Quick Actions
                </Typography>
                <Grid container spacing={1}>
                  {[
                    { label: 'Report Disaster', icon: <ReportIcon />, onClick: () => navigate('/report') },
                    { label: 'Live Map', icon: <MapIcon />, onClick: () => navigate('/map') },
                    { label: 'Rescue Teams', icon: <GroupsIcon />, onClick: () => navigate('/rescue-teams') },
                    { label: 'Notifications', icon: <NotificationsIcon />, onClick: () => navigate('/notifications') },
                    { label: 'Audit Logs', icon: <DescriptionIcon />, onClick: () => navigate('/audit-logs') },
                    { label: 'Analytics', icon: <BarChartIcon />, onClick: () => navigate('/analytics') },
                    { label: 'Export CSV', icon: <DownloadIcon />, onClick: () => handleExport('csv') },
                    { label: 'Export Excel', icon: <DownloadIcon />, onClick: () => handleExport('excel') },
                    { label: 'System Settings', icon: <SettingsIcon />, onClick: () => navigate('/admin-control') },
                  ].map((a, i) => (
                    <Grid size={{ xs: 6, sm: 4 }} key={i}>
                      <Button
                        fullWidth variant="outlined" startIcon={a.icon} onClick={a.onClick}
                        sx={{ py: 1.2, justifyContent: 'flex-start', fontSize: '0.78rem' }}
                      >
                        {a.label}
                      </Button>
                    </Grid>
                  ))}
                </Grid>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary' }}>
                  <WifiTetheringIcon fontSize="small" />
                  <Typography variant="caption">
                    Live feed last updated: {lastLiveUpdate ? lastLiveUpdate.toLocaleTimeString() : '—'}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary', mt: 0.5 }}>
                  <FlightIcon fontSize="small" />
                  <Typography variant="caption">
                    Monitoring: {overview?.disasters || 0} disasters · {overview?.drones || 0} drones · {overview?.teams || 0} teams · {overview?.vehicles || 0} vehicles
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary', mt: 0.5 }}>
                  <RefreshIcon fontSize="small" />
                  <Typography variant="caption">
                    Last REST refresh: {lastRefreshed ? lastRefreshed.toLocaleTimeString() : '—'} (auto every 30s)
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, md: 7 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Typography variant="h6" sx={{ fontSize: '0.95rem', mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <SettingsIcon color="primary" /> System Settings
                </Typography>
                {!settings.length ? (
                  <Alert severity="info">No system settings available.</Alert>
                ) : (
                  <Grid container spacing={1}>
                    {settings.map(s => (
                      <Grid size={{ xs: 12, sm: 6 }} key={s.settingKey}>
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 1, border: '1px solid', borderColor: 'divider', borderRadius: 1.5, gap: 1 }}>
                          <Box sx={{ flex: 1, minWidth: 0 }}>
                            <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.4px' }}>
                              {s.settingKey.replace(/([A-Z])/g, ' $1').trim()}
                            </Typography>
                            {editingSetting === s.settingKey ? (
                              <TextField size="small" fullWidth value={editValue} onChange={e => setEditValue(e.target.value)} sx={{ mt: 0.5 }} />
                            ) : (
                              <Typography variant="body2" sx={{ fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                {s.settingValue}
                              </Typography>
                            )}
                          </Box>
                          {editingSetting === s.settingKey ? (
                            <Box sx={{ display: 'flex', gap: 0.5 }}>
                              <IconButton size="small" color="primary" onClick={() => handleSaveSetting(s.settingKey)}><CheckIcon fontSize="small" /></IconButton>
                              <IconButton size="small" onClick={() => setEditingSetting(null)}><CloseIcon fontSize="small" /></IconButton>
                            </Box>
                          ) : (
                            <IconButton size="small" onClick={() => { setEditingSetting(s.settingKey); setEditValue(s.settingValue || '') }}>
                              <EditIcon fontSize="small" />
                            </IconButton>
                          )}
                        </Box>
                      </Grid>
                    ))}
                  </Grid>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Footer />
      </Box>
    </Box>
  )
}

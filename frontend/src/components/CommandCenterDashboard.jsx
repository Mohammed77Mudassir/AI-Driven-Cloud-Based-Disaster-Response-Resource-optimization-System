import { useState, useEffect, useCallback, useMemo, useRef } from 'react'
import {
  Box, Card, CardContent, Typography, Grid, Chip, LinearProgress, IconButton,
  Tooltip, TextField, Select, MenuItem, InputAdornment, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Paper, Avatar, List, ListItem,
  ListItemAvatar, ListItemText,
} from '@mui/material'
import {
  ComposedChart, Area, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ChartTooltip,
  ResponsiveContainer, Legend, Cell,
} from 'recharts'
import toast from 'react-hot-toast'
import { commandCenterApi } from '../services/api'
import { useWebSocket } from '../context/WebSocketContext'
import AnimatedCounter from './AnimatedCounter'
import { StatsSkeleton, TableSkeleton } from './LoadingSkeleton'
import EmptyState from './EmptyState'
import GroupsIcon from '@mui/icons-material/Groups'
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import HotelIcon from '@mui/icons-material/Hotel'
import FlagIcon from '@mui/icons-material/Flag'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import TimerIcon from '@mui/icons-material/Timer'
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty'
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar'
import BuildIcon from '@mui/icons-material/Build'
import RefreshIcon from '@mui/icons-material/Refresh'
import SearchIcon from '@mui/icons-material/Search'
import WifiTetheringIcon from '@mui/icons-material/WifiTethering'
import WifiOffIcon from '@mui/icons-material/WifiOff'
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive'
import TimelineIcon from '@mui/icons-material/Timeline'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import PersonIcon from '@mui/icons-material/Person'
import LocalShippingIcon from '@mui/icons-material/LocalShipping'

const TEAM_STATUS_COLORS = {
  AVAILABLE: '#2E7D32',
  ON_MISSION: '#0F4C81',
  STANDING_BY: '#F9A825',
  TRAINING: '#F57C00',
  DEPLOYED: '#C62828',
  RETURNED: '#00897B',
  OFF_DUTY: '#757575',
}

const MISSION_STATUS_COLORS = {
  PENDING: '#F9A825',
  ASSIGNED: '#0F4C81',
  IN_PROGRESS: '#F57C00',
  COMPLETED: '#2E7D32',
  CANCELLED: '#C62828',
}

const VEHICLE_STATUS_COLORS = {
  AVAILABLE: '#2E7D32',
  DEPLOYED: '#0F4C81',
  IN_MAINTENANCE: '#F57C00',
  OUT_OF_SERVICE: '#C62828',
}

const PRIORITY_COLORS = {
  LOW: '#757575',
  MEDIUM: '#F9A825',
  HIGH: '#E65100',
  CRITICAL: '#C62828',
}

const WORKLOAD_COLORS = {
  LOW: '#2E7D32',
  MEDIUM: '#F9A825',
  HIGH: '#F57C00',
  CRITICAL: '#C62828',
}

const ALERT_COLORS = {
  CRITICAL: '#C62828',
  WARNING: '#F57C00',
  INFO: '#0F4C81',
}

const PRIORITY_RANK = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }
const STATUS_RANK = { IN_PROGRESS: 0, ASSIGNED: 1, PENDING: 2 }

const KPI_META = {
  TOTAL_TEAMS: { icon: <GroupsIcon />, color: '#0F4C81' },
  TEAMS_ON_MISSION: { icon: <RocketLaunchIcon />, color: '#F57C00' },
  AVAILABLE_TEAMS: { icon: <CheckCircleIcon />, color: '#2E7D32' },
  TEAMS_RESTING: { icon: <HotelIcon />, color: '#00897B' },
  ACTIVE_MISSIONS: { icon: <FlagIcon />, color: '#4d96ff' },
  CRITICAL_MISSIONS: { icon: <WarningAmberIcon />, color: '#C62828' },
  AVG_RESPONSE: { icon: <TimerIcon />, color: '#7b4dff' },
  AVG_DURATION: { icon: <HourglassEmptyIcon />, color: '#6a5acd' },
  VEHICLE_AVAILABILITY: { icon: <DirectionsCarIcon />, color: '#26a69a' },
  EQUIPMENT_READINESS: { icon: <BuildIcon />, color: '#ff8a65' },
}

const fmt = (iso) => {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

const fmtTime = (iso) => {
  if (!iso) return '-'
  return new Date(iso).toLocaleString(undefined, { timeStyle: 'short' })
}

const formatMinutes = (minutes) => {
  if (!minutes && minutes !== 0) return '-'
  if (minutes >= 60) return `${(minutes / 60).toFixed(1)}h`
  return `${Math.round(minutes)}m`
}

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

function KpiCard({ kpi }) {
  const meta = KPI_META[kpi.key] || { icon: <FlagIcon />, color: '#0F4C81' }
  const value = kpi.unit === '%'
    ? Math.round(kpi.value)
    : kpi.unit === 'min'
      ? Math.round(kpi.value)
      : kpi.value
  const suffix = kpi.unit === '%' ? '%' : kpi.unit === 'min' ? '' : ''
  return (
    <Card sx={{ height: '100%', borderTop: `3px solid ${meta.color}` }}>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 1.5, p: 2 }}>
        <Box aria-hidden sx={{
          width: 44, height: 44, borderRadius: 2, display: 'flex', alignItems: 'center',
          justifyContent: 'center', backgroundColor: `${meta.color}15`, color: meta.color, flexShrink: 0,
        }}>
          {meta.icon}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <AnimatedCounter
            value={value}
            separator={false}
            sx={{ fontWeight: 800, lineHeight: 1.15, fontSize: '1.35rem' }}
          />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.74rem', lineHeight: 1.25 }} noWrap>
            {kpi.label}{suffix && <span style={{ color: meta.color }}> {suffix}</span>}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  )
}

function StatusChip({ value, colors, label }) {
  const color = colors[value] || '#607D8B'
  return (
    <Chip
      label={label || (value || '').replace(/_/g, ' ')}
      size="small"
      sx={{ fontWeight: 700, fontSize: '0.68rem', backgroundColor: `${color}18`, color }}
    />
  )
}

export default function CommandCenterDashboard() {
  const { connected, status, subscribe } = useWebSocket()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [lastRefreshed, setLastRefreshed] = useState(null)
  const [lastLiveUpdate, setLastLiveUpdate] = useState(null)

  const [filters, setFilters] = useState({ search: '', teamId: '', status: '', priority: '', missionType: '', vehicle: '' })
  const [teamStatusFilter, setTeamStatusFilter] = useState('ALL')
  const [sortKey, setSortKey] = useState('priority')
  const [sortDir, setSortDir] = useState('desc')

  const lastReloadRef = useRef(0)

  const load = useCallback(async (silent = false) => {
    if (!silent) setLoading(true)
    try {
      const res = await commandCenterApi.getDashboard()
      setData(res.data)
      setLastRefreshed(new Date())
    } catch {
      if (!silent) toast.error('Failed to load command center data')
    } finally {
      if (!silent) setLoading(false)
    }
  }, [])

  const throttledReload = useCallback(() => {
    const now = Date.now()
    if (now - lastReloadRef.current > 10000) {
      lastReloadRef.current = now
      load(true)
    }
  }, [load])

  useEffect(() => {
    load()
    const poll = setInterval(() => load(true), 60000)
    return () => clearInterval(poll)
  }, [load])

  useEffect(() => {
    if (!subscribe) return
    const onMissionEvent = () => { setLastLiveUpdate(new Date()); throttledReload() }
    const unsubs = [
      subscribe('MISSION_CREATED', onMissionEvent),
      subscribe('MISSION_STATUS', onMissionEvent),
      subscribe('TEAM_LOCATION', onMissionEvent),
      subscribe('LOCATION_SNAPSHOT', () => setLastLiveUpdate(new Date())),
      subscribe('NOTIFICATION', () => setLastLiveUpdate(new Date())),
    ]
    return () => unsubs.forEach(u => u())
  }, [subscribe, throttledReload])

  const activeMissions = useMemo(() => {
    if (!data) return []
    const list = [...data.activeMissions]
    if (filters.search) {
      const q = filters.search.toLowerCase()
      return list.filter(m =>
        (m.title || '').toLowerCase().includes(q) ||
        (m.missionCode || '').toLowerCase().includes(q) ||
        (m.teamName || '').toLowerCase().includes(q) ||
        (m.teamLeader || '').toLowerCase().includes(q) ||
        (m.location || '').toLowerCase().includes(q) ||
        (m.disasterName || '').toLowerCase().includes(q))
    }
    return list
  }, [data, filters.search])

  const filteredMissions = useMemo(() => {
    let list = activeMissions
    if (filters.teamId) list = list.filter(m => String(m.teamId) === String(filters.teamId))
    if (filters.status) list = list.filter(m => m.status === filters.status)
    if (filters.priority) list = list.filter(m => m.priority === filters.priority)
    if (filters.missionType) list = list.filter(m => m.missionType === filters.missionType)
    if (filters.vehicle) list = list.filter(m => (m.assignedVehicle || '').toLowerCase().includes(filters.vehicle.toLowerCase()))
    const sorted = [...list]
    sorted.sort((a, b) => {
      let cmp = 0
      if (sortKey === 'priority') cmp = (PRIORITY_RANK[a.priority] ?? 9) - (PRIORITY_RANK[b.priority] ?? 9)
      else if (sortKey === 'status') cmp = (STATUS_RANK[a.status] ?? 9) - (STATUS_RANK[b.status] ?? 9)
      else if (sortKey === 'startTime') cmp = new Date(a.startTime || 0) - new Date(b.startTime || 0)
      return sortDir === 'asc' ? -cmp : cmp
    })
    return sorted
  }, [activeMissions, filters.teamId, filters.status, filters.priority, filters.missionType, filters.vehicle, sortKey, sortDir])

  const filteredTeams = useMemo(() => {
    if (!data) return []
    let list = data.teams
    if (teamStatusFilter !== 'ALL') list = list.filter(t => t.status === teamStatusFilter)
    if (filters.search) {
      const q = filters.search.toLowerCase()
      list = list.filter(t =>
        (t.teamName || '').toLowerCase().includes(q) ||
        (t.teamLeader || '').toLowerCase().includes(q) ||
        (t.specialization || '').toLowerCase().includes(q) ||
        (t.location || '').toLowerCase().includes(q))
    }
    return list
  }, [data, teamStatusFilter, filters.search])

  const missionTypeOptions = useMemo(() =>
    [...new Set((data?.activeMissions || []).map(m => m.missionType).filter(Boolean))], [data])
  const vehicleOptions = useMemo(() =>
    [...new Set((data?.activeMissions || []).map(m => m.assignedVehicle).filter(Boolean))], [data])

  const trendData = useMemo(() =>
    (data?.responseAnalytics?.monthlyTrend || []).map(m => ({
      name: m.month,
      avg: m.averageResponseMinutes,
      completed: m.missionsCompleted,
    })), [data])

  const workloadData = useMemo(() =>
    (data?.workloads || []).map(w => ({ name: w.teamName, score: Math.round(w.score), level: w.level })), [data])

  if (loading && !data) {
    return (
      <Box>
        <StatsSkeleton count={10} />
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 2 }}>
          <TableSkeleton rows={4} /><TableSkeleton rows={4} />
        </Box>
      </Box>
    )
  }

  const kpis = data?.kpis || []
  const analytics = data?.responseAnalytics || {}
  const isOffline = status === 'disconnected' || status === 'reconnecting'

  return (
    <Box>
      {/* Operational banner */}
      <Paper sx={{
        p: 2, mb: 2, borderRadius: 3, display: 'flex', flexWrap: 'wrap', alignItems: 'center',
        gap: 1.5, background: 'linear-gradient(135deg, #0F4C81 0%, #1A6BB5 100%)', color: '#fff',
      }}>
        <Box sx={{ flex: 1, minWidth: 220 }}>
          <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: '0.3px' }}>
            Emergency Operations Command Center
          </Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.75)', mt: 0.2 }}>
            Live rescue team, mission, fleet and readiness posture
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
            {connected
              ? <WifiTetheringIcon fontSize="small" sx={{ color: '#6bcb77' }} />
              : <WifiOffIcon fontSize="small" sx={{ color: isOffline ? '#ff6b6b' : '#F9A825' }} />}
            <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.85)' }}>
              {status === 'connected' ? 'Live feed connected' : `Live feed ${status}`}
            </Typography>
          </Box>
          <LiveClock />
          <Tooltip title="Refresh dashboard">
            <IconButton
              size="small" onClick={() => load()}
              sx={{ color: '#fff', backgroundColor: 'rgba(255,255,255,0.12)', '&:hover': { backgroundColor: 'rgba(255,255,255,0.22)' } }}
            >
              <RefreshIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </Paper>

      {/* Filters */}
      <Paper sx={{ p: 1.5, mb: 2, borderRadius: 2 }}>
        <Box sx={{ display: 'flex', gap: 1.25, alignItems: 'center', flexWrap: 'wrap' }}>
          <TextField
            size="small"
            placeholder="Search mission, team, leader, location…"
            value={filters.search}
            onChange={e => setFilters(f => ({ ...f, search: e.target.value }))}
            sx={{ minWidth: 250 }}
            slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon sx={{ color: '#636E72' }} /></InputAdornment> } }}
          />
          <Select size="small" value={filters.teamId} onChange={e => setFilters(f => ({ ...f, teamId: e.target.value }))} displayEmpty sx={{ minWidth: 140 }}>
            <MenuItem value="">All Teams</MenuItem>
            {(data?.teams || []).map(t => <MenuItem key={t.teamId} value={t.teamId}>{t.teamName}</MenuItem>)}
          </Select>
          <Select size="small" value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))} displayEmpty sx={{ minWidth: 140 }}>
            <MenuItem value="">All Status</MenuItem>
            {Object.keys(MISSION_STATUS_COLORS).map(s => <MenuItem key={s} value={s}>{s.replace('_', ' ')}</MenuItem>)}
          </Select>
          <Select size="small" value={filters.priority} onChange={e => setFilters(f => ({ ...f, priority: e.target.value }))} displayEmpty sx={{ minWidth: 130 }}>
            <MenuItem value="">All Priority</MenuItem>
            {Object.keys(PRIORITY_COLORS).map(p => <MenuItem key={p} value={p}>{p}</MenuItem>)}
          </Select>
          <Select size="small" value={filters.missionType} onChange={e => setFilters(f => ({ ...f, missionType: e.target.value }))} displayEmpty sx={{ minWidth: 160 }}>
            <MenuItem value="">All Disaster Types</MenuItem>
            {missionTypeOptions.map(t => <MenuItem key={t} value={t}>{t.replace(/_/g, ' ')}</MenuItem>)}
          </Select>
          <Select size="small" value={filters.vehicle} onChange={e => setFilters(f => ({ ...f, vehicle: e.target.value }))} displayEmpty sx={{ minWidth: 160 }}>
            <MenuItem value="">All Vehicles</MenuItem>
            {vehicleOptions.map(v => <MenuItem key={v} value={v}>{v}</MenuItem>)}
          </Select>
          <Box sx={{ ml: 'auto' }}>
            <Typography variant="caption" color="text.secondary">
              Updated {lastRefreshed ? lastRefreshed.toLocaleTimeString() : '—'} · Live {lastLiveUpdate ? lastLiveUpdate.toLocaleTimeString() : '—'}
            </Typography>
          </Box>
        </Box>
      </Paper>

      {/* KPI cards */}
      <Grid container spacing={1.5} sx={{ mb: 2.5 }}>
        {kpis.map(k => (
          <Grid size={{ xs: 6, sm: 4, md: 3, lg: 2.4 }} key={k.key}>
            <KpiCard kpi={k} />
          </Grid>
        ))}
      </Grid>

      {/* Active Mission Panel */}
      <Card sx={{ mb: 2.5 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
              <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                <FlagIcon sx={{ fontSize: 20 }} />
              </Box>
              <Box>
                <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Active Mission Panel</Typography>
                <Typography variant="caption" color="text.secondary">
                  {filteredMissions.length} ongoing mission{filteredMissions.length === 1 ? '' : 's'}
                </Typography>
              </Box>
            </Box>
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
              <Select size="small" value={sortKey} onChange={e => setSortKey(e.target.value)} sx={{ minWidth: 130 }}>
                <MenuItem value="priority">Sort: Priority</MenuItem>
                <MenuItem value="status">Sort: Status</MenuItem>
                <MenuItem value="startTime">Sort: Start Time</MenuItem>
              </Select>
              <Tooltip title={sortDir === 'desc' ? 'Sort descending' : 'Sort ascending'}>
                <IconButton size="small" onClick={() => setSortDir(d => (d === 'desc' ? 'asc' : 'desc'))}>
                  {sortDir === 'desc' ? '↓' : '↑'}
                </IconButton>
              </Tooltip>
            </Box>
          </Box>
          {filteredMissions.length === 0 ? (
            <EmptyState title="No active missions" message="Ongoing rescue missions will appear here in real time." />
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ boxShadow: 'none' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Mission</TableCell>
                    <TableCell>Disaster Type</TableCell>
                    <TableCell>Assigned Team</TableCell>
                    <TableCell>Priority</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Team Leader</TableCell>
                    <TableCell>Vehicle</TableCell>
                    <TableCell>Current Location</TableCell>
                    <TableCell>Start Time</TableCell>
                    <TableCell>Est. Completion</TableCell>
                    <TableCell sx={{ minWidth: 130 }}>Progress</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredMissions.map(m => (
                    <TableRow key={m.missionId} hover>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>{m.title}</Typography>
                        <Typography variant="caption" color="text.secondary">{m.missionCode}</Typography>
                      </TableCell>
                      <TableCell>{m.missionType ? m.missionType.replace(/_/g, ' ') : '-'}</TableCell>
                      <TableCell>{m.teamName || '-'}</TableCell>
                      <TableCell><StatusChip value={m.priority} colors={PRIORITY_COLORS} /></TableCell>
                      <TableCell><StatusChip value={m.status} colors={MISSION_STATUS_COLORS} /></TableCell>
                      <TableCell>{m.teamLeader || '-'}</TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <LocalShippingIcon sx={{ fontSize: 15, color: 'text.disabled' }} />
                          <Typography variant="body2">{m.assignedVehicle || '-'}</Typography>
                        </Box>
                      </TableCell>
                      <TableCell>{m.location || '-'}</TableCell>
                      <TableCell>{fmtTime(m.startTime)}</TableCell>
                      <TableCell>{fmtTime(m.estimatedCompletionTime)}</TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Box sx={{ flex: 1, minWidth: 60 }}>
                            <LinearProgress
                              variant="determinate"
                              value={m.progress}
                              sx={{
                                height: 7, borderRadius: 4,
                                backgroundColor: '#eee',
                                '& .MuiLinearProgress-bar': {
                                  borderRadius: 4,
                                  backgroundColor: m.progress >= 100 ? '#2E7D32' : m.status === 'IN_PROGRESS' ? '#F57C00' : '#4d96ff',
                                },
                              }}
                            />
                          </Box>
                          <Typography variant="caption" sx={{ fontWeight: 700, width: 32 }}>{m.progress}%</Typography>
                          {m.delayMinutes > 0 && (
                            <Chip
                              label={`+${m.delayMinutes}m delay`}
                              size="small"
                              sx={{ fontSize: '0.62rem', fontWeight: 700, color: '#C62828', backgroundColor: '#C6282818' }}
                            />
                          )}
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* Analytics + Workload */}
      <Grid container spacing={2} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, lg: 8 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 1.5 }}>
                <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                  <TimerIcon sx={{ fontSize: 20 }} />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Response Time Analytics</Typography>
                  <Typography variant="caption" color="text.secondary">Monthly response trend & completion performance</Typography>
                </Box>
              </Box>
              <Grid container spacing={1.5} sx={{ mb: 2 }}>
                {[
                  { label: 'Average Response', value: formatMinutes(analytics.averageResponseMinutes), color: '#0F4C81' },
                  { label: 'Fastest Response', value: formatMinutes(analytics.fastestResponseMinutes), color: '#2E7D32' },
                  { label: 'Slowest Response', value: formatMinutes(analytics.slowestResponseMinutes), color: '#C62828' },
                  { label: 'Avg Mission Duration', value: formatMinutes(analytics.averageMissionDurationMinutes), color: '#6a5acd' },
                  { label: 'Completion Rate', value: `${Math.round(analytics.completionRate || 0)}%`, color: '#00897B' },
                ].map(s => (
                  <Grid size={{ xs: 6, sm: 4, md: 2.4 }} key={s.label}>
                    <Box sx={{ p: 1.25, borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
                      <Typography variant="h6" sx={{ fontWeight: 800, color: s.color, lineHeight: 1.1 }}>{s.value}</Typography>
                      <Typography variant="caption" color="text.secondary">{s.label}</Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
              {trendData.length === 0 ? (
                <EmptyState title="No trend data" message="Monthly response-time data will appear here." />
              ) : (
                <ResponsiveContainer width="100%" height={220}>
                  <ComposedChart data={trendData}>
                    <defs>
                      <linearGradient id="respGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#4d96ff" stopOpacity={0.7} />
                        <stop offset="95%" stopColor="#4d96ff" stopOpacity={0.05} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                    <YAxis tick={{ fontSize: 11 }} />
                    <ChartTooltip />
                    <Legend wrapperStyle={{ fontSize: 11 }} />
                    <Bar dataKey="completed" name="Missions Completed" fill="#6bcb77" radius={[3, 3, 0, 0]} barSize={18} />
                    <Area type="monotone" dataKey="avg" name="Avg Response (min)" stroke="#4d96ff" strokeWidth={2} fill="url(#respGrad)" />
                  </ComposedChart>
                </ResponsiveContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 1.5 }}>
                <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                  <GroupsIcon sx={{ fontSize: 20 }} />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Team Workload</Typography>
                  <Typography variant="caption" color="text.secondary">Composite load score per team</Typography>
                </Box>
              </Box>
              {workloadData.length === 0 ? (
                <EmptyState title="No workload data" message="Team workload profiles will appear here." />
              ) : (
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={workloadData} layout="vertical" margin={{ left: 8 }}>
                    <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                    <XAxis type="number" domain={[0, 100]} tick={{ fontSize: 11 }} />
                    <YAxis type="category" dataKey="name" width={110} tick={{ fontSize: 10 }} />
                    <ChartTooltip />
                    <Bar dataKey="score" radius={[0, 4, 4, 0]}>
                      {workloadData.map((w, i) => (
                        <Cell key={i} fill={WORKLOAD_COLORS[w.level] || '#607D8B'} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              )}
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1 }}>
                {Object.entries(WORKLOAD_COLORS).map(([level, color]) => (
                  <Chip key={level} label={level} size="small" sx={{ fontWeight: 700, fontSize: '0.66rem', backgroundColor: `${color}18`, color }} />
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Team Availability */}
      <Card sx={{ mb: 2.5 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
              <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                <GroupsIcon sx={{ fontSize: 20 }} />
              </Box>
              <Box>
                <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Team Availability</Typography>
                <Typography variant="caption" color="text.secondary">Deployable posture of every rescue team</Typography>
              </Box>
            </Box>
            <Select size="small" value={teamStatusFilter} onChange={e => setTeamStatusFilter(e.target.value)} sx={{ minWidth: 150 }}>
              <MenuItem value="ALL">All Availability</MenuItem>
              {Object.keys(TEAM_STATUS_COLORS).map(s => <MenuItem key={s} value={s}>{s.replace('_', ' ')}</MenuItem>)}
            </Select>
          </Box>
          {filteredTeams.length === 0 ? (
            <EmptyState title="No teams found" message="Adjust your search or availability filter." />
          ) : (
            <Grid container spacing={1.5}>
              {filteredTeams.map(t => {
                const color = TEAM_STATUS_COLORS[t.status] || '#607D8B'
                return (
                  <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={t.teamId}>
                    <Card variant="outlined" sx={{ height: '100%', borderLeft: `4px solid ${color}` }}>
                      <CardContent sx={{ p: 1.75 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
                          <Box sx={{ minWidth: 0 }}>
                            <Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.2 }}>{t.teamName}</Typography>
                            <Typography variant="caption" color="text.secondary">{t.specialization || 'General Purpose'}</Typography>
                          </Box>
                          <StatusChip value={t.status} colors={TEAM_STATUS_COLORS} />
                        </Box>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.6, mt: 1.25 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                            <PersonIcon sx={{ fontSize: 15, color: 'text.disabled' }} />
                            <Typography variant="body2" sx={{ fontSize: '0.8rem' }}>
                              {t.teamLeader || 'No leader'} · <b>{t.teamSize || 0}</b>/{t.maxCapacity || '-'} members
                            </Typography>
                          </Box>
                          {t.assignedMissionCode && (
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                              <FlagIcon sx={{ fontSize: 15, color: 'text.disabled' }} />
                              <Typography variant="body2" sx={{ fontSize: '0.8rem' }} noWrap title={t.assignedMissionTitle}>
                                {t.assignedMissionCode} — {t.assignedMissionTitle}
                              </Typography>
                            </Box>
                          )}
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                            <LocationOnIcon sx={{ fontSize: 15, color: 'text.disabled' }} />
                            <Typography variant="body2" sx={{ fontSize: '0.8rem' }} noWrap>{t.location || 'Location unknown'}</Typography>
                          </Box>
                          <Typography variant="caption" color="text.disabled" sx={{ mt: 0.25 }}>
                            Last update: {fmtTime(t.lastLocationUpdateAt)}
                          </Typography>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                )
              })}
            </Grid>
          )}
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1.5 }}>
            {Object.entries(TEAM_STATUS_COLORS).map(([s, c]) => (
              <Box key={s} sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <Box sx={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: c }} />
                <Typography variant="caption" color="text.secondary">{s.replace('_', ' ')}</Typography>
              </Box>
            ))}
          </Box>
        </CardContent>
      </Card>

      {/* Equipment Readiness + Vehicle Status */}
      <Grid container spacing={2} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, lg: 5 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 1.5 }}>
                <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                  <BuildIcon sx={{ fontSize: 20 }} />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Equipment Readiness</Typography>
                  <Typography variant="caption" color="text.secondary">Available / deployed / maintenance / missing</Typography>
                </Box>
              </Box>
              {(data?.equipmentReadiness || []).length === 0 ? (
                <EmptyState title="No equipment data" message="Equipment readiness per team will appear here." />
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.75 }}>
                  {(data?.equipmentReadiness || []).map(e => {
                    const pct = e.readinessPercent
                    const color = pct >= 70 ? '#2E7D32' : pct >= 50 ? '#F57C00' : '#C62828'
                    return (
                      <Box key={e.teamId}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5, gap: 1 }}>
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>{e.teamName}</Typography>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Chip label={`${Math.round(pct)}% ready`} size="small" sx={{ fontWeight: 700, fontSize: '0.66rem', backgroundColor: `${color}18`, color }} />
                            {e.missing > 0 && (
                              <Chip label={`${e.missing} missing`} size="small" sx={{ fontWeight: 700, fontSize: '0.66rem', backgroundColor: '#C6282818', color: '#C62828' }} />
                            )}
                          </Box>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={Math.min(100, pct)}
                          sx={{ height: 8, borderRadius: 4, backgroundColor: '#eee', '& .MuiLinearProgress-bar': { borderRadius: 4, backgroundColor: color } }}
                        />
                        <Typography variant="caption" color="text.secondary">
                          {e.available} available · {e.deployed} deployed · {e.maintenance} in maintenance · {e.missing} missing of {e.assigned} assigned
                        </Typography>
                      </Box>
                    )
                  })}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 7 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 1.5 }}>
                <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                  <DirectionsCarIcon sx={{ fontSize: 20 }} />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Vehicle Status</Typography>
                  <Typography variant="caption" color="text.secondary">Fleet fuel, maintenance and GPS posture</Typography>
                </Box>
              </Box>
              {(data?.vehicles || []).length === 0 ? (
                <EmptyState title="No vehicles" message="Registered fleet vehicles will appear here." />
              ) : (
                <TableContainer component={Paper} variant="outlined" sx={{ boxShadow: 'none' }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Vehicle</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>Fuel</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Team</TableCell>
                        <TableCell>Mission</TableCell>
                        <TableCell>Last Maint.</TableCell>
                        <TableCell>GPS</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {(data?.vehicles || []).map(v => (
                        <TableRow key={v.vehicleId} hover sx={{ backgroundColor: v.unavailable ? 'rgba(198,40,40,0.04)' : undefined }}>
                          <TableCell>
                            <Typography variant="body2" sx={{ fontWeight: 600 }}>{v.registrationNumber || '-'}</Typography>
                            {v.model && <Typography variant="caption" color="text.secondary">{v.model}</Typography>}
                          </TableCell>
                          <TableCell>{v.vehicleType ? v.vehicleType.replace(/_/g, ' ') : '-'}</TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, minWidth: 70 }}>
                              <Box sx={{ flex: 1 }}>
                                <LinearProgress
                                  variant="determinate"
                                  value={Math.min(100, v.fuelLevel || 0)}
                                  sx={{
                                    height: 6, borderRadius: 3, backgroundColor: '#eee',
                                    '& .MuiLinearProgress-bar': {
                                      borderRadius: 3,
                                      backgroundColor: v.lowFuel ? '#C62828' : v.fuelLevel < 50 ? '#F57C00' : '#2E7D32',
                                    },
                                  }}
                                />
                              </Box>
                              <Typography variant="caption" sx={{ fontWeight: 700, color: v.lowFuel ? '#C62828' : 'inherit' }}>{v.fuelLevel}%</Typography>
                            </Box>
                          </TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                              <StatusChip value={v.status} colors={VEHICLE_STATUS_COLORS} />
                              {v.lowFuel && <Chip label="Low fuel" size="small" sx={{ fontWeight: 700, fontSize: '0.62rem', color: '#C62828', backgroundColor: '#C6282818' }} />}
                            </Box>
                          </TableCell>
                          <TableCell>{v.teamName || '-'}</TableCell>
                          <TableCell>
                            <Typography variant="body2" sx={{ fontSize: '0.78rem' }} noWrap title={v.assignedMissionTitle || ''}>
                              {v.assignedMissionTitle || '-'}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2" sx={{ fontSize: '0.78rem' }}>{fmtTime(v.lastMaintainedAt)}</Typography>
                            {v.maintenanceRequired && <Chip label="Maint. due" size="small" sx={{ fontWeight: 700, fontSize: '0.6rem', color: '#F57C00', backgroundColor: '#F57C0018' }} />}
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={v.gpsActive ? 'ON' : 'OFF'}
                              size="small"
                              sx={{ fontWeight: 700, fontSize: '0.62rem', backgroundColor: v.gpsActive ? '#2E7D3218' : '#C6282818', color: v.gpsActive ? '#2E7D32' : '#C62828' }}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Timelines + Alerts */}
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, lg: 7 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 1.5 }}>
                <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                  <TimelineIcon sx={{ fontSize: 20 }} />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Mission Timeline</Typography>
                  <Typography variant="caption" color="text.secondary">Lifecycle milestones of ongoing missions</Typography>
                </Box>
              </Box>
              {(data?.missionTimelines || []).length === 0 ? (
                <EmptyState title="No timelines" message="Mission lifecycle timelines will appear here." />
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {(data?.missionTimelines || []).map(tl => (
                    <Box key={tl.missionId} sx={{ p: 1.5, borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 1, mb: 1 }}>
                        <Typography variant="body2" sx={{ fontWeight: 700 }}>{tl.title}</Typography>
                        <StatusChip value={tl.status} colors={MISSION_STATUS_COLORS} />
                      </Box>
                      <Box sx={{ position: 'relative', pl: 2.25 }}>
                        {tl.milestones.map((ms, i) => {
                          const done = ms.done
                          const isCurrent = !done && i === tl.milestones.findIndex(m => !m.done)
                          const color = done ? '#2E7D32' : isCurrent ? '#F57C00' : '#B0BEC5'
                          return (
                            <Box key={ms.eventType} sx={{ position: 'relative', pb: 1.1 }}>
                              {i < tl.milestones.length - 1 && (
                                <Box sx={{ position: 'absolute', left: -20.5, top: 14, bottom: -4, width: 2, backgroundColor: done ? '#2E7D32' : '#E0E0E0' }} />
                              )}
                              <Box sx={{ position: 'absolute', left: -24.5, top: 2, width: 12, height: 12, borderRadius: '50%', backgroundColor: color, border: '2px solid #fff', boxShadow: '0 0 0 1px ' + color }} />
                              <Typography variant="body2" sx={{ fontSize: '0.8rem', fontWeight: done ? 700 : isCurrent ? 700 : 400, color: done ? '#2E7D32' : isCurrent ? '#F57C00' : 'text.secondary' }}>
                                {ms.label}
                                {ms.timestamp && <Typography component="span" variant="caption" sx={{ ml: 1, color: 'text.disabled' }}>{fmtTime(ms.timestamp)}</Typography>}
                              </Typography>
                            </Box>
                          )
                        })}
                      </Box>
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 5 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 1.5 }}>
                <Box aria-hidden sx={{ width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main' }}>
                  <NotificationsActiveIcon sx={{ fontSize: 20 }} />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ lineHeight: 1.1 }}>Operational Alerts</Typography>
                  <Typography variant="caption" color="text.secondary">{data?.alerts?.length || 0} actionable alerts</Typography>
                </Box>
              </Box>
              {(data?.alerts || []).length === 0 ? (
                <EmptyState title="No alerts" message="All systems are within operational parameters." />
              ) : (
                <List dense disablePadding>
                  {(data?.alerts || []).map((a, i) => {
                    const color = ALERT_COLORS[a.severity] || '#0F4C81'
                    return (
                      <ListItem key={i} disablePadding sx={{ py: 0.75, alignItems: 'flex-start' }}>
                        <ListItemAvatar sx={{ minWidth: 36, mt: 0.25 }}>
                          <Avatar sx={{ width: 26, height: 26, fontSize: '0.65rem', fontWeight: 800, bgcolor: `${color}18`, color }}>
                            {a.severity?.[0] || '•'}
                          </Avatar>
                        </ListItemAvatar>
                        <ListItemText
                          primary={
                            <Typography variant="body2" sx={{ fontSize: '0.8rem' }}>
                              <b>{a.message}</b>
                              {a.entityName && <Typography component="span" variant="caption" sx={{ color: 'text.disabled' }}> · {a.entityName}</Typography>}
                            </Typography>
                          }
                          secondary={
                            <Typography variant="caption" color="text.secondary">
                              {a.category?.replace(/_/g, ' ')} · {fmtTime(a.timestamp)}
                            </Typography>
                          }
                        />
                      </ListItem>
                    )
                  })}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}

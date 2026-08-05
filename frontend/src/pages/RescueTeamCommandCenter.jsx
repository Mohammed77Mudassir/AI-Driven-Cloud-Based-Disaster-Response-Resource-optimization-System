import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import { useWebSocket } from '../context/WebSocketContext'
import { commandCenterApi } from '../services/api'
import toast from 'react-hot-toast'
import EmptyState from '../components/EmptyState'
import { StatsSkeleton } from '../components/LoadingSkeleton'
import {
  Box, Typography, Card, Paper, Grid, Chip, IconButton, Button, LinearProgress,
  Tooltip as MuiTooltip, Divider, Stack
} from '@mui/material'
import RefreshIcon from '@mui/icons-material/Refresh'
import WarningIcon from '@mui/icons-material/Warning'
import ErrorOutlinedIcon from '@mui/icons-material/ErrorOutlined'
import InfoIcon from '@mui/icons-material/Info'
import GroupsIcon from '@mui/icons-material/Groups'
import AssignmentIcon from '@mui/icons-material/Assignment'
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar'
import BatteryAlertIcon from '@mui/icons-material/BatteryAlert'
import ScheduleIcon from '@mui/icons-material/Schedule'
import FlagIcon from '@mui/icons-material/Flag'
import TimerIcon from '@mui/icons-material/Timer'
import SpeedIcon from '@mui/icons-material/Speed'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import BuildIcon from '@mui/icons-material/Build'
import TimelineIcon from '@mui/icons-material/Timeline'
import InsightsIcon from '@mui/icons-material/Insights'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, PieChart, Pie, Cell, BarChart, Bar, Legend } from 'recharts'

const teamStatusColors = {
  AVAILABLE: '#2E7D32',
  DEPLOYED: '#F57C00',
  ON_MISSION: '#0F4C81',
  STANDING_BY: '#F9A825',
  RETURNED: '#607D8B',
  OFF_DUTY: '#757575'
}

const missionStatusColors = {
  PENDING: '#F9A825',
  ASSIGNED: '#0F4C81',
  IN_PROGRESS: '#F57C00',
  COMPLETED: '#2E7D32',
  CANCELLED: '#C62828'
}

const vehicleStatusColors = {
  AVAILABLE: '#2E7D32',
  DEPLOYED: '#0F4C81',
  IN_MAINTENANCE: '#F57C00',
  OUT_OF_SERVICE: '#C62828'
}

const priorityColors = {
  LOW: '#2E7D32',
  MEDIUM: '#F9A825',
  HIGH: '#F57C00',
  CRITICAL: '#C62828'
}

const severityColors = {
  CRITICAL: '#C62828',
  WARNING: '#F57C00',
  INFO: '#0F4C81'
}

const workloadColors = {
  LOW: '#2E7D32',
  MEDIUM: '#F9A825',
  HIGH: '#F57C00',
  CRITICAL: '#C62828'
}

const kpiConfig = {
  TOTAL_TEAMS: { color: '#0F4C81', icon: <GroupsIcon /> },
  TEAMS_ON_MISSION: { color: '#F57C00', icon: <AssignmentIcon /> },
  AVAILABLE_TEAMS: { color: '#2E7D32', icon: <CheckCircleIcon /> },
  TEAMS_RESTING: { color: '#607D8B', icon: <ScheduleIcon /> },
  ACTIVE_MISSIONS: { color: '#F9A825', icon: <FlagIcon /> },
  CRITICAL_MISSIONS: { color: '#C62828', icon: <WarningIcon /> },
  AVG_RESPONSE: { color: '#00897B', icon: <TimerIcon /> },
  AVG_DURATION: { color: '#6A1B9A', icon: <SpeedIcon /> },
  VEHICLE_AVAILABILITY: { color: '#1565C0', icon: <DirectionsCarIcon /> },
  EQUIPMENT_READINESS: { color: '#2E7D32', icon: <BuildIcon /> }
}

const alertIcon = (severity) => {
  if (severity === 'CRITICAL') return <ErrorOutlinedIcon sx={{ color: '#C62828', fontSize: 20 }} />
  if (severity === 'WARNING') return <WarningIcon sx={{ color: '#F57C00', fontSize: 20 }} />
  return <InfoIcon sx={{ color: '#0F4C81', fontSize: 20 }} />
}

const fmt = (iso) => {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

const fmtTime = (iso) => {
  if (!iso) return '-'
  return new Date(iso).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
}

const StatusChip = ({ status, colors, label }) => (
  <Chip label={label || (status || '').replace(/_/g, ' ')} size="small"
    sx={{ fontWeight: 600, backgroundColor: `${colors[status] || '#607D8B'}20`, color: colors[status] || '#607D8B' }} />
)

const SectionCard = ({ title, icon, action, children, minHeight }) => (
  <Paper sx={{ borderRadius: 2, overflow: 'hidden', height: '100%' }}>
    <Box sx={{ p: 2, pb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
      {icon && <Box sx={{ color: '#0F4C81', display: 'flex' }}>{icon}</Box>}
      <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1rem' }}>{title}</Typography>
      {action && <Box sx={{ ml: 'auto' }}>{action}</Box>}
    </Box>
    <Box sx={{ p: 2, pt: 1 }} style={{ minHeight }}>
      {children}
    </Box>
  </Paper>
)

export default function RescueTeamCommandCenter() {
  const { user: currentUser } = useAuth()
  const { connected, lastMessage, subscribe } = useWebSocket()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [teamFilter, setTeamFilter] = useState('ALL')
  const [lastRefreshed, setLastRefreshed] = useState(null)
  const refreshTimerRef = useRef(null)

  const loadDashboard = useCallback(async ({ silent = false } = {}) => {
    if (!silent) setLoading(true)
    try {
      const res = await commandCenterApi.getDashboard()
      if (res?.data) {
        setData(res.data)
        setLastRefreshed(new Date())
      }
    } catch {
      toast.error('Failed to load command center data')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadDashboard()
    const poll = setInterval(() => loadDashboard({ silent: true }), 60000)
    return () => { clearInterval(poll); if (refreshTimerRef.current) clearTimeout(refreshTimerRef.current) }
  }, [loadDashboard])

  // Debounced live refresh — mission/team/location events invalidate the snapshot.
  useEffect(() => {
    if (!subscribe) return
    const scheduleRefresh = () => {
      if (refreshTimerRef.current) clearTimeout(refreshTimerRef.current)
      refreshTimerRef.current = setTimeout(() => loadDashboard({ silent: true }), 1000)
    }
    const unsubs = [
      subscribe('MISSION_CREATED', scheduleRefresh),
      subscribe('MISSION_STATUS', scheduleRefresh),
      subscribe('TEAM_LOCATION', scheduleRefresh),
      subscribe('LOCATION_SNAPSHOT', scheduleRefresh),
      subscribe('DASHBOARD', scheduleRefresh),
    ]
    return () => unsubs.forEach(u => u())
  }, [subscribe, loadDashboard])

  if (loading && !data) return <><StatsSkeleton /><StatsSkeleton /></>

  if (!data) {
    return <EmptyState title="No operational data" message="Command center data could not be loaded. Try refreshing." onAction={() => loadDashboard()} actionLabel="Retry" />
  }

  const kpis = data.kpis || []
  const activeMissions = data.activeMissions || []
  const teams = (data.teams || []).filter(t => teamFilter === 'ALL' || t.status === teamFilter)
  const alerts = data.alerts || []
  const analytics = data.responseAnalytics || {}
  const equipment = data.equipmentReadiness || []
  const vehicles = data.vehicles || []
  const workloads = data.workloads || []
  const timelines = data.missionTimelines || []

  const aggEquipment = [
    { name: 'Available', value: equipment.reduce((s, e) => s + e.available, 0), color: '#2E7D32' },
    { name: 'Deployed', value: equipment.reduce((s, e) => s + e.deployed, 0), color: '#0F4C81' },
    { name: 'In Maintenance', value: equipment.reduce((s, e) => s + e.maintenance, 0), color: '#F57C00' },
    { name: 'Missing', value: equipment.reduce((s, e) => s + e.missing, 0), color: '#C62828' }
  ].filter(d => d.value > 0)

  const workloadChart = workloads.map(w => ({ name: w.teamName, score: w.score }))

  return (
    <Box>
      {/* Header row */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2, flexWrap: 'wrap' }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>Command Center</Typography>
          <Typography variant="body2" color="text.secondary">
            {lastRefreshed ? `Updated ${lastRefreshed.toLocaleTimeString()}` : 'Loading...'}
          </Typography>
        </Box>
        <Chip label={connected ? 'Live' : 'Offline'}
          size="small"
          sx={{ fontWeight: 700, backgroundColor: connected ? '#2E7D3220' : '#C6282820', color: connected ? '#2E7D32' : '#C62828' }} />
        {currentUser?.permissions?.includes('RESCUE_TEAM_VIEW') &&
          <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
            <Button size="small" variant="outlined" startIcon={<RefreshIcon />} onClick={() => loadDashboard()}>Refresh</Button>
          </Box>}
      </Box>

      {/* KPI grid */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {kpis.map(k => {
          const cfg = kpiConfig[k.key] || { color: '#0F4C81', icon: <InsightsIcon /> }
          return (
            <Grid size={{ xs: 12, sm: 6, md: 3, lg: 2.4 }} key={k.key}>
              <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box sx={{ width: 40, height: 40, borderRadius: 2, backgroundColor: `${cfg.color}15`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  {cfg.icon && <Box sx={{ color: cfg.color, display: 'flex' }}>{cfg.icon}</Box>}
                </Box>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="h5" sx={{ fontWeight: 700, lineHeight: 1.1 }}>{k.value}</Typography>
                  <MuiTooltip title={k.hint || ''}>
                    <Typography variant="caption" color="text.secondary" noWrap>{k.label}</Typography>
                  </MuiTooltip>
                </Box>
              </Card>
            </Grid>
          )
        })}
      </Grid>

      {/* Active missions + alerts */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, lg: 7 }}>
          <SectionCard title={`Active Missions (${activeMissions.length})`} icon={<FlagIcon />} minHeight={200}>
            {activeMissions.length === 0 ? (
              <EmptyState title="No active missions" message="Missions assigned to rescue teams will appear here" />
            ) : (
              <Stack spacing={1.5}>
                {activeMissions.map(m => (
                  <Paper key={m.missionId} variant="outlined" sx={{ p: 1.5, borderRadius: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5, flexWrap: 'wrap' }}>
                      <Box sx={{ flex: 1, minWidth: 200 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                          <Typography variant="body1" sx={{ fontWeight: 700 }}>{m.title}</Typography>
                          <Chip label={m.missionCode} size="small" variant="outlined" sx={{ fontWeight: 600 }} />
                        </Box>
                        <Typography variant="caption" color="text.secondary" display="block">
                          {m.disasterName || 'No disaster linked'} · {m.missionType?.replace(/_/g, ' ')} · Started {fmtTime(m.startTime)}
                        </Typography>
                        <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
                          <strong>{m.teamName}</strong>{m.teamLeader ? ` · ${m.teamLeader}` : ''} · {m.assignedVehicle || 'No vehicle assigned'}
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                          <Box sx={{ flex: 1 }}>
                            <LinearProgress variant="determinate" value={m.progress || 0} sx={{ height: 8, borderRadius: 4, backgroundColor: '#E0E0E0' }} />
                          </Box>
                          <Typography variant="caption" fontWeight={700}>{m.progress || 0}%</Typography>
                        </Box>
                      </Box>
                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, alignItems: 'flex-end' }}>
                        <StatusChip status={m.status} colors={missionStatusColors} />
                        <Chip label={m.priority || 'LOW'} size="small" sx={{ fontWeight: 700, backgroundColor: `${priorityColors[m.priority] || '#607D8B'}20`, color: priorityColors[m.priority] || '#607D8B' }} />
                        {m.delayMinutes > 0 && <Chip icon={<WarningIcon fontSize="small" />} label={`Delayed ${m.delayMinutes}m`} size="small" sx={{ fontWeight: 700, backgroundColor: '#C6282820', color: '#C62828' }} />}
                      </Box>
                    </Box>
                  </Paper>
                ))}
              </Stack>
            )}
          </SectionCard>
        </Grid>
        <Grid size={{ xs: 12, lg: 5 }}>
          <SectionCard title={`Operational Alerts (${alerts.length})`} icon={<WarningIcon />} minHeight={200}>
            {alerts.length === 0 ? (
              <EmptyState title="All clear" message="No operational alerts right now" />
            ) : (
              <Stack spacing={1}>
                {alerts.map((a, i) => (
                  <Paper key={`${a.category}-${a.entityId}-${i}`} variant="outlined"
                    sx={{ p: 1.25, borderRadius: 2, display: 'flex', gap: 1.25, alignItems: 'flex-start', backgroundColor: `${severityColors[a.severity]}08` }}>
                    {alertIcon(a.severity)}
                    <Box sx={{ flex: 1, minWidth: 0 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                        <Chip label={a.category?.replace(/_/g, ' ')} size="small" sx={{ fontWeight: 700, backgroundColor: `${severityColors[a.severity]}20`, color: severityColors[a.severity] }} />
                        <Typography variant="caption" color="text.secondary">{fmtTime(a.timestamp)}</Typography>
                      </Box>
                      <Typography variant="body2" sx={{ mt: 0.5 }}>{a.message}</Typography>
                    </Box>
                  </Paper>
                ))}
              </Stack>
            )}
          </SectionCard>
        </Grid>
      </Grid>

      {/* Analytics + readiness */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, lg: 7 }}>
          <SectionCard title="Response Performance (6 months)" icon={<InsightsIcon />} minHeight={260}>
            <Grid container spacing={2} sx={{ mb: 1 }}>
              {[
                { label: 'Avg Response', value: `${analytics.averageResponseMinutes ?? 0} min` },
                { label: 'Fastest', value: `${analytics.fastestResponseMinutes ?? 0} min` },
                { label: 'Slowest', value: `${analytics.slowestResponseMinutes ?? 0} min` },
                { label: 'Completion Rate', value: `${analytics.completionRate ?? 0}%` },
              ].map(s => (
                <Grid size={{ xs: 6, sm: 3 }} key={s.label}>
                  <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderRadius: 2 }}>
                    <Typography variant="h6" sx={{ fontWeight: 700 }}>{s.value}</Typography>
                    <Typography variant="caption" color="text.secondary">{s.label}</Typography>
                  </Paper>
                </Grid>
              ))}
            </Grid>
            <ResponsiveContainer width="100%" height={170}>
              <AreaChart data={(analytics.monthlyTrend || [])} margin={{ top: 5, right: 5, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="respGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0F4C81" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="#0F4C81" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Area type="monotone" dataKey="averageResponseMinutes" name="Avg Response (min)" stroke="#0F4C81" fill="url(#respGrad)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </SectionCard>
        </Grid>
        <Grid size={{ xs: 12, lg: 5 }}>
          <SectionCard title="Equipment Readiness" icon={<BuildIcon />} minHeight={260}>
            <Grid container spacing={2} alignItems="center">
              <Grid size={{ xs: 12, sm: 5 }}>
                <ResponsiveContainer width="100%" height={170}>
                  <PieChart>
                    <Pie data={aggEquipment.length ? aggEquipment : [{ name: 'No data', value: 1, color: '#E0E0E0' }]} dataKey="value" nameKey="name" innerRadius={45} outerRadius={75} paddingAngle={2}>
                      {aggEquipment.map((d, i) => <Cell key={i} fill={d.color} />)}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </Grid>
              <Grid size={{ xs: 12, sm: 7 }}>
                <Stack spacing={1}>
                  {equipment.length === 0 && <Typography variant="body2" color="text.secondary">No equipment assigned</Typography>}
                  {equipment.filter(e => e.assigned > 0).slice(0, 6).map(e => (
                    <Box key={e.teamId}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.25 }}>
                        <Typography variant="caption" sx={{ fontWeight: 600 }}>{e.teamName}</Typography>
                        <Typography variant="caption" color="text.secondary">{e.available}/{e.assigned} ready</Typography>
                      </Box>
                      <LinearProgress variant="determinate" value={e.readinessPercent || 0}
                        sx={{ height: 7, borderRadius: 4, backgroundColor: '#E0E0E0', '& .MuiLinearProgress-bar': { backgroundColor: (e.readinessPercent || 0) >= 70 ? '#2E7D32' : (e.readinessPercent || 0) >= 40 ? '#F9A825' : '#C62828' } }} />
                    </Box>
                  ))}
                </Stack>
              </Grid>
            </Grid>
          </SectionCard>
        </Grid>
      </Grid>

      {/* Team status */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12 }}>
          <SectionCard title={`Team Status (${teams.length})`} icon={<GroupsIcon />}
            action={
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                <Chip label="All" size="small" onClick={() => setTeamFilter('ALL')} sx={{ cursor: 'pointer', fontWeight: teamFilter === 'ALL' ? 700 : 500, backgroundColor: teamFilter === 'ALL' ? '#0F4C8120' : 'transparent', color: teamFilter === 'ALL' ? '#0F4C81' : 'inherit' }} />
                {Object.keys(teamStatusColors).map(s => (
                  <Chip key={s} label={s.replace(/_/g, ' ')} size="small" onClick={() => setTeamFilter(s)} sx={{ cursor: 'pointer', fontWeight: teamFilter === s ? 700 : 500, backgroundColor: teamFilter === s ? `${teamStatusColors[s]}20` : 'transparent', color: teamFilter === s ? teamStatusColors[s] : 'inherit' }} />
                ))}
              </Box>
            }>
            {teams.length === 0 ? (
              <EmptyState title="No teams" message="No rescue teams match the current filter" />
            ) : (
              <Grid container spacing={2}>
                {teams.map(t => (
                  <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={t.teamId}>
                    <Card variant="outlined" sx={{ p: 1.5, borderRadius: 2, height: '100%' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                        <Typography variant="body1" sx={{ fontWeight: 700, noWrap: true }}>{t.teamName}</Typography>
                        <StatusChip status={t.status} colors={teamStatusColors} />
                      </Box>
                      <Typography variant="caption" color="text.secondary" display="block">
                        {t.teamLeader || 'No leader'} · {t.teamSize ?? 0} members{t.maxCapacity ? `/${t.maxCapacity}` : ''}
                      </Typography>
                      <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
                        <strong>{t.specialization || 'Generalist'}</strong>
                      </Typography>
                      <Typography variant="caption" display="block" sx={{ mt: 0.25, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <LocationOnIcon sx={{ fontSize: 14, color: '#607D8B' }} /> {t.location || 'Unknown location'}
                      </Typography>
                      {t.assignedMissionCode ? (
                        <Box sx={{ mt: 1 }}>
                          <Chip size="small" label={`${t.assignedMissionCode} — ${t.assignedMissionTitle}`} sx={{ maxWidth: '100%', fontWeight: 600, backgroundColor: '#0F4C8120', color: '#0F4C81', '& .MuiChip-label': { overflow: 'hidden', textOverflow: 'ellipsis' } }} />
                        </Box>
                      ) : (
                        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>No active mission</Typography>
                      )}
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )}
          </SectionCard>
        </Grid>
      </Grid>

      {/* Fleet + workload */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, lg: 7 }}>
          <SectionCard title={`Fleet Status (${vehicles.length})`} icon={<DirectionsCarIcon />} minHeight={200}>
            {vehicles.length === 0 ? (
              <EmptyState title="No vehicles" message="Registered rescue vehicles will appear here" />
            ) : (
              <Stack spacing={1}>
                {vehicles.map(v => (
                  <Paper key={v.vehicleId} variant="outlined" sx={{ p: 1.25, borderRadius: 2, display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
                    <Box sx={{ width: 38, height: 38, borderRadius: 2, backgroundColor: `${vehicleStatusColors[v.status] || '#607D8B'}15`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                      {v.lowFuel ? <BatteryAlertIcon sx={{ color: '#C62828', fontSize: 20 }} /> : <DirectionsCarIcon sx={{ color: vehicleStatusColors[v.status] || '#607D8B', fontSize: 20 }} />}
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 160 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {v.vehicleType?.replace(/_/g, ' ')} {v.registrationNumber ? `· ${v.registrationNumber}` : ''}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {v.teamName || 'Unassigned'} · Fuel {v.fuelLevel ?? '-'}%{v.assignedMissionTitle ? ` · ${v.assignedMissionTitle}` : ''}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, alignItems: 'flex-end' }}>
                      <StatusChip status={v.status} colors={vehicleStatusColors} />
                      {v.lowFuel && <Chip icon={<BatteryAlertIcon fontSize="small" />} label="Low fuel" size="small" sx={{ fontWeight: 700, backgroundColor: '#C6282820', color: '#C62828' }} />}
                      {v.maintenanceRequired && !v.unavailable && <Chip icon={<BuildIcon fontSize="small" />} label="Maint. due" size="small" sx={{ fontWeight: 700, backgroundColor: '#F57C0020', color: '#F57C00' }} />}
                    </Box>
                  </Paper>
                ))}
              </Stack>
            )}
          </SectionCard>
        </Grid>
        <Grid size={{ xs: 12, lg: 5 }}>
          <SectionCard title="Team Workload" icon={<InsightsIcon />} minHeight={200}>
            {workloads.length === 0 ? (
              <EmptyState title="No workload data" message="Team workloads will appear here" />
            ) : (
              <>
                <ResponsiveContainer width="100%" height={190}>
                  <BarChart data={workloadChart} margin={{ top: 5, right: 5, left: -25, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
                    <XAxis dataKey="name" tick={{ fontSize: 10 }} interval={0} angle={-20} textAnchor="end" height={45} />
                    <YAxis tick={{ fontSize: 11 }} domain={[0, 100]} />
                    <Tooltip />
                    <Bar dataKey="score" name="Workload score" radius={[4, 4, 0, 0]}>
                      {workloads.map(w => <Cell key={w.teamId} fill={workloadColors[w.level] || '#607D8B'} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
                <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', mt: 0.5 }}>
                  {workloads.map(w => (
                    <Chip key={w.teamId} size="small" label={`${w.teamName}: ${w.level}`} sx={{ fontWeight: 600, backgroundColor: `${workloadColors[w.level]}20`, color: workloadColors[w.level] }} />
                  ))}
                </Stack>
              </>
            )}
          </SectionCard>
        </Grid>
      </Grid>

      {/* Mission timelines */}
      <Grid size={{ xs: 12 }} sx={{ mb: 3 }}>
        <SectionCard title="Active Mission Timelines" icon={<TimelineIcon />}>
          {timelines.length === 0 ? (
            <EmptyState title="No mission timelines" message="Timelines for active missions will appear here" />
          ) : (
            <Grid container spacing={3}>
              {timelines.map(t => (
                <Grid size={{ xs: 12, md: 6, xl: 4 }} key={t.missionId}>
                  <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 2, height: '100%' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', mb: 1.5 }}>
                      <Typography variant="body2" sx={{ fontWeight: 700, flex: 1 }}>{t.title}</Typography>
                      <StatusChip status={t.status} colors={missionStatusColors} />
                    </Box>
                    <Stack spacing={0}>
                      {t.milestones.map((m, i) => (
                        <Box key={m.label} sx={{ display: 'flex', gap: 1.25 }}>
                          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                            <Box sx={{ width: 12, height: 12, borderRadius: '50%', flexShrink: 0, backgroundColor: m.done ? '#2E7D32' : '#E0E0E0', border: m.done ? 'none' : '2px solid #BDBDBD', mt: 0.5 }} />
                            {i < t.milestones.length - 1 && <Box sx={{ width: 2, flex: 1, minHeight: 18, backgroundColor: m.done ? '#2E7D32' : '#E0E0E0' }} />}
                          </Box>
                          <Box sx={{ pb: 1.25, minWidth: 0 }}>
                            <Typography variant="body2" sx={{ fontWeight: m.done ? 600 : 400, color: m.done ? 'text.primary' : 'text.secondary' }}>{m.label}</Typography>
                            <Typography variant="caption" color="text.secondary">{m.timestamp ? fmt(m.timestamp) : m.done ? '—' : 'Pending'}</Typography>
                          </Box>
                        </Box>
                      ))}
                    </Stack>
                  </Paper>
                </Grid>
              ))}
            </Grid>
          )}
        </SectionCard>
      </Grid>
    </Box>
  )
}

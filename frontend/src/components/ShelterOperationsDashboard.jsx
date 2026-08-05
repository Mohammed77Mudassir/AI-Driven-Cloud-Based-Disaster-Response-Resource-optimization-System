import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Box, Typography, Chip, Stack, Button, IconButton, Tooltip,
  TextField, Select, MenuItem, FormControl, InputLabel, LinearProgress,
  Alert, Divider
} from '@mui/material'
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ChartTooltip, Cell, PieChart, Pie, Legend } from 'recharts'
import StatCard from './StatCard'
import SectionCard from './SectionCard'
import ChartCard from './ChartCard'
import { StatsSkeleton, ChartSkeleton } from './LoadingSkeleton'
import EmptyState from './EmptyState'
import { shelterOperationsApi } from '../services/api'
import NightShelterIcon from '@mui/icons-material/NightShelter'
import HomeIcon from '@mui/icons-material/Home'
import GroupsIcon from '@mui/icons-material/Groups'
import PersonIcon from '@mui/icons-material/Person'
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom'
import PercentIcon from '@mui/icons-material/Percent'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import ErrorIcon from '@mui/icons-material/Error'
import RestaurantIcon from '@mui/icons-material/Restaurant'
import WaterDropIcon from '@mui/icons-material/WaterDrop'
import MedicalServicesIcon from '@mui/icons-material/MedicalServices'
import BoltIcon from '@mui/icons-material/Bolt'
import WifiIcon from '@mui/icons-material/Wifi'
import WifiOffIcon from '@mui/icons-material/WifiOff'
import WcIcon from '@mui/icons-material/Wc'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import RefreshIcon from '@mui/icons-material/Refresh'
import SearchIcon from '@mui/icons-material/Search'
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff'
import CalendarTodayIcon from '@mui/icons-material/CalendarToday'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import TrendingDownIcon from '@mui/icons-material/TrendingDown'
import TrendingFlatIcon from '@mui/icons-material/TrendingFlat'
import ScheduleIcon from '@mui/icons-material/Schedule'

const STATUS_META = {
  READY: { label: 'Ready', color: 'success', hex: '#2e7d32' },
  BUSY: { label: 'Busy', color: 'warning', hex: '#f9a825' },
  NEAR_CAPACITY: { label: 'Near Capacity', color: 'warning', hex: '#ef6c00' },
  FULL: { label: 'Full', color: 'error', hex: '#d32f2f' }
}

const LEVEL_META = {
  EXCELLENT: { label: 'Excellent', color: 'success', hex: '#2e7d32' },
  GOOD: { label: 'Good', color: 'info', hex: '#0277bd' },
  MODERATE: { label: 'Moderate', color: 'warning', hex: '#f9a825' },
  CRITICAL: { label: 'Critical', color: 'error', hex: '#d32f2f' }
}

const FOOD_META = {
  SUFFICIENT: { label: 'Sufficient', color: 'success', hex: '#2e7d32' },
  LOW: { label: 'Low', color: 'warning', hex: '#f9a825' },
  CRITICAL: { label: 'Critical', color: 'error', hex: '#d32f2f' }
}

const WATER_META = {
  OK: { label: 'OK', color: 'success', hex: '#2e7d32' },
  LOW: { label: 'Low', color: 'warning', hex: '#f9a825' },
  CRITICAL: { label: 'Critical', color: 'error', hex: '#d32f2f' }
}

const POWER_META = {
  AVAILABLE: { label: 'Available', color: 'success', hex: '#2e7d32' },
  BACKUP_MODE: { label: 'Backup Mode', color: 'warning', hex: '#f9a825' },
  POWER_FAILURE: { label: 'Power Failure', color: 'error', hex: '#d32f2f' }
}

const COMM_META = {
  ONLINE: { label: 'Online', color: 'success', hex: '#2e7d32' },
  DEGRADED: { label: 'Degraded', color: 'warning', hex: '#f9a825' },
  OFFLINE: { label: 'Offline', color: 'error', hex: '#d32f2f' }
}

const KPI_META = {
  TOTAL_SHELTERS: { icon: <HomeIcon />, color: 'primary.main' },
  TOTAL_CAPACITY: { icon: <GroupsIcon />, color: 'success.main' },
  CURRENT_OCCUPANCY: { icon: <PersonIcon />, color: 'warning.main' },
  AVAILABLE_SPACE: { icon: <MeetingRoomIcon />, color: 'info.main' },
  OCCUPANCY_RATE: { icon: <PercentIcon />, color: 'error.main' },
  SHELTERS_AVAILABLE: { icon: <CheckCircleIcon />, color: 'secondary.main' },
  SHELTERS_NEAR_FULL: { icon: <WarningAmberIcon />, color: '#ef6c00' },
  FULL_SHELTERS: { icon: <ErrorIcon />, color: 'error.main' }
}

const occupancyColor = (p) => (p <= 60 ? '#2e7d32' : p <= 85 ? '#f9a825' : p <= 95 ? '#ef6c00' : '#d32f2f')
const formatVal = (kpi) => {
  const isPct = kpi.unit === '%'
  const v = isPct ? Math.round(kpi.value * 10) / 10 : Math.round(kpi.value)
  return isPct && kpi.value % 1 !== 0 ? v.toFixed(1) : String(v)
}

const today = () => new Date().toLocaleString()

const TrendChip = ({ trend }) => {
  if (trend === 'RISING') return <Chip size="small" color="warning" icon={<TrendingUpIcon />} label="Rising" />
  if (trend === 'FALLING') return <Chip size="small" color="success" icon={<TrendingDownIcon />} label="Falling" />
  return <Chip size="small" color="default" icon={<TrendingFlatIcon />} label="Stable" />
}

export default function ShelterOperationsDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [lastRefreshed, setLastRefreshed] = useState(today())
  const [autoRefresh, setAutoRefresh] = useState(true)

  // Filters
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('ALL')
  const [city, setCity] = useState('ALL')
  const [foodFilter, setFoodFilter] = useState('ALL')
  const [waterFilter, setWaterFilter] = useState('ALL')
  const [powerFilter, setPowerFilter] = useState('ALL')
  const [commFilter, setCommFilter] = useState('ALL')
  const [readinessFilter, setReadinessFilter] = useState('ALL')
  const [minCapacity, setMinCapacity] = useState('')
  const [sortBy, setSortBy] = useState('name')

  const loadRef = useRef(0)

  const load = useCallback(async () => {
    const token = ++loadRef.current
    try {
      const res = await shelterOperationsApi.getDashboard()
      if (token === loadRef.current) { setData(res.data); setError(false) }
    } catch {
      if (token === loadRef.current) setError(true)
    } finally {
      if (token === loadRef.current) { setLoading(false); setLastRefreshed(today()) }
    }
  }, [])

  useEffect(() => { load() }, [load])

  useEffect(() => {
    if (!autoRefresh) return undefined
    const id = setInterval(load, 60000)
    return () => clearInterval(id)
  }, [autoRefresh, load])

  const cities = useMemo(() => {
    if (!data) return []
    const set = new Set(data.shelters.map(s => (s.address || '').trim()).filter(Boolean))
    return [...set].sort()
  }, [data])

  const filtered = useMemo(() => {
    if (!data) return []
    const q = search.trim().toLowerCase()
    const minCap = parseInt(minCapacity, 10)
    return data.shelters.filter(s => {
      if (q && !`${s.name} ${s.address}`.toLowerCase().includes(q)) return false
      if (status !== 'ALL' && s.status !== status) return false
      if (city !== 'ALL' && (s.address || '').trim() !== city) return false
      if (foodFilter !== 'ALL' && s.foodStatus !== foodFilter) return false
      if (waterFilter !== 'ALL' && s.waterStatus !== waterFilter) return false
      if (powerFilter !== 'ALL' && s.powerStatus !== powerFilter) return false
      if (commFilter !== 'ALL' && s.commStatus !== commFilter) return false
      if (readinessFilter !== 'ALL' && s.readinessLevel !== readinessFilter) return false
      if (!Number.isNaN(minCap) && s.capacity < minCap) return false
      return true
    }).sort((a, b) => {
      switch (sortBy) {
        case 'occupancy': return b.occupancyPercent - a.occupancyPercent
        case 'capacity': return b.capacity - a.capacity
        case 'readiness': return b.readinessScore - a.readinessScore
        case 'food': return a.foodDaysRemaining - b.foodDaysRemaining
        case 'water': return a.waterDaysRemaining - b.waterDaysRemaining
        default: return a.name.localeCompare(b.name)
      }
    })
  }, [data, search, status, city, foodFilter, waterFilter, powerFilter, commFilter, readinessFilter, minCapacity, sortBy])

  const resetFilters = () => {
    setSearch(''); setStatus('ALL'); setCity('ALL'); setFoodFilter('ALL'); setWaterFilter('ALL')
    setPowerFilter('ALL'); setCommFilter('ALL'); setReadinessFilter('ALL'); setMinCapacity(''); setSortBy('name')
  }

  const kpiValue = (key) => data?.kpis?.find(k => k.key === key)

  if (loading) {
    return (
      <Box>
        <StatsSkeleton count={8} />
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 2 }}>
          <ChartSkeleton /><ChartSkeleton />
        </Box>
      </Box>
    )
  }

  if (error || !data) {
    return (
      <SectionCard title="Shelter Operations Dashboard">
        <Alert severity="error" sx={{ mb: 2 }}>Failed to load shelter operations data.</Alert>
        <Button variant="contained" onClick={load}>Retry</Button>
      </SectionCard>
    )
  }

  const kpis = data.kpis || []
  const alerts = data.alerts || []
  const totalOccupancy = data.shelters.reduce((s, x) => s + x.occupancy, 0)
  const totalAvailable = data.shelters.reduce((s, x) => s + x.availableSpace, 0)
  const capacityPie = [
    { name: 'Occupied', value: totalOccupancy, fill: '#ef6c00' },
    { name: 'Available', value: totalAvailable, fill: '#2e7d32' }
  ]
  const powerCounts = (key) => data.shelters.filter(s => s.powerStatus === key).length
  const powerPie = [
    { name: 'Available', value: powerCounts('AVAILABLE'), fill: '#2e7d32' },
    { name: 'Backup Mode', value: powerCounts('BACKUP_MODE'), fill: '#f9a825' },
    { name: 'Power Failure', value: powerCounts('POWER_FAILURE'), fill: '#d32f2f' }
  ]
  const occupancyChart = filtered.map(s => ({ name: s.name, value: s.occupancyPercent }))
  const readinessChart = filtered.map(s => ({ name: s.name, value: s.readinessScore, level: s.readinessLevel }))
  const foodChart = filtered.map(s => ({ name: s.name, value: s.foodDaysRemaining, status: s.foodStatus }))
  const waterChart = filtered.map(s => ({ name: s.name, value: s.waterDaysRemaining, status: s.waterStatus }))
  const medicalChart = filtered.map(s => ({ name: s.name, value: s.medicalReadinessPercent }))
  const forecastChart = filtered.map(s => ({ name: s.name, arrivals: s.dailyArrivals, departures: s.dailyDepartures }))

  return (
    <Box>
      {/* Operational banner */}
      <SectionCard sx={{ mb: 2 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} alignItems={{ md: 'center' }} justifyContent="space-between" spacing={1.5}>
          <Box>
            <Typography variant="subtitle1" fontWeight={700} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <NightShelterIcon color="primary" /> Shelter Operations Command Center
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Live operational snapshot · Last refreshed {lastRefreshed}
            </Typography>
          </Box>
          <Stack direction="row" spacing={1} alignItems="center">
            <Tooltip title="Refresh now">
              <IconButton size="small" onClick={load}><RefreshIcon fontSize="small" /></IconButton>
            </Tooltip>
            <Tooltip title={autoRefresh ? 'Auto-refresh every 60s (on)' : 'Auto-refresh (off)'}>
              <Chip
                size="small"
                color={autoRefresh ? 'success' : 'default'}
                label={`Auto ${autoRefresh ? 'ON' : 'OFF'}`}
                onClick={() => setAutoRefresh(v => !v)}
              />
            </Tooltip>
            <Chip size="small" color="info" variant="outlined" icon={<ScheduleIcon />} label={`${data.shelters.length} shelters`} />
          </Stack>
        </Stack>
      </SectionCard>

      {/* KPI cards */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4, 1fr)' }, gap: 2, mb: 3 }}>
        {kpis.map((kpi, i) => {
          const meta = KPI_META[kpi.key] || { icon: <PercentIcon />, color: 'primary.main' }
          const unit = kpi.unit ? ` ${kpi.unit}` : ''
          return (
            <StatCard
              key={kpi.key}
              icon={meta.icon}
              value={formatVal(kpi)}
              label={`${kpi.label}${unit}`}
              sublabel={kpi.hint || undefined}
              color={meta.color}
              delay={i * 50}
            />
          )
        })}
      </Box>

      {/* Operational alerts */}
      {alerts.length > 0 && (
        <SectionCard title="Operational Alerts" subtitle={`${alerts.length} active`} icon={WarningAmberIcon} sx={{ mb: 3 }}>
          <Stack spacing={1}>
            {alerts.map((a, i) => (
              <Alert
                key={i}
                severity={a.severity === 'CRITICAL' ? 'error' : 'warning'}
                icon={a.severity === 'CRITICAL' ? <ErrorIcon fontSize="inherit" /> : <WarningAmberIcon fontSize="inherit" />}
                sx={{ alignItems: 'center' }}
              >
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems={{ sm: 'center' }} justifyContent="space-between">
                  <Typography variant="body2" fontWeight={600}>{a.message}</Typography>
                  <Stack direction="row" spacing={1}>
                    <Chip size="small" variant="outlined" label={a.category} />
                    <Chip size="small" variant="outlined" color="primary" label={a.shelterName} />
                  </Stack>
                </Stack>
              </Alert>
            ))}
          </Stack>
        </SectionCard>
      )}

      {/* Charts & analytics */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' }, gap: 2, mb: 3 }}>
        <ChartCard title="Occupancy Distribution" subtitle="Occupancy % by shelter (green ≤60 · amber ≤85 · orange ≤95 · red >95)">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={occupancyChart} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="%" domain={[0, 100]} tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v}%`} />
              <Bar dataKey="value" name="Occupancy">
                {occupancyChart.map((e, i) => <Cell key={i} fill={occupancyColor(e.value)} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Capacity Utilization" subtitle="Occupied vs available space across all shelters">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={capacityPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                {capacityPie.map((e, i) => <Cell key={i} fill={e.fill} />)}
              </Pie>
              <Legend />
              <ChartTooltip />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Shelter Readiness" subtitle="Overall readiness score by shelter">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={readinessChart} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="%" domain={[0, 100]} tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v}%`} />
              <Bar dataKey="value" name="Readiness">
                {readinessChart.map((e, i) => <Cell key={i} fill={(LEVEL_META[e.level] || {}).hex || '#0277bd'} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Food Stock Levels" subtitle="Estimated days of food remaining by shelter">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={foodChart} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="d" tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v} days`} />
              <Bar dataKey="value" name="Days remaining">
                {foodChart.map((e, i) => <Cell key={i} fill={(FOOD_META[e.status] || {}).hex || '#d32f2f'} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Water Availability" subtitle="Estimated days of water remaining by shelter">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={waterChart} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="d" tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v} days`} />
              <Bar dataKey="value" name="Days remaining">
                {waterChart.map((e, i) => <Cell key={i} fill={(WATER_META[e.status] || {}).hex || '#d32f2f'} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Medical Kit Readiness" subtitle="Stocked vs minimum required kits by shelter">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={medicalChart} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="%" domain={[0, 100]} tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v}%`} />
              <Bar dataKey="value" name="Readiness" fill="#0277bd" />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Power Status" subtitle="Grid / generator posture across shelters">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={powerPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                {powerPie.map((e, i) => <Cell key={i} fill={e.fill} />)}
              </Pie>
              <Legend />
              <ChartTooltip />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Capacity Forecast" subtitle="Projected daily arrivals vs departures by shelter (estimate)">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={forecastChart} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis tick={{ fontSize: 11 }} />
              <ChartTooltip />
              <Legend />
              <Bar dataKey="arrivals" name="Daily arrivals" fill="#2e7d32" />
              <Bar dataKey="departures" name="Daily departures" fill="#ef6c00" />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      </Box>

      {/* Filters */}
      <SectionCard
        title="Shelter Operations"
        subtitle={`${filtered.length} of ${data.shelters.length} shelters shown`}
        action={
          <Button size="small" startIcon={<FilterAltOffIcon />} onClick={resetFilters}>Reset</Button>
        }
        sx={{ mb: 3 }}
      >
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(5, 1fr)' }, gap: 2, mb: 2.5 }}>
          <TextField
            size="small"
            placeholder="Search by name or city..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            slotProps={{ input: { startAdornment: <SearchIcon fontSize="small" sx={{ mr: 0.5, color: 'text.secondary' }} /> } }}
          />
          <FormControl size="small" fullWidth>
            <InputLabel>Occupancy Status</InputLabel>
            <Select label="Occupancy Status" value={status} onChange={e => setStatus(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {Object.entries(STATUS_META).map(([k, m]) => <MenuItem key={k} value={k}>{m.label}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>City / Location</InputLabel>
            <Select label="City / Location" value={city} onChange={e => setCity(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {cities.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Food Availability</InputLabel>
            <Select label="Food Availability" value={foodFilter} onChange={e => setFoodFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {Object.entries(FOOD_META).map(([k, m]) => <MenuItem key={k} value={k}>{m.label}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Water Availability</InputLabel>
            <Select label="Water Availability" value={waterFilter} onChange={e => setWaterFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {Object.entries(WATER_META).map(([k, m]) => <MenuItem key={k} value={k}>{m.label}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Electricity Status</InputLabel>
            <Select label="Electricity Status" value={powerFilter} onChange={e => setPowerFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {Object.entries(POWER_META).map(([k, m]) => <MenuItem key={k} value={k}>{m.label}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Internet Status</InputLabel>
            <Select label="Internet Status" value={commFilter} onChange={e => setCommFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {Object.entries(COMM_META).map(([k, m]) => <MenuItem key={k} value={k}>{m.label}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Readiness Score</InputLabel>
            <Select label="Readiness Score" value={readinessFilter} onChange={e => setReadinessFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {Object.entries(LEVEL_META).map(([k, m]) => <MenuItem key={k} value={k}>{m.label}</MenuItem>)}
            </Select>
          </FormControl>
          <TextField
            size="small"
            label="Min Capacity"
            type="number"
            value={minCapacity}
            onChange={e => setMinCapacity(e.target.value)}
          />
          <FormControl size="small" fullWidth>
            <InputLabel>Sort By</InputLabel>
            <Select label="Sort By" value={sortBy} onChange={e => setSortBy(e.target.value)}>
              <MenuItem value="name">Name (A–Z)</MenuItem>
              <MenuItem value="capacity">Capacity</MenuItem>
              <MenuItem value="occupancy">Occupancy</MenuItem>
              <MenuItem value="readiness">Readiness Score</MenuItem>
              <MenuItem value="food">Food Days Left</MenuItem>
              <MenuItem value="water">Water Days Left</MenuItem>
            </Select>
          </FormControl>
        </Box>

        {filtered.length === 0 ? (
          <EmptyState title="No shelters match" message="Try adjusting or resetting the filters." />
        ) : (
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr', xl: '1fr 1fr 1fr' }, gap: 2 }}>
            {filtered.map(s => {
              const sm = STATUS_META[s.status] || { label: s.status, color: 'default' }
              const lm = LEVEL_META[s.readinessLevel] || { label: s.readinessLevel, color: 'default' }
              const fm = FOOD_META[s.foodStatus] || { label: s.foodStatus, color: 'default' }
              const wm = WATER_META[s.waterStatus] || { label: s.waterStatus, color: 'default' }
              const pm = POWER_META[s.powerStatus] || { label: s.powerStatus, color: 'default' }
              const cm = COMM_META[s.commStatus] || { label: s.commStatus, color: 'default' }
              const occPct = Math.round(s.occupancyPercent)
              return (
                <SectionCard
                  key={s.id}
                  title={s.name}
                  subtitle={s.address}
                  icon={NightShelterIcon}
                  action={
                    <Stack direction="row" spacing={0.75}>
                      <Chip size="small" color={sm.color} label={sm.label} sx={{ '& .MuiChip-label': { px: 1 } }} />
                      <Chip size="small" color={lm.color} label={`${lm.label} ${Math.round(s.readinessScore)}`} sx={{ '& .MuiChip-label': { px: 1 } }} />
                    </Stack>
                  }
                >
                  {/* Occupancy */}
                  <Box sx={{ mb: 1.5 }}>
                    <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 0.5 }}>
                      <Typography variant="caption" color="text.secondary">
                        Occupancy · {s.occupancy}/{s.capacity} · {s.availableSpace} free
                      </Typography>
                      <Typography variant="caption" fontWeight={700}>{occPct}%</Typography>
                    </Stack>
                    <LinearProgress
                      variant="determinate" value={occPct}
                      sx={{ height: 8, borderRadius: 2, bgcolor: `${occupancyColor(occPct)}22`, '& .MuiLinearProgress-bar': { bgcolor: occupancyColor(occPct) } }}
                    />
                  </Box>

                  {/* Mini metric tiles */}
                  <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1, mb: 1.5 }}>
                    <Box sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: 'action.hover' }}>
                      <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mb: 0.25 }}>
                        <RestaurantIcon fontSize="small" sx={{ color: (FOOD_META[s.foodStatus] || {}).hex }} />
                        <Typography variant="caption" color="text.secondary">Food</Typography>
                      </Stack>
                      <Typography variant="body2" fontWeight={700}>{s.foodPacks} packs</Typography>
                      <Typography variant="caption" color="text.secondary">{s.foodDaysRemaining}d left</Typography>
                      <Box sx={{ mt: 0.5 }}><Chip size="small" color={fm.color} label={fm.label} sx={{ '& .MuiChip-label': { px: 0.75 } }} /></Box>
                    </Box>

                    <Box sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: 'action.hover' }}>
                      <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mb: 0.25 }}>
                        <WaterDropIcon fontSize="small" sx={{ color: (WATER_META[s.waterStatus] || {}).hex }} />
                        <Typography variant="caption" color="text.secondary">Water</Typography>
                      </Stack>
                      <Typography variant="body2" fontWeight={700}>{s.waterLitres} L</Typography>
                      <Typography variant="caption" color="text.secondary">{s.waterDaysRemaining}d left</Typography>
                      <Box sx={{ mt: 0.5 }}><Chip size="small" color={wm.color} label={wm.label} sx={{ '& .MuiChip-label': { px: 0.75 } }} /></Box>
                    </Box>

                    <Box sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: 'action.hover' }}>
                      <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mb: 0.25 }}>
                        <MedicalServicesIcon fontSize="small" sx={{ color: s.medicalReadinessPercent < 50 ? '#d32f2f' : s.medicalReadinessPercent < 75 ? '#f9a825' : '#2e7d32' }} />
                        <Typography variant="caption" color="text.secondary">Medical Kits</Typography>
                      </Stack>
                      <Typography variant="body2" fontWeight={700}>{s.availableMedicalKits} free</Typography>
                      <Typography variant="caption" color="text.secondary">{s.totalMedicalKits} total · {Math.round(s.medicalReadinessPercent)}% ready</Typography>
                    </Box>

                    <Box sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: 'action.hover' }}>
                      <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mb: 0.25 }}>
                        <BoltIcon fontSize="small" sx={{ color: (POWER_META[s.powerStatus] || {}).hex }} />
                        <Typography variant="caption" color="text.secondary">Power</Typography>
                      </Stack>
                      <Typography variant="body2" fontWeight={700}>{pm.label}</Typography>
                      <Typography variant="caption" color="text.secondary">{s.generatorFuelPercent}% fuel · {s.backupRuntimeHours}h backup</Typography>
                    </Box>

                    <Box sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: 'action.hover' }}>
                      <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mb: 0.25 }}>
                        {s.commStatus === 'OFFLINE' ? <WifiOffIcon fontSize="small" color="error" /> : <WifiIcon fontSize="small" sx={{ color: (COMM_META[s.commStatus] || {}).hex }} />}
                        <Typography variant="caption" color="text.secondary">Internet</Typography>
                      </Stack>
                      <Typography variant="body2" fontWeight={700}>{s.networkType}</Typography>
                      <Typography variant="caption" color="text.secondary">{s.signalStrengthPercent}% signal</Typography>
                      <Box sx={{ mt: 0.5 }}><Chip size="small" color={cm.color} label={cm.label} sx={{ '& .MuiChip-label': { px: 0.75 } }} /></Box>
                    </Box>

                    <Box sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: 'action.hover' }}>
                      <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mb: 0.25 }}>
                        <WcIcon fontSize="small" sx={{ color: s.sanitationAvailable ? '#2e7d32' : '#d32f2f' }} />
                        <Typography variant="caption" color="text.secondary">Sanitation</Typography>
                      </Stack>
                      <Typography variant="body2" fontWeight={700}>{s.sanitationAvailable ? 'Operational' : 'At Risk'}</Typography>
                      <Typography variant="caption" color="text.secondary">{s.sanitationPercent}% readiness</Typography>
                    </Box>
                  </Box>

                  {/* Capacity forecast */}
                  <Box sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: 'action.hover', mb: 1 }}>
                    <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 0.5 }}>
                      <Stack direction="row" alignItems="center" spacing={0.75}>
                        <CalendarTodayIcon fontSize="small" color="primary" />
                        <Typography variant="caption" fontWeight={700}>Capacity Forecast</Typography>
                        <Chip size="small" variant="outlined" label="Estimate" sx={{ '& .MuiChip-label': { px: 0.75 } }} />
                      </Stack>
                      <TrendChip trend={s.forecastTrend} />
                    </Stack>
                    <Typography variant="caption" color="text.secondary">
                      +{s.dailyArrivals}/day arrivals · −{s.dailyDepartures}/day departures
                    </Typography>
                    <Box sx={{ mt: 0.5 }}>
                      {s.estimatedDaysToFull == null ? (
                        <Typography variant="body2" color="success.main" fontWeight={600}>No projected full date (net inflow ≤ 0)</Typography>
                      ) : s.estimatedDaysToFull === 0 ? (
                        <Typography variant="body2" color="error.main" fontWeight={600}>Full now · {s.availableSpace} space left</Typography>
                      ) : (
                        <Typography variant="body2" fontWeight={600}>
                          ~{s.estimatedDaysToFull} days to full · {s.estimatedFullDate}
                        </Typography>
                      )}
                    </Box>
                  </Box>

                  <Stack direction="row" alignItems="center" spacing={0.5} sx={{ color: 'text.secondary' }}>
                    <LocationOnIcon fontSize="inherit" />
                    <Typography variant="caption">{s.latitude}, {s.longitude}{s.contact ? ` · ${s.contact}` : ''}</Typography>
                  </Stack>
                </SectionCard>
              )
            })}
          </Box>
        )}
      </SectionCard>

      <Divider sx={{ my: 3 }} />
      <Typography variant="caption" color="text.disabled">
        Metrics are derived from live shelter capacity, occupancy, supplies and utility flags.
        Food/water stock volumes, generator fuel, internet signal, sanitation and the capacity forecast use documented estimates when live telemetry is unavailable.
        Updated automatically every 60 seconds; refresh manually for the latest snapshot.
      </Typography>
    </Box>
  )
}

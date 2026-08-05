import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Box, Typography, Chip, Stack, Button, IconButton, Tooltip,
  TextField, Select, MenuItem, FormControl, InputLabel, LinearProgress,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Alert, Divider
} from '@mui/material'
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ChartTooltip, Cell, PieChart, Pie, Legend } from 'recharts'
import StatCard from './StatCard'
import SectionCard from './SectionCard'
import ChartCard from './ChartCard'
import { StatsSkeleton, TableSkeleton, ChartSkeleton } from './LoadingSkeleton'
import EmptyState from './EmptyState'
import { hospitalOperationsApi } from '../services/api'
import LocalHospitalIcon from '@mui/icons-material/LocalHospital'
import HotelIcon from '@mui/icons-material/Hotel'
import HealingIcon from '@mui/icons-material/Healing'
import MedicalServicesIcon from '@mui/icons-material/MedicalServices'
import EmergencyIcon from '@mui/icons-material/Emergency'
import BloodtypeIcon from '@mui/icons-material/Bloodtype'
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart'
import SpeedIcon from '@mui/icons-material/Speed'
import RefreshIcon from '@mui/icons-material/Refresh'
import SearchIcon from '@mui/icons-material/Search'
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff'
import ScheduleIcon from '@mui/icons-material/Schedule'
import GroupsIcon from '@mui/icons-material/Groups'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'

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

const KPI_META = {
  TOTAL_HOSPITALS: { icon: <LocalHospitalIcon />, color: 'primary.main' },
  AVAILABLE_BEDS: { icon: <HotelIcon />, color: 'success.main' },
  ICU_BEDS: { icon: <HealingIcon />, color: 'warning.main' },
  DOCTORS: { icon: <MedicalServicesIcon />, color: 'info.main' },
  AMBULANCES: { icon: <EmergencyIcon />, color: 'secondary.main' },
  BLOOD_UNITS: { icon: <BloodtypeIcon />, color: 'error.main' },
  EMERGENCY_CAPACITY: { icon: <MonitorHeartIcon />, color: '#00897b' },
  UTILIZATION: { icon: <SpeedIcon />, color: 'primary.main' }
}

const utilColor = (p) => (p <= 60 ? '#2e7d32' : p <= 85 ? '#f9a825' : '#d32f2f')
const formatVal = (kpi) => {
  const isPct = kpi.unit === '%'
  const v = isPct ? Math.round(kpi.value * 10) / 10 : Math.round(kpi.value)
  return isPct && kpi.value % 1 !== 0 ? v.toFixed(1) : String(v)
}

const today = () => new Date().toLocaleString()

export default function HospitalOperationsDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [lastRefreshed, setLastRefreshed] = useState(today())
  const [autoRefresh, setAutoRefresh] = useState(true)

  // Filters
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('ALL')
  const [city, setCity] = useState('ALL')
  const [capacityLevel, setCapacityLevel] = useState('ALL')
  const [icuFilter, setIcuFilter] = useState('ALL')
  const [bedFilter, setBedFilter] = useState('ALL')
  const [bloodFilter, setBloodFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('name')

  const loadRef = useRef(0)

  const load = useCallback(async () => {
    const token = ++loadRef.current
    try {
      const res = await hospitalOperationsApi.getDashboard()
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
    const set = new Set(data.hospitals.map(h => (h.address || '').trim()).filter(Boolean))
    return [...set].sort()
  }, [data])

  const filtered = useMemo(() => {
    if (!data) return []
    const q = search.trim().toLowerCase()
    return data.hospitals.filter(h => {
      if (q && !`${h.name} ${h.address}`.toLowerCase().includes(q)) return false
      if (status !== 'ALL' && h.status !== status) return false
      if (city !== 'ALL' && (h.address || '').trim() !== city) return false
      if (capacityLevel !== 'ALL' && h.emergencyCapacityLevel !== capacityLevel) return false
      if (icuFilter === 'ICU_AVAILABLE' && h.availableIcuBeds <= 0) return false
      if (icuFilter === 'ICU_FULL' && h.availableIcuBeds > 0) return false
      if (bedFilter === 'HAS_BEDS' && h.availableBeds <= 0) return false
      if (bedFilter === 'NO_BEDS' && h.availableBeds > 0) return false
      if (bloodFilter !== 'ALL') {
        if (!h.bloodBankPresent) return false
        if (bloodFilter === 'ADEQUATE' && h.bloodStockStatus !== 'ADEQUATE') return false
        if (bloodFilter === 'LOW' && h.bloodStockStatus !== 'LOW') return false
      }
      return true
    }).sort((a, b) => {
      switch (sortBy) {
        case 'utilization': return b.bedUtilizationPercent - a.bedUtilizationPercent
        case 'icu': return b.icuOccupancyPercent - a.icuOccupancyPercent
        case 'capacity': return b.emergencyCapacityScore - a.emergencyCapacityScore
        case 'beds': return b.availableBeds - a.availableBeds
        default: return a.name.localeCompare(b.name)
      }
    })
  }, [data, search, status, city, capacityLevel, icuFilter, bedFilter, bloodFilter, sortBy])

  const resetFilters = () => {
    setSearch(''); setStatus('ALL'); setCity('ALL'); setCapacityLevel('ALL')
    setIcuFilter('ALL'); setBedFilter('ALL'); setBloodFilter('ALL'); setSortBy('name')
  }

  const kpiValue = (key) => data?.kpis?.find(k => k.key === key)

  if (loading) {
    return (
      <Box>
        <StatsSkeleton count={8} />
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 2 }}>
          <ChartSkeleton /><ChartSkeleton />
        </Box>
        <Box sx={{ mt: 2 }}><TableSkeleton rows={5} /></Box>
      </Box>
    )
  }

  if (error || !data) {
    return (
      <SectionCard title="Hospital Operations Dashboard">
        <Alert severity="error" sx={{ mb: 2 }}>Failed to load hospital operations data.</Alert>
        <Button variant="contained" onClick={load}>Retry</Button>
      </SectionCard>
    )
  }

  const kpis = data.kpis || []
  const lowBloodGroups = (data.bloodGroups || []).filter(g => g.status === 'LOW')
  const bloodChartData = data.bloodGroups.map(g => ({ name: g.group, units: g.units, fill: g.status === 'LOW' ? '#d32f2f' : '#2e7d32' }))
  const bedChartData = filtered.map(h => ({ name: h.name, value: h.bedUtilizationPercent }))
  const icuChartData = filtered.map(h => ({ name: h.name, value: h.icuOccupancyPercent }))
  const capacityChartData = filtered.map(h => ({ name: h.name, value: h.emergencyCapacityScore }))
  const doctorPie = [
    { name: 'On Duty', value: data.doctorSummary?.onDuty || 0, fill: '#2e7d32' },
    { name: 'Off Duty', value: data.doctorSummary?.offDuty || 0, fill: '#ef6c00' }
  ]
  const ambulancePie = [
    { name: 'Available', value: data.ambulanceSummary?.available || 0, fill: '#2e7d32' },
    { name: 'On Emergency', value: data.ambulanceSummary?.onEmergency || 0, fill: '#f9a825' },
    { name: 'Maintenance', value: data.ambulanceSummary?.underMaintenance || 0, fill: '#d32f2f' }
  ]

  return (
    <Box>
      {/* Operational banner */}
      <SectionCard sx={{ mb: 2 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} alignItems={{ md: 'center' }} justifyContent="space-between" spacing={1.5}>
          <Box>
            <Typography variant="subtitle1" fontWeight={700} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <MonitorHeartIcon color="primary" /> Hospital Operations Command Center
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
            <Chip size="small" color="info" variant="outlined" icon={<ScheduleIcon />} label={`${data.hospitals.length} facilities`} />
          </Stack>
        </Stack>
      </SectionCard>

      {/* KPI cards */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4, 1fr)' }, gap: 2, mb: 3 }}>
        {kpis.map((kpi, i) => {
          const meta = KPI_META[kpi.key] || { icon: <SpeedIcon />, color: 'primary.main' }
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

      {/* Doctor + Ambulance summaries */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mb: 3 }}>
        <SectionCard title="Doctor Availability" subtitle="Aggregate medical staffing across facilities" icon={GroupsIcon}>
          <Stack direction="row" spacing={1.5} flexWrap="wrap" useFlexGap sx={{ mb: 1.5 }}>
            <Chip label={`Total: ${data.doctorSummary?.totalDoctors || 0}`} variant="outlined" size="small" />
            <Chip label={`Available: ${data.doctorSummary?.availableDoctors || 0}`} color="success" size="small" />
            <Chip label={`On Duty: ${data.doctorSummary?.onDuty || 0}`} color="info" size="small" />
            <Chip label={`Off Duty: ${data.doctorSummary?.offDuty || 0}`} color="warning" size="small" />
          </Stack>
          <Box sx={{ mb: 0.5, display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant="caption" color="text.secondary">Availability</Typography>
            <Typography variant="caption" fontWeight={700}>{Math.round(data.doctorSummary?.availabilityPercent || 0)}%</Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={data.doctorSummary?.availabilityPercent || 0}
            color={data.doctorSummary?.availabilityPercent >= 60 ? 'success' : 'warning'}
            sx={{ height: 8, borderRadius: 2 }}
          />
        </SectionCard>

        <SectionCard title="Ambulance Management" subtitle="Fleet status from the resource module" icon={EmergencyIcon}>
          <Stack direction="row" spacing={1.5} flexWrap="wrap" useFlexGap sx={{ mb: 1.5 }}>
            <Chip label={`Total: ${data.ambulanceSummary?.total || 0}`} variant="outlined" size="small" />
            <Chip label={`Available: ${data.ambulanceSummary?.available || 0}`} color="success" size="small" />
            <Chip label={`On Emergency: ${data.ambulanceSummary?.onEmergency || 0}`} color="warning" size="small" />
            <Chip label={`Maintenance: ${data.ambulanceSummary?.underMaintenance || 0}`} color="error" size="small" />
          </Stack>
          <Box sx={{ mb: 0.5, display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant="caption" color="text.secondary">Fleet availability</Typography>
            <Typography variant="caption" fontWeight={700}>{Math.round(data.ambulanceSummary?.availabilityPercent || 0)}%</Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={data.ambulanceSummary?.availabilityPercent || 0}
            color={data.ambulanceSummary?.availabilityPercent >= 60 ? 'success' : 'warning'}
            sx={{ height: 8, borderRadius: 2 }}
          />
        </SectionCard>
      </Box>

      {/* Charts */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' }, gap: 2, mb: 3 }}>
        <ChartCard title="Bed Utilization" subtitle="Occupied / total capacity by facility">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={bedChartData} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="%" domain={[0, 100]} tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v}%`} />
              <Bar dataKey="value" name="Utilization">
                {bedChartData.map((e, i) => <Cell key={i} fill={utilColor(e.value)} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="ICU Occupancy" subtitle="ICU pressure by facility (green ≤60 · amber ≤85 · red >85)">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={icuChartData} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="%" domain={[0, 100]} tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v}%`} />
              <Bar dataKey="value" name="ICU Occupancy">
                {icuChartData.map((e, i) => <Cell key={i} fill={utilColor(e.value)} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Emergency Capacity" subtitle="Readiness score by facility">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={capacityChartData} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
              <YAxis unit="%" domain={[0, 100]} tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v}%`} />
              <Bar dataKey="value" name="Score">
                {filtered.map((h, i) => <Cell key={i} fill={(LEVEL_META[h.emergencyCapacityLevel] || {}).hex || '#0277bd'} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Blood Stock Distribution" subtitle="Inventory by blood group (red = low stock)">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={bloodChartData} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <ChartTooltip formatter={(v) => `${v} units`} />
              <Bar dataKey="units" name="Units">
                {bloodChartData.map((e, i) => <Cell key={i} fill={e.fill} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Doctor Availability" subtitle="On duty vs off duty">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={doctorPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                {doctorPie.map((e, i) => <Cell key={i} fill={e.fill} />)}
              </Pie>
              <Legend />
              <ChartTooltip />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Ambulance Status" subtitle="Fleet composition">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={ambulancePie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                {ambulancePie.map((e, i) => <Cell key={i} fill={e.fill} />)}
              </Pie>
              <Legend />
              <ChartTooltip />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>
      </Box>

      {/* Filters */}
      <SectionCard
        title="Hospital Operations"
        subtitle={`${filtered.length} of ${data.hospitals.length} facilities shown`}
        action={
          <Button size="small" startIcon={<FilterAltOffIcon />} onClick={resetFilters}>Reset</Button>
        }
        sx={{ mb: 3 }}
      >
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4, 1fr)' }, gap: 2, mb: 2.5 }}>
          <TextField
            size="small"
            placeholder="Search by name or city..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            slotProps={{ input: { startAdornment: <SearchIcon fontSize="small" sx={{ mr: 0.5, color: 'text.secondary' }} /> } }}
          />
          <FormControl size="small" fullWidth>
            <InputLabel>Hospital Status</InputLabel>
            <Select label="Hospital Status" value={status} onChange={e => setStatus(e.target.value)}>
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
            <InputLabel>Emergency Capacity</InputLabel>
            <Select label="Emergency Capacity" value={capacityLevel} onChange={e => setCapacityLevel(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              {Object.entries(LEVEL_META).map(([k, m]) => <MenuItem key={k} value={k}>{m.label}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>ICU Status</InputLabel>
            <Select label="ICU Status" value={icuFilter} onChange={e => setIcuFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              <MenuItem value="ICU_AVAILABLE">ICU Available</MenuItem>
              <MenuItem value="ICU_FULL">ICU Full</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Bed Availability</InputLabel>
            <Select label="Bed Availability" value={bedFilter} onChange={e => setBedFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              <MenuItem value="HAS_BEDS">Has Beds</MenuItem>
              <MenuItem value="NO_BEDS">No Beds</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Blood Availability</InputLabel>
            <Select label="Blood Availability" value={bloodFilter} onChange={e => setBloodFilter(e.target.value)}>
              <MenuItem value="ALL">All</MenuItem>
              <MenuItem value="ADEQUATE">Adequate</MenuItem>
              <MenuItem value="LOW">Low / Shortage</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" fullWidth>
            <InputLabel>Sort By</InputLabel>
            <Select label="Sort By" value={sortBy} onChange={e => setSortBy(e.target.value)}>
              <MenuItem value="name">Name (A–Z)</MenuItem>
              <MenuItem value="beds">Available Beds</MenuItem>
              <MenuItem value="utilization">Bed Utilization</MenuItem>
              <MenuItem value="icu">ICU Occupancy</MenuItem>
              <MenuItem value="capacity">Emergency Capacity</MenuItem>
            </Select>
          </FormControl>
        </Box>

        {filtered.length === 0 ? (
          <EmptyState title="No hospitals match" message="Try adjusting or resetting the filters." />
        ) : (
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Hospital</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Beds</TableCell>
                  <TableCell>ICU</TableCell>
                  <TableCell>Doctors</TableCell>
                  <TableCell>Waiting</TableCell>
                  <TableCell>Emergency Capacity</TableCell>
                  <TableCell>Blood Bank</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filtered.map(h => {
                  const sm = STATUS_META[h.status] || { label: h.status, color: 'default' }
                  const lm = LEVEL_META[h.emergencyCapacityLevel] || { label: h.emergencyCapacityLevel, color: 'default' }
                  const bedPct = Math.round(h.bedUtilizationPercent)
                  const icuPct = Math.round(h.icuOccupancyPercent)
                  return (
                    <TableRow key={h.id} hover>
                      <TableCell sx={{ maxWidth: 220 }}>
                        <Typography fontWeight={600} noWrap>{h.name}</Typography>
                        <Typography variant="caption" color="text.secondary" noWrap>{h.address}</Typography>
                      </TableCell>
                      <TableCell>
                        <Chip size="small" color={sm.color} label={sm.label} sx={{ '& .MuiChip-label': { px: 1 } }} />
                      </TableCell>
                      <TableCell sx={{ minWidth: 160 }}>
                        <Typography variant="caption">{h.availableBeds}/{h.totalBeds} free</Typography>
                        <LinearProgress
                          variant="determinate" value={bedPct}
                          sx={{ height: 7, borderRadius: 2, bgcolor: `${utilColor(bedPct)}22`, '& .MuiLinearProgress-bar': { bgcolor: utilColor(bedPct) } }}
                        />
                        <Typography variant="caption" color="text.secondary">{bedPct}% utilized</Typography>
                      </TableCell>
                      <TableCell sx={{ minWidth: 160 }}>
                        <Typography variant="caption">{h.availableIcuBeds}/{h.totalIcuBeds} free</Typography>
                        <LinearProgress
                          variant="determinate" value={icuPct}
                          sx={{ height: 7, borderRadius: 2, bgcolor: `${utilColor(icuPct)}22`, '& .MuiLinearProgress-bar': { bgcolor: utilColor(icuPct) } }}
                        />
                        <Typography variant="caption" color="text.secondary">{icuPct}% occupied</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">{h.doctorsAvailable} <Typography component="span" variant="caption" color="text.secondary">/ {h.totalDoctors}</Typography></Typography>
                        <Typography variant="caption" color="text.secondary">{Math.round(h.doctorAvailabilityPercent)}% available</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight={600}>{h.estimatedWaitingTimeMinutes} min</Typography>
                        <Typography variant="caption" color="text.secondary">Load {h.patientLoad}%</Typography>
                      </TableCell>
                      <TableCell>
                        <Chip size="small" color={lm.color} label={`${lm.label} · ${Math.round(h.emergencyCapacityScore)}`} sx={{ '& .MuiChip-label': { px: 1 } }} />
                      </TableCell>
                      <TableCell>
                        {h.bloodBankPresent ? (
                          <>
                            <Chip
                              size="small"
                              icon={<BloodtypeIcon />}
                              color={h.bloodStockStatus === 'LOW' ? 'error' : 'success'}
                              label={`${h.bloodUnits} units`}
                              sx={{ '& .MuiChip-label': { px: 0.5 } }}
                            />
                            {h.bloodStockStatus === 'LOW' && (
                              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.5, color: 'error.main' }}>
                                <WarningAmberIcon fontSize="inherit" />
                                <Typography variant="caption">Shortage</Typography>
                              </Box>
                            )}
                          </>
                        ) : <Typography variant="caption" color="text.disabled">Not available</Typography>}
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </SectionCard>

      {/* Blood bank dashboard */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' }, gap: 2 }}>
        <SectionCard title="Blood Group Availability" subtitle="Aggregate stock across blood banks" icon={BloodtypeIcon}>
          {lowBloodGroups.length > 0 && (
            <Alert severity="warning" icon={<WarningAmberIcon />} sx={{ mb: 1.5 }}>
              Low stock detected for: {lowBloodGroups.map(g => g.group).join(', ')}
            </Alert>
          )}
          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1 }}>
            {(data.bloodGroups || []).map(g => (
              <Stack key={g.group} direction="row" alignItems="center" justifyContent="space-between" sx={{ px: 1.5, py: 1, borderRadius: 1.5, bgcolor: g.status === 'LOW' ? 'error.light' : 'action.hover' }}>
                <Typography variant="body2" fontWeight={700}>{g.group}</Typography>
                <Stack direction="row" alignItems="center" spacing={1}>
                  <Typography variant="caption" color="text.secondary">{g.units} units</Typography>
                  <Chip size="small" color={g.status === 'LOW' ? 'error' : 'success'} label={g.status} sx={{ '& .MuiChip-label': { px: 0.75 } }} />
                </Stack>
              </Stack>
            ))}
          </Box>
        </SectionCard>

        <SectionCard title="Facility Blood Inventory" subtitle="Per-hospital blood bank snapshot" icon={BloodtypeIcon}>
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Hospital</TableCell>
                  <TableCell>Stock</TableCell>
                  <TableCell align="right">Units</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.hospitals.filter(h => h.bloodBankPresent).map(h => (
                  <TableRow key={h.id} hover>
                    <TableCell><Typography noWrap sx={{ maxWidth: 180 }}>{h.name}</Typography></TableCell>
                    <TableCell>
                      <Chip size="small" color={h.bloodStockStatus === 'LOW' ? 'error' : 'success'} label={h.bloodStockStatus === 'LOW' ? 'Low' : 'Adequate'} sx={{ '& .MuiChip-label': { px: 0.75 } }} />
                    </TableCell>
                    <TableCell align="right">{h.bloodUnits} units</TableCell>
                  </TableRow>
                ))}
                {data.hospitals.filter(h => h.bloodBankPresent).length === 0 && (
                  <TableRow><TableCell colSpan={3} sx={{ py: 3 }}><EmptyState title="No blood banks" message="No facility currently reports a blood bank." /></TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
      </Box>

      <Divider sx={{ my: 3 }} />
      <Typography variant="caption" color="text.disabled">
        Metrics are derived from live hospital capacity, doctor staffing and ambulance fleet data.
        Occupancy, ICU pressure, waiting time and blood inventory use documented estimates when live telemetry is unavailable.
        Updated automatically every 60 seconds; refresh manually for the latest snapshot.
      </Typography>
    </Box>
  )
}

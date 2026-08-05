import { useState, useEffect } from 'react'
import { routeApi } from '../services/api'
import toast from 'react-hot-toast'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import SectionCard from '../components/SectionCard'
import MapView from '../components/MapView'
import {
  Box, Typography, Card, Button, TextField, Grid, Chip,
  ToggleButtonGroup, ToggleButton, IconButton, Divider, CircularProgress,
  useTheme, FormControlLabel, Switch, Stack, Alert, Tooltip, MenuItem
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import RouteIcon from '@mui/icons-material/Route'
import SpeedIcon from '@mui/icons-material/Speed'
import StraightenIcon from '@mui/icons-material/Straighten'
import RefreshIcon from '@mui/icons-material/Refresh'
import ClearIcon from '@mui/icons-material/Clear'
import TrafficIcon from '@mui/icons-material/Traffic'
import BlockIcon from '@mui/icons-material/Block'
import EmergencyIcon from '@mui/icons-material/Emergency'
import AltRouteIcon from '@mui/icons-material/AltRoute'
import ReportIcon from '@mui/icons-material/Report'
import ListAltIcon from '@mui/icons-material/ListAlt'
import CompareArrowsIcon from '@mui/icons-material/CompareArrows'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'

const ROUTE_COLORS = ['#1976d2', '#2e7d32', '#d32f2f', '#7b1fa2']
const CLOSURE_TYPES = ['CLOSED', 'BLOCKED', 'FLOODED', 'LANDSLIDE', 'ACCIDENT']
const CLOSURE_COLORS = { CLOSED: '#d32f2f', BLOCKED: '#e65100', FLOODED: '#0277bd', LANDSLIDE: '#6a1b9a', ACCIDENT: '#c2185b' }

const fmt = (v, unit = '') => (v === undefined || v === null || isNaN(v)) ? '-' : `${Number(v).toLocaleString(undefined, { maximumFractionDigits: 1 })} ${unit}`.trim()

function RouteStatusChip({ status, fallback }) {
  if (fallback) return <Chip size="small" icon={<RouteIcon sx={{ fontSize: 14 }} />} label="Offline fallback" sx={{ backgroundColor: '#ff980030', color: '#e65100', fontWeight: 600, fontSize: '0.7rem' }} />
  switch (status) {
    case 'BLOCKED_AVOIDED':
      return <Chip size="small" icon={<BlockIcon sx={{ fontSize: 14 }} />} label="Closures avoided" sx={{ backgroundColor: '#ff980030', color: '#e65100', fontWeight: 600, fontSize: '0.7rem' }} />
    case 'TRAFFIC':
      return <Chip size="small" icon={<TrafficIcon sx={{ fontSize: 14 }} />} label="Traffic aware" sx={{ backgroundColor: '#2e7d3225', color: '#2e7d32', fontWeight: 600, fontSize: '0.7rem' }} />
    default:
      return <Chip size="small" icon={<CheckCircleIcon sx={{ fontSize: 14 }} />} label="OK" sx={{ backgroundColor: '#2e7d3225', color: '#2e7d32', fontWeight: 600, fontSize: '0.7rem' }} />
  }
}

function TrafficChip({ traffic }) {
  if (!traffic) return null
  if (traffic.available) {
    const levelColor = traffic.level === 'HEAVY' ? '#d32f2f' : traffic.level === 'MODERATE' ? '#e65100' : '#2e7d32'
    return <Chip size="small" icon={<TrafficIcon sx={{ fontSize: 14 }} />} label={`Traffic: ${traffic.level || 'OK'}`} sx={{ backgroundColor: `${levelColor}22`, color: levelColor, fontWeight: 600, fontSize: '0.7rem' }} />
  }
  return <Chip size="small" icon={<TrafficIcon sx={{ fontSize: 14 }} />} label="Standard routing" sx={{ backgroundColor: '#88888822', color: '#616161', fontWeight: 600, fontSize: '0.7rem' }} />
}

function StatRow({ label, value }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 0.35 }}>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
      <Typography variant="body2" sx={{ fontWeight: 700 }}>{value}</Typography>
    </Box>
  )
}

export default function RouteOptimization() {
  const theme = useTheme()

  const [startLat, setStartLat] = useState('')
  const [startLng, setStartLng] = useState('')
  const [endLat, setEndLat] = useState('')
  const [endLng, setEndLng] = useState('')
  const [destinations, setDestinations] = useState([])
  const [mode, setMode] = useState('fastest')
  const [emergencyMode, setEmergencyMode] = useState(false)
  const [avoidClosures, setAvoidClosures] = useState(true)
  const [alternativesCount, setAlternativesCount] = useState(2)
  const [optimizeOrder, setOptimizeOrder] = useState(true)

  const [calculating, setCalculating] = useState(false)
  const [optimizing, setOptimizing] = useState(false)
  const [result, setResult] = useState(null)
  const [selectedRouteIdx, setSelectedRouteIdx] = useState(0)
  const [mapLocations, setMapLocations] = useState([])
  const [optimizeResult, setOptimizeResult] = useState(null)

  const [closures, setClosures] = useState([])
  const [loadingClosures, setLoadingClosures] = useState(false)
  const [closureForm, setClosureForm] = useState({
    type: 'CLOSED', latitude: '', longitude: '', radius: '1.5', locationName: '', description: ''
  })

  const loadClosures = async () => {
    setLoadingClosures(true)
    try {
      const res = await routeApi.getClosures()
      setClosures(res.data || [])
    } catch {
      toast.error('Failed to load road closures')
    } finally {
      setLoadingClosures(false)
    }
  }

  useEffect(() => {
    loadClosures()
  }, [])

  const handleAddDestination = () => {
    setDestinations([...destinations, { lat: '', lng: '' }])
  }

  const handleDestChange = (idx, field, value) => {
    const updated = [...destinations]
    updated[idx][field] = value
    setDestinations(updated)
  }

  const handleRemoveDestination = (idx) => {
    setDestinations(destinations.filter((_, i) => i !== idx))
  }

  const buildPayload = () => ({
    start: { latitude: parseFloat(startLat), longitude: parseFloat(startLng) },
    end: { latitude: parseFloat(endLat), longitude: parseFloat(endLng) },
    destinations: destinations.filter(d => d.lat && d.lng).map(d => ({
      latitude: parseFloat(d.lat),
      longitude: parseFloat(d.lng)
    })),
    mode,
    emergencyMode,
    avoidRoadClosures: avoidClosures,
    alternativesCount,
    optimizeOrder
  })

  const buildMapLocations = (locs, closureList) => [
    ...locs,
    ...closureList.map(c => ({
      latitude: c.latitude,
      longitude: c.longitude,
      name: `${c.type}${c.locationName ? ` - ${c.locationName}` : ''}`,
      entityType: 'Road Closure',
      markerColor: 'gray',
      status: c.status
    }))
  ]

  const handleCalculate = async () => {
    if (!startLat || !startLng || !endLat || !endLng) {
      toast.error('Please fill in start and end locations')
      return
    }
    setCalculating(true)
    try {
      const res = await routeApi.calculate(buildPayload())
      const data = res.data
      setResult(data)
      setSelectedRouteIdx(0)

      const locs = [
        { latitude: parseFloat(startLat), longitude: parseFloat(startLng), name: 'Start', entityType: 'Start', markerColor: 'green' },
        ...destinations.filter(d => d.lat && d.lng).map((d, i) => ({
          latitude: parseFloat(d.lat),
          longitude: parseFloat(d.lng),
          name: `Stop ${i + 1}`,
          entityType: 'Waypoint',
          markerColor: 'yellow'
        })),
        { latitude: parseFloat(endLat), longitude: parseFloat(endLng), name: 'End', entityType: 'Destination', markerColor: 'red' },
      ]
      setMapLocations(buildMapLocations(locs, closures))
      toast.success(`Route calculated (${data.provider === 'haversine' ? 'offline' : data.provider})`)
    } catch {
      toast.error('Failed to calculate route')
    } finally {
      setCalculating(false)
    }
  }

  const handleOptimize = async () => {
    const validDests = destinations.filter(d => d.lat && d.lng)
    if (!startLat || !startLng || !endLat || !endLng || validDests.length < 2) {
      toast.error('Add at least 2 waypoints to optimize the visiting order')
      return
    }
    setOptimizing(true)
    try {
      const res = await routeApi.optimize({
        start: { latitude: parseFloat(startLat), longitude: parseFloat(startLng) },
        end: { latitude: parseFloat(endLat), longitude: parseFloat(endLng) },
        destinations: validDests.map(d => ({ latitude: parseFloat(d.lat), longitude: parseFloat(d.lng) }))
      })
      setOptimizeResult(res.data)
      const ordered = res.data.optimizedOrder || []
      if (ordered.length > 0) {
        const withoutEnd = ordered.slice(0, -1).map(p => ({ lat: String(p.latitude), lng: String(p.longitude) }))
        setDestinations(withoutEnd)
        toast.success(`Order optimized - saves ${res.data.savingsKm} km`)
      }
    } catch {
      toast.error('Failed to optimize visit order')
    } finally {
      setOptimizing(false)
    }
  }

  const handleRefresh = () => {
    if (!result) return
    handleCalculate()
  }

  const handleClear = () => {
    setStartLat(''); setStartLng(''); setEndLat(''); setEndLng('')
    setDestinations([]); setResult(null); setOptimizeResult(null)
    setMapLocations([]); setSelectedRouteIdx(0)
  }

  const handleAddClosure = async () => {
    if (!closureForm.latitude || !closureForm.longitude) {
      toast.error('Enter closure latitude and longitude')
      return
    }
    try {
      await routeApi.createClosure({
        type: closureForm.type,
        latitude: parseFloat(closureForm.latitude),
        longitude: parseFloat(closureForm.longitude),
        avoidanceRadiusKm: parseFloat(closureForm.radius) || 1.5,
        locationName: closureForm.locationName || undefined,
        description: closureForm.description || undefined
      })
      setClosureForm({ type: 'CLOSED', latitude: '', longitude: '', radius: '1.5', locationName: '', description: '' })
      await loadClosures()
      toast.success('Road closure reported - routes will avoid it')
    } catch {
      toast.error('Failed to report road closure')
    }
  }

  const handleDeleteClosure = async (id) => {
    try {
      await routeApi.deleteClosure(id)
      await loadClosures()
      toast.success('Road closure cleared')
    } catch {
      toast.error('Failed to clear road closure')
    }
  }

  const routes = (result?.alternatives || []).map((alt, i) => ({
    points: (alt.geometry || []).map(([lat, lng]) => [lat, lng]),
    color: ROUTE_COLORS[i % ROUTE_COLORS.length],
    weight: 6,
    dashArray: alt.preference === 'alternative' ? '8 6' : undefined,
    selected: i === selectedRouteIdx,
    popup: { title: alt.label, body: `${fmt(alt.distanceKm, 'km')} · ${fmt(alt.timeMinutes, 'min')}` }
  }))

  const selectedRoute = result?.alternatives?.[selectedRouteIdx]
  const traffic = result?.traffic

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Route Optimization"
          subtitle="Enterprise emergency navigation - road routing, traffic-aware ETA, alternate routes and closure avoidance"
          actions={
            <>
              <Button size="small" variant="outlined" startIcon={<RefreshIcon />} onClick={handleRefresh} disabled={!result}>
                Refresh
              </Button>
              <Button size="small" variant="outlined" color="error" startIcon={<ClearIcon />} onClick={handleClear}>
                Clear
              </Button>
            </>
          }
        />

        {result?.fallback && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Routing provider unavailable - using offline Haversine estimates. Distances are straight-line and ETA is an estimate.
          </Alert>
        )}

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 4 }}>
            <SectionCard title="Route Details" icon={RouteIcon}>
              <Typography variant="subtitle2" sx={{ mb: 1, color: theme.palette.primary.main, fontWeight: 600 }}>Start Location</Typography>
              <Grid container spacing={1} sx={{ mb: 2 }}>
                <Grid size={{ xs: 6 }}>
                  <TextField label="Latitude" size="small" fullWidth type="number" value={startLat} onChange={e => setStartLat(e.target.value)} placeholder="e.g. 28.6139" />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField label="Longitude" size="small" fullWidth type="number" value={startLng} onChange={e => setStartLng(e.target.value)} placeholder="e.g. 77.2090" />
                </Grid>
              </Grid>

              <Typography variant="subtitle2" sx={{ mb: 1, color: theme.palette.error.main, fontWeight: 600 }}>End Location</Typography>
              <Grid container spacing={1} sx={{ mb: 2 }}>
                <Grid size={{ xs: 6 }}>
                  <TextField label="Latitude" size="small" fullWidth type="number" value={endLat} onChange={e => setEndLat(e.target.value)} placeholder="e.g. 19.0760" />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField label="Longitude" size="small" fullWidth type="number" value={endLng} onChange={e => setEndLng(e.target.value)} placeholder="e.g. 72.8777" />
                </Grid>
              </Grid>

              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Waypoints</Typography>
                <Box>
                  <Button size="small" startIcon={<AddIcon />} onClick={handleAddDestination} variant="outlined" sx={{ fontSize: '0.75rem', mr: 1 }}>
                    Add
                  </Button>
                  <Tooltip title="Optimize the visiting order to minimize distance & time">
                    <Button size="small" startIcon={<CompareArrowsIcon />} onClick={handleOptimize} variant="outlined" color="success" sx={{ fontSize: '0.75rem' }} disabled={optimizing}>
                      {optimizing ? <CircularProgress size={14} color="inherit" /> : 'Optimize'}
                    </Button>
                  </Tooltip>
                </Box>
              </Box>

              {destinations.map((d, i) => (
                <Box key={i} sx={{ mb: 1.5, p: 1.5, backgroundColor: 'action.hover', borderRadius: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                    <Typography variant="caption" sx={{ fontWeight: 600, color: theme.palette.accent.main }}>Stop {i + 1}</Typography>
                    <IconButton size="small" onClick={() => handleRemoveDestination(i)} sx={{ color: theme.palette.error.main }}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                  <Grid container spacing={1}>
                    <Grid size={{ xs: 6 }}>
                      <TextField label="Lat" size="small" fullWidth type="number" value={d.lat} onChange={e => handleDestChange(i, 'lat', e.target.value)} />
                    </Grid>
                    <Grid size={{ xs: 6 }}>
                      <TextField label="Lng" size="small" fullWidth type="number" value={d.lng} onChange={e => handleDestChange(i, 'lng', e.target.value)} />
                    </Grid>
                  </Grid>
                </Box>
              ))}

              <Divider sx={{ my: 2 }} />

              <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>Optimization Mode</Typography>
              <ToggleButtonGroup
                value={mode}
                exclusive
                onChange={(_, v) => v && setMode(v)}
                fullWidth
                size="small"
                sx={{ mb: 1.5 }}
              >
                <ToggleButton value="fastest">
                  <SpeedIcon sx={{ mr: 0.5, fontSize: 16 }} /> Fastest
                </ToggleButton>
                <ToggleButton value="shortest">
                  <StraightenIcon sx={{ mr: 0.5, fontSize: 16 }} /> Shortest
                </ToggleButton>
              </ToggleButtonGroup>

              <FormControlLabel
                control={<Switch size="small" checked={emergencyMode} onChange={e => setEmergencyMode(e.target.checked)} />}
                label={<Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}><EmergencyIcon sx={{ fontSize: 16, color: theme.palette.error.main }} /> Emergency Mode</Box>}
                sx={{ mb: 0.5 }}
              />
              <FormControlLabel
                control={<Switch size="small" checked={avoidClosures} onChange={e => setAvoidClosures(e.target.checked)} />}
                label={<Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}><BlockIcon sx={{ fontSize: 16, color: theme.palette.accent.main }} /> Avoid Road Closures</Box>}
                sx={{ mb: 0.5 }}
              />
              <FormControlLabel
                control={<Switch size="small" checked={optimizeOrder} onChange={e => setOptimizeOrder(e.target.checked)} />}
                label={<Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}><AltRouteIcon sx={{ fontSize: 16, color: theme.palette.primary.main }} /> Optimize Visit Order</Box>}
                sx={{ mb: 1.5 }}
              />

              <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>Alternate Routes</Typography>
              <TextField
                select
                size="small"
                fullWidth
                value={alternativesCount}
                onChange={e => setAlternativesCount(Number(e.target.value))}
                sx={{ mb: 2 }}
              >
                {[1, 2, 3].map(n => (
                  <MenuItem key={n} value={n}>{n === 1 ? 'Single route' : `${n} routes (fastest + ${n - 1} alt)`}</MenuItem>
                ))}
              </TextField>

              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={handleCalculate}
                disabled={calculating}
                startIcon={calculating ? <CircularProgress size={18} color="inherit" /> : <RouteIcon />}
                sx={{ py: 1.2 }}
              >
                {calculating ? 'Calculating...' : 'Calculate Route'}
              </Button>

              {optimizeResult && (
                <Card sx={{ mt: 2, p: 2, backgroundColor: '#2e7d3210', border: `1px solid ${theme.palette.divider}` }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#2e7d32', mb: 1 }}>
                    <CompareArrowsIcon sx={{ fontSize: 16, verticalAlign: 'middle', mr: 0.5 }} />
                    Optimized Visit Order
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 1 }}>
                    {optimizeResult.orderLabels.map((label, i) => (
                      <Chip key={i} size="small" label={`${i + 1}. ${label}`} sx={{ fontSize: '0.68rem', backgroundColor: `${ROUTE_COLORS[i % ROUTE_COLORS.length]}22`, color: ROUTE_COLORS[i % ROUTE_COLORS.length], fontWeight: 600 }} />
                    ))}
                  </Box>
                  <StatRow label="Original distance" value={fmt(optimizeResult.originalDistanceKm, 'km')} />
                  <StatRow label="Optimized distance" value={fmt(optimizeResult.optimizedDistanceKm, 'km')} />
                  <StatRow label="Savings" value={`${fmt(optimizeResult.savingsKm, 'km')} (${fmt(optimizeResult.savingsPercent, '%')})`} />
                  <Typography variant="caption" color="text.secondary">Algorithm: {optimizeResult.algorithm}</Typography>
                </Card>
              )}

              {result && (
                <Card sx={{ mt: 2, p: 2, backgroundColor: theme.palette.action.hover, border: `1px solid ${theme.palette.divider}` }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 600, color: theme.palette.primary.main }}>Route Details Panel</Typography>
                    <RouteStatusChip status={result.routeStatus} fallback={result.fallback} />
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="body2" color="text.secondary">Total Distance:</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>{fmt(selectedRoute?.distanceKm, 'km')}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">Est. Time:</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>{fmt(selectedRoute?.timeMinutes, 'min')}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 0.5 }}>
                    <Typography variant="body2" color="text.secondary">Traffic:</Typography>
                    <TrafficChip traffic={traffic} />
                  </Box>
                  <Divider sx={{ my: 1 }} />
                  <StatRow label="Average speed" value={fmt(selectedRoute?.averageSpeedKmph, 'km/h')} />
                  <StatRow label="Turns" value={selectedRoute?.turnCount ?? '-'} />
                  <StatRow label="Waypoints" value={result.waypoints?.length ?? '-'} />
                  <StatRow label="Routing provider" value={<Chip size="small" label={result.provider || '-'} sx={{ backgroundColor: '#1976d220', color: '#1976d2', fontWeight: 600, fontSize: '0.68rem' }} />} />
                  <StatRow label="Route status" value={result.routeStatus || '-'} />
                  <StatRow label="Last updated" value={result.lastUpdated ? new Date(result.lastUpdated).toLocaleTimeString() : '-'} />
                  {traffic && !traffic.available && (
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                      {traffic.message}
                    </Typography>
                  )}
                </Card>
              )}
            </SectionCard>
          </Grid>

          <Grid size={{ xs: 12, md: 8 }}>
            <SectionCard title="Route Map" icon={RouteIcon} sx={{ minHeight: 500 }}>
              {mapLocations.length > 0 || routes.length > 0 ? (
                <MapView
                  locations={mapLocations}
                  routes={routes}
                  fitToRoutes
                  center={mapLocations.length > 0
                    ? [mapLocations.reduce((s, l) => s + l.latitude, 0) / mapLocations.length,
                       mapLocations.reduce((s, l) => s + l.longitude, 0) / mapLocations.length]
                    : [20.5937, 78.9629]}
                  zoom={6}
                  height="500px"
                />
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: 450, color: theme.palette.text.disabled }}>
                  <RouteIcon sx={{ fontSize: 64, mb: 2 }} />
                  <Typography variant="body1" color="text.secondary">Enter route details and calculate to see the road map</Typography>
                </Box>
              )}
            </SectionCard>

            {result && result.alternatives?.length > 0 && (
              <SectionCard title="Route Comparison" subtitle="Select a preferred route - fastest, shortest or alternative" icon={CompareArrowsIcon} sx={{ mt: 3 }}>
                <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} useFlexGap>
                  {result.alternatives.map((alt, i) => {
                    const selected = i === selectedRouteIdx
                    return (
                      <Card
                        key={alt.index}
                        onClick={() => setSelectedRouteIdx(i)}
                        sx={{
                          flex: 1, p: 1.75, cursor: 'pointer',
                          border: `2px solid ${selected ? ROUTE_COLORS[i % ROUTE_COLORS.length] : theme.palette.divider}`,
                          backgroundColor: selected ? `${ROUTE_COLORS[i % ROUTE_COLORS.length]}0d` : theme.palette.background.paper,
                          transition: 'all 0.15s ease', '&:hover': { boxShadow: 3 }
                        }}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 0.75 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                            <Box sx={{ width: 14, height: 14, borderRadius: '50%', backgroundColor: ROUTE_COLORS[i % ROUTE_COLORS.length] }} />
                            <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>{alt.label}</Typography>
                          </Box>
                          {alt.recommended && <Chip size="small" label="Recommended" sx={{ backgroundColor: '#2e7d3225', color: '#2e7d32', fontWeight: 700, fontSize: '0.62rem' }} />}
                        </Box>
                        {alt.summary && <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.75 }}>{alt.summary}</Typography>}
                        <StatRow label="Distance" value={fmt(alt.distanceKm, 'km')} />
                        <StatRow label="Time" value={fmt(alt.timeMinutes, 'min')} />
                        <StatRow label="Turns" value={alt.turnCount ?? '-'} />
                        {alt.trafficAware && alt.delayMinutes > 0 && (
                          <StatRow label="Traffic delay" value={`+${fmt(alt.delayMinutes, 'min')}`} />
                        )}
                        {alt.blockedRoadsAvoided && (
                          <Chip size="small" icon={<BlockIcon sx={{ fontSize: 13 }} />} label="Closures avoided" sx={{ mt: 1, backgroundColor: '#ff980030', color: '#e65100', fontWeight: 600, fontSize: '0.65rem' }} />
                        )}
                        {selected && (
                          <Button size="small" variant="contained" sx={{ mt: 1.25, backgroundColor: ROUTE_COLORS[i % ROUTE_COLORS.length], '&:hover': { backgroundColor: ROUTE_COLORS[i % ROUTE_COLORS.length] } }} fullWidth>
                            Selected Route
                          </Button>
                        )}
                      </Card>
                    )
                  })}
                </Stack>

                {selectedRoute?.steps?.length > 0 && (
                  <Card sx={{ mt: 2, p: 2, backgroundColor: theme.palette.action.hover }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, mb: 1 }}>
                      <ListAltIcon sx={{ fontSize: 18, color: theme.palette.primary.main }} />
                      <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Turn-by-turn directions - {selectedRoute.label}</Typography>
                    </Box>
                    <Stack spacing={0.5}>
                      {selectedRoute.steps.map((step, si) => (
                        <Typography key={si} variant="body2" color="text.secondary">
                          <Box component="span" sx={{ display: 'inline-block', width: 18, fontWeight: 700, color: ROUTE_COLORS[selectedRouteIdx % ROUTE_COLORS.length] }}>{si + 1}.</Box> {step}
                        </Typography>
                      ))}
                    </Stack>
                  </Card>
                )}
              </SectionCard>
            )}

            <SectionCard title="Road Closures" subtitle="Report closures, blockages, floods, landslides or accidents - the engine routes around them" icon={ReportIcon} sx={{ mt: 3 }}>
              <Grid container spacing={1.5} sx={{ mb: 2 }}>
                <Grid size={{ xs: 6, md: 3 }}>
                  <TextField
                    select size="small" fullWidth label="Type" value={closureForm.type}
                    onChange={e => setClosureForm({ ...closureForm, type: e.target.value })}
                  >
                    {CLOSURE_TYPES.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
                  </TextField>
                </Grid>
                <Grid size={{ xs: 6, md: 2 }}>
                  <TextField size="small" fullWidth type="number" label="Latitude" value={closureForm.latitude} onChange={e => setClosureForm({ ...closureForm, latitude: e.target.value })} />
                </Grid>
                <Grid size={{ xs: 6, md: 2 }}>
                  <TextField size="small" fullWidth type="number" label="Longitude" value={closureForm.longitude} onChange={e => setClosureForm({ ...closureForm, longitude: e.target.value })} />
                </Grid>
                <Grid size={{ xs: 6, md: 2 }}>
                  <TextField size="small" fullWidth type="number" label="Radius (km)" value={closureForm.radius} onChange={e => setClosureForm({ ...closureForm, radius: e.target.value })} />
                </Grid>
                <Grid size={{ xs: 12, md: 3 }}>
                  <Button variant="contained" fullWidth sx={{ height: 40 }} startIcon={<AddIcon />} onClick={handleAddClosure}>
                    Report Closure
                  </Button>
                </Grid>
                <Grid size={{ xs: 6, md: 4 }}>
                  <TextField size="small" fullWidth label="Location name" value={closureForm.locationName} onChange={e => setClosureForm({ ...closureForm, locationName: e.target.value })} />
                </Grid>
                <Grid size={{ xs: 6, md: 8 }}>
                  <TextField size="small" fullWidth label="Description" value={closureForm.description} onChange={e => setClosureForm({ ...closureForm, description: e.target.value })} />
                </Grid>
              </Grid>

              {loadingClosures ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}><CircularProgress size={24} /></Box>
              ) : closures.length === 0 ? (
                <Typography variant="body2" color="text.secondary">No active road closures. Routes will use all available roads.</Typography>
              ) : (
                <Stack spacing={1}>
                  {closures.map(c => (
                    <Box key={c.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, p: 1.25, borderRadius: 1, backgroundColor: 'action.hover' }}>
                      <Box sx={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: CLOSURE_COLORS[c.type] || '#888', flexShrink: 0 }} />
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Typography variant="body2" sx={{ fontWeight: 700 }}>
                          {c.type}
                          {c.locationName && <Box component="span" color="text.secondary" sx={{ fontWeight: 400 }}> - {c.locationName}</Box>}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {c.latitude.toFixed(4)}, {c.longitude.toFixed(4)} · avoid {c.avoidanceRadiusKm} km
                          {c.reportedAt && <> · {new Date(c.reportedAt).toLocaleString()}</>}
                        </Typography>
                      </Box>
                      <IconButton size="small" onClick={() => handleDeleteClosure(c.id)} sx={{ color: theme.palette.error.main }} title="Clear closure">
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ))}
                </Stack>
              )}
            </SectionCard>
          </Grid>
        </Grid>

        <Footer />
      </Box>
    </Box>
  )
}

import { useState, useEffect, useCallback } from 'react'
import Sidebar from '../components/Sidebar'
import LiveMap from '../components/LiveMap'
import WebSocketStatus from '../components/WebSocketStatus'
import { useWebSocket } from '../context/WebSocketContext'
import Breadcrumbs from '../components/Breadcrumbs'
import Footer from '../components/Footer'
import { monitoringApi } from '../services/api'
import {
  Box, Typography, ToggleButton, ToggleButtonGroup, Chip, Card, CardContent,
  FormControl, InputLabel, Select, MenuItem, Button, IconButton, Paper, Stack,
  CircularProgress, Tooltip
} from '@mui/material'
import MapIcon from '@mui/icons-material/Map'
import TerrainIcon from '@mui/icons-material/Terrain'
import SatelliteIcon from '@mui/icons-material/Satellite'
import RouteIcon from '@mui/icons-material/Route'
import LayersIcon from '@mui/icons-material/Layers'
import WhatshotIcon from '@mui/icons-material/Whatshot'
import ClearIcon from '@mui/icons-material/Clear'
import CloseIcon from '@mui/icons-material/Close'

const LEGEND = [
  { label: 'Critical', color: '#ff4444' },
  { label: 'High', color: '#ff8800' },
  { label: 'Medium', color: '#ffbb33' },
  { label: 'Low/OK', color: '#00C851' },
  { label: 'Hospital', color: '#33b5e5' },
  { label: 'Shelter', color: '#00897b' },
  { label: 'Drone', color: '#888' },
]

export default function MapPage() {
  const [mapType, setMapType] = useState('street')
  const [locations, setLocations] = useState([])
  const [heatPoints, setHeatPoints] = useState([])
  const [clusterEnabled, setClusterEnabled] = useState(true)
  const [heatEnabled, setHeatEnabled] = useState(false)
  const [loading, setLoading] = useState(true)
  const [liveMode, setLiveMode] = useState(false)

  const [fromId, setFromId] = useState('')
  const [toId, setToId] = useState('')
  const [route, setRoute] = useState(null)
  const [routeLoading, setRouteLoading] = useState(false)

  const { connected, subscribe } = useWebSocket()

  const loadLocations = useCallback(async () => {
    try {
      const [locRes, heatRes] = await Promise.all([
        monitoringApi.getLocations(),
        monitoringApi.getHeatmap(),
      ])
      setLocations(locRes.data || [])
      setHeatPoints(heatRes.data || [])
    } catch {} finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadLocations() }, [loadLocations])

  useEffect(() => {
    const interval = setInterval(() => {
      if (!connected) loadLocations()
    }, 15000)
    return () => clearInterval(interval)
  }, [connected, loadLocations])

  useEffect(() => {
    const unsubSnapshot = subscribe('LOCATION_SNAPSHOT', (data) => {
      if (Array.isArray(data)) setLocations(data)
      setLiveMode(true)
    })
    const unsubDrone = subscribe('DRONE_LOCATION', (data) => {
      if (!Array.isArray(data)) return
      setLocations(prev => {
        const droneIds = new Set(data.map(d => d.entityId))
        const rest = prev.filter(l => l.entityType !== 'DRONE' || !droneIds.has(l.entityId))
        return [...rest, ...data]
      })
    })
    const unsubDisaster = subscribe('DISASTER', () => loadLocations())
    return () => {
      unsubSnapshot()
      unsubDrone()
      unsubDisaster()
    }
  }, [subscribe, loadLocations])

  const computeRoute = async () => {
    const from = locations.find(l => `${l.entityType}-${l.entityId}` === fromId)
    const to = locations.find(l => `${l.entityType}-${l.entityId}` === toId)
    if (!from || !to) return
    setRouteLoading(true)
    try {
      const res = await monitoringApi.getRoute({
        fromLat: from.latitude,
        fromLng: from.longitude,
        toLat: to.latitude,
        toLng: to.longitude,
        steps: 40,
        speedKmph: 50,
      })
      setRoute(res.data)
    } catch {
      setRoute(null)
    } finally {
      setRouteLoading(false)
    }
  }

  const locationOptions = locations.filter(l => l.latitude && l.longitude)

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <Typography>Loading map...</Typography>
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <Breadcrumbs />
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box>
            <Typography variant="h4" fontWeight={700}>Live Map</Typography>
            <Typography variant="body2" color="text.secondary">
              Real-time disaster and resource mapping {liveMode && connected ? '· streaming live updates' : ''}
            </Typography>
          </Box>
          <WebSocketStatus connected={connected} />
        </Box>

        <Stack direction="row" spacing={1.5} sx={{ mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
          <ToggleButtonGroup
            value={mapType} exclusive size="small"
            onChange={(_, val) => val && setMapType(val)}
          >
            <ToggleButton value="street"><MapIcon sx={{ mr: 0.5, fontSize: 18 }} />Street</ToggleButton>
            <ToggleButton value="terrain"><TerrainIcon sx={{ mr: 0.5, fontSize: 18 }} />Terrain</ToggleButton>
            <ToggleButton value="satellite"><SatelliteIcon sx={{ mr: 0.5, fontSize: 18 }} />Satellite</ToggleButton>
          </ToggleButtonGroup>

          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <Tooltip title="Group nearby markers when zoomed out">
              <Chip
                icon={<LayersIcon sx={{ fontSize: 15 }} />}
                label="Clusters"
                size="small"
                color={clusterEnabled ? 'primary' : 'default'}
                variant={clusterEnabled ? 'filled' : 'outlined'}
                onClick={() => setClusterEnabled(c => !c)}
                clickable
              />
            </Tooltip>
            <Tooltip title="Show severity heat map overlay">
              <Chip
                icon={<WhatshotIcon sx={{ fontSize: 15 }} />}
                label="Heat"
                size="small"
                color={heatEnabled ? 'error' : 'default'}
                variant={heatEnabled ? 'filled' : 'outlined'}
                onClick={() => setHeatEnabled(h => !h)}
                clickable
              />
            </Tooltip>
          </Box>

          <Box sx={{ flex: 1 }} />

          {LEGEND.map(l => (
            <Chip
              key={l.label}
              icon={<Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: l.color }} />}
              label={l.label}
              size="small"
              variant="outlined"
            />
          ))}
        </Stack>

        <Card>
          <CardContent sx={{ p: 1 }}>
            <Box sx={{ position: 'relative' }}>
              <LiveMap
                locations={locations}
                heatPoints={heatPoints}
                route={route}
                height="600px"
                mapType={mapType}
                clusterEnabled={clusterEnabled}
                heatEnabled={heatEnabled}
              />

              {route && (
                <Paper
                  elevation={6}
                  sx={{
                    position: 'absolute', top: 16, left: 16, p: 2, borderRadius: 2,
                    maxWidth: 260, zIndex: 1000
                  }}
                >
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography variant="subtitle2" fontWeight={700} sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <RouteIcon fontSize="small" color="primary" /> Route Summary
                    </Typography>
                    <IconButton size="small" onClick={() => setRoute(null)}>
                      <CloseIcon fontSize="small" />
                    </IconButton>
                  </Box>
                  <Box sx={{ display: 'flex', gap: 2, mb: 0.5 }}>
                    <Box>
                      <Typography variant="caption" color="text.secondary" display="block">Distance</Typography>
                      <Typography variant="h6" fontWeight={700}>{route.totalDistanceKm?.toFixed(2)} km</Typography>
                    </Box>
                    <Box>
                      <Typography variant="caption" color="text.secondary" display="block">ETA</Typography>
                      <Typography variant="h6" fontWeight={700}>{route.totalTimeMinutes?.toFixed(0)} min</Typography>
                    </Box>
                  </Box>
                  {route.message && (
                    <Typography variant="caption" color="text.secondary">{route.message}</Typography>
                  )}
                </Paper>
              )}
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ mt: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
              <RouteIcon color="primary" />
              <Typography variant="h6">Route Planner</Typography>
              {routeLoading && <CircularProgress size={18} sx={{ ml: 1 }} />}
            </Box>
            <Stack direction="row" spacing={1.5} sx={{ flexWrap: 'wrap', alignItems: 'center' }}>
              <FormControl size="small" sx={{ minWidth: 220 }}>
                <InputLabel>From</InputLabel>
                <Select
                  value={fromId}
                  label="From"
                  onChange={e => { setFromId(e.target.value); setRoute(null) }}
                >
                  {locationOptions.map(l => (
                    <MenuItem key={`from-${l.entityType}-${l.entityId}`} value={`${l.entityType}-${l.entityId}`}>
                      {l.entityType} — {l.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <FormControl size="small" sx={{ minWidth: 220 }}>
                <InputLabel>To</InputLabel>
                <Select
                  value={toId}
                  label="To"
                  onChange={e => { setToId(e.target.value); setRoute(null) }}
                >
                  {locationOptions.map(l => (
                    <MenuItem key={`to-${l.entityType}-${l.entityId}`} value={`${l.entityType}-${l.entityId}`}>
                      {l.entityType} — {l.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Button
                variant="contained"
                startIcon={<RouteIcon />}
                disabled={!fromId || !toId || fromId === toId || routeLoading}
                onClick={computeRoute}
              >
                Calculate Route
              </Button>
              {route && (
                <Button size="small" startIcon={<ClearIcon />} onClick={() => setRoute(null)}>
                  Clear
                </Button>
              )}
              {fromId && toId && fromId === toId && (
                <Typography variant="caption" color="error">Select two different locations</Typography>
              )}
            </Stack>
          </CardContent>
        </Card>
        <Footer />
      </Box>
    </Box>
  )
}

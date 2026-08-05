import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import MapView from '../components/MapView'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import SectionCard from '../components/SectionCard'
import StatusBadge from '../components/StatusBadge'
import { StatsSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { droneApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, Typography, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, LinearProgress, Button,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, Select, MenuItem,
  FormControl, InputLabel, IconButton, Switch, FormControlLabel, Grid, useTheme
} from '@mui/material'
import FlightIcon from '@mui/icons-material/Flight'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import RocketIcon from '@mui/icons-material/Rocket'
import BatteryChargingFullIcon from '@mui/icons-material/BatteryChargingFull'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'

const emptyForm = { droneId: '', status: 'AVAILABLE', battery: 100, cameraStatus: true, latitude: 20.5937, longitude: 78.9629, missionStatus: 'PENDING' }

export default function DroneMonitoring() {
  const [drones, setDrones] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialog, setDialog] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [autoRefresh, setAutoRefresh] = useState(true)
  const theme = useTheme()

  useEffect(() => {
    loadDrones()
    const interval = setInterval(() => { if (autoRefresh) loadDrones() }, 10000)
    return () => clearInterval(interval)
  }, [autoRefresh])

  const loadDrones = async () => {
    try {
      const res = await droneApi.getAll()
      setDrones(res.data)
    } catch {} finally { setLoading(false) }
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    try {
      await droneApi.create(form)
      toast.success('Drone added')
      setDialog(false)
      setForm(emptyForm)
      loadDrones()
    } catch { toast.error('Failed to add drone') }
  }

  const handleDelete = async (id) => {
    try {
      await droneApi.delete(id)
      toast.success('Drone removed')
      loadDrones()
    } catch { toast.error('Failed to delete') }
  }

  const locations = drones.map(d => ({
    entityType: 'DRONE', entityId: d.id, name: `Drone ${d.droneId}`,
    latitude: d.latitude, longitude: d.longitude, status: d.status,
    markerColor: d.status === 'IN_MISSION' ? 'red' : d.status === 'AVAILABLE' ? 'green' : 'yellow'
  }))

  const stats = [
    { icon: <FlightIcon />, value: drones.length, label: 'Total Drones', color: 'primary.main' },
    { icon: <CheckCircleIcon />, value: drones.filter(d => d.status === 'AVAILABLE').length, label: 'Available', color: 'success.main' },
    { icon: <RocketIcon />, value: drones.filter(d => d.status === 'IN_MISSION').length, label: 'In Mission', color: 'accent.main' },
    { icon: <BatteryChargingFullIcon />, value: drones.filter(d => d.status === 'CHARGING').length, label: 'Charging', color: 'warning.main' },
  ]

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <StatsSkeleton />
        <Box sx={{ mt: 2 }}><TableSkeleton /></Box>
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Drone Monitoring"
          subtitle="Track and manage drone fleet"
          actions={
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
              <FormControlLabel
                control={<Switch size="small" checked={autoRefresh} onChange={e => setAutoRefresh(e.target.checked)} />}
                label={<Typography variant="caption">Auto-refresh</Typography>}
              />
              <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setDialog(true); setForm(emptyForm) }}>
                Add Drone
              </Button>
            </Box>
          }
        />

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4,1fr)' }, gap: 2, mb: 3 }}>
          {stats.map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} color={s.color} />
          ))}
        </Box>

        <SectionCard title="Live Fleet Map" subtitle="Real-time drone positions" sx={{ mb: 3 }}>
          <MapView locations={locations} height="350px" />
        </SectionCard>

        <SectionCard
          title="Drone Fleet"
          subtitle={`${drones.length} registered drone${drones.length === 1 ? '' : 's'}`}
        >
          <TableContainer component={Paper} variant="outlined" sx={{ boxShadow: 'none', mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Drone ID</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Battery</TableCell>
                  <TableCell>Camera</TableCell>
                  <TableCell>Mission</TableCell>
                  <TableCell>Location</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {drones.length === 0 ? (
                  <TableRow><TableCell colSpan={7} sx={{ py: 4 }}><EmptyState title="No drones registered" /></TableCell></TableRow>
                ) : drones.map(d => (
                  <TableRow key={d.id} hover>
                    <TableCell><Chip label={d.droneId} size="small" sx={{ fontWeight: 600 }} /></TableCell>
                    <TableCell><StatusBadge status={d.status} /></TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 100 }}>
                        <LinearProgress variant="determinate" value={d.battery} sx={{
                          flex: 1, height: 8, borderRadius: 4,
                          backgroundColor: theme.palette.action.hover,
                          '& .MuiLinearProgress-bar': {
                            backgroundColor: d.battery > 50 ? theme.palette.success.main : d.battery > 20 ? theme.palette.warning.main : theme.palette.error.main,
                            borderRadius: 4
                          }
                        }} />
                        <Typography variant="caption" fontWeight={600}>{d.battery}%</Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{d.cameraStatus ? '✅' : '❌'}</TableCell>
                    <TableCell><StatusBadge status={d.missionStatus} /></TableCell>
                    <TableCell>
                      <Typography variant="caption">{d.latitude?.toFixed(3)}, {d.longitude?.toFixed(3)}</Typography>
                    </TableCell>
                    <TableCell>
                      <IconButton size="small" color="error" onClick={() => handleDelete(d.id)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
        <Footer />

        <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
          <DialogTitle>Add Drone</DialogTitle>
          <Box component="form" onSubmit={handleCreate}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}>
                  <TextField fullWidth size="small" label="Drone ID" required
                    value={form.droneId} onChange={e => setForm({ ...form, droneId: e.target.value })} />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Status</InputLabel>
                    <Select label="Status" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                      <MenuItem value="AVAILABLE">Available</MenuItem>
                      <MenuItem value="IN_MISSION">In Mission</MenuItem>
                      <MenuItem value="CHARGING">Charging</MenuItem>
                      <MenuItem value="MAINTENANCE">Maintenance</MenuItem>
                      <MenuItem value="OFFLINE">Offline</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField fullWidth size="small" label="Battery %" type="number" min="0" max="100" required
                    value={form.battery} onChange={e => setForm({ ...form, battery: parseInt(e.target.value) })} />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Camera</InputLabel>
                    <Select label="Camera" value={form.cameraStatus} onChange={e => setForm({ ...form, cameraStatus: e.target.value === 'true' })}>
                      <MenuItem value="true">Active</MenuItem>
                      <MenuItem value="false">Inactive</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField fullWidth size="small" label="Latitude" type="number" step="any"
                    value={form.latitude} onChange={e => setForm({ ...form, latitude: parseFloat(e.target.value) })} />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField fullWidth size="small" label="Longitude" type="number" step="any"
                    value={form.longitude} onChange={e => setForm({ ...form, longitude: parseFloat(e.target.value) })} />
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDialog(false)}>Cancel</Button>
              <Button type="submit" variant="contained">Add Drone</Button>
            </DialogActions>
          </Box>
        </Dialog>
      </Box>
    </Box>
  )
}

import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import MapView from '../components/MapView'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import SectionCard from '../components/SectionCard'
import ShelterOperationsDashboard from '../components/ShelterOperationsDashboard'
import { StatsSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { shelterApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, Typography, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Grid, Button, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Select, MenuItem, FormControl, InputLabel, IconButton, LinearProgress, Paper, Tabs, Tab, useTheme
} from '@mui/material'
import HomeIcon from '@mui/icons-material/Home'
import PeopleIcon from '@mui/icons-material/People'
import PercentIcon from '@mui/icons-material/Percent'
import NightShelterIcon from '@mui/icons-material/NightShelter'
import SpaceDashboardIcon from '@mui/icons-material/SpaceDashboard'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'

const emptyForm = { name: '', capacity: 0, occupancy: 0, foodAvailable: false, waterAvailable: false, medicalKits: 0, powerAvailable: false, contact: '', latitude: 20.5937, longitude: 78.9629, address: '' }

export default function ShelterManagement() {
  const [shelters, setShelters] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialog, setDialog] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [tab, setTab] = useState(0)
  const theme = useTheme()

  useEffect(() => { loadShelters() }, [])

  const loadShelters = async () => {
    try { const res = await shelterApi.getAll(); setShelters(res.data) }
    catch {} finally { setLoading(false) }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingId) { await shelterApi.update(editingId, form); toast.success('Updated') }
      else { await shelterApi.create(form); toast.success('Added') }
      setDialog(false); setEditingId(null); setForm(emptyForm); loadShelters()
    } catch { toast.error('Failed') }
  }

  const handleEdit = (s) => { setForm(s); setEditingId(s.id); setDialog(true) }
  const handleDelete = async (id) => { try { await shelterApi.delete(id); toast.success('Deleted'); loadShelters() } catch { toast.error('Failed') } }

  const locations = shelters.map(s => ({
    entityType: 'SHELTER', entityId: s.id, name: s.name,
    latitude: s.latitude, longitude: s.longitude,
    status: `${s.occupancy}/${s.capacity}`, markerColor: s.occupancyPercent > 80 ? 'red' : s.occupancyPercent > 50 ? 'yellow' : 'green'
  }))

  const totalCapacity = shelters.reduce((s, sh) => s + sh.capacity, 0)
  const totalOccupancy = shelters.reduce((s, sh) => s + sh.occupancy, 0)
  const occupancyPercent = totalCapacity > 0 ? Math.round((totalOccupancy / totalCapacity) * 100) : 0

  const stats = [
    { icon: <HomeIcon />, value: shelters.length, label: 'Total Shelters', color: 'primary.main' },
    { icon: <PeopleIcon />, value: totalCapacity, label: 'Total Capacity', color: 'success.main' },
    { icon: <PeopleIcon />, value: totalOccupancy, label: 'Current Occupancy', color: 'warning.main' },
    { icon: <PercentIcon />, value: occupancyPercent, label: 'Occupancy %', color: occupancyPercent > 80 ? 'error.main' : occupancyPercent > 50 ? 'warning.main' : 'success.main' },
  ]

  if (loading && tab === 1) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
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
          title="Shelter Management"
          subtitle="Track shelter capacity and supplies"
          actions={
            tab === 1 ? (
              <Button variant="contained" startIcon={<AddIcon />}
                onClick={() => { setDialog(true); setEditingId(null); setForm(emptyForm) }}>
                Add Shelter
              </Button>
            ) : undefined
          }
        />

        <Paper sx={{ borderRadius: 2, mb: 3, overflow: 'hidden' }}>
          <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto" sx={{ '& .MuiTab-root': { textTransform: 'none', fontWeight: 600 } }}>
            <Tab icon={<SpaceDashboardIcon />} iconPosition="start" label="Operations Dashboard" />
            <Tab icon={<NightShelterIcon />} iconPosition="start" label="Shelters" />
          </Tabs>
        </Paper>

        {tab === 0 && <ShelterOperationsDashboard />}

        {tab === 1 && (
          <>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4, 1fr)' }, gap: 2, mb: 3 }}>
          {stats.map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} color={s.color} />
          ))}
        </Box>

        <SectionCard title="Shelter Locations" subtitle="Geographic view of all registered shelters" sx={{ mb: 3 }}>
          <MapView locations={locations} height="300px" />
        </SectionCard>

        <SectionCard title="Shelters" subtitle={`${shelters.length} registered`}>
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow><TableCell>Name</TableCell><TableCell>Capacity</TableCell><TableCell>Occupancy</TableCell><TableCell>%</TableCell><TableCell>Food</TableCell><TableCell>Water</TableCell><TableCell>Medical</TableCell><TableCell>Power</TableCell><TableCell align="right">Actions</TableCell></TableRow>
              </TableHead>
              <TableBody>
                {shelters.length === 0 ? (
                  <TableRow><TableCell colSpan={9} sx={{ py: 4 }}><EmptyState title="No shelters" /></TableCell></TableRow>
                ) : shelters.map(s => (
                  <TableRow key={s.id} hover>
                    <TableCell><Typography fontWeight={600}>{s.name}</Typography></TableCell>
                    <TableCell>{s.capacity}</TableCell>
                    <TableCell>{s.occupancy}</TableCell>
                    <TableCell sx={{ minWidth: 100 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <LinearProgress variant="determinate" value={s.occupancyPercent || 0} sx={{
                          flex: 1, height: 8, borderRadius: 4,
                          backgroundColor: theme.palette.divider,
                          '& .MuiLinearProgress-bar': {
                            backgroundColor: s.occupancyPercent > 80 ? theme.palette.error.main : s.occupancyPercent > 50 ? theme.palette.warning.main : theme.palette.success.main,
                            borderRadius: 4
                          }
                        }} />
                        <Typography variant="caption" fontWeight={600}>{s.occupancyPercent || 0}%</Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{s.foodAvailable ? '✅' : '❌'}</TableCell>
                    <TableCell>{s.waterAvailable ? '✅' : '❌'}</TableCell>
                    <TableCell>{s.medicalKits}</TableCell>
                    <TableCell>{s.powerAvailable ? '✅' : '❌'}</TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => handleEdit(s)}><EditIcon fontSize="small" /></IconButton>
                      <IconButton size="small" color="error" onClick={() => handleDelete(s.id)}><DeleteIcon fontSize="small" /></IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
        <Footer />
          </>
        )}

        <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
          <DialogTitle>{editingId ? 'Edit Shelter' : 'Add Shelter'}</DialogTitle>
          <Box component="form" onSubmit={handleSubmit}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Name" required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Contact" value={form.contact} onChange={e => setForm({ ...form, contact: e.target.value })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Capacity" type="number" required value={form.capacity} onChange={e => setForm({ ...form, capacity: parseInt(e.target.value) })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Occupancy" type="number" required value={form.occupancy} onChange={e => setForm({ ...form, occupancy: parseInt(e.target.value) })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Medical Kits" type="number" value={form.medicalKits} onChange={e => setForm({ ...form, medicalKits: parseInt(e.target.value) })} /></Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Power</InputLabel>
                    <Select label="Power" value={form.powerAvailable} onChange={e => setForm({ ...form, powerAvailable: e.target.value === 'true' })}>
                      <MenuItem value="true">Available</MenuItem><MenuItem value="false">Not Available</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Food</InputLabel>
                    <Select label="Food" value={form.foodAvailable} onChange={e => setForm({ ...form, foodAvailable: e.target.value === 'true' })}>
                      <MenuItem value="true">Available</MenuItem><MenuItem value="false">Not Available</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Water</InputLabel>
                    <Select label="Water" value={form.waterAvailable} onChange={e => setForm({ ...form, waterAvailable: e.target.value === 'true' })}>
                      <MenuItem value="true">Available</MenuItem><MenuItem value="false">Not Available</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDialog(false)}>Cancel</Button>
              <Button type="submit" variant="contained">{editingId ? 'Update' : 'Add'} Shelter</Button>
            </DialogActions>
          </Box>
        </Dialog>
      </Box>
    </Box>
  )
}

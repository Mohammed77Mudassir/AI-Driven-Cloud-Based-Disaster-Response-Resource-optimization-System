import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import MapView from '../components/MapView'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import SectionCard from '../components/SectionCard'
import HospitalOperationsDashboard from '../components/HospitalOperationsDashboard'
import { StatsSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { hospitalApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, Typography, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Grid, Button, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Select, MenuItem, FormControl, InputLabel, IconButton, Paper, Tabs, Tab
} from '@mui/material'
import LocalHospitalIcon from '@mui/icons-material/LocalHospital'
import HotelIcon from '@mui/icons-material/Hotel'
import HealingIcon from '@mui/icons-material/Healing'
import PersonIcon from '@mui/icons-material/Person'
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'

const emptyForm = { name: '', availableBeds: 0, icuBeds: 0, doctorsAvailable: 0, emergencyContact: '', bloodBank: false, latitude: 20.5937, longitude: 78.9629, address: '' }

export default function HospitalManagement() {
  const [hospitals, setHospitals] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialog, setDialog] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [tab, setTab] = useState(0)

  useEffect(() => { loadHospitals() }, [])

  const loadHospitals = async () => {
    try { const res = await hospitalApi.getAll(); setHospitals(res.data) }
    catch {} finally { setLoading(false) }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingId) { await hospitalApi.update(editingId, form); toast.success('Hospital updated') }
      else { await hospitalApi.create(form); toast.success('Hospital added') }
      setDialog(false); setEditingId(null); setForm(emptyForm); loadHospitals()
    } catch { toast.error('Operation failed') }
  }

  const handleEdit = (h) => { setForm(h); setEditingId(h.id); setDialog(true) }
  const handleDelete = async (id) => { try { await hospitalApi.delete(id); toast.success('Deleted'); loadHospitals() } catch { toast.error('Failed') } }

  const locations = hospitals.map(h => ({
    entityType: 'HOSPITAL', entityId: h.id, name: h.name,
    latitude: h.latitude, longitude: h.longitude, status: `Beds: ${h.availableBeds}`, markerColor: 'blue'
  }))

  const stats = [
    { icon: <LocalHospitalIcon />, value: hospitals.length, label: 'Total Hospitals', color: 'primary.main' },
    { icon: <HotelIcon />, value: hospitals.reduce((s, h) => s + h.availableBeds, 0), label: 'Total Beds', color: 'success.main' },
    { icon: <HealingIcon />, value: hospitals.reduce((s, h) => s + h.icuBeds, 0), label: 'ICU Beds', color: 'warning.main' },
    { icon: <PersonIcon />, value: hospitals.reduce((s, h) => s + h.doctorsAvailable, 0), label: 'Available Doctors', color: 'primary.main' },
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
          title="Hospital Management"
          subtitle="Track hospital capacity and resources"
          actions={
            tab === 1 ? (
              <Button variant="contained" startIcon={<AddIcon />}
                onClick={() => { setDialog(true); setEditingId(null); setForm(emptyForm) }}>
                Add Hospital
              </Button>
            ) : undefined
          }
        />

        <Paper sx={{ borderRadius: 2, mb: 3, overflow: 'hidden' }}>
          <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto" sx={{ '& .MuiTab-root': { textTransform: 'none', fontWeight: 600 } }}>
            <Tab icon={<MonitorHeartIcon />} iconPosition="start" label="Operations Dashboard" />
            <Tab icon={<LocalHospitalIcon />} iconPosition="start" label="Hospitals" />
          </Tabs>
        </Paper>

        {tab === 0 && <HospitalOperationsDashboard />}

        {tab === 1 && (
          <>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4, 1fr)' }, gap: 2, mb: 3 }}>
          {stats.map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} color={s.color} />
          ))}
        </Box>

        <SectionCard title="Hospital Locations" subtitle="Geographic view of all registered facilities" sx={{ mb: 3 }}>
          <MapView locations={locations} height="300px" />
        </SectionCard>

        <SectionCard title="Hospitals" subtitle={`${hospitals.length} registered`}>
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow><TableCell>Name</TableCell><TableCell>Available Beds</TableCell><TableCell>ICU Beds</TableCell><TableCell>Doctors</TableCell><TableCell>Blood Bank</TableCell><TableCell>Contact</TableCell><TableCell align="right">Actions</TableCell></TableRow>
              </TableHead>
              <TableBody>
                {hospitals.length === 0 ? (
                  <TableRow><TableCell colSpan={7} sx={{ py: 4 }}><EmptyState title="No hospitals" message="No hospitals registered in the system." /></TableCell></TableRow>
                ) : hospitals.map(h => (
                  <TableRow key={h.id} hover>
                    <TableCell><Typography fontWeight={600}>{h.name}</Typography></TableCell>
                    <TableCell><Chip label={h.availableBeds} size="small" color="primary" /></TableCell>
                    <TableCell><Chip label={h.icuBeds} size="small" color="secondary" /></TableCell>
                    <TableCell>{h.doctorsAvailable}</TableCell>
                    <TableCell>{h.bloodBank ? '✅' : '❌'}</TableCell>
                    <TableCell>{h.emergencyContact}</TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => handleEdit(h)}><EditIcon fontSize="small" /></IconButton>
                      <IconButton size="small" color="error" onClick={() => handleDelete(h.id)}><DeleteIcon fontSize="small" /></IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
          </>
        )}
        <Footer />

        <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
          <DialogTitle>{editingId ? 'Edit Hospital' : 'Add Hospital'}</DialogTitle>
          <Box component="form" onSubmit={handleSubmit}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Name" required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Emergency Contact" value={form.emergencyContact} onChange={e => setForm({ ...form, emergencyContact: e.target.value })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Available Beds" type="number" required value={form.availableBeds} onChange={e => setForm({ ...form, availableBeds: parseInt(e.target.value) })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="ICU Beds" type="number" required value={form.icuBeds} onChange={e => setForm({ ...form, icuBeds: parseInt(e.target.value) })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Doctors Available" type="number" value={form.doctorsAvailable} onChange={e => setForm({ ...form, doctorsAvailable: parseInt(e.target.value) })} /></Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Blood Bank</InputLabel>
                    <Select label="Blood Bank" value={form.bloodBank} onChange={e => setForm({ ...form, bloodBank: e.target.value === 'true' })}>
                      <MenuItem value="true">Available</MenuItem>
                      <MenuItem value="false">Not Available</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12 }}><TextField fullWidth size="small" label="Address" value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} /></Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDialog(false)}>Cancel</Button>
              <Button type="submit" variant="contained">{editingId ? 'Update' : 'Add'} Hospital</Button>
            </DialogActions>
          </Box>
        </Dialog>
      </Box>
    </Box>
  )
}

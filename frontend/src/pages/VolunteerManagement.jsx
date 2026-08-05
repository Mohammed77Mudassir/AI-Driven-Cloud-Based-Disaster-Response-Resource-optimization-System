import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import SectionCard from '../components/SectionCard'
import StatusBadge from '../components/StatusBadge'
import { StatsSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { volunteerApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, Typography, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Grid, Button, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Select, MenuItem, FormControl, InputLabel, IconButton
} from '@mui/material'
import PeopleIcon from '@mui/icons-material/People'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'

const emptyForm = { name: '', email: '', phone: '', skills: '', available: true, latitude: 20.5937, longitude: 78.9629 }

export default function VolunteerManagement() {
  const [volunteers, setVolunteers] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialog, setDialog] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [filter, setFilter] = useState('ALL')

  useEffect(() => { loadVolunteers() }, [])

  const loadVolunteers = async () => {
    try { const res = await volunteerApi.getAll(); setVolunteers(res.data) }
    catch {} finally { setLoading(false) }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingId) { await volunteerApi.update(editingId, form); toast.success('Updated') }
      else { await volunteerApi.create(form); toast.success('Added') }
      setDialog(false); setEditingId(null); setForm(emptyForm); loadVolunteers()
    } catch { toast.error('Failed') }
  }

  const handleEdit = (v) => { setForm(v); setEditingId(v.id); setDialog(true) }
  const handleDelete = async (id) => { try { await volunteerApi.delete(id); toast.success('Deleted'); loadVolunteers() } catch { toast.error('Failed') } }

  const filtered = filter === 'ALL' ? volunteers : volunteers.filter(v => filter === 'AVAILABLE' ? v.available : !v.available)

  const stats = [
    { icon: <PeopleIcon />, value: volunteers.length, label: 'Total Volunteers', color: 'primary.main' },
    { icon: <CheckCircleIcon />, value: volunteers.filter(v => v.available).length, label: 'Available', color: 'success.main' },
    { icon: <RocketLaunchIcon />, value: volunteers.filter(v => !v.available).length, label: 'Deployed', color: 'warning.main' },
  ]

  if (loading) return (
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
          title="Volunteer Management"
          subtitle="Manage volunteer registration and deployment"
          actions={
            <Button variant="contained" startIcon={<AddIcon />}
              onClick={() => { setDialog(true); setEditingId(null); setForm(emptyForm) }}>
              Add Volunteer
            </Button>
          }
        />

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: 'repeat(3, 1fr)' }, gap: 2, mb: 3 }}>
          {stats.map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} color={s.color} />
          ))}
        </Box>

        <SectionCard
          title="Volunteers"
          subtitle={`${volunteers.length} registered`}
          action={
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <Select value={filter} onChange={e => setFilter(e.target.value)} displayEmpty>
                <MenuItem value="ALL">All</MenuItem>
                <MenuItem value="AVAILABLE">Available</MenuItem>
                <MenuItem value="DEPLOYED">Deployed</MenuItem>
              </Select>
            </FormControl>
          }
        >
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow><TableCell>Name</TableCell><TableCell>Email</TableCell><TableCell>Phone</TableCell><TableCell>Skills</TableCell><TableCell>Status</TableCell><TableCell align="right">Actions</TableCell></TableRow>
              </TableHead>
              <TableBody>
                {filtered.length === 0 ? (
                  <TableRow><TableCell colSpan={6} sx={{ py: 4 }}><EmptyState title="No volunteers" /></TableCell></TableRow>
                ) : filtered.map(v => (
                  <TableRow key={v.id} hover>
                    <TableCell><Typography fontWeight={600}>{v.name}</Typography></TableCell>
                    <TableCell>{v.email}</TableCell>
                    <TableCell>{v.phone}</TableCell>
                    <TableCell><Chip label={v.skills} size="small" sx={{ fontWeight: 600, fontSize: '0.7rem' }} /></TableCell>
                    <TableCell><StatusBadge status={v.available ? 'Available' : 'Deployed'} /></TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => handleEdit(v)}><EditIcon fontSize="small" /></IconButton>
                      <IconButton size="small" color="error" onClick={() => handleDelete(v.id)}><DeleteIcon fontSize="small" /></IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
        <Footer />

        <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
          <DialogTitle>{editingId ? 'Edit Volunteer' : 'Add Volunteer'}</DialogTitle>
          <Box component="form" onSubmit={handleSubmit}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Name" required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Email" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Phone" value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} /></Grid>
                <Grid size={{ xs: 6 }}><TextField fullWidth size="small" label="Skills" value={form.skills} onChange={e => setForm({ ...form, skills: e.target.value })} placeholder="e.g. First Aid, Rescue" /></Grid>
                <Grid size={{ xs: 12 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Status</InputLabel>
                    <Select label="Status" value={form.available} onChange={e => setForm({ ...form, available: e.target.value === 'true' })}>
                      <MenuItem value="true">Available</MenuItem>
                      <MenuItem value="false">Deployed</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDialog(false)}>Cancel</Button>
              <Button type="submit" variant="contained">{editingId ? 'Update' : 'Add'} Volunteer</Button>
            </DialogActions>
          </Box>
        </Dialog>
      </Box>
    </Box>
  )
}

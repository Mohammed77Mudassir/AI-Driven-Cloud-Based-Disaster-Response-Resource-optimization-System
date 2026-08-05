import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import SectionCard from '../components/SectionCard'
import StatusBadge from '../components/StatusBadge'
import { StatsSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { resourceApi, missionApi } from '../services/api'
import HistoryIcon from '@mui/icons-material/History'
import toast from 'react-hot-toast'
import {
  Box, Typography, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Grid, Button, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Select, MenuItem, FormControl, InputLabel, IconButton, useTheme
} from '@mui/material'
import InventoryIcon from '@mui/icons-material/Inventory'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import ReplyIcon from '@mui/icons-material/Reply'
import EngineeringIcon from '@mui/icons-material/Engineering'

const resourceTypes = ['AMBULANCE', 'FIRE_TRUCK', 'POLICE_TEAM', 'MEDICAL_TEAM', 'RESCUE_TEAM', 'BOAT', 'HELICOPTER', 'GENERATOR']
const conditions = ['GOOD', 'FAIR', 'POOR']
const emptyForm = { resourceType: 'AMBULANCE', quantity: 1, available: true, condition: 'GOOD', location: '', latitude: 20.5937, longitude: 78.9629 }

const statusColor = (r) => {
  if (r.deployedQuantity > 0 && r.available) return { label: 'Partial' }
  if (r.deployedQuantity > 0) return { label: 'Deployed' }
  if (r.inMaintenanceQuantity > 0) return { label: 'Maintenance' }
  if (r.available) return { label: 'Available' }
  return { label: 'Unavailable' }
}

const fmt = (iso) => {
  if (!iso) return '-'
  return new Date(iso).toLocaleDateString(undefined, { dateStyle: 'medium' })
}

export default function ResourceManagement() {
  const [resources, setResources] = useState([])
  const [missions, setMissions] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialog, setDialog] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)

  const [actionResource, setActionResource] = useState(null)
  const [actionType, setActionType] = useState('')
  const [actionQuantity, setActionQuantity] = useState(1)
  const [actionMission, setActionMission] = useState('')
  const theme = useTheme()

  useEffect(() => {
    Promise.all([loadResources(), loadMissions()])
  }, [])

  const loadResources = async () => {
    try { const res = await resourceApi.getAll(); setResources(res.data) }
    catch {} finally { setLoading(false) }
  }

  const loadMissions = async () => {
    try { setMissions((await missionApi.getAll()).data) }
    catch { /* mission list optional */ }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingId) { await resourceApi.update(editingId, form); toast.success('Updated') }
      else { await resourceApi.create(form); toast.success('Added') }
      setDialog(false); setEditingId(null); setForm(emptyForm); loadResources()
    } catch { toast.error('Failed') }
  }

  const handleEdit = (r) => { setForm({ ...r, latitude: r.latitude, longitude: r.longitude }); setEditingId(r.id); setDialog(true) }
  const handleDelete = async (id) => { try { await resourceApi.delete(id); toast.success('Deleted'); loadResources() } catch { toast.error('Failed') } }

  const openAction = (r, type) => { setActionResource(r); setActionType(type); setActionQuantity(1); setActionMission('') }

  const runAction = async () => {
    const q = parseInt(actionQuantity, 10)
    try {
      if (actionType === 'deploy') {
        if (!actionMission) { toast.error('Select a mission'); return }
        await resourceApi.deploy(actionResource.id, q, actionMission)
      } else if (actionType === 'return') await resourceApi.returnResource(actionResource.id, q)
      else if (actionType === 'maintenance') await resourceApi.startMaintenance(actionResource.id, q)
      else if (actionType === 'maintenanceComplete') await resourceApi.completeMaintenance(actionResource.id, q)
      toast.success('Resource updated')
      setActionResource(null); setActionType('')
      loadResources()
    } catch { toast.error('Failed to update resource') }
  }

  const typeCounts = resourceTypes.map(t => ({
    type: t, count: resources.filter(r => r.resourceType === t).reduce((s, r) => s + (r.totalQuantity || r.quantity || 0), 0)
  }))

  const stats = [
    { icon: <InventoryIcon />, value: resources.reduce((s, r) => s + (r.totalQuantity || r.quantity || 0), 0), label: 'Total Resources', color: 'primary.main' },
    { icon: <CheckCircleIcon />, value: resources.reduce((s, r) => s + (r.available ? (r.quantity || 0) : 0), 0), label: 'Available', color: 'success.main' },
    { icon: <RocketLaunchIcon />, value: resources.reduce((s, r) => s + (r.deployedQuantity || 0), 0), label: 'Deployed', color: 'warning.main' },
    { icon: <EngineeringIcon />, value: resources.reduce((s, r) => s + (r.inMaintenanceQuantity || 0), 0), label: 'In Maintenance', color: 'accent.main' },
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
          title="Resource Management"
          subtitle="Track inventory, deploy to missions, and manage maintenance"
          actions={
            <Button variant="contained" startIcon={<AddIcon />}
              onClick={() => { setDialog(true); setEditingId(null); setForm(emptyForm) }}>
              Add Resource
            </Button>
          }
        />

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4, 1fr)' }, gap: 2, mb: 3 }}>
          {stats.map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} color={s.color} />
          ))}
        </Box>

        <SectionCard title="Resource Inventory" subtitle="Units on hand by type" sx={{ mb: 3 }}>
          <Grid container spacing={1}>
            {typeCounts.map(t => (
              <Grid size={{ xs: 6, sm: 3 }} key={t.type}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', p: 1.5, bgcolor: 'action.hover', borderRadius: 1.5 }}>
                  <Typography variant="caption" fontWeight={600}>{t.type.replace('_', ' ')}</Typography>
                  <Typography variant="body2" fontWeight={700} color="primary.main">{t.count}</Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </SectionCard>

        <SectionCard title="Resources" subtitle={`${resources.length} registered`}>
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Type</TableCell>
                  <TableCell>Available/Total</TableCell>
                  <TableCell>Deployed</TableCell>
                  <TableCell>Maintenance</TableCell>
                  <TableCell>Condition</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Mission</TableCell>
                  <TableCell>Maintenance Due</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {resources.length === 0 ? (
                  <TableRow><TableCell colSpan={9} sx={{ py: 4 }}><EmptyState title="No resources" message="No resources registered in the system." /></TableCell></TableRow>
                ) : resources.map(r => {
                  const sc = statusColor(r)
                  return (
                    <TableRow key={r.id} hover>
                      <TableCell><Chip label={r.resourceType?.replace('_', ' ')} size="small" sx={{ fontWeight: 600, fontSize: '0.7rem' }} /></TableCell>
                      <TableCell><Typography fontWeight={600}>{r.quantity}/{r.totalQuantity || r.quantity}</Typography></TableCell>
                      <TableCell>{r.deployedQuantity || 0}</TableCell>
                      <TableCell>{r.inMaintenanceQuantity || 0}</TableCell>
                      <TableCell>
                        <Chip label={r.condition || 'GOOD'} size="small" sx={{
                          fontWeight: 600, fontSize: '0.7rem',
                          color: r.condition === 'GOOD' ? theme.palette.success.main : r.condition === 'FAIR' ? theme.palette.warning.main : theme.palette.error.main,
                          bgcolor: 'action.hover'
                        }} />
                      </TableCell>
                      <TableCell><StatusBadge status={sc.label} /></TableCell>
                      <TableCell>{r.assignedMissionTitle || '—'}</TableCell>
                      <TableCell>{fmt(r.maintenanceDueAt)}</TableCell>
                      <TableCell align="right">
                        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                          {r.quantity > 0 && <IconButton size="small" onClick={() => openAction(r, 'deploy')} sx={{ color: theme.palette.primary.main }} title="Deploy"><RocketLaunchIcon fontSize="small" /></IconButton>}
                          {r.deployedQuantity > 0 && <IconButton size="small" onClick={() => openAction(r, 'return')} sx={{ color: theme.palette.success.main }} title="Return"><ReplyIcon fontSize="small" /></IconButton>}
                          {r.quantity > 0 && <IconButton size="small" onClick={() => openAction(r, 'maintenance')} sx={{ color: theme.palette.accent.main }} title="Start Maintenance"><EngineeringIcon fontSize="small" /></IconButton>}
                          {r.inMaintenanceQuantity > 0 && <IconButton size="small" onClick={() => openAction(r, 'maintenanceComplete')} sx={{ color: theme.palette.success.main }} title="Complete Maintenance"><CheckCircleIcon fontSize="small" /></IconButton>}
                          <IconButton size="small" onClick={() => handleEdit(r)}><EditIcon fontSize="small" /></IconButton>
                          <IconButton size="small" color="error" onClick={() => handleDelete(r.id)}><DeleteIcon fontSize="small" /></IconButton>
                        </Box>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
        <Footer />

        <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
          <DialogTitle>{editingId ? 'Edit Resource' : 'Add Resource'}</DialogTitle>
          <Box component="form" onSubmit={handleSubmit}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Type</InputLabel>
                    <Select label="Type" value={form.resourceType} onChange={e => setForm({ ...form, resourceType: e.target.value })}>
                      {resourceTypes.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField fullWidth size="small" label="Quantity" type="number" min="1" required
                    value={form.quantity} onChange={e => setForm({ ...form, quantity: parseInt(e.target.value) })} />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Condition</InputLabel>
                    <Select label="Condition" value={form.condition || 'GOOD'} onChange={e => setForm({ ...form, condition: e.target.value })}>
                      {conditions.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField fullWidth size="small" label="Location" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} />
                </Grid>
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
              <Button type="submit" variant="contained">{editingId ? 'Update' : 'Add'} Resource</Button>
            </DialogActions>
          </Box>
        </Dialog>

        <Dialog open={!!actionResource} onClose={() => setActionResource(null)} maxWidth="xs" fullWidth>
          <DialogTitle>Resource Action — {actionResource?.resourceType?.replace('_', ' ')}</DialogTitle>
          <DialogContent>
            <Typography variant="body2" mb={2}>
              {actionType === 'deploy' && `Deploy units to a mission. Available: ${actionResource?.quantity}`}
              {actionType === 'return' && `Return units from deployment. Deployed: ${actionResource?.deployedQuantity}`}
              {actionType === 'maintenance' && `Send units for maintenance. Available: ${actionResource?.quantity}`}
              {actionType === 'maintenanceComplete' && `Complete maintenance. In maintenance: ${actionResource?.inMaintenanceQuantity}`}
            </Typography>
            {actionType === 'deploy' && (
              <Select fullWidth size="small" value={actionMission} onChange={e => setActionMission(e.target.value)} displayEmpty sx={{ mb: 2 }}>
                <MenuItem value="" disabled>Select Mission</MenuItem>
                {missions.map(m => <MenuItem key={m.id} value={m.id}>{m.missionCode} — {m.title} ({m.status})</MenuItem>)}
              </Select>
            )}
            <TextField fullWidth size="small" label="Quantity" type="number" min="1"
              value={actionQuantity} onChange={e => setActionQuantity(e.target.value)} />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setActionResource(null)}>Cancel</Button>
            <Button variant="contained" onClick={runAction}>Confirm</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Box>
  )
}

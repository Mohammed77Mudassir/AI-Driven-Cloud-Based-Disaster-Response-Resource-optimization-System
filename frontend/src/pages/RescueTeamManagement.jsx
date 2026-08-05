import { useState, useEffect, cloneElement, useMemo } from 'react'
import { useAuth } from '../context/AuthContext'
import { rescueTeamApi, disasterApi, vehicleApi, equipmentApi, missionApi, shiftApi } from '../services/api'
import toast from 'react-hot-toast'
import RescueTeamCommandCenter from './RescueTeamCommandCenter'
import Sidebar from '../components/Sidebar'
import Breadcrumbs from '../components/Breadcrumbs'
import Footer from '../components/Footer'
import EmptyState from '../components/EmptyState'
import { TableSkeleton, StatsSkeleton } from '../components/LoadingSkeleton'
import {
  Box, Typography, Card, Table, TableContainer, TableHead, TableBody,
  TableRow, TableCell, Button, TextField, Select, MenuItem, Dialog,
  DialogTitle, DialogContent, DialogActions, IconButton, Chip,
  InputAdornment, Grid, Paper, TablePagination, Tabs, Tab, FormControl, InputLabel
} from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import SearchIcon from '@mui/icons-material/Search'
import AddIcon from '@mui/icons-material/Add'
import GroupsIcon from '@mui/icons-material/Groups'
import PersonAddIcon from '@mui/icons-material/PersonAdd'
import AssignmentIcon from '@mui/icons-material/Assignment'
import VisibilityIcon from '@mui/icons-material/Visibility'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar'
import BuildIcon from '@mui/icons-material/Build'
import FlagIcon from '@mui/icons-material/Flag'
import ScheduleIcon from '@mui/icons-material/Schedule'
import StarIcon from '@mui/icons-material/Star'
import HistoryIcon from '@mui/icons-material/History'
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch'
import ReplyIcon from '@mui/icons-material/Reply'
import EngineeringIcon from '@mui/icons-material/Engineering'

const teamStatusColors = {
  AVAILABLE: '#2E7D32',
  DEPLOYED: '#F57C00',
  ON_MISSION: '#0F4C81',
  STANDING_BY: '#F9A825',
  RETURNED: '#607D8B',
  OFF_DUTY: '#757575'
}

const vehicleStatusColors = {
  AVAILABLE: '#2E7D32',
  DEPLOYED: '#0F4C81',
  IN_MAINTENANCE: '#F57C00',
  OUT_OF_SERVICE: '#C62828'
}

const equipmentStatusColors = {
  AVAILABLE: '#2E7D32',
  DEPLOYED: '#0F4C81',
  IN_MAINTENANCE: '#F57C00',
  DEPLETED: '#C62828',
  OUT_OF_SERVICE: '#616161'
}

const missionStatusColors = {
  PENDING: '#F9A825',
  ASSIGNED: '#0F4C81',
  IN_PROGRESS: '#F57C00',
  COMPLETED: '#2E7D32',
  CANCELLED: '#C62828'
}

const shiftStatusColors = {
  SCHEDULED: '#0F4C81',
  ACTIVE: '#2E7D32',
  COMPLETED: '#607D8B',
  CANCELLED: '#C62828'
}

const fmt = (iso) => {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

const vehicleTypes = ['AMBULANCE', 'FIRE_TRUCK', 'POLICE_VAN', 'WATER_TANKER', 'BUS', 'TRUCK', 'SUV', 'HELICOPTER', 'BOAT', 'MOTORBIKE']
const equipmentTypes = ['MEDICAL', 'RESCUE', 'COMMUNICATION', 'PROTECTION', 'POWER', 'WATER', 'FOOD', 'SEARCH', 'OTHER']
const conditions = ['GOOD', 'FAIR', 'POOR']
const missionTypes = ['SEARCH_AND_RESCUE', 'MEDICAL_EVAC', 'FIRE_SUPPRESSION', 'RELIEF_DISTRIBUTION', 'EVACUATION', 'RECON', 'LOGISTICS', 'OTHER']
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

const emptyTeamForm = () => ({
  teamName: '', teamLeader: '', location: '', latitude: '', longitude: '',
  contactNumber: '', memberCount: '', specialty: '', maxCapacity: ''
})

const emptyVehicleForm = () => ({
  teamId: '', vehicleType: 'AMBULANCE', registrationNumber: '', model: '',
  capacity: '', fuelLevel: 100, notes: ''
})

const emptyEquipmentForm = () => ({
  teamId: '', name: '', equipmentType: 'MEDICAL', totalQuantity: '',
  condition: 'GOOD', notes: ''
})

const emptyMissionForm = () => ({
  title: '', missionType: 'SEARCH_AND_RESCUE', description: '', teamId: '',
  disasterId: '', priority: 'MEDIUM', startTime: '', endTime: '', instructions: ''
})

const emptyShiftForm = () => ({
  teamId: '', memberId: '', shiftType: 'DAY', shiftStart: '', shiftEnd: '', notes: ''
})

export default function RescueTeamManagement() {
  const { user: currentUser } = useAuth()
  const canManage = currentUser?.permissions?.includes('RESCUE_TEAM_MANAGE')
  const [tab, setTab] = useState(0)

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <Breadcrumbs />
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>Rescue Team Management</Typography>
            <Typography variant="body2" color="text.secondary">Coordinate teams, members, vehicles, equipment, missions and shifts</Typography>
          </Box>
        </Box>
        <Paper sx={{ borderRadius: 2, mb: 3, overflow: 'hidden' }}>
          <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto" sx={{ '& .MuiTab-root': { textTransform: 'none', fontWeight: 600 } }}>
            <Tab icon={<RocketLaunchIcon />} iconPosition="start" label="Command Center" />
            <Tab icon={<GroupsIcon />} iconPosition="start" label="Teams" />
            <Tab icon={<PersonAddIcon />} iconPosition="start" label="Members" />
            <Tab icon={<DirectionsCarIcon />} iconPosition="start" label="Vehicles" />
            <Tab icon={<BuildIcon />} iconPosition="start" label="Equipment" />
            <Tab icon={<FlagIcon />} iconPosition="start" label="Missions" />
            <Tab icon={<ScheduleIcon />} iconPosition="start" label="Shifts" />
          </Tabs>
        </Paper>
        {tab === 0 && <RescueTeamCommandCenter />}
        {tab === 1 && <TeamsTab canManage={canManage} />}
        {tab === 2 && <MembersTab canManage={canManage} />}
        {tab === 3 && <VehiclesTab canManage={canManage} />}
        {tab === 4 && <EquipmentTab canManage={canManage} />}
        {tab === 5 && <MissionsTab canManage={canManage} />}
        {tab === 6 && <ShiftsTab canManage={canManage} />}
        <Footer />
      </Box>
    </Box>
  )
}

function TeamsTab({ canManage }) {
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)

  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [editTeam, setEditTeam] = useState(null)
  const [teamForm, setTeamForm] = useState(emptyTeamForm())

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)

  const [assignDialogOpen, setAssignDialogOpen] = useState(false)
  const [assignTarget, setAssignTarget] = useState(null)
  const [assignDisaster, setAssignDisaster] = useState('')
  const [disasters, setDisasters] = useState([])

  const loadTeams = async () => {
    setLoading(true)
    try { setTeams((await rescueTeamApi.getAll()).data) }
    catch { toast.error('Failed to load rescue teams') }
    finally { setLoading(false) }
  }

  useEffect(() => {
    loadTeams()
    disasterApi.getAll().then(res => setDisasters(res.data.content || [])).catch(() => {})
  }, [])

  const filtered = useMemo(() => teams.filter(t => {
    const matchesSearch = (t.teamName || '').toLowerCase().includes(search.toLowerCase()) ||
      (t.teamLeader || '').toLowerCase().includes(search.toLowerCase()) ||
      (t.location || '').toLowerCase().includes(search.toLowerCase()) ||
      (t.specialty || '').toLowerCase().includes(search.toLowerCase())
    const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter
    return matchesSearch && matchesStatus
  }), [teams, search, statusFilter])

  const totalTeams = teams.length
  const available = teams.filter(t => t.status === 'AVAILABLE').length
  const onMission = teams.filter(t => t.status === 'ON_MISSION').length
  const deployed = teams.filter(t => t.status === 'DEPLOYED').length

  const handleCreate = async () => {
    try {
      await rescueTeamApi.create(teamForm)
      toast.success('Team created')
      setCreateDialogOpen(false)
      setTeamForm(emptyTeamForm())
      loadTeams()
    } catch { toast.error('Failed to create team') }
  }

  const handleEditOpen = (t) => {
    setEditTeam(t)
    setTeamForm({
      teamName: t.teamName || '', teamLeader: t.teamLeader || '', location: t.location || '',
      latitude: t.latitude || '', longitude: t.longitude || '', contactNumber: t.contactNumber || '',
      memberCount: t.memberCount || '', specialty: t.specialty || '', maxCapacity: t.maxCapacity || ''
    })
    setEditDialogOpen(true)
  }

  const handleEditSave = async () => {
    try {
      await rescueTeamApi.update(editTeam.id, teamForm)
      toast.success('Team updated')
      setEditDialogOpen(false)
      loadTeams()
    } catch { toast.error('Failed to update team') }
  }

  const handleDeleteConfirm = async () => {
    try {
      await rescueTeamApi.delete(deleteTarget.id)
      toast.success('Team deleted')
      setDeleteDialogOpen(false)
      loadTeams()
    } catch { toast.error('Failed to delete team') }
  }

  const handleAssignConfirm = async () => {
    if (!assignDisaster) { toast.error('Please select a disaster'); return }
    try {
      await rescueTeamApi.assignToDisaster(assignTarget.id, assignDisaster)
      toast.success('Team assigned to disaster')
      setAssignDialogOpen(false)
      loadTeams()
    } catch { toast.error('Failed to assign team') }
  }

  if (loading) return (<><StatsSkeleton /><TableSkeleton rows={8} /></>)

  return (
    <>
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {[
          { label: 'Total Teams', value: totalTeams, icon: <GroupsIcon />, color: '#0F4C81' },
          { label: 'Available', value: available, icon: <CheckCircleIcon />, color: '#2E7D32' },
          { label: 'Deployed', value: deployed, icon: <AssignmentIcon />, color: '#F57C00' },
          { label: 'On Mission', value: onMission, icon: <VisibilityIcon />, color: '#0F4C81' },
        ].map(stat => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={stat.label}>
            <Card sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ width: 48, height: 48, borderRadius: 2, backgroundColor: `${stat.color}15`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                {cloneElement(stat.icon, { sx: { color: stat.color, fontSize: 24 } })}
              </Box>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 700 }}>{stat.value}</Typography>
                <Typography variant="body2" color="text.secondary">{stat.label}</Typography>
              </Box>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Paper sx={{ borderRadius: 2, overflow: 'hidden' }}>
        <Box sx={{ p: 2, display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
          <TextField
            size="small" placeholder="Search by team name, leader, location, specialty..."
            value={search} onChange={e => { setSearch(e.target.value); setPage(0) }} sx={{ minWidth: 280 }}
            slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon sx={{ color: '#636E72' }} /></InputAdornment> } }}
          />
          <Select size="small" value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0) }} sx={{ minWidth: 150 }}>
            <MenuItem value="ALL">All Status</MenuItem>
            {Object.keys(teamStatusColors).map(s => <MenuItem key={s} value={s}>{s.replace('_', ' ')}</MenuItem>)}
          </Select>
          {canManage && <Box sx={{ ml: 'auto' }}><Button variant="contained" startIcon={<AddIcon />} onClick={() => { setTeamForm(emptyTeamForm()); setCreateDialogOpen(true) }}>Create Team</Button></Box>}
        </Box>

        {filtered.length === 0 ? (
          <EmptyState title="No teams found" message="Try adjusting your search or filters" actionLabel="Create Team" onAction={() => setCreateDialogOpen(true)} />
        ) : (
          <>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Team Name</TableCell>
                    <TableCell>Leader</TableCell>
                    <TableCell>Members</TableCell>
                    <TableCell>Specialty</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Assigned Disaster</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map(t => (
                    <TableRow key={t.id} hover>
                      <TableCell><Typography variant="body2" sx={{ fontWeight: 600 }}>{t.teamName}</Typography></TableCell>
                      <TableCell>{t.teamLeader || '-'}</TableCell>
                      <TableCell>{t.memberCount || 0}/{t.maxCapacity || '-'}</TableCell>
                      <TableCell>{t.specialty || '-'}</TableCell>
                      <TableCell>
                        <Chip label={t.status || 'STANDING_BY'} size="small" sx={{ backgroundColor: `${teamStatusColors[t.status] || '#F9A825'}20`, color: teamStatusColors[t.status] || '#F9A825', fontWeight: 600 }} />
                      </TableCell>
                      <TableCell>{t.assignedDisasterName ? `${t.assignedDisasterName} (${fmt(t.deployedAt)})` : '-'}</TableCell>
                      <TableCell align="right">
                        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                          <MemberDialogTrigger teamId={t.id} teamName={t.teamName} />
                          {canManage && <>
                            <IconButton size="small" onClick={() => handleEditOpen(t)} sx={{ color: '#0F4C81' }} title="Edit"><EditIcon fontSize="small" /></IconButton>
                            <IconButton size="small" onClick={() => { setAssignTarget(t); setAssignDisaster(''); setAssignDialogOpen(true) }} sx={{ color: '#4d96ff' }} title="Assign to Disaster"><AssignmentIcon fontSize="small" /></IconButton>
                            <IconButton size="small" onClick={() => { setDeleteTarget(t); setDeleteDialogOpen(true) }} sx={{ color: '#C62828' }} title="Delete"><DeleteIcon fontSize="small" /></IconButton>
                          </>}
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination component="div" count={filtered.length} page={page} onPageChange={(_, p) => setPage(p)} rowsPerPage={rowsPerPage} onRowsPerPageChange={e => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0) }} />
          </>
        )}
      </Paper>

      <Dialog open={createDialogOpen || editDialogOpen} onClose={() => { setCreateDialogOpen(false); setEditDialogOpen(false) }} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>{editDialogOpen ? `Edit Team — ${editTeam?.teamName}` : 'Create Rescue Team'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField label="Team Name" fullWidth size="small" value={teamForm.teamName} onChange={e => setTeamForm(f => ({ ...f, teamName: e.target.value }))} required />
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}><TextField label="Leader" fullWidth size="small" value={teamForm.teamLeader} onChange={e => setTeamForm(f => ({ ...f, teamLeader: e.target.value }))} /></Grid>
              <Grid size={{ xs: 6 }}><TextField label="Specialty" fullWidth size="small" value={teamForm.specialty} onChange={e => setTeamForm(f => ({ ...f, specialty: e.target.value }))} /></Grid>
            </Grid>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}><TextField label="Member Count" fullWidth size="small" type="number" value={teamForm.memberCount} onChange={e => setTeamForm(f => ({ ...f, memberCount: e.target.value }))} /></Grid>
              <Grid size={{ xs: 6 }}><TextField label="Max Capacity" fullWidth size="small" type="number" value={teamForm.maxCapacity} onChange={e => setTeamForm(f => ({ ...f, maxCapacity: e.target.value }))} /></Grid>
            </Grid>
            <TextField label="Location" fullWidth size="small" value={teamForm.location} onChange={e => setTeamForm(f => ({ ...f, location: e.target.value }))} />
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}><TextField label="Latitude" fullWidth size="small" type="number" value={teamForm.latitude} onChange={e => setTeamForm(f => ({ ...f, latitude: e.target.value }))} /></Grid>
              <Grid size={{ xs: 6 }}><TextField label="Longitude" fullWidth size="small" type="number" value={teamForm.longitude} onChange={e => setTeamForm(f => ({ ...f, longitude: e.target.value }))} /></Grid>
            </Grid>
            <TextField label="Contact Number" fullWidth size="small" value={teamForm.contactNumber} onChange={e => setTeamForm(f => ({ ...f, contactNumber: e.target.value }))} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setCreateDialogOpen(false); setEditDialogOpen(false) }}>Cancel</Button>
          <Button variant="contained" onClick={editDialogOpen ? handleEditSave : handleCreate}>{editDialogOpen ? 'Save' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete team <strong>{deleteTarget?.teamName}</strong>? This action cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" color="error" onClick={handleDeleteConfirm}>Delete</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={assignDialogOpen} onClose={() => setAssignDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Assign Team to Disaster — {assignTarget?.teamName}</DialogTitle>
        <DialogContent>
          <Select fullWidth size="small" value={assignDisaster} onChange={e => setAssignDisaster(e.target.value)} displayEmpty sx={{ mt: 1 }}>
            <MenuItem value="" disabled>Select a disaster</MenuItem>
            {disasters.map(d => <MenuItem key={d.id} value={d.id}>{d.disasterType} — {d.location} ({d.status})</MenuItem>)}
          </Select>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAssignDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleAssignConfirm}>Assign</Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

function MemberDialogTrigger({ teamId, teamName }) {
  const [open, setOpen] = useState(false)
  const { user: currentUser } = useAuth()
  const canManage = currentUser?.permissions?.includes('RESCUE_TEAM_MANAGE')
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(false)
  const [memberForm, setMemberForm] = useState({ name: '', role: '', speciality: '', phone: '', available: true, skills: '', certifications: '' })

  const loadMembers = async () => {
    setLoading(true)
    try { setMembers((await rescueTeamApi.getMembers(teamId)).data) }
    catch { toast.error('Failed to load members'); setMembers([]) }
    finally { setLoading(false) }
  }

  const handleAddMember = async () => {
    if (!memberForm.name) { toast.error('Name is required'); return }
    try {
      await rescueTeamApi.addMember(teamId, memberForm)
      toast.success('Member added')
      setMemberForm({ name: '', role: '', speciality: '', phone: '', available: true, skills: '', certifications: '' })
      loadMembers()
    } catch { toast.error('Failed to add member') }
  }

  const handlePromote = async (memberId) => {
    try { await rescueTeamApi.assignLeader(memberId, teamId); toast.success('Leader assigned'); loadMembers() }
    catch { toast.error('Failed to assign leader') }
  }

  const handleToggleAvailable = async (m) => {
    try { await rescueTeamApi.updateMember(m.id, { ...m, available: !m.available }); loadMembers() }
    catch { toast.error('Failed to update member') }
  }

  const handleRemove = async (memberId) => {
    try { await rescueTeamApi.removeMember(memberId); toast.success('Member removed'); loadMembers() }
    catch { toast.error('Failed to remove member') }
  }

  return (
    <>
      <IconButton size="small" onClick={() => { setOpen(true); loadMembers() }} sx={{ color: '#00897B' }} title="View Members"><GroupsIcon fontSize="small" /></IconButton>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 600, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>Team Members — {teamName}</span>
        </DialogTitle>
        <DialogContent>
          {loading ? <TableSkeleton rows={3} /> : members.length === 0 ? (
            <EmptyState title="No members" message="This team has no members yet" />
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Role</TableCell>
                    <TableCell>Speciality</TableCell>
                    <TableCell>Skills</TableCell>
                    <TableCell>Phone</TableCell>
                    <TableCell>Status</TableCell>
                    {canManage && <TableCell align="right">Actions</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {members.map(m => (
                    <TableRow key={m.id}>
                      <TableCell sx={{ fontWeight: 600 }}>
                        {m.name} {m.isLeader && <StarIcon sx={{ fontSize: 16, color: '#F9A825', verticalAlign: 'middle' }} />}
                      </TableCell>
                      <TableCell><Chip label={m.role || 'Member'} size="small" sx={{ fontWeight: 600 }} /></TableCell>
                      <TableCell>{m.speciality || '-'}</TableCell>
                      <TableCell>{m.skills || '-'}</TableCell>
                      <TableCell>{m.phone || '-'}</TableCell>
                      <TableCell>
                        <Chip label={m.available ? 'Available' : 'Unavailable'} size="small" sx={{ fontWeight: 600, backgroundColor: m.available ? '#2E7D3220' : '#C6282820', color: m.available ? '#2E7D32' : '#C62828' }} />
                      </TableCell>
                      {canManage && (
                        <TableCell align="right">
                          <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                            {!m.isLeader && <IconButton size="small" onClick={() => handlePromote(m.id)} sx={{ color: '#F9A825' }} title="Assign Leader"><StarIcon fontSize="small" /></IconButton>}
                            <IconButton size="small" onClick={() => handleToggleAvailable(m)} sx={{ color: '#0F4C81' }} title="Toggle availability"><CheckCircleIcon fontSize="small" /></IconButton>
                            <IconButton size="small" onClick={() => handleRemove(m.id)} sx={{ color: '#C62828' }} title="Remove"><DeleteIcon fontSize="small" /></IconButton>
                          </Box>
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
          {canManage && (
            <Paper variant="outlined" sx={{ p: 2, mt: 2 }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>Add Member</Typography>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}><TextField label="Name" size="small" fullWidth value={memberForm.name} onChange={e => setMemberForm(f => ({ ...f, name: e.target.value }))} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}>
                  <Select size="small" fullWidth value={memberForm.role} onChange={e => setMemberForm(f => ({ ...f, role: e.target.value }))} displayEmpty>
                    <MenuItem value="" disabled>Role</MenuItem>
                    {['Leader', 'Medic', 'Rescuer', 'Driver', 'Technician', 'Coordinator'].map(r => <MenuItem key={r} value={r}>{r}</MenuItem>)}
                  </Select>
                </Grid>
                <Grid size={{ xs: 6, sm: 4 }}><TextField label="Phone" size="small" fullWidth value={memberForm.phone} onChange={e => setMemberForm(f => ({ ...f, phone: e.target.value }))} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><TextField label="Speciality" size="small" fullWidth value={memberForm.speciality} onChange={e => setMemberForm(f => ({ ...f, speciality: e.target.value }))} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><TextField label="Skills" size="small" fullWidth value={memberForm.skills} onChange={e => setMemberForm(f => ({ ...f, skills: e.target.value }))} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><TextField label="Certifications" size="small" fullWidth value={memberForm.certifications} onChange={e => setMemberForm(f => ({ ...f, certifications: e.target.value }))} /></Grid>
                <Grid size={{ xs: 12 }}><Button variant="contained" size="small" onClick={handleAddMember}>Add Member</Button></Grid>
              </Grid>
            </Paper>
          )}
        </DialogContent>
        <DialogActions><Button onClick={() => setOpen(false)}>Close</Button></DialogActions>
      </Dialog>
    </>
  )
}

function MembersTab({ canManage }) {
  const [members, setMembers] = useState([])
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)

  const load = async () => {
    setLoading(true)
    try {
      const teamsRes = (await rescueTeamApi.getAll()).data
      setTeams(teamsRes)
      const all = []
      for (const t of teamsRes) {
        try { all.push(...(await rescueTeamApi.getMembers(t.id)).data) } catch { /* skip */ }
      }
      setMembers(all)
    } catch { toast.error('Failed to load members') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handlePromote = async (m) => {
    try { await rescueTeamApi.assignLeader(m.id, m.teamId); toast.success('Leader assigned'); load() }
    catch { toast.error('Failed to assign leader') }
  }

  const handleRemove = async (id) => {
    try { await rescueTeamApi.removeMember(id); toast.success('Member removed'); load() }
    catch { toast.error('Failed to remove member') }
  }

  if (loading) return <TableSkeleton rows={8} />

  return (
    <Paper sx={{ borderRadius: 2, overflow: 'hidden' }}>
      <Box sx={{ p: 2 }}>
        <Typography variant="h6" sx={{ fontWeight: 700 }}>All Team Members</Typography>
      </Box>
      {members.length === 0 ? (
        <EmptyState title="No members found" message="Members are added per team from the Teams tab" />
      ) : (
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Team</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Speciality</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {members.map(m => (
                <TableRow key={m.id} hover>
                  <TableCell sx={{ fontWeight: 600 }}>{m.name} {m.isLeader && <StarIcon sx={{ fontSize: 16, color: '#F9A825', verticalAlign: 'middle' }} />}</TableCell>
                  <TableCell>{m.teamName || '-'}</TableCell>
                  <TableCell><Chip label={m.role || 'Member'} size="small" sx={{ fontWeight: 600 }} /></TableCell>
                  <TableCell>{m.speciality || '-'}</TableCell>
                  <TableCell>
                    <Chip label={m.available ? 'Available' : 'Unavailable'} size="small" sx={{ fontWeight: 600, backgroundColor: m.available ? '#2E7D3220' : '#C6282820', color: m.available ? '#2E7D32' : '#C62828' }} />
                  </TableCell>
                  <TableCell align="right">
                    {canManage && (
                      <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                        {!m.isLeader && <IconButton size="small" onClick={() => handlePromote(m)} sx={{ color: '#F9A825' }} title="Assign Leader"><StarIcon fontSize="small" /></IconButton>}
                        <IconButton size="small" onClick={() => handleRemove(m.id)} sx={{ color: '#C62828' }} title="Remove"><DeleteIcon fontSize="small" /></IconButton>
                      </Box>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
      {teams.length > 0 && (
        <Box sx={{ p: 2, borderTop: '1px solid #eee', display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
          <Typography variant="body2" color="text.secondary">Add members from: </Typography>
          {teams.map(t => <MemberDialogTrigger key={t.id} teamId={t.id} teamName={t.teamName} />)}
        </Box>
      )}
    </Paper>
  )
}

function useMissions() {
  const [missions, setMissions] = useState([])
  const loadMissions = async () => { try { setMissions((await missionApi.getAll()).data) } catch { toast.error('Failed to load missions') } }
  return { missions, loadMissions }
}

function VehiclesTab({ canManage }) {
  const [vehicles, setVehicles] = useState([])
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyVehicleForm())
  const [actionTarget, setActionTarget] = useState(null)
  const [actionType, setActionType] = useState('')
  const [actionMission, setActionMission] = useState('')
  const { missions } = useMissions()

  const load = async () => {
    setLoading(true)
    try {
      const [v, t] = await Promise.all([vehicleApi.getAll(), rescueTeamApi.getAll()])
      setVehicles(v.data); setTeams(t.data)
    } catch { toast.error('Failed to load vehicles') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async () => {
    try {
      if (editing) { await vehicleApi.update(editing.id, form); toast.success('Vehicle updated') }
      else { await vehicleApi.create(form); toast.success('Vehicle added') }
      setDialogOpen(false); setEditing(null); setForm(emptyVehicleForm()); load()
    } catch { toast.error('Failed to save vehicle') }
  }

  const handleAction = async () => {
    try {
      if (actionType === 'deploy') await vehicleApi.deploy(actionTarget.id, actionMission || undefined)
      else if (actionType === 'return') await vehicleApi.returnVehicle(actionTarget.id)
      else if (actionType === 'maintenance') await vehicleApi.startMaintenance(actionTarget.id)
      else if (actionType === 'maintenanceComplete') await vehicleApi.completeMaintenance(actionTarget.id)
      toast.success('Vehicle updated')
      setActionTarget(null); setActionMission(''); load()
    } catch { toast.error('Failed to update vehicle') }
  }

  if (loading) return <TableSkeleton rows={8} />

  return (
    <>
      <Paper sx={{ borderRadius: 2, overflow: 'hidden' }}>
        <Box sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Fleet Inventory</Typography>
          {canManage && <Box sx={{ ml: 'auto' }}><Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setForm(emptyVehicleForm()); setDialogOpen(true) }}>Add Vehicle</Button></Box>}
        </Box>
        {vehicles.length === 0 ? (
          <EmptyState title="No vehicles" message="Register vehicles assigned to rescue teams" />
        ) : (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Vehicle</TableCell>
                  <TableCell>Team</TableCell>
                  <TableCell>Capacity</TableCell>
                  <TableCell>Fuel</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Mission</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {vehicles.map(v => (
                  <TableRow key={v.id} hover>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{v.vehicleType?.replace('_', ' ')}</Typography>
                      <Typography variant="caption" color="text.secondary">{v.registrationNumber} · {v.model}</Typography>
                    </TableCell>
                    <TableCell>{v.teamName || '-'}</TableCell>
                    <TableCell>{v.capacity}</TableCell>
                    <TableCell><Chip label={`${v.fuelLevel}%`} size="small" sx={{ fontWeight: 600, backgroundColor: v.fuelLevel < 25 ? '#C6282820' : '#2E7D3220', color: v.fuelLevel < 25 ? '#C62828' : '#2E7D32' }} /></TableCell>
                    <TableCell><Chip label={v.status?.replace('_', ' ')} size="small" sx={{ backgroundColor: `${vehicleStatusColors[v.status]}20`, color: vehicleStatusColors[v.status], fontWeight: 600 }} /></TableCell>
                    <TableCell>{v.assignedMissionTitle || '-'}</TableCell>
                    <TableCell align="right">
                      {canManage ? (
                        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                          {v.status === 'AVAILABLE' && <IconButton size="small" onClick={() => { setActionTarget(v); setActionType('deploy'); setActionMission('') }} sx={{ color: '#0F4C81' }} title="Deploy"><RocketLaunchIcon fontSize="small" /></IconButton>}
                          {v.status === 'DEPLOYED' && <IconButton size="small" onClick={() => setActionTarget({ ...v, actionType: 'return' })} sx={{ color: '#2E7D32' }} title="Return"><ReplyIcon fontSize="small" /></IconButton>}
                          {(v.status === 'AVAILABLE' || v.status === 'DEPLOYED') && <IconButton size="small" onClick={() => setActionTarget({ ...v, actionType: 'maintenance' })} sx={{ color: '#F57C00' }} title="Start Maintenance"><EngineeringIcon fontSize="small" /></IconButton>}
                          {v.status === 'IN_MAINTENANCE' && <IconButton size="small" onClick={() => setActionTarget({ ...v, actionType: 'maintenanceComplete' })} sx={{ color: '#2E7D32' }} title="Complete Maintenance"><CheckCircleIcon fontSize="small" /></IconButton>}
                          <IconButton size="small" onClick={() => { setEditing(v); setForm({ ...emptyVehicleForm(), teamId: v.teamId || '', vehicleType: v.vehicleType, registrationNumber: v.registrationNumber, model: v.model, capacity: v.capacity, fuelLevel: v.fuelLevel, notes: v.notes }); setDialogOpen(true) }} sx={{ color: '#0F4C81' }} title="Edit"><EditIcon fontSize="small" /></IconButton>
                          <IconButton size="small" onClick={() => vehicleApi.delete(v.id).then(() => { toast.success('Deleted'); load() }).catch(() => toast.error('Failed'))} sx={{ color: '#C62828' }} title="Delete"><DeleteIcon fontSize="small" /></IconButton>
                        </Box>
                      ) : <Typography variant="caption" color="text.secondary">View only</Typography>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>{editing ? 'Edit Vehicle' : 'Add Vehicle'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Select size="small" fullWidth value={form.teamId} onChange={e => setForm(f => ({ ...f, teamId: e.target.value }))} displayEmpty>
              <MenuItem value="" disabled>Select Team</MenuItem>
              {teams.map(t => <MenuItem key={t.id} value={t.id}>{t.teamName}</MenuItem>)}
            </Select>
            <Select size="small" fullWidth value={form.vehicleType} onChange={e => setForm(f => ({ ...f, vehicleType: e.target.value }))}>
              {vehicleTypes.map(t => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
            </Select>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}><TextField label="Registration No." size="small" fullWidth value={form.registrationNumber} onChange={e => setForm(f => ({ ...f, registrationNumber: e.target.value }))} /></Grid>
              <Grid size={{ xs: 6 }}><TextField label="Model" size="small" fullWidth value={form.model} onChange={e => setForm(f => ({ ...f, model: e.target.value }))} /></Grid>
            </Grid>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}><TextField label="Capacity" size="small" fullWidth type="number" value={form.capacity} onChange={e => setForm(f => ({ ...f, capacity: e.target.value }))} /></Grid>
              <Grid size={{ xs: 6 }}><TextField label="Fuel Level (%)" size="small" fullWidth type="number" value={form.fuelLevel} onChange={e => setForm(f => ({ ...f, fuelLevel: e.target.value }))} /></Grid>
            </Grid>
            <TextField label="Notes" size="small" fullWidth value={form.notes} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>{editing ? 'Save' : 'Add'}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!actionTarget} onClose={() => setActionTarget(null)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Vehicle Action</DialogTitle>
        <DialogContent>
          {actionTarget?.actionType === 'return' && <Typography mb={1}>Return vehicle <strong>{actionTarget.registrationNumber}</strong> from its mission?</Typography>}
          {actionTarget?.actionType === 'maintenance' && <Typography mb={1}>Send vehicle <strong>{actionTarget.registrationNumber}</strong> to maintenance?</Typography>}
          {actionTarget?.actionType === 'maintenanceComplete' && <Typography mb={1}>Mark maintenance complete for <strong>{actionTarget.registrationNumber}</strong>? Fuel will be refilled to at least 50%.</Typography>}
          {actionTarget?.actionType === 'deploy' && (
            <>
              <Typography mb={1}>Deploy vehicle <strong>{actionTarget.registrationNumber}</strong> to a mission:</Typography>
              <Select size="small" fullWidth value={actionMission} onChange={e => setActionMission(e.target.value)} displayEmpty>
                <MenuItem value="" disabled>Select Mission</MenuItem>
                {missions.map(m => <MenuItem key={m.id} value={m.id}>{m.missionCode} — {m.title} ({m.status})</MenuItem>)}
              </Select>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setActionTarget(null)}>Cancel</Button>
          <Button variant="contained" onClick={() => {
            if (actionType === 'deploy' && !actionMission) { toast.error('Select a mission'); return }
            setActionType(actionTarget?.actionType || actionType)
          }}>Continue</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={actionType !== ''} onClose={() => setActionType('')} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Confirm</DialogTitle>
        <DialogContent>
          <Typography>Proceed with <strong>{actionType}</strong> for vehicle <strong>{actionTarget?.registrationNumber}</strong>?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setActionType('')}>Cancel</Button>
          <Button variant="contained" onClick={() => { handleAction(); setActionType('') }}>Confirm</Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

function EquipmentTab({ canManage }) {
  const [equipment, setEquipment] = useState([])
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyEquipmentForm())
  const [actionTarget, setActionTarget] = useState(null)
  const [actionType, setActionType] = useState('')
  const [actionQuantity, setActionQuantity] = useState(1)
  const [actionMission, setActionMission] = useState('')
  const { missions } = useMissions()

  const load = async () => {
    setLoading(true)
    try {
      const [e, t] = await Promise.all([equipmentApi.getAll(), rescueTeamApi.getAll()])
      setEquipment(e.data); setTeams(t.data)
    } catch { toast.error('Failed to load equipment') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async () => {
    try {
      if (editing) { await equipmentApi.update(editing.id, form); toast.success('Equipment updated') }
      else { await equipmentApi.create(form); toast.success('Equipment added') }
      setDialogOpen(false); setEditing(null); setForm(emptyEquipmentForm()); load()
    } catch { toast.error('Failed to save equipment') }
  }

  const handleAction = async () => {
    try {
      if (actionType === 'deploy') await equipmentApi.deploy(actionTarget.id, parseInt(actionQuantity, 10), actionMission || undefined)
      else if (actionType === 'return') await equipmentApi.returnEquipment(actionTarget.id, parseInt(actionQuantity, 10))
      else if (actionType === 'maintenance') await equipmentApi.startMaintenance(actionTarget.id, parseInt(actionQuantity, 10))
      else if (actionType === 'maintenanceComplete') await equipmentApi.completeMaintenance(actionTarget.id, parseInt(actionQuantity, 10))
      toast.success('Equipment updated')
      setActionTarget(null); setActionQuantity(1); setActionMission(''); load()
    } catch { toast.error('Failed to update equipment') }
  }

  if (loading) return <TableSkeleton rows={8} />

  return (
    <>
      <Paper sx={{ borderRadius: 2, overflow: 'hidden' }}>
        <Box sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Equipment Inventory</Typography>
          {canManage && <Box sx={{ ml: 'auto' }}><Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setForm(emptyEquipmentForm()); setDialogOpen(true) }}>Add Equipment</Button></Box>}
        </Box>
        {equipment.length === 0 ? (
          <EmptyState title="No equipment" message="Register equipment assigned to rescue teams" />
        ) : (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Item</TableCell>
                  <TableCell>Team</TableCell>
                  <TableCell>Available/Total</TableCell>
                  <TableCell>Deployed</TableCell>
                  <TableCell>Maint.</TableCell>
                  <TableCell>Condition</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {equipment.map(e => (
                  <TableRow key={e.id} hover>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{e.name}</Typography>
                      <Typography variant="caption" color="text.secondary">{e.equipmentType?.replace('_', ' ')}</Typography>
                    </TableCell>
                    <TableCell>{e.teamName || '-'}</TableCell>
                    <TableCell><Typography fontWeight={600}>{e.availableQuantity}/{e.totalQuantity}</Typography></TableCell>
                    <TableCell>{e.deployedQuantity}</TableCell>
                    <TableCell>{e.inMaintenanceQuantity}</TableCell>
                    <TableCell><Chip label={e.condition} size="small" sx={{ fontWeight: 600, backgroundColor: e.condition === 'GOOD' ? '#2E7D3220' : e.condition === 'FAIR' ? '#F9A82520' : '#C6282820', color: e.condition === 'GOOD' ? '#2E7D32' : e.condition === 'FAIR' ? '#F9A825' : '#C62828' }} /></TableCell>
                    <TableCell><Chip label={e.status?.replace('_', ' ')} size="small" sx={{ backgroundColor: `${equipmentStatusColors[e.status]}20`, color: equipmentStatusColors[e.status], fontWeight: 600 }} /></TableCell>
                    <TableCell align="right">
                      {canManage ? (
                        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                          {e.availableQuantity > 0 && <IconButton size="small" onClick={() => { setActionTarget(e); setActionType('deploy'); setActionQuantity(1); setActionMission('') }} sx={{ color: '#0F4C81' }} title="Deploy"><RocketLaunchIcon fontSize="small" /></IconButton>}
                          {e.deployedQuantity > 0 && <IconButton size="small" onClick={() => { setActionTarget(e); setActionType('return'); setActionQuantity(1) }} sx={{ color: '#2E7D32' }} title="Return"><ReplyIcon fontSize="small" /></IconButton>}
                          {e.availableQuantity > 0 && <IconButton size="small" onClick={() => { setActionTarget(e); setActionType('maintenance'); setActionQuantity(1) }} sx={{ color: '#F57C00' }} title="Start Maintenance"><EngineeringIcon fontSize="small" /></IconButton>}
                          {e.inMaintenanceQuantity > 0 && <IconButton size="small" onClick={() => { setActionTarget(e); setActionType('maintenanceComplete'); setActionQuantity(1) }} sx={{ color: '#2E7D32' }} title="Complete Maintenance"><CheckCircleIcon fontSize="small" /></IconButton>}
                          <IconButton size="small" onClick={() => { setEditing(e); setForm({ teamId: e.teamId || '', name: e.name, equipmentType: e.equipmentType, totalQuantity: e.totalQuantity, condition: e.condition, notes: e.notes }); setDialogOpen(true) }} sx={{ color: '#0F4C81' }} title="Edit"><EditIcon fontSize="small" /></IconButton>
                          <IconButton size="small" onClick={() => equipmentApi.delete(e.id).then(() => { toast.success('Deleted'); load() }).catch(() => toast.error('Failed'))} sx={{ color: '#C62828' }} title="Delete"><DeleteIcon fontSize="small" /></IconButton>
                        </Box>
                      ) : <Typography variant="caption" color="text.secondary">View only</Typography>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>{editing ? 'Edit Equipment' : 'Add Equipment'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Select size="small" fullWidth value={form.teamId} onChange={e => setForm(f => ({ ...f, teamId: e.target.value }))} displayEmpty>
              <MenuItem value="" disabled>Select Team</MenuItem>
              {teams.map(t => <MenuItem key={t.id} value={t.id}>{t.teamName}</MenuItem>)}
            </Select>
            <TextField label="Name" size="small" fullWidth value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Select size="small" fullWidth value={form.equipmentType} onChange={e => setForm(f => ({ ...f, equipmentType: e.target.value }))}>
                  {equipmentTypes.map(t => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
                </Select>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Select size="small" fullWidth value={form.condition} onChange={e => setForm(f => ({ ...f, condition: e.target.value }))}>
                  {conditions.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                </Select>
              </Grid>
            </Grid>
            <TextField label="Total Quantity" size="small" fullWidth type="number" value={form.totalQuantity} onChange={e => setForm(f => ({ ...f, totalQuantity: e.target.value }))} />
            <TextField label="Notes" size="small" fullWidth value={form.notes} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>{editing ? 'Save' : 'Add'}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!actionTarget} onClose={() => setActionTarget(null)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Equipment Action</DialogTitle>
        <DialogContent>
          <Typography mb={1}>
            {actionType === 'deploy' && `Deploy ${actionTarget?.name} to mission:`}
            {actionType === 'return' && `Return how many units of ${actionTarget?.name}?`}
            {actionType === 'maintenance' && `Send how many units of ${actionTarget?.name} to maintenance?`}
            {actionType === 'maintenanceComplete' && `Complete maintenance for how many units of ${actionTarget?.name}?`}
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {actionType === 'deploy' && (
              <Select size="small" fullWidth value={actionMission} onChange={e => setActionMission(e.target.value)} displayEmpty>
                <MenuItem value="" disabled>Select Mission</MenuItem>
                {missions.map(m => <MenuItem key={m.id} value={m.id}>{m.missionCode} — {m.title} ({m.status})</MenuItem>)}
              </Select>
            )}
            <TextField label="Quantity" size="small" type="number" value={actionQuantity} onChange={e => setActionQuantity(e.target.value)} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setActionTarget(null)}>Cancel</Button>
          <Button variant="contained" onClick={() => {
            if (actionType === 'deploy' && !actionMission) { toast.error('Select a mission'); return }
            handleAction()
          }}>Confirm</Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

function MissionsTab({ canManage }) {
  const [missions, setMissions] = useState([])
  const [teams, setTeams] = useState([])
  const [disasters, setDisasters] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyMissionForm())
  const [transitionTarget, setTransitionTarget] = useState(null)
  const [transitions, setTransitions] = useState([])
  const [eventsTarget, setEventsTarget] = useState(null)
  const [events, setEvents] = useState([])
  const [eventsLoading, setEventsLoading] = useState(false)

  const load = async () => {
    setLoading(true)
    try {
      const [m, t, d] = await Promise.all([missionApi.getAll(), rescueTeamApi.getAll(), disasterApi.getAll()])
      setMissions(m.data); setTeams(t.data); setDisasters(d.data.content || [])
    } catch { toast.error('Failed to load missions') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async () => {
    try {
      if (editing) { await missionApi.update(editing.id, form); toast.success('Mission updated') }
      else { await missionApi.create(form); toast.success('Mission created') }
      setDialogOpen(false); setEditing(null); setForm(emptyMissionForm()); load()
    } catch { toast.error('Failed to save mission') }
  }

  const openTransitions = async (m) => {
    setTransitionTarget(m)
    try { setTransitions((await missionApi.getTransitions(m.status)).data) }
    catch { setTransitions([]) }
  }

  const applyTransition = async (nextStatus) => {
    try {
      await missionApi.updateStatus(transitionTarget.id, nextStatus)
      toast.success(`Mission ${nextStatus}`)
      setTransitionTarget(null); load()
    } catch { toast.error('Failed to update mission status') }
  }

  const openEvents = async (m) => {
    setEventsTarget(m); setEventsLoading(true)
    try { setEvents((await missionApi.getEvents(m.id)).data) }
    catch { setEvents([]) }
    finally { setEventsLoading(false) }
  }

  if (loading) return <TableSkeleton rows={8} />

  return (
    <>
      <Paper sx={{ borderRadius: 2, overflow: 'hidden' }}>
        <Box sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Rescue Missions</Typography>
          {canManage && <Box sx={{ ml: 'auto' }}><Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setForm(emptyMissionForm()); setDialogOpen(true) }}>Create Mission</Button></Box>}
        </Box>
        {missions.length === 0 ? (
          <EmptyState title="No missions" message="Create a mission and assign a team" />
        ) : (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Mission</TableCell>
                  <TableCell>Team</TableCell>
                  <TableCell>Disaster</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Started</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {missions.map(m => (
                  <TableRow key={m.id} hover>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{m.title}</Typography>
                      <Typography variant="caption" color="text.secondary">{m.missionCode} · {m.missionType?.replace('_', ' ')}</Typography>
                    </TableCell>
                    <TableCell>{m.teamName || '-'}</TableCell>
                    <TableCell>{m.disasterName || '-'}</TableCell>
                    <TableCell>
                      <Chip label={m.priority} size="small" sx={{ fontWeight: 600, backgroundColor: m.priority === 'CRITICAL' ? '#C6282820' : m.priority === 'HIGH' ? '#F57C0020' : '#F9A82520', color: m.priority === 'CRITICAL' ? '#C62828' : m.priority === 'HIGH' ? '#E65100' : '#8d6e00' }} />
                    </TableCell>
                    <TableCell><Chip label={m.status?.replace('_', ' ')} size="small" sx={{ backgroundColor: `${missionStatusColors[m.status]}20`, color: missionStatusColors[m.status], fontWeight: 600 }} /></TableCell>
                    <TableCell>{fmt(m.startTime)}</TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                        <IconButton size="small" onClick={() => openEvents(m)} sx={{ color: '#00897B' }} title="History"><HistoryIcon fontSize="small" /></IconButton>
                        {canManage && m.status !== 'COMPLETED' && m.status !== 'CANCELLED' && (
                          <IconButton size="small" onClick={() => openTransitions(m)} sx={{ color: '#F57C00' }} title="Change Status"><AssignmentIcon fontSize="small" /></IconButton>
                        )}
                        {canManage && (
                          <>
                            <IconButton size="small" onClick={() => { setEditing(m); setForm({ title: m.title, missionType: m.missionType, description: m.description, teamId: m.teamId || '', disasterId: m.disasterId || '', priority: m.priority, startTime: m.startTime || '', endTime: m.endTime || '', instructions: m.instructions || '' }); setDialogOpen(true) }} sx={{ color: '#0F4C81' }} title="Edit"><EditIcon fontSize="small" /></IconButton>
                            <IconButton size="small" onClick={() => missionApi.delete(m.id).then(() => { toast.success('Deleted'); load() }).catch(() => toast.error('Failed'))} sx={{ color: '#C62828' }} title="Delete"><DeleteIcon fontSize="small" /></IconButton>
                          </>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>{editing ? 'Edit Mission' : 'Create Mission'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField label="Title" size="small" fullWidth value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} />
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Select size="small" fullWidth value={form.missionType} onChange={e => setForm(f => ({ ...f, missionType: e.target.value }))}>
                  {missionTypes.map(t => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
                </Select>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Select size="small" fullWidth value={form.priority} onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}>
                  {priorities.map(p => <MenuItem key={p} value={p}>{p}</MenuItem>)}
                </Select>
              </Grid>
            </Grid>
            <TextField label="Description" size="small" fullWidth multiline rows={2} value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Select size="small" fullWidth value={form.teamId} onChange={e => setForm(f => ({ ...f, teamId: e.target.value }))} displayEmpty>
                  <MenuItem value="" disabled>Select Team</MenuItem>
                  {teams.map(t => <MenuItem key={t.id} value={t.id}>{t.teamName}</MenuItem>)}
                </Select>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Select size="small" fullWidth value={form.disasterId} onChange={e => setForm(f => ({ ...f, disasterId: e.target.value }))} displayEmpty>
                  <MenuItem value="" disabled>Select Disaster</MenuItem>
                  {disasters.map(d => <MenuItem key={d.id} value={d.id}>{d.disasterType} — {d.location}</MenuItem>)}
                </Select>
              </Grid>
            </Grid>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}><TextField label="Start Time" size="small" fullWidth type="datetime-local" value={form.startTime} onChange={e => setForm(f => ({ ...f, startTime: e.target.value }))} /></Grid>
              <Grid size={{ xs: 6 }}><TextField label="End Time" size="small" fullWidth type="datetime-local" value={form.endTime} onChange={e => setForm(f => ({ ...f, endTime: e.target.value }))} /></Grid>
            </Grid>
            <TextField label="Instructions" size="small" fullWidth multiline rows={2} value={form.instructions} onChange={e => setForm(f => ({ ...f, instructions: e.target.value }))} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>{editing ? 'Save' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!transitionTarget} onClose={() => setTransitionTarget(null)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Change Mission Status</DialogTitle>
        <DialogContent>
          <Typography mb={2}><strong>{transitionTarget?.missionCode}</strong> — current status <Chip label={transitionTarget?.status} size="small" sx={{ fontWeight: 600 }} /></Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {transitions.length === 0 ? (
              <Typography color="text.secondary" variant="body2">No transitions available from this status.</Typography>
            ) : transitions.map(t => (
              <Button key={t} variant="outlined" fullWidth onClick={() => applyTransition(t)}>{t.replace('_', ' ')}</Button>
            ))}
          </Box>
        </DialogContent>
        <DialogActions><Button onClick={() => setTransitionTarget(null)}>Close</Button></DialogActions>
      </Dialog>

      <Dialog open={!!eventsTarget} onClose={() => setEventsTarget(null)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>Mission History — {eventsTarget?.missionCode}</DialogTitle>
        <DialogContent>
          {eventsLoading ? <TableSkeleton rows={4} /> : events.length === 0 ? (
            <EmptyState title="No events" message="No history recorded for this mission" />
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow><TableCell>Time</TableCell><TableCell>Event</TableCell><TableCell>Details</TableCell><TableCell>By</TableCell></TableRow>
                </TableHead>
                <TableBody>
                  {events.map(ev => (
                    <TableRow key={ev.id}>
                      <TableCell>{fmt(ev.occurredAt)}</TableCell>
                      <TableCell><Chip label={ev.eventType?.replace('_', ' ')} size="small" sx={{ fontWeight: 600 }} /></TableCell>
                      <TableCell>{ev.message}</TableCell>
                      <TableCell>{ev.performedBy || '-'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions><Button onClick={() => setEventsTarget(null)}>Close</Button></DialogActions>
      </Dialog>
    </>
  )
}

function ShiftsTab({ canManage }) {
  const [shifts, setShifts] = useState([])
  const [teams, setTeams] = useState([])
  const [membersByTeam, setMembersByTeam] = useState({})
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyShiftForm())
  const [rosterDate, setRosterDate] = useState('')

  const load = async (roster) => {
    setLoading(true)
    try {
      const t = (await rescueTeamApi.getAll()).data
      setTeams(t)
      const memberMap = {}
      for (const team of t) {
        try { memberMap[team.id] = (await rescueTeamApi.getMembers(team.id)).data } catch { memberMap[team.id] = [] }
      }
      setMembersByTeam(memberMap)
      if (roster) setShifts((await shiftApi.getRoster(roster)).data)
      else setShifts((await shiftApi.getAll()).data)
    } catch { toast.error('Failed to load shifts') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async () => {
    try {
      if (editing) { await shiftApi.update(editing.id, form); toast.success('Shift updated') }
      else { await shiftApi.create(form); toast.success('Shift scheduled') }
      setDialogOpen(false); setEditing(null); setForm(emptyShiftForm()); load(rosterDate || undefined)
    } catch { toast.error('Failed to save shift') }
  }

  const updateShiftStatus = async (shift, status) => {
    try { await shiftApi.updateStatus(shift.id, status); toast.success(`Shift ${status}`); load(rosterDate || undefined) }
    catch { toast.error('Failed to update shift') }
  }

  if (loading) return <TableSkeleton rows={8} />

  return (
    <Paper sx={{ borderRadius: 2, overflow: 'hidden' }}>
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="h6" sx={{ fontWeight: 700 }}>Shift Schedule</Typography>
        <TextField size="small" type="date" value={rosterDate} onChange={e => { setRosterDate(e.target.value); load(e.target.value) }} label="Roster Date" slotProps={{ inputLabel: { shrink: true } }} />
        {canManage && <Box sx={{ ml: 'auto' }}><Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setForm(emptyShiftForm()); setDialogOpen(true) }}>Schedule Shift</Button></Box>}
      </Box>
      {shifts.length === 0 ? (
        <EmptyState title="No shifts" message={rosterDate ? 'No shifts on this date' : 'Schedule shifts for your team members'} />
      ) : (
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Member</TableCell>
                <TableCell>Team</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Start</TableCell>
                <TableCell>End</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Notes</TableCell>
                {canManage && <TableCell align="right">Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {shifts.map(s => (
                <TableRow key={s.id} hover>
                  <TableCell sx={{ fontWeight: 600 }}>{s.memberName || '-'}</TableCell>
                  <TableCell>{s.teamName || '-'}</TableCell>
                  <TableCell><Chip label={s.shiftType?.replace('_', ' ')} size="small" sx={{ fontWeight: 600 }} /></TableCell>
                  <TableCell>{fmt(s.shiftStart)}</TableCell>
                  <TableCell>{fmt(s.shiftEnd)}</TableCell>
                  <TableCell><Chip label={s.shiftStatus?.replace('_', ' ')} size="small" sx={{ backgroundColor: `${shiftStatusColors[s.shiftStatus]}20`, color: shiftStatusColors[s.shiftStatus], fontWeight: 600 }} /></TableCell>
                  <TableCell>{s.notes || '-'}</TableCell>
                  {canManage && (
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                        {s.shiftStatus === 'SCHEDULED' && <IconButton size="small" onClick={() => updateShiftStatus(s, 'ACTIVE')} sx={{ color: '#2E7D32' }} title="Start Shift"><CheckCircleIcon fontSize="small" /></IconButton>}
                        {s.shiftStatus === 'ACTIVE' && <IconButton size="small" onClick={() => updateShiftStatus(s, 'COMPLETED')} sx={{ color: '#0F4C81' }} title="Complete Shift"><AssignmentIcon fontSize="small" /></IconButton>}
                        {(s.shiftStatus === 'SCHEDULED' || s.shiftStatus === 'ACTIVE') && <IconButton size="small" onClick={() => updateShiftStatus(s, 'CANCELLED')} sx={{ color: '#C62828' }} title="Cancel Shift"><DeleteIcon fontSize="small" /></IconButton>}
                        {s.shiftStatus !== 'COMPLETED' && <IconButton size="small" onClick={() => { setEditing(s); setForm({ teamId: s.teamId || '', memberId: s.memberId || '', shiftType: s.shiftType, shiftStart: s.shiftStart || '', shiftEnd: s.shiftEnd || '', notes: s.notes || '' }); setDialogOpen(true) }} sx={{ color: '#0F4C81' }} title="Edit"><EditIcon fontSize="small" /></IconButton>}
                      </Box>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600 }}>{editing ? 'Edit Shift' : 'Schedule Shift'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Select size="small" fullWidth value={form.teamId} onChange={e => { setForm(f => ({ ...f, teamId: e.target.value, memberId: '' })) }} displayEmpty>
              <MenuItem value="" disabled>Select Team</MenuItem>
              {teams.map(t => <MenuItem key={t.id} value={t.id}>{t.teamName}</MenuItem>)}
            </Select>
            <Select size="small" fullWidth value={form.memberId} onChange={e => setForm(f => ({ ...f, memberId: e.target.value }))} displayEmpty disabled={!form.teamId}>
              <MenuItem value="" disabled>Select Member</MenuItem>
              {(membersByTeam[form.teamId] || []).map(m => <MenuItem key={m.id} value={m.id}>{m.name} ({m.role || 'Member'})</MenuItem>)}
            </Select>
            <Select size="small" fullWidth value={form.shiftType} onChange={e => setForm(f => ({ ...f, shiftType: e.target.value }))}>
              {['DAY', 'NIGHT', 'ROTATION', 'ON_CALL'].map(t => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
            </Select>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}><TextField label="Shift Start" size="small" fullWidth type="datetime-local" value={form.shiftStart} onChange={e => setForm(f => ({ ...f, shiftStart: e.target.value }))} /></Grid>
              <Grid size={{ xs: 6 }}><TextField label="Shift End" size="small" fullWidth type="datetime-local" value={form.shiftEnd} onChange={e => setForm(f => ({ ...f, shiftEnd: e.target.value }))} /></Grid>
            </Grid>
            <TextField label="Notes" size="small" fullWidth value={form.notes} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>{editing ? 'Save' : 'Schedule'}</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  )
}

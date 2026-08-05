import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../services/api'
import toast from 'react-hot-toast'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import SectionCard from '../components/SectionCard'
import StatusBadge from '../components/StatusBadge'
import EmptyState from '../components/EmptyState'
import { TableSkeleton, StatsSkeleton } from '../components/LoadingSkeleton'
import {
  Box, Typography, Table, TableContainer, TableHead, TableBody,
  TableRow, TableCell, Button, TextField, Select, MenuItem, Dialog,
  DialogTitle, DialogContent, DialogActions, IconButton, Chip,
  InputAdornment, TablePagination, useTheme
} from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import SearchIcon from '@mui/icons-material/Search'
import LockResetIcon from '@mui/icons-material/LockReset'
import SwapHorizIcon from '@mui/icons-material/SwapHoriz'
import BlockIcon from '@mui/icons-material/Block'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import PeopleIcon from '@mui/icons-material/People'
import ShieldIcon from '@mui/icons-material/Shield'
import GroupIcon from '@mui/icons-material/Group'

export default function UserManagement() {
  const { user: currentUser } = useAuth()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const theme = useTheme()
  const roleColors = { ADMIN: theme.palette.primary.main, USER: theme.palette.secondary.main }

  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [editUser, setEditUser] = useState(null)
  const [editForm, setEditForm] = useState({ email: '', phone: '', address: '', emergencyContact: '' })

  const [roleDialogOpen, setRoleDialogOpen] = useState(false)
  const [roleTarget, setRoleTarget] = useState(null)
  const [newRole, setNewRole] = useState('USER')

  const [passwordDialogOpen, setPasswordDialogOpen] = useState(false)
  const [passwordTarget, setPasswordTarget] = useState(null)
  const [newPassword, setNewPassword] = useState('')

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)

  useEffect(() => { loadUsers() }, [])

  const loadUsers = async () => {
    setLoading(true)
    try {
      const res = await userApi.getAll()
      setUsers(res.data)
    } catch {
      toast.error('Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  const filtered = users.filter(u => {
    const matchesSearch = u.username?.toLowerCase().includes(search.toLowerCase()) ||
      u.email?.toLowerCase().includes(search.toLowerCase())
    const matchesRole = roleFilter === 'ALL' || u.role === roleFilter
    const matchesStatus = statusFilter === 'ALL' || u.status === statusFilter
    return matchesSearch && matchesRole && matchesStatus
  })

  const totalUsers = users.length
  const activeUsers = users.filter(u => u.status === 'ACTIVE').length
  const adminUsers = users.filter(u => u.role === 'ADMIN').length
  const regularUsers = users.filter(u => u.role === 'USER').length

  const handleEditOpen = (u) => {
    setEditUser(u)
    setEditForm({
      email: u.email || '',
      phone: u.phone || '',
      address: u.address || '',
      emergencyContact: u.emergencyContact || ''
    })
    setEditDialogOpen(true)
  }

  const handleEditSave = async () => {
    try {
      await userApi.update(editUser.id, editForm)
      toast.success('User updated')
      setEditDialogOpen(false)
      loadUsers()
    } catch {
      toast.error('Failed to update user')
    }
  }

  const handleToggleStatus = async (u) => {
    try {
      if (u.status === 'ACTIVE') {
        await userApi.deactivate(u.id)
        toast.success('User deactivated')
      } else {
        await userApi.activate(u.id)
        toast.success('User activated')
      }
      loadUsers()
    } catch {
      toast.error('Failed to update status')
    }
  }

  const handleChangeRoleOpen = (u) => {
    setRoleTarget(u)
    setNewRole(u.role)
    setRoleDialogOpen(true)
  }

  const handleChangeRoleSave = async () => {
    try {
      await userApi.changeRole(roleTarget.id, newRole)
      toast.success('Role changed')
      setRoleDialogOpen(false)
      loadUsers()
    } catch {
      toast.error('Failed to change role')
    }
  }

  const handleResetPasswordOpen = (u) => {
    setPasswordTarget(u)
    setNewPassword('')
    setPasswordDialogOpen(true)
  }

  const handleResetPasswordSave = async () => {
    if (!newPassword || newPassword.length < 6) {
      toast.error('Password must be at least 6 characters')
      return
    }
    try {
      await userApi.resetPassword(passwordTarget.id, newPassword)
      toast.success('Password reset successfully')
      setPasswordDialogOpen(false)
    } catch {
      toast.error('Failed to reset password')
    }
  }

  const handleDeleteOpen = (u) => {
    setDeleteTarget(u)
    setDeleteDialogOpen(true)
  }

  const handleDeleteConfirm = async () => {
    try {
      await userApi.delete(deleteTarget.id)
      toast.success('User deleted')
      setDeleteDialogOpen(false)
      loadUsers()
    } catch {
      toast.error('Failed to delete user')
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex' }}>
        <Sidebar />
        <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
          <StatsSkeleton />
          <TableSkeleton rows={8} />
        </Box>
      </Box>
    )
  }

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="User Management"
          subtitle="Manage all system users, roles, and permissions"
        />

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4,1fr)' }, gap: 2, mb: 3 }}>
          {[
            { label: 'Total Users', value: totalUsers, icon: <PeopleIcon />, color: 'primary.main' },
            { label: 'Active Users', value: activeUsers, icon: <CheckCircleIcon />, color: 'success.main' },
            { label: 'Admins', value: adminUsers, icon: <ShieldIcon />, color: 'accent.main' },
            { label: 'Regular Users', value: regularUsers, icon: <GroupIcon />, color: 'warning.main' },
          ].map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} color={s.color} />
          ))}
        </Box>

        <SectionCard
          title="All Users"
          subtitle={`${filtered.length} of ${users.length} users`}
          action={
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
              <TextField
                size="small"
                placeholder="Search by username or email..."
                value={search}
                onChange={e => { setSearch(e.target.value); setPage(0) }}
                sx={{ minWidth: 280 }}
                slotProps={{
                  input: {
                    startAdornment: <InputAdornment position="start"><SearchIcon sx={{ color: theme.palette.text.secondary }} /></InputAdornment>
                  }
                }}
              />
              <Select size="small" value={roleFilter} onChange={e => { setRoleFilter(e.target.value); setPage(0) }} sx={{ minWidth: 130 }}>
                <MenuItem value="ALL">All Roles</MenuItem>
                <MenuItem value="USER">User</MenuItem>
                <MenuItem value="ADMIN">Admin</MenuItem>
              </Select>
              <Select size="small" value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0) }} sx={{ minWidth: 130 }}>
                <MenuItem value="ALL">All Status</MenuItem>
                <MenuItem value="ACTIVE">Active</MenuItem>
                <MenuItem value="INACTIVE">Inactive</MenuItem>
              </Select>
            </Box>
          }
        >
          {filtered.length === 0 ? (
            <EmptyState title="No users found" message="Try adjusting your search or filters" />
          ) : (
            <>
              <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Username</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>Role</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Last Login</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map(u => (
                      <TableRow key={u.id} hover>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                            <Box sx={{ width: 32, height: 32, borderRadius: '50%', backgroundColor: theme.palette.primary.main, color: theme.palette.primary.contrastText, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 700 }}>
                              {u.username?.[0]?.toUpperCase()}
                            </Box>
                            <Typography variant="body2" sx={{ fontWeight: 600 }}>{u.username}</Typography>
                          </Box>
                        </TableCell>
                        <TableCell>{u.email}</TableCell>
                        <TableCell>
                          <Chip label={u.role} size="small" sx={{ backgroundColor: `${roleColors[u.role]}20`, color: roleColors[u.role], fontWeight: 600 }} />
                        </TableCell>
                        <TableCell>
                          <StatusBadge status={u.status || 'ACTIVE'} />
                        </TableCell>
                        <TableCell>{u.lastLogin ? new Date(u.lastLogin).toLocaleDateString() : 'Never'}</TableCell>
                        <TableCell align="right">
                          <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                            <IconButton size="small" onClick={() => handleEditOpen(u)} sx={{ color: theme.palette.primary.main }} title="Edit User"><EditIcon fontSize="small" /></IconButton>
                            <IconButton size="small" onClick={() => handleToggleStatus(u)} sx={{ color: u.status === 'ACTIVE' ? theme.palette.error.main : theme.palette.success.main }} title={u.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}>
                              {u.status === 'ACTIVE' ? <BlockIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
                            </IconButton>
                            <IconButton size="small" onClick={() => handleChangeRoleOpen(u)} sx={{ color: theme.palette.accent.main }} title="Change Role"><SwapHorizIcon fontSize="small" /></IconButton>
                            <IconButton size="small" onClick={() => handleResetPasswordOpen(u)} sx={{ color: theme.palette.secondary.main }} title="Reset Password"><LockResetIcon fontSize="small" /></IconButton>
                            <IconButton size="small" onClick={() => handleDeleteOpen(u)} sx={{ color: theme.palette.error.main }} title="Delete"><DeleteIcon fontSize="small" /></IconButton>
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
        </SectionCard>

        <Footer />

        <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="xs" fullWidth>
          <DialogTitle sx={{ fontWeight: 600 }}>Edit User — {editUser?.username}</DialogTitle>
          <DialogContent>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
              <TextField label="Email" fullWidth size="small" value={editForm.email} onChange={e => setEditForm(f => ({ ...f, email: e.target.value }))} />
              <TextField label="Phone" fullWidth size="small" value={editForm.phone} onChange={e => setEditForm(f => ({ ...f, phone: e.target.value }))} />
              <TextField label="Address" fullWidth size="small" multiline rows={2} value={editForm.address} onChange={e => setEditForm(f => ({ ...f, address: e.target.value }))} />
              <TextField label="Emergency Contact" fullWidth size="small" value={editForm.emergencyContact} onChange={e => setEditForm(f => ({ ...f, emergencyContact: e.target.value }))} />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
            <Button variant="contained" onClick={handleEditSave}>Save</Button>
          </DialogActions>
        </Dialog>

        <Dialog open={roleDialogOpen} onClose={() => setRoleDialogOpen(false)} maxWidth="xs" fullWidth>
          <DialogTitle sx={{ fontWeight: 600 }}>Change Role — {roleTarget?.username}</DialogTitle>
          <DialogContent>
            <Select fullWidth size="small" value={newRole} onChange={e => setNewRole(e.target.value)} sx={{ mt: 1 }}>
              <MenuItem value="USER">User</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </Select>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setRoleDialogOpen(false)}>Cancel</Button>
            <Button variant="contained" onClick={handleChangeRoleSave}>Change</Button>
          </DialogActions>
        </Dialog>

        <Dialog open={passwordDialogOpen} onClose={() => setPasswordDialogOpen(false)} maxWidth="xs" fullWidth>
          <DialogTitle sx={{ fontWeight: 600 }}>Reset Password — {passwordTarget?.username}</DialogTitle>
          <DialogContent>
            <TextField label="New Password" type="password" fullWidth size="small" value={newPassword} onChange={e => setNewPassword(e.target.value)} sx={{ mt: 1 }} />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setPasswordDialogOpen(false)}>Cancel</Button>
            <Button variant="contained" onClick={handleResetPasswordSave}>Reset</Button>
          </DialogActions>
        </Dialog>

        <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)} maxWidth="xs" fullWidth>
          <DialogTitle sx={{ fontWeight: 600 }}>Confirm Delete</DialogTitle>
          <DialogContent>
            <Typography>Are you sure you want to delete user <strong>{deleteTarget?.username}</strong>? This action cannot be undone.</Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
            <Button variant="contained" color="error" onClick={handleDeleteConfirm}>Delete</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Box>
  )
}

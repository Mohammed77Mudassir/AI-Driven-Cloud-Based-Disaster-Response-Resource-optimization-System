import { useState, useEffect } from 'react'
import {
  Box, Typography, Card, CardContent, Grid, Button, TextField, Tabs, Tab,
  Chip, Avatar, Alert, List, ListItem, ListItemText, ListItemIcon, Divider,
  InputAdornment, useTheme
} from '@mui/material'
import {
  Person as PersonIcon, Security as SecurityIcon, History as HistoryIcon,
  Badge as BadgeIcon, Email as EmailIcon, Phone as PhoneIcon,
  LocationOn as LocationIcon, Warning as WarningIcon, Save as SaveIcon
} from '@mui/icons-material'
import { userApi, getStoredUserSafe } from '../services/api'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import EmptyState from '../components/EmptyState'
import { TableSkeleton } from '../components/LoadingSkeleton'
import toast from 'react-hot-toast'

const tabIcons = { 0: <PersonIcon />, 1: <HistoryIcon />, 2: <SecurityIcon /> }

export default function UserProfile() {
  const [tab, setTab] = useState(0)
  const [profile, setProfile] = useState(null)
  const [activity, setActivity] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({ email: '', phone: '', address: '', emergencyContact: '' })
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [passwordError, setPasswordError] = useState('')
  const theme = useTheme()

  const user = getStoredUserSafe()

  useEffect(() => { loadProfile() }, [])

  const loadProfile = async () => {
    setLoading(true)
    try {
      const [profileRes, activityRes] = await Promise.all([
        userApi.getProfile().catch(() => null),
        userApi.getActivity(user.id).catch(() => null)
      ])
      const data = profileRes?.data || {}
      setProfile(data)
      setForm({
        email: data.email || user.email || '',
        phone: data.phone || '',
        address: data.address || '',
        emergencyContact: data.emergencyContact || ''
      })
      setActivity(activityRes?.data || null)
    } catch {
      toast.error('Failed to load profile')
    } finally {
      setLoading(false)
    }
  }

  const handleSaveProfile = async () => {
    setSaving(true)
    try {
      await userApi.updateProfile(form)
      toast.success('Profile updated')
      loadProfile()
    } catch {
      toast.error('Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  const handleChangePassword = async () => {
    setPasswordError('')
    if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
      setPasswordError('All fields are required')
      return
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError('New passwords do not match')
      return
    }
    if (passwordForm.newPassword.length < 6) {
      setPasswordError('Password must be at least 6 characters')
      return
    }
    try {
      await userApi.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      })
      toast.success('Password changed')
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch {
      setPasswordError('Failed to change password. Check your current password.')
    }
  }

  const firstLetter = (profile?.username || user?.username || 'U')[0].toUpperCase()
  const displayName = profile?.username || user?.username || 'User'
  const displayEmail = profile?.email || user?.email || ''
  const displayRole = profile?.role || user?.role || 'USER'

  const activityList = activity?.recentActivity || activity?.activities || activity?.recentActivities || []

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <TableSkeleton rows={6} />
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Profile"
          subtitle="Manage your account details, activity, and security"
        />

        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, flexWrap: 'wrap' }}>
              <Avatar sx={{
                width: 72, height: 72, bgcolor: 'primary.main', color: 'primary.contrastText',
                fontSize: '1.8rem', fontWeight: 700
              }}>
                {firstLetter}
              </Avatar>
              <Box sx={{ flex: 1 }}>
                <Typography variant="h5" sx={{ fontWeight: 700 }}>{displayName}</Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>{displayEmail}</Typography>
                <Chip
                  icon={<BadgeIcon />}
                  label={displayRole}
                  size="small"
                  color={displayRole === 'ADMIN' ? 'error' : 'primary'}
                  variant="outlined"
                />
              </Box>
            </Box>
          </CardContent>
        </Card>

        <Card>
          <Tabs
            value={tab} onChange={(_, v) => setTab(v)}
            sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
          >
            {['Profile', 'Activity', 'Security'].map((label, i) => (
              <Tab key={label} icon={tabIcons[i]} iconPosition="start" label={label} />
            ))}
          </Tabs>

          {tab === 0 && (
            <CardContent>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth label="Email" type="email" value={form.email}
                    onChange={e => setForm({ ...form, email: e.target.value })}
                    slotProps={{ input: { startAdornment: <InputAdornment position="start"><EmailIcon sx={{ color: 'text.secondary', fontSize: 20 }} /></InputAdornment> } }}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth label="Phone" value={form.phone}
                    onChange={e => setForm({ ...form, phone: e.target.value })}
                    slotProps={{ input: { startAdornment: <InputAdornment position="start"><PhoneIcon sx={{ color: 'text.secondary', fontSize: 20 }} /></InputAdornment> } }}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth label="Address" value={form.address}
                    onChange={e => setForm({ ...form, address: e.target.value })}
                    slotProps={{ input: { startAdornment: <InputAdornment position="start"><LocationIcon sx={{ color: 'text.secondary', fontSize: 20 }} /></InputAdornment> } }}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth label="Emergency Contact" value={form.emergencyContact}
                    onChange={e => setForm({ ...form, emergencyContact: e.target.value })}
                    slotProps={{ input: { startAdornment: <InputAdornment position="start"><WarningIcon sx={{ color: 'text.secondary', fontSize: 20 }} /></InputAdornment> } }}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Button variant="contained" startIcon={<SaveIcon />} onClick={handleSaveProfile} disabled={saving}>
                      {saving ? 'Saving...' : 'Update Profile'}
                    </Button>
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          )}

          {tab === 1 && (
            <CardContent>
              <Grid container spacing={3} sx={{ mb: 3 }}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Box sx={{ textAlign: 'center', p: 2, bgcolor: theme.palette.action.hover, borderRadius: 2 }}>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: theme.palette.primary.main }}>
                      {activity?.lastLogin ? new Date(activity.lastLogin).toLocaleDateString() : 'N/A'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">Last Login</Typography>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Box sx={{ textAlign: 'center', p: 2, bgcolor: theme.palette.action.hover, borderRadius: 2 }}>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: theme.palette.primary.main }}>
                      {activity?.totalReports || 0}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">Total Reports</Typography>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Box sx={{ textAlign: 'center', p: 2, bgcolor: theme.palette.action.hover, borderRadius: 2 }}>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: theme.palette.primary.main }}>
                      {activity?.assignedDisasters || 0}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">Assigned Disasters</Typography>
                  </Box>
                </Grid>
              </Grid>

              <Typography variant="h6" sx={{ mb: 1 }}>Recent Activity</Typography>
              {activityList.length === 0 ? (
                <EmptyState title="No activity" message="No recent activity recorded." />
              ) : (
                <List>
                  {activityList.map((act, i) => (
                    <Box key={i}>
                      {i > 0 && <Divider component="li" />}
                      <ListItem>
                        <ListItemIcon sx={{ minWidth: 36 }}>
                          <Box sx={{
                            width: 8, height: 8, borderRadius: '50%',
                            backgroundColor: [theme.palette.primary.main, theme.palette.secondary.main, theme.palette.accent.main, theme.palette.error.main][i % 4]
                          }} />
                        </ListItemIcon>
                        <ListItemText
                          primary={act.action || act.description || act.message || act.type}
                          secondary={act.timestamp ? new Date(act.timestamp).toLocaleString() : ''}
                        />
                      </ListItem>
                    </Box>
                  ))}
                </List>
              )}
            </CardContent>
          )}

          {tab === 2 && (
            <CardContent>
              <Grid container spacing={2} maxWidth={500}>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth label="Current Password" type="password"
                    value={passwordForm.currentPassword}
                    onChange={e => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth label="New Password" type="password"
                    value={passwordForm.newPassword}
                    onChange={e => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth label="Confirm New Password" type="password"
                    value={passwordForm.confirmPassword}
                    onChange={e => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                  />
                </Grid>
                {passwordError && (
                  <Grid size={{ xs: 12 }}>
                    <Alert severity="error">{passwordError}</Alert>
                  </Grid>
                )}
                <Grid size={{ xs: 12 }}>
                  <Button variant="contained" onClick={handleChangePassword}>
                    Change Password
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          )}
        </Card>

        <Footer />
      </Box>
    </Box>
  )
}

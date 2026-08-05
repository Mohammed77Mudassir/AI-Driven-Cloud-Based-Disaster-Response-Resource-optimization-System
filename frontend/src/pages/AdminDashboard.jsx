import { useState, useEffect, useMemo } from 'react'
import { disasterApi } from '../services/api'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import ChartCard from '../components/ChartCard'
import SectionCard from '../components/SectionCard'
import StatusBadge from '../components/StatusBadge'
import { StatsSkeleton, ChartSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import toast from 'react-hot-toast'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, AreaChart, Area } from 'recharts'
import {
  Box, Typography, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, TablePagination, TextField, InputAdornment,
  Select, MenuItem, FormControl, IconButton, Dialog, DialogTitle, DialogContent,
  DialogContentText, DialogActions, Button, useTheme
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import AssessmentIcon from '@mui/icons-material/Assessment'
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty'
import SyncIcon from '@mui/icons-material/Sync'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import DeleteIcon from '@mui/icons-material/Delete'

const COLORS = ['#F87171', '#FBBF24', '#4ADE80', '#4DA3FF']
const statusOptions = ['PENDING', 'VERIFIED', 'ASSIGNED', 'RESOURCES_DISPATCHED', 'IN_PROGRESS', 'RESOLVED']

const formatDate = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleDateString()
}

export default function AdminDashboard() {
  const [disasters, setDisasters] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [deleteDialog, setDeleteDialog] = useState(null)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const theme = useTheme()
  const gridColor = theme.palette.divider
  const tooltipStyle = {
    backgroundColor: theme.palette.background.paper,
    border: `1px solid ${theme.palette.divider}`,
    borderRadius: 10,
    fontSize: '0.8rem',
  }

  useEffect(() => { loadDisasters() }, [])
  useEffect(() => { setPage(0) }, [search, statusFilter])

  const loadDisasters = async () => {
    try {
      const res = await disasterApi.getAll()
      setDisasters(res.data.content || [])
    } catch {
      toast.error('Failed to load disasters')
    } finally {
      setLoading(false)
    }
  }

  const handleStatusUpdate = async (id, newStatus) => {
    try {
      await disasterApi.updateStatus(id, { status: newStatus })
      toast.success('Status updated')
      loadDisasters()
    } catch {
      toast.error('Failed to update status')
    }
  }

  const handleDelete = async (id) => {
    try {
      await disasterApi.delete(id)
      toast.success('Disaster deleted')
      setDeleteDialog(null)
      loadDisasters()
    } catch {
      toast.error('Failed to delete')
    }
  }

  const filtered = disasters.filter(d => {
    const q = search.toLowerCase()
    const matchesSearch = (d.disasterType || '').toLowerCase().includes(q) ||
      (d.location || '').toLowerCase().includes(q) ||
      (d.reportedBy || '').toLowerCase().includes(q)
    const matchesStatus = statusFilter === 'ALL' || d.status === statusFilter
    return matchesSearch && matchesStatus
  })

  const statusCounts = {
    PENDING: disasters.filter(d => d.status === 'PENDING').length,
    ASSIGNED: disasters.filter(d => d.status === 'ASSIGNED').length,
    IN_PROGRESS: disasters.filter(d => d.status === 'IN_PROGRESS').length,
    RESOLVED: disasters.filter(d => d.status === 'RESOLVED').length,
  }

  const pieData = [
    { name: 'Pending', value: statusCounts.PENDING },
    { name: 'Assigned', value: statusCounts.ASSIGNED },
    { name: 'In Progress', value: statusCounts.IN_PROGRESS },
    { name: 'Resolved', value: statusCounts.RESOLVED },
  ].filter(d => d.value > 0)

  const severityData = [
    { name: 'Low', count: disasters.filter(d => d.severity === 'Low').length },
    { name: 'Medium', count: disasters.filter(d => d.severity === 'Medium').length },
    { name: 'High', count: disasters.filter(d => d.severity === 'High').length },
    { name: 'Critical', count: disasters.filter(d => d.severity === 'Critical').length },
  ]

  const monthlyTrend = useMemo(() => {
    const counts = {}
    disasters.forEach(d => {
      if (!d.date) return
      const parsed = new Date(d.date)
      if (Number.isNaN(parsed.getTime())) return
      const month = parsed.toISOString().slice(0, 7)
      counts[month] = (counts[month] || 0) + 1
    })
    return Object.entries(counts)
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([month, count]) => ({ month, count }))
  }, [disasters])

  const paged = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

  const statCards = [
    { icon: <AssessmentIcon />, value: disasters.length, label: 'Total Reports', color: 'primary.main' },
    { icon: <HourglassEmptyIcon />, value: statusCounts.PENDING, label: 'Pending', color: 'warning.main' },
    { icon: <SyncIcon />, value: statusCounts.IN_PROGRESS, label: 'In Progress', color: 'accent.main' },
    { icon: <CheckCircleIcon />, value: statusCounts.RESOLVED, label: 'Resolved', color: 'success.main' },
  ]

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <StatsSkeleton />
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 2 }}>
          <ChartSkeleton /><ChartSkeleton />
        </Box>
        <Box sx={{ mt: 2 }}><TableSkeleton /></Box>
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Incident Overview"
          subtitle="Manage all disaster reports across the national command"
        />

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', lg: 'repeat(4, 1fr)' }, gap: 2, mb: 3 }}>
          {statCards.map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} color={s.color} />
          ))}
        </Box>

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mb: 3 }}>
          <ChartCard title="Disasters by Severity" subtitle="Distribution across severity bands">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={severityData}>
                <CartesianGrid strokeDasharray="3 3" stroke={gridColor} vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip contentStyle={tooltipStyle} cursor={{ fill: theme.palette.action.hover }} />
                <Bar dataKey="count" fill={theme.palette.primary.main} radius={[6, 6, 0, 0]} maxBarSize={48} />
              </BarChart>
            </ResponsiveContainer>
          </ChartCard>
          <ChartCard title="Status Distribution" subtitle="Current lifecycle breakdown">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={82} innerRadius={46} paddingAngle={3} strokeWidth={0}>
                  {pieData.map((_, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                </Pie>
                <Tooltip contentStyle={tooltipStyle} />
              </PieChart>
            </ResponsiveContainer>
          </ChartCard>
        </Box>

        <ChartCard title="Monthly Incident Trend" subtitle="Reports per month across the year" height={280} sx={{ mb: 3 }}>
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={monthlyTrend}>
              <defs>
                <linearGradient id="trendGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={theme.palette.primary.main} stopOpacity={0.8} />
                  <stop offset="95%" stopColor={theme.palette.primary.main} stopOpacity={0.05} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke={gridColor} vertical={false} />
              <XAxis dataKey="month" tick={{ fontSize: 11, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={tooltipStyle} />
              <Area type="monotone" dataKey="count" stroke={theme.palette.primary.main} fill="url(#trendGrad)" strokeWidth={2.5} dot={{ r: 3 }} />
            </AreaChart>
          </ResponsiveContainer>
        </ChartCard>

        <SectionCard
          title="All Disaster Reports"
          subtitle={`${filtered.length} incident${filtered.length === 1 ? '' : 's'} found`}
          action={
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
              <FormControl size="small" sx={{ minWidth: 140 }}>
                <Select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} displayEmpty aria-label="Filter by status">
                  <MenuItem value="ALL">All Status</MenuItem>
                  {statusOptions.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                </Select>
              </FormControl>
              <TextField
                size="small" placeholder="Search…" aria-label="Search reports"
                value={search}
                onChange={e => setSearch(e.target.value)}
                slotProps={{
                  input: {
                    startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>
                  }
                }}
                sx={{ width: { xs: '100%', sm: 220 } }}
              />
            </Box>
          }
        >
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Type</TableCell>
                  <TableCell>Location</TableCell>
                  <TableCell>Severity</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Reported By</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paged.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} sx={{ py: 4 }}>
                      <EmptyState title="No incidents found" message="No disasters match your criteria." />
                    </TableCell>
                  </TableRow>
                ) : paged.map(d => (
                  <TableRow key={d.id} hover>
                    <TableCell>
                      <Typography sx={{ fontWeight: 600, fontSize: '0.84rem' }}>{d.disasterType}</Typography>
                      {d.reportId && <Typography variant="caption" color="text.disabled" sx={{ fontFamily: 'monospace', fontSize: '0.65rem' }}>{d.reportId}</Typography>}
                    </TableCell>
                    <TableCell>{d.location}</TableCell>
                    <TableCell><StatusBadge status={d.severity} /></TableCell>
                    <TableCell><StatusBadge status={d.priority || '—'} /></TableCell>
                    <TableCell>
                      <FormControl size="small" sx={{ minWidth: 160 }}>
                        <Select
                          value={d.status}
                          onChange={e => handleStatusUpdate(d.id, e.target.value)}
                          aria-label={`Update status for ${d.disasterType}`}
                          sx={{ fontSize: '0.8rem', '& .MuiSelect-select': { py: 0.5 } }}
                        >
                          {statusOptions.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                        </Select>
                      </FormControl>
                    </TableCell>
                    <TableCell>{d.reportedBy}</TableCell>
                    <TableCell>{formatDate(d.date)}</TableCell>
                    <TableCell align="right">
                      <IconButton size="small" color="error" onClick={() => setDeleteDialog(d)} aria-label={`Delete ${d.disasterType}`}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={filtered.length}
            page={page}
            rowsPerPage={rowsPerPage}
            rowsPerPageOptions={[10, 25, 50]}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10))
              setPage(0)
            }}
          />
        </SectionCard>
        <Footer />

        <Dialog open={!!deleteDialog} onClose={() => setDeleteDialog(null)} maxWidth="xs" fullWidth>
          <DialogTitle>Delete Disaster Report</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Are you sure you want to delete this disaster report? This action cannot be undone.
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDeleteDialog(null)}>Cancel</Button>
            <Button onClick={() => handleDelete(deleteDialog.id)} color="error" variant="contained">Delete</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Box>
  )
}

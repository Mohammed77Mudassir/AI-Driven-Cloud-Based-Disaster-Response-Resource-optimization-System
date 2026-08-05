import { useState, useEffect } from 'react'
import { disasterApi } from '../services/api'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import { useWebSocket } from '../context/WebSocketContext'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import ChartCard from '../components/ChartCard'
import SectionCard from '../components/SectionCard'
import StatusBadge from '../components/StatusBadge'
import { StatsSkeleton, ChartSkeleton, TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts'
import {
  Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, InputAdornment, useTheme
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import AssessmentIcon from '@mui/icons-material/Assessment'
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty'
import SyncIcon from '@mui/icons-material/Sync'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'

const COLORS = ['#F87171', '#FBBF24', '#4ADE80', '#4DA3FF']

const safeDate = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleDateString()
}

export default function UserDashboard() {
  const [disasters, setDisasters] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const { connected } = useWebSocket()
  const theme = useTheme()
  const gridColor = theme.palette.divider
  const tooltipStyle = {
    backgroundColor: theme.palette.background.paper,
    border: `1px solid ${theme.palette.divider}`,
    borderRadius: 10,
    fontSize: '0.8rem',
  }

  useEffect(() => { loadDisasters() }, [])

  const loadDisasters = async () => {
    try {
      const res = await disasterApi.getMy()
      setDisasters(res.data.content || [])
    } catch {
    } finally {
      setLoading(false)
    }
  }

  const filtered = disasters.filter(d => {
    const q = search.toLowerCase()
    return (d.disasterType || '').toLowerCase().includes(q) ||
      (d.location || '').toLowerCase().includes(q) ||
      (d.status || '').toLowerCase().includes(q)
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
          title="Command Dashboard"
          subtitle="Live overview of your disaster reports and incident posture"
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

        <SectionCard title="My Disaster Reports" subtitle="Reports you submitted to the command center" action={
          <TextField
            size="small" placeholder="Search reports…" aria-label="Search reports"
            value={search}
            onChange={e => setSearch(e.target.value)}
            slotProps={{
              input: {
                startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>
              }
            }}
            sx={{ width: { xs: '100%', sm: 260 } }}
          />
        }>
          <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Type</TableCell>
                  <TableCell>Location</TableCell>
                  <TableCell>Severity</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Date</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filtered.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} sx={{ py: 4 }}>
                      <EmptyState title="No reports found" message="No disasters match your search criteria." />
                    </TableCell>
                  </TableRow>
                ) : filtered.map(d => (
                  <TableRow key={d.id} hover>
                    <TableCell>
                      <Box sx={{ fontWeight: 600, fontSize: '0.84rem' }}>{d.disasterType}</Box>
                    </TableCell>
                    <TableCell>{d.location}</TableCell>
                    <TableCell><StatusBadge status={d.severity} /></TableCell>
                    <TableCell><StatusBadge status={d.status} /></TableCell>
                    <TableCell>{safeDate(d.date)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
        <Footer />
      </Box>
    </Box>
  )
}

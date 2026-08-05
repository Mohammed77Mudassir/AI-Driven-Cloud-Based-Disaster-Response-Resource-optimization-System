import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatCard from '../components/StatCard'
import ChartCard from '../components/ChartCard'
import SectionCard from '../components/SectionCard'
import { StatsSkeleton, ChartSkeleton } from '../components/LoadingSkeleton'
import { analyticsApi, disasterApi, getStoredUserSafe } from '../services/api'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line } from 'recharts'
import {
  Box, Typography, Grid, LinearProgress, useTheme
} from '@mui/material'
import WavesIcon from '@mui/icons-material/Waves'
import LocalHospitalIcon from '@mui/icons-material/LocalHospital'
import HomeIcon from '@mui/icons-material/Home'
import PeopleIcon from '@mui/icons-material/People'
import InventoryIcon from '@mui/icons-material/Inventory'
import TimerIcon from '@mui/icons-material/Timer'

const COLORS = ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#ff8a65', '#ce93d8']

export default function AdvancedAnalytics() {
  const [analytics, setAnalytics] = useState(null)
  const [disasters, setDisasters] = useState([])
  const [loading, setLoading] = useState(true)
  const userRole = getStoredUserSafe().role
  const theme = useTheme()
  const gridColor = theme.palette.divider
  const tooltipStyle = {
    backgroundColor: theme.palette.background.paper,
    border: `1px solid ${theme.palette.divider}`,
    borderRadius: 10,
    fontSize: '0.8rem',
  }
  const tickStyle = { fontSize: 12, fill: theme.palette.text.secondary }

  useEffect(() => {
    Promise.all([
      analyticsApi.get().catch(() => null),
      userRole === 'ADMIN' ? disasterApi.getAll().catch(() => ({ data: { content: [] } })) : Promise.resolve({ data: { content: [] } })
    ]).then(([analyticsRes, disastersRes]) => {
      setAnalytics(analyticsRes?.data || null)
      setDisasters(disastersRes?.data?.content || [])
    }).finally(() => setLoading(false))
  }, [])

  const monthlyData = analytics?.disastersByMonth ? Object.entries(analytics.disastersByMonth).map(([month, count]) => ({ month, count })) : []
  const typeData = analytics?.disastersByType ? Object.entries(analytics.disastersByType).map(([name, value]) => ({ name, value })) : []
  const severityData = analytics?.disastersBySeverity ? Object.entries(analytics.disastersBySeverity).map(([name, count]) => ({ name, count })) : []
  const statusData = analytics?.disastersByStatus ? Object.entries(analytics.disastersByStatus).map(([name, value]) => ({ name, value })) : []

  const metrics = [
    { label: 'Avg Response Time', value: `${(analytics?.averageResponseTimeHours || 0).toFixed(1)}h` },
    { label: 'Avg Resolution Time', value: `${(analytics?.averageResolutionTimeHours || 0).toFixed(1)}h` },
    { label: 'Resource Utilization', value: `${analytics?.resourceUtilizationPercent || 0}%` },
    { label: 'Volunteer Activity', value: `${analytics?.volunteerActivityPercent || 0}%` },
    { label: 'Hospital Occupancy', value: `${analytics?.hospitalOccupancyPercent || 0}%` },
    { label: 'Shelter Occupancy', value: `${analytics?.shelterOccupancyPercent || 0}%` },
  ]

  const utilizationMetrics = [
    { label: 'Resource Utilization', value: analytics?.resourceUtilizationPercent || 0, color: theme.palette.primary.main },
    { label: 'Volunteer Activity', value: analytics?.volunteerActivityPercent || 0, color: theme.palette.success.main },
    { label: 'Hospital Occupancy', value: analytics?.hospitalOccupancyPercent || 0, color: theme.palette.accent.main },
    { label: 'Shelter Occupancy', value: analytics?.shelterOccupancyPercent || 0, color: theme.palette.error.main },
  ]

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <StatsSkeleton />
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 2 }}>
          <ChartSkeleton /><ChartSkeleton /><ChartSkeleton /><ChartSkeleton />
        </Box>
      </Box>
    </Box>
  )

  const statCards = [
    { icon: <WavesIcon />, value: analytics?.totalDisasters || 0, label: 'Total Disasters', color: 'primary.main' },
    { icon: <LocalHospitalIcon />, value: analytics?.totalHospitals || 0, label: 'Hospitals', color: 'success.main' },
    { icon: <HomeIcon />, value: analytics?.totalShelters || 0, label: 'Shelters', color: 'accent.main' },
    { icon: <PeopleIcon />, value: analytics?.totalVolunteers || 0, label: 'Volunteers', color: 'warning.main' },
    { icon: <InventoryIcon />, value: analytics?.totalResources || 0, label: 'Resources', color: 'primary.main' },
    { icon: <TimerIcon />, value: analytics?.averageResponseTimeHours || 0, label: 'Avg Response', sublabel: `${(analytics?.averageResponseTimeHours || 0).toFixed(1)} hours`, color: 'warning.main' },
  ]

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Advanced Analytics"
          subtitle="Comprehensive platform analytics and insights"
        />

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr 1fr', sm: '1fr 1fr 1fr', lg: 'repeat(6, 1fr)' }, gap: 2, mb: 3 }}>
          {statCards.map((s, i) => (
            <StatCard key={i} icon={s.icon} value={s.value} label={s.label} sublabel={s.sublabel} color={s.color} />
          ))}
        </Box>

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mb: 3 }}>
          {monthlyData.length > 0 && (
            <ChartCard title="Monthly Disasters">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={monthlyData}>
                  <CartesianGrid strokeDasharray="3 3" stroke={gridColor} vertical={false} />
                  <XAxis dataKey="month" tick={tickStyle} axisLine={false} tickLine={false} />
                  <YAxis tick={tickStyle} axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={tooltipStyle} cursor={{ fill: theme.palette.action.hover }} />
                  <Legend />
                  <Line type="monotone" dataKey="count" stroke={theme.palette.primary.main} strokeWidth={2} dot={{ r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </ChartCard>
          )}
          {typeData.length > 0 && (
            <ChartCard title="Disasters by Type">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={typeData}>
                  <CartesianGrid strokeDasharray="3 3" stroke={gridColor} vertical={false} />
                  <XAxis dataKey="name" tick={tickStyle} axisLine={false} tickLine={false} />
                  <YAxis tick={tickStyle} axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={tooltipStyle} cursor={{ fill: theme.palette.action.hover }} />
                  <Bar dataKey="value" fill={theme.palette.accent.main} radius={[4, 4, 0, 0]} maxBarSize={48} />
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          )}
        </Box>

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mb: 3 }}>
          {severityData.length > 0 && (
            <ChartCard title="Severity Trends">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={severityData}>
                  <CartesianGrid strokeDasharray="3 3" stroke={gridColor} vertical={false} />
                  <XAxis dataKey="name" tick={tickStyle} axisLine={false} tickLine={false} />
                  <YAxis tick={tickStyle} axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={tooltipStyle} cursor={{ fill: theme.palette.action.hover }} />
                  <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                    {severityData.map((_, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          )}
          {statusData.length > 0 && (
            <ChartCard title="Status Distribution">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={statusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                    {statusData.map((_, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                  </Pie>
                  <Tooltip contentStyle={tooltipStyle} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </ChartCard>
          )}
        </Box>

        <SectionCard title="Performance Metrics">
          <Grid container spacing={2}>
            {metrics.map((m, i) => (
              <Grid size={{ xs: 6, sm: 4, md: 2 }} key={i}>
                <Box sx={{ p: 1.5, bgcolor: theme.palette.action.hover, borderRadius: 1.5 }}>
                  <Typography variant="caption" color="text.secondary" display="block">{m.label}</Typography>
                  <Typography variant="h5" fontWeight={700}>{m.value}</Typography>
                </Box>
              </Grid>
            ))}
          </Grid>

          <Typography variant="h6" sx={{ mt: 3, mb: 2 }}>Utilization Rates</Typography>
          <Grid container spacing={2}>
            {utilizationMetrics.map((um, i) => (
              <Grid size={{ xs: 12, sm: 6 }} key={i}>
                <Box sx={{ mb: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="body2">{um.label}</Typography>
                    <Typography variant="body2" fontWeight={700}>{um.value}%</Typography>
                  </Box>
                  <LinearProgress variant="determinate" value={um.value} sx={{
                    height: 10, borderRadius: 5,
                    backgroundColor: theme.palette.action.hover,
                    '& .MuiLinearProgress-bar': { backgroundColor: um.color, borderRadius: 5 }
                  }} />
                </Box>
              </Grid>
            ))}
          </Grid>
        </SectionCard>
        <Footer />
      </Box>
    </Box>
  )
}

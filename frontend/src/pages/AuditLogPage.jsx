import { useState, useEffect } from 'react'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import SectionCard from '../components/SectionCard'
import { TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import { auditLogApi } from '../services/api'
import {
  Box, Typography, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, TextField, InputAdornment, ToggleButtonGroup, ToggleButton, useTheme
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import TimelineIcon from '@mui/icons-material/Timeline'
import TableChartIcon from '@mui/icons-material/TableChart'

export default function AuditLogPage() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [usernameFilter, setUsernameFilter] = useState('')
  const [view, setView] = useState('table')
  const theme = useTheme()

  useEffect(() => {
    auditLogApi.getAll().then(res => setLogs(res.data)).catch(() => {}).finally(() => setLoading(false))
  }, [])

  const filtered = logs.filter(log =>
    !usernameFilter || log.performedBy?.toLowerCase().includes(usernameFilter.toLowerCase())
  )

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <TableSkeleton />
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Audit Logs"
          subtitle="Track all system activities"
          actions={
            <ToggleButtonGroup value={view} exclusive size="small" onChange={(_, v) => v && setView(v)}>
              <ToggleButton value="table"><TableChartIcon sx={{ mr: 0.5, fontSize: 18 }} />Table</ToggleButton>
              <ToggleButton value="timeline"><TimelineIcon sx={{ mr: 0.5, fontSize: 18 }} />Timeline</ToggleButton>
            </ToggleButtonGroup>
          }
        />

        <SectionCard
          title="Activity History"
          subtitle={`${filtered.length} entries`}
          action={
            <TextField size="small" placeholder="Filter by username..."
              value={usernameFilter} onChange={e => setUsernameFilter(e.target.value)}
              slotProps={{
                input: {
                  startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>
                }
              }}
              sx={{ width: { xs: '100%', sm: 240 } }}
            />
          }
        >
          {view === 'table' ? (
            <TableContainer sx={{ mx: -1.5, width: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Action</TableCell>
                    <TableCell>Entity</TableCell>
                    <TableCell>Entity ID</TableCell>
                    <TableCell>Performed By</TableCell>
                    <TableCell>Details</TableCell>
                    <TableCell>Timestamp</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filtered.length === 0 ? (
                    <TableRow><TableCell colSpan={6} sx={{ py: 4 }}><EmptyState title="No audit logs" message="No activities have been recorded yet." /></TableCell></TableRow>
                  ) : filtered.map(log => (
                    <TableRow key={log.id} hover>
                      <TableCell><Chip label={log.action} size="small" color="primary" variant="outlined" sx={{ fontWeight: 600, fontSize: '0.7rem' }} /></TableCell>
                      <TableCell>{log.entityType}</TableCell>
                      <TableCell><Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{log.entityId}</Typography></TableCell>
                      <TableCell>{log.performedBy}</TableCell>
                      <TableCell sx={{ maxWidth: 300 }}><Typography variant="body2" noWrap>{log.details}</Typography></TableCell>
                      <TableCell><Typography variant="caption">{new Date(log.timestamp).toLocaleString()}</Typography></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Box sx={{ position: 'relative', pl: 3 }}>
              {filtered.length === 0 ? (
                <EmptyState title="No audit logs" />
              ) : (
                filtered.map(log => (
                  <Box key={log.id} sx={{ position: 'relative', pb: 2.5, pl: 3, borderLeft: `2px solid ${theme.palette.divider}`, ml: 1 }}>
                    <Box sx={{
                      position: 'absolute', left: -8, top: 4, width: 14, height: 14, borderRadius: '50%',
                      backgroundColor: theme.palette.primary.main, border: `2px solid ${theme.palette.background.paper}`
                    }} />
                    <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 0.5 }}>
                      <Chip label={log.action} size="small" color="primary" variant="outlined" sx={{ fontWeight: 600, fontSize: '0.65rem' }} />
                      <Typography variant="caption" color="text.secondary">{new Date(log.timestamp).toLocaleString()}</Typography>
                    </Box>
                    <Typography variant="body2">
                      <strong>{log.performedBy}</strong> performed <strong>{log.action}</strong> on {log.entityType} <Typography variant="caption" component="span" sx={{ fontFamily: 'monospace' }}>#{log.entityId}</Typography>
                    </Typography>
                    {log.details && <Typography variant="caption" color="text.secondary">{log.details}</Typography>}
                  </Box>
                ))
              )}
            </Box>
          )}
        </SectionCard>
        <Footer />
      </Box>
    </Box>
  )
}

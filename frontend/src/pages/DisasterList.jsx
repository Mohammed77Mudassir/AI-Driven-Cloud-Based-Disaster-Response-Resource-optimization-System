import { useState, useEffect, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { disasterApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import StatusBadge from '../components/StatusBadge'
import { TableSkeleton } from '../components/LoadingSkeleton'
import EmptyState from '../components/EmptyState'
import toast from 'react-hot-toast'
import {
  Box, Card, CardContent, Typography, Chip, TextField, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, InputAdornment, FormControl, Select,
  MenuItem, Grid, TablePagination, Button, ToggleButton, ToggleButtonGroup, Tooltip, IconButton
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import AddIcon from '@mui/icons-material/Add'
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward'
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward'
import VisibilityIcon from '@mui/icons-material/Visibility'
import { DISASTER_TYPES, SEVERITIES, PRIORITIES, STATUSES, statusLabel } from '../constants/disaster'

const safeDate = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleDateString()
}

const SortableHeader = ({ label, field, sortBy, sortDir, onSort }) => (
  <TableCell
    component="th"
    scope="col"
    aria-sort={sortBy === field ? (sortDir === 'asc' ? 'ascending' : 'descending') : 'none'}
    sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
    tabIndex={0}
    role="button"
    aria-label={`Sort by ${label}`}
    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); onSort(field) } }}
    onClick={() => onSort(field)}
  >
    {label} {sortBy === field && (sortDir === 'asc' ? '↑' : '↓')}
  </TableCell>
)

const SORT_FIELDS = [
  { value: 'date', label: 'Date' },
  { value: 'disasterType', label: 'Type' },
  { value: 'severity', label: 'Severity' },
  { value: 'status', label: 'Status' },
  { value: 'priority', label: 'Priority' },
  { value: 'location', label: 'Location' },
]

export default function DisasterList() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const legacyUser = user?.role === 'USER'

  const [data, setData] = useState({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })
  const [loading, setLoading] = useState(true)
  const [viewMode, setViewMode] = useState(legacyUser ? 'mine' : 'all')

  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [filters, setFilters] = useState({ type: '', severity: '', status: '', priority: '', source: '' })
  const [sortBy, setSortBy] = useState('date')
  const [sortDir, setSortDir] = useState('desc')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)

  const firstRun = useRef(true)

  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedSearch(search)
      setPage(0)
    }, 400)
    return () => clearTimeout(t)
  }, [search])

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const params = {
        search: debouncedSearch || undefined,
        type: filters.type || undefined,
        severity: filters.severity || undefined,
        status: filters.status || undefined,
        priority: filters.priority || undefined,
        source: viewMode === 'all' ? (filters.source || undefined) : undefined,
        page,
        size,
        sortBy,
        sortDir,
      }
      const res = viewMode === 'all' && !legacyUser
        ? await disasterApi.getAll(params)
        : await disasterApi.getMy(params)
      setData(res.data)
    } catch {
      toast.error('Failed to load disasters')
    } finally {
      setLoading(false)
    }
  }, [debouncedSearch, filters, viewMode, page, size, sortBy, sortDir, legacyUser])

  useEffect(() => {
    if (firstRun.current) {
      firstRun.current = false
    }
    load()
  }, [load])

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(d => d === 'asc' ? 'desc' : 'asc')
    } else {
      setSortBy(field)
      setSortDir('asc')
    }
    setPage(0)
  }

  const clearFilters = () => {
    setFilters({ type: '', severity: '', status: '', priority: '', source: '' })
    setSearch('')
    setDebouncedSearch('')
    setSortBy('date')
    setSortDir('desc')
    setPage(0)
  }

  const activeFilters = Object.values(filters).filter(Boolean).length + (debouncedSearch ? 1 : 0)

  if (loading && data.content.length === 0) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}><TableSkeleton /></Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Disaster Reports"
          subtitle="Search, filter, sort and track disaster reports across the command center."
          actions={
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/report')}>
              Report Disaster
            </Button>
          }
        />

        {!legacyUser && (
          <ToggleButtonGroup
            exclusive size="small" value={viewMode}
            onChange={(_, v) => { if (v) { setViewMode(v); setPage(0) } }}
            sx={{ mb: 2 }}
          >
            <ToggleButton value="all">All Reports</ToggleButton>
            <ToggleButton value="mine">My Reports</ToggleButton>
          </ToggleButtonGroup>
        )}

        <Card>
          <CardContent>
            {/* Toolbar */}
            <Grid container spacing={1.5} sx={{ mb: 2 }}>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <TextField
                  size="small" fullWidth placeholder="Search type, location, description, report ID..."
                  aria-label="Search disasters"
                  value={search} onChange={e => setSearch(e.target.value)}
                  slotProps={{
                    input: {
                      startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>,
                    }
                  }}
                />
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <FormControl fullWidth size="small">
                  <Select displayEmpty value={filters.type} onChange={e => { setFilters({ ...filters, type: e.target.value }); setPage(0) }}>
                    <MenuItem value="">All Types</MenuItem>
                    {DISASTER_TYPES.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <FormControl fullWidth size="small">
                  <Select displayEmpty value={filters.severity} onChange={e => { setFilters({ ...filters, severity: e.target.value }); setPage(0) }}>
                    <MenuItem value="">All Severities</MenuItem>
                    {SEVERITIES.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <FormControl fullWidth size="small">
                  <Select displayEmpty value={filters.status} onChange={e => { setFilters({ ...filters, status: e.target.value }); setPage(0) }}>
                    <MenuItem value="">All Statuses</MenuItem>
                    {STATUSES.map(s => <MenuItem key={s} value={s}>{statusLabel[s]}</MenuItem>)}
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <FormControl fullWidth size="small">
                  <Select displayEmpty value={filters.priority} onChange={e => { setFilters({ ...filters, priority: e.target.value }); setPage(0) }}>
                    <MenuItem value="">All Priorities</MenuItem>
                    {PRIORITIES.map(p => <MenuItem key={p} value={p}>{p.charAt(0) + p.slice(1).toLowerCase()}</MenuItem>)}
                  </Select>
                </FormControl>
              </Grid>
              {viewMode === 'all' && !legacyUser && (
                <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                  <FormControl fullWidth size="small">
                    <Select displayEmpty value={filters.source} onChange={e => { setFilters({ ...filters, source: e.target.value }); setPage(0) }}>
                      <MenuItem value="">All Sources</MenuItem>
                      <MenuItem value="PUBLIC">Public Citizen</MenuItem>
                      <MenuItem value="SYSTEM">System / Personnel</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              )}
              <Grid size={{ xs: 12, sm: 8, md: 5 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, height: '100%' }}>
                  <FormControl size="small" sx={{ minWidth: 130 }}>
                    <Select value={sortBy} onChange={e => { setSortBy(e.target.value); setPage(0) }}>
                      {SORT_FIELDS.map(f => <MenuItem key={f.value} value={f.value}>{f.label}</MenuItem>)}
                    </Select>
                  </FormControl>
                  <IconButton size="small" onClick={() => { setSortDir(d => d === 'asc' ? 'desc' : 'asc'); setPage(0) }}>
                    {sortDir === 'asc' ? <ArrowUpwardIcon /> : <ArrowDownwardIcon />}
                  </IconButton>
                  <Typography variant="caption" color="text.secondary">Sort</Typography>
                </Box>
              </Grid>
            </Grid>

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
              <Typography variant="body2" color="text.secondary">
                {data.totalElements} report{data.totalElements !== 1 ? 's' : ''}
                {activeFilters > 0 && ` · ${activeFilters} active filter${activeFilters > 1 ? 's' : ''}`}
              </Typography>
              {activeFilters > 0 && (
                <Button size="small" color="inherit" onClick={clearFilters}>Clear filters</Button>
              )}
            </Box>

            <TableContainer component={Paper} variant="outlined" sx={{ boxShadow: 'none' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <SortableHeader label="Type" field="disasterType" sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
                    <SortableHeader label="Location" field="location" sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
                    <SortableHeader label="Severity" field="severity" sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
                    <SortableHeader label="Priority" field="priority" sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
                    <SortableHeader label="Status" field="status" sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
                    <SortableHeader label="Date" field="date" sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
                    <TableCell>Report ID</TableCell>
                    <TableCell align="right">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {data.content.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} sx={{ py: 4 }}>
                        <EmptyState
                          title="No disasters found"
                          message="No disasters match your current search and filters."
                          actionLabel="Clear filters"
                          onAction={clearFilters}
                        />
                      </TableCell>
                    </TableRow>
                  ) : data.content.map(d => (
                    <TableRow key={d.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/disasters/${d.id}`)}>
                      <TableCell>
                        <Chip label={d.disasterType} size="small" sx={{ fontWeight: 600, fontSize: '0.72rem' }} />
                      </TableCell>
                      <TableCell>{d.location}</TableCell>
                      <TableCell>
                        <StatusBadge status={d.severity} />
                      </TableCell>
                      <TableCell>
                        <StatusBadge status={d.priority} />
                      </TableCell>
                      <TableCell>
                        <StatusBadge status={d.status} />
                      </TableCell>
                      <TableCell>{safeDate(d.date)}</TableCell>
                      <TableCell>
                        {d.reportId && (
                          <Tooltip title={d.reportId}>
                            <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{d.reportId}</Typography>
                          </Tooltip>
                        )}
                      </TableCell>
                      <TableCell align="right">
                        <Tooltip title="View details">
                          <IconButton size="small" aria-label={`View details for ${d.disasterType} at ${d.location}`} onClick={(e) => { e.stopPropagation(); navigate(`/disasters/${d.id}`) }}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>

            <TablePagination
              component="div"
              count={data.totalElements}
              page={data.page}
              rowsPerPage={data.size}
              rowsPerPageOptions={[5, 10, 25, 50]}
              onPageChange={(_, p) => setPage(p)}
              onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0) }}
              sx={{ '.MuiTablePagination-selectLabel, .MuiTablePagination-displayedRows': { fontSize: '0.8rem' } }}
            />
          </CardContent>
        </Card>
        <Footer />
      </Box>
    </Box>
  )
}

import { useState, useEffect } from 'react'
import {
  Box, Typography, Card, CardContent, Grid, Button, TextField, Select, MenuItem,
  Dialog, DialogTitle, DialogContent, DialogActions, Chip, IconButton, Alert,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper
} from '@mui/material'
import { Edit as EditIcon, Delete as DeleteIcon, Add as AddIcon, Compare as CompareIcon } from '@mui/icons-material'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { caseStudyApi, getStoredUserSafe } from '../services/api'
import Sidebar from '../components/Sidebar'
import Breadcrumbs from '../components/Breadcrumbs'
import Footer from '../components/Footer'
import EmptyState from '../components/EmptyState'
import { TableSkeleton, ChartSkeleton } from '../components/LoadingSkeleton'
import toast from 'react-hot-toast'

const disasterTypes = ['Flood', 'Earthquake', 'Cyclone', 'Wildfire', 'Tsunami', 'Landslide', 'Drought', 'Epidemic']
const severityLevels = ['Low', 'Medium', 'High', 'Critical']

const emptyForm = {
  title: '', disasterType: '', location: '', year: '', severity: 'Low',
  description: '', lessonsLearned: '', estimatedDamage: '', affectedPopulation: '',
  resourcesUsed: '', responseTimeHours: '', recoveryTimeDays: '', recommendations: ''
}

const severityColor = { Low: 'success', Medium: 'warning', High: 'error', Critical: 'error' }

export default function CaseStudyAnalysis() {
  const [caseStudies, setCaseStudies] = useState([])
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({ year: '', disasterType: '', location: '' })
  const [dialogOpen, setDialogOpen] = useState(false)
  const [detailOpen, setDetailOpen] = useState(false)
  const [selectedStudy, setSelectedStudy] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [compType, setCompType] = useState('')
  const [compFrom, setCompFrom] = useState('')
  const [compTo, setCompTo] = useState('')
  const [comparison, setComparison] = useState(null)
  const [compLoading, setCompLoading] = useState(false)

  const userRole = getStoredUserSafe().role
  const isAdmin = userRole === 'ADMIN'

  useEffect(() => { loadCaseStudies() }, [])

  const loadCaseStudies = async () => {
    setLoading(true)
    try {
      const params = {}
      if (filters.year) params.year = filters.year
      if (filters.disasterType) params.disasterType = filters.disasterType
      if (filters.location) params.location = filters.location
      const res = Object.keys(params).length ? await caseStudyApi.search(params) : await caseStudyApi.getAll()
      setCaseStudies(res.data || [])
    } catch {
      setCaseStudies([])
      toast.error('Failed to load case studies')
    } finally {
      setLoading(false)
    }
  }

  const handleOpenAdd = () => { setForm(emptyForm); setEditingId(null); setDialogOpen(true) }
  const handleOpenEdit = (study) => {
    setForm({
      title: study.title || '',
      disasterType: study.disasterType || '',
      location: study.location || '',
      year: study.year?.toString() || '',
      severity: study.severity || 'Low',
      description: study.description || '',
      lessonsLearned: study.lessonsLearned || '',
      estimatedDamage: study.estimatedDamage?.toString() || '',
      affectedPopulation: study.affectedPopulation?.toString() || '',
      resourcesUsed: study.resourcesUsed || '',
      responseTimeHours: study.responseTimeHours?.toString() || '',
      recoveryTimeDays: study.recoveryTimeDays?.toString() || '',
      recommendations: study.recommendations || ''
    })
    setEditingId(study.id)
    setDialogOpen(true)
  }

  const handleSave = async () => {
    try {
      const payload = {
        ...form,
        year: parseInt(form.year) || 0,
        estimatedDamage: parseFloat(form.estimatedDamage) || 0,
        affectedPopulation: parseInt(form.affectedPopulation) || 0,
        responseTimeHours: parseFloat(form.responseTimeHours) || 0,
        recoveryTimeDays: parseFloat(form.recoveryTimeDays) || 0
      }
      if (editingId) {
        await caseStudyApi.update(editingId, payload)
        toast.success('Case study updated')
      } else {
        await caseStudyApi.create(payload)
        toast.success('Case study created')
      }
      setDialogOpen(false)
      loadCaseStudies()
    } catch {
      toast.error('Failed to save case study')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this case study?')) return
    try {
      await caseStudyApi.delete(id)
      toast.success('Case study deleted')
      loadCaseStudies()
    } catch {
      toast.error('Failed to delete case study')
    }
  }

  const handleCompare = async () => {
    if (!compType || !compFrom || !compTo) {
      toast.error('Select disaster type and year range')
      return
    }
    setCompLoading(true)
    try {
      const res = await caseStudyApi.getComparison({ disasterType: compType, fromYear: compFrom, toYear: compTo })
      setComparison(res.data || [])
    } catch {
      setComparison([])
      toast.error('Failed to load comparison')
    } finally {
      setCompLoading(false)
    }
  }

  const handleViewDetail = (study) => { setSelectedStudy(study); setDetailOpen(true) }

  const filteredStudies = caseStudies.filter(s => {
    if (filters.year && s.year?.toString() !== filters.year) return false
    if (filters.disasterType && s.disasterType !== filters.disasterType) return false
    if (filters.location && !s.location?.toLowerCase().includes(filters.location.toLowerCase())) return false
    return true
  })

  const renderFilters = () => (
    <Card sx={{ p: 2, mb: 3 }}>
      <Grid container spacing={2} alignItems="center">
        <Grid size={{ xs: 12, sm: 3 }}>
          <TextField
            fullWidth size="small" label="Year" type="number"
            value={filters.year} onChange={e => setFilters({ ...filters, year: e.target.value })}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 3 }}>
          <Select
            fullWidth size="small" displayEmpty value={filters.disasterType}
            onChange={e => setFilters({ ...filters, disasterType: e.target.value })}
          >
            <MenuItem value="">All Types</MenuItem>
            {disasterTypes.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
          </Select>
        </Grid>
        <Grid size={{ xs: 12, sm: 3 }}>
          <TextField
            fullWidth size="small" label="Location"
            value={filters.location} onChange={e => setFilters({ ...filters, location: e.target.value })}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 3 }}>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button variant="contained" onClick={loadCaseStudies}>Search</Button>
            {isAdmin && (
              <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={handleOpenAdd}>
                Add Case Study
              </Button>
            )}
          </Box>
        </Grid>
      </Grid>
    </Card>
  )

  const renderCard = (study) => (
    <Card key={study.id} sx={{ cursor: 'pointer', '&:hover': { boxShadow: 4 } }} onClick={() => handleViewDetail(study)}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 600, fontSize: '1rem', flex: 1 }}>{study.title}</Typography>
          {isAdmin && (
            <Box sx={{ display: 'flex', gap: 0.5 }}>
              <IconButton size="small" onClick={e => { e.stopPropagation(); handleOpenEdit(study) }}>
                <EditIcon fontSize="small" />
              </IconButton>
              <IconButton size="small" onClick={e => { e.stopPropagation(); handleDelete(study.id) }}>
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Box>
          )}
        </Box>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 1.5 }}>
          <Chip label={study.disasterType} size="small" color="primary" variant="outlined" />
          <Chip label={study.severity} size="small" color={severityColor[study.severity] || 'default'} />
          <Chip label={study.location} size="small" variant="outlined" />
        </Box>
        <Typography variant="body2" color="text.secondary">
          Year: {study.year} | Damage: ${(study.estimatedDamage || 0).toLocaleString()} | Population: {(study.affectedPopulation || 0).toLocaleString()}
        </Typography>
      </CardContent>
    </Card>
  )

  const renderDetailDialog = () => (
    <Dialog open={detailOpen} onClose={() => setDetailOpen(false)} maxWidth="md" fullWidth>
      {selectedStudy && (
        <>
          <DialogTitle sx={{ fontWeight: 700 }}>{selectedStudy.title}</DialogTitle>
          <DialogContent dividers>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Typography variant="subtitle2">Disaster Type</Typography>
                <Typography variant="body2" gutterBottom>{selectedStudy.disasterType}</Typography>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Typography variant="subtitle2">Location</Typography>
                <Typography variant="body2" gutterBottom>{selectedStudy.location}</Typography>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Typography variant="subtitle2">Year</Typography>
                <Typography variant="body2" gutterBottom>{selectedStudy.year}</Typography>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Typography variant="subtitle2">Severity</Typography>
                <Chip label={selectedStudy.severity} size="small" color={severityColor[selectedStudy.severity] || 'default'} />
              </Grid>
              {selectedStudy.description && (
                <Grid size={{ xs: 12 }}>
                  <Typography variant="subtitle2">Description</Typography>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{selectedStudy.description}</Typography>
                </Grid>
              )}
              {selectedStudy.lessonsLearned && (
                <Grid size={{ xs: 12 }}>
                  <Typography variant="subtitle2">Lessons Learned</Typography>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{selectedStudy.lessonsLearned}</Typography>
                </Grid>
              )}
              {selectedStudy.recommendations && (
                <Grid size={{ xs: 12 }}>
                  <Typography variant="subtitle2">Recommendations</Typography>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{selectedStudy.recommendations}</Typography>
                </Grid>
              )}
              <Grid size={{ xs: 4 }}>
                <Typography variant="subtitle2">Estimated Damage</Typography>
                <Typography variant="body2">${(selectedStudy.estimatedDamage || 0).toLocaleString()}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography variant="subtitle2">Affected Population</Typography>
                <Typography variant="body2">{(selectedStudy.affectedPopulation || 0).toLocaleString()}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography variant="subtitle2">Resources Used</Typography>
                <Typography variant="body2">{selectedStudy.resourcesUsed || 'N/A'}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography variant="subtitle2">Response Time</Typography>
                <Typography variant="body2">{selectedStudy.responseTimeHours ? `${selectedStudy.responseTimeHours}h` : 'N/A'}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography variant="subtitle2">Recovery Time</Typography>
                <Typography variant="body2">{selectedStudy.recoveryTimeDays ? `${selectedStudy.recoveryTimeDays} days` : 'N/A'}</Typography>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDetailOpen(false)}>Close</Button>
          </DialogActions>
        </>
      )}
    </Dialog>
  )

  const renderFormDialog = () => (
    <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
      <DialogTitle>{editingId ? 'Edit Case Study' : 'Add Case Study'}</DialogTitle>
      <DialogContent dividers>
        <Grid container spacing={2} sx={{ mt: 0.5 }}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth label="Title" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Select fullWidth displayEmpty value={form.disasterType} onChange={e => setForm({ ...form, disasterType: e.target.value })}>
              <MenuItem value="" disabled>Disaster Type</MenuItem>
              {disasterTypes.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
            </Select>
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField fullWidth label="Location" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField fullWidth label="Year" type="number" value={form.year} onChange={e => setForm({ ...form, year: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <Select fullWidth value={form.severity} onChange={e => setForm({ ...form, severity: e.target.value })}>
              {severityLevels.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </Select>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField fullWidth multiline rows={3} label="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField fullWidth multiline rows={3} label="Lessons Learned" value={form.lessonsLearned} onChange={e => setForm({ ...form, lessonsLearned: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField fullWidth label="Estimated Damage ($)" type="number" value={form.estimatedDamage} onChange={e => setForm({ ...form, estimatedDamage: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField fullWidth label="Affected Population" type="number" value={form.affectedPopulation} onChange={e => setForm({ ...form, affectedPopulation: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField fullWidth label="Resources Used" value={form.resourcesUsed} onChange={e => setForm({ ...form, resourcesUsed: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField fullWidth label="Response Time (hours)" type="number" value={form.responseTimeHours} onChange={e => setForm({ ...form, responseTimeHours: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField fullWidth label="Recovery Time (days)" type="number" value={form.recoveryTimeDays} onChange={e => setForm({ ...form, recoveryTimeDays: e.target.value })} />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField fullWidth multiline rows={3} label="Recommendations" value={form.recommendations} onChange={e => setForm({ ...form, recommendations: e.target.value })} />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
        <Button variant="contained" onClick={handleSave}>{editingId ? 'Update' : 'Create'}</Button>
      </DialogActions>
    </Dialog>
  )

  const renderComparison = () => (
    <Card sx={{ p: 2, mb: 3 }}>
      <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
        <CompareIcon /> Comparison
      </Typography>
      <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, sm: 3 }}>
          <Select fullWidth size="small" displayEmpty value={compType} onChange={e => setCompType(e.target.value)}>
            <MenuItem value="" disabled>Disaster Type</MenuItem>
            {disasterTypes.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
          </Select>
        </Grid>
        <Grid size={{ xs: 6, sm: 2 }}>
          <TextField fullWidth size="small" label="From Year" type="number" value={compFrom} onChange={e => setCompFrom(e.target.value)} />
        </Grid>
        <Grid size={{ xs: 6, sm: 2 }}>
          <TextField fullWidth size="small" label="To Year" type="number" value={compTo} onChange={e => setCompTo(e.target.value)} />
        </Grid>
        <Grid size={{ xs: 12, sm: 3 }}>
          <Button variant="contained" onClick={handleCompare} disabled={compLoading}>
            {compLoading ? 'Loading...' : 'Compare'}
          </Button>
        </Grid>
      </Grid>

      {compLoading && <ChartSkeleton />}

      {comparison && comparison.length > 0 && (
        <>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <Paper sx={{ p: 2 }}>
                <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>Response Times (hours)</Typography>
                <ResponsiveContainer width="100%" height={250}>
                  <BarChart data={comparison}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="title" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="responseTimeHours" fill="#0F4C81" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <Paper sx={{ p: 2 }}>
                <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>Estimated Damages ($)</Typography>
                <ResponsiveContainer width="100%" height={250}>
                  <BarChart data={comparison}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="title" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="estimatedDamage" fill="#00897B" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>
          </Grid>
          <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Title</TableCell>
                  <TableCell>Year</TableCell>
                  <TableCell>Severity</TableCell>
                  <TableCell>Response (h)</TableCell>
                  <TableCell>Damage ($)</TableCell>
                  <TableCell>Population</TableCell>
                  <TableCell>Recovery (days)</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {comparison.map((s, i) => (
                  <TableRow key={i}>
                    <TableCell>{s.title}</TableCell>
                    <TableCell>{s.year}</TableCell>
                    <TableCell><Chip label={s.severity} size="small" color={severityColor[s.severity] || 'default'} /></TableCell>
                    <TableCell>{s.responseTimeHours}</TableCell>
                    <TableCell>{(s.estimatedDamage || 0).toLocaleString()}</TableCell>
                    <TableCell>{(s.affectedPopulation || 0).toLocaleString()}</TableCell>
                    <TableCell>{s.recoveryTimeDays}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      {comparison && comparison.length === 0 && !compLoading && (
        <Alert severity="info">No case studies match the comparison criteria.</Alert>
      )}
    </Card>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flexGrow: 1, p: 3, minHeight: '100vh', backgroundColor: 'background.default' }}>
        <Breadcrumbs />
        <Typography variant="h4" sx={{ mb: 0.5 }}>Case Study Analysis</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Analyze historical disaster case studies and compare response strategies
        </Typography>

        {renderFilters()}

        {loading ? (
          <TableSkeleton rows={6} />
        ) : (
          <>
            {filteredStudies.length === 0 ? (
              <EmptyState title="No case studies found" message="Try adjusting your filters or add a new case study." actionLabel={isAdmin ? 'Add Case Study' : undefined} onAction={isAdmin ? handleOpenAdd : undefined} />
            ) : (
              <Grid container spacing={2} sx={{ mb: 4 }}>
                {filteredStudies.map(s => (
                  <Grid size={{ xs: 12, sm: 6, md: 4 }} key={s.id}>{renderCard(s)}</Grid>
                ))}
              </Grid>
            )}
          </>
        )}

        {renderComparison()}
        {renderDetailDialog()}
        {renderFormDialog()}

        <Footer />
      </Box>
    </Box>
  )
}

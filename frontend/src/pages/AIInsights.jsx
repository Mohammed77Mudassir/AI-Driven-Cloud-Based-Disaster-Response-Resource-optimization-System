import { useState } from 'react'
import Sidebar from '../components/Sidebar'
import Breadcrumbs from '../components/Breadcrumbs'
import Footer from '../components/Footer'
import { aiApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, Card, CardContent, Typography, TextField, Select, MenuItem, FormControl,
  InputLabel, Button, CircularProgress, Grid, Chip, Alert, LinearProgress,
  Divider, List, ListItem, ListItemIcon, ListItemText, Paper, Tooltip
} from '@mui/material'
import SendIcon from '@mui/icons-material/Send'
import LocalHospitalIcon from '@mui/icons-material/LocalHospital'
import VillaIcon from '@mui/icons-material/Villa'
import PeopleIcon from '@mui/icons-material/People'
import WarningIcon from '@mui/icons-material/Warning'
import InventoryIcon from '@mui/icons-material/Inventory'
import SpeedIcon from '@mui/icons-material/Speed'
import ShieldIcon from '@mui/icons-material/Shield'
import TimerIcon from '@mui/icons-material/Timer'
import VerifiedIcon from '@mui/icons-material/Verified'
import GpsFixedIcon from '@mui/icons-material/GpsFixed'
import EmergencyIcon from '@mui/icons-material/Emergency'
import AnalyticsIcon from '@mui/icons-material/Analytics'
import ScaleIcon from '@mui/icons-material/Scale'

const disasterTypes = ['Flood', 'Earthquake', 'Cyclone', 'Wildfire', 'Tsunami', 'Landslide', 'Drought', 'Epidemic']
const severities = ['Low', 'Medium', 'High', 'Critical']

const scoreColor = (s) => (s >= 80 ? '#C62828' : s >= 60 ? '#EF6C00' : s >= 40 ? '#F9A825' : '#2E7D32')
const statusColor = (s) => {
  switch (s) {
    case 'ADEQUATE': return '#2E7D32'
    case 'PARTIAL': return '#F9A825'
    case 'CRITICAL': return '#C62828'
    default: return '#757575'
  }
}
const damageColor = (v) => {
  switch (v) {
    case 'Catastrophic': return '#C62828'
    case 'Severe': return '#EF6C00'
    case 'Moderate': return '#F9A825'
    default: return '#2E7D32'
  }
}

const FactorList = ({ factors }) => {
  if (!factors?.length) return null
  return (
    <Box sx={{ mt: 1 }}>
      {factors.map((f, i) => (
        <Box key={i} sx={{ mb: 1 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="caption" sx={{ fontWeight: 600 }}>{f.name}</Typography>
            <Typography variant="caption" color="text.secondary">
              {Math.round(f.contribution)} / {Math.round(f.weight * 100)}
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={Math.min(100, f.value)}
            sx={{ height: 5, borderRadius: 3, '& .MuiLinearProgress-bar': { backgroundColor: scoreColor(f.value) } }}
          />
          <Typography variant="caption" color="text.secondary">{f.description}</Typography>
        </Box>
      ))}
    </Box>
  )
}

const TimeCard = ({ title, icon, estimate }) => {
  if (!estimate) return null
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
          {icon}
          <Typography variant="subtitle1" fontWeight={700}>{title}</Typography>
        </Box>
        <Typography variant="h4" fontWeight={800} color="primary.main">
          {estimate.value} <Typography component="span" variant="body2" color="text.secondary">{estimate.unit}</Typography>
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Range: {estimate.min}–{estimate.max} {estimate.unit}
        </Typography>
        <Divider sx={{ my: 1 }} />
        <Typography variant="caption" color="text.secondary">{estimate.note}</Typography>
      </CardContent>
    </Card>
  )
}

export default function AIInsights() {
  const [form, setForm] = useState({
    disasterType: '', severity: '', location: '', latitude: '', longitude: '',
    population: '', infrastructureFactor: ''
  })
  const [analysis, setAnalysis] = useState(null)
  const [recommendations, setRecommendations] = useState(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const data = {
        disasterType: form.disasterType,
        severity: form.severity,
        location: form.location,
        latitude: parseFloat(form.latitude) || 0,
        longitude: parseFloat(form.longitude) || 0,
        population: parseInt(form.population, 10) > 0 ? parseInt(form.population, 10) : undefined,
        infrastructureFactor: form.infrastructureFactor !== '' ? parseFloat(form.infrastructureFactor) : undefined
      }
      const [anaRes, recRes] = await Promise.all([
        aiApi.analyze(data),
        aiApi.recommend(data)
      ])
      setAnalysis(anaRes.data)
      setRecommendations(recRes.data)
      toast.success('AI analysis complete')
    } catch {
      toast.error('AI analysis failed')
    } finally {
      setLoading(false)
    }
  }

  const c = analysis?.confidence
  const evac = recommendations?.evacuation

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <Breadcrumbs />
        <Typography variant="h4" fontWeight={700} gutterBottom>AI Insights</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Deterministic offline engine — damage, risk, priority, time estimates & data-backed recommendations
        </Typography>

        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Box component="form" onSubmit={handleSubmit}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                  <FormControl fullWidth size="small" required>
                    <InputLabel>Disaster Type</InputLabel>
                    <Select label="Disaster Type" value={form.disasterType}
                      onChange={e => setForm({ ...form, disasterType: e.target.value })}>
                      <MenuItem value="">Select type</MenuItem>
                      {disasterTypes.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                  <FormControl fullWidth size="small" required>
                    <InputLabel>Severity</InputLabel>
                    <Select label="Severity" value={form.severity}
                      onChange={e => setForm({ ...form, severity: e.target.value })}>
                      <MenuItem value="">Select severity</MenuItem>
                      {severities.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                  <TextField fullWidth size="small" label="Location" required
                    value={form.location} onChange={e => setForm({ ...form, location: e.target.value })}
                    placeholder="e.g. Mumbai" />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <TextField fullWidth size="small" label="Latitude" type="number"
                    value={form.latitude} onChange={e => setForm({ ...form, latitude: e.target.value })}
                    placeholder="19.0760" />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <TextField fullWidth size="small" label="Longitude" type="number"
                    value={form.longitude} onChange={e => setForm({ ...form, longitude: e.target.value })}
                    placeholder="72.8777" />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <TextField fullWidth size="small" label="Population (optional)" type="number"
                    value={form.population} onChange={e => setForm({ ...form, population: e.target.value })}
                    placeholder="120000" />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <TextField fullWidth size="small" label="Infrastructure (0–1)" type="number"
                    inputProps={{ min: 0, max: 1, step: 0.1 }}
                    value={form.infrastructureFactor}
                    onChange={e => setForm({ ...form, infrastructureFactor: e.target.value })}
                    placeholder="0.6" />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Button type="submit" variant="contained" disabled={loading}
                    startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <SendIcon />}>
                    {loading ? 'Analyzing...' : 'Run AI Analysis'}
                  </Button>
                </Grid>
              </Grid>
            </Box>
          </CardContent>
        </Card>

        {(analysis || recommendations) && (
          <>
            {analysis && (
              <Paper elevation={1} sx={{ p: 2, mb: 3, bgcolor: '#EAF4EC' }}>
                <Grid container spacing={2} alignItems="center">
                  <Grid size={{ xs: 12, md: 4 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <VerifiedIcon color="success" />
                      <Box>
                        <Typography variant="body2" fontWeight={700}>Overall confidence</Typography>
                        <Typography variant="caption" color="text.secondary">{c?.basis}</Typography>
                      </Box>
                    </Box>
                    <Typography variant="h4" fontWeight={800} color="primary.main" sx={{ mt: 1 }}>
                      {Math.round(c?.overall || 0)}%
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 12, md: 8 }}>
                    {[
                      ['Input completeness', c?.inputCompleteness],
                      ['Model coverage', c?.modelCoverage],
                      ['Data quality', c?.dataQuality],
                      ['Historical basis', c?.historicalBasis]
                    ].map(([label, value]) => (
                      <Box key={label} sx={{ mb: 0.5 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="caption">{label}</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700 }}>{Math.round(value || 0)}%</Typography>
                        </Box>
                        <LinearProgress variant="determinate" value={value || 0}
                          sx={{ height: 5, borderRadius: 3 }} />
                      </Box>
                    ))}
                  </Grid>
                </Grid>
                <Box sx={{ mt: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Chip size="small" icon={<GpsFixedIcon />} label={`${analysis.model?.name} v${analysis.model?.version}`} variant="outlined" />
                  <Chip size="small" color="success" label="OFFLINE MODEL" />
                </Box>
              </Paper>
            )}

            {analysis && (
              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid size={{ xs: 12, md: 4 }}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <WarningIcon color="error" />
                        <Typography variant="subtitle1" fontWeight={700}>Damage Prediction</Typography>
                      </Box>
                      <Chip label={analysis.damage?.damageLevel}
                        sx={{ fontWeight: 700, bgcolor: `${damageColor(analysis.damage?.damageLevel)}18`, color: damageColor(analysis.damage?.damageLevel) }} />
                      <Box sx={{ mt: 1.5 }}>
                        <Typography variant="caption" color="text.secondary" display="block">Affected population</Typography>
                        <Typography variant="h6" fontWeight={700}>{(analysis.damage?.affectedPopulation || 0).toLocaleString()}</Typography>
                        <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>Economic loss</Typography>
                        <Typography variant="h6" fontWeight={700}>₹{(analysis.damage?.economicLossINR || 0).toLocaleString()}</Typography>
                        <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>Casualties estimate</Typography>
                        <Typography variant="h6" fontWeight={700}>{(analysis.damage?.casualtiesEstimate || 0).toLocaleString()}</Typography>
                      </Box>
                      <Divider sx={{ my: 1.5 }} />
                      <Typography variant="caption" color="text.secondary">{analysis.damage?.infrastructureImpact}</Typography>
                      <Divider sx={{ my: 1.5 }} />
                      <Typography variant="caption" fontWeight={700}>Hazard drivers</Typography>
                      <List dense disablePadding>
                        {(analysis.damage?.hazardDrivers || []).map((d, i) => (
                          <ListItem key={i} dense sx={{ px: 0, py: 0.25 }}>
                            <ListItemIcon sx={{ minWidth: 24 }}><EmergencyIcon fontSize="small" color="error" /></ListItemIcon>
                            <ListItemText primary={<Typography variant="caption">{d}</Typography>} />
                          </ListItem>
                        ))}
                      </List>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 4 }}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <ShieldIcon sx={{ color: scoreColor(analysis.risk?.score) }} />
                        <Typography variant="subtitle1" fontWeight={700}>Risk Prediction</Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Typography variant="h3" fontWeight={800} sx={{ color: scoreColor(analysis.risk?.score) }}>
                          {analysis.risk?.score}
                        </Typography>
                        <Chip label={analysis.risk?.level} size="small"
                          sx={{ fontWeight: 700, bgcolor: `${scoreColor(analysis.risk?.score)}18`, color: scoreColor(analysis.risk?.score) }} />
                      </Box>
                      <FactorList factors={analysis.risk?.factors} />
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 4 }}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <AnalyticsIcon sx={{ color: scoreColor(analysis.priority?.score) }} />
                        <Typography variant="subtitle1" fontWeight={700}>Priority Score</Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Typography variant="h3" fontWeight={800} sx={{ color: scoreColor(analysis.priority?.score) }}>
                          {analysis.priority?.score}
                        </Typography>
                        <Chip label={analysis.priority?.label} size="small"
                          sx={{ fontWeight: 700, bgcolor: `${scoreColor(analysis.priority?.score)}18`, color: scoreColor(analysis.priority?.score) }} />
                      </Box>
                      <FactorList factors={analysis.priority?.factors} />
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <TimeCard title="Response Time" icon={<SpeedIcon color="primary" />} estimate={analysis.responseTime} />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TimeCard title="Recovery Time" icon={<TimerIcon color="primary" />} estimate={analysis.recoveryTime} />
                </Grid>

                <Grid size={{ xs: 12 }}>
                  <Card>
                    <CardContent>
                      <Typography variant="subtitle1" fontWeight={700} gutterBottom>
                        Recommended Response
                      </Typography>
                      <List dense disablePadding>
                        {(analysis.recommendations || []).map((r, i) => (
                          <ListItem key={i} dense sx={{ px: 0, py: 0.25 }}>
                            <ListItemIcon sx={{ minWidth: 28 }}>
                              <ScaleIcon fontSize="small" color="primary" />
                            </ListItemIcon>
                            <ListItemText primary={<Typography variant="body2">{r}</Typography>} />
                          </ListItem>
                        ))}
                      </List>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            )}

            {recommendations && (
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <LocalHospitalIcon color="error" />
                        <Typography variant="subtitle1" fontWeight={700}>Hospital Recommendations</Typography>
                      </Box>
                      <List dense disablePadding>
                        {(recommendations.hospitals || []).map((h, i) => (
                          <ListItem key={h.id} dense sx={{ px: 0, py: 0.5 }}>
                            <Box sx={{ width: '100%' }}>
                              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Typography variant="body2" fontWeight={600}>
                                  {i + 1}. {h.name}
                                </Typography>
                                <Tooltip title={`Match score ${h.score}/100`}>
                                  <Chip size="small" label={`${h.score}`} sx={{ fontWeight: 700, bgcolor: '#E3F2FD', color: '#1565C0' }} />
                                </Tooltip>
                              </Box>
                              <Typography variant="caption" color="text.secondary">
                                {h.distanceKm} km · {h.availableBeds} beds · {h.icuBeds} ICU · {h.doctorsAvailable} doctors{h.bloodBank ? ' · blood bank' : ''}
                              </Typography>
                            </Box>
                          </ListItem>
                        ))}
                      </List>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <VillaIcon color="warning" />
                        <Typography variant="subtitle1" fontWeight={700}>Shelter Recommendations</Typography>
                      </Box>
                      <List dense disablePadding>
                        {(recommendations.shelters || []).map((s, i) => (
                          <ListItem key={s.id} dense sx={{ px: 0, py: 0.5 }}>
                            <Box sx={{ width: '100%' }}>
                              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Typography variant="body2" fontWeight={600}>{i + 1}. {s.name}</Typography>
                                <Tooltip title={`Match score ${s.score}/100`}>
                                  <Chip size="small" label={`${s.score}`} sx={{ fontWeight: 700, bgcolor: '#FFF8E1', color: '#F57F17' }} />
                                </Tooltip>
                              </Box>
                              <Typography variant="caption" color="text.secondary">
                                {s.distanceKm} km · {s.availableSpace} spaces free{s.foodAvailable ? ' · food' : ''}{s.waterAvailable ? ' · water' : ''}{s.powerAvailable ? ' · power' : ''}
                              </Typography>
                            </Box>
                          </ListItem>
                        ))}
                      </List>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <PeopleIcon color="success" />
                        <Typography variant="subtitle1" fontWeight={700}>Volunteer Recommendations</Typography>
                      </Box>
                      <List dense disablePadding>
                        {(recommendations.volunteers || []).map((v, i) => (
                          <ListItem key={v.id} dense sx={{ px: 0, py: 0.5 }}>
                            <Box sx={{ width: '100%' }}>
                              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Typography variant="body2" fontWeight={600}>{i + 1}. {v.name}</Typography>
                                <Typography variant="caption" color="text.secondary">skill {Math.round(v.skillMatch)}%</Typography>
                              </Box>
                              <Typography variant="caption" color="text.secondary">{v.skills} · {v.distanceKm} km</Typography>
                            </Box>
                          </ListItem>
                        ))}
                      </List>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <WarningIcon color="error" />
                        <Typography variant="subtitle1" fontWeight={700}>Evacuation Plan</Typography>
                      </Box>
                      {evac && (
                        <>
                          <Chip size="small" label={`Danger radius ${evac.dangerRadiusKm} km`} sx={{ mb: 1, fontWeight: 700, bgcolor: '#FFEBEE', color: '#C62828' }} />
                          <Typography variant="body2" sx={{ mb: 1 }}>
                            <strong>Window:</strong> {evac.evacuationWindow}
                          </Typography>
                          <Typography variant="caption" fontWeight={700} display="block">Priority zones</Typography>
                          <List dense disablePadding>
                            {(evac.priorityZones || []).map((z, i) => (
                              <ListItem key={i} dense sx={{ px: 0, py: 0.1 }}>
                                <ListItemIcon sx={{ minWidth: 24 }}><EmergencyIcon fontSize="small" color="error" /></ListItemIcon>
                                <ListItemText primary={<Typography variant="caption">{z}</Typography>} />
                              </ListItem>
                            ))}
                          </List>
                          <Divider sx={{ my: 1 }} />
                          <Typography variant="caption" fontWeight={700} display="block">Instructions</Typography>
                          {(evac.instructions || []).map((ins, i) => (
                            <Typography key={i} variant="caption" display="block" color="text.secondary">
                              • {ins}
                            </Typography>
                          ))}
                        </>
                      )}
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12 }}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <InventoryIcon color="primary" />
                        <Typography variant="subtitle1" fontWeight={700}>Resource Optimization (vs live inventory)</Typography>
                      </Box>
                      <Grid container spacing={1.5}>
                        {(recommendations.resources || []).map((r) => (
                          <Grid size={{ xs: 6, sm: 4, md: 3 }} key={r.resourceType}>
                            <Paper sx={{ p: 1.5, bgcolor: '#F8F9FA' }}>
                              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Typography variant="caption" fontWeight={700}>{r.resourceType.replace(/_/g, ' ')}</Typography>
                                <Chip size="small" label={r.status} sx={{ fontSize: '0.6rem', fontWeight: 700, bgcolor: `${statusColor(r.status)}18`, color: statusColor(r.status) }} />
                              </Box>
                              <Typography variant="h6" fontWeight={700}>
                                {r.available}<Typography component="span" variant="caption" color="text.secondary"> / {r.required} needed</Typography>
                              </Typography>
                              <Typography variant="caption" color="text.secondary">{r.deficit > 0 ? `Deficit ${r.deficit}` : 'Fully covered'}</Typography>
                              <Tooltip title={r.nearestAssets}>
                                <Typography variant="caption" color="primary.main" display="block" sx={{ mt: 0.5, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                  {r.nearestAssets}
                                </Typography>
                              </Tooltip>
                            </Paper>
                          </Grid>
                        ))}
                      </Grid>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12 }}>
                  {(recommendations.summary || []).map((s, i) => (
                    <Alert key={i} severity="info" sx={{ mb: 1, fontSize: '0.85rem' }}>{s}</Alert>
                  ))}
                </Grid>
              </Grid>
            )}
          </>
        )}
        <Footer />
      </Box>
    </Box>
  )
}

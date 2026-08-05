import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { disasterApi } from '../services/api'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import SectionCard from '../components/SectionCard'
import FileUploadDropzone from '../components/FileUploadDropzone'
import toast from 'react-hot-toast'
import {
  Box, Card, CardContent, Typography, TextField, Select, MenuItem, FormControl,
  InputLabel, Button, CircularProgress, Grid, Alert, IconButton, InputAdornment, Paper, useTheme
} from '@mui/material'
import SendIcon from '@mui/icons-material/Send'
import MyLocationIcon from '@mui/icons-material/MyLocation'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import { DISASTER_TYPES, SEVERITIES, PRIORITIES } from '../constants/disaster'

export default function ReportDisaster() {
  const navigate = useNavigate()
  const theme = useTheme()
  const [form, setForm] = useState({
    disasterType: '', description: '', severity: '', location: '',
    address: '', latitude: '', longitude: '', priority: 'MEDIUM',
  })
  const [attachments, setAttachments] = useState([])
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [locating, setLocating] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const detectLocation = () => {
    if (!navigator.geolocation) {
      toast.error('Geolocation is not supported by your browser')
      return
    }
    setLocating(true)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setForm(prev => ({
          ...prev,
          latitude: pos.coords.latitude.toFixed(6),
          longitude: pos.coords.longitude.toFixed(6),
        }))
        toast.success('GPS location captured')
        setLocating(false)
      },
      () => {
        toast.error('Could not access your location. Allow location permission and try again.')
        setLocating(false)
      },
      { enableHighAccuracy: true, timeout: 12000 }
    )
  }

  const validate = () => {
    const next = {}
    if (!form.disasterType) next.disasterType = 'Disaster type is required'
    if (!form.severity) next.severity = 'Severity is required'
    if (!form.description.trim()) next.description = 'Description is required'
    else if (form.description.trim().length < 10) next.description = 'Description must be at least 10 characters'
    if (!form.location.trim()) next.location = 'Location is required'
    const lat = parseFloat(form.latitude)
    const lng = parseFloat(form.longitude)
    if (form.latitude !== '' && (isNaN(lat) || lat < -90 || lat > 90)) next.latitude = 'Valid latitude is required (-90 to 90)'
    if (form.longitude !== '' && (isNaN(lng) || lng < -180 || lng > 180)) next.longitude = 'Valid longitude is required (-180 to 180)'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) {
      toast.error('Please fix the highlighted fields')
      return
    }
    setLoading(true)
    try {
      const payload = {
        ...form,
        latitude: parseFloat(form.latitude) || 0,
        longitude: parseFloat(form.longitude) || 0,
        attachments: attachments.map(({ id, ...rest }) => rest),
      }
      const res = await disasterApi.create(payload)
      toast.success('Disaster reported successfully')
      navigate(`/disasters/${res.data.id}`)
    } catch (err) {
      const data = err.response?.data
      if (data && typeof data === 'object') {
        const msgs = Object.values(data).filter(Boolean).join('. ')
        toast.error(msgs || 'Failed to report disaster')
      } else {
        toast.error('Failed to report disaster')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <PageHeader
          title="Report a Disaster"
          subtitle="Submit details about a disaster event. Attach photos, videos or documents as evidence."
        />

        <Box sx={{ maxWidth: 860 }}>
          <SectionCard title="Report Details" subtitle="Fill in the details of the disaster event">
            <Box component="form" onSubmit={handleSubmit} noValidate>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <FormControl fullWidth size="small" required error={!!errors.disasterType}>
                    <InputLabel>Disaster Type</InputLabel>
                    <Select name="disasterType" label="Disaster Type" value={form.disasterType} onChange={handleChange}>
                      <MenuItem value="">Select type</MenuItem>
                      {DISASTER_TYPES.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
                    </Select>
                    {errors.disasterType && <Typography variant="caption" color="error" sx={{ ml: 1.5, mt: 0.5 }}>{errors.disasterType}</Typography>}
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <FormControl fullWidth size="small" required error={!!errors.severity}>
                    <InputLabel>Severity</InputLabel>
                    <Select name="severity" label="Severity" value={form.severity} onChange={handleChange}>
                      <MenuItem value="">Select severity</MenuItem>
                      {SEVERITIES.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                    </Select>
                    {errors.severity && <Typography variant="caption" color="error" sx={{ ml: 1.5, mt: 0.5 }}>{errors.severity}</Typography>}
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Priority</InputLabel>
                    <Select name="priority" label="Priority" value={form.priority} onChange={handleChange}>
                      {PRIORITIES.map(p => <MenuItem key={p} value={p}>{p.charAt(0) + p.slice(1).toLowerCase()}</MenuItem>)}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth size="small" label="Location" name="location" required
                    error={!!errors.location} helperText={errors.location || 'e.g. Mumbai, Maharashtra'}
                    value={form.location} onChange={handleChange} placeholder="e.g. Mumbai, Maharashtra" />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField fullWidth size="small" label="Address / Area Details" name="address" multiline rows={2}
                    value={form.address} onChange={handleChange}
                    placeholder="Street, colony, nearest landmark (optional)" />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField fullWidth size="small" label="Description" name="description" required multiline rows={4}
                    error={!!errors.description} helperText={errors.description || 'Describe the disaster situation...'}
                    value={form.description} onChange={handleChange} placeholder="Describe the disaster situation..." />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth size="small" label="Latitude" name="latitude" type="number"
                    error={!!errors.latitude} helperText={errors.latitude || 'e.g. 19.0760'}
                    value={form.latitude} onChange={handleChange}
                    slotProps={{
                      input: {
                        startAdornment: (
                          <InputAdornment position="start"><LocationOnIcon fontSize="small" /></InputAdornment>
                        )
                      }
                    }}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth size="small" label="Longitude" name="longitude" type="number"
                    error={!!errors.longitude} helperText={errors.longitude || 'e.g. 72.8777'}
                    value={form.longitude} onChange={handleChange}
                    slotProps={{
                      input: {
                        startAdornment: (
                          <InputAdornment position="start"><LocationOnIcon fontSize="small" /></InputAdornment>
                        )
                      }
                    }}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Button
                    size="small" variant="outlined" color="secondary"
                    startIcon={locating ? <CircularProgress size={16} color="inherit" /> : <MyLocationIcon />}
                    onClick={detectLocation} disabled={locating}
                  >
                    {locating ? 'Detecting location...' : 'Use My GPS Location'}
                  </Button>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Paper variant="outlined" sx={{ p: 2, backgroundColor: theme.palette.action.hover }}>
                    <Typography variant="subtitle2" fontWeight={600} gutterBottom>
                      Evidence Attachments (Images · Videos · PDFs)
                    </Typography>
                    <FileUploadDropzone value={attachments} onChange={setAttachments} />
                  </Paper>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Button type="submit" variant="contained" disabled={loading} startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <SendIcon />}>
                    {loading ? 'Submitting...' : 'Report Disaster'}
                  </Button>
                </Grid>
              </Grid>
            </Box>
          </SectionCard>
        </Box>
        <Footer />
      </Box>
    </Box>
  )
}

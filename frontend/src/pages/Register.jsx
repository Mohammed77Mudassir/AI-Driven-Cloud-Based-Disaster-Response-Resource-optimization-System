import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'
import {
  Box, TextField, Button, Typography,
  Alert, CircularProgress, InputAdornment, Link as MuiLink
} from '@mui/material'
import PersonIcon from '@mui/icons-material/Person'
import EmailIcon from '@mui/icons-material/Email'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import AuthLayout from '../components/AuthLayout'

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match')
      return
    }
    setLoading(true)
    try {
      await register({ username: form.username, email: form.email, password: form.password })
      toast.success('Registration successful. Please login.')
      navigate('/login')
    } catch (err) {
      const msg = err.response?.data?.error || 'Registration failed'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      title="Create Account"
      subtitle="Join the national disaster management system"
      maxWidth={460}
    >
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box component="form" onSubmit={handleSubmit}>
        <TextField
          fullWidth size="small" label="Username" required margin="normal"
          value={form.username}
          onChange={e => setForm({ ...form, username: e.target.value })}
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><PersonIcon fontSize="small" /></InputAdornment> } }}
        />
        <TextField
          fullWidth size="small" label="Email" type="email" required margin="normal"
          value={form.email}
          onChange={e => setForm({ ...form, email: e.target.value })}
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><EmailIcon fontSize="small" /></InputAdornment> } }}
        />
        <TextField
          fullWidth size="small" label="Password" type="password" required margin="normal"
          value={form.password}
          onChange={e => setForm({ ...form, password: e.target.value })}
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><LockOutlinedIcon fontSize="small" /></InputAdornment> } }}
        />
        <TextField
          fullWidth size="small" label="Confirm Password" type="password" required margin="normal"
          value={form.confirmPassword}
          onChange={e => setForm({ ...form, confirmPassword: e.target.value })}
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><LockOutlinedIcon fontSize="small" /></InputAdornment> } }}
        />
        <Button
          type="submit" fullWidth variant="contained" size="large" disabled={loading}
          sx={{ mt: 2, py: 1.3 }}
        >
          {loading ? <CircularProgress size={22} color="inherit" /> : 'Register'}
        </Button>
      </Box>

      <Box sx={{ textAlign: 'center', mt: 2.5 }}>
        <Typography variant="body2">
          Already have an account?{' '}
          <MuiLink component={Link} to="/login" sx={{ fontWeight: 700 }}>Sign in</MuiLink>
        </Typography>
      </Box>
    </AuthLayout>
  )
}

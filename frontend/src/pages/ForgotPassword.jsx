import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, TextField, Button, Typography, Alert, CircularProgress,
  InputAdornment, Link as MuiLink, Stack
} from '@mui/material'
import LockResetIcon from '@mui/icons-material/LockReset'
import MailOutlineIcon from '@mui/icons-material/MailOutlined'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import AuthLayout from '../components/AuthLayout'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authApi.forgotPassword(email)
      setDone(true)
      toast.success('Password reset link sent if the email exists')
    } catch (err) {
      const msg = err.response?.data?.error || 'Something went wrong'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      title="Forgot Password"
      subtitle="Enter your registered email to receive a secure reset link."
    >
      {done ? (
        <Alert severity="success" sx={{ mb: 2 }}>
          If an account exists for that email, a password reset link has been sent. Please check your inbox.
        </Alert>
      ) : (
        <Box component="form" onSubmit={handleSubmit}>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <TextField
            fullWidth size="small" label="Email Address" type="email" required
            value={email} onChange={e => setEmail(e.target.value)} autoComplete="email"
            slotProps={{
              input: {
                startAdornment: <InputAdornment position="start"><MailOutlineIcon fontSize="small" /></InputAdornment>
              }
            }}
          />
          <Button type="submit" fullWidth variant="contained" size="large" disabled={loading}
            startIcon={<LockResetIcon />} sx={{ mt: 2.5, py: 1.3 }}>
            {loading ? <CircularProgress size={22} color="inherit" /> : 'Send Reset Link'}
          </Button>
        </Box>
      )}

      <Stack sx={{ mt: 2.5 }} direction="row" justifyContent="center">
        <Button size="small" startIcon={<ArrowBackIcon />} onClick={() => navigate('/login')} sx={{ textTransform: 'none' }}>
          Back to Login
        </Button>
      </Stack>
    </AuthLayout>
  )
}

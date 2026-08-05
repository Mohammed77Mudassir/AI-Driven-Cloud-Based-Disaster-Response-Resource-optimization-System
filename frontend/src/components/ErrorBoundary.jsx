import { Component } from 'react'
import { Box, Typography, Button } from '@mui/material'
import ErrorIcon from '@mui/icons-material/Error'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null, errorInfo: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ errorInfo })
    console.error('ErrorBoundary caught:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback
      return (
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', p: 4, textAlign: 'center' }}>
          <ErrorIcon sx={{ fontSize: 64, color: '#C62828', mb: 2 }} />
          <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>Something went wrong</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
            An unexpected error occurred. Please try refreshing the page.
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ mb: 3, maxWidth: 500, fontFamily: 'monospace' }}>
            {this.state.error?.message}
          </Typography>
          <Button variant="contained" onClick={() => { this.setState({ hasError: false }); window.location.href = '/' }}>
            Reload Application
          </Button>
        </Box>
      )
    }
    return this.props.children
  }
}

import { Box, Typography } from '@mui/material'
import Breadcrumbs from './Breadcrumbs'
import WebSocketStatus from './WebSocketStatus'
import { useWebSocket } from '../context/WebSocketContext'

export default function PageHeader({ title, subtitle, actions, showLive = true, showBreadcrumbs = true }) {
  const { connected, status, reconnectAttempt } = useWebSocket()

  return (
    <Box sx={{ mb: 2.5 }}>
      {showBreadcrumbs && <Breadcrumbs />}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap' }}>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 800, letterSpacing: '-0.3px' }}>
            {title}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
              {subtitle}
            </Typography>
          )}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
          {showLive && <WebSocketStatus connected={connected} status={status} reconnectAttempt={reconnectAttempt} />}
          {actions}
        </Box>
      </Box>
    </Box>
  )
}

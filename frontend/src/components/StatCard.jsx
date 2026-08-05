import { Box, Card, CardContent, Typography } from '@mui/material'
import AnimatedCounter from './AnimatedCounter'

export default function StatCard({ icon, value, label, sublabel, color = 'primary.main', delay = 0, format }) {
  return (
    <Card
      sx={{
        height: '100%',
        position: 'relative',
        overflow: 'hidden',
        borderTop: `3px solid ${color}`,
        '&::after': {
          content: '""',
          position: 'absolute',
          right: -20,
          top: -20,
          width: 90,
          height: 90,
          borderRadius: '50%',
          background: `${color}`,
          opacity: 0.06,
        },
      }}
    >
      <CardContent sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box
          aria-hidden
          sx={{
            width: 50,
            height: 50,
            borderRadius: 2.5,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: `${color}1A`,
            color,
            flexShrink: 0,
          }}
        >
          {icon}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <AnimatedCounter value={value} duration={700} sx={{ fontWeight: 800, lineHeight: 1.15, fontSize: '1.5rem' }} />
          <Typography variant="body2" color="text.secondary" noWrap>{label}</Typography>
          {sublabel && <Typography variant="caption" color="text.disabled" noWrap>{sublabel}</Typography>}
        </Box>
      </CardContent>
    </Card>
  )
}

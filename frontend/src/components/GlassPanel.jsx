import { Box, Typography, useTheme } from '@mui/material'

export default function GlassPanel({ children, sx = {} }) {
  const theme = useTheme()
  return (
    <Box
      sx={{
        position: 'relative',
        borderRadius: 3,
        border: `1px solid ${theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.1)' : 'rgba(255,255,255,0.7)'}`,
        background: theme.palette.mode === 'dark'
          ? 'linear-gradient(160deg, rgba(21,34,56,0.9), rgba(16,27,46,0.85))'
          : 'rgba(255,255,255,0.82)',
        backdropFilter: 'blur(14px)',
        WebkitBackdropFilter: 'blur(14px)',
        boxShadow: `0 8px 32px ${theme.palette.mode === 'dark' ? 'rgba(0,0,0,0.45)' : 'rgba(16,24,40,0.12)'}`,
        ...sx,
      }}
    >
      {children}
    </Box>
  )
}

export function GlassSection({ title, subtitle, icon: Icon, children, sx = {} }) {
  const theme = useTheme()
  return (
    <GlassPanel sx={{ p: { xs: 2, sm: 3 }, ...sx }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
        {Icon && <Icon sx={{ color: theme.palette.primary.main, fontSize: 22 }} />}
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>{title}</Typography>
          {subtitle && <Typography variant="caption" color="text.secondary">{subtitle}</Typography>}
        </Box>
      </Box>
      {children}
    </GlassPanel>
  )
}

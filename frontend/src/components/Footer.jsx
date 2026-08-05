import { Box, Typography, Divider, Stack, Chip, useTheme } from '@mui/material';

export default function Footer() {
  const theme = useTheme();
  return (
    <Box component="footer" sx={{ mt: 4, pt: 1.5 }}>
      <Divider sx={{ mb: 2 }} />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} justifyContent="space-between" alignItems="center">
        <Box>
          <Typography variant="body2" color="text.secondary">
            AI Disaster Management System{' '}
            <Box component="span" sx={{ color: 'primary.main', fontWeight: 700 }}>v3.0</Box>{' '}
            · Emergency Operations Center
          </Typography>
          <Typography variant="caption" color="text.disabled" display="block" sx={{ mt: 0.25 }}>
            &copy; {new Date().getFullYear()} National Disaster Management Authority. All Rights Reserved.
          </Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Chip label="Emergency 112" size="small" variant="soft" color="error" sx={{ fontWeight: 700 }} />
          <Chip label="Helpline 1078" size="small" variant="soft" color="info" sx={{ fontWeight: 700 }} />
          <Chip label={`${new Date().getFullYear()}`} size="small" variant="soft" color="default" />
        </Stack>
      </Stack>
    </Box>
  );
}

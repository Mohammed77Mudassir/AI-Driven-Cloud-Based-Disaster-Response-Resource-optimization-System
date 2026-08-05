import { Box, Typography, Button } from '@mui/material';
import SearchOffIcon from '@mui/icons-material/SearchOff';

export default function EmptyState({ title = 'No data found', message = 'There are no items to display.', actionLabel, onAction, icon }) {
  return (
    <Box sx={{ textAlign: 'center', py: 6, px: 2 }}>
      <Box
        aria-hidden
        sx={{
          width: 80,
          height: 80,
          mx: 'auto',
          mb: 2,
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: 'action.hover',
          color: 'text.disabled',
        }}
      >
        {icon || <SearchOffIcon sx={{ fontSize: 40 }} />}
      </Box>
      <Typography variant="h6" sx={{ fontWeight: 700 }} gutterBottom>
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3, maxWidth: 360, mx: 'auto' }}>
        {message}
      </Typography>
      {actionLabel && onAction && (
        <Button variant="contained" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </Box>
  );
}

import { Chip, Tooltip, useTheme } from '@mui/material';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import SyncIcon from '@mui/icons-material/Sync';

export default function WebSocketStatus({ connected, status, reconnectAttempt }) {
  const theme = useTheme();
  const effectiveStatus = status ?? (connected ? 'connected' : 'disconnected');
  const isLive = effectiveStatus === 'connected';

  const labelMap = {
    connected: 'LIVE',
    connecting: 'CONNECTING',
    reconnecting: `RECONNECTING${reconnectAttempt ? ` (${reconnectAttempt})` : ''}`,
    disconnected: 'OFFLINE',
  };
  const label = labelMap[effectiveStatus] || 'OFFLINE';
  const color = isLive
    ? theme.palette.success.main
    : effectiveStatus === 'reconnecting'
    ? theme.palette.warning.main
    : theme.palette.error.main;

  const tooltipText = isLive
    ? 'Live connection active'
    : effectiveStatus === 'connecting'
    ? 'Connecting to live feed…'
    : effectiveStatus === 'reconnecting'
    ? `Reconnecting (attempt ${reconnectAttempt || 1})…`
    : 'No connection';

  return (
    <Tooltip title={tooltipText}>
      <Chip
        icon={effectiveStatus === 'reconnecting'
          ? <SyncIcon sx={{ fontSize: 12, color, animation: 'dms-spin 1.2s linear infinite' }} />
          : <FiberManualRecordIcon sx={{ fontSize: 12, color }} />}
        label={label}
        size="small"
        sx={{
          backgroundColor: `${color}1F`,
          color,
          fontWeight: 700,
          fontSize: '0.7rem',
          border: `1px solid ${color}66`,
          letterSpacing: '0.3px',
        }}
      />
    </Tooltip>
  );
}

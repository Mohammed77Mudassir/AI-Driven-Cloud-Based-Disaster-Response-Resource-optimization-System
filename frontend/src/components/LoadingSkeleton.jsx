import { Skeleton, Card, Box, Stack } from '@mui/material';

export function TableSkeleton({ rows = 5 }) {
  return (
    <Card sx={{ p: 2.5 }}>
      <Stack spacing={1.25}>
        {Array.from({ length: rows }).map((_, i) => (
          <Skeleton key={i} variant="rectangular" height={44} sx={{ borderRadius: 1.5 }} />
        ))}
      </Stack>
    </Card>
  );
}

export function StatsSkeleton({ count = 4 }) {
  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
      {Array.from({ length: count }).map((_, i) => (
        <Card key={i} sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 2 }}>
          <Skeleton variant="rounded" width={50} height={50} sx={{ borderRadius: 2.5 }} />
          <Box sx={{ flex: 1 }}>
            <Skeleton variant="text" width="55%" sx={{ fontSize: '1.6rem' }} />
            <Skeleton variant="text" width="70%" />
          </Box>
        </Card>
      ))}
    </Box>
  );
}

export function ChartSkeleton({ height = 250 }) {
  return (
    <Card sx={{ p: 2.5 }}>
      <Skeleton variant="text" width="40%" sx={{ fontSize: '1rem' }} />
      <Skeleton variant="rectangular" height={height} sx={{ mt: 2, borderRadius: 2 }} />
    </Card>
  );
}

export function PageSkeleton() {
  return (
    <Box>
      <StatsSkeleton />
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 2 }}>
        <ChartSkeleton /><ChartSkeleton />
      </Box>
      <Box sx={{ mt: 2 }}><TableSkeleton /></Box>
    </Box>
  );
}

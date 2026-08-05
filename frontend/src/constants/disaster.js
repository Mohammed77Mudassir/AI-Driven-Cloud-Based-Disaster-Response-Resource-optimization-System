export const DISASTER_TYPES = [
  'Flood', 'Earthquake', 'Cyclone', 'Wildfire', 'Tsunami', 'Landslide', 'Drought', 'Epidemic',
]

export const SEVERITIES = ['Low', 'Medium', 'High', 'Critical']

export const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export const STATUSES = [
  'PENDING', 'VERIFIED', 'ASSIGNED', 'RESOURCES_DISPATCHED', 'IN_PROGRESS', 'RESOLVED',
]

export const severityColor = {
  Low: '#6bcb77',
  Medium: '#f9a825',
  High: '#ff8800',
  Critical: '#e53935',
}

export const statusColor = {
  PENDING: '#f9a825',
  VERIFIED: '#26a69a',
  ASSIGNED: '#4d96ff',
  RESOURCES_DISPATCHED: '#8e24aa',
  IN_PROGRESS: '#ff8800',
  RESOLVED: '#6bcb77',
}

export const priorityColor = {
  LOW: '#6bcb77',
  MEDIUM: '#f9a825',
  HIGH: '#ff8800',
  CRITICAL: '#e53935',
}

export const statusLabel = {
  PENDING: 'Pending',
  VERIFIED: 'Verified',
  ASSIGNED: 'Assigned',
  RESOURCES_DISPATCHED: 'Resources Dispatched',
  IN_PROGRESS: 'In Progress',
  RESOLVED: 'Resolved',
}

/**
 * Allowed status transitions (mirrors the backend DisasterService policy).
 * PENDING -> VERIFIED -> ASSIGNED -> RESOURCES_DISPATCHED -> IN_PROGRESS -> RESOLVED
 */
export const STATUS_TRANSITIONS = {
  PENDING: ['VERIFIED', 'ASSIGNED'],
  VERIFIED: ['ASSIGNED', 'IN_PROGRESS'],
  ASSIGNED: ['RESOURCES_DISPATCHED', 'IN_PROGRESS'],
  RESOURCES_DISPATCHED: ['IN_PROGRESS'],
  IN_PROGRESS: ['RESOLVED'],
  RESOLVED: [],
}

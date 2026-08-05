import axios from 'axios'

const BASE = '/api'

const getStoredUser = () => {
  let raw = localStorage.getItem('user')
  if (!raw) raw = sessionStorage.getItem('user')
  if (!raw) return null
  try {
    const user = JSON.parse(raw)
    if (!user || typeof user !== 'object' || !user.token) return null
    return user
  } catch { return null }
}

export const getStoredUserSafe = () => getStoredUser() || {}

const setStoredUser = (user) => {
  const storage = user?.persist ? localStorage : sessionStorage
  storage.setItem('user', JSON.stringify(user))
}

const clearStoredUser = () => {
  localStorage.removeItem('user')
  sessionStorage.removeItem('user')
}

export const api = axios.create({
  baseURL: BASE,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const user = getStoredUser()
  if (user?.token) config.headers.Authorization = `Bearer ${user.token}`
  return config
})

let isRefreshing = false
let queue = []

const processQueue = (error) => {
  queue.forEach(p => p(error))
  queue = []
}

// Endpoints that return 401 legitimately (bad credentials / expired refresh
// token) and must never trigger the token-refresh flow or wipe the session.
const AUTH_BYPASS_PATTERNS = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
  '/auth/verify-email',
  '/auth/forgot-password',
  '/auth/reset-password'
]

const isAuthBypass = (url) => AUTH_BYPASS_PATTERNS.some(p => url?.includes(p))

const refreshAccessToken = async () => {
  const user = getStoredUser()
  if (!user?.refreshToken) throw new Error('No refresh token')
  const res = await axios.post(`${BASE}/auth/refresh`, { refreshToken: user.refreshToken })
  const { token, refreshToken } = res.data
  const updated = { ...user, token, refreshToken: refreshToken || user.refreshToken, persist: user.persist }
  setStoredUser(updated)
  return updated
}

api.interceptors.response.use(
  res => res,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && !original?._retry && !isAuthBypass(original?.url)) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          queue.push(err => err ? reject(err) : resolve(api(original)))
        })
      }
      original._retry = true
      isRefreshing = true
      try {
        const updated = await refreshAccessToken()
        original.headers.Authorization = `Bearer ${updated.token}`
        processQueue(null)
        return api(original)
      } catch (refreshError) {
        processQueue(refreshError)
        clearStoredUser()
        if (!window.location.pathname.startsWith('/login') &&
            !window.location.pathname.startsWith('/public')) {
          window.location.href = '/login'
        }
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }
    return Promise.reject(error)
  }
)

// Public axios instance - intentionally NO auth headers and NO session redirect.
export const publicApi = axios.create({
  baseURL: BASE,
  headers: { 'Content-Type': 'application/json' }
})

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  me: () => api.get('/auth/me'),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken }),
  verifyEmail: (token) => api.post('/auth/verify-email', { token }),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) => api.post('/auth/reset-password', { token, newPassword })
}

export const publicReportApi = {
  submit: (data) => publicApi.post('/public/disasters', data),
  track: (reportId) => publicApi.get(`/public/disasters/${reportId}`),
  trackTimeline: (reportId) => publicApi.get(`/public/disasters/${reportId}/timeline`)
}

export const disasterApi = {
  getAll: (params) => api.get('/disasters', { params }),
  getMy: (params) => api.get('/disasters/my', { params }),
  getById: (id) => api.get(`/disasters/${id}`),
  getDetail: (id) => api.get(`/disasters/${id}/detail`),
  create: (data) => api.post('/disasters', data),
  update: (id, data) => api.put(`/disasters/${id}`, data),
  updatePriority: (id, priority) => api.put(`/disasters/${id}/priority`, { priority }),
  updateStatus: (id, data) => api.put(`/disasters/${id}/status`, data),
  delete: (id) => api.delete(`/disasters/${id}`),
  getStatusFlow: () => api.get('/disasters/status-flow'),
  getTimeline: (id) => api.get(`/disasters/${id}/timeline`),
  getComments: (id) => api.get(`/disasters/${id}/comments`),
  addComment: (id, data) => api.post(`/disasters/${id}/comments`, data),
  deleteComment: (commentId) => api.delete(`/disasters/comments/${commentId}`),
  getAssignments: (id) => api.get(`/disasters/${id}/assignments`)
}

export const predictionApi = {
  predict: (data) => api.post('/predictions', data)
}

export const recommendationApi = {
  recommend: (data) => api.post('/recommendations', data)
}

export const aiApi = {
  analyze: (data) => api.post('/ai/analyze', data),
  recommend: (data) => api.post('/ai/recommendations', data),
  models: () => api.get('/ai/models'),
  selfTest: () => api.get('/ai/self-test')
}

export const weatherApi = {
  get: (location, latitude, longitude) => api.get('/weather', { params: { location, latitude, longitude } })
}

export const hospitalApi = {
  getAll: () => api.get('/hospitals'),
  getById: (id) => api.get(`/hospitals/${id}`),
  create: (data) => api.post('/hospitals', data),
  update: (id, data) => api.put(`/hospitals/${id}`, data),
  delete: (id) => api.delete(`/hospitals/${id}`)
}

export const hospitalOperationsApi = {
  getDashboard: () => api.get('/hospitals/operations/dashboard')
}

export const shelterApi = {
  getAll: () => api.get('/shelters'),
  getById: (id) => api.get(`/shelters/${id}`),
  create: (data) => api.post('/shelters', data),
  update: (id, data) => api.put(`/shelters/${id}`, data),
  delete: (id) => api.delete(`/shelters/${id}`)
}

export const shelterOperationsApi = {
  getDashboard: () => api.get('/shelters/operations/dashboard')
}

export const volunteerApi = {
  getAll: () => api.get('/volunteers'),
  getById: (id) => api.get(`/volunteers/${id}`),
  getAvailable: () => api.get('/volunteers/available'),
  create: (data) => api.post('/volunteers', data),
  update: (id, data) => api.put(`/volunteers/${id}`, data),
  delete: (id) => api.delete(`/volunteers/${id}`)
}

export const resourceApi = {
  getAll: () => api.get('/resources'),
  getById: (id) => api.get(`/resources/${id}`),
  getAvailable: () => api.get('/resources/available'),
  getByDisaster: (id) => api.get(`/resources/disaster/${id}`),
  getByMission: (id) => api.get(`/resources/mission/${id}`),
  create: (data) => api.post('/resources', data),
  update: (id, data) => api.put(`/resources/${id}`, data),
  delete: (id) => api.delete(`/resources/${id}`),
  deploy: (id, quantity, missionId) => api.put(`/resources/${id}/deploy`, { quantity, missionId }),
  returnResource: (id, quantity) => api.put(`/resources/${id}/return`, { quantity }),
  startMaintenance: (id, quantity) => api.put(`/resources/${id}/maintenance`, { quantity }),
  completeMaintenance: (id, quantity) => api.put(`/resources/${id}/maintenance/complete`, { quantity }),
  getMovements: () => api.get('/resources/movements'),
  getMovementsByResource: (id) => api.get(`/resources/${id}/movements`),
  getMovementsByMission: (missionId) => api.get('/resources/movements', { params: { missionId } }),
  getMovementsByType: (type) => api.get('/resources/movements', { params: { type } })
}

export const droneApi = {
  getAll: () => api.get('/drones'),
  getById: (id) => api.get(`/drones/${id}`),
  create: (data) => api.post('/drones', data),
  update: (id, data) => api.put(`/drones/${id}`, data),
  delete: (id) => api.delete(`/drones/${id}`),
  updateLocation: (id, data) => api.put(`/drones/${id}/location`, data)
}

export const notificationApi = {
  getAll: () => api.get('/notifications'),
  getUnread: () => api.get('/notifications/unread'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all')
}

export const auditLogApi = {
  getAll: () => api.get('/audit-logs'),
  getByUser: (username) => api.get(`/audit-logs/user/${username}`)
}

export const analyticsApi = {
  get: () => api.get('/analytics')
}

export const exportApi = {
  csv: () => api.get('/exports/csv', { responseType: 'blob' }),
  pdf: () => api.get('/exports/pdf', { responseType: 'blob' }),
  excel: () => api.get('/exports/excel', { responseType: 'blob' })
}

export const locationApi = {
  getAll: () => api.get('/locations'),
  getLive: () => api.get('/locations/live')
}

export const monitoringApi = {
  overview: () => api.get('/monitoring/overview'),
  locations: () => api.get('/monitoring/locations'),
  heatmap: () => api.get('/monitoring/heatmap'),
  distance: (params) => api.get('/monitoring/distance', { params }),
  eta: (params) => api.get('/monitoring/eta', { params }),
  route: (params) => api.get('/monitoring/route', { params }),
  wsStatus: () => api.get('/monitoring/ws-status'),
  getOverview: () => api.get('/monitoring/overview'),
  getLocations: () => api.get('/monitoring/locations'),
  getHeatmap: () => api.get('/monitoring/heatmap'),
  getDistance: (params) => api.get('/monitoring/distance', { params }),
  getEta: (params) => api.get('/monitoring/eta', { params }),
  getRoute: (params) => api.get('/monitoring/route', { params }),
  getWsStatus: () => api.get('/monitoring/ws-status')
}


export const timelineApi = {
  getByDisaster: (id) => api.get(`/timelines/disaster/${id}`)
}

export const userApi = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  update: (id, data) => api.put(`/users/${id}`, data),
  delete: (id) => api.delete(`/users/${id}`),
  activate: (id) => api.put(`/users/${id}/activate`),
  deactivate: (id) => api.put(`/users/${id}/deactivate`),
  changeRole: (id, role) => api.put(`/users/${id}/role`, { role }),
  resetPassword: (id, newPassword) => api.post(`/users/${id}/reset-password`, { newPassword }),
  changePassword: (data) => api.post('/users/change-password', data),
  getProfile: () => api.get('/users/profile'),
  updateProfile: (data) => api.put('/users/profile', data),
  getActivity: (id) => api.get(`/users/activity/${id}`),
  search: (query) => api.get('/users', { params: { search: query } }),
  filterByRole: (role) => api.get('/users', { params: { role } }),
  filterByStatus: (status) => api.get('/users', { params: { status } })
}

export const rescueTeamApi = {
  getAll: () => api.get('/rescue-teams'),
  getById: (id) => api.get(`/rescue-teams/${id}`),
  create: (data) => api.post('/rescue-teams', data),
  update: (id, data) => api.put(`/rescue-teams/${id}`, data),
  delete: (id) => api.delete(`/rescue-teams/${id}`),
  assignToDisaster: (teamId, disasterId) => api.put(`/rescue-teams/${teamId}/assign/${disasterId}`),
  updateStatus: (id, status) => api.put(`/rescue-teams/${id}/status`, { status }),
  getByStatus: (status) => api.get(`/rescue-teams/status/${status}`),
  getByDisaster: (id) => api.get(`/rescue-teams/disaster/${id}`),
  getMembers: (id) => api.get(`/rescue-teams/${id}/members`),
  addMember: (teamId, data) => api.post(`/rescue-teams/${teamId}/members`, data),
  updateMember: (memberId, data) => api.put(`/rescue-teams/members/${memberId}`, data),
  assignLeader: (memberId, teamId) => api.put(`/rescue-teams/members/${memberId}/leader`, { teamId }),
  removeMember: (memberId) => api.delete(`/rescue-teams/members/${memberId}`),
  getAvailabilityOverview: () => api.get('/rescue-teams/availability'),
  getAvailability: (id) => api.get(`/rescue-teams/${id}/availability`),
  getLatestLocations: () => api.get('/rescue-teams/locations/latest'),
  getTeamLocations: (id) => api.get(`/rescue-teams/${id}/locations`),
  getLatestLocation: (id) => api.get(`/rescue-teams/${id}/locations/latest`),
  recordLocation: (id, data) => api.post(`/rescue-teams/${id}/location`, data)
}

export const commandCenterApi = {
  getDashboard: () => api.get('/rescue-teams/command-center/dashboard')
}

export const vehicleApi = {
  getAll: () => api.get('/rescue-vehicles'),
  getById: (id) => api.get(`/rescue-vehicles/${id}`),
  getByStatus: (status) => api.get(`/rescue-vehicles/status/${status}`),
  getByTeam: (teamId) => api.get(`/rescue-vehicles/team/${teamId}`),
  getByMission: (missionId) => api.get(`/rescue-vehicles/mission/${missionId}`),
  create: (data) => api.post('/rescue-vehicles', data),
  update: (id, data) => api.put(`/rescue-vehicles/${id}`, data),
  delete: (id) => api.delete(`/rescue-vehicles/${id}`),
  deploy: (id, missionId) => api.put(`/rescue-vehicles/${id}/deploy`, { missionId }),
  returnVehicle: (id) => api.put(`/rescue-vehicles/${id}/return`),
  startMaintenance: (id) => api.put(`/rescue-vehicles/${id}/maintenance`),
  completeMaintenance: (id) => api.put(`/rescue-vehicles/${id}/maintenance/complete`)
}

export const equipmentApi = {
  getAll: () => api.get('/rescue-equipment'),
  getById: (id) => api.get(`/rescue-equipment/${id}`),
  getByStatus: (status) => api.get(`/rescue-equipment/status/${status}`),
  getByTeam: (teamId) => api.get(`/rescue-equipment/team/${teamId}`),
  getByMission: (missionId) => api.get(`/rescue-equipment/mission/${missionId}`),
  create: (data) => api.post('/rescue-equipment', data),
  update: (id, data) => api.put(`/rescue-equipment/${id}`, data),
  delete: (id) => api.delete(`/rescue-equipment/${id}`),
  deploy: (id, quantity, missionId) => api.put(`/rescue-equipment/${id}/deploy`, { quantity, missionId }),
  returnEquipment: (id, quantity) => api.put(`/rescue-equipment/${id}/return`, { quantity }),
  startMaintenance: (id, quantity) => api.put(`/rescue-equipment/${id}/maintenance`, { quantity }),
  completeMaintenance: (id, quantity) => api.put(`/rescue-equipment/${id}/maintenance/complete`, { quantity })
}

export const missionApi = {
  getAll: () => api.get('/missions'),
  getById: (id) => api.get(`/missions/${id}`),
  getByStatus: (status) => api.get(`/missions/status/${status}`),
  getByTeam: (teamId) => api.get(`/missions/team/${teamId}`),
  getByTeamAndStatus: (teamId, status) => api.get(`/missions/team/${teamId}/status/${status}`),
  getByDisaster: (disasterId) => api.get(`/missions/disaster/${disasterId}`),
  getEvents: (id) => api.get(`/missions/${id}/events`),
  getTransitions: (status) => api.get(`/missions/transitions/${status}`),
  create: (data) => api.post('/missions', data),
  update: (id, data) => api.put(`/missions/${id}`, data),
  updateStatus: (id, status) => api.put(`/missions/${id}/status`, { status }),
  delete: (id) => api.delete(`/missions/${id}`)
}

export const shiftApi = {
  getAll: () => api.get('/shifts'),
  getById: (id) => api.get(`/shifts/${id}`),
  getByTeam: (teamId) => api.get(`/shifts/team/${teamId}`),
  getByMember: (memberId) => api.get(`/shifts/member/${memberId}`),
  getRoster: (date) => api.get('/shifts/roster', { params: { date } }),
  getRosterRange: (from, to) => api.get('/shifts/roster', { params: { from, to } }),
  create: (data) => api.post('/shifts', data),
  update: (id, data) => api.put(`/shifts/${id}`, data),
  updateStatus: (id, status) => api.put(`/shifts/${id}/status`, { status }),
  delete: (id) => api.delete(`/shifts/${id}`)
}

export const routeApi = {
  calculate: (data) => api.post('/routes/calculate', data),
  optimize: (data) => api.post('/routes/optimize', data),
  getClosures: () => api.get('/routes/closures'),
  createClosure: (data) => api.post('/routes/closures', data),
  deleteClosure: (id) => api.delete(`/routes/closures/${id}`)
}

export const caseStudyApi = {
  getAll: () => api.get('/case-studies'),
  getById: (id) => api.get(`/case-studies/${id}`),
  create: (data) => api.post('/case-studies', data),
  update: (id, data) => api.put(`/case-studies/${id}`, data),
  delete: (id) => api.delete(`/case-studies/${id}`),
  search: (params) => api.get('/case-studies/search', { params }),
  getComparison: (params) => api.get('/case-studies/comparison', { params })
}

export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),
  getSettings: () => api.get('/admin/settings'),
  updateSetting: (key, value) => api.put(`/admin/settings/${key}`, { value }),
  getNotifications: () => api.get('/admin/notifications')
}

export default api

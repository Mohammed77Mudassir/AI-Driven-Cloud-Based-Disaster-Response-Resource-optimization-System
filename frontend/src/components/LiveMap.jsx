import { useState, useEffect, useRef, useMemo, useCallback } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './LiveMap.css'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

const markerColors = {
  red: '#ff4444',
  orange: '#ff8800',
  yellow: '#ffbb33',
  green: '#00C851',
  blue: '#33b5e5',
  teal: '#00897b',
  gray: '#888',
}

const ANIMATED_TYPES = new Set(['DRONE', 'RESCUE_TEAM', 'VEHICLE'])

function createColoredIcon(color, animate) {
  const inner = animate
    ? `<span class="dms-ping"></span>`
    : ''
  return L.divIcon({
    className: 'custom-marker dms-marker',
    html: `<div class="dms-marker" style="color:${color}">${inner}<div class="dms-marker-dot" style="background:${color}"></div></div>`,
    iconSize: [30, 30],
    iconAnchor: [15, 15],
    popupAnchor: [0, -15],
  })
}

function clusterIcon(count) {
  return L.divIcon({
    className: 'dms-cluster',
    html: `<div class="dms-cluster-inner">${count}</div>`,
    iconSize: [38, 38],
    iconAnchor: [19, 19],
  })
}

function waypointIcon(label, color) {
  return L.divIcon({
    className: 'dms-waypoint',
    html: `<div class="dms-waypoint" style="background:${color}"><span>${label}</span></div>`,
    iconSize: [26, 26],
    iconAnchor: [13, 13],
    popupAnchor: [0, -13],
  })
}

const shouldAnimate = (loc) =>
  ANIMATED_TYPES.has(loc?.entityType) || (loc?.speed > 0) || loc?.entityType === 'DRONE'

function LocationPopup({ loc }) {
  return (
    <div style={{ minWidth: '170px', maxWidth: '220px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 2 }}>
        <span style={{ width: 9, height: 9, borderRadius: '50%', backgroundColor: markerColors[loc.markerColor] || '#888', display: 'inline-block' }} />
        <strong>{loc.name || loc.entityType}</strong>
      </div>
      <span style={{ fontSize: '0.8rem', color: '#666' }}>{loc.entityType}</span>
      {loc.severity && <div style={{ fontSize: '0.8rem' }}>Severity: <b>{loc.severity}</b></div>}
      {loc.status && <div style={{ fontSize: '0.8rem' }}>Status: {loc.status}</div>}
      {loc.speed != null && loc.speed > 0 && (
        <div style={{ fontSize: '0.8rem' }}>Speed: {loc.speed?.toFixed(1)} km/h</div>
      )}
      {loc.battery != null && loc.entityType === 'DRONE' && (
        <div style={{ fontSize: '0.8rem' }}>Battery: {loc.battery}%</div>
      )}
      {loc.occupancy != null && loc.entityType === 'SHELTER' && (
        <div style={{ fontSize: '0.8rem' }}>Occupancy: {loc.occupancy}%</div>
      )}
      <div style={{ fontSize: '0.78rem', color: '#999' }}>
        {loc.latitude?.toFixed(4)}, {loc.longitude?.toFixed(4)}
      </div>
      {loc.updatedAt && (
        <div style={{ fontSize: '0.72rem', color: '#bbb' }}>
          Updated: {new Date(loc.updatedAt).toLocaleTimeString()}
        </div>
      )}
    </div>
  )
}

// ---------------------------------------------------------------------------
// Grid clustering with pure Leaflet (no extra plugin required).
// ---------------------------------------------------------------------------
function MarkersLayer({ locations, clusterEnabled }) {
  const map = useMap()
  const [view, setView] = useState(0)

  useEffect(() => {
    const update = () => setView(v => v + 1)
    map.on('moveend zoomend', update)
    return () => {
      map.off('moveend zoomend', update)
    }
  }, [map])

  const rendered = useMemo(() => {
    if (!locations.length) return []
    if (!clusterEnabled || locations.length <= 60) {
      return locations.map(loc => ({
        loc,
        lat: loc.latitude,
        lng: loc.longitude,
        count: 1,
        isCluster: false,
      }))
    }

    const bounds = map.getBounds()
    const size = map.getSize()
    const cellPx = 64
    const cols = Math.max(1, Math.round(size.x / cellPx))
    const rows = Math.max(1, Math.round(size.y / cellPx))
    const south = bounds.getSouth()
    const north = bounds.getNorth()
    const west = bounds.getWest()
    const east = bounds.getEast()
    const latSpan = (north - south) / rows
    const lngSpan = (east - west) / cols

    const groups = new Map()
    locations.forEach(loc => {
      const cx = Math.min(cols - 1, Math.max(0, Math.floor((loc.longitude - west) / lngSpan)))
      const cy = Math.min(rows - 1, Math.max(0, Math.floor((loc.latitude - south) / latSpan)))
      const key = `${cx}:${cy}`
      if (!groups.has(key)) groups.set(key, [])
      groups.get(key).push(loc)
    })

    const out = []
    groups.forEach(group => {
      if (group.length === 1) {
        const loc = group[0]
        out.push({ loc, lat: loc.latitude, lng: loc.longitude, count: 1, isCluster: false })
      } else {
        let lat = 0
        let lng = 0
        group.forEach(l => {
          lat += l.latitude
          lng += l.longitude
        })
        out.push({
          count: group.length,
          lat: lat / group.length,
          lng: lng / group.length,
          isCluster: true,
          members: group,
        })
      }
    })
    return out
  }, [locations, clusterEnabled, map, view])

  return (
    <>
      {rendered.map((item, idx) => {
        if (item.isCluster) {
          return (
            <Marker key={`cluster-${idx}`} position={[item.lat, item.lng]} icon={clusterIcon(item.count)}>
              <Popup>
                <div style={{ minWidth: '150px' }}>
                  <strong>{item.count} locations</strong>
                  <div style={{ fontSize: '0.8rem', color: '#666', marginTop: 4 }}>
                    {item.members.slice(0, 6).map(m => `${m.entityType}${m.name ? ` · ${m.name}` : ''}`).join(', ')}
                    {item.count > 6 ? ` +${item.count - 6} more` : ''}
                  </div>
                </div>
              </Popup>
            </Marker>
          )
        }
        const loc = item.loc
        const color = markerColors[loc.markerColor] || markerColors.blue
        return (
          <Marker
            key={`${loc.entityType}-${loc.entityId || idx}`}
            position={[loc.latitude, loc.longitude]}
            icon={createColoredIcon(color, shouldAnimate(loc))}
          >
            <Popup>
              <LocationPopup loc={loc} />
            </Popup>
          </Marker>
        )
      })}
    </>
  )
}

// ---------------------------------------------------------------------------
// Canvas heat map overlay (pure Leaflet, no leaflet.heat plugin).
// ---------------------------------------------------------------------------
function HeatLayer({ points, visible }) {
  const map = useMap()
  const canvasRef = useRef(null)

  useEffect(() => {
    if (!visible) {
      if (canvasRef.current) {
        const ctx = canvasRef.current.getContext('2d')
        ctx?.clearRect(0, 0, canvasRef.current.width, canvasRef.current.height)
      }
      return
    }

    const pane = map.getPane('overlayPane')
    let canvas = canvasRef.current
    if (!canvas) {
      canvas = document.createElement('canvas')
      canvas.style.position = 'absolute'
      canvas.style.top = '0'
      canvas.style.left = '0'
      canvas.style.pointerEvents = 'none'
      canvasRef.current = canvas
      pane.appendChild(canvas)
    }

    const size = map.getSize()
    canvas.width = size.x
    canvas.height = size.y
    canvas.style.width = `${size.x}px`
    canvas.style.height = `${size.y}px`

    const draw = () => {
      const ctx = canvas.getContext('2d')
      if (!ctx) return
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      if (!points.length) return
      ctx.globalCompositeOperation = 'lighter'
      const zoom = map.getZoom()
      points.forEach(p => {
        const pt = map.latLngToContainerPoint([p.latitude, p.longitude])
        const r = Math.max(16, (24 + p.intensity * 30) * Math.pow(1.12, Math.max(0, zoom - 5)))
        const alpha = Math.min(0.6, 0.15 + p.intensity * 0.45)
        const grad = ctx.createRadialGradient(pt.x, pt.y, 0, pt.x, pt.y, r)
        grad.addColorStop(0, `rgba(255,40,0,${alpha})`)
        grad.addColorStop(0.5, `rgba(255,120,0,${alpha * 0.55})`)
        grad.addColorStop(1, 'rgba(255,160,0,0)')
        ctx.fillStyle = grad
        ctx.beginPath()
        ctx.arc(pt.x, pt.y, r, 0, Math.PI * 2)
        ctx.fill()
      })
    }

    draw()
    map.on('move zoom', draw)
    return () => {
      map.off('move zoom', draw)
    }
  }, [map, points, visible])

  return null
}

// ---------------------------------------------------------------------------
// Route polyline + A/B waypoint markers.
// ---------------------------------------------------------------------------
function RouteLayer({ route }) {
  if (!route?.route?.length) return null
  const positions = route.route.map(([lat, lng]) => [lat, lng])
  const start = positions[0]
  const end = positions[positions.length - 1]
  return (
    <>
      <Polyline
        positions={positions}
        pathOptions={{ color: '#0F4C81', weight: 5, opacity: 0.9, dashArray: '10 8' }}
      />
      {start && <Marker position={start} icon={waypointIcon('A', '#2E7D32')} />}
      {end && <Marker position={end} icon={waypointIcon('B', '#C62828')} />}
    </>
  )
}

function FitRoute({ route }) {
  const map = useMap()
  useEffect(() => {
    if (route?.route?.length) {
      map.fitBounds(route.route.map(([lat, lng]) => [lat, lng]), { padding: [50, 50] })
    }
  }, [map, route])
  return null
}

// ---------------------------------------------------------------------------
// Main exported component
// ---------------------------------------------------------------------------
export default function LiveMap({
  locations = [],
  heatPoints = [],
  route = null,
  center = [20.5937, 78.9629],
  zoom = 5,
  height = '500px',
  mapType = 'street',
  clusterEnabled = true,
  heatEnabled = false,
}) {
  const tileUrl =
    mapType === 'satellite'
      ? 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
      : mapType === 'terrain'
      ? 'https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png'
      : 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'

  const attribution =
    mapType === 'satellite'
      ? '&copy; Esri'
      : mapType === 'terrain'
      ? '&copy; OpenTopoMap'
      : '&copy; OpenStreetMap contributors'

  return (
    <div style={{ height, width: '100%', borderRadius: '12px', overflow: 'hidden', position: 'relative' }}>
      <MapContainer center={center} zoom={zoom} style={{ height: '100%', width: '100%' }} key={mapType}>
        <TileLayer url={tileUrl} attribution={attribution} />
        <HeatLayer points={heatPoints} visible={heatEnabled} />
        <MarkersLayer locations={locations} clusterEnabled={clusterEnabled} />
        <RouteLayer route={route} />
        <FitRoute route={route} />
      </MapContainer>
    </div>
  )
}

import { useState, useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

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
  gray: '#888',
  purple: '#aa66cc',
}

function createColoredIcon(color) {
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="background:${color};width:24px;height:24px;border-radius:50%;border:3px solid white;box-shadow:0 2px 6px rgba(0,0,0,0.3);display:flex;align-items:center;justify-content:center;font-size:12px;color:white;font-weight:bold"></div>`,
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12],
  })
}

function FitBounds({ bounds, enabled }) {
  const map = useMap()
  useEffect(() => {
    if (!enabled || !bounds || bounds.length === 0) return
    map.fitBounds(bounds, { padding: [40, 40] })
  }, [enabled, map, bounds])
  return null
}

/**
 * Leaflet/OpenStreetMap map. Keeps the original props (`locations`, `center`,
 * `zoom`, `height`, `mapType`) for full backward compatibility and adds:
 *  - `routes`: array of { points: [[lat,lng],...], color, weight, dashArray }
 *    rendered as coloured route polylines (optionally highlight one).
 *  - `fitToRoutes`: auto-zoom the map to fit markers + routes.
 */
export default function MapView({ locations = [], center = [20.5937, 78.9629], zoom = 5, height = '400px', mapType = 'street', routes = [], fitToRoutes = false }) {
  const tileUrl = mapType === 'satellite'
    ? 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
    : mapType === 'terrain'
    ? 'https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png'
    : 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'

  const attribution = mapType === 'satellite'
    ? '&copy; Esri'
    : mapType === 'terrain'
    ? '&copy; OpenTopoMap'
    : '&copy; OpenStreetMap contributors'

  const allPoints = [
    ...locations.map(l => [l.latitude, l.longitude]),
    ...routes.flatMap(r => r.points || []),
  ]

  const bounds = allPoints.length > 0
    ? L.latLngBounds(allPoints.map(p => [p[0], p[1]]))
    : null

  return (
    <div style={{ height, width: '100%', borderRadius: '12px', overflow: 'hidden' }}>
      <MapContainer center={center} zoom={zoom} style={{ height: '100%', width: '100%' }} key={mapType}>
        <TileLayer url={tileUrl} attribution={attribution} />
        <FitBounds bounds={bounds} enabled={fitToRoutes} />
        {routes.map((route, ri) => (
          <Polyline
            key={ri}
            positions={(route.points || []).map(p => [p[0], p[1]])}
            pathOptions={{
              color: route.color || '#1976d2',
              weight: route.selected ? (route.weight || 6) : (route.weight || 3),
              opacity: route.selected ? 1 : 0.65,
              dashArray: route.dashArray,
              lineJoin: 'round',
            }}
          >
            {route.popup && (
              <Popup>
                <div style={{ minWidth: '150px' }}>
                  <strong>{route.popup.title}</strong><br />
                  <span style={{ fontSize: '0.85rem', color: '#666' }}>{route.popup.body}</span>
                </div>
              </Popup>
            )}
          </Polyline>
        ))}
        {locations.map((loc, idx) => {
          const color = markerColors[loc.markerColor] || markerColors.blue
          return (
            <Marker key={`${loc.entityType}-${loc.entityId || idx}`} position={[loc.latitude, loc.longitude]} icon={createColoredIcon(color)}>
              <Popup>
                <div style={{ minWidth: '150px' }}>
                  <strong>{loc.name || loc.entityType}</strong><br />
                  <span style={{ fontSize: '0.85rem', color: '#666' }}>{loc.entityType}</span><br />
                  {loc.status && <><span style={{ fontSize: '0.85rem' }}>Status: {loc.status}</span><br /></>}
                  <span style={{ fontSize: '0.85rem' }}>Lat: {loc.latitude?.toFixed(4)}, Lng: {loc.longitude?.toFixed(4)}</span>
                </div>
              </Popup>
            </Marker>
          )
        })}
      </MapContainer>
    </div>
  )
}

import { createServer } from 'vite'

const server = await createServer({
  server: { middlewareMode: true },
  appType: 'custom',
  logLevel: 'error',
})

try {
  const mod = await server.ssrLoadModule('/src/pages/PublicReport.jsx')
  const PublicReport = mod.default
  if (typeof PublicReport !== 'function') {
    throw new Error('PublicReport default export is not a component')
  }

  const React = (await server.ssrLoadModule('react')).default
  const { renderToString } = await server.ssrLoadModule('react-dom/server')
  const { MemoryRouter } = await server.ssrLoadModule('react-router-dom')

  const html = renderToString(
    React.createElement(
      MemoryRouter,
      { initialEntries: ['/public-report'] },
      React.createElement(PublicReport)
    )
  )

  const checks = [
    'Report a Disaster',
    'Help Us Save Lives',
    'Incident Information',
    'Reporter Information',
    'Location Information',
    'Incident Description',
    'Evidence Attachments',
    'Disaster Type',
    'Severity',
    'Priority',
    'Your Name',
    'Mobile Number',
    'Email (optional)',
    'Address',
    'Latitude',
    'Longitude',
    'Description',
    'Submit Report',
    'Reset',
    'Share My GPS Location',
    'Emergency Helpline',
    'Confidential',
    'Track a submitted report',
    'Authorized Personnel Login',
  ]

  const missing = checks.filter((c) => !html.includes(c))
  if (missing.length) {
    console.log('MISSING RENDER CONTENT:', missing.join(', '))
    process.exitCode = 1
  } else {
    console.log('SSR RENDER OK — all expected content present. HTML length:', html.length)
  }
} catch (err) {
  console.error('SSR RENDER FAILED:', err && err.stack ? err.stack : err)
  process.exitCode = 1
} finally {
  await server.close()
}

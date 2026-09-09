// All calls go through the /api prefix, which vite.config.js proxies to
// http://localhost:8080 in development.
const API_BASE_URL = '/api/digital-content'

export async function fetchContentList() {
  const response = await fetch(`${API_BASE_URL}/export-content-dto-list`)

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }

  const payload = await response.json()

  if (payload.status === false || payload.errorMessage) {
    throw new Error(payload.errorMessage || 'Failed to load content list')
  }

  return payload.responseObject ?? []
}

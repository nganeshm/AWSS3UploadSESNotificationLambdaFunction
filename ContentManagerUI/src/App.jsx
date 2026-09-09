import { useEffect, useMemo, useState } from 'react'
import { fetchContentList } from './api/contentApi'
import ContentGrid from './components/ContentGrid'
import GridSkeleton from './components/GridSkeleton'
import './App.css'

function App() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' })

  useEffect(() => {
    let ignore = false

    async function loadContent() {
      setLoading(true)
      setError(null)
      try {
        const list = await fetchContentList()
        if (!ignore) setItems(list)
      } catch (err) {
        if (!ignore) setError(err.message)
      } finally {
        if (!ignore) setLoading(false)
      }
    }

    loadContent()
    return () => {
      ignore = true
    }
  }, [])

  function handleSort(key) {
    setSortConfig((previous) => {
      if (previous.key !== key) return { key, direction: 'asc' }
      return { key, direction: previous.direction === 'asc' ? 'desc' : 'asc' }
    })
  }

  const visibleItems = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()

    const filtered = term
      ? items.filter((item) =>
          [item.contentName, item.contentType, item.description]
            .filter(Boolean)
            .some((field) => field.toLowerCase().includes(term)),
        )
      : items

    if (!sortConfig.key) return filtered

    return [...filtered].sort((a, b) => {
      const valueA = a[sortConfig.key] ?? ''
      const valueB = b[sortConfig.key] ?? ''
      if (valueA < valueB) return sortConfig.direction === 'asc' ? -1 : 1
      if (valueA > valueB) return sortConfig.direction === 'asc' ? 1 : -1
      return 0
    })
  }, [items, searchTerm, sortConfig])

  return (
    <div className="app-shell">
      <h1 className="app-heading">AWS S3 Content Manager</h1>

      <div className="grid-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="Search by name, type, or description..."
          value={searchTerm}
          onChange={(event) => setSearchTerm(event.target.value)}
          disabled={loading || Boolean(error)}
        />
        <span className="item-count">
          {loading
            ? 'Loading…'
            : `${visibleItems.length} item${visibleItems.length === 1 ? '' : 's'}`}
        </span>
      </div>

      <div className="grid-container">
        {loading && <GridSkeleton />}
        {!loading && error && <p className="error-message">⚠ {error}</p>}
        {!loading && !error && (
          <ContentGrid items={visibleItems} sortConfig={sortConfig} onSort={handleSort} />
        )}
      </div>
    </div>
  )
}

export default App

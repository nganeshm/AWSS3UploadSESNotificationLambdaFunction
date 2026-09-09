import PropTypes from 'prop-types'

const COLUMNS = [
  { key: 'index', label: '#', sortable: false },
  { key: 'contentName', label: 'Content Name', sortable: true },
  { key: 'contentType', label: 'Type', sortable: true },
  { key: 'fileSize', label: 'Size', sortable: true },
  { key: 'description', label: 'Description', sortable: true },
  { key: 'download', label: 'Download', sortable: false },
]

function formatFileSize(bytes) {
  if (bytes === null || bytes === undefined || Number.isNaN(bytes)) return '—'
  if (bytes === 0) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB']
  const exponent = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  const value = bytes / 1024 ** exponent
  return `${value.toFixed(exponent === 0 ? 0 : 1)} ${units[exponent]}`
}

function ContentGrid({ items, sortConfig, onSort }) {
  if (items.length === 0) {
    return <p className="empty-state">No content matches your search.</p>
  }

  return (
    <table className="content-grid">
      <thead>
        <tr>
          {COLUMNS.map((column) => (
            <th
              key={column.key}
              className={column.sortable ? 'sortable' : undefined}
              onClick={column.sortable ? () => onSort(column.key) : undefined}
            >
              {column.label}
              {sortConfig.key === column.key && (
                <span className="sort-indicator">{sortConfig.direction === 'asc' ? ' ▲' : ' ▼'}</span>
              )}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {items.map((item, index) => (
          <tr key={`${item.contentName}-${index}`} className="grid-row">
            <td className="row-index">{index + 1}</td>
            <td className="content-name">{item.contentName}</td>
            <td>
              <span className="type-badge">{item.contentType || 'FILE'}</span>
            </td>
            <td>{formatFileSize(item.fileSize)}</td>
            <td className="description-cell">{item.description || '—'}</td>
            <td>
              {item.contentDownloadUrl ? (
                <a
                  className="download-btn"
                  href={item.contentDownloadUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  title={`Open ${item.contentName}`}
                >
                  ⬇ Download
                </a>
              ) : (
                <span className="download-btn download-btn-disabled" title="Download link unavailable">
                  ⬇ Download
                </span>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}

ContentGrid.propTypes = {
  items: PropTypes.arrayOf(
    PropTypes.shape({
      contentName: PropTypes.string.isRequired,
      contentType: PropTypes.string,
      fileSize: PropTypes.number,
      description: PropTypes.string,
      contentDownloadUrl: PropTypes.string,
    }),
  ).isRequired,
  sortConfig: PropTypes.shape({
    key: PropTypes.string,
    direction: PropTypes.oneOf(['asc', 'desc']),
  }).isRequired,
  onSort: PropTypes.func.isRequired,
}

export default ContentGrid

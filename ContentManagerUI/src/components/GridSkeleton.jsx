const SKELETON_ROW_COUNT = 6

const SKELETON_COLUMN_WIDTHS = ['xs', 'lg', 'sm', 'sm', 'md', 'btn']

function GridSkeleton() {
  return (
    <table className="content-grid" aria-busy="true" aria-label="Loading content">
      <thead>
        <tr>
          <th>#</th>
          <th>Content Name</th>
          <th>Type</th>
          <th>Size</th>
          <th>Description</th>
          <th>Download</th>
        </tr>
      </thead>
      <tbody>
        {Array.from({ length: SKELETON_ROW_COUNT }).map((_, rowIndex) => (
          <tr key={rowIndex} className="skeleton-row">
            {SKELETON_COLUMN_WIDTHS.map((size, colIndex) => (
              <td key={colIndex}>
                <span
                  className={`skeleton-block skeleton-${size}`}
                  style={{ animationDelay: `${(rowIndex * SKELETON_COLUMN_WIDTHS.length + colIndex) * 40}ms` }}
                />
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  )
}

export default GridSkeleton

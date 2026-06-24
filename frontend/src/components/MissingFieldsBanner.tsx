import { formatLabel } from '../utils/formatLabel'
import './MissingFieldsBanner.css'

interface MissingFieldsBannerProps {
  missingFields: string[]
}

export function MissingFieldsBanner({ missingFields }: MissingFieldsBannerProps) {
  if (missingFields.length === 0) return null

  return (
    <div className="missing-fields-banner" role="alert">
      <span className="missing-fields-banner__icon" aria-hidden="true">
        ⚠
      </span>
      <div>
        <p className="missing-fields-banner__title">Missing fields ({missingFields.length})</p>
        <p className="missing-fields-banner__list">{missingFields.map(formatLabel).join(', ')}</p>
      </div>
    </div>
  )
}

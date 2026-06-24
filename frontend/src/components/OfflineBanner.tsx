import './OfflineBanner.css'

interface OfflineBannerProps {
  reason: string
}

export function OfflineBanner({ reason }: OfflineBannerProps) {
  return (
    <div className="offline-banner" role="status">
      <span aria-hidden="true">⚠</span>
      Running in offline mode — showing local fallback data ({reason}).
    </div>
  )
}

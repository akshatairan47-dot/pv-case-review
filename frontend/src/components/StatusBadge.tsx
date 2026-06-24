import type { FieldStatus } from '../types/case'
import './StatusBadge.css'

const STATUS_LABELS: Record<FieldStatus, string> = {
  NEW: 'New',
  OVERRIDDEN: 'Overridden',
  UNCHANGED: 'Unchanged',
  RETAINED: 'Retained',
}

interface StatusBadgeProps {
  status: FieldStatus
}

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={`status-badge status-badge--${status.toLowerCase()}`}>
      {STATUS_LABELS[status]}
    </span>
  )
}

import { useState } from 'react'
import type { CaseField } from '../types/case'
import { RaiseQueryModal } from './RaiseQueryModal'
import './FieldCard.css'

interface FieldCardProps {
  label: string
  field: CaseField
  caseId: string
  fieldPath: string
}

type ConfidenceLevel = 'low' | 'medium' | 'high'

function getConfidenceLevel(confidence: number): ConfidenceLevel {
  if (confidence < 0.8) return 'low'
  if (confidence <= 0.9) return 'medium'
  return 'high'
}

export function FieldCard({ label, field, caseId, fieldPath }: FieldCardProps) {
  const level = getConfidenceLevel(field.confidence)
  const isOverridden = field.status === 'OVERRIDDEN'
  const [isQueryModalOpen, setIsQueryModalOpen] = useState(false)

  return (
    <div className={`field-card field-card--${level}`}>
      <div className="field-card__header">
        <span className="field-card__label">{label}</span>
        <span className="field-card__confidence">{Math.round(field.confidence * 100)}%</span>
      </div>
      {isOverridden ? (
        <div className="field-card__values">
          <p className="field-card__value field-card__value--current">{field.value}</p>
          <div className="field-card__previous">
            <span className="field-card__previous-label">Previous</span>
            <span className="field-card__previous-value">{field.previous_value}</span>
          </div>
        </div>
      ) : (
        <p className="field-card__value">{field.value}</p>
      )}
      <p className="field-card__source">{field.source}</p>
      {isOverridden && (
        <button
          type="button"
          className="field-card__raise-query"
          onClick={() => setIsQueryModalOpen(true)}
        >
          Raise Query
        </button>
      )}
      {isQueryModalOpen && (
        <RaiseQueryModal
          caseId={caseId}
          fieldPath={fieldPath}
          onClose={() => setIsQueryModalOpen(false)}
        />
      )}
    </div>
  )
}

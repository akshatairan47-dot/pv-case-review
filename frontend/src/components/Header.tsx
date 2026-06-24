import { useState } from 'react'
import './Header.css'

type Classification = 'significant' | 'non-significant' | ''

const CLASSIFICATIONS: Classification[] = ['', 'significant', 'non-significant']

const CLASSIFICATION_LABELS: Record<Classification, string> = {
  '': '—',
  significant: 'Significant',
  'non-significant': 'Non-significant',
}

function toClassification(value: string): Classification {
  return value === 'significant' || value === 'non-significant' ? value : ''
}

interface HeaderProps {
  caseId: string
  initialClassification: string
}

export function Header({ caseId, initialClassification }: HeaderProps) {
  const [classification, setClassification] = useState<Classification>(
    toClassification(initialClassification),
  )

  return (
    <header className="case-header">
      <div className="case-header__case-id">
        <span className="case-header__label">Case ID</span>
        <span className="case-header__value">{caseId}</span>
      </div>
      <div className="case-header__classification">
        <label htmlFor="case-classification">Classification</label>
        <select
          id="case-classification"
          value={classification}
          onChange={(event) => setClassification(toClassification(event.target.value))}
        >
          {CLASSIFICATIONS.map((option) => (
            <option key={option} value={option}>
              {CLASSIFICATION_LABELS[option]}
            </option>
          ))}
        </select>
      </div>
    </header>
  )
}

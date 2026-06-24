import { FieldCard } from './FieldCard'
import { formatLabel } from '../utils/formatLabel'
import type { CaseSection } from '../types/case'
import './SectionGroup.css'

interface SectionGroupProps {
  name: string
  fields: CaseSection
  caseId: string
}

export function SectionGroup({ name, fields, caseId }: SectionGroupProps) {
  return (
    <section className="section-group">
      <h3 className="section-group__title">{formatLabel(name)}</h3>
      <div className="section-group__fields">
        {Object.entries(fields).map(([key, field]) => (
          <FieldCard
            key={key}
            label={formatLabel(key)}
            field={field}
            caseId={caseId}
            fieldPath={`${name}.${key}`}
          />
        ))}
      </div>
    </section>
  )
}

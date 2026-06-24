export type FieldStatus = 'UNCHANGED' | 'OVERRIDDEN' | 'NEW' | 'RETAINED'

export interface CaseField<T = string> {
  value: T
  confidence: number
  source: string
  status: FieldStatus
  /** Present when status is OVERRIDDEN. */
  previous_value?: T
}

export type CaseSection = Record<string, CaseField>

export interface CaseSections {
  patient: CaseSection
  suspect_drug: CaseSection
  adverse_event: CaseSection
  reporter: CaseSection
  [section: string]: CaseSection
}

export interface PvCase {
  case_id: string
  version: number
  case_classification: string
  extracted_at: string
  source_document: string
  sections: CaseSections
  missing_fields: string[]
}

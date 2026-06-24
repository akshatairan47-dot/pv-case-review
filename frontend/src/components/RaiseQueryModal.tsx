import { useState } from 'react'
import type { FormEvent } from 'react'
import { submitQuery } from '../api/queries'
import './RaiseQueryModal.css'

interface RaiseQueryModalProps {
  caseId: string
  fieldPath: string
  onClose: () => void
}

export function RaiseQueryModal({ caseId, fieldPath, onClose }: RaiseQueryModalProps) {
  const [question, setQuestion] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    submitQuery({ case_id: caseId, field_path: fieldPath, question })
      .then(() => onClose())
      .catch((err: unknown) => {
        setSubmitting(false)
        setError(err instanceof Error ? err.message : 'Failed to submit query')
      })
  }

  return (
    <div className="query-modal-overlay" onClick={onClose}>
      <div className="query-modal" onClick={(event) => event.stopPropagation()}>
        <h3 className="query-modal__title">Raise Query</h3>
        <p className="query-modal__field-path">{fieldPath}</p>
        <form onSubmit={handleSubmit}>
          <textarea
            className="query-modal__textarea"
            value={question}
            onChange={(event) => setQuestion(event.target.value)}
            placeholder="Type your question..."
            rows={4}
            autoFocus
          />
          {error && <p className="query-modal__error">{error}</p>}
          <div className="query-modal__actions">
            <button
              type="button"
              className="query-modal__cancel"
              onClick={onClose}
              disabled={submitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="query-modal__submit"
              disabled={submitting || !question.trim()}
            >
              {submitting ? 'Submitting...' : 'Submit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

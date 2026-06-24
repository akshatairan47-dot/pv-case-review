import { useEffect, useState } from 'react'
import { fetchCase } from '../api/cases'
import type { PvCase } from '../types/case'

interface UseCaseResult {
  data: PvCase | null
  loading: boolean
  error: Error | null
}

interface CaseRequestState {
  requestedFor: string | null
  data: PvCase | null
  error: Error | null
}

export function useCase(caseId: string): UseCaseResult {
  const [state, setState] = useState<CaseRequestState>({
    requestedFor: null,
    data: null,
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()

    fetchCase(caseId, controller.signal)
      .then((result) => {
        setState({ requestedFor: caseId, data: result, error: null })
      })
      .catch((err: unknown) => {
        if (controller.signal.aborted) return
        setState({
          requestedFor: caseId,
          data: null,
          error: err instanceof Error ? err : new Error(String(err)),
        })
      })

    return () => controller.abort()
  }, [caseId])

  return {
    data: state.data,
    error: state.error,
    loading: state.requestedFor !== caseId,
  }
}

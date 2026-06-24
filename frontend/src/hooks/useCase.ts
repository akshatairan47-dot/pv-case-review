import { useEffect, useState } from 'react'
import { fetchCase } from '../api/cases'
import fallbackCase from '../data/fallbackCase.json'
import type { PvCase } from '../types/case'

interface UseCaseResult {
  data: PvCase | null
  loading: boolean
  error: Error | null
  isOffline: boolean
}

interface CaseRequestState {
  requestedFor: string | null
  data: PvCase | null
  error: Error | null
  isOffline: boolean
}

export function useCase(caseId: string): UseCaseResult {
  const [state, setState] = useState<CaseRequestState>({
    requestedFor: null,
    data: null,
    error: null,
    isOffline: false,
  })

  useEffect(() => {
    const controller = new AbortController()

    fetchCase(caseId, controller.signal)
      .then((result) => {
        setState({ requestedFor: caseId, data: result, error: null, isOffline: false })
      })
      .catch((err: unknown) => {
        if (controller.signal.aborted) return
        setState({
          requestedFor: caseId,
          data: fallbackCase as PvCase,
          error: err instanceof Error ? err : new Error(String(err)),
          isOffline: true,
        })
      })

    return () => controller.abort()
  }, [caseId])

  return {
    data: state.data,
    error: state.error,
    isOffline: state.isOffline,
    loading: state.requestedFor !== caseId,
  }
}

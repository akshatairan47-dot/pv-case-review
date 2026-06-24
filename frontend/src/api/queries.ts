import type { RaiseQueryPayload } from '../types/query'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function submitQuery(payload: RaiseQueryPayload): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/queries`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error(`Failed to submit query: ${response.status} ${response.statusText}`)
  }
}

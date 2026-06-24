import type { PvCase } from '../types/case'

// Defaults to a relative path so requests go through the Vite dev proxy
// (see vite.config.ts) and avoid the backend's CORS rejection in local dev.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function fetchCase(caseId: string, signal?: AbortSignal): Promise<PvCase> {
  const response = await fetch(`${API_BASE_URL}/cases/${encodeURIComponent(caseId)}`, { signal })

  if (!response.ok) {
    throw new Error(`Failed to fetch case ${caseId}: ${response.status} ${response.statusText}`)
  }

  return response.json() as Promise<PvCase>
}

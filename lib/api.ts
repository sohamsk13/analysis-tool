const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api'

export interface ComparisonRules {
  checkMissingFields: boolean
  checkValueMismatches: boolean
  validateISINFormat: boolean
  validateDateFormat: boolean
  validateTrackerIdFormat: boolean
  checkDecisionConsistency: boolean
}

export interface ComparisonRequest {
  file1: {
    content: string
    fileName: string
  }
  file2: {
    content: string
    fileName: string
  }
  rules: ComparisonRules
}

export interface Discrepancy {
  type: string
  fieldName: string
  recordId: string
  values: Record<string, any>
  description: string
}

export interface ValidationError {
  recordId: string
  fieldName: string
  error: string
  validationType: string
}

export interface ComparisonResult {
  summary: Record<string, any>
  discrepancies: Discrepancy[]
  validationErrors: ValidationError[]
  rawData: Record<string, any>
}

export async function compareFiles(
  request: ComparisonRequest
): Promise<ComparisonResult> {
  console.log('[v0] Starting file comparison')
  try {
    const response = await fetch(`${API_BASE_URL}/replay/compare`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    })

    if (!response.ok) {
      const error = await response.text()
      console.error('[v0] API error response:', error)
      throw new Error(`API error: ${response.statusText}`)
    }

    const data = await response.json()
    console.log('[v0] Comparison result received:', data)
    return data
  } catch (error) {
    console.error('[v0] Comparison failed:', error)
    throw error
  }
}

export async function healthCheck(): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/replay/health`)
    return response.ok
  } catch {
    return false
  }
}

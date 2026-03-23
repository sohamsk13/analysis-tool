'use client'

import { useState, useEffect } from 'react'
import { FileUploader } from '@/components/FileUploader'
import { RuleManager } from '@/components/RuleManager'
import { ResultsViewer } from '@/components/ResultsViewer'
import { Button } from '@/components/ui/button'
import { compareFiles, healthCheck } from '@/lib/api'

export default function Home() {
  const [file1, setFile1] = useState<{ content: string; fileName: string } | null>(null)
  const [file2, setFile2] = useState<{ content: string; fileName: string } | null>(null)
  const [rules, setRules] = useState({
    checkMissingFields: true,
    checkValueMismatches: true,
    validateISINFormat: true,
    validateDateFormat: true,
    validateTrackerIdFormat: true,
    checkDecisionConsistency: true,
  })
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [backendHealthy, setBackendHealthy] = useState<boolean | null>(null)

  useEffect(() => {
    const checkBackend = async () => {
      const healthy = await healthCheck()
      setBackendHealthy(healthy)
    }
    checkBackend()
  }, [])

  const handleCompare = async () => {
    if (!file1 || !file2) {
      setError('Please upload both files')
      return
    }

    if (!backendHealthy) {
      setError(
        'Backend server is not available. Ensure the Spring Boot server is running on http://localhost:8081'
      )
      return
    }

    setLoading(true)
    setError(null)

    try {
      const result = await compareFiles({
        file1: { content: file1.content, fileName: file1.fileName },
        file2: { content: file2.content, fileName: file2.fileName },
        rules,
      })
      setResult(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-background text-foreground dark:bg-slate-950 dark:text-slate-50">
      <header className="border-b border-border bg-card p-6">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-between gap-4">
            <div className="flex-1">
              <h1 className="text-3xl font-bold tracking-tight">Replay Analysis Tool</h1>
              <p className="text-muted-foreground mt-2">
                Compare and analyze YAML replay files with advanced validation and consistency checks
              </p>
            </div>
            <div className="text-right">
              {backendHealthy === null ? (
                <p className="text-sm text-muted-foreground">Checking backend...</p>
              ) : backendHealthy ? (
                <div className="flex items-center gap-2 text-sm text-green-600">
                  <div className="w-2 h-2 bg-green-600 rounded-full" />
                  Backend connected
                </div>
              ) : (
                <div className="flex items-center gap-2 text-sm text-destructive">
                  <div className="w-2 h-2 bg-destructive rounded-full" />
                  Backend unavailable
                </div>
              )}
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column: Upload */}
          <div className="lg:col-span-2 space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <FileUploader
                title="First File"
                onUpload={(content, fileName) =>
                  setFile1({ content, fileName })
                }
              />
              <FileUploader
                title="Second File"
                onUpload={(content, fileName) =>
                  setFile2({ content, fileName })
                }
              />
            </div>

            {error && (
              <div className="bg-destructive/10 text-destructive border border-destructive/20 rounded-lg p-4">
                {error}
              </div>
            )}

            <Button
              onClick={handleCompare}
              disabled={!file1 || !file2 || loading}
              size="lg"
              className="w-full"
            >
              {loading ? 'Comparing...' : 'Compare Files'}
            </Button>
          </div>

          {/* Right Column: Rules */}
          <div>
            <RuleManager rules={rules} onRulesChange={setRules} />
          </div>
        </div>

        {/* Results */}
        {result && <ResultsViewer result={result} />}
      </main>
    </div>
  )
}

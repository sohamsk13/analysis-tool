'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Button } from '@/components/ui/button'
import { Download } from 'lucide-react'

interface Discrepancy {
  type: string
  fieldName: string
  recordId: string
  values: Record<string, any>
  description: string
}

interface ValidationError {
  recordId: string
  fieldName: string
  error: string
  validationType: string
}

interface Result {
  summary: Record<string, any>
  discrepancies: Discrepancy[]
  validationErrors: ValidationError[]
}

interface ResultsViewerProps {
  result: Result
}

export function ResultsViewer({ result }: ResultsViewerProps) {
  const [activeTab, setActiveTab] = useState('summary')

  const handleExportJSON = () => {
    const dataStr = JSON.stringify(result, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'replay-analysis-result.json'
    link.click()
  }

  return (
    <Card className="mt-8">
      <CardHeader className="flex flex-row items-center justify-between gap-4">
        <CardTitle>Analysis Results</CardTitle>
        <Button
          variant="outline"
          size="sm"
          onClick={handleExportJSON}
          className="gap-2"
        >
          <Download className="w-4 h-4" />
          Export JSON
        </Button>
      </CardHeader>
      <CardContent>
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="summary">Summary</TabsTrigger>
            <TabsTrigger value="discrepancies">
              Discrepancies ({result.discrepancies.length})
            </TabsTrigger>
            <TabsTrigger value="validation">
              Validation ({result.validationErrors.length})
            </TabsTrigger>
            <TabsTrigger value="raw">Raw JSON</TabsTrigger>
          </TabsList>

          <TabsContent value="summary" className="space-y-4">
            <SummaryTab summary={result.summary} />
          </TabsContent>

          <TabsContent value="discrepancies" className="space-y-4">
            <DiscrepanciesTab discrepancies={result.discrepancies} />
          </TabsContent>

          <TabsContent value="validation" className="space-y-4">
            <ValidationTab errors={result.validationErrors} />
          </TabsContent>

          <TabsContent value="raw" className="space-y-4">
            <pre className="bg-muted p-4 rounded-lg overflow-auto text-sm font-mono">
              {JSON.stringify(result, null, 2)}
            </pre>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  )
}

function SummaryTab({ summary }: { summary: Record<string, any> }) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
      {Object.entries(summary).map(([key, value]) => (
        <div key={key} className="bg-muted p-4 rounded-lg">
          <p className="text-xs text-muted-foreground uppercase tracking-wide">
            {key.replace(/_/g, ' ')}
          </p>
          <p className="text-lg font-semibold text-foreground mt-1">
            {String(value)}
          </p>
        </div>
      ))}
    </div>
  )
}

function DiscrepanciesTab({ discrepancies }: { discrepancies: Discrepancy[] }) {
  if (discrepancies.length === 0) {
    return (
      <div className="text-center py-8 text-muted-foreground">
        No discrepancies found
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {discrepancies.map((disc, idx) => (
        <div key={idx} className="bg-muted/50 border border-border p-4 rounded-lg">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1">
              <p className="font-semibold text-foreground">{disc.fieldName}</p>
              <p className="text-sm text-muted-foreground">{disc.description}</p>
              <p className="text-xs text-muted-foreground mt-1">
                Type: {disc.type} | Record: {disc.recordId}
              </p>
            </div>
            <span className="bg-primary/10 text-primary px-2 py-1 rounded text-xs font-medium">
              {disc.type}
            </span>
          </div>
          {Object.keys(disc.values).length > 0 && (
            <pre className="bg-background p-2 rounded text-xs font-mono overflow-auto mt-3">
              {JSON.stringify(disc.values, null, 2)}
            </pre>
          )}
        </div>
      ))}
    </div>
  )
}

function ValidationTab({ errors }: { errors: ValidationError[] }) {
  if (errors.length === 0) {
    return (
      <div className="text-center py-8 text-muted-foreground">
        No validation errors found
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {errors.map((err, idx) => (
        <div key={idx} className="bg-destructive/10 border border-destructive/20 p-4 rounded-lg">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1">
              <p className="font-semibold text-foreground">{err.fieldName}</p>
              <p className="text-sm text-muted-foreground">{err.error}</p>
              <p className="text-xs text-muted-foreground mt-1">
                Record: {err.recordId} | Type: {err.validationType}
              </p>
            </div>
            <span className="bg-destructive/20 text-destructive px-2 py-1 rounded text-xs font-medium">
              {err.validationType}
            </span>
          </div>
        </div>
      ))}
    </div>
  )
}

'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Switch } from '@/components/ui/switch'
import { Label } from '@/components/ui/label'

interface Rules {
  checkMissingFields: boolean
  checkValueMismatches: boolean
  validateISINFormat: boolean
  validateDateFormat: boolean
  validateTrackerIdFormat: boolean
  checkDecisionConsistency: boolean
}

interface RuleManagerProps {
  rules: Rules
  onRulesChange: (rules: Rules) => void
}

export function RuleManager({ rules, onRulesChange }: RuleManagerProps) {
  const toggleRule = (key: keyof Rules) => {
    onRulesChange({
      ...rules,
      [key]: !rules[key],
    })
  }

  const ruleDescriptions = {
    checkMissingFields: 'Check for missing fields across files',
    checkValueMismatches: 'Detect value differences in matching records',
    validateISINFormat: 'Validate ISIN format (XX + 9-digit alphanumeric + check digit)',
    validateDateFormat: 'Validate date format in date fields',
    validateTrackerIdFormat: 'Validate tracker ID format',
    checkDecisionConsistency: 'Check consistency of boolean fields across records',
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Comparison Rules</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {Object.entries(rules).map(([key, value]) => (
          <div key={key} className="flex items-start justify-between gap-3">
            <div className="flex-1">
              <Label
                htmlFor={key}
                className="text-sm font-medium cursor-pointer"
              >
                {ruleDescriptions[key as keyof Rules]}
              </Label>
            </div>
            <Switch
              id={key}
              checked={value}
              onCheckedChange={() => toggleRule(key as keyof Rules)}
            />
          </div>
        ))}
      </CardContent>
    </Card>
  )
}

package com.replay.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonRules {
    private boolean checkMissingFields = true;
    private boolean checkValueMismatches = true;
    private boolean validateISINFormat = true;
    private boolean validateDateFormat = true;
    private boolean validateTrackerIdFormat = true;
    private boolean checkDecisionConsistency = true;
}

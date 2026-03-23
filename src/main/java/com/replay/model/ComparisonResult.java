package com.replay.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ComparisonResult {
    private Map<String, Object> summary;
    private List<Discrepancy> discrepancies;
    private List<ValidationError> validationErrors;
    private Map<String, Object> rawData;

    public ComparisonResult() {
        this.summary = new HashMap<>();
        this.discrepancies = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
        this.rawData = new HashMap<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Discrepancy {
        private String type; // "missing_field", "value_mismatch", "inconsistency"
        private String fieldName;
        private String recordId;
        private Map<String, Object> values; // file1_value, file2_value, etc.
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String recordId;
        private String fieldName;
        private String error;
        private String validationType; // "isin_format", "date_format", "tracker_id_format"
    }
}

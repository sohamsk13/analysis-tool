package com.replay.service;

import com.replay.model.ComparisonResult;
import com.replay.model.ComparisonRules;
import com.replay.model.ReplayFile;
import com.replay.model.ReplayRecord;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ComparisonService {
    private final YamlParserService parserService;
    private final ValidationService validationService;

    public ComparisonService(YamlParserService parserService, ValidationService validationService) {
        this.parserService = parserService;
        this.validationService = validationService;
    }

    public ComparisonResult compareFiles(ReplayFile file1, ReplayFile file2, ComparisonRules rules) {
        ComparisonResult result = new ComparisonResult();

        // Validate both files
        if (rules.isValidateISINFormat() || rules.isValidateDateFormat() || rules.isValidateTrackerIdFormat()) {
            validationService.validateFile(file1, result);
            validationService.validateFile(file2, result);
        }

        // Check missing fields
        if (rules.isCheckMissingFields()) {
            checkMissingFields(file1, file2, result);
        }

        // Check value mismatches
        if (rules.isCheckValueMismatches()) {
            checkValueMismatches(file1, file2, result);
        }

        // Check decision consistency
        if (rules.isCheckDecisionConsistency()) {
            checkDecisionConsistency(file1, file2, result);
        }

        // Build summary
        buildSummary(file1, file2, result);

        // Store raw data
        result.getRawData().put("file1", file1);
        result.getRawData().put("file2", file2);

        return result;
    }

    private void checkMissingFields(ReplayFile file1, ReplayFile file2, ComparisonResult result) {
        Set<String> fields1 = getAllFieldNames(file1);
        Set<String> fields2 = getAllFieldNames(file2);

        // Fields in file1 but not in file2
        for (String field : fields1) {
            if (!fields2.contains(field)) {
                result.getDiscrepancies().add(
                    new ComparisonResult.Discrepancy(
                        "missing_field",
                        field,
                        "all_records",
                        Map.of("missing_in", "file2"),
                        "Field '" + field + "' present in file1 but missing in file2"
                    )
                );
            }
        }

        // Fields in file2 but not in file1
        for (String field : fields2) {
            if (!fields1.contains(field)) {
                result.getDiscrepancies().add(
                    new ComparisonResult.Discrepancy(
                        "missing_field",
                        field,
                        "all_records",
                        Map.of("missing_in", "file1"),
                        "Field '" + field + "' present in file2 but missing in file1"
                    )
                );
            }
        }
    }

    private void checkValueMismatches(ReplayFile file1, ReplayFile file2, ComparisonResult result) {
        Map<String, ReplayRecord> records1 = indexRecordsById(file1);
        Map<String, ReplayRecord> records2 = indexRecordsById(file2);

        // Compare matching records
        for (String recordId : records1.keySet()) {
            if (records2.containsKey(recordId)) {
                ReplayRecord rec1 = records1.get(recordId);
                ReplayRecord rec2 = records2.get(recordId);

                for (String field : rec1.getFields().keySet()) {
                    Object value1 = rec1.getField(field);
                    Object value2 = rec2.getField(field);

                    if (!Objects.equals(value1, value2)) {
                        result.getDiscrepancies().add(
                            new ComparisonResult.Discrepancy(
                                "value_mismatch",
                                field,
                                recordId,
                                Map.of("file1_value", value1, "file2_value", value2),
                                "Value mismatch for field '" + field + "' in record " + recordId
                            )
                        );
                    }
                }
            }
        }
    }

    private void checkDecisionConsistency(ReplayFile file1, ReplayFile file2, ComparisonResult result) {
        checkFileConsistency(file1, "file1", result);
        checkFileConsistency(file2, "file2", result);
    }

    private void checkFileConsistency(ReplayFile file, String fileName, ComparisonResult result) {
        // Check consistency of boolean-like fields across records
        Set<String> booleanFields = new HashSet<>();
        
        for (ReplayRecord record : file.getRecords()) {
            for (String field : record.getFields().keySet()) {
                String value = record.getField(field).toString().toLowerCase();
                if (value.equals("true") || value.equals("false")) {
                    booleanFields.add(field);
                }
            }
        }

        // For each boolean field, check if all records have consistent values
        for (String field : booleanFields) {
            Set<String> values = new HashSet<>();
            for (ReplayRecord record : file.getRecords()) {
                Object value = record.getField(field);
                if (value != null) {
                    values.add(value.toString().toLowerCase());
                }
            }

            if (values.size() > 1) {
                result.getDiscrepancies().add(
                    new ComparisonResult.Discrepancy(
                        "inconsistency",
                        field,
                        "multiple_records",
                        Map.of("file", fileName, "inconsistent_values", values),
                        "Inconsistent values for field '" + field + "' across records in " + fileName
                    )
                );
            }
        }
    }

    private void buildSummary(ReplayFile file1, ReplayFile file2, ComparisonResult result) {
        Map<String, Object> summary = result.getSummary();
        summary.put("file1_name", file1.getFileName());
        summary.put("file2_name", file2.getFileName());
        summary.put("file1_record_count", file1.getRecords().size());
        summary.put("file2_record_count", file2.getRecords().size());
        summary.put("total_discrepancies", result.getDiscrepancies().size());
        summary.put("total_validation_errors", result.getValidationErrors().size());
        summary.put("file1_has_parsing_errors", file1.hasParsingErrors());
        summary.put("file2_has_parsing_errors", file2.hasParsingErrors());
    }

    private Set<String> getAllFieldNames(ReplayFile file) {
        Set<String> fieldNames = new HashSet<>();
        for (ReplayRecord record : file.getRecords()) {
            fieldNames.addAll(record.getFields().keySet());
        }
        return fieldNames;
    }

    private Map<String, ReplayRecord> indexRecordsById(ReplayFile file) {
        Map<String, ReplayRecord> map = new HashMap<>();
        for (ReplayRecord record : file.getRecords()) {
            map.put(record.getRecordId(), record);
        }
        return map;
    }
}

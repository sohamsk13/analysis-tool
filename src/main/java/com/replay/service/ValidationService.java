package com.replay.service;

import com.replay.model.ComparisonResult;
import com.replay.model.ReplayFile;
import com.replay.model.ReplayRecord;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    // ISIN format: 2-letter country code + 9-digit alphanumeric + 1 check digit
    private static final Pattern ISIN_PATTERN = Pattern.compile("^[A-Z]{2}[A-Z0-9]{9}[0-9]$");
    
    // Tracker ID format: typically alphanumeric with underscores
    private static final Pattern TRACKER_ID_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    public void validateFile(ReplayFile file, ComparisonResult result) {
        for (ReplayRecord record : file.getRecords()) {
            validateRecord(record, result);
        }
    }

    private void validateRecord(ReplayRecord record, ComparisonResult result) {
        for (String fieldName : record.getFields().keySet()) {
            Object value = record.getField(fieldName);
            
            if (value == null) {
                continue;
            }

            String stringValue = value.toString().trim();

            // Validate ISIN fields
            if (fieldName.toLowerCase().contains("isin") && !stringValue.isEmpty()) {
                if (!isValidISIN(stringValue)) {
                    result.getValidationErrors().add(
                        new ComparisonResult.ValidationError(
                            record.getRecordId(),
                            fieldName,
                            stringValue,
                            "isin_format"
                        )
                    );
                }
            }

            // Validate date fields
            if (fieldName.toLowerCase().contains("date") && !stringValue.isEmpty()) {
                if (!isValidDate(stringValue)) {
                    result.getValidationErrors().add(
                        new ComparisonResult.ValidationError(
                            record.getRecordId(),
                            fieldName,
                            stringValue,
                            "date_format"
                        )
                    );
                }
            }

            // Validate tracker ID fields
            if (fieldName.toLowerCase().contains("tracker") && !stringValue.isEmpty()) {
                if (!isValidTrackerId(stringValue)) {
                    result.getValidationErrors().add(
                        new ComparisonResult.ValidationError(
                            record.getRecordId(),
                            fieldName,
                            stringValue,
                            "tracker_id_format"
                        )
                    );
                }
            }
        }
    }

    private boolean isValidISIN(String isin) {
        return ISIN_PATTERN.matcher(isin).matches();
    }

    private boolean isValidDate(String date) {
        try {
            // Try common date formats
            LocalDate.parse(date); // ISO format
            return true;
        } catch (DateTimeParseException e) {
            // Try other formats
            try {
                LocalDate.parse(date.replace("/", "-")); // DD-MM-YYYY or similar
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private boolean isValidTrackerId(String trackerId) {
        return TRACKER_ID_PATTERN.matcher(trackerId).matches();
    }
}

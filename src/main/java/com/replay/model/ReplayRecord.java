package com.replay.model;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayRecord {
    private String recordId;
    private Map<String, Object> fields;
    private Map<String, String> errors; // parsing errors for this record

    public ReplayRecord(String recordId) {
        this.recordId = recordId;
        this.fields = new HashMap<>();
        this.errors = new HashMap<>();
    }

    public Object getField(String fieldName) {
        return fields.get(fieldName);
    }

    public void setField(String fieldName, Object value) {
        fields.put(fieldName, value);
    }

    public void addError(String fieldName, String error) {
        errors.put(fieldName, error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

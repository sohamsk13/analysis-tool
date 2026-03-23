package com.replay.service;

import com.replay.model.ReplayFile;
import com.replay.model.ReplayRecord;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class YamlParserService {
    private final Yaml yaml = new Yaml();

    public ReplayFile parseYamlContent(String content, String fileName) {
        ReplayFile replayFile = new ReplayFile(fileName);

        try {
            // Split by lines and attempt lenient parsing
            String[] lines = content.split("\n");
            ReplayRecord currentRecord = null;
            int recordCounter = 0;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Check if this is a new record (starts with - or is a key: value pair at root level)
                if (line.startsWith("-")) {
                    if (currentRecord != null) {
                        replayFile.addRecord(currentRecord);
                    }
                    currentRecord = new ReplayRecord("record_" + (++recordCounter));
                    line = line.substring(1).trim();
                }

                // Parse key-value pair
                if (currentRecord != null && !line.isEmpty()) {
                    parseKeyValuePair(line, currentRecord, i);
                }
            }

            // Add the last record
            if (currentRecord != null) {
                replayFile.addRecord(currentRecord);
            }

        } catch (Exception e) {
            replayFile.addParsingError("Fatal parsing error: " + e.getMessage());
        }

        return replayFile;
    }

    private void parseKeyValuePair(String line, ReplayRecord record, int lineNumber) {
        try {
            // Handle format: "key: value" or just "value" (missing key)
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                String key = parts[0].trim();
                String value = parts.length > 1 ? parts[1].trim() : "";

                // Clean up key (remove quotes)
                key = cleanString(key);
                value = cleanString(value);

                if (!key.isEmpty()) {
                    record.setField(key, value);
                }
            } else {
                // Missing key, just value
                String value = cleanString(line);
                if (!value.isEmpty()) {
                    record.addError("missing_key_line_" + lineNumber, "Line has value but no key: " + line);
                }
            }
        } catch (Exception e) {
            record.addError("parse_error_line_" + lineNumber, "Error parsing line: " + e.getMessage());
        }
    }

    private String cleanString(String str) {
        if (str == null) return "";
        str = str.trim();
        // Remove quotes
        if ((str.startsWith("\"") && str.endsWith("\"")) || 
            (str.startsWith("'") && str.endsWith("'"))) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }

    public List<String> extractFieldNames(ReplayFile file) {
        Set<String> fieldNames = new HashSet<>();
        for (ReplayRecord record : file.getRecords()) {
            fieldNames.addAll(record.getFields().keySet());
        }
        return new ArrayList<>(fieldNames);
    }
}

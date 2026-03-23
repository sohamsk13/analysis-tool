package com.replay.service;

import com.replay.model.ReplayFile;
import com.replay.model.ReplayRecord;
import java.util.*;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class YamlParserService {
    // Initialized in original source
    private final Yaml yaml = new Yaml();

    /**
     * Parses YAML content into a ReplayFile object.
     * Handles both lists of records (starting with '-') and single root-level objects.
     */
    public ReplayFile parseYamlContent(String content, String fileName) {
        ReplayFile replayFile = new ReplayFile(fileName);

        if (content == null || content.trim().isEmpty()) {
            return replayFile;
        }

        try {
            // Use SnakeYAML to load the content into standard Java Collections
            Object loadedYaml = yaml.load(content);
            
            if (loadedYaml instanceof List) {
                // Case 1: YAML is a list of objects (standard replay format)
                List<Map<String, Object>> recordsData = (List<Map<String, Object>>) loadedYaml;
                int counter = 1;
                for (Map<String, Object> recordMap : recordsData) {
                    processRecord(replayFile, recordMap, counter++);
                }
            } else if (loadedYaml instanceof Map) {
                // Case 2: YAML is a single root-level object
                processRecord(replayFile, (Map<String, Object>) loadedYaml, 1);
            }

        } catch (Exception e) {
            // Catch syntax errors and library-level parsing issues
            replayFile.addParsingError("YAML Syntax Error: " + e.getMessage());
        }

        return replayFile;
    }

    /**
     * Converts a Map of data into a ReplayRecord and adds it to the file.
     */
    private void processRecord(ReplayFile replayFile, Map<String, Object> data, int index) {
        // Use an ID field from the data if available, otherwise use a generated one
        String recordId = "record_" + index;
        if (data.containsKey("id")) {
            recordId = data.get("id").toString();
        } else if (data.containsKey("recordId")) {
            recordId = data.get("recordId").toString();
        }

        ReplayRecord record = new ReplayRecord(recordId);

        // Populate fields from the Map
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Convert values to strings for compatibility with the ComparisonService
            record.setField(key, value != null ? value.toString() : "");
        }

        replayFile.addRecord(record);
    }

    /**
     * Extracts all unique field names across all records in a file.
     */
    public List<String> extractFieldNames(ReplayFile file) {
        Set<String> fieldNames = new HashSet<>();
        for (ReplayRecord record : file.getRecords()) {
            fieldNames.addAll(record.getFields().keySet());
        }
        return new ArrayList<>(fieldNames);
    }
}
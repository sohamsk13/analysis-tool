package com.replay.controller;

import com.replay.model.ComparisonResult;
import com.replay.model.ComparisonRules;
import com.replay.model.ReplayFile;
import com.replay.service.ComparisonService;
import com.replay.service.YamlParserService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/replay")
@CrossOrigin(origins = "http://localhost:3000")
public class ReplayController {
    private final YamlParserService parserService;
    private final ComparisonService comparisonService;

    public ReplayController(YamlParserService parserService, ComparisonService comparisonService) {
        this.parserService = parserService;
        this.comparisonService = comparisonService;
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parseFile(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String fileName = request.get("fileName");

            if (content == null || content.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Content is required"));
            }

            ReplayFile replayFile = parserService.parseYamlContent(content, fileName);
            return ResponseEntity.ok(replayFile);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

   // src/main/java/com/replay/controller/ReplayController.java

@PostMapping("/compare")
public ResponseEntity<?> compareFiles(@RequestBody Map<String, Object> request) {
    try {
        // Safe extraction of file data
        Map<String, String> file1Data = (Map<String, String>) request.get("file1");
        Map<String, String> file2Data = (Map<String, String>) request.get("file2");

        if (file1Data == null || file2Data == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Both files are required"));
        }

        // Robust rule extraction to avoid ClassCastException
        ComparisonRules rules = new ComparisonRules();
        Object rulesObj = request.get("rules");
        if (rulesObj instanceof Map) {
            Map<String, Object> rulesMap = (Map<String, Object>) rulesObj;
            rules.setCheckMissingFields(convertObjectToBoolean(rulesMap.get("checkMissingFields")));
            rules.setCheckValueMismatches(convertObjectToBoolean(rulesMap.get("checkValueMismatches")));
            rules.setValidateISINFormat(convertObjectToBoolean(rulesMap.get("validateISINFormat")));
            rules.setValidateDateFormat(convertObjectToBoolean(rulesMap.get("validateDateFormat")));
            rules.setValidateTrackerIdFormat(convertObjectToBoolean(rulesMap.get("validateTrackerIdFormat")));
            rules.setCheckDecisionConsistency(convertObjectToBoolean(rulesMap.get("checkDecisionConsistency")));
        }

        // Parse files with content checks
        ReplayFile file1 = parserService.parseYamlContent(
            file1Data.getOrDefault("content", ""), 
            file1Data.getOrDefault("fileName", "file1.yaml")
        );
        ReplayFile file2 = parserService.parseYamlContent(
            file2Data.getOrDefault("content", ""), 
            file2Data.getOrDefault("fileName", "file2.yaml")
        );

        ComparisonResult result = comparisonService.compareFiles(file1, file2, rules);
        return ResponseEntity.ok(result);

    } catch (Exception e) {
        e.printStackTrace(); // This is crucial to see the error in your Spring Boot console
        return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
    }
}

// Helper method to safely handle boolean conversion
private boolean convertObjectToBoolean(Object obj) {
    if (obj instanceof Boolean) return (Boolean) obj;
    if (obj instanceof String) return Boolean.parseBoolean((String) obj);
    return true; // Default to true if missing
}


    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }
}

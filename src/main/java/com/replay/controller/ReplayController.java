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

    @PostMapping("/compare")
    public ResponseEntity<?> compareFiles(@RequestBody Map<String, Object> request) {
        try {
            Map<String, String> file1Data = (Map<String, String>) request.get("file1");
            Map<String, String> file2Data = (Map<String, String>) request.get("file2");
            Map<String, Boolean> rulesData = (Map<String, Boolean>) request.get("rules");

            if (file1Data == null || file2Data == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Both files are required"));
            }

            // Parse files
            ReplayFile file1 = parserService.parseYamlContent(
                file1Data.get("content"),
                file1Data.getOrDefault("fileName", "file1.yaml")
            );

            ReplayFile file2 = parserService.parseYamlContent(
                file2Data.get("content"),
                file2Data.getOrDefault("fileName", "file2.yaml")
            );

            // Build rules from request
            ComparisonRules rules = new ComparisonRules();
            if (rulesData != null) {
                rules.setCheckMissingFields(rulesData.getOrDefault("checkMissingFields", true));
                rules.setCheckValueMismatches(rulesData.getOrDefault("checkValueMismatches", true));
                rules.setValidateISINFormat(rulesData.getOrDefault("validateISINFormat", true));
                rules.setValidateDateFormat(rulesData.getOrDefault("validateDateFormat", true));
                rules.setValidateTrackerIdFormat(rulesData.getOrDefault("validateTrackerIdFormat", true));
                rules.setCheckDecisionConsistency(rulesData.getOrDefault("checkDecisionConsistency", true));
            }

            // Perform comparison
            ComparisonResult result = comparisonService.compareFiles(file1, file2, rules);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }
}

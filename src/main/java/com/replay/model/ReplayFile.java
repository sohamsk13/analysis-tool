package com.replay.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayFile {
    private String fileName;
    private List<ReplayRecord> records;
    private List<String> parsingErrors;

    public ReplayFile(String fileName) {
        this.fileName = fileName;
        this.records = new ArrayList<>();
        this.parsingErrors = new ArrayList<>();
    }

    public void addRecord(ReplayRecord record) {
        records.add(record);
    }

    public void addParsingError(String error) {
        parsingErrors.add(error);
    }

    public boolean hasParsingErrors() {
        return !parsingErrors.isEmpty();
    }
}

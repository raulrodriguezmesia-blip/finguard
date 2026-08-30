package com.eros.agent.analysis;

import java.time.Instant;
import java.util.List;

public class AnalysisResult {
    private String status;
    private List<Finding> findings;
    private String risk_level;
    private Instant timestamp;

    public AnalysisResult(String status, List<Finding> findings, String risk_level) {
        this.status = status;
        this.findings = findings;
        this.risk_level = risk_level;
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Finding> getFindings() { return findings; }
    public void setFindings(List<Finding> findings) { this.findings = findings; }
    public String getRisk_level() { return risk_level; }
    public void setRisk_level(String risk_level) { this.risk_level = risk_level; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

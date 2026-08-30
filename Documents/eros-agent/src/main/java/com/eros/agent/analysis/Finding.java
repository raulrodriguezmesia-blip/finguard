package com.eros.agent.analysis;

public class Finding {
    private String phase;
    private String severity;
    private String description;
    private int line;
    private String fix;

    public Finding(String phase, String severity, String description, int line, String fix) {
        this.phase = phase;
        this.severity = severity;
        this.description = description;
        this.line = line;
        this.fix = fix;
    }

    // Getters and setters
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }
    public String getFix() { return fix; }
    public void setFix(String fix) { this.fix = fix; }
}

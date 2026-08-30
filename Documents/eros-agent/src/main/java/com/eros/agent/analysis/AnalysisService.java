package com.eros.agent.analysis;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AnalysisService {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER)\\s+.*\\{.*\\}.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HARDCODED_SECRET_PATTERN = Pattern.compile(
        "(password|secret|api_key|token)\\s*=\\s*[\"'][^\"']+[\"']",
        Pattern.CASE_INSENSITIVE
    );

    public AnalysisResult analyze(String code, String language) {
        List<Finding> findings = new ArrayList<>();

        // Phase 1: Syntax Check
        if (code == null || code.trim().isEmpty()) {
            findings.add(new Finding("syntax_check", "ERROR", "Empty code provided", 0, null));
            return new AnalysisResult("completed", findings, "ERROR");
        }

        // Phase 2: Quality Review
        if (code.length() > 1000) {
            findings.add(new Finding("quality_review", "WARNING", "Code exceeds recommended length", 0, "Consider refactoring into smaller functions"));
        }

        // Phase 3: Security Audit
        if (SQL_INJECTION_PATTERN.matcher(code).matches()) {
            findings.add(new Finding("security_audit", "CRITICAL", "SQL Injection vulnerability detected", 1, "Use parameterized queries"));
        }

        if (HARDCODED_SECRET_PATTERN.matcher(code).find()) {
            findings.add(new Finding("security_audit", "HIGH", "Hardcoded secret detected", 0, "Use environment variables or a secrets manager"));
        }

        // Phase 4: Performance Analysis
        if (code.contains("for") && code.contains("for")) {
            findings.add(new Finding("performance_analysis", "MEDIUM", "Nested loops detected - potential O(n²) complexity", 0, "Consider using hash maps for O(1) lookups"));
        }

        // Phase 5: Refactoring Suggestions
        if (!findings.isEmpty()) {
            findings.add(new Finding("refactoring", "INFO", "Consider applying security best practices", 0, "Review OWASP guidelines"));
        }

        String riskLevel = calculateRiskLevel(findings);
        return new AnalysisResult("completed", findings, riskLevel);
    }

    private String calculateRiskLevel(List<Finding> findings) {
        if (findings.stream().anyMatch(f -> f.severity.equals("CRITICAL"))) return "CRITICAL";
        if (findings.stream().anyMatch(f -> f.severity.equals("HIGH"))) return "HIGH";
        if (findings.stream().anyMatch(f -> f.severity.equals("MEDIUM"))) return "MEDIUM";
        if (findings.stream().anyMatch(f -> f.severity.equals("WARNING"))) return "WARNING";
        return "LOW";
    }
}

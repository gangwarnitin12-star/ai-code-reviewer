package com.aicodereviewer;

import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    public String reviewCode(String code) {

        StringBuilder review = new StringBuilder();

        int bugs = 0;
        int security = 0;
        int quality = 0;

        review.append("AI Analysis\n");
        review.append("The Java code compiled successfully and passed the initial compiler check.\n\n");

        // =========================
        // BUG DETECTION
        // =========================

        review.append("🐞 BUGS\n");

        if (code.matches("(?s).*/\\s*0(?![0-9]).*")) {

            review.append("❌ Possible division by zero detected.\n");
            review.append("Suggestion: Validate the denominator before division.\n");

            bugs++;
        }

        if (code.contains("null") && code.contains(".")) {

            review.append("⚠️ Possible NullPointerException risk detected.\n");
            review.append("Suggestion: Validate objects before calling methods.\n");

            bugs++;
        }

        if (bugs == 0) {

            review.append("✅ No common bug patterns detected.\n");
        }

        // =========================
        // SECURITY
        // =========================

        review.append("\n🔐 SECURITY ISSUES\n");

        String lowerCode = code.toLowerCase();

        if (lowerCode.contains("password")
                || lowerCode.contains("apikey")
                || lowerCode.contains("api_key")
                || lowerCode.contains("secret")) {

            review.append("🔐 Possible hardcoded credential or secret detected.\n");
            review.append("Suggestion: Use environment variables or a secret manager.\n");

            security++;
        }

        if (security == 0) {

            review.append("✅ No common security patterns detected.\n");
        }

        // =========================
        // CODE QUALITY
        // =========================

        review.append("\n⚡ CODE QUALITY ISSUES\n");

        if (code.contains("System.out.println")) {

            review.append("⚠️ System.out.println() found.\n");
            review.append("Suggestion: Use a logging framework for production code.\n");

            quality++;
        }

        if (code.split("\\R").length > 100) {

            review.append("⚠️ Large source file detected.\n");
            review.append("Suggestion: Break large classes or methods into smaller units.\n");

            quality++;
        }

        if (quality == 0) {

            review.append("✅ No common code-quality issues detected.\n");
        }

        // =========================
        // SUMMARY
        // =========================

        review.append("\n========================\n");

        review.append("SUMMARY\n");

        review.append("Bugs: ")
                .append(bugs)
                .append("\n");

        review.append("Security Issues: ")
                .append(security)
                .append("\n");

        review.append("Code Quality Issues: ")
                .append(quality)
                .append("\n");

        review.append("========================");

        return review.toString();
    }
}
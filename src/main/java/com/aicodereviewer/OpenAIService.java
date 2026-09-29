package com.aicodereviewer;

import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    public String reviewCode(String code) {

        StringBuilder review = new StringBuilder();

        review.append("===== AI CODE REVIEW =====\n\n");

        int issues = 0;
        System.out.println("Demo Pull Request Code Review");
        // 1. Division by zero
        if (code.contains("/0") || code.contains("/ 0")) {
            review.append("❌ BUG: Possible division by zero detected.\n");
            review.append("   Suggestion: Check the denominator before division.\n\n");
            issues++;
        }

        // 2. Null handling
        if (code.contains("null") && code.contains(".")) {
            review.append("⚠️ WARNING: Possible NullPointerException risk.\n");
            review.append("   Suggestion: Validate objects before using them.\n\n");
            issues++;
        }

        // 3. System.out.println
        if (code.contains("System.out.println")) {
            review.append("⚠️ CODE QUALITY: System.out.println() found.\n");
            review.append("   Suggestion: Use a proper logging framework in production.\n\n");
            issues++;
        }

        // 4. Hardcoded credentials
        String lowerCode = code.toLowerCase();

        if (lowerCode.contains("password")
                || lowerCode.contains("apikey")
                || lowerCode.contains("api_key")) {

            review.append("🔐 SECURITY: Possible hardcoded credential detected.\n");
            review.append("   Suggestion: Use environment variables or a secret manager.\n\n");
            issues++;
        }

        // 5. Large code
        if (code.split("\n").length > 100) {
            review.append("⚠️ CODE QUALITY: File appears very large.\n");
            review.append("   Suggestion: Break the code into smaller methods/classes.\n\n");
            issues++;
        }

        // No issues
        if (issues == 0) {
            review.append("✅ No common issues detected.\n");
            review.append("Code looks good based on the current checks.\n");
        }

        review.append("\nTotal issues found: ")
                .append(issues);

        return review.toString();
        
    }
}
package com.aicodereviewer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class codeReviewController {

    private final OpenAIService openAIService;
    private final GitHubService gitHubService;
    private final JavaCompilerService compilerService;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public codeReviewController(
            OpenAIService openAIService,
            GitHubService gitHubService,
            JavaCompilerService compilerService) {

        this.openAIService = openAIService;
        this.gitHubService = gitHubService;
        this.compilerService = compilerService;
    }

    @GetMapping("/")
    public ResponseEntity<Resource> home() {

        ClassPathResource resource =
                new ClassPathResource("static/index.html");

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from AI Code Reviewer!";
    }

    // =========================
    // JAVA CODE REVIEW
    // =========================

    @PostMapping("/review")
    public ResponseEntity<Map<String, Object>> review(
            @RequestBody String code) {

        if (code == null || code.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "compiled", false,
                            "compilerMessage",
                            "Please enter Java code."
                    )
            );
        }

        // STEP 1: COMPILE JAVA CODE

        JavaCompilerService.CompileResult result =
                compilerService.compile(code);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("compiled", result.success());

        response.put(
                "compilerMessage",
                result.message()
        );

        response.put(
                "compilerErrors",
                result.errors()
        );

        // Compilation failed → STOP

        if (!result.success()) {

            response.put("aiAnalysis", null);
            response.put("review", null);

            return ResponseEntity.ok(response);
        }

        // Compilation successful → AI Analysis

        response.put(
                "aiAnalysis",
                "Compilation passed. Running code analysis for bugs, security and quality."
        );

        response.put(
                "review",
                openAIService.reviewCode(code)
        );

        return ResponseEntity.ok(response);
    }

    // =========================
    // GITHUB WEBHOOK
    // =========================

    @PostMapping("/webhook")
    public String webhook(
            @RequestBody String payload) {

        try {

            JsonNode root =
                    objectMapper.readTree(payload);

            String action =
                    root.path("action").asText();

            String repository =
                    root.path("repository")
                            .path("full_name")
                            .asText();

            int prNumber =
                    root.path("number").asInt();

            System.out.println(
                    "===== GITHUB WEBHOOK ====="
            );

            System.out.println(
                    "Action: " + action
            );

            System.out.println(
                    "Repository: " + repository
            );

            System.out.println(
                    "PR Number: " + prNumber
            );

            if (repository.isEmpty()
                    || prNumber == 0) {

                return "Invalid GitHub PR webhook payload";
            }

            String files =
                    gitHubService.getPullRequestFiles(
                            repository,
                            prNumber
                    );

            JsonNode filesJson =
                    objectMapper.readTree(files);

            StringBuilder combinedCode =
                    new StringBuilder();

            for (JsonNode file : filesJson) {

                String filename =
                        file.path("filename")
                                .asText();

                String patch =
                        file.path("patch")
                                .asText("");

                if (!patch.isEmpty()) {

                    combinedCode
                            .append("\n===== FILE: ")
                            .append(filename)
                            .append(" =====\n")
                            .append(patch)
                            .append("\n");
                }
            }

            // Compile GitHub code first

            JavaCompilerService.CompileResult compile =
                    compilerService.compile(
                            combinedCode.toString()
                    );

            String review;

            if (!compile.success()) {

                review =
                        "❌ Compilation Failed\n\n"
                                + String.join(
                                        "\n",
                                        compile.errors()
                                );

            } else {

                review =
                        openAIService.reviewCode(
                                combinedCode.toString()
                        );
            }

            gitHubService.addPullRequestComment(
                    repository,
                    prNumber,
                    review
            );

            return "Webhook processed successfully";

        } catch (Exception e) {

            e.printStackTrace();

            return "Webhook processing failed: "
                    + e.getMessage();
        }
    }
}
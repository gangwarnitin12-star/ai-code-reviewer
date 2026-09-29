package com.aicodereviewer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class codeReviewController {

    private final OpenAIService openAIService;
    private final GitHubService gitHubService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public codeReviewController(
            OpenAIService openAIService,
            GitHubService gitHubService) {

        this.openAIService = openAIService;
        this.gitHubService = gitHubService;
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

    @PostMapping("/webhook")
    public String webhook(@RequestBody String payload) {

        try {
            JsonNode root = objectMapper.readTree(payload);

            String action = root.path("action").asText();
            String repository = root.path("repository")
                    .path("full_name").asText();
            int prNumber = root.path("number").asInt();

            System.out.println("===== GITHUB WEBHOOK =====");
            System.out.println("Action: " + action);
            System.out.println("Repository: " + repository);
            System.out.println("PR Number: " + prNumber);

            if (repository.isEmpty() || prNumber == 0) {
                return "Invalid GitHub PR webhook payload";
            }

            String files = gitHubService.getPullRequestFiles(
                    repository,
                    prNumber
            );

            JsonNode filesJson = objectMapper.readTree(files);

            StringBuilder combinedCode = new StringBuilder();

            for (JsonNode file : filesJson) {

                String filename = file.path("filename").asText();
                String patch = file.path("patch").asText("");

                System.out.println("Reviewing: " + filename);

                if (!patch.isEmpty()) {
                    combinedCode.append("\n===== FILE: ")
                            .append(filename)
                            .append(" =====\n");

                    combinedCode.append(patch);
                    combinedCode.append("\n");
                }
            }

            String review = openAIService.reviewCode(
                    combinedCode.toString()
            );

            gitHubService.addPullRequestComment(
                    repository,
                    prNumber,
                    review
            );

            System.out.println("===== CODE REVIEW =====");
            System.out.println(review);

            return "Webhook processed successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Webhook processing failed: " + e.getMessage();
        }
    }

    @PostMapping("/review")
    public String review(@RequestBody String code) {
        return openAIService.reviewCode(code);
    }
}
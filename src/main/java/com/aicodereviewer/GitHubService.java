package com.aicodereviewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GitHubService {

    @Value("${github.token:test-token}")
    private String githubToken;

    private final HttpClient client = HttpClient.newHttpClient();

    // Get changed files from a Pull Request
    public String getPullRequestFiles(
            String repository,
            int pullRequestNumber) throws Exception {

        String url = "https://api.github.com/repos/"
                + repository
                + "/pulls/"
                + pullRequestNumber
                + "/files";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new RuntimeException(
                    "GitHub API error: HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }

        return response.body();
    }

    // Add review as a comment on the Pull Request
    public void addPullRequestComment(
            String repository,
            int pullRequestNumber,
            String review) throws Exception {

        String url = "https://api.github.com/repos/"
                + repository
                + "/issues/"
                + pullRequestNumber
                + "/comments";

        String safeReview = review
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");

        String json = "{\"body\":\"" + safeReview + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Content-Type", "application/json")
                .POST(
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new RuntimeException(
                    "GitHub comment error: HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }

        System.out.println("✅ Review comment posted to GitHub!");
    }
}
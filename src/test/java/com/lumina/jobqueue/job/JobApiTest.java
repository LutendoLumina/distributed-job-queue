package com.lumina.jobqueue.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JobApiTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    JobRepository jobRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void cleanDatabase() {
        jobRepository.deleteAll();
    }

    @Test
    void createsJob() throws Exception {
        var response = post("""
                {"type":"SEND_EMAIL","payload":{"to":"a@b.com"}}
                """);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"status\":\"PENDING\"");
    }

    @Test
    void rejectsTooManyAttempts() throws Exception {
        var response = post("""
                {"type":"SEND_EMAIL","payload":{},"maxAttempts":50}
                """);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("maxAttempts");
    }

    @Test
    void rejectsEmptyType() throws Exception {
        var response = post("""
                {"type":"","payload":{}}
                """);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("type");
    }

    @Test
    void returns404ForMissingJob() throws Exception {
        var response = get("/jobs/00000000-0000-0000-0000-000000000000");

        assertThat(response.statusCode()).isEqualTo(404);
    }

    // --- helpers ---

    private HttpResponse<String> post(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/jobs"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
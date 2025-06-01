package io.sanity.sdk;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SanityClient {

    private final String projectId;
    private final String dataset;
    private final String token;
    private final HttpClient client;


    public SanityClient(String projectId, String dataset, String token, HttpClient client) {
        this.projectId = projectId;
        this.dataset = dataset;
        this.token = token;
        this.client = client;
    }

    public String query(String groqQuery) throws Exception {
        String encodeGroqQuery = URLEncoder.encode(groqQuery, StandardCharsets.UTF_8);
        String url = String.format("https://%s.api.sanity.io/v1/data/query/%s?query=%s", projectId, dataset, encodeGroqQuery);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}

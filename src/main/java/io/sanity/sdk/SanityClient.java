package io.sanity.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


public class SanityClient {
    private static final Logger logger = LoggerFactory.getLogger(SanityClient.class);

    private final String projectId;
    private final String dataset;
    private final String apiToken;
    private final HttpClient httpClient;


    public SanityClient(String projectId, String dataset, String token, HttpClient client) {
        this.projectId = projectId;
        this.dataset = dataset;
        this.apiToken = token;
        this.httpClient = client;
    }

    /**
     * Execute a GROQ query against Sanity and return raw JSON
     *
     * @param query The GROQ query to execute
     * @return The raw JSON response
     * @throws SanityFetchException if the request fails
     */
    public String query(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format("https://%s.api.sanity.io/v1/data/query/%s?query=%s", projectId, dataset, encodedQuery);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json");

            // Add authorization header if token is available
            if (apiToken != null && !apiToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + apiToken);
            }

            HttpRequest request = requestBuilder.GET().build();


            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Sanity API error - Status: {}, Body: {}", response.statusCode(), response.body());
                throw new SanityFetchException(
                        String.format("Sanity API returned status %d: %s", response.statusCode(), response.body())
                );
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new SanityFetchException("Failed to fetch data from Sanity", e);
        }
    }
}

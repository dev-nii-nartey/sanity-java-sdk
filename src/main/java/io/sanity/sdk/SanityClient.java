package io.sanity.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;


public class SanityClient {
    private static final Logger logger = LoggerFactory.getLogger(SanityClient.class);

    private final String projectId;
    private final String dataset;
    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();



    public SanityClient(String projectId, String dataset, String token) {
        this.projectId = projectId;
        this.dataset = dataset;
        this.apiToken = token;
        this.httpClient = HttpClient.newHttpClient();
    }


    /**
     * Constructs a base HTTP request builder with predefined configuration for interacting with the Sanity API.
     *
     * @param endpoint The specific API endpoint to be appended to the Sanity base URL.
     * @return An instance of {@code HttpRequest.Builder} pre-configured with the URI, headers, and other settings.
     */
    private HttpRequest.Builder baseRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://%s.api.sanity.io/v1/%s/%s", projectId, endpoint, dataset)))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json");
    }


 /**
  * Sends a query to the Sanity API and retrieves the query results as a string.
  * Handles encoding of the query and authentication via an API token if provided.
  * Throws an exception if the API returns a non-200 status code or if an I/O or interruption error occurs.
  *
  * @param query The GROQ query to be sent to the Sanity API.
  * @return The response body from the Sanity API as a JSON string.
  * @throws SanityFetchException If an error occurs while sending the request or processing the response.
  */
 String query(String query) {
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
            //result
            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new SanityFetchException("Failed to fetch data from Sanity", e);
        }
    }





    /**
     * Sends a set of mutations to the Sanity API and retrieves the API response as a string.
     * This method constructs a payload containing the mutations, serializes it into JSON,
     * and sends it as a POST request to the Sanity mutation endpoint.
     *
     * @param mutations A list of maps representing the mutations to be sent. Each map contains mutation data.
     * @return The response body from the Sanity API as a JSON string.
     * @throws IOException If an I/O error occurs during the request.
     * @throws InterruptedException If the operation is interrupted while waiting for the response.
     */
    // Shared mutation sender
    private String sendMutation(List<Map<String, Object>> mutations) throws IOException, InterruptedException {
        Map<String, Object> payload = Map.of("mutations", mutations);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = baseRequest("data/mutate")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}

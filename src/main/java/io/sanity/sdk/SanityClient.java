package io.sanity.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
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
     * @throws IOException          If an I/O error occurs while sending the request.
     * @throws InterruptedException If the operation is interrupted while waiting for the response.     */
    public String query(String query) throws IOException, InterruptedException {
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

        return response.body();

    }


    /**
     * Creates a new document in the Sanity dataset by sending a mutation request to the Sanity API.
     * This method constructs a mutation to create the specified document and sends it to the API.
     *
     * @param document A map representing the document to be created. The map should contain key-value pairs
     *                 that define the document's structure and content.
     * @return The response body from the Sanity API as a JSON string, which typically includes details about
     * the created document, such as its ID and revision.
     * @throws IOException          If an I/O error occurs while sending the request.
     * @throws InterruptedException If the operation is interrupted while waiting for the response.
     */
    public String createDocument(Map<String, Object> document) throws IOException, InterruptedException {
        Map<String, Object> mutation = new HashMap<>();
        mutation.put("create", document);

        return sendMutation(List.of(mutation));
    }


    /**
     * Updates an existing document in the Sanity dataset by applying the specified updates.
     * This method constructs a mutation to patch the document with the provided updates
     * and sends it to the Sanity API.
     *
     * @param documentId The ID of the document to be updated.
     * @param updates A map representing the updates to be applied to the document.
     *                The keys in the map correspond to the fields to be updated,
     *                and the values represent the new values for those fields.
     * @return The response body from the Sanity API as a JSON string, which typically includes details
     *         about the updated document, such as its ID and revision.
     * @throws IOException If an I/O error occurs while sending the request.
     * @throws InterruptedException If the operation is interrupted while waiting for the response.
     */
    public String updateDocument(String documentId, Map<String, Object> updates) throws IOException, InterruptedException {
        Map<String, Object> patch = new HashMap<>();
        patch.put("id", documentId);
        patch.put("set", updates);

        Map<String, Object> mutation = new HashMap<>();
        mutation.put("patch", patch);

        return sendMutation(List.of(mutation));
    }



    /**
     * Deletes a document from the Sanity dataset by sending a delete mutation request to the Sanity API.
     * This method constructs a mutation to delete the specified document and sends it to the API.
     *
     * @param documentId The ID of the document to be deleted.
     * @return The response body from the Sanity API as a JSON string, which typically includes details
     *         about the deletion operation.
     * @throws IOException If an I/O error occurs while sending the request.
     * @throws InterruptedException If the operation is interrupted while waiting for the response.
     */
    public String deleteDocument(String documentId) throws IOException, InterruptedException {
        Map<String, Object> mutation = new HashMap<>();
        mutation.put("delete", Map.of("id", documentId));

        return sendMutation(List.of(mutation));
    }


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


    public String uploadImage(File imageFile) throws Exception {
        return uploadAsset(imageFile, "images");
    }

    public String uploadFile(File file) throws Exception {
        return uploadAsset(file, "files");
    }

    /**
     * Uploads an asset (e.g., image or file) to the Sanity API.
     * Constructs an HTTP request to upload the specified file to the appropriate asset endpoint based on the asset type.
     *
     * @param file The file to be uploaded. It must exist and be accessible.
     * @param assetType The type of asset to upload (e.g., "images" or "files").
     * @return The response body as a string, typically containing details about the uploaded asset.
     * @throws Exception If an error occurs during the file upload process, such as network issues or invalid inputs.
     */
    private String uploadAsset(File file, String assetType) throws Exception {
        String endpoint = String.format("https://%s.api.sanity.io/v1/assets/%s/%s",
                projectId, assetType, dataset);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", Files.probeContentType(file.toPath()))
                .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public Map<String, Object> buildImageReference(String assetId) {
        return Map.of(
                "_type", "image",
                "asset", Map.of(
                        "_type", "reference",
                        "_ref", assetId
                )
        );
    }


    public String extractAssetIdFromResponse(String jsonResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        return root.path("document").path("_id").asText();
    }


}

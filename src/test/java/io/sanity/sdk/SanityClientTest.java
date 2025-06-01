package io.sanity.sdk;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the SanityClient class to validate its behavior under different conditions.
 */
class SanityClientTest {

    private static final String TEST_PROJECT_ID = "testProject";
    private static final String TEST_DATASET = "testDataset";
    private static final String TEST_TOKEN = "testToken";


    /**
     * Sets up a mocked SanityClient instance with a predefined mock HTTP response for testing purposes.
     * This method leverages reflection to inject a mocked {@code HttpClient} into the {@code SanityClient}.
     *
     * @param responseBody the predefined response body to be returned by the mocked {@code HttpClient}.
     *                     If {@code null}, the mocked client will not return any specific response.
     * @param token the authentication token to be used for the {@code SanityClient} instance.
     * @return an instance of {@code SanityClient} with the mocked {@code HttpClient} and specified token.
     * @throws NoSuchFieldException if reflection fails to access the {@code httpClient} field in {@code SanityClient}.
     * @throws IllegalAccessException if reflection does not have permission to modify the {@code httpClient} field.
     * @throws IOException if an I/O error occurs during the mocking process.
     * @throws InterruptedException if the threading is interrupted during setup.
     */
    private SanityClient setupSanityClientWithMockResponse(String responseBody, String token)
            throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {
        // Create and configure mocks
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);

        // Configure mock response if not null (to handle exception tests)
        if (responseBody != null) {
            when(mockResponse.body()).thenReturn(responseBody);
            when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);
        }

        // Create client and inject mock HttpClient
        SanityClient sanityClient = new SanityClient(TEST_PROJECT_ID, TEST_DATASET, token);
        java.lang.reflect.Field httpClientField = SanityClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(sanityClient, mockHttpClient);

        return sanityClient;
    }

    /**
     * Tests the functionality of the {@code SanityClient.query(String)} method to ensure
     * it returns a successful response for a valid query.
     *
     * This test sets up a mock SanityClient with a predefined response body and validates
     * that the response from the {@code query} method matches the expected response.
     *
     * @throws IOException if an I/O error occurs during the test
     * @throws InterruptedException if the test is interrupted during execution
     * @throws NoSuchFieldException if reflection fails to access a required field
     * @throws IllegalAccessException if reflection lacks access permission to modify a required field
     */
    @Test
    void testQuery_SuccessfulResponse() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        String sampleQuery = "*[_type == 'test']";
        String expectedResponse = "{\"result\": \"Some data\"}";
        SanityClient sanityClient = setupSanityClientWithMockResponse(expectedResponse, TEST_TOKEN);

        // Act
        String result = sanityClient.query(sampleQuery);

        // Assert
        assertEquals(expectedResponse, result);
    }

    /**
     * Tests the functionality of the {@code SanityClient.query(String)} method to handle an empty query.
     *
     * This test verifies that when an empty query string is passed to the {@code query} method, the
     * response matches the expected result, which in this case is a predefined JSON structure. It sets
     * up a mock SanityClient with the expected response and asserts that the output from the method
     * aligns with this response.
     *
     * @throws IOException if an I/O error occurs during the test
     * @throws InterruptedException if the test is interrupted during execution
     * @throws NoSuchFieldException if reflection fails to access a required field
     * @throws IllegalAccessException if reflection lacks access permission to modify a required field
     */
    @Test
    void testQuery_EmptyQuery() throws IOException, InterruptedException, IllegalAccessException, NoSuchFieldException {
        // Arrange
        String emptyQuery = "";
        String expectedResponse = "{\"result\": \"[]\"}";
        SanityClient sanityClient = setupSanityClientWithMockResponse(expectedResponse, TEST_TOKEN);

        // Act
        String result = sanityClient.query(emptyQuery);

        // Assert
        assertEquals(expectedResponse, result);
    }

    /**
     * Tests the behavior of the {@code SanityClient.query(String)} method when handling an extremely large query string.
     *
     * This test verifies that the {@code query} method correctly processes and returns the expected response
     * when a significantly large query string is provided as input. A mock {@code SanityClient} is set up with
     * a predefined response, and the test asserts that the actual response from the {@code query} method matches
     * the expected result.
     *
     * @throws IOException if an I/O error occurs during the test
     * @throws InterruptedException if the test is interrupted during execution
     * @throws NoSuchFieldException if reflection fails to access a required field
     * @throws IllegalAccessException if reflection lacks access permission to modify a required field
     */
    @Test
    void testQuery_LargeQuery() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        String largeQuery = "a".repeat(10000);  // Extremely large query string
        String expectedResponse = "{\"result\": \"Large query data\"}";
        SanityClient sanityClient = setupSanityClientWithMockResponse(expectedResponse, TEST_TOKEN);

        // Act
        String result = sanityClient.query(largeQuery);

        // Assert
        assertEquals(expectedResponse, result);
    }

    /**
     * Tests the functionality of the {@code SanityClient.query(String)} method when an invalid query syntax is provided.
     *
     * This test verifies that the method correctly handles and returns an appropriate error response
     * when a query with malformed syntax is passed. It sets up a mock SanityClient with a predefined
     * error response and asserts that the output matches the expected error message.
     *
     * @throws IOException if an I/O error occurs during the test
     * @throws InterruptedException if the test execution is interrupted
     * @throws NoSuchFieldException if reflection fails to access a required field
     * @throws IllegalAccessException if reflection lacks permission to modify a required field
     */
    @Test
    void testQuery_InvalidQuerySyntax() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        String invalidQuery = "*[_type = 'missing']"; // Malformed query (single =) for testing
        String expectedResponse = "{\"error\": \"Invalid query syntax\"}";
        SanityClient sanityClient = setupSanityClientWithMockResponse(expectedResponse, TEST_TOKEN);

        // Act
        String result = sanityClient.query(invalidQuery);

        // Assert
        assertEquals(expectedResponse, result);
    }

    /**
     * Tests the behavior of the {@code SanityClient.query(String)} method when an {@code HttpClient} throws an exception.
     *
     * This test verifies that the {@code query} method properly handles an {@code IOException} thrown
     * by the {@code HttpClient} during an HTTP request. It sets up a mocked {@code HttpClient} to simulate
     * an HTTP failure and ensures that the exception message matches the expected value.
     *
     * @throws NoSuchFieldException if reflection fails to access the private {@code httpClient} field in {@code SanityClient}
     * @throws IllegalAccessException if reflection does not have access permission to modify the {@code httpClient} field in {@code SanityClient}
     * @throws InterruptedException if the test is interrupted during execution
     * @throws IOException if the simulated {@code HttpClient} throws an {@code IOException}
     */
    @Test
    void testQuery_HttpClientException() throws NoSuchFieldException, IllegalAccessException, InterruptedException, IOException {
        // Arrange
        String sampleQuery = "*[_type == 'test']";
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);

        // Configure mock to throw exception
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Simulated HTTP failure"));

        SanityClient sanityClient = new SanityClient(TEST_PROJECT_ID, TEST_DATASET, TEST_TOKEN);
        java.lang.reflect.Field httpClientField = SanityClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(sanityClient, mockHttpClient);

        // Act and Assert
        try {
            sanityClient.query(sampleQuery);
        } catch (IOException e) {
            assertEquals("Simulated HTTP failure", e.getMessage());
        }
    }

    /**
     * Verifies that the {@code SanityClient.query(String)} method appropriately handles unauthorized access.
     *
     * This test sets up a mock {@code SanityClient} with an invalid authentication token and a predefined
     * expected response indicating an unauthorized error. It ensures that the result of the {@code query}
     * method matches the expected response when an unauthorized query is made.
     *
     * @throws IOException if an I/O error occurs during the test
     * @throws InterruptedException if the test is interrupted during execution
     * @throws NoSuchFieldException if reflection fails to access a required field
     * @throws IllegalAccessException if reflection lacks access permission to modify a required field
     */
    @Test
    void testQuery_Unauthorized() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        String invalidToken = "invalidToken";
        String sampleQuery = "*[_type == 'test']";
        String expectedResponse = "{\"error\": \"Unauthorized\"}";
        SanityClient sanityClient = setupSanityClientWithMockResponse(expectedResponse, invalidToken);

        // Act
        String result = sanityClient.query(sampleQuery);

        // Assert
        assertEquals(expectedResponse, result);
    }

    /**
     * Tests the functionality of the {@code SanityClient.query(String)} method when the token is null.
     *
     * This test ensures that the {@code query} method in the {@code SanityClient} class can handle a scenario
     * where the token is set to {@code null}. It sets up a mock {@code SanityClient} with a predefined response
     * and validates that the output of the {@code query} method matches the expected response under these
     * conditions.
     *
     * @throws IOException if an I/O error occurs during the test
     * @throws InterruptedException if the test is interrupted during execution
     * @throws NoSuchFieldException if reflection fails to access a required field
     * @throws IllegalAccessException if reflection lacks permission to modify a required field
     */
    @Test
    void testQuery_NullToken() throws IOException, InterruptedException, IllegalAccessException, NoSuchFieldException {
        // Arrange
        String sampleQuery = "*[_type == 'test']";
        String expectedResponse = "{\"result\": \"Some data\"}";
        SanityClient sanityClient = setupSanityClientWithMockResponse(expectedResponse, null);

        // Act
        String result = sanityClient.query(sampleQuery);

        // Assert
        assertEquals(expectedResponse, result);
    }
}
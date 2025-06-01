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

class SanityClientTest {

    @Test
    void testQuery_SuccessfulResponse() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        String projectId = "testProject";
        String dataset = "testDataset";
        String token = "testToken";
        String sampleQuery = "*[_type == 'test']";

        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{\"result\": \"Some data\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        SanityClient sanityClient = new SanityClient(projectId, dataset, token);
        // Reflection is used here to inject the mocked HttpClient into the SanityClient
        java.lang.reflect.Field httpClientField = SanityClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(sanityClient, mockHttpClient);

        // Act
        String result = sanityClient.query(sampleQuery);

        // Assert
        assertEquals("{\"result\": \"Some data\"}", result);
    }

    @Test
    void testQuery_EmptyQuery() throws IOException, InterruptedException, IllegalAccessException, NoSuchFieldException {
        // Arrange
        String projectId = "testProject";
        String dataset = "testDataset";
        String token = "testToken";
        String emptyQuery = "";

        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{\"result\": \"[]\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        SanityClient sanityClient = new SanityClient(projectId, dataset, token);
        // Reflection to inject mocked HttpClient into the SanityClient
        java.lang.reflect.Field httpClientField = SanityClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(sanityClient, mockHttpClient);

        // Act
        String result = sanityClient.query(emptyQuery);

        // Assert
        assertEquals("{\"result\": \"[]\"}", result);
    }

    @Test
    void testQuery_Unauthorized() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        String projectId = "testProject";
        String dataset = "testDataset";
        String invalidToken = "invalidToken";
        String sampleQuery = "*[_type == 'test']";

        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{\"error\": \"Unauthorized\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        SanityClient sanityClient = new SanityClient(projectId, dataset, invalidToken);
        // Reflection to inject mocked HttpClient into the SanityClient
        java.lang.reflect.Field httpClientField = SanityClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(sanityClient, mockHttpClient);

        // Act
        String result = sanityClient.query(sampleQuery);

        // Assert
        assertEquals("{\"error\": \"Unauthorized\"}", result);
    }

    @Test
    void testQuery_NullToken() throws IOException, InterruptedException, IllegalAccessException, NoSuchFieldException {
        // Arrange
        String projectId = "testProject";
        String dataset = "testDataset";
        String token = null;
        String sampleQuery = "*[_type == 'test']";

        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{\"result\": \"Some data\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        SanityClient sanityClient = new SanityClient(projectId, dataset, token);
        // Reflection to inject mocked HttpClient into the SanityClient
        java.lang.reflect.Field httpClientField = SanityClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(sanityClient, mockHttpClient);

        // Act
        String result = sanityClient.query(sampleQuery);

        // Assert
        assertEquals("{\"result\": \"Some data\"}", result);
    }

}
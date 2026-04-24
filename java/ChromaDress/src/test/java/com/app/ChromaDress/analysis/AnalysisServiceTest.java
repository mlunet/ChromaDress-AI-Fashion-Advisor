package com.app.ChromaDress.analysis;

import com.app.ChromaDress.core.exception.PythonAnalysisException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisServiceTest {

    private MockWebServer mockWebServer;
    private AnalysisService analysisService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        HttpClient httpClient = HttpClient.create()
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10)));

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        analysisService = new AnalysisService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("The call to microservice should return color and category.")
    void shouldReturnAnalysisDataSuccessfully() throws InterruptedException {
        String mockResponse = """
                {
                    "color": "#ff0000",
                    "category": "dress"
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse));

        MockMultipartFile mockImage = new MockMultipartFile(
                "file",
                "mock_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake_image".getBytes());

        AnalysisDTO result = analysisService.analyzeImage(mockImage);

        assertNotNull(result);
        assertEquals("#ff0000", result.color());
        assertEquals("dress", result.category());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertTrue(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE));
    }

    @Test
    @DisplayName("Should raise a PythonAnalysisException if server sends error.")
    void shouldRaisePythonAnalysisExceptionIfServerError() {
        String mockResponse = """
                {
                    "status": "error",
                    "type": "AIModelError",
                    "message": "Classification model failed."
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse));

        MockMultipartFile mockImage = new MockMultipartFile(
                "file",
                "mock_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake_image".getBytes());

        PythonAnalysisException exception = assertThrows(
                PythonAnalysisException.class,
                () -> analysisService.analyzeImage(mockImage));

        assertEquals("Classification model failed.", exception.getMessage());
        assertEquals("AIModelError", exception.getType());
        assertEquals(503, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should raise a PythonAnalysisException if server sends empty response.")
    void shouldRaisePythonAnalysisExceptionIfServerResponseEmpty() {
        String mockResponse = "";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse));

        MockMultipartFile mockImage = new MockMultipartFile(
                "file",
                "mock_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake_image".getBytes());

        PythonAnalysisException exception = assertThrows(
                PythonAnalysisException.class,
                () -> analysisService.analyzeImage(mockImage));

        assertEquals("The server did not respond correctly. Please try again in a few moments.", exception.getMessage());
        assertEquals("ServiceUnavailable", exception.getType());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should raise a ReadTimeoutException if server takes too long.")
    void shouldRaiseReadTimeoutExceptionIfDelay() {
        String mockResponse = """
                {
                    "color": "#ff0000",
                    "category": "dress"
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse)
                .setBodyDelay(11, TimeUnit.SECONDS));

        MockMultipartFile mockImage = new MockMultipartFile(
                "file",
                "mock_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake_image".getBytes());

        Exception exception = assertThrows(
                Exception.class,
                () -> analysisService.analyzeImage(mockImage));

        boolean isTimeout = exception instanceof io.netty.handler.timeout.ReadTimeoutException ||
                exception.getCause() instanceof io.netty.handler.timeout.ReadTimeoutException;

        assertTrue(isTimeout, "Exception should be a ReadTimeoutException");
    }
}

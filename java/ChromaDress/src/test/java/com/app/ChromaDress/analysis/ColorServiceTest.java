package com.app.ChromaDress.analysis;

import com.app.ChromaDress.core.exception.PythonAnalysisException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.awt.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ColorServiceTest {

    private MockWebServer mockWebServer;
    private ColorService colorService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        colorService = new ColorService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("The call to microservice should return classic palette")
    void shouldReturnClassicPaletteSuccessfully() throws InterruptedException {
        String mockResponse = """
                {
                    "suggestions": [["#f0f000"], ["#00ff00"]]
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse));

        ClassicColorDTO result = colorService.getClassicPalette("#ff0000");

        assertNotNull(result);
        assertEquals("[[#f0f000], [#00ff00]]", result.suggestions().toString());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/classic-palette?hex=%23ff0000", recordedRequest.getPath());
    }

    @Test
    @DisplayName("Should raise a PythonAnalysisException if server sends error.")
    void shouldRaisePythonAnalysisExceptionIfServerError() throws InterruptedException {
        String mockResponse = """
                {
                    "status": "error",
                    "type": "AppError",
                    "message": "Invalid HEX color format."
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse));

        PythonAnalysisException exception = assertThrows(
                PythonAnalysisException.class,
                () -> colorService.getClassicPalette("invalidHex"));

        assertEquals("Invalid HEX color format.", exception.getMessage());
        assertEquals("AppError", exception.getType());
        assertEquals(400, exception.getStatusCode());
    }

}

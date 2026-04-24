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

class WadaSanzoServiceTest {

    private MockWebServer mockWebServer;
    private WadaSanzoService wadaSanzoService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        wadaSanzoService = new WadaSanzoService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("The call to microservice should return classic palette")
    void shouldReturnWadaPaletteSuccessfully() throws InterruptedException {
        String mockResponse = """
                {
                    "original_color": "#00ff00",
                    "wada_name": "Dull Viridian Green",
                    "wada_hex": "#009465",
                    "combinations": [["#cb2f43","#59256a"], ["#eea78c", "#f37420", "#111314"]]
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse));

        WadaColorDTO result = wadaSanzoService.getWadaPalette("#00ff00");

        assertNotNull(result);
        assertEquals("#00ff00", result.originalColor());
        assertEquals("Dull Viridian Green", result.wadaName());
        assertEquals("#009465", result.wadaHex());
        assertEquals("[[#cb2f43, #59256a], [#eea78c, #f37420, #111314]]", result.combinations().toString());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/palette?hex=%2300ff00", recordedRequest.getPath());
    }

    @Test
    @DisplayName("Should raise a PythonAnalysisException if server sends error.")
    void shouldRaisePythonAnalysisExceptionIfServerError() {
        String mockResponse = """
                {
                    "status": "error",
                    "type": "AppError",
                    "message": "Error during distance calculation: invalid HEX colors."
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mockResponse));

        PythonAnalysisException exception = assertThrows(
                PythonAnalysisException.class,
                () -> wadaSanzoService.getWadaPalette("invalidHex"));

        assertEquals("Error during distance calculation: invalid HEX colors.", exception.getMessage());
        assertEquals("AppError", exception.getType());
        assertEquals(400, exception.getStatusCode());
    }
}

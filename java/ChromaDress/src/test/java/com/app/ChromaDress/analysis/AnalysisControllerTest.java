package com.app.ChromaDress.analysis;

import com.app.ChromaDress.auth.JwtFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtFilter jwtFilter;
    @MockitoBean
    private AnalysisFacade analysisFacade;

    @Test
    @DisplayName("Should return an AnalysisDTO when an image is uploaded.")
    void shouldReturnAnalysisDTO() throws Exception {
        AnalysisDTO mockResult = new AnalysisDTO("#ff0000", "dress");
        when(analysisFacade.analyzeImage(any())).thenReturn(mockResult);

        MockMultipartFile mockImage = new MockMultipartFile(
                "image",
                "fake_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake_content".getBytes());

        mockMvc.perform(multipart("/api/analysis/detect")
                        .file(mockImage))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("#ff0000"))
                .andExpect(jsonPath("$.category").value("dress"));
    }

    @Test
    @DisplayName("Should raise a HandlerMethodValidationException if file is empty.")
    void shouldRaiseHandlerMethodValidationExceptionIfEmpty() throws Exception {
        MockMultipartFile mockImage = new MockMultipartFile(
                "image",
                "fake_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "".getBytes());

        mockMvc.perform(multipart("/api/analysis/detect")
                        .file(mockImage))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid image file. Only PNG and JPEG images are supported."))
                .andExpect(jsonPath("$.type").value("HandlerMethodValidationException"));
    }

    @Test
    @DisplayName("Should raise a HandlerMethodValidationException if file is not an image.")
    void shouldRaiseHandlerMethodValidationExceptionIfNotImage() throws Exception {
        MockMultipartFile mockImage = new MockMultipartFile(
                "image",
                "fake_image.png",
                MediaType.APPLICATION_PDF_VALUE,
                "fake_pdf".getBytes());

        mockMvc.perform(multipart("/api/analysis/detect")
                        .file(mockImage))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid image file. Only PNG and JPEG images are supported."))
                .andExpect(jsonPath("$.type").value("HandlerMethodValidationException"));
    }
}

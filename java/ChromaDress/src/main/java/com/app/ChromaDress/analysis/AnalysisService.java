package com.app.ChromaDress.analysis;

import com.app.ChromaDress.core.exception.PythonAnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisService {

  private final WebClient webClient;

  public AnalysisDTO analyzeImage(MultipartFile file) {

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", file.getResource());

    return webClient.post().uri("/analyze").contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromMultipartData(builder.build()))
        .retrieve().onStatus(HttpStatusCode::isError,
            response -> response.bodyToMono(Map.class).flatMap(errorBody -> {
              String message = (String) errorBody.getOrDefault("message",
                  "Analysis service momentarily unavailable.");
              String type = (String) errorBody.getOrDefault("type", "AnalysisServiceError");
              int statusCode = response.statusCode().value();
              return Mono.<Throwable>error(new PythonAnalysisException(message, type, statusCode));
            }).switchIfEmpty(Mono.error(new PythonAnalysisException(
                "The server did not respond correctly. Please try again in a few moments.",
                "ServiceUnavailable", response.statusCode().value()))))
        .bodyToMono(AnalysisDTO.class).block();
  }
}

package com.app.ChromaDress.analysis;

import com.app.ChromaDress.core.exception.PythonAnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WadaSanzoService {

  private final WebClient webClient;

  @Cacheable(value = "wadaPalettes", key = "#hex")
  public WadaColorDTO getWadaPalette(String hex) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder.path("/palette").queryParam("hex", hex).build())
        .accept(MediaType.APPLICATION_JSON).retrieve().onStatus(HttpStatusCode::isError,
            response -> response.bodyToMono(Map.class).flatMap(errorBody -> {
              String message = (String) errorBody.getOrDefault("message",
                  "Analysis service momentarily unavailable.");
              String type = (String) errorBody.getOrDefault("type", "AnalysisServiceError");
              int statusCode = response.statusCode().value();
              return Mono.<Throwable>error(new PythonAnalysisException(message, type, statusCode));
            }).switchIfEmpty(Mono.error(new PythonAnalysisException(
                "The server did not respond correctly. Please try again in a few moments.",
                "ServiceUnavailable", response.statusCode().value()))))
        .bodyToMono(WadaColorDTO.class).block();
  }
}

package com.app.ChromaDress.analysis;

import com.app.ChromaDress.core.validation.ValidImage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

  private final AnalysisFacade analysisFacade;

  @PostMapping("/detect")
  public ResponseEntity<AnalysisDTO> detectColorAndCategory(
      @ValidImage @RequestParam("image") MultipartFile image) {
    AnalysisDTO result = analysisFacade.analyzeImage(image);
    return ResponseEntity.ok(result);
  }
}
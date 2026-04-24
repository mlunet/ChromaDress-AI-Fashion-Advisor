package com.app.ChromaDress.analysis;

import com.app.ChromaDress.core.validation.ValidImage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisFacade analysisFacade;

    @PostMapping("/detect")
    public ResponseEntity<AnalysisDTO> detectColorAndCategory(@ValidImage @RequestParam("image") MultipartFile image) {
        AnalysisDTO result = analysisFacade.analyzeImage(image);
        return ResponseEntity.ok(result);
    }
}
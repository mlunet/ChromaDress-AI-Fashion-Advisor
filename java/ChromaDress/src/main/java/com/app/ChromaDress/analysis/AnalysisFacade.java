package com.app.ChromaDress.analysis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisFacade {

  private final AnalysisService analysisService;
  private final WadaSanzoService wadaSanzoService;
  private final ColorService colorService;

  public AnalysisDTO analyzeImage(MultipartFile file) {
    return analysisService.analyzeImage(file);
  }

  public List<List<String>> getSuggestedPalette(String hexColor, PaletteType type) {
    if (type == PaletteType.WADA) {
      return wadaSanzoService.getWadaPalette(hexColor).combinations();
    }
    return colorService.getClassicPalette(hexColor).suggestions();
  }
}

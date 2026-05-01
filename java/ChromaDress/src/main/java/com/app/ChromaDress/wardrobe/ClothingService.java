package com.app.ChromaDress.wardrobe;

import com.app.ChromaDress.analysis.*;
import com.app.ChromaDress.core.file.FileService;
import com.app.ChromaDress.core.exception.ResourceNotFoundException;
import com.app.ChromaDress.core.utils.SecurityUtils;
import com.app.ChromaDress.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClothingService {

  private final ClothingRepository clothingRepository;
  private final ClothingItemMapper mapper;
  private final FileService fileService;
  private final AnalysisFacade analysisFacade;
  private final SecurityUtils securityUtils;

  public ClothingResponseDTO saveItem(ClothingRequestDTO dto) throws IOException {
    User user = securityUtils.getUserPrincipal();
    AnalysisDTO aiResult = analysisFacade.analyzeImage(dto.image());
    String fileName = fileService.saveFile(dto.image());

    ClothingItem item = mapper.RequestDtoToEntity(dto, user);
    item.setImageUrl(fileName);
    item.setHexAndRgb(aiResult.color());
    item.setHslFromHex(aiResult.color());

    ClothingItem saved = clothingRepository.save(item);
    return mapper.EntityToResponseDto(saved);
  }

  public List<ClothingResponseDTO> getUserCloset() {
    User user = securityUtils.getUserPrincipal();

    return clothingRepository.findByUserId(user.getId()).stream().map(mapper::EntityToResponseDto)
        .toList();
  }

  public List<OutfitSuggestionDTO> getOutfitSuggestions(Long clothingId, PaletteType type) {
    ClothingItem selectedItem = clothingRepository.findById(clothingId)
        .orElseThrow(() -> new ResourceNotFoundException("Clothing not found."));

    List<List<String>> allPalettes = analysisFacade.getSuggestedPalette(selectedItem.getHexColor(),
        type);

    List<OutfitSuggestionDTO> finalSuggestions = new ArrayList<>();

    for (int i = 0; i < allPalettes.size(); i++) {
      List<String> palette = allPalettes.get(i);
      List<ClothingItem> foundInWardrobe = new ArrayList<>();

      for (String hexSuggestion : palette) {
        String formattedHex = hexSuggestion.startsWith("#") ? hexSuggestion : "#" + hexSuggestion;
        float[] hsl = new float[3];
        Color color = Color.decode(formattedHex);
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsl);

        float h = hsl[0] * 360;
        float s = hsl[1] * 100;
        float l = hsl[2] * 100;

        List<ClothingItem> matches = clothingRepository.findSimilarHSL(h, s, l, clothingId,
            selectedItem.getCategory());

        if (!matches.isEmpty()) {
          ClothingItem bestMatch = matches.getFirst();

          if (!foundInWardrobe.contains(bestMatch)) {
            foundInWardrobe.add(bestMatch);
          }
        }
      }

      List<ClothingResponseDTO> foundInWardrobeDTO = foundInWardrobe.stream()
          .map(mapper::EntityToResponseDto).toList();

      String title = (type == PaletteType.WADA) ? "Wada combination " + (i + 1) : "Classic Harmony";
      finalSuggestions.add(new OutfitSuggestionDTO(title, palette, foundInWardrobeDTO));
    }
    return finalSuggestions;
  }

  @Transactional
  @Caching(evict = {@CacheEvict(value = "wadaPalettes", key = "#result"),
      @CacheEvict(value = "classicPalettes", key = "#result")})
  public String deleteItem(Long clothingId) {
    User user = securityUtils.getUserPrincipal();

    ClothingItem item = clothingRepository.findByIdAndUserId(clothingId, user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Clothing item not found."));

    String hexToEvict = item.getHexColor();
    fileService.deleteFile(item.getImageUrl());
    clothingRepository.delete(item);

    return hexToEvict;
  }
}

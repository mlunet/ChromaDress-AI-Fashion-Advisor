package com.app.ChromaDress.wardrobe;

import java.util.List;

public record OutfitSuggestionDTO(String combinationId, List<String> paletteHex,
                                  List<ClothingResponseDTO> matchingClothes) {

}

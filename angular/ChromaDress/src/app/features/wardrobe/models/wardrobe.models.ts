export interface ClothingRequestDTO {
  readonly name: string;
  readonly category: string;
  readonly image: File;
}

export interface ClothingResponseDTO {
  readonly id: number;
  readonly name: string;
  readonly category: string;
  readonly color: string;
  readonly imageUrl: string;
}

export interface OutfitSuggestionDTO {
  readonly combinationId: string;
  readonly paletteHex: string[];
  readonly matchingClothes: ClothingResponseDTO[];
}

export enum PaletteType {
  WADA = 'WADA',
  CLASSIC = 'CLASSIC',
}

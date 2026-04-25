import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ClothingService } from '../../services/clothing.service';
import { OutfitSuggestionDTO, PaletteType } from '../../models/wardrobe.models';
import { ClothingUploadComponent } from '../clothing-upload/clothing-upload.component';

@Component({
  selector: 'app-wardrobe-home',
  imports: [ClothingUploadComponent],
  templateUrl: './wardrobe-home.component.html',
  styleUrl: './wardrobe-home.component.css',
})
export class WardrobeHomeComponent implements OnInit {
  public clothingService = inject(ClothingService);

  isUploadModalOpen = signal(false);
  activeSuggestions = signal<OutfitSuggestionDTO[]>([]);
  isLoading = signal(false);
  selectedPaletteType = signal<PaletteType>(PaletteType.CLASSIC);
  selectedCategory = signal<string>('ALL');
  searchQuery = signal<string>('');

  filteredItems = computed(() => {
    const allItems = this.clothingService.items();
    const category = this.selectedCategory();
    const query = this.searchQuery().toLowerCase();

    return allItems.filter((item) => {
      const matchesCategory = category === 'ALL' || item.category === category;
      const matchesSearch = item.name.toLowerCase().includes(query);
      return matchesCategory && matchesSearch;
    });
  });

  ngOnInit(): void {
    this.clothingService.loadCloset().subscribe({
      error: (err) =>
        console.error($localize`:@@wardrobe.errorLoading:Error during wardrobe loading.`, err),
    });
  }

  openUploadModal() {
    this.isUploadModalOpen.set(true);
  }

  closeUploadModal() {
    this.isUploadModalOpen.set(false);
  }

  onDelete(id: number): void {
    const confirmMessage = $localize`:@@wardrobe.confirmDelete:Are you sure you want to delete this item?`;
    if (confirm(confirmMessage)) {
      this.clothingService.deleteItem(id).subscribe();
    }
  }

  onGetSuggestions(id: number): void {
    this.isLoading.set(true);
    this.clothingService.getOutfitSuggestions(id, this.selectedPaletteType()).subscribe({
      next: (suggestions) => {
        this.activeSuggestions.set(suggestions);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error($localize`:@@wardrobe.errorSuggestions:Error getting suggestions.`, err);
        this.isLoading.set(false);
      },
    });
  }

  setPaletteType(type: 'WADA' | 'CLASSIC') {
    this.selectedPaletteType.set(type as PaletteType);
  }

  closeSuggestions() {
    this.activeSuggestions.set([]);
  }
}

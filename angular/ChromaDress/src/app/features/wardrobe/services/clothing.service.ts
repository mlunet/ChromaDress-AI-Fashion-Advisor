import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { API_CONFIG } from '../../../core/config/api.config';
import { ClothingResponseDTO, OutfitSuggestionDTO, PaletteType } from '../models/wardrobe.models';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ClothingService {
  private readonly http = inject(HttpClient);
  private readonly URL = `${API_CONFIG.baseUrl}${API_CONFIG.clothing.base}`;

  private itemsSignal = signal<ClothingResponseDTO[]>([]);
  public readonly items = this.itemsSignal.asReadonly();

  uploadItem(name: string, category: string, image: File): Observable<ClothingResponseDTO> {
    const formData = new FormData();
    formData.append('name', name);
    formData.append('category', category);
    formData.append('image', image);

    return this.http.post<ClothingResponseDTO>(`${this.URL}/upload`, formData).pipe(
      tap((newItem) => {
        this.itemsSignal.update((current) => [...current, newItem]);
      }),
    );
  }

  loadCloset(): Observable<ClothingResponseDTO[]> {
    return this.http
      .get<ClothingResponseDTO[]>(`${this.URL}/my-closet`)
      .pipe(tap((data) => this.itemsSignal.set(data)));
  }

  getOutfitSuggestions(id: number, type: PaletteType): Observable<OutfitSuggestionDTO[]> {
    const params = new HttpParams().set('type', type);
    return this.http.get<OutfitSuggestionDTO[]>(`${this.URL}/${id}/recommendations`, {
      params,
    });
  }

  deleteItem(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.URL}/${id}`)
      .pipe(tap(() => this.itemsSignal.update((current) => current.filter((i) => i.id !== id))));
  }
}

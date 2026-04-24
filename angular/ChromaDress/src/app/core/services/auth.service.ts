import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { API_CONFIG } from '../config/api.config';
import { AuthResponseDTO, LoginDTO, RegistrationDTO } from '../models/auth.models';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly URL = `${API_CONFIG.baseUrl}${API_CONFIG.auth.base}`;

  private authState = signal<string | null>(localStorage.getItem('access_token'));

  public readonly isAuthenticated = computed(() => !!this.authState());

  login(credentials: LoginDTO): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.URL}/login`, credentials).pipe(
      tap((response) => {
        this.saveTokens(response.accessToken, response.refreshToken);
        this.authState.set(response.accessToken);
      }),
    );
  }

  register(data: RegistrationDTO): Observable<any> {
    return this.http.post<AuthResponseDTO>(`${this.URL}/register`, data).pipe(
      tap((response) => {
        this.saveTokens(response.accessToken, response.refreshToken);
        this.authState.set(response.accessToken);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    this.authState.set(null);
  }

  refreshToken(): Observable<AuthResponseDTO> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<AuthResponseDTO>(`${this.URL}/refresh`, { refreshToken }).pipe(
      tap((response) => {
        this.saveTokens(response.accessToken, response.refreshToken);
        this.authState.set(response.accessToken);
      }),
    );
  }

  public getToken(): string | null {
    return this.authState();
  }

  public getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token');
  }

  private saveTokens(access: string, refresh: string): void {
    localStorage.setItem('access_token', access);
    localStorage.setItem('refresh_token', refresh);
  }
}

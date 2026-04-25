import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  public authService = inject(AuthService);
  private router = inject(Router);
  protected currentLang = window.location.pathname.split('/')[1] || 'en-US';

  switchLang(lang: string) {
    if (lang === this.currentLang) return;

    const pathParts = window.location.pathname.split('/');
    pathParts[1] = lang;

    const newPath = pathParts.join('/');
    window.location.href = newPath;
  }

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}

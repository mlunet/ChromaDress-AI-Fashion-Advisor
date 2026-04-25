import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./features/auth/components/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full',
  },
  {
    path: 'auth/register',
    loadComponent: () =>
      import('./features/auth/components/register/register.component').then(
        (m) => m.RegisterComponent,
      ),
  },
  {
    path: 'wardrobe',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/wardrobe/pages/wardrobe-home/wardrobe-home.component').then(
        (m) => m.WardrobeHomeComponent,
      ),
  },
  {
    path: '**',
    canActivate: [authGuard],
    redirectTo: 'wardrobe',
    pathMatch: 'full',
  },
];

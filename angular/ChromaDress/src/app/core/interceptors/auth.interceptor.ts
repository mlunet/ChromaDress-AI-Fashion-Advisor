import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, Observable, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  let authReq = req;

  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status == 401 && !req.url.includes('/login') && !req.url.includes('/refresh')) {
        return handle401RetryPattern(authReq, next, authService);
      }
      return throwError(() => error);
    }),
  );
};

const handle401RetryPattern = (req: HttpRequest<unknown>, next: HttpHandlerFn, authService: AuthService): Observable<HttpEvent<unknown>> => {
  return authService.refreshToken().pipe(
    switchMap((res) => {
      const newRequest = req.clone({
        setHeaders: {
          Authorization: `Bearer ${res.accessToken}`,
        },
      });
      return next(newRequest);
    }),
    catchError((refreshError) => {
      authService.logout();
      return throwError(() => refreshError);
    }),
  );
};

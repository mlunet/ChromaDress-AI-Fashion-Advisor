import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ErrorResponse } from '../models/error-response.model';
import { inject } from '@angular/core';
import { HotToastService } from '@ngxpert/hot-toast';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(HotToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occured.';

      if (error.error && typeof error.error === 'object') {
        const serverError = error.error as ErrorResponse;
        errorMessage = serverError.message || errorMessage;
      }

      if (error.status !== 401 && error.status !== 0) {
        toast.error(errorMessage, {
          dismissible: true,
        });
      }

      return throwError(() => ({
        ...error,
        friendlyMessage: errorMessage,
      }));
    }),
  );
};

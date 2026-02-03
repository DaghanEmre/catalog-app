import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    const token = auth.getToken();
    const isApi = req.url.startsWith('/api');

    if (token && isApi) {
        req = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
    }

    return next(req).pipe(
        catchError((err) => {
            if (err?.status === 401) {
                auth.logout();
                router.navigate(['/login']);
            }
            return throwError(() => err);
        })
    );
};

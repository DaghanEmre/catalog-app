import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, User } from '../../shared/models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly TOKEN_KEY = 'auth_token';
    private readonly USER_KEY = 'auth_user';

    private readonly currentUserSubject = new BehaviorSubject<User | null>(this.readUser());
    readonly currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient) { }

    login(payload: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>('/api/auth/login', payload).pipe(
            tap((res) => {
                sessionStorage.setItem(this.TOKEN_KEY, res.token);
                const user: User = { username: res.username, role: res.role };
                sessionStorage.setItem(this.USER_KEY, JSON.stringify(user));
                this.currentUserSubject.next(user);
            })
        );
    }

    logout(): void {
        sessionStorage.removeItem(this.TOKEN_KEY);
        sessionStorage.removeItem(this.USER_KEY);
        this.currentUserSubject.next(null);
    }

    getToken(): string | null {
        return sessionStorage.getItem(this.TOKEN_KEY);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    getCurrentUser(): User | null {
        return this.currentUserSubject.value;
    }

    isAdmin(): boolean {
        const u = this.getCurrentUser();
        return u?.role === 'ROLE_ADMIN';
    }

    private readUser(): User | null {
        const raw = sessionStorage.getItem(this.USER_KEY);
        try {
            return raw ? (JSON.parse(raw) as User) : null;
        } catch {
            return null;
        }
    }
}

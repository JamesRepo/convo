import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import {
    User,
    AuthenticationRequest,
    AuthenticationResponse,
    RegisterRequest
} from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly API_URL = `${environment.apiUrl}/auth`;
    private readonly TOKEN_KEY = 'auth_token';

    private currentUserSubject = new BehaviorSubject<User | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
    public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

    constructor(
        private http: HttpClient,
        private router: Router,
    ) {
        this.checkToken();
    }

    register(request: RegisterRequest): Observable<AuthenticationResponse> {
        return this.http.post<AuthenticationResponse>(`${this.API_URL}/register`, request)
            .pipe(
                tap(response => this.handleAuthentication(response))
            );
    }

    login(request: AuthenticationRequest): Observable<AuthenticationResponse> {
        return this.http.post<AuthenticationResponse>(`${this.API_URL}/login`, request)
            .pipe(
                tap(response => this.handleAuthentication(response))
            );
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    isTokenExpired(): boolean {
        const token = this.getToken();
        if (!token) {
            return true;
        }

        try {
            const decoded: any = jwtDecode(token);
            const expirationDate = decoded.exp * 1000;
            return Date.now() > expirationDate;
        } catch {
            return true;
        }
    }

    private handleAuthentication(response: AuthenticationResponse): void {
        localStorage.setItem(this.TOKEN_KEY, response.token);

        const user: User = {
            id: response.userId,
            username: response.username,
            email: response.email,
            status: 'ONLINE'
        };

        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
    }

    private checkToken(): void {
        const token = this.getToken();
        if (token && !this.isTokenExpired()) {
            // Optionally fetch current user data from API
            this.isAuthenticatedSubject.next(true);
        }
    }

    getCurrentUsername(): string | null {
        const token = this.getToken();

        if (!token) {
            return null;
        }

        try {
            const decoded: any = jwtDecode(token);
            return decoded.sub;
        } catch {
            return null;
        }
    }
}
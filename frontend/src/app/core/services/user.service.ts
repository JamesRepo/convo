import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly API_URL = `${environment.apiUrl}/users`;

    private onlineUsersSubject = new BehaviorSubject<User[]>([]);
    public onlineUsers$ = this.onlineUsersSubject.asObservable();

    constructor(private http: HttpClient) {}

    getCurrentUser(): Observable<User> {
        return this.http.get<User>(`${this.API_URL}/me`);
    }

    getOnlineUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.API_URL}/online`)
            .pipe(
                tap(users => this.onlineUsersSubject.next(users))
            );
    }

    updateProfile(user: Partial<User>): Observable<User> {
        return this.http.put<User>(`${this.API_URL}/me`, user);
    }

    updateStatus(status: 'ONLINE' | 'OFFLINE' | 'AWAY'): Observable<void> {
        return this.http.put<void>(`${this.API_URL}/status`, { status });
    }
}
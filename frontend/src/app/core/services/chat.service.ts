import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ChatRoom } from '../models/chat-room.model';
import { ChatMessage } from '../models/message.model';
import { environment } from '../../../environments/environment';

export interface PagedMessages {
    content: ChatMessage[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

@Injectable({
    providedIn: 'root'
})
export class ChatService {
    private readonly API_URL = `${environment.apiUrl}/chat`;

    private chatRoomsSubject = new BehaviorSubject<ChatRoom[]>([]);
    public chatRooms$ = this.chatRoomsSubject.asObservable();

    private currentRoomSubject = new BehaviorSubject<ChatRoom | null>(null);
    public currentRoom$ = this.currentRoomSubject.asObservable();

    private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
    public messages$ = this.messagesSubject.asObservable();

    constructor(private http: HttpClient) {}

    loadChatRooms(): Observable<ChatRoom[]> {
        return this.http.get<ChatRoom[]>(`${this.API_URL}/rooms`)
            .pipe(
                tap(rooms => this.chatRoomsSubject.next(rooms))
            );
    }

    getChatRoom(roomId: number): Observable<ChatRoom> {
        return this.http.get<ChatRoom>(`${this.API_URL}/room/${roomId}`)
            .pipe(
                tap(room => this.currentRoomSubject.next(room))
            );
    }

    loadMessages(roomId: number, page: number = 0, size: number = 50): Observable<PagedMessages> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<PagedMessages>(`${this.API_URL}/room/${roomId}/messages`, { params })
            .pipe(
                tap(pagedMessages => {
                    // Convert timestamp strings to Date objects
                    const messages = pagedMessages.content.map(msg => ({
                        ...msg,
                        timestamp: new Date(msg.timestamp)
                    }));

                    // For pagination, prepend older messages
                    if (page === 0) {
                        this.messagesSubject.next(messages.reverse());
                    } else {
                        const currentMessages = this.messagesSubject.value;
                        this.messagesSubject.next([...messages.reverse(), ...currentMessages]);
                    }
                })
            );
    }

    searchMessages(roomId: number, keyword: string): Observable<ChatMessage[]> {
        const params = new HttpParams().set('keyword', keyword);
        return this.http.get<ChatMessage[]>(`${this.API_URL}/room/${roomId}/search`, { params });
    }

    createChatRoom(name: string, description: string): Observable<ChatRoom> {
        return this.http.post<ChatRoom>(`${this.API_URL}/room`, { name, description })
            .pipe(
                tap(room => {
                    const currentRooms = this.chatRoomsSubject.value;
                    this.chatRoomsSubject.next([...currentRooms, room]);
                })
            );
    }

    updateChatRoom(roomId: number, name: string, description: string): Observable<ChatRoom> {
        return this.http.put<ChatRoom>(`${this.API_URL}/room/${roomId}`, { name, description })
            .pipe(
                tap(updatedRoom => {
                    const currentRooms = this.chatRoomsSubject.value;
                    const updatedRooms = currentRooms.map(room =>
                        room.id === roomId ? updatedRoom : room
                    );
                    this.chatRoomsSubject.next(updatedRooms);
                })
            );
    }

    deleteChatRoom(roomId: number): Observable<void> {
        return this.http.delete<void>(`${this.API_URL}/room/${roomId}`)
            .pipe(
                tap(() => {
                    const currentRooms = this.chatRoomsSubject.value;
                    const updatedRooms = currentRooms.filter(room => room.id !== roomId);
                    this.chatRoomsSubject.next(updatedRooms);
                    // Clear current room if it was deleted
                    const currentRoom = this.currentRoomSubject.value;
                    if (currentRoom && currentRoom.id === roomId) {
                        this.currentRoomSubject.next(null);
                    }
                })
            );
    }

    markMessageAsRead(messageId: number): Observable<void> {
        return this.http.post<void>(`${this.API_URL}/messages/${messageId}/read`, {});
    }

    addMessage(message: ChatMessage): void {
        const currentMessages = this.messagesSubject.value;
        this.messagesSubject.next([...currentMessages, message]);
    }

    clearMessages(): void {
        this.messagesSubject.next([]);
    }

    setCurrentRoom(room: ChatRoom | null): void {
        this.currentRoomSubject.next(room);
    }

    getCurrentRoom(): ChatRoom | null {
        return this.currentRoomSubject.value;
    }
}
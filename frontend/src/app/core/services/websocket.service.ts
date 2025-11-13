import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, filter } from 'rxjs';
import { Client, StompSubscription, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { AuthService } from './auth.service';
import { ChatMessage } from '../models/message.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class WebSocketService {
    private stompClient: Client | null = null;
    private messageSubject = new BehaviorSubject<ChatMessage | null>(null);
    private typingSubject = new BehaviorSubject<ChatMessage | null>(null);
    private connectionSubject = new BehaviorSubject<boolean>(false);

    public message$ = this.messageSubject.asObservable().pipe(
        filter(msg => msg !== null)
    ) as Observable<ChatMessage>;

    public typing$ = this.typingSubject.asObservable().pipe(
        filter(msg => msg !== null)
    ) as Observable<ChatMessage>;

    public connected$ = this.connectionSubject.asObservable();

    private subscriptions: Map<string, StompSubscription> = new Map();

    constructor(private authService: AuthService) {}

    connect(): void {
        if (this.stompClient?.connected) {
            return;
        }

        const token = this.authService.getToken();
        if (!token) {
            console.error('No token available for WebSocket connection');
            return;
        }

        this.stompClient = new Client({
            webSocketFactory: () => new SockJS(`${environment.wsUrl}`),
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            debug: (str) => {
                console.debug('STOMP: ' + str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.stompClient.onConnect = () => {
            this.connectionSubject.next(true);
        };

        this.stompClient.onStompError = (frame) => {
            console.error('STOMP error:', frame);
            this.connectionSubject.next(false);
        };

        this.stompClient.onWebSocketClose = () => {
            this.connectionSubject.next(false);
        };

        this.stompClient.activate();
    }

    disconnect(): void {
        if (this.stompClient) {
            this.subscriptions.forEach(sub => sub.unsubscribe());
            this.subscriptions.clear();
            this.stompClient.deactivate();
            this.connectionSubject.next(false);
        }
    }

    subscribeToRoom(roomId: number): void {
        if (!this.stompClient?.connected) {
            console.error('Cannot subscribe: WebSocket not connected');
            return;
        }

        const messageDestination = `/topic/room/${roomId}`;
        const typingDestination = `/topic/typing/${roomId}`;

        // Unsubscribe from previous room if exists
        if (this.subscriptions.has(`message-${roomId}`) || this.subscriptions.has(`typing-${roomId}`)) {
            this.unsubscribeFromRoom(roomId);
        }

        // Subscribe to messages
        const messageSub = this.stompClient.subscribe(
            messageDestination,
            (message: IMessage) => {
                const chatMessage: ChatMessage = JSON.parse(message.body);
                chatMessage.timestamp = new Date(chatMessage.timestamp);
                this.messageSubject.next(chatMessage);
            }
        );
        this.subscriptions.set(`message-${roomId}`, messageSub);

        // Subscribe to typing indicators
        const typingSub = this.stompClient.subscribe(
            typingDestination,
            (message: IMessage) => {
                const chatMessage: ChatMessage = JSON.parse(message.body);
                this.typingSubject.next(chatMessage);
            }
        );
        this.subscriptions.set(`typing-${roomId}`, typingSub);

        // Send join message
        this.sendJoinMessage(roomId);
    }

    unsubscribeFromRoom(roomId: number): void {
        const messageSub = this.subscriptions.get(`message-${roomId}`);
        const typingSub = this.subscriptions.get(`typing-${roomId}`);

        if (messageSub) {
            messageSub.unsubscribe();
            this.subscriptions.delete(`message-${roomId}`);
        }

        if (typingSub) {
            typingSub.unsubscribe();
            this.subscriptions.delete(`typing-${roomId}`);
        }

        // Send leave message
        this.sendLeaveMessage(roomId);
    }

    sendMessage(roomId: number, content: string): void {
        if (!this.stompClient?.connected) {
            console.error('Cannot send message: WebSocket not connected');
            return;
        }

        const message: Partial<ChatMessage> = {
            content: content,
            chatRoomId: roomId,
            senderUsername: this.authService.getCurrentUsername() || '',
            type: 'CHAT' as any
        };

        this.stompClient.publish({
            destination: `/app/chat/${roomId}`,
            body: JSON.stringify(message),
        });
    }

    sendTypingIndicator(roomId: number): void {
        if (!this.stompClient?.connected) {
            return;
        }

        const message: Partial<ChatMessage> = {
            chatRoomId: roomId,
            senderUsername: this.authService.getCurrentUsername() || '',
            type: 'TYPING' as any
        };

        this.stompClient.publish({
            destination: `/app/typing/${roomId}`,
            body: JSON.stringify(message)
        });
    }

    private sendJoinMessage(roomId: number): void {
        if (!this.stompClient?.connected) {
            return;
        }

        this.stompClient.publish({
            destination: `/app/join/${roomId}`,
            body: JSON.stringify({})
        });
    }

    private sendLeaveMessage(roomId: number): void {
        if (!this.stompClient?.connected) {
            return;
        }

        this.stompClient.publish({
            destination: `/app/leave/${roomId}`,
            body: JSON.stringify({})
        });
    }

    isConnected(): boolean {
        return this.stompClient?.connected || false;
    }
}
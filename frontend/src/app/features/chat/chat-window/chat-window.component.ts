import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, takeUntil } from 'rxjs';
import { ChatService } from '../../../core/services/chat.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { ChatRoom } from '../../../core/models/chat-room.model';
import { ChatMessage } from '../../../core/models/message.model';
import { MessageListComponent } from '../message-list/message-list.component';
import { MessageInputComponent } from '../message-input/message-input.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-chat-window',
    standalone: true,
    imports: [
        CommonModule,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        MessageListComponent,
        MessageInputComponent
    ],
    templateUrl: './chat-window.component.html',
    styleUrls: ['./chat-window.component.scss']
})
export class ChatWindowComponent implements OnInit, OnDestroy {
    currentRoom: ChatRoom | null = null;
    messages: ChatMessage[] = [];
    loading = false;
    typingUsers: Set<string> = new Set();
    typingMessage = '';
    askingOracle = false;

    private destroy$ = new Subject<void>();
    private typingTimeouts = new Map<string, any>();
    private currentUsername: string | null;

    constructor(
        private chatService: ChatService,
        private webSocketService: WebSocketService,
        private authService: AuthService
    ) {
        this.currentUsername = this.authService.getCurrentUsername();
    }

    ngOnInit(): void {
        // Subscribe to current room changes
        this.chatService.currentRoom$
            .pipe(takeUntil(this.destroy$))
            .subscribe(room => {
                if (room && room.id !== this.currentRoom?.id) {
                    this.loadRoom(room);
                }
            });

        // Subscribe to messages
        this.chatService.messages$
            .pipe(takeUntil(this.destroy$))
            .subscribe(messages => {
                this.messages = messages;
            });

        // Subscribe to WebSocket messages
        this.webSocketService.message$
            .pipe(takeUntil(this.destroy$))
            .subscribe(message => {
                this.handleIncomingMessage(message);
            });

        // Subscribe to typing indicators
        this.webSocketService.typing$
            .pipe(takeUntil(this.destroy$))
            .subscribe(message => {
                this.handleTypingIndicator(message);
            });

        // Connect WebSocket if not already connected
        if (!this.webSocketService.isConnected()) {
            this.webSocketService.connect();
        }
    }

    ngOnDestroy(): void {
        // Unsubscribe from current room
        if (this.currentRoom) {
            this.webSocketService.unsubscribeFromRoom(this.currentRoom.id);
        }

        this.destroy$.next();
        this.destroy$.complete();
    }

    private loadRoom(room: ChatRoom): void {
        this.loading = true;

        // Unsubscribe from previous room
        if (this.currentRoom) {
            this.webSocketService.unsubscribeFromRoom(this.currentRoom.id);
        }

        this.currentRoom = room;
        this.chatService.clearMessages();

        // Load messages
        this.chatService.loadMessages(room.id).subscribe({
            next: () => {
                this.loading = false;
                // Subscribe to new room
                this.webSocketService.subscribeToRoom(room.id);
            },
            error: (error) => {
                console.error('Error loading messages:', error);
                this.loading = false;
            }
        });
    }

    sendMessage(content: string): void {
        if (this.currentRoom && content.trim()) {
            this.webSocketService.sendMessage(this.currentRoom.id, content);
        }
    }

    onTyping(): void {
        if (this.currentRoom) {
            this.webSocketService.sendTypingIndicator(this.currentRoom.id);
        }
    }

    private handleIncomingMessage(message: ChatMessage): void {
        if (message.chatRoomId === this.currentRoom?.id) {
            this.chatService.addMessage(message);
        }
    }

    askOracle(): void {
        if (!this.currentRoom || this.askingOracle) {
            return;
        }

        this.askingOracle = true;
        this.chatService.askOracle(this.currentRoom.id).subscribe({
            next: (message) => {
                // In case the WebSocket is delayed, ensure the message is added locally once.
                message.timestamp = new Date(message.timestamp);
                const exists = this.messages.some(m => m.id === message.id);
                if (!exists && message.chatRoomId === this.currentRoom?.id) {
                    this.chatService.addMessage(message);
                }
                this.askingOracle = false;
            },
            error: (error) => {
                console.error('Error asking the Oracle:', error);
                this.askingOracle = false;
            }
        });
    }

    private handleTypingIndicator(message: ChatMessage): void {
        if (message.senderUsername === this.currentUsername) {
            return; // Don't show own typing indicator
        }

        const username = message.senderUsername;
        this.typingUsers.add(username);
        this.updateTypingMessage();

        // Clear existing timeout
        if (this.typingTimeouts.has(username)) {
            clearTimeout(this.typingTimeouts.get(username));
        }

        // Set new timeout to remove typing indicator
        const timeout = setTimeout(() => {
            this.typingUsers.delete(username);
            this.updateTypingMessage();
            this.typingTimeouts.delete(username);
        }, 3000);

        this.typingTimeouts.set(username, timeout);
    }

    private updateTypingMessage(): void {
        const users = Array.from(this.typingUsers);

        if (users.length === 0) {
            this.typingMessage = '';
        } else if (users.length === 1) {
            this.typingMessage = `${users[0]} is typing...`;
        } else if (users.length === 2) {
            this.typingMessage = `${users[0]} and ${users[1]} are typing...`;
        } else {
            this.typingMessage = `${users[0]} and ${users.length - 1} others are typing...`;
        }
    }
}